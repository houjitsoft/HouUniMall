/**
 * Nuxt 4 配置文件
 *
 * 包含项目的全局配置，包括开发工具、组件自动导入、运行时配置等
 * 已根据 Nuxt 4 最佳实践优化性能与构建配置
 */
import { fileURLToPath } from 'node:url'
import config from './config'

// 构建时在 Node 环境执行，通过 globalThis 读取 env 避免 TS 对 process 的类型检查
const env = (typeof (globalThis as any).process !== 'undefined' && (globalThis as any).process?.env) as Record<string, string | undefined> | undefined

export default defineNuxtConfig({
    alias: {
        '@utils': fileURLToPath(new URL('./app/utils', import.meta.url))
    },

    /**
     * 兼容性日期
     * 用于确定 Nuxt 使用的默认行为和 API 版本
     */
    compatibilityDate: '2025-02-09',

    /**
     * 开发工具配置
     * 开发环境启用，生产环境由 $production 覆盖为禁用
     */
    devtools: { enabled: true },

    /**
     * 开发服务器配置
     * Nuxt 4 中 port/host 需在此配置，vite.server 已排除该选项
     */
    devServer: {
        port: 8813,
        host: 'localhost'
    },

    /**
     * 全局样式文件
     */
    css: ['~/assets/css/main.css'],

    /**
     * 路由规则 - 混合渲染策略
     * 根据路由类型配置不同的缓存与渲染策略，提升性能
     */
    routeRules: {
        // 首页预渲染
        '/': { prerender: true },
        // 静态页面可启用 SWR 缓存
        '/article': { swr: 3600 },
        '/feature': { swr: 3600 },
        // 商品详情：静态托管下必须产出 HTML，否则 nginx 会回落首页并把 URL 改成 /
        '/product/**': { prerender: true },
        '/payment/**': { ssr: true },
        '/login/**': { ssr: true },
        // 店铺详情为动态段；显式预渲染常见店铺，避免 nginx 回落到首页 HTML
        '/store/**': { ssr: true },
            '/store/list': { prerender: true },
        // 历史导航路径，重定向到资讯中心，避免 nginx 回落首页
        '/article/list': { redirect: '/information' }
    },

    /**
     * Nitro 服务端配置
     */
    nitro: {
        prerender: {
            autoSubfolderIndex: false,
            crawlLinks: true,
            // 个别非首页链接依赖后台接口，预渲染失败时仍需产出可部署的首页静态包。
            failOnError: false,
            // 静态托管下 /store/:id、/product/:id 无实体 HTML 时会 try_files 到首页
            routes: [
                '/store/list',
                '/store/1',
                '/store/2',
                '/store/3',
                '/activity/seckill',
                '/activity/discount',
                '/information',
                '/article/list',
                // 演示商品兜底（构建钩子还会按接口追加）
                ...Array.from({ length: 16 }, (_, i) => `/product/${i + 1}`)
            ]
        },
        compressPublicAssets: true
    },

    hooks: {
        /**
         * 构建时拉取在售商品 ID，预渲染 /product/:id，避免硬刷新回落首页。
         */
        async 'nitro:config'(nitroConfig) {
            const routes = new Set<string>(nitroConfig.prerender?.routes || [])
            const endpoints = [
                'http://127.0.0.1:8391/web/mall/goods/front/lists',
                'https://unimall.houjit.com/web/mall/goods/front/lists'
            ]
            for (const url of endpoints) {
                try {
                    const res = await fetch(url, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ page: 1, limit: 200 })
                    })
                    if (!res.ok) continue
                    const json = await res.json() as any
                    const list = json?.data?.lists || json?.data?.list || []
                    if (!Array.isArray(list) || list.length === 0) continue
                    for (const item of list) {
                        const id = Number(item?.id ?? item?.goodsId ?? 0)
                        if (id > 0) routes.add(`/product/${id}`)
                    }
                    break
                } catch {
                    // 尝试下一个 endpoint
                }
            }
            nitroConfig.prerender = nitroConfig.prerender || {}
            nitroConfig.prerender.routes = [...routes]
        }
    },

    /**
     * 实验性功能 - 提升开发体验与构建性能
     */
    experimental: {
        // Windows 上 buildCache 易出现 Skipping unsafe cache path，导致还原损坏缓存后找不到 client.manifest.mjs
        buildCache: (globalThis as any).process?.platform !== 'win32',
        // 原生 async context，减少 "Nuxt instance is unavailable" 错误
        asyncContext: true,
        // Windows 平台使用 parcel watcher 可提升大项目监听性能
        watcher: (globalThis as any).process?.platform === 'win32' ? 'parcel' : 'chokidar-granular',
        // NuxtLink 预取策略：'interaction' 表示用户交互时预取，减少初始请求
        defaults: {
            nuxtLink: {
                prefetch: true,
                prefetchOn: { interaction: true }
            }
        }
    },

    /**
     * 未来版本兼容 - 提前启用新特性
     */
    future: {
        compatibilityVersion: 4,
        typescriptBundlerResolution: true
    },

    /**
     * App 配置
     */
    app: {
        /**
         * 页面头部配置（SEO：默认 description/keywords）
         */
        head: {
            title: config.app.name,
            meta: [
                { charset: 'utf-8' },
                { name: 'viewport', content: 'width=device-width, initial-scale=1' },
                { name: 'renderer', content: 'webkit' },
                { name: 'description', content: 'B2C商城，提供商品选购、店铺浏览、限时折扣、秒杀促销、积分兑换、在线支付等一站式购物服务。' },
                { name: 'keywords', content: 'B2C商城,在线商城,商品购物,店铺,秒杀,限时折扣,积分商城,在线支付' }
            ]
        }
    },

    /**
     * Vite 配置
     */
    vite: {
        ssr: {
            noExternal: ['**/*.css', '**/*.scss']
        },
        server: {
            open: true
        },
        build: {
            sourcemap: false
        }
    },
    /**
     * 组件自动导入配置
     * 
     * 配置组件自动导入的目录和行为
     */
    components: {
        dirs: [
            {
                path: '~/components',
                extensions: ['vue'],
                prefix: '',
                pathPrefix: false
            }
        ]
    },

    /**
     * 模块配置
     * 
     * 引入 Nuxt UI 组件库和 Pinia 状态管理
     */
    modules: ['@nuxt/ui', '@pinia/nuxt'],

    /**
     * Nuxt UI 配置
     *
     * 禁用字体模块以避免从 Google Fonts 获取元数据时的警告
     */
    ui: {
        fonts: false
    },

    /**
     * Nuxt Icon 配置（由 @nuxt/ui 引入）
     * 使用本地 lucide 集合（需安装 @iconify-json/lucide），服务端提供图标接口
     */
    icon: {
        serverBundle: {
            collections: ['lucide','heroicons']
        }
    },

    /**
     * 运行时配置
     * 
     * 配置运行时可用的公共和私有配置
     */
    runtimeConfig: {
        /**
         * 私有配置
         * 
         * 仅在服务器端可用的配置
         */
        apiSecret: env?.API_SECRET || 'default_secret',

        /**
         * 公共配置
         * 
         * 在客户端和服务器端都可用的配置
         */
        public: {
            appName: config.app.name,
            appVersion: config.app.version,
            /** 站点根 URL，用于 canonical、og:url、sitemap，可通过 NUXT_PUBLIC_SITE_URL 覆盖 */
            siteUrl: env?.NUXT_PUBLIC_SITE_URL || (config.app as any).siteUrl,
            apiBaseUrl: env?.NUXT_PUBLIC_API_BASE_URL || config.api.baseUrl,
            appEnv: env?.NUXT_PUBLIC_APP_ENV || env?.NODE_ENV || 'development',
            apiTimeout: config.api.timeout,
            apiRetryCount: config.api.retryCount,
            storagePrefix: config.storage.prefix,
            loggerLevel: config.logger.level,
            loggerEnabled: config.logger.enabled
        }
    },

    /**
     * 生产环境特定配置（c12 环境覆盖）
     * 生产构建时自动合并，禁用 devtools 以减小包体积
     */
    $production: {
        devtools: { enabled: false }
    }
})
