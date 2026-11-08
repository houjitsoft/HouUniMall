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

export async function loadSellerPlugins(menu: any[], plugins: any[] = []) {
    let available = plugins
    if (!available.length) {
        try {
            const response = await request.post(
                { url: '/web/plugin-center/list', params: { status: 'enabled' } },
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
