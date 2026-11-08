import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuthInfo } from '@/utils/auth'
import { isSuccessCode, shouldPassAsEmpty, resolveError, logBackendMessage } from '@/utils/errorCode'

function resolveBaseURL(serviceType) {
    const base = ((import.meta.env.VITE_APP_BASE_URL || '') + '/').replace(/\/+$/, '') + '/'
    const name = String(serviceType || '').replace(/^\/?mall-/, '').replace(/\/$/, '')
    if (!name) return base
    const aliases = { 'third-party': 'thirdparty', 'auth-server': 'auth', business: 'merchant', 'bidding-procurement': 'merchant' }
    const routes = {
        ai: 'web/ai', base: 'web/base', userms: 'web/user', auth: 'web/auth', merchant: 'web/merchant',
        thirdparty: 'web/thirdparty', goods: 'web/mall/goods', order: 'web/order', payment: 'web/pay',
        supplier: 'web/plugin/supplier', activity: 'web/plugin/activity', cms: 'web/plugin/cms',
        finance: 'web/plugin/finance', rebate: 'web/mall/module/rebate', rider: 'web/plugin/rider',
        webpos: 'web/plugin/webpos', im: 'web/plugin/im', job: 'web/job'
    }
    const route = routes[aliases[name] || name] || `web/${aliases[name] || name}`
    return `${base}${route}/`
}

// 重试配置
const MAX_RETRIES = 3
const retryDelay = (retryCount) => Math.min(1000 * 2 ** retryCount, 30000)

// 创建axios实例的函数
export const createService = (ServiceType) => {
    // 创建axios实例
    const service = axios.create({
        baseURL: resolveBaseURL(ServiceType), // api 的 base_url
        timeout: 10000 // 优化超时时间
    })
    service.defaults.baseURL = resolveBaseURL(ServiceType)
    
    // request拦截器 - 优化性能
    service.interceptors.request.use(
        config => {
            if (typeof config.url === 'string' && config.url.startsWith('/') && !config.url.startsWith('/web/')) {
                config.url = config.url.slice(1)
            }
            // 添加请求时间戳，避免缓存问题
            if (config.method?.toLowerCase() === 'get') {
                config.params = {
                    ...config.params,
                    _t: Date.now()
                }
            }
            
            // 添加token
            const token = getToken()
            if (token) {
                config.headers['Token'] = token
                
                // 处理FormData类型和普通JSON数据
                if (config.data) {
                    if (config.data instanceof FormData) {
                        // 对于FormData，使用append方法添加token
                        config.data.append('token', token)
                    } else {
                        // 对于普通JSON数据，直接设置token属性
                        config.data.token = token
                        // 确保JSON数据的Content-Type设置正确
                        if (!config.headers['Content-Type']) {
                            config.headers['Content-Type'] = 'application/json;charset=utf-8'
                        }
                    }
                }
            }
            
            return config
        },
        error => {
            console.error('请求错误:', error)
            return Promise.reject(error)
        }
    )

    // response 拦截器 - 优化错误处理和重试机制
    service.interceptors.response.use(
        response => {
            const res = response.data
            // 成功码直接放行；「查询为空」类语义码按 EMPTY_STATE 放行且不弹 Toast。
            // 不再用裸码白名单 VALID_CODES=[0,1,10060,11007,14052,10047,16012]：
            // 其中 11007 是「权限不足」，静默放行会让用户点了按钮没反应，必须显式提示。
            if (isSuccessCode(res.code) || shouldPassAsEmpty(res.code)) {
                return res
            }
            const resolved = resolveError(res.code, { backendMessage: res.message })
            logBackendMessage(res, { url: response.config?.url })
            ElMessage({
                message: resolved.text,
                type: 'error',
                duration: 3000,
                showClose: true
            })
            return Promise.reject(new Error(resolved.text))
        },
        async error => {
            const config = error.config
            
            // 初始化重试计数
            config.retryCount = config.retryCount || 0
            
            // 处理网络错误并实现自动重试
            if (error.response) {
                const { status } = error.response
                
                // 对于服务器错误，尝试重试
                if ([500, 502, 503, 504].includes(status) && config.retryCount < MAX_RETRIES) {
                    config.retryCount++
                    
                    // 延迟重试
                    await new Promise(resolve => setTimeout(resolve, retryDelay(config.retryCount)))
                    
                    // 重新发送请求
                    return service(config)
                }
                
                let errorMessage = '请求失败，请稍后重试'
                
                switch (status) {
                    case 400:
                        errorMessage = '请求错误(400)'
                        break
                    case 401:
                        errorMessage = '未授权，请重新登录(401)'
                        // 登出操作使用防抖，避免重复执行
                        if (!window.isLoggingOut) {
                            window.isLoggingOut = true
                            try {
                                clearAuthInfo()
                            } finally {
                                window.isLoggingOut = false
                                location.reload()
                            }
                        }
                        break
                    case 403:
                        errorMessage = '拒绝访问(403)'
                        break
                    case 404:
                        errorMessage = '请求出错(404)'
                        break
                    case 408:
                        errorMessage = '请求超时(408)'
                        break
                    case 500:
                        errorMessage = '服务器错误(500)'
                        break
                    case 501:
                        errorMessage = '服务未实现(501)'
                        break
                    case 502:
                        errorMessage = '您的网络有问题，请稍等(502)'
                        break
                    case 503:
                        errorMessage = '服务不可用(503)'
                        break
                    case 504:
                        errorMessage = '网络超时(504)'
                        break
                    case 505:
                        errorMessage = 'HTTP版本不受支持(505)'
                        break
                    default:
                        errorMessage = `连接出错(${status})!`
                }
                
                ElMessage({
                    message: errorMessage,
                    type: 'error',
                    duration: 3000,
                    showClose: true
                })
            } else if (error.request) {
                // 请求已发出但没有收到响应 - 网络问题
                ElMessage({
                    message: '网络错误，请检查您的网络连接',
                    type: 'error',
                    duration: 3000,
                    showClose: true
                })
            } else {
                // 请求配置出错
                console.error('请求配置错误:', error)
                ElMessage({
                    message: error.message || '请求失败，请稍后重试',
                    type: 'error',
                    duration: 3000,
                    showClose: true
                })
            }
            
            return Promise.reject(error)
        }
    )

    return service
}

// 保持向后兼容
export const requestBody = createService

// 导出常用的请求方法，避免冗余Promise包装
export const http = {
    // 创建特定服务类型的API实例
    createApi: (ServiceType) => {
        const service = createService(ServiceType)
        return {
            get: (url, params = {}) => service.get(url, { params }),
            post: (url, data = {}) => service.post(url, data),
            patch: (url, data = {}) => service.patch(url, data),
            delete: (url, params = {}) => service.delete(url, { params })
        }
    }
}

// 为了向后兼容，保留原有方法但使用更简洁的实现
const defaultService = createService('')
export function post(url, params) {
    return defaultService.post(url, params)
}

export function patchData(url, params) {
    return defaultService.patch(url, params)
}

export function deleteData(url, params) {
    return defaultService.delete(url, { params })
}

export function get(url, params) {
    return defaultService.get(url, { params })
}
