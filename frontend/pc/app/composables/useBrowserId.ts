/**
 * 浏览器ID工具类
 * 
 * 用于获取和生成浏览器唯一标识符
 * ID会持久化存储在 localStorage 中，确保同一浏览器每次访问都能获取到相同的ID
 */

/** 浏览器ID在 localStorage 中的存储键名 */
const BROWSER_ID_KEY = 'font_b2b_browser_id'

/**
 * 生成UUID v4格式的唯一标识符
 * @returns UUID字符串
 */
const generateUUID = (): string => {
    // 优先使用浏览器原生 crypto.randomUUID() API（现代浏览器支持）
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
        return crypto.randomUUID()
    }

    // 降级方案：手动生成UUID v4
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = (Math.random() * 16) | 0
        const v = c === 'x' ? r : (r & 0x3) | 0x8
        return v.toString(16)
    })
}

/**
 * 获取浏览器ID
 * 
 * 如果 localStorage 中已存在浏览器ID，则直接返回
 * 如果不存在，则生成新的UUID并存储到 localStorage 中
 * 
 * @returns 浏览器唯一标识符
 * @example
 * const browserId = useBrowserId()
 * console.log(browserId) // '550e8400-e29b-41d4-a716-446655440000'
 */
export const useBrowserId = (): string => {
    // 确保在浏览器环境中运行
    if (typeof window === 'undefined' || typeof localStorage === 'undefined') {
        // SSR环境或localStorage不可用时，返回临时ID（不持久化）
        return generateUUID()
    }

    try {
        // 尝试从 localStorage 中获取已存在的浏览器ID
        const storedId = localStorage.getItem(BROWSER_ID_KEY)

        if (storedId) {
            return storedId
        }

        // 如果不存在，生成新的ID并存储
        const newId = generateUUID()
        localStorage.setItem(BROWSER_ID_KEY, newId)

        return newId
    } catch (error) {
        // localStorage 可能被禁用或已满，返回临时ID
        console.warn('无法访问 localStorage，返回临时浏览器ID:', error)
        return generateUUID()
    }
}

/**
 * 重置浏览器ID
 * 
 * 清除当前存储的浏览器ID，下次调用 useBrowserId() 时会生成新的ID
 * 
 * @example
 * resetBrowserId()
 * const newId = useBrowserId() // 获取新的浏览器ID
 */
export const resetBrowserId = (): void => {
    if (typeof window === 'undefined' || typeof localStorage === 'undefined') {
        return
    }

    try {
        localStorage.removeItem(BROWSER_ID_KEY)
    } catch (error) {
        console.warn('无法清除浏览器ID:', error)
    }
}

/**
 * 获取当前存储的浏览器ID（不生成新ID）
 * 
 * @returns 浏览器ID，如果不存在则返回 null
 * @example
 * const currentId = getBrowserId()
 * if (currentId) {
 *   console.log('当前浏览器ID:', currentId)
 * } else {
 *   console.log('尚未生成浏览器ID')
 * }
 */
export const getBrowserId = (): string | null => {
    if (typeof window === 'undefined' || typeof localStorage === 'undefined') {
        return null
    }

    try {
        return localStorage.getItem(BROWSER_ID_KEY)
    } catch (error) {
        console.warn('无法访问 localStorage:', error)
        return null
    }
}
