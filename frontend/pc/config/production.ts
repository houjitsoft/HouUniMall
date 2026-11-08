/**
 * 正式生产环境配置
 *
 * 在 default 基础上覆盖，用于 npm run build / build:prod 打包
 */
export default {
    /**
     * API 配置（覆盖默认配置）
     */
    api: {
        baseUrl: '' // 生产环境使用当前站点的 bff-web
    },

    /**
     * 日志配置（覆盖默认配置）
     */
    logger: {
        level: 'warn', // 生产环境使用警告级别日志
        enabled: true // 是否启用日志
    },

    /**
     * 生产环境特定配置
     */
    prod: {
        enableMonitoring: false, // 是否启用应用监控
        // monitoringApi: 'https://monitor.yourdomain.com' // 监控服务 API
    }
}
