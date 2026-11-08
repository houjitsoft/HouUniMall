import { computed } from 'vue'
import { queryMerchantPlusRule } from '~/services/base.service'
import { getUserMemberInformation } from '~/services/userms.service'
import { usePlusStore } from '~/stores/modules/plus'
import { useUserStore } from '~/stores/modules/user'

const PLUS_OPEN_TEXT = '开通会员立省更多>'
const PLUS_RENEW_TEXT = '现在续费PLUS享特惠价>'

/** 并发合并：多处同时 await 只触发一轮请求 */
let refreshPromise: Promise<void> | null = null

function parseIsPlus(v: unknown): boolean {
    return v === 1 || v === '1'
}

/**
 * 刷新 PLUS 状态：未登录仅拉平台开关；已登录先拉平台开关，开启后再拉用户会员信息。
 * 与布局内 Header 搭配即可；子组件只读 `usePlusStore` / `usePlus()`，无需重复调用。
 */
export async function refreshPlusState(): Promise<void> {
    if (refreshPromise) return refreshPromise

    const plusStore = usePlusStore()
    const userStore = useUserStore()

    refreshPromise = (async () => {
        try {
            const ruleRes = await queryMerchantPlusRule({})
            const ok = ruleRes?.code === 1
            plusStore.setPlatformPlusFromResponse(ruleRes?.data, ok)

            if (!userStore.isLogin || !plusStore.platformPlusEnabled) {
                plusStore.clearUserMember()
                return
            }

            const memRes = await getUserMemberInformation({})
            if (memRes?.code === 1 && memRes.data) {
                const d = memRes.data
                plusStore.setUserMemberFromResponse(parseIsPlus(d.isplusMember), d.memberName)
            } else {
                plusStore.clearUserMember()
            }
        } catch (e) {
            console.error('refreshPlusState failed:', e)
            plusStore.setPlatformLoadFailed()
            plusStore.clearUserMember()
        } finally {
            refreshPromise = null
        }
    })()

    return refreshPromise
}

export function usePlus() {
    const plusStore = usePlusStore()
    const userStore = useUserStore()

    const showPlusEntry = computed(() => plusStore.showPlusEntry)

    const plusActionText = computed(() => {
        if (!showPlusEntry.value) return ''
        if (!userStore.isLogin) return PLUS_OPEN_TEXT
        return plusStore.isPlusMember ? PLUS_RENEW_TEXT : PLUS_OPEN_TEXT
    })

    const showHeaderPlusBadge = computed(() => userStore.isLogin && showPlusEntry.value)

    return {
        refreshPlusState,
        showPlusEntry,
        plusActionText,
        showHeaderPlusBadge,
        plusStore
    }
}

export { PLUS_OPEN_TEXT, PLUS_RENEW_TEXT }
