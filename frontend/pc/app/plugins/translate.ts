/**
 * 注册全局 $t：服务端/预渲染时直接返回原文，客户端使用 useTranslate 的翻译逻辑。
 * 另注册 $tg：商品名称展示（已本地化直接显示，中文走机翻兜底）。
 * 客户端采用延迟初始化：首次调用 $t 时才执行 useTranslate()，确保此时 Pinia 已激活，避免
 * "getActivePinia() was called but there was no active Pinia" 报错。
 *
 * hydration 完成前强制返回原文，与 SSR 一致，避免 localStorage 语言包导致 Hydration mismatch。
 */
import { getActivePinia } from 'pinia'
import { useMallStore } from '~/stores/modules/mall'
import { displayGoodsName } from '~/utils/displayGoodsName'

export default defineNuxtPlugin((nuxtApp) => {
    if (import.meta.server) {
        ;(nuxtApp.vueApp as any).config.globalProperties.$t = (key: string) => key
        ;(nuxtApp.vueApp as any).config.globalProperties.$tg = (key: string) => key
        return
    }

    let hydrated = false
    nuxtApp.hook('app:mounted', () => {
        hydrated = true
    })

    let cachedT: ((key: string) => string) | null = null
    const ensureT = () => {
        if (!cachedT) {
            const { t } = useTranslate()
            cachedT = t
        }
        return cachedT
    }

    ;(nuxtApp.vueApp as any).config.globalProperties.$t = (key: string) => {
        if (!hydrated) return key == null ? '' : key
        return ensureT()(key)
    }
    ;(nuxtApp.vueApp as any).config.globalProperties.$tg = (key: string) => {
        if (!hydrated) return key == null ? '' : key
        try {
            const pinia = getActivePinia()
            const lang = pinia ? useMallStore(pinia).getLanguageCode : 'cn'
            return displayGoodsName(key, ensureT(), lang)
        } catch {
            return key
        }
    }
})
