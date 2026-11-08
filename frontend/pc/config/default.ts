/**
 * 默认环境配置（测试生产 / 开发）
 *
 * 用于测试生产环境打包（npm run build:test）及本地开发
 * 正式生产环境由 production.ts 在 default 基础上覆盖
 */
export default {
    /**
     * 应用程序基本信息
     */
    app: {
        name: 'B2C商城系统',
        version: '1.0.0',
        /** 站点根 URL，用于 canonical、og:url、sitemap，生产环境建议用 NUXT_PUBLIC_SITE_URL 覆盖 */
        siteUrl: 'https://unimall.houjit.com'
    },

    /**
     * API 配置
     */
    api: {
        timeout: 30000, // 请求超时时间（毫秒）
        loginTimeout: 120000, // 登录接口超时时间（毫秒）
        retryCount: 1, // 请求失败重试次数
        prefix: '', // API 路径前缀（已去除）
        baseUrl: '' // 统一通过同源 bff-web 访问；可用 NUXT_PUBLIC_API_BASE_URL 覆盖
    },

    /**
     * 存储配置
     */
    storage: {
        prefix: 'font_b2c_' // 本地存储键前缀
    },

    /**
     * 日志配置
     */
    logger: {
        level: 'debug', // 日志级别（开发环境使用 debug）
        enabled: true // 是否启用日志
    },

    /**
     * 开发环境特定配置
     */
    dev: {
        enableMock: true, // 是否启用 API 模拟
        mockApiBaseUrl: '/mock' // 模拟 API 基础 URL
    }
}
