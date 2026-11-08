/**
 * 配置管理中心
 *
 * 环境判断优先 NUXT_PUBLIC_APP_ENV，其次 NODE_ENV（nuxt build 会强制为 production，故测试包用 NUXT_PUBLIC_APP_ENV）：
 * - 正式生产：build / build:prod，不设 NUXT_PUBLIC_APP_ENV → 用 NODE_ENV=production → default + production.ts
 * - 测试生产：build:test 设 NUXT_PUBLIC_APP_ENV=development → 仅 default.ts（baseUrl 等为测试域名）
 */
import defaultConfig from './default' // 测试生产环境 / 开发环境配置
import productionConfig from './production' // 正式生产环境配置

/**
 * 获取当前运行环境
 *
 * 优先使用 NUXT_PUBLIC_APP_ENV，其次 NODE_ENV，默认 development（测试生产）
 */
const getCurrentEnvironment = (): string => {
    // 在 Nuxt 环境中，应该通过 import.meta.env 或 useRuntimeConfig 访问环境变量
    // 但由于这是配置文件，我们需要使用更直接的方法
    try {
        // 尝试访问 import.meta.env（现代浏览器和 Node.js 环境）
        const metaEnv = (import.meta as any).env
        if (metaEnv?.NUXT_PUBLIC_APP_ENV) {
            return metaEnv.NUXT_PUBLIC_APP_ENV
        }
        if (metaEnv?.NODE_ENV) {
            return metaEnv.NODE_ENV
        }
    } catch (e) {
        // 忽略错误
    }

    try {
        // 尝试访问 process.env（Node.js 环境）
        const _process = (globalThis as any).process
        if (_process?.env?.NUXT_PUBLIC_APP_ENV) {
            return _process.env.NUXT_PUBLIC_APP_ENV
        }
        if (_process?.env?.NODE_ENV) {
            return _process.env.NODE_ENV
        }
    } catch (e) {
        // 忽略错误
    }

    // 默认是开发环境
    return 'development'
}

/**
 * 深度合并两个对象
 * 
 * 用于合并默认配置和环境特定配置
 * 环境特定配置会覆盖默认配置中的相同属性
 */
const deepMerge = <T = any>(target: T, source: any): T => {
    const result = { ...target }

    Object.keys(source).forEach(key => {
        if (typeof source[key] === 'object' && source[key] !== null && !Array.isArray(source[key])) {
            (result as any)[key] = typeof (result as any)[key] === 'object' && (result as any)[key] !== null
                ? deepMerge((result as any)[key], source[key])
                : source[key]
        } else {
            (result as any)[key] = source[key]
        }
    })

    return result
}

/**
 * 获取当前环境对应的配置
 *
 * - production → 正式生产，返回 production.ts 覆盖项
 * - development 及其他 → 测试生产/开发，仅用 default.ts（返回空覆盖）
 */
const getEnvironmentConfig = () => {
    const env = getCurrentEnvironment()

    switch (env) {
        case 'production':
            return productionConfig
        case 'development':
        default:
            return {}
    }
}

/**
 * 合并并解析配置
 * 
 * 将默认配置和当前环境特定配置合并
 * 环境特定配置会覆盖默认配置中的相同属性
 */
const config = deepMerge(defaultConfig, getEnvironmentConfig())

/**
 * 导出完整配置
 * 
 * 包含应用程序的所有配置，根据当前环境自动合并
 */
export default config

/**
 * 导出当前环境信息
 */
export const currentEnvironment = getCurrentEnvironment()

/**
 * 导出环境检测方法
 */
export const isDevelopment = currentEnvironment === 'development'
export const isProduction = currentEnvironment === 'production'

/**
 * 导出配置访问方法
 * 
 * 提供函数式方式访问配置，方便在需要动态获取配置的场景使用
 */
export const getConfig = () => config
