<template>
    <div class="plus-recharge">
        <PlusHeroShell>
            <div class="plus-recharge__wrap">
                <h1 id="plusRechargeTitle" class="plus-recharge__hero-title">{{ $t('购买PLUS会员') }}</h1>
            </div>
        </PlusHeroShell>

        <div class="plus-recharge__panel">
            <section class="plus-recharge__section" aria-label="卡种选择">
                <div class="plus-recharge__section-head">
                    <img :src="IMAGE_URL('plusImg/titleZuoX2.png')" alt="" class="plus-recharge__section-side" />
                    <h2 class="plus-recharge__section-title">{{ $t('卡种选择') }}</h2>
                    <img :src="IMAGE_URL('plusImg/titleYouX2.png')" alt="" class="plus-recharge__section-side" />
                </div>

                <div v-if="displayPackages.length" class="plus-recharge__cards-shell" role="list">
                    <PlusPackageCard :packages="displayPackages" :active-key="activePackageKey"
                        variant-prefix="plus-card--" title-style="recharge" title-tag="h3" :show-check="true"
                        @select="selectPackage" />
                </div>
            </section>

            <section class="plus-recharge__section" aria-label="支付与协议">
                <p class="plus-recharge__agreement-hint">
                    {{ $t('购买即视为同意') }}
                    <button type="button" class="plus-recharge__agreement-link" @click="onAgreementClick">
                        {{ $t('《PLUS会员用户协议》') }}
                    </button>
                </p>

                <div class="plus-recharge__pay-bar">
                    <div class="plus-recharge__pay-amount">
                        <span class="plus-recharge__pay-label">{{ $t('应付金额：') }}</span>
                        <span class="plus-recharge__pay-num">{{
                            formatPriceWithSymbolSafe(activePackage?.thaliPrice)
                            }}</span>
                        <span class="plus-recharge__pay-unit">/ {{ amountPeriodWord }}</span>
                    </div>
                    <button type="button" class="plus-recharge__pay-btn" @click="onAgreeAndPay">
                        {{ $t('同意协议并支付') }}
                    </button>
                </div>
            </section>

            <section class="plus-recharge__equity" aria-label="卡内权益">
                <div class="plus-recharge__section-head plus-recharge__section-head--tight">
                    <img :src="IMAGE_URL('plusImg/titleZuoX2.png')" alt="" class="plus-recharge__section-side" />
                    <h2 class="plus-recharge__section-title">{{ $t('卡内权益') }}</h2>
                    <img :src="IMAGE_URL('plusImg/titleYouX2.png')" alt="" class="plus-recharge__section-side" />
                </div>
                <p class="plus-recharge__equity-sub">{{ privilegeSubtitle }}</p>

                <div class="plus-recharge__equity-rows">
                    <div class="plus-recharge__equity-row plus-recharge__equity-row--5" role="list">
                        <article v-for="slot in equitySlots" :key="slot.type" role="listitem"
                            class="plus-recharge__equity-item" :style="{ backgroundImage: `url(${slot.bg})` }">
                            <div class="eq__inner">
                                <template v-if="slot.type === 1">
                                    <div class="eq__name" :class="`eq__name--type-${slot.type}`">{{
                                        $t('尊享会员价') }}</div>
                                    <div class="eq__desc">
                                        <span>PLUS<i class="eq__vip-type"></i>{{ $t(planShortLabel) }}{{ $t('会员可在当前会员等级的基础上享受购物再降')
                                            }}</span>
                                        <span class="eq__value">{{ equityDesc(slot.eq) || '--' }}</span>
                                        <span>{{ $t('折扣') }}</span>
                                    </div>
                                </template>

                                <template v-else-if="slot.type === 2">
                                    <div class="eq__name" :class="`eq__name--type-${slot.type}`">{{
                                        $t('无门槛红包') }}</div>
                                    <div class="eq__desc">
                                        <span>PLUS<i class="eq__vip-type"></i>{{ $t(planShortLabel) }}{{ $t('会员尊享') }}</span>
                                        <span class="eq__with-list">
                                            <i class="eq__link">{{ $t('无门槛红包') }}</i>
                                            <ul v-if="slot.eq?.redList?.length" class="eq__red-list">
                                                <li v-for="(value, index) in slot.eq?.redList"
                                                    :key="value.redpacketTId ?? index">
                                                    <p class="eq__red-item-idx"><i>{{ index + 1 }}.</i></p>
                                                    <p class="eq__red-item-main">
                                                        <span>{{ value.redpacketTTitle }}{{
                                                            value.redpacketTPrice
                                                            }}{{ $t('元红包') }}</span>
                                                    </p>
                                                </li>
                                            </ul>
                                        </span>
                                        <span>{{ $t('元消费红包，全品类商品均可用') }}</span>
                                    </div>
                                </template>

                                <template v-else-if="slot.type === 3">
                                    <div class="eq__name" :class="`eq__name--type-${slot.type}`">{{
                                        $t('红包券') }}</div>
                                    <div class="eq__desc">
                                        <span>PLUS<i class="eq__vip-type"></i>{{ $t(planShortLabel) }}{{ $t('会员每月定期领取') }}{{
                                            slot.eq?.redList?.length ?? 0 }}{{ $t('张') }}</span>
                                        <span class="eq__with-list">
                                            <i class="eq__link">{{ $t('红包消费券') }}</i>
                                            <ol v-if="slot.eq?.redList?.length" class="eq__red-list eq__red-list--ol">
                                                <li v-for="(value, index) in slot.eq?.redList"
                                                    :key="value.redpacketTId ?? index">
                                                    <p class="eq__red-item-idx"><i>{{ index + 1 }}.</i></p>
                                                    <p class="eq__red-item-main">
                                                        <span>{{ value.redpacketTTitle }},{{ $t('满') }}{{
                                                            value.redpacketTOrderlimit }}{{ $t('减') }}{{
                                                                value.redpacketTPrice }}{{ $t('元红包') }}</span>
                                                    </p>
                                                </li>
                                            </ol>
                                        </span>
                                        <span>，{{ $t('全品类商品均可用') }}</span>
                                    </div>
                                </template>

                                <template v-else-if="slot.type === 4">
                                    <div class="eq__name" :class="`eq__name--type-${slot.type}`">{{
                                        $t('赠送积分') }}</div>
                                    <div class="eq__desc">
                                        <span>{{ $t('开通PLUS') }}<i class="eq__vip-type"></i>{{ $t(planShortLabel) }}{{ $t('会员即可获得')
                                            }}</span>
                                        <span class="eq__value">{{ equityDesc(slot.eq) || '--' }}</span>
                                        <span>{{ $t('积分') }}</span>
                                    </div>
                                </template>

                                <template v-else-if="slot.type === 5">
                                    <div class="eq__name" :class="`eq__name--type-${slot.type}`">{{
                                        $t('购物积分翻倍') }}</div>
                                    <div class="eq__desc">
                                        <span>{{ $t('开通PLUS') }}<i class="eq__vip-type"></i>{{ $t(planShortLabel) }}{{
                                            $t('会员提升下单积分倍率 , 订单金额 : 积分=') }}</span>
                                        <span class="eq__value">{{ equityDesc(slot.eq) || '--' }}</span>
                                    </div>
                                </template>
                            </div>
                        </article>
                    </div>
                </div>
            </section>
        </div>

        <PopupModal v-model="agreementVisible" :title="$t('PLUS会员用户协议')" width="min(920px, 92vw)"
            content-class="plus-recharge__agreement-modal">
            <Loading v-if="agreementLoading" compact :text="$t('加载中...')" />
            <div v-else class="plus-recharge__agreement-body" v-html="agreementHtml" />
        </PopupModal>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { selectPlusMember } from '~/services/userms.service'
import { selectPlusUser } from '~/services/base.service'
import type { PlusEquity, PlusMemberPackageDatum } from '~/types/plus'
import PlusPackageCard from '~/components/member/PlusPackageCard.vue'
import PlusHeroShell from '~/components/member/PlusHeroShell.vue'
import PopupModal from '~/components/Modal/PopupModal.vue'
import { ConfirmModal } from '~/composables/useConfirmModal'
import Loading from '~/components/Load/Loading.vue'
import { usePaymentStore } from '~/stores'

definePageMeta({ layout: 'full-screen' })

const route = useRoute()
const paymentStore = usePaymentStore()

const plusPackages = ref<PlusMemberPackageDatum[]>([])
const activePackageKey = ref<string>('t-1')

const agreementVisible = ref(false)
const agreementLoading = ref(false)
const agreementHtml = ref<string>('')

function normalizeKeyFromQuery(): string | null {
    const q = route.query as Record<string, unknown>
    const thaliId = q.thaliId != null ? Number(q.thaliId) : Number.NaN
    if (Number.isFinite(thaliId) && thaliId > 0) return `id-${thaliId}`
    const thaliType = q.thaliType != null ? Number(q.thaliType) : Number.NaN
    if (Number.isFinite(thaliType) && thaliType > 0) return `t-${thaliType}`
    return null
}

const initialKey = normalizeKeyFromQuery()
if (initialKey) activePackageKey.value = initialKey

const displayPackages = computed(() => {
    const list = plusPackages.value.filter((p) => (p.thaliStatus ?? 1) === 1)
    return [...list].sort((a, b) => (a.thaliType ?? 0) - (b.thaliType ?? 0))
})

watch(
    displayPackages,
    (list) => {
        if (!list.length) return
        const has = list.some(
            (p) => `id-${p.thaliId}` === activePackageKey.value || `t-${p.thaliType}` === activePackageKey.value
        )
        if (has) return
        const month = list.find((p) => p.thaliType === 1)
        const pick = month ?? list[0]
        if (!pick) return
        activePackageKey.value =
            pick.thaliId != null && pick.thaliId > 0 ? `id-${pick.thaliId}` : `t-${pick.thaliType ?? 0}`
    },
    { immediate: true }
)

const activePackage = computed<PlusMemberPackageDatum | null>(() => {
    const list = displayPackages.value
    const key = activePackageKey.value
    const byId = list.find((p) => `id-${p.thaliId}` === key)
    if (byId) return byId
    const byType = list.find((p) => `t-${p.thaliType}` === key)
    return byType ?? list[0] ?? null
})

const equityList = computed<PlusEquity[]>(() => {
    const raw = activePackage.value?.plusEquitys
    if (!Array.isArray(raw)) return []
    return raw.filter((e) => (e.equityStatus ?? 1) === 1)
})

/** 权益类型 1–5 与五张背景图一一对应（与设计一致） */
const EQUITY_TYPE_ORDER = [1, 2, 3, 4, 5] as const
const equityBgByType: Record<number, string> = {
    1: IMAGE_URL('plusImg/equity_1X2.png'),
    2: IMAGE_URL('plusImg/equity_4X2.png'),
    3: IMAGE_URL('plusImg/equity_5X2.png'),
    4: IMAGE_URL('plusImg/equity_3X2.png'),
    5: IMAGE_URL('plusImg/equity_2X2.png'),
}

function equityByType(t: number): PlusEquity | null {
    return equityList.value.find((e) => Number(e.equityType) === t) ?? null
}

const equitySlots = computed(() =>
    EQUITY_TYPE_ORDER.map((type) => ({
        type,
        bg: equityBgByType[type] ?? '',
        eq: equityByType(type),
    })).filter((slot) => slot.eq)
)

const privilegeCount = computed(() => {
    const n = activePackage.value?.equityTotal
    if (typeof n === 'number' && n > 0) return n
    return equityList.value.length || 5
})

const planShortLabel = computed(() => {
    const t = activePackage.value?.thaliType
    if (t === 1) return '月卡'
    if (t === 2) return '季卡'
    if (t === 3) return '年卡'
    return '会员'
})

const privilegeSubtitle = computed(() => `${planShortLabel.value}尊享${privilegeCount.value}大特权`)

const amountPeriodWord = computed(() => {
    const t = activePackage.value?.thaliType
    if (t === 1) return '月'
    if (t === 2) return '季'
    if (t === 3) return '年'
    return '月'
})

function equityDesc(eq: PlusEquity | null): string {
    if (!eq) return ''
    const v = eq.equityValue
    return typeof v === 'string' && v.trim() ? v.trim() : ''
}

function packageKey(pkg: PlusMemberPackageDatum): string {
    if (pkg.thaliId != null && pkg.thaliId > 0) return `id-${pkg.thaliId}`
    return `t-${pkg.thaliType ?? 0}`
}

function selectPackage(pkg: PlusMemberPackageDatum) {
    activePackageKey.value = packageKey(pkg)
}

function onAgreementClick() {
    void (async () => {
        try {
            agreementLoading.value = true
            agreementVisible.value = true
            const res = await selectPlusUser()
            if (res.code === 1 && typeof res.data === 'string') {
                agreementHtml.value = res.data
                return
            }
            agreementVisible.value = false
            await ConfirmModal.confirm({
                title: '温馨提示',
                message: res.message || '获取协议失败',
                confirmText: '确定',
                cancelText: '取消',
            })
        } catch (e) {
            agreementVisible.value = false
            await ConfirmModal.confirm({
                title: '温馨提示',
                message: '系统繁忙，请稍后再试',
                confirmText: '确定',
                cancelText: '取消',
            })
            if (import.meta.dev) console.error('[plusRecharge] selectPlusUser failed', e)
        } finally {
            agreementLoading.value = false
        }
    })()
}

function onAgreeAndPay() {
    const p = activePackage.value
    if (!p) return
    paymentStore.setPlusPaymentContext({
        payPrice: Number(p.thaliPrice ?? 0),
        thaliEquity: String(p.thaliEquity ?? ''),
        thaliType: Number(p.thaliType ?? 0),
    })
    navigateTo('/payment/online?plusStatus=1')
    if (import.meta.dev) {
        console.debug('[plusRecharge] pay', p.thaliId, p.thaliType, p.thaliPrice)
    }
}

onMounted(async () => {
    try {
        const res = await selectPlusMember()
        if (res.code === 1 && Array.isArray(res.data)) {
            plusPackages.value = res.data
        }
    } catch (e) {
        console.error('selectPlusMember failed:', e)
    }
})
</script>

<style lang="scss" scoped>
.plus-recharge {
    width: 100%;
    min-height: 100%;
    padding: 0 0 56px;
    box-sizing: border-box;
}

.plus-recharge__agreement-modal {
    .modal-body {
        padding: 0;
    }
}

.plus-recharge__agreement-body {
    max-height: min(70vh, 620px);
    overflow: auto;
    padding: 24px 20px;
    color: rgba(0, 0, 0, 0.82);
    font-size: 14px;
    line-height: 1.75;

    :deep(p) {
        margin: 0 0 12px;
    }

    :deep(img) {
        max-width: 100%;
        height: auto;
    }
}

.plus-recharge__wrap {
    position: relative;
    z-index: 1;
    max-width: 1200px;
    margin: 0 auto;
    padding: 80px clamp(16px, 4vw, 28px) calc(40px + 156px * 0.32);
    display: flex;
    flex-direction: column;
    align-items: center;
    box-sizing: border-box;
}

.plus-recharge__hero-title {
    margin: 0 0 18px;
    text-align: center;
    font-size: 50px;
    font-weight: 800;
    line-height: 1.25;
    letter-spacing: 0.1em;
    background: linear-gradient(180deg, #fff6e8 0%, #e8c078 45%, #c9a063 100%);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    filter: drop-shadow(0 2px 10px rgba(0, 0, 0, 0.45));
}

.plus-recharge__panel {
    position: relative;
    z-index: 10;
    width: 1200px;
    max-width: 100%;
    margin: 0 auto;
    transform: translateY(-50px);
    background: linear-gradient(180deg, #fffefb 0%, #f5f0e8 55%, #ebe4da 100%);
    border-radius: 18px;
    padding: 28px clamp(18px, 3vw, 36px) 32px;
    box-shadow:
        0 4px 0 rgba(255, 255, 255, 0.55) inset,
        0 24px 48px rgba(0, 0, 0, 0.35);
    border: 1px solid rgba(201, 160, 99, 0.22);
}

.plus-recharge__section-head {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    margin: 0 0 22px;

    &--tight {
        margin-top: 28px;
        margin-bottom: 10px;
    }
}

.plus-recharge__section-side {
    width: 99px;
    height: 22px;
    object-fit: contain;
    display: block;
    pointer-events: none;
}

.plus-recharge__section-title {
    margin: 0;
    font-size: 32px;
    font-weight: 800;
    color: #3d2914;
    letter-spacing: 0.2em;
    white-space: nowrap;
}

.plus-recharge__cards-shell {
    width: 100%;
    padding: 0;
    box-sizing: border-box;
    margin-bottom: 18px;
    display: flex;
    flex-wrap: nowrap;
    justify-content: center;
    gap: clamp(16px, 3vw, 20px);
    overflow: visible;
}

/* `.plus-recharge__cards` wrapper removed */


.plus-recharge__agreement-hint {
    margin: 0 0 14px;
    font-size: 12px;
    color: #6b5c4a;
    text-align: center;
    line-height: 1.6;
}

.plus-recharge__section[aria-label='支付与协议'] {
    margin-top: 50px;
}

.plus-recharge__agreement-link {
    padding: 0;
    border: none;
    background: none;
    font: inherit;
    color: #8b6914;
    text-decoration: underline;
    cursor: pointer;

    &:hover {
        color: #5c470e;
    }
}

.plus-recharge__pay-bar {
    display: flex;
    flex-wrap: nowrap;
    align-items: stretch;
    width: auto;
    margin: 0 auto 8px;
    justify-content: center;
    border-radius: 999px;
    overflow: visible;
    position: relative;
    box-shadow: none;
}

.plus-recharge__pay-amount {
    display: inline-block;
    width: 259px;
    height: 60px;
    background: linear-gradient(147deg, #FEE4C8 0%, #FCC996 100%);
    box-shadow: none;
    border-radius: 100px 10px 200px 100px;
    border: 1px solid #FFFFFF;
    transform: translateZ(1px);
    text-align: center;
    line-height: 60px;
    font-weight: 500;
    text-shadow: 0px 2px 6px rgba(0, 0, 0, 0.1);
    color: #4A2A14;
    margin-right: -26px;
    position: relative;
    z-index: 2;
}

.plus-recharge__pay-label {
    font-weight: 400;
}

.plus-recharge__pay-num {
    font-size: 18px;
    font-weight: 400;
    color: #c62828;
}

.plus-recharge__pay-unit {
    font-weight: 400;
    color: #6b4b2a;
}

.plus-recharge__pay-btn {
    display: inline-block;
    width: 274px;
    height: 60px;
    background: linear-gradient(33deg, #94A2C1 0%, #515F83 100%);
    box-shadow: none;
    border-radius: 200px 100px 100px 10px;
    border: 1px solid #FFFFFF;
    margin-left: -55px;
    text-align: center;
    line-height: 60px;
    font-weight: 500;
    text-shadow: 0px 2px 6px rgba(0, 0, 0, 0.1);
    color: #FFFFFF;
    cursor: pointer;
    position: relative;
    z-index: 1;
    padding-left: 20px;
}

.plus-recharge__equity-sub {
    margin: 0 0 20px;
    font-size: 13px;
    font-weight: 600;
    color: #6b5c4a;
    text-align: center;
}

.plus-recharge__equity-rows {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.plus-recharge__equity-row {
    display: grid;
    gap: 14px;
    justify-content: center;
}

.plus-recharge__equity-row--5 .plus-recharge__equity-item {
    min-height: 148px;
}

.plus-recharge__equity-row--5 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    align-items: stretch;
}

.plus-recharge__equity-item {
    position: relative;
    border-radius: 12px;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    overflow: visible;
    box-shadow: 0 10px 24px rgba(15, 18, 30, 0.15);
}

.eq__inner {
    position: absolute;
    left: 125px;
    top: 0;
    right: auto;
    bottom: auto;
    padding: 14px 16px 16px;
    background: transparent;
    text-align: left;
    box-sizing: border-box;
}

.eq__name {
    font-size: 16px;
    font-weight: 400;
    color: #474A4B;
    line-height: 1.25;
    max-width: 100%;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin-top: 10px;
}

.eq__name--type-1 {
    color: #474A4B;
    background: linear-gradient(180deg, #AE6B3D 0%, #4A2A14 100%);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.eq__name--type-2 {
    color: #e39934;
    background: linear-gradient(180deg, #F6A347 0%, #AD7D00 100%);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.eq__name--type-3 {
    color: #5FF4B2;
    background: linear-gradient(180deg, #04E883 0%, #18BC66 100%);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.eq__name--type-4 {
    color: #FF4A47;
    background: linear-gradient(180deg, #FF8381 0%, #FF4A47 100%);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.eq__name--type-5 {
    color: #3ba1fc;
    background: linear-gradient(180deg, #67BEFF 0%, #1386F8 100%);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.eq__desc {
    margin-top: 15px;
    font-size: 12px;
    font-weight: 400;
    line-height: 1.45;
    color: #666666;
    display: flex;
    flex-wrap: wrap;
    gap: 6px 6px;
}

.eq__value {
    font-weight: 900;
}

.eq__vip-type {
    display: inline-block;
    width: 10px;
    height: 10px;
    margin: 0 4px;
    line-height: 16px;
    vertical-align: middle;
}

.eq__with-list {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.eq__link {
    color: #409EFF;
    cursor: pointer;
    font-style: normal;
    line-height: 1.6;
}

.eq__red-list {
    position: absolute;
    left: 0;
    bottom: calc(100% + 8px);
    width: 260px;
    max-width: 70vw;
    margin: 0;
    padding: 10px 12px;
    list-style: none;
    border-radius: 10px;
    background: linear-gradient(110deg, #FFF5E2 0%, #F6E1AD 100%);
    color: #965D20;
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.18);
    display: none;
    z-index: 10;
}

.eq__red-list--ol {
    width: 220px;
    list-style: decimal;
    padding-left: 22px;
}

.eq__with-list:hover .eq__red-list {
    display: block;
}

.eq__red-list li {
    display: flex;
    gap: 8px;
    padding: 6px 0;
}

.eq__red-item-idx {
    margin: 0;
    flex: 0 0 auto;
    color: rgba(0, 0, 0, 0.6);
}

.eq__red-item-idx i {
    line-height: 1.6;
}

.eq__red-item-main {
    margin: 0;
    flex: 1 1 auto;
}

@media (max-width: 900px) {
    .plus-recharge__cards-shell {
        justify-content: flex-start;
    }

    :deep(.plus-hero-shell) {
        min-height: 500px;
        padding-bottom: 120px;
    }

    .plus-recharge__panel {
        max-width: none;
        margin: 12px clamp(16px, 4vw, 28px) 0;
    }

    .plus-recharge__equity-row--5 .plus-recharge__equity-item {
        max-width: 100%;
    }

    .plus-recharge__pay-bar {
        border-radius: 14px;
        flex-direction: column;
    }

    .plus-recharge__pay-amount {
        border-right: 1px solid rgba(201, 160, 99, 0.35);
        border-bottom: none;
    }

    .plus-recharge__pay-btn {
        flex: 1 1 auto;
    }
}
</style>
