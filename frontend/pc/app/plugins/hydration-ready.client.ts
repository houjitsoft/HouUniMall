/**
 * 标记 hydration 完成，并在之后延迟恢复 mall localStorage 偏好。
 * 过早 $patch 币种/汇率会改价格 DOM，触发 Hydration mismatch。
 */
import { readMallPersistState } from '~/utils/mallPersistStorage'
import { useMallStore } from '~/stores/modules/mall'
import type { BaseCurrencySymbolEntity, NavLogoConfig } from '~/types/mall'

export default defineNuxtPlugin((nuxtApp) => {
    if (import.meta.server) return

    const ready = ref(false)

    const restoreMallPersist = () => {
        try {
            ;(window as any).__HOU_MALL_HYDRATED__ = true
        } catch {
            // ignore
        }
        ready.value = true

        try {
            const data = readMallPersistState()
            if (!data) return
            const mallStore = useMallStore()
            if (Array.isArray(data.navLogoConfigs)) {
                mallStore.setNavLogoConfigs(data.navLogoConfigs as NavLogoConfig[])
            }
            if (data.baseCurrencySymbolEntity && typeof data.baseCurrencySymbolEntity === 'object') {
                mallStore.setBaseCurrencySymbolEntity(
                    data.baseCurrencySymbolEntity as BaseCurrencySymbolEntity
                )
            }
            if (typeof data.languageCode === 'string' || data.languageCode === null) {
                mallStore.setLanguageCode((data.languageCode as string | null) ?? null)
            }
            if (typeof data.homePlatformRedpacketClosed === 'boolean') {
                mallStore.setHomePlatformRedpacketClosed(data.homePlatformRedpacketClosed)
            }
        } catch (e) {
            console.warn('mall persist manual restore skipped:', e)
        }
    }

    nuxtApp.hook('app:mounted', () => {
        // 两帧 + 短延时，确保越过 Vue hydrate 校验窗口
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                setTimeout(restoreMallPersist, 50)
            })
        })
    })

    return {
        provide: {
            mallHydrated: ready
        }
    }
})
