import { useIsLogin } from '~/composables/useAuth'
import { getToken } from '~/utils/auth'

/** Open a protected mall sub-application in the current tab by default. */
const openExternalCenter = (fullUrl: string, options?: { newTab?: boolean }) => {
    if (!useIsLogin()) return

    if (options?.newTab === true) {
        window.open(fullUrl, '_blank', 'noopener,noreferrer')
    } else {
        window.location.assign(fullUrl)
    }
}

/**
 * 子应用对外 URL。
 * bbc.houjit.com 的 nginx root 是 public/mall/pc，子应用用 location ^~ /{app}
 * 映射到 public/mall/{app}，因此浏览器路径是 /{app}/#...，不是 /mall/{app}/#...。
 */
const mallAppUrl = (app: string, url: string) => {
    const baseURL = String(openExternalUrl().baseURL || '').replace(/\/$/, '')
    const path = String(url || '/').startsWith('/') ? url : `/${url}`
    return `${baseURL}/${app}/#${path}`
}

export const UNIAPP_URL = (url: string) => openExternalCenter(mallAppUrl('h5', url))
export const UCENTER_URL = (url: string) => openExternalCenter(mallAppUrl('ucenter', url))
export const BCENTER_URL = (url: string) => openExternalCenter(mallAppUrl('bcenter', url))
export const SUPPLIER_URL = (url: string) => openExternalCenter(mallAppUrl('supplier', url))
export const TCENTER_URL = (url: string, _data?: any) => openExternalCenter(mallAppUrl('tcenter', url))

// IM chat retains its legacy location and mirrors the token for its app.
export const IMCHAT_URL = (url: string) => {
    localStorage.setItem('token', getToken() ?? '')
    const baseURL = String(openExternalUrl().baseURL || '').replace(/\/$/, '')
    openExternalCenter(`${baseURL}/imchat/#${url}`)
}

export const IMAGE_URL = (url: string) => {
    const path = String(url || '').replace(/^\/+/, '')
    const baseURL = String(openExternalUrl().baseURL || '').replace(/\/$/, '')
    return `${baseURL}/uploads/mall-demo/pc/${path}`
}

export const FILE_URL = (url: string) => {
    const baseURL = String(openExternalUrl().baseURL || '').replace(/\/$/, '')
    return `${baseURL}/resource/files/pc/${url}`
}

const openExternalUrl = () => {
    const runtimeConfig = useRuntimeConfig()
    return {
        baseURL: runtimeConfig.public.apiBaseUrl,
        timeout: runtimeConfig.public.apiTimeout,
        retryCount: runtimeConfig.public.apiRetryCount
    }
}
