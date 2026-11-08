/**
 * 认证工具类
 * 
 * 封装了 Cookie 操作和 Token 管理功能
 */

import type { CookieOptions } from '~/types/utils'
import { encryptAesCbcPkcs7Base64, decryptAesCbcPkcs7Base64 } from '~/utils/cryptoAes'

/**
 * 设置 Cookie
 * @param name Cookie 名称
 * @param value Cookie 值
 * @param options Cookie 配置选项
 */
export const setCookie = (name: string, value: string, options: CookieOptions = {}) => {
    if (typeof document === 'undefined') {
        return
    }

    let cookieStr = `${encodeURIComponent(name)}=${encodeURIComponent(value)}`

    if (options.path) {
        cookieStr += `; path=${options.path}`
    }

    if (options.domain) {
        cookieStr += `; domain=${options.domain}`
    }

    if (options.expires) {
        cookieStr += `; expires=${options.expires.toUTCString()}`
    }

    if (options.maxAge) {
        cookieStr += `; max-age=${options.maxAge}`
    }

    if (options.secure) {
        cookieStr += '; secure'
    }

    if (options.sameSite) {
        cookieStr += `; samesite=${options.sameSite}`
    }

    document.cookie = cookieStr
}

/**
 * 获取 Cookie
 * @param name Cookie 名称
 * @returns Cookie 值，不存在则返回 null
 */
export const getCookie = (name: string): string | null => {
    if (typeof document === 'undefined') {
        return null
    }

    const cookies = document.cookie.split(';').map(cookie => cookie.trim())
    const targetCookie = cookies.find(cookie =>
        cookie.startsWith(`${encodeURIComponent(name)}=`)
    )

    if (targetCookie) {
        const parts = targetCookie.split('=')
        return parts[1] ? decodeURIComponent(parts[1]) : null
    }

    return null
}

/**
 * 删除 Cookie
 * @param name Cookie 名称
 * @param options Cookie 配置选项（需要与设置时的路径和域名一致）
 */
export const deleteCookie = (name: string, options: CookieOptions = {}) => {
    setCookie(name, '', {
        ...options,
        expires: new Date(0),
        maxAge: -1
    })
}

/**
 * Token 管理相关常量
 */
const TOKEN_KEY = 'token'

/** Token 过期时间：一个月（30 天，单位：秒） */
const TOKEN_MAX_AGE = 30 * 24 * 60 * 60

/**
 * 设置 Token
 * @param token Token 值
 * @param options Cookie 配置选项
 */
export const setToken = (token: string, options: CookieOptions = {}) => {
    const encryptedToken = encryptAesCbcPkcs7Base64(token)
    setCookie(TOKEN_KEY, encryptedToken, {
        path: '/',
        sameSite: 'lax',
        maxAge: TOKEN_MAX_AGE,
        ...options
    })
}

/**
 * 获取 Token
 * @returns Token 值，不存在则返回 null
 */
export const getToken = (): string | null => {
    const rawToken = getCookie(TOKEN_KEY)
    if (!rawToken) return null

    // 优先按密文解密；解密失败则兼容旧明文 cookie（避免历史登录态立即失效）
    const decrypted = decryptAesCbcPkcs7Base64(rawToken)
    return decrypted || rawToken
}

/**
 * 删除 Token
 * @param options Cookie 配置选项（需要与设置时的路径和域名一致）
 */
export const deleteToken = (options: CookieOptions = {}) => {
    deleteCookie(TOKEN_KEY, {
        path: '/',
        sameSite: 'lax',
        ...options
    })
}

/**
 * 认证相关工具方法
 */
export const auth = {
    setCookie,
    getCookie,
    deleteCookie,
    setToken,
    getToken,
    deleteToken
}

export default auth