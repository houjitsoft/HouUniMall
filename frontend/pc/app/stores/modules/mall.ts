import { defineStore } from 'pinia'
import type { NavLogoConfig, BaseCurrencySymbolEntity } from '~/types/mall'
import type { MallState } from '~/types/store'
import { mallPersistStorage } from '~/utils/mallPersistStorage'

export const useMallStore = defineStore('mall', {
    // 状态
    state: (): MallState => ({
        navLogoConfigs: [],
        baseCurrencySymbolEntity: null,
        languageCode: null,
        homePlatformRedpacketClosed: false,
        homePlatformRedpacketEntryVisible: false
    }),

    // 计算属性
    getters: {
        // 获取导航Logo和文案配置
        getNavLogoConfigs: (state) => Array.isArray(state.navLogoConfigs) ? state.navLogoConfigs : [],
        // 获取导航Logo和文案配置的 Map 形式
        getNavLogoConfigMap: (state) => {
            const configs = Array.isArray(state.navLogoConfigs) ? state.navLogoConfigs : []
            return new Map<string, string>(configs.map(config => [config.configKey, config.configValue]))
        },
        // 商城支付 logo URL（key: logo_copywriting_ico_pay，类型为 url）
        getPaymentLogoUrl: (state) => {
            const configs = Array.isArray(state.navLogoConfigs) ? state.navLogoConfigs : []
            return configs.find(c => c.configKey === 'logo_copywriting_ico_pay')?.configValue || ''
        },
        // 获取当前启用的货币符号（status===1 仅有一项）
        getBaseCurrencySymbolEntity: (state) => state.baseCurrencySymbolEntity,
        // 获取当前选中的语言编码（如 en、jp、ru）
        getLanguageCode: (state) => state.languageCode,
        // 获取首页平台红包关闭状态
        getHomePlatformRedpacketClosed: (state) => state.homePlatformRedpacketClosed,
        // 获取首页平台红包浮标是否展示
        getHomePlatformRedpacketEntryVisible: (state) => state.homePlatformRedpacketEntryVisible
    },

    // 操作方法
    actions: {
        // 设置导航Logo和文案配置
        setNavLogoConfigs(configs: NavLogoConfig[]) {
            // 接口异常或旧版 localStorage 可能写入对象，始终保持数组，避免 map/find 在 SSR 期间崩溃
            const raw = configs as unknown
            const list = Array.isArray(raw)
                ? raw
                : (raw && typeof raw === 'object'
                    ? ((raw as any).list || (raw as any).configs || (raw as any).data || [])
                    : [])
            this.navLogoConfigs = Array.isArray(list) ? list.filter(Boolean) : []
        },

        /**
         * 设置币种列表并同步当前选中项：若已有持久化的选中项且仍在列表中则保留，否则用接口里 status===1 的项
         * @param entities 来自 queryCountryAndCurrency 的 baseCurrencySymbolEntities
         */
        setBaseCurrencySymbolEntities(entities: BaseCurrencySymbolEntity[]) {
            const list = Array.isArray(entities) ? entities : []
            const defaultByStatus = list.find((e) => e.status === 1)
            if (this.baseCurrencySymbolEntity?.id != null) {
                const stillInList = list.find((e) => e.id === this.baseCurrencySymbolEntity!.id)
                if (stillInList) {
                    this.baseCurrencySymbolEntity = stillInList
                    return
                }
            }
            this.baseCurrencySymbolEntity = defaultByStatus ?? null
        },

        /**
         * 直接设置当前选中的货币（用于语言/币种下拉切换）
         * @param entity 选中的货币实体，传 null 可清空
         */
        setBaseCurrencySymbolEntity(entity: BaseCurrencySymbolEntity | null) {
            this.baseCurrencySymbolEntity = entity
        },

        /**
         * 设置当前选中的语言编码（由 pinia persist 写入 sessionStorage）
         * @param code 语言编码，如 'en'、'jp'、'ru'，传 null 表示清空
         */
        setLanguageCode(code: string | null): void {
            this.languageCode = code
        },

        /**
         * 设置首页平台红包关闭状态
         * @param closed true 表示已关闭，不再展示
         */
        setHomePlatformRedpacketClosed(closed: boolean): void {
            this.homePlatformRedpacketClosed = closed
        },

        /**
         * 设置首页平台红包浮标是否展示（接口驱动，不写 persist）
         * @param visible 是否展示浮标
         */
        setHomePlatformRedpacketEntryVisible(visible: boolean): void {
            this.homePlatformRedpacketEntryVisible = visible
        },

        // 根据配置键获取配置值
        getConfigValueByKey(key: string): string | undefined {
            return this.getNavLogoConfigMap.get(key)
        },

        // 清除导航Logo和文案配置
        clearNavLogoConfigs() {
            this.navLogoConfigs = []
        }
    },

    persist: {
        key: 'mall-state',
        // hydration 前不读 localStorage，见 mallPersistStorage / hydration-ready.client
        storage: mallPersistStorage,
        pick: ['navLogoConfigs', 'baseCurrencySymbolEntity', 'languageCode', 'homePlatformRedpacketClosed']
    } as any
})
