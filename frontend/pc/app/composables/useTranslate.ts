/**
 * 全局翻译 composable：语言包缓存 + 百度翻译回填，供 $t 使用。
 * cn 直接返回原文；非 cn 先查内存/session 缓存，未命中再请求百度翻译并回写缓存。
 * 仅在 getActivePinia() 存在时使用 mallStore，避免 SSR/插件阶段报错导致 500。
 */
import { ref, computed, watch } from 'vue'
import { getActivePinia } from 'pinia'
import { useMallStore } from '~/stores/modules/mall'
import { getI18nPackage, baiduTrans } from '~/services/thirdParty.service'

const cache = ref<Record<string, string>>({})
/** 缓存版本，递增后使依赖 $t 的视图重新渲染 */
const localeVersion = ref(0)
const pendingKeys = new Set<string>()
const SESSION_KEY_PREFIX = 'mall_pc_i18n_'

function normalizePack(res: any): Record<string, string> {
    if (res && typeof res === 'object' && res.data != null && typeof res.data === 'object') return res.data
    if (res && typeof res === 'object' && !Array.isArray(res) && typeof (res as any).code !== 'number') {
        return res as Record<string, string>
    }
    return {}
}

function readSessionPack(code: string): Record<string, string> {
    if (typeof sessionStorage === 'undefined') return {}
    try {
        const raw = sessionStorage.getItem(SESSION_KEY_PREFIX + code)
        if (!raw) return {}
        const parsed = JSON.parse(raw)
        return parsed && typeof parsed === 'object' ? parsed : {}
    } catch {
        return {}
    }
}

function writeSessionPack(code: string, pack: Record<string, string>) {
    if (typeof sessionStorage === 'undefined') return
    try {
        sessionStorage.setItem(SESSION_KEY_PREFIX + code, JSON.stringify(pack))
    } catch {
        // quota / private mode
    }
}

export function useTranslate() {
    const pinia = getActivePinia()
    const mallStore = pinia ? useMallStore(pinia) : null
    const currentLang = computed(() => mallStore?.getLanguageCode ?? 'cn')

    watch(
        currentLang,
        async (code: string) => {
            if (!code || code === 'cn') {
                cache.value = {}
                localeVersion.value++
                return
            }
            const sessionPack = readSessionPack(code)
            cache.value = { ...sessionPack }
            localeVersion.value++
            try {
                const res = await getI18nPackage(code)
                const pack = normalizePack(res)
                if (Object.keys(pack).length > 0) {
                    cache.value = { ...pack, ...cache.value }
                    writeSessionPack(code, cache.value)
                    localeVersion.value++
                }
            } catch {
                // 静态包失败不影响百度按需翻译
            }
        },
        { immediate: true }
    )

    function t(key: string): string {
        if (key == null || key === '') return ''
        const lang = currentLang.value
        if (!lang || lang === 'cn') return key
        localeVersion.value // 依赖版本，缓存更新后视图重算
        const hit = cache.value[key]
        if (hit != null && hit !== '') return hit
        if (!pendingKeys.has(key)) {
            pendingKeys.add(key)
            void fetchTrans(key, lang)
        }
        return key
    }

    return { t }
}

/** 请求百度翻译并写入缓存，完成后递增 localeVersion 触发重渲染 */
async function fetchTrans(key: string, to: string): Promise<void> {
    try {
        const res = await baiduTrans({ src: key, to, from: 'cn' })
        const dst = (res as any)?.data?.dst
        if (dst != null && dst !== '') {
            cache.value = { ...cache.value, [key]: dst }
            writeSessionPack(to, cache.value)
            localeVersion.value++
        }
    } catch {
        // 翻译失败时保留原文，不再重试
    } finally {
        pendingKeys.delete(key)
    }
}
