import { markRaw } from 'vue'
import * as VueRuntime from 'vue'
import * as VueRouterRuntime from 'vue-router'
import * as PiniaRuntime from 'pinia'
import * as ElementPlusRuntime from 'element-plus'
import * as ElementPlusIconsRuntime from '@element-plus/icons-vue'
import request from '@/utils/request'
import { createSellerPluginRegistry } from './runtimeCore.mjs'

const PLUGIN_RUNTIME_GLOBAL_KEY = '__HOUCLOUD_PLUGIN_RUNTIME__'

function exposeHostRuntime() {
    ;(globalThis as any)[PLUGIN_RUNTIME_GLOBAL_KEY] = {
        vue: VueRuntime,
        'vue-router': VueRouterRuntime,
        pinia: PiniaRuntime,
        'element-plus': ElementPlusRuntime,
        '@element-plus/icons-vue': ElementPlusIconsRuntime
    }
}

exposeHostRuntime()

const registry = createSellerPluginRegistry({
    importModule: (url: string) => import(/* @vite-ignore */ url),
    createContext: (code: string) => ({
        code,
        host: {
            navigate: async (path: string) => (await import('@/router')).default.push(path)
        },
        async callMerchantApi(action: string, body: Record<string, any> = {}) {
            const safeAction = String(action || '').replace(/^\/+|\/+$/g, '')
            if (!/^[a-z][a-z-]*$/.test(safeAction)) throw new Error('商家插件操作名无效')
            return request.post({ url: `/advertising/${safeAction}`, params: body })
        }
    })
})

/**
 * 当前站点实例标识（域名，不含端口），形态与 `hou_market_plugin_license.instance_id`
 * 的域名类授权行一致。
 *
 * <p>口径与管理端 `utils/site.js#getSiteInstanceId` 一致：由域名统一派生，不人工填写，
 * 否则会绑到别的站点。
 *
 * <p><b>注意</b>：插件中心列表接口目前忽略此值、只认服务端解析的会话实例，
 * 详见 {@link loadSellerPlugins} 内注释。
 */
function sellerSiteInstanceId(): string {
    return typeof window === 'undefined' ? '' : window.location.hostname || ''
}

export async function loadSellerPlugins(menu: any[], plugins: any[] = []) {
    let available = plugins
    if (!available.length) {
        try {
            // 🔴 过滤条件必须放 **请求体**（`data`），不能用 `params`：
            // BFF `PluginCenterController#list` 只接 `@RequestBody Map`，
            // 查询串参数会被整个丢弃 —— 用 params 时 `status=enabled` 静默失效，
            // 商家端会拿到全量插件（含未启用的），再靠前端门禁过滤，口径与管理端不一致。
            // 管理端 `api/plugin-center` 一直是 `request.post(url, params)` 走 body。
            //
            // ⚠️ instanceId **不能**用来决定 licensed：后端
            // `PluginCenterService.listPlugins` 用 `SiteInstanceResolver.resolveOrNull()`
            // 服务端解析实例（`u-{会话 userId}`），并**丢弃**请求体里的 instanceId
            // ——那是防越权设计（否则任何登录用户都能拿别人站点的授权装付费插件）。
            // 带上它只是与管理端调用形态保持一致；真正决定商家端能否装载的是
            // 「授权表里有该会话派生实例的有效授权行」。
            const instanceId = sellerSiteInstanceId()
            const payload: Record<string, any> = { status: 'enabled' }
            if (instanceId) payload.instanceId = instanceId
            const response = await request.post(
                { url: '/web/plugin-center/list', data: payload },
                { urlPrefix: '' }
            )
            available = Array.isArray(response) ? response : response?.list || []
        } catch {
            available = []
        }
    }
    await registry.sync(menu, available)
}

export function loadSellerRouteView(component: string) {
    const view = registry.resolve(component)
    return view ? markRaw(view) : null
}

export function isSellerRouteAvailable(component: string) {
    return Boolean(registry.resolve(component))
}

export function unloadSellerPlugins() {
    return registry.reset()
}

export function sellerPluginRegistry() {
    return registry
}
