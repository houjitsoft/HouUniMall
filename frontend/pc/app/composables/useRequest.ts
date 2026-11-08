/**
 * API 请求工具类
 *
 * 封装了 HTTP 请求方法，支持 GET、POST、PUT、DELETE 等常用请求
 * 使用 Nuxt 4 内置的 $fetch 工具，提供统一的请求配置和错误处理
 *
 * 不携带 token：在 get/post/put 等方法的 options 中传入 { skipToken: true }（如登录接口）
 */

import type { FetchOptions, FetchContext } from 'ofetch'
import { getToken, deleteToken } from '../utils/auth'
import { resolveClientApiBase, resolveServerApiBase } from '../utils/apiBase'
import type { ApiResponse } from '~/types/api'
import { resolveError, shouldRedirectLogin } from '../utils/errorCode'

/** 扩展请求配置，支持 skipToken（不携带 token，如登录接口） */
export type RequestOptions = FetchOptions & { skipToken?: boolean }

/**
 * 是否为「需要清除登录态」的认证失败。
 *
 * 过去是裸码数组 `[10002, 10086, 11002]`，而 11002 是跨枚举冲突码
 * （BizCodeEnume=用户不存在 / RiderCodeEnume=正在审核），码表.md 附录 B 明确本轮不认领，
 * 拿它清登录态会把业务错误误判成 token 失效。现在只看错误码语义：category === 'AUTH'。
 */
const isAuthFailure = (data: any): boolean => {
    if (!data || typeof data.code !== 'number') return false
    return shouldRedirectLogin(resolveError(data.code, { backendMessage: data.message }))
}

/**
 * 检查响应并清除用户信息（当 HTTP 状态为 401，或业务码按语义判为认证失败时）
 */
const clearUserIfNeeded = async (response: Response | undefined) => {
    if (!response || typeof response.clone !== 'function' || typeof window === 'undefined') return
    const shouldClear = response.status === 401
    if (shouldClear) {
        const { useUserStore } = await import('~/stores/modules/user')
        const userStore = useUserStore()
        userStore.clearUserInfo()
        deleteToken()
        return
    }
    try {
        const data = await response.clone().json()
        if (isAuthFailure(data)) {
            const { useUserStore } = await import('~/stores/modules/user')
            const userStore = useUserStore()
            userStore.clearUserInfo()
            deleteToken()
        }
    } catch {
        // 非 JSON 响应或解析失败时忽略
    }
}

// 重新导出类型，保持向后兼容
export type { ApiResponse }

/**
 * 获取请求配置
 * 
 * 从 Nuxt 运行时配置中获取 API 基础 URL、超时时间和重试次数
 */
const getRequestConfig = () => {
    const runtimeConfig = useRuntimeConfig()
    // 浏览器同源可走相对路径；SSR/预渲染必须有绝对地址，否则拉不到 mall_style.pc_floors。
    // 服务端兜底是站点绝对地址（siteUrl），不是 loopback —— 构建机上 8391 没有 BFF，
    // 用 loopback 兜底会把 fetch failed 的错误快照烤进预渲染 HTML（2026-10-29 线上故障）。
    const baseURL = import.meta.server
        ? resolveServerApiBase(runtimeConfig)
        : resolveClientApiBase(runtimeConfig)
    return {
        baseURL,
        timeout: runtimeConfig.public.apiTimeout,
        loginTimeout: runtimeConfig.public.apiLoginTimeout,
        retryCount: runtimeConfig.public.apiRetryCount
    }
}

/** 将历史 mall-* 服务路径统一收口到 bff-web，避免浏览器直连 PaaS。 */
const normalizeMallPath = (value: unknown): unknown => {
    if (typeof value !== 'string') return value
    const match = value.match(/^\/?mall-([a-z0-9-]+)(\/.*)?$/i)
    if (!match || value.startsWith('/web/')) return value
    const suffix = (match[2] || '').replace(/\/{2,}/g, '/')
    // 认证/第三方接口已由 BFF 提供明确的 Web 路由，不能再转发到已下线的 legacy 网关。
    if (match[1].toLowerCase() === 'third-party') {
        if (/\/sms\/(sendCode|sendVerificationCode)$/i.test(suffix)) return '/web/auth/send-code'
        if (/\/sms\/code\/threecheckphoneandsend$/i.test(suffix)) return '/web/auth/send-code'
        if (/\/web\/oss\/uploadFile$/i.test(suffix)) return '/web/thirdparty/oss/uploadFile'
        if (/\/web\/oss\/upload$/i.test(suffix)) return '/web/thirdparty/oss/uploadFile'
    }
    if (match[1].toLowerCase() === 'auth-server') {
        if (/getAuthorizationQRCode$/i.test(suffix)) return '/web/auth/wechat/qrcode'
        if (/checkAuthorizationStatus$/i.test(suffix)) return '/web/auth/wechat/login-status'
        if (/generate$/i.test(suffix)) return '/web/auth/generate'
        if (/verifyGenerateCaptcha$/i.test(suffix)) return '/web/auth/verifyGenerateCaptcha'
    }
    const aliases: Record<string, string> = {
        // Keep the historical service names accepted by the individual
        // service modules, but normalize them to the canonical BFF owners.
        'auth-server': 'auth', 'third-party': 'thirdparty',
        goods: 'goods', order: 'order', userms: 'userms', base: 'base',
        business: 'merchant', merchant: 'merchant', payment: 'payment',
        activity: 'activity', supplier: 'supplier', ai: 'ai',
        cms: 'cms', rider: 'rider', webpos: 'webpos', finance: 'finance'
    }
    const service = aliases[match[1].toLowerCase()]
    const routes: Record<string, string> = {
        base: '/web/base', auth: '/web/auth', thirdparty: '/web/thirdparty',
        goods: '/web/mall/goods', order: '/web/order', payment: '/web/pay',
        userms: '/web/user', merchant: '/web/merchant', activity: '/web/plugin/activity',
        cms: '/web/plugin/cms', rider: '/web/plugin/rider', supplier: '/web/plugin/supplier',
        webpos: '/web/plugin/webpos', finance: '/web/plugin/finance'
    }
    return service ? `${routes[service] || `/web/${service}`}${suffix}` : value
}

/**
 * 错误状态码对应的提示信息
 */
const errorMessages: Record<number, string> = {
    400: '请求参数错误，请检查您的输入',
    404: '请求的资源不存在',
    500: '服务器内部错误，请稍后重试',
    501: '服务器不支持该请求方法',
    503: '服务器暂时不可用，请稍后重试'
}

/**
 * 检查是否为表单数据类型请求
 * @param headers 请求头
 * @returns 是否为表单数据类型
 */
const isFormDataRequest = (headers: any): boolean => {
    if (!headers) return false

    const contentType = (headers instanceof Headers)
        ? (headers.get('Content-Type') || headers.get('content-type'))
        : (headers['Content-Type'] || headers['content-type'])

    return !!contentType?.includes('application/x-www-form-urlencoded')
}

/**
 * 显示错误提示
 * @param status 状态码
 * @param message 错误信息
 */
const showErrorToast = (status: number, message?: string) => {
    const defaultMessage = errorMessages[status] || '请求失败，请稍后重试'
    // 使用 Nuxt 的 useToast 或其他通知组件显示错误提示
    // 这里使用 console.error 作为默认实现，实际项目中应该使用 UI 组件库的通知功能
    console.error(`HTTP ${status}: ${message || defaultMessage}`)
}

/**
 * 创建 API 请求实例
 * 
 * 使用 $fetch.create 方法创建一个具有默认配置的请求实例
 */
const apiRequest = $fetch.create({
    headers: {
        'Content-Type': 'application/json'
    },
    async onRequest({ request, options }: FetchContext) {
        // 动态获取基础 URL（确保每次请求时获取最新配置）
        const requestConfig = getRequestConfig()
        options.baseURL = requestConfig.baseURL
        if (typeof request === 'string') {
            request = normalizeMallPath(request) as string
        }
        if (options.timeout == null) {
            options.timeout = requestConfig.timeout
        }
        // 是否跳过 token：由调用方通过 options.skipToken 指定（如登录接口）
        const opts = options as unknown as Record<string, unknown>
        const skipToken = !!opts.skipToken
        if (skipToken) delete opts.skipToken
        const token = skipToken ? null : getToken()
        const method = String(options.method || 'GET').toUpperCase()

        // FormData 请求：移除 Content-Type，让浏览器自动设置 multipart/form-data; boundary=...
        if (options.body instanceof FormData) {
            const headers = new Headers(options.headers as HeadersInit)
            headers.delete('Content-Type')
            if (token) {
                headers.set('token', token)
                headers.set('Authorization', `Bearer ${token}`)
            }
            options.headers = headers
        } else if (method === 'GET' || method === 'HEAD') {
            // GET 不应带 application/json，部分链路会因此排队/异常
            const headers = new Headers(options.headers as HeadersInit)
            headers.delete('Content-Type')
            if (token) {
                headers.set('token', token)
                headers.set('Authorization', `Bearer ${token}`)
            }
            options.headers = headers
        } else if (token) {
            // 非 FormData：添加 Token 到请求头
            const headers = new Headers(options.headers as HeadersInit)
            headers.set('token', token)
            // 下游 PAAS 统一从 Authorization 解析 JWT；保留 token 头兼容旧接口。
            headers.set('Authorization', `Bearer ${token}`)
            options.headers = headers
        }

        // 处理表单数据请求
        if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
            if (isFormDataRequest(options.headers)) {
                // 转换为 URLSearchParams 格式
                const searchParams = new URLSearchParams()
                Object.entries(options.body as Record<string, any>).forEach(([key, value]) => {
                    if (value !== undefined && value !== null) {
                        searchParams.append(key, String(value))
                    }
                })
                options.body = searchParams
            }
        }

        // 在请求参数中添加 token
        if (token) {
            if (options.method?.toUpperCase() === 'GET') {
                // GET 请求：在查询参数中添加 token
                if (typeof request === 'string') {
                    try {
                        let fullUrl: string
                        // 检查是否是完整 URL
                        if (request.startsWith('http://') || request.startsWith('https://')) {
                            fullUrl = request
                        } else {
                            // 相对路径，尝试结合 baseURL 构造完整 URL
                            const baseURL = options.baseURL || getRequestConfig().baseURL
                            if (baseURL && (baseURL.startsWith('http://') || baseURL.startsWith('https://'))) {
                                // baseURL 是完整 URL，可以安全地构造
                                fullUrl = new URL(request, baseURL).toString()
                            } else {
                                // baseURL 不存在或不是完整 URL，使用查询字符串方式添加 token
                                const separator = request.includes('?') ? '&' : '?'
                                request = `${request}${separator}token=${encodeURIComponent(token)}`
                                // 提前结束，避免后续的 URL 构造
                                return
                            }
                        }
                        // 使用 URL 对象添加 token 参数
                        const url = new URL(fullUrl)
                        url.searchParams.set('token', token)
                        request = url.toString()
                    } catch (error) {
                        // 如果 URL 构造失败，使用查询字符串方式添加 token
                        const separator = request.includes('?') ? '&' : '?'
                        request = `${request}${separator}token=${encodeURIComponent(token)}`
                    }
                }
            } else if (options.body && typeof options.body === 'object') {
                // 其他请求：在请求体中添加 token
                if (options.body instanceof FormData) {
                    options.body.append('token', token)
                } else if (!isFormDataRequest(options.headers)) {
                    // 对于 JSON 数据，直接在请求体中添加 token
                    options.body = {
                        ...(options.body as Record<string, any>),
                        token
                    }
                }
            }
        }
    },
    async onResponse({ response }: FetchContext) {
        await clearUserIfNeeded(response)

        if (response && !response.ok) {
            // 301/302 多为登录跳转或网关重定向，不算业务故障
            if ([301, 302, 303, 307, 308].includes(response.status)) return
            console.error(`API Error: ${response.status} ${response.statusText}`)
            if ([400, 404, 500, 501, 503].includes(response.status)) {
                showErrorToast(response.status, response.statusText)
            }
        }
    },
    async onResponseError({ error, response }: FetchContext) {
        if (response && [301, 302, 303, 307, 308].includes(response.status)) return
        console.error('Request failed:', error)
        if (response) {
            await clearUserIfNeeded(response)
            if ([400, 404, 500, 501, 503].includes(response.status)) {
                showErrorToast(response.status, error?.message)
            }
        }
    }
})

/**
 * API 请求 composable
 * 
 * 提供常用的 HTTP 请求方法，符合 Nuxt composable 最佳实践
 */
export const useRequest = () => {
    /**
     * GET 请求
     * @param url 请求地址
     * @param options 请求配置
     * @returns 响应数据
     */
    const get = async <T = any>(url: string, options?: RequestOptions): Promise<ApiResponse<T>> => {
        return apiRequest(normalizeMallPath(url) as string, { ...options, method: 'GET' }) as Promise<ApiResponse<T>>
    }

    /**
     * POST 请求
     * @param url 请求地址
     * @param data 请求体数据
     * @param options 请求配置（可传 skipToken: true 不携带 token）
     * @returns 响应数据
     */
    const post = async <T = any>(url: string, data?: any, options?: RequestOptions): Promise<ApiResponse<T>> => {
        return apiRequest(normalizeMallPath(url) as string, { ...options, method: 'POST', body: data }) as Promise<ApiResponse<T>>
    }

    /**
     * PUT 请求
     * @param url 请求地址
     * @param data 请求体数据
     * @param options 请求配置（可传 skipToken: true 不携带 token）
     * @returns 响应数据
     */
    const put = async <T = any>(url: string, data?: any, options?: RequestOptions): Promise<ApiResponse<T>> => {
        return apiRequest(normalizeMallPath(url) as string, { ...options, method: 'PUT', body: data }) as Promise<ApiResponse<T>>
    }

    /**
     * DELETE 请求
     * @param url 请求地址
     * @param options 请求配置
     * @returns 响应数据
     */
    const del = async <T = any>(url: string, options?: RequestOptions): Promise<ApiResponse<T>> => {
        return apiRequest(normalizeMallPath(url) as string, { ...options, method: 'DELETE' }) as Promise<ApiResponse<T>>
    }

    /**
     * 发送 Form 数据请求（application/x-www-form-urlencoded）
     * @param url 请求地址
     * @param data 请求体数据
     * @param options 请求配置（可传 skipToken: true 不携带 token）
     * @returns 响应数据
     */
    const postForm = async <T = any>(url: string, data?: any, options?: RequestOptions): Promise<ApiResponse<T>> => {
        return apiRequest(normalizeMallPath(url) as string, {
            ...options,
            method: 'POST',
            body: data,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                ...options?.headers
            }
        }) as Promise<ApiResponse<T>>
    }

    /**
     * 获取请求配置
     * @returns 请求配置对象
     */
    const getConfig = () => {
        return getRequestConfig()
    }

    return {
        get,
        post,
        put,
        delete: del, // 避免与 JavaScript 关键字冲突
        del, // 同时提供 del 作为别名
        postForm,
        getConfig
    }
}
