import { merge } from 'lodash'
import configs from '@/config'
import { Axios } from './axios'
import { ContentTypeEnum, RequestCodeEnum, RequestMethodsEnum } from '@/enums/requestEnums'
import type { AxiosHooks } from './type'
import { clearAuthInfo, getToken } from '../auth'
import feedback from '../feedback'
import NProgress from 'nprogress'
import { AxiosError, type AxiosRequestConfig } from 'axios'
import router from '@/router'
import { PageEnum } from '@/enums/pageEnum'
import { createInvalidSessionHandler, resolveSameOriginHttpUrl } from './requestRules.mjs'

const invalidSessionHandler = createInvalidSessionHandler({
    clearAuth: clearAuthInfo,
    isLoginPage: () => router.currentRoute.value.path === PageEnum.LOGIN,
    redirectToLogin: () => router.push(PageEnum.LOGIN)
})

const safeResponseMessage = (msg: unknown, fallback: string) =>
    typeof msg === 'string' && msg.trim() ? msg : fallback

// 处理axios的钩子函数
const axiosHooks: AxiosHooks = {
    requestInterceptorsHook(config) {
        NProgress.start()
        const { withToken, isParamsToData } = config.requestOptions
        const params = config.params || {}
        const headers = config.headers || {}

        const token = getToken()
        invalidSessionHandler.observeAuthenticatedSession(token)
        // 添加token
        if (withToken) {
            headers['Authorization'] = token
        }
        // POST请求下如果无data，则将params视为data
        if (
            isParamsToData &&
            !Reflect.has(config, 'data') &&
            config.method?.toUpperCase() === RequestMethodsEnum.POST
        ) {
            config.data = params
            config.params = {}
        }
        config.headers = headers
        return config
    },
    requestInterceptorsCatchHook(err) {
        NProgress.done()
        return err
    },
    async responseInterceptorsHook(response) {
        NProgress.done()
        const { isTransformResponse, isReturnDefaultResponse } = response.config.requestOptions

        //返回默认响应，当需要获取响应头及其他数据时可使用
        if (isReturnDefaultResponse) {
            return response
        }
        // 是否需要对数据进行处理
        if (!isTransformResponse) {
            return response.data
        }
        const { code, data, show, msg } = response.data
        switch (code) {
            case RequestCodeEnum.SUCCESS:
                if (show) {
                    msg && feedback.msgSuccess(msg)
                }
                return data
            case RequestCodeEnum.FAIL: {
                const failureMessage = safeResponseMessage(msg, '请求失败，请稍后重试')
                if (show) {
                    feedback.msgError(failureMessage)
                }
                return Promise.reject(new Error(failureMessage))
            }
            case RequestCodeEnum.LOGIN_FAILURE:
                void invalidSessionHandler.handle()
                return Promise.reject(
                    new Error(safeResponseMessage(msg, '登录状态已失效，请重新登录'))
                )
            case RequestCodeEnum.OPEN_NEW_PAGE: {
                const targetUrl = resolveSameOriginHttpUrl(data?.url, window.location.href)
                if (!targetUrl) {
                    return Promise.reject(new Error('跳转地址无效'))
                }
                window.location.href = targetUrl
                return data
            }
            default: {
                const unknownMessage = safeResponseMessage(msg, '未知响应状态，请稍后重试')
                if (show) {
                    feedback.msgError(unknownMessage)
                }
                return Promise.reject(new Error(unknownMessage))
            }
        }
    },
    responseInterceptorsCatchHook(error) {
        NProgress.done()
        if (error.code === AxiosError.ERR_CANCELED) {
            return
        }

        let message = '请求失败，请稍后重试'
        if (error.code === AxiosError.ECONNABORTED || error.code === 'ETIMEDOUT') {
            message = '请求超时，请稍后重试'
        } else if (error.code === AxiosError.ERR_NETWORK) {
            message = '网络连接异常，请检查网络后重试'
        } else if (error.response) {
            message = '服务请求失败，请稍后重试'
        }

        error.message = message
        feedback.msgError(message)
    }
}

const defaultOptions: AxiosRequestConfig = {
    //接口超时时间
    timeout: configs.timeout,
    // 基础接口地址
    baseURL: configs.baseUrl,
    //请求头
    headers: { 'Content-Type': ContentTypeEnum.JSON, version: configs.version },
    // 处理 axios的钩子函数
    axiosHooks: axiosHooks,
    // 每个接口可以单独配置
    requestOptions: {
        // 是否将params视为data参数，仅限post请求
        isParamsToData: true,
        //是否返回默认的响应
        isReturnDefaultResponse: false,
        // 需要对返回数据进行处理
        isTransformResponse: true,
        // 接口拼接地址
        urlPrefix: configs.urlPrefix,
        // 忽略重复请求
        ignoreCancelToken: false,
        // 是否携带token
        withToken: true,
        // 开启请求超时重新发起请求请求机制
        isOpenRetry: true,
        // 重新请求次数
        retryCount: 2
    }
}

function createAxios(opt?: Partial<AxiosRequestConfig>) {
    return new Axios(
        // 深度合并
        merge(defaultOptions, opt || {})
    )
}
const request = createAxios()
export default request
