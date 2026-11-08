<template>
    <div class="plus-landing" :style="{ backgroundImage: `url(${IMAGE_URL('plusImg/nokaitong.jpg')})` }">
        <!-- 主视觉：背景 + 顶栏叠层 + 套餐卡 -->
        <PlusHeroShell>

            <div class="plus-hero-body">
                <h1 class="plus-hero-title">
                    <span class="plus-hero-title-lead">{{ $t("PLUS会员") }}</span>
                    <span class="plus-hero-title-tail">{{ $t("享专属特权") }}</span>
                </h1>
                <p class="plus-hero-sub">{{ $t(heroSubText) }}</p>
            </div>

            <!-- 错叠在 hero 底边：约 70% 在背景内、约 30% 伸入下方浅灰区 -->
            <div class="plus-cards-shell">
                <div class="plus-cards">
                    <PlusPackageCard :packages="displayPackages" :active-key="activePackageKey"
                        variant-prefix="plus-card--" title-style="plus" title-tag="h2" :show-open="true"
                        :open-text="$t('立即开通')" @select="selectPackage" @open="handleOpen" />
                </div>
            </div>
        </PlusHeroShell>

        <!-- 权益区：概览条 + 详情块（数据来自当前选中套餐的 plusEquitys） -->
        <section class="plus-body">
            <div class="plus-body-inner">
                <!-- 会员特权 -->
                <div class="plus-privileges-heading" aria-label="PLUS会员专属特权">
                    <img :src="IMAGE_URL('plusImg/titleZuoX2.png')" alt="" class="plus-privileges-heading__side" />
                    <span class="plus-privileges-heading__text">{{ $t("PLUS会员专属特权") }}</span>
                    <img :src="IMAGE_URL('plusImg/titleYouX2.png')" alt="" class="plus-privileges-heading__side" />
                </div>
                <PlusPrivilegesBar :background-style="plusBenefitFbagiveBg" :equities="overviewEquities" :max-icons="5">
                    <template #title>{{ $t(privilegesTitle) }}</template>
                </PlusPrivilegesBar>

                <div class="plus-benefit-list">
                    <div v-for="(eq, idx) in plusBenefitDetailEquities" :key="eq.equityId ?? `deq-${idx}`"
                        class="plus-benefit-block">
                        <div class="plus-benefit-next">
                            <img :src="IMAGE_URL('plusImg/nextX2.png')" alt="" class="plus-benefit-next__img" />
                        </div>

                        <article class="plus-benefit" :class="equityBlockClass(eq)" :style="plusBenefitFbagiveBg">
                            <!-- 1 PLUS专属折扣 -->
                            <div v-if="equityTypeNum(eq) === 1" class="benefit-fig-layout">
                                <div class="benefit-fig-copy">
                                    <p class="benefit-fig-kicker">{{ $t("会员尊享") }}</p>
                                    <h4 class="benefit-fig-title">{{ $t("折上折") }}</h4>
                                    <div class="benefit-fig-divider" />
                                    <p class="benefit-fig-desc">{{ benefitDescription(eq) }}</p>
                                    <button type="button" class="plus-benefit-btn" @click="openActivePackage">
                                        {{ benefitCta(eq) }}
                                    </button>
                                </div>
                                <div class="benefit-fig-visual benefit-fig-visual--t1">
                                    <div class="viz-t1-coupon" :style="plusVizT1ZhekouBg">
                                        <div class="viz-t1-coupon__left">
                                            <div class="viz-t1-coupon__discount">
                                                <span class="viz-t1-coupon__num">{{ formatDiscountShow(eq) }}</span>
                                                <span class="viz-t1-coupon__unit">{{ $t("折") }}</span>
                                            </div>
                                            <p class="viz-t1-coupon__sub">{{ $t("会员折扣直降") }}</p>
                                        </div>
                                        <div class="viz-t1-coupon__tear" aria-hidden="true" />
                                        <div class="viz-t1-coupon__right">
                                            <p class="viz-t1-coupon__rtitle">{{ $t("PLUS商品直降") }}</p>
                                            <p class="viz-t1-coupon__rsub">{{ $t("可与商品优惠活动叠加") }}</p>
                                            <p class="viz-t1-coupon__foot">
                                                {{ $t('总折扣限额') }}{{ discountLimitYuan(eq) }}{{ $t("元，一单回本") }}
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- 3 红包券 -->
                            <div v-else-if="equityTypeNum(eq) === 3" class="benefit-fig-layout">
                                <div class="benefit-fig-copy">
                                    <p class="benefit-fig-lead-gold">{{ $t(eq.equityValue || "100 元/月") }}</p>
                                    <h4 class="benefit-fig-title">{{ $t(eq.equityName || "红包大礼包") }}</h4>
                                    <div class="benefit-fig-divider" />
                                    <p class="benefit-fig-desc">{{ benefitDescription(eq) }}</p>
                                    <button type="button" class="plus-benefit-btn" @click="openActivePackage">
                                        {{ benefitCta(eq) }}
                                    </button>
                                </div>
                                <div class="benefit-fig-visual benefit-fig-visual--t2">
                                    <div class="viz-t2-scene"
                                        :style="{ backgroundImage: `url(${IMAGE_URL('plusImg/quanX2.png')})` }">
                                        <div class="viz-t2-coupon-list">
                                            <div v-for="(r, idx) in equityRedListForT2(eq)"
                                                :key="r.redpacketTId ?? `r-${idx}`" class="viz-t2-coupon-mini"
                                                :style="{ backgroundImage: `url(${IMAGE_URL('plusImg/hongbao.png')})` }">
                                                <span class="viz-t2-coupon-mini__p">{{ formatRedPacketTPrice(r)
                                                }}</span>
                                                <span class="viz-t2-coupon-mini__m">{{ $t("全品类") }}</span>
                                                <span class="viz-t2-coupon-mini__r">{{ formatRedPacketOrderLine(r)
                                                }}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- 4 送积分 -->
                            <div v-else-if="equityTypeNum(eq) === 4" class="benefit-fig-layout">
                                <div class="benefit-fig-copy benefit-fig-copy--t3">
                                    <p class="benefit-fig-title-gold">{{ $t("赠送积分") }}</p>
                                    <p class="benefit-fig-hero-num">{{ pointsHeroValue(eq) }}</p>
                                    <div class="benefit-fig-divider" />
                                    <p class="benefit-fig-desc">{{ benefitDescription(eq) }}</p>
                                    <button type="button" class="plus-benefit-btn" @click="openActivePackage">
                                        {{ benefitCta(eq) }}
                                    </button>
                                </div>
                                <div class="benefit-fig-visual benefit-fig-visual--t3">
                                    <div class="viz-t3-cards">
                                        <div class="viz-t3-card viz-t3-card--pink"
                                            :style="{ backgroundImage: `url(${IMAGE_URL('plusImg/presentExpLeftX2.png')})` }">
                                            <div class="viz-t3-card__ico viz-t3-card__ico--coin" />
                                            <div class="viz-t3-card__inner">
                                                <p class="viz-t3-card__h">{{ pointsHeroValue(eq) }} {{ $t("积分") }}</p>
                                                <p class="viz-t3-card__s">{{ $t("免费积分 兑换积分好礼") }}</p>
                                            </div>
                                        </div>
                                        <div class="viz-t3-card viz-t3-card--grey"
                                            :style="{ backgroundImage: `url(${IMAGE_URL('plusImg/presentExpRightX2.png')})` }">
                                            <div class="viz-t3-card__ico viz-t3-card__ico--gem" />
                                            <div class="viz-t3-card__inner">
                                                <p class="viz-t3-card__h">{{ $t("专属PLUS价") }}</p>
                                                <p class="viz-t3-card__s">{{ $t("专属PLUS商品 享受特惠价") }}</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- 5 积分倍率 -->
                            <div v-else-if="equityTypeNum(eq) === 5" class="benefit-fig-layout">
                                <div class="benefit-fig-copy">
                                    <p class="benefit-fig-lead-gold">{{ multiplierLead(eq) }}</p>
                                    <h4 class="benefit-fig-title">{{ $t("购物送积分") }}</h4>
                                    <div class="benefit-fig-divider" />
                                    <p class="benefit-fig-desc">{{ benefitDescription(eq) }}</p>
                                    <button type="button" class="plus-benefit-btn" @click="openActivePackage">
                                        {{ benefitCta(eq) }}
                                    </button>
                                </div>
                                <div class="benefit-fig-visual benefit-fig-visual--t5">
                                    <div class="viz-t5-vs" :style="plusVizT5VsBg">
                                        <div class="viz-t5-panel viz-t5-panel--left">
                                            <p class="viz-t5-panel__val">
                                                <em>{{ multiplierCompare(eq).low }} {{ $t("倍") }}</em>
                                            </p>
                                        </div>
                                        <div class="viz-t5-panel viz-t5-panel--right">
                                            <p class="viz-t5-panel__val">
                                                <em>{{ multiplierCompare(eq).high }} {{ $t("倍") }}</em>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </article>
                    </div>
                </div>
            </div>
        </section>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { selectPlusMember } from '~/services/userms.service'
import type { PlusMemberPackageDatum, PlusEquity, PlusRedList } from '~/types/plus'
import PlusPackageCard from '~/components/member/PlusPackageCard.vue'
import PlusHeroShell from '~/components/member/PlusHeroShell.vue'
import PlusPrivilegesBar from '~/components/member/PlusPrivilegesBar.vue'

definePageMeta({ layout: 'full-screen' })

const DEFAULT_HERO_SUB = '尊敬的PLUS会员用户，开通成为商城PLUS会员，领千元大礼包'

function bgStyle(relativePath: string) {
    return { backgroundImage: `url(${IMAGE_URL(relativePath)})` }
}

/** 权益详情各板块统一底图 */
const plusBenefitFbagiveBg = bgStyle('plusImg/fbagive.png')
/** type1 右侧券区域底图 */
const plusVizT1ZhekouBg = bgStyle('plusImg/zhekouX2.png')
/** type5 购物送积分 VS 区整图底图 */
const plusVizT5VsBg = bgStyle('plusImg/dangqianjifen.png')

const plusPackages = ref<PlusMemberPackageDatum[]>([])

const heroSubText = computed(() => {
    const text = activePackage.value?.propaganda
    return typeof text === 'string' && text.trim() ? text.trim() : DEFAULT_HERO_SUB
})
/** 当前用于下方权益展示的套餐 key：thaliId>0 用 id，否则用 thaliType */
const activePackageKey = ref<string>('t-1')

const displayPackages = computed(() => {
    const list = plusPackages.value.filter((p) => (p.thaliStatus ?? 1) === 1)
    return [...list].sort((a, b) => (a.thaliType ?? 0) - (b.thaliType ?? 0))
})

const activePackage = computed<PlusMemberPackageDatum | null>(() => {
    const list = displayPackages.value
    const key = activePackageKey.value
    const byId = list.find((p) => `id-${p.thaliId}` === key)
    if (byId) return byId
    const byType = list.find((p) => `t-${p.thaliType}` === key)
    return byType ?? list[0] ?? null
})

watch(
    displayPackages,
    (list) => {
        if (!list.length) return
        const has = list.some(
            (p) => `id-${p.thaliId}` === activePackageKey.value || `t-${p.thaliType}` === activePackageKey.value
        )
        if (!has) {
            const month = list.find((p) => p.thaliType === 1)
            const pick = month ?? list[0]
            if (!pick) return
            activePackageKey.value =
                pick.thaliId != null && pick.thaliId > 0 ? `id-${pick.thaliId}` : `t-${pick.thaliType ?? 0}`
        }
    },
    { immediate: true }
)

const activePlanShortName = computed(() => {
    const t = activePackage.value?.thaliType
    if (t === 1) return '月卡'
    if (t === 2) return '季卡'
    if (t === 3) return '年卡'
    return '会员'
})

const equityList = computed(() => {
    const pkg = activePackage.value
    const raw = pkg?.plusEquitys
    if (!Array.isArray(raw)) return []
    return raw.filter((e) => (e.equityStatus ?? 1) === 1)
})

const privilegeCount = computed(() => {
    const n = activePackage.value?.equityTotal
    if (typeof n === 'number' && n > 0) return n
    return equityList.value.length || 0
})

const privilegesTitle = computed(() => `PLUS ${activePlanShortName.value} 尊享（${privilegeCount.value} 大会员特权）`)

/** 概览区最多展示 5 项（与设计图一致） */
const overviewEquities = computed(() => equityList.value.slice(0, 5))

const sortedDetailEquities = computed(() => {
    const list = [...equityList.value]
    const order = (e: PlusEquity) => {
        const n = equityTypeNum(e)
        if (n >= 1 && n <= 5) return n
        return 999
    }
    return list.sort((a, b) => order(a) - order(b))
})

/** 仅渲染已有专属模板的权益类型，避免去掉兜底后出现空白板块 */
const plusBenefitDetailEquities = computed(() =>
    sortedDetailEquities.value.filter((e) => {
        const n = equityTypeNum(e)
        return n === 1 || n === 3 || n === 4 || n === 5
    })
)

/** 接口 equityType 数值（非法则 NaN） */
function equityTypeNum(eq: PlusEquity): number {
    const n = Number(eq.equityType)
    return Number.isFinite(n) ? n : Number.NaN
}

function equityBlockClass(eq: PlusEquity): string {
    const n = equityTypeNum(eq)
    if (n === 1) return 'plus-benefit--eq-1'
    if (n === 3) return 'plus-benefit--eq-3'
    if (n === 4) return 'plus-benefit--eq-4'
    if (n === 5) return 'plus-benefit--eq-5'
    return 'plus-benefit--eq-other'
}

function benefitKind(e: PlusEquity): 'discount' | 'redpacket' | 'points' | 'type5' | 'generic' {
    const n = equityTypeNum(e)
    if (n === 1) return 'discount'
    if (n === 3) return 'redpacket'
    if (n === 4) return 'points'
    if (n === 5) return 'type5'
    return 'generic'
}

function benefitDescription(eq: PlusEquity): string {
    const k = benefitKind(eq)
    if (k === 'discount') return '优惠叠加 享你所想'
    if (k === 'redpacket') return '全品类优惠券任意组合，每月1张，当月可用'
    if (k === 'points') return '开通即可享受'
    if (k === 'type5') return '买的越多赠送越多'
    return '具体权益以商城规则为准。'
}

function benefitCta(eq: PlusEquity): string {
    const k = benefitKind(eq)
    if (k === 'discount') return '开通领取'
    if (k === 'redpacket') return '兑专享好券'
    if (k === 'points') return '开通立享'
    if (k === 'type5') return '开通赚积分'
    return '立即开通'
}

/** 送积分主数字展示 */
function pointsHeroValue(eq: PlusEquity): string {
    const v = eq.equityValue
    if (v == null || v === '') return '500'
    const m = String(v).match(/(\d+(?:\.\d+)?)/)
    const num = m?.[1]
    return (num && String(num)) || String(v).replace(/\D/g, '') || '500'
}

/** equityValue 按 `:` 拆成左右段（trim），用于 type5 倍率文案 */
function equityColonSegments(eq: PlusEquity): [string, string] {
    const raw = eq.equityValue
    if (raw == null || !String(raw).trim()) return ['', '']
    const parts = String(raw).trim().split(':')
    const a = parts[0] != null ? String(parts[0]).trim() : ''
    const b = parts[1] != null ? String(parts[1]).trim() : ''
    return [a, b]
}

function pickMultiplierSegment(seg: string, fallback: string): string {
    if (!seg) return fallback
    const m = seg.match(/(\d+(?:\.\d+)?)/)
    if (m?.[1]) return m[1]
    const n = seg.replace(/[^\d.]/g, '')
    return n || fallback
}

/** 积分倍率顶部文案，如「100倍」；取分割后 [1] */
function multiplierLead(eq: PlusEquity): string {
    const s = equityColonSegments(eq)[1]
    if (!s) return '100倍'
    if (s.includes('倍')) return s
    const n = s.replace(/[^\d.]/g, '')
    if (n) return `${n}倍`
    return '100倍'
}

/** VS 区：[0] 左、[1] 右（模板外再拼「倍」） */
function multiplierCompare(eq: PlusEquity): { low: string; high: string } {
    const [seg0, seg1] = equityColonSegments(eq)
    if (!seg0 && !seg1) return { low: '10', high: '100' }
    return {
        low: pickMultiplierSegment(seg0, '10'),
        high: pickMultiplierSegment(seg1, '100'),
    }
}

function discountLimitYuan(eq: PlusEquity): number {
    const d = eq.discountLimit
    if (d != null && Number.isFinite(Number(d))) return Number(d)
    return 100
}

/** type3 红包区：无 redList 时与旧逻辑一致的占位 */
function equityRedListForT2(eq: PlusEquity): PlusRedList[] {
    const list = eq.redList
    if (Array.isArray(list) && list.length > 0) return list
    return [{ redpacketTPrice: 100, redpacketTOrderlimit: 101 }]
}

function formatRedPacketTPrice(r: PlusRedList): string {
    const p = r.redpacketTPrice
    if (p != null && Number.isFinite(Number(p))) return `¥ ${Number(p).toFixed(0)}`
    return '¥ --'
}

function formatRedPacketOrderLine(r: PlusRedList): string {
    const lim = r.redpacketTOrderlimit
    if (lim != null && Number.isFinite(Number(lim))) return `满${Number(lim)}可用`
    return '满—可用'
}

function formatDiscountShow(eq: PlusEquity): string {
    const v = eq.equityValue
    if (v == null || v === '') return '9.50'
    const s = String(v).replace(/折/g, '').trim()
    return s || '9.50'
}

function packageKey(pkg: PlusMemberPackageDatum): string {
    if (pkg.thaliId != null && pkg.thaliId > 0) return `id-${pkg.thaliId}`
    return `t-${pkg.thaliType ?? 0}`
}

function selectPackage(pkg: PlusMemberPackageDatum) {
    activePackageKey.value = packageKey(pkg)
}

const handleOpen = (pkg: PlusMemberPackageDatum) => {
    const thaliId = pkg.thaliId != null ? Number(pkg.thaliId) : undefined
    const thaliType = pkg.thaliType != null ? Number(pkg.thaliType) : undefined
    navigateTo({
        path: '/member/plusRecharge',
        query: {
            ...(Number.isFinite(thaliId) && (thaliId as number) > 0 ? { thaliId: String(thaliId) } : {}),
            ...(Number.isFinite(thaliType) && (thaliType as number) > 0 ? { thaliType: String(thaliType) } : {}),
        },
    })
}

function openActivePackage() {
    const p = activePackage.value
    if (p) handleOpen(p)
}

onMounted(async () => {
    try {
        const res = await selectPlusMember()
        if (res.code === 1 && Array.isArray(res.data) && res.data.length > 0) {
            plusPackages.value = res.data
        }
    } catch (e) {
        console.error('selectPlusMember failed:', e)
    }
})
</script>

<style lang="scss" scoped>
.plus-landing {
    --plus-card-overlap: clamp(104px, 16vw, 156px);
    --plus-gold: #e8c078;
    --plus-gold-deep: #c9a063;
    --plus-ink: #1b2130;
    --plus-surface: #eef0f4;
    --plus-radius-lg: 16px;
    --plus-radius-md: 12px;
    --plus-shadow-card: 0 4px 24px rgba(15, 18, 30, 0.12);
    --plus-shadow-card-hover: 0 12px 40px rgba(15, 18, 30, 0.18);
    width: 100%;
    min-height: 100%;
    background-size: cover;
    background-position: center top;
    background-repeat: no-repeat;
}

:deep(.plus-hero-shell) {
    position: relative;
    z-index: 0;
    min-height: 567px;
    width: 100%;
    overflow: visible;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    box-sizing: border-box;
}


.plus-hero-body {
    position: relative;
    z-index: 1;
    max-width: 1200px;
    margin: 0 auto;
    padding: 80px clamp(16px, 4vw, 28px) calc(40px + var(--plus-card-overlap) * 0.32);
    display: flex;
    flex-direction: column;
    align-items: center;
    box-sizing: border-box;
}

.plus-hero-title {
    margin: 0 0 18px;
    font-size: 50px;
    font-weight: 800;
    line-height: 1.25;
    text-align: center;
    letter-spacing: 0.1em;
    display: inline-flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 0.28em;
    filter: drop-shadow(0 3px 14px rgba(0, 0, 0, 0.55));
}

.plus-hero-title-lead {
    color: #ffe0ba;
}

.plus-hero-title-tail {
    color: #ffffff;
}

.plus-hero-sub {
    margin: 0;
    font-size: 24px;
    line-height: 1.8;
    color: #e59739;
    text-align: center;
    text-shadow: 0 2px 12px rgba(0, 0, 0, 0.45);
}

.plus-cards-shell {
    position: absolute;
    left: 50%;
    bottom: 0;
    z-index: 4;
    width: 100%;
    max-width: 1200px;
    padding: 0;
    box-sizing: border-box;
    transform: translate(-50%, 30%);
    pointer-events: none;
}

.plus-cards-shell .plus-cards {
    pointer-events: auto;
}

.plus-cards {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: clamp(16px, 3vw, 20px);
    width: 100%;
}

/* ========== 下方权益区 ========== */
.plus-body {
    width: 100%;
    padding: 0 0 56px;
    box-sizing: border-box;
    /* 为错叠在 hero 外的半幅卡片留出空间，避免权益区与卡片重叠 */
    padding-top: calc(var(--plus-card-overlap) * 0.6);
}

.plus-body-inner {
    max-width: 1200px;
    margin: 0 auto;
    padding: 12px 0 0;
    box-sizing: border-box;
}

/* 图5：深蓝底 + 金边圆标 + 内嵌 SVG */
.plus-privileges-heading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 18px;
    margin-top: 90px;
}

.plus-privileges-heading__side {
    width: 99px;
    height: 22px;
    object-fit: contain;
    display: block;
    pointer-events: none;
}

.plus-privileges-heading__text {
    font-size: 36px;
    font-weight: 800;
    color: #000;
}

.plus-benefit-list {
    display: flex;
    flex-direction: column;
    gap: 0;
}

.plus-benefit-block {
    display: flex;
    flex-direction: column;
    gap: 0;
}

.plus-benefit-next {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    margin-bottom: 20px;
    pointer-events: none;
}

.plus-benefit-next__img {
    width: clamp(44px, 6vw, 72px);
    height: auto;
    object-fit: contain;
    display: block;
}

.plus-benefit {
    padding: 0;
    border-radius: var(--plus-radius-lg);
    background: transparent;
    background-size: 100% 100%;
    background-position: center;
    background-repeat: no-repeat;
    border: 1px solid rgba(255, 255, 255, 0.06);
    box-shadow: none;
    box-sizing: border-box;
    height: 354px;
}

.benefit-fig-layout {
    display: grid;
    grid-template-columns: minmax(220px, 1fr) minmax(260px, 1.05fr);
    gap: clamp(18px, 4vw, 36px);
    align-items: center;
    height: 100%;
    padding: clamp(22px, 3vw, 32px) clamp(18px, 3vw, 36px);
    box-sizing: border-box;
}

.benefit-fig-copy {
    min-width: 0;
}

.benefit-fig-kicker {
    margin: 0 0 8px;
    font-size: 13px;
    font-weight: 600;
    color: #e8c078;
    letter-spacing: 0.14em;
}

.benefit-fig-title {
    margin: 0 0 12px;
    font-size: clamp(20px, 2.4vw, 26px);
    font-weight: 800;
    color: #fff;
    line-height: 1.25;
}

.benefit-fig-lead-gold {
    margin: 0 0 10px;
    font-size: 18px;
    font-weight: 800;
    color: #f0c674;
}

.benefit-fig-title-gold {
    margin: 0 0 4px;
    font-size: 18px;
    font-weight: 800;
    color: #f0c674;
}

.benefit-fig-hero-num {
    margin: 0 0 12px;
    font-size: clamp(44px, 6vw, 56px);
    font-weight: 800;
    color: #fff;
    line-height: 1;
    letter-spacing: 0.02em;
}

.benefit-fig-copy--t3 .benefit-fig-divider {
    margin-top: 4px;
}

.benefit-fig-divider {
    width: 240px;
    max-width: 100%;
    height: 1px;
    background: rgba(255, 255, 255, 0.32);
    border-radius: 999px;
    margin-bottom: 18px;
}

.benefit-fig-desc {
    margin: 0 0 20px;
    font-size: 14px;
    line-height: 1.65;
    color: rgba(255, 255, 255, 0.78);
}

/* type1 左侧更贴近图示层级 */
.plus-benefit--eq-1 .benefit-fig-kicker {
    font-size: 32px;
    font-weight: 700;
    color: #f4ca3b;
}

.plus-benefit--eq-1 .benefit-fig-layout {
    grid-template-columns: minmax(320px, 1fr) auto;
    gap: clamp(24px, 4vw, 56px);
    align-items: center;
    padding-top: 48px;
    padding-bottom: 40px;
}

.plus-benefit--eq-1 .benefit-fig-copy {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

.plus-benefit--eq-1 .benefit-fig-title {
    font-size: 32px;
    margin-bottom: 14px;
    color: #fff;
}

.plus-benefit--eq-1 .benefit-fig-divider {
    width: 320px;
    margin-bottom: 16px;
    background: #fff;
}

.plus-benefit--eq-1 .benefit-fig-desc {
    color: #fff;
}

.plus-benefit--eq-1 .plus-benefit-btn {
    width: 228px;
    height: 50px;
    padding: 0;
    margin-top: 20px;
    border: none;
    border-radius: 25px;
    font-size: 20px;
    font-weight: 500;
    letter-spacing: normal;
    color: #4a2a14;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    background: linear-gradient(147deg, #fee4c8 0%, #fcc996 100%);
    box-shadow: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
}

.benefit-fig-visual {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 160px;
}

.benefit-fig-visual--t1 {
    justify-content: flex-end;
}

.benefit-fig-visual--t3 {
    justify-content: stretch;
    width: 100%;
    min-height: 0;
}

.plus-benefit--eq-1 .viz-t1-coupon {
    margin-left: auto;
}

/* type3 红包券：左约 40% / 右约 60%，右侧 quanX2 背景放大铺满 */
.plus-benefit--eq-3 .benefit-fig-layout {
    grid-template-columns: minmax(240px, 2fr) minmax(280px, 3fr);
    gap: clamp(16px, 3vw, 32px);
    align-items: center;
}

.plus-benefit--eq-3 .benefit-fig-visual--t2 {
    justify-content: center;
}

.plus-benefit--eq-3 .viz-t2-scene {
    width: 659px;
    max-width: 100%;
    height: 257px;
    box-sizing: border-box;
    background-size: 100% 100%;
    background-position: center;
    background-repeat: no-repeat;
}

/* type4 送积分：左约 28% / 右约 72%，与设计图一致 */
.plus-benefit--eq-4 .benefit-fig-layout {
    grid-template-columns: minmax(200px, 0.28fr) minmax(260px, 0.72fr);
    gap: clamp(24px, 4vw, 48px);
    align-items: center;
    padding: clamp(20px, 2.5vw, 32px) clamp(16px, 3vw, 36px);
}

.plus-benefit--eq-4 .benefit-fig-copy--t3 {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    max-width: 100%;
}

.plus-benefit--eq-4 .benefit-fig-title-gold {
    margin: 0 0 10px;
    font-size: 32px;
    font-weight: 800;
    color: #f4ca3b;
    letter-spacing: 0.06em;
}

.plus-benefit--eq-4 .benefit-fig-hero-num {
    margin: 0 0 14px;
    font-size: 32px;
}

.plus-benefit--eq-4 .benefit-fig-divider {
    width: 100%;
    max-width: 260px;
    margin-bottom: 14px;
}

.plus-benefit--eq-4 .benefit-fig-desc {
    margin-bottom: 18px;
    color: rgba(255, 255, 255, 0.78);
    font-size: 20px;
    line-height: 1.65;
}

.plus-benefit--eq-4 .plus-benefit-btn {
    width: 228px;
    height: 50px;
    padding: 0;
    margin-top: 4px;
    border: none;
    border-radius: 25px;
    font-size: 20px;
    font-weight: 500;
    letter-spacing: normal;
    color: #4a2a14;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    background: linear-gradient(147deg, #fee4c8 0%, #fcc996 100%);
    box-shadow: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
}

.plus-benefit--eq-4 .viz-t3-cards {
    justify-content: center;
    gap: clamp(14px, 2vw, 22px);
    max-width: 100%;
    margin: 0;
}

.plus-benefit--eq-4 .viz-t3-card {
    padding: 14px 16px 14px 14px;
    align-items: center;
}

.plus-benefit--eq-4 .viz-t3-card__inner {
    margin-left: auto;
    align-self: center;
    max-width: 58%;
    padding: 12px 14px;
    background: none;
    box-shadow: none;
    border-radius: 0;
}

.plus-benefit--eq-4 .viz-t3-card--pink .viz-t3-card__h {
    margin: 0 0 6px;
    font-size: 26px;
    font-weight: 600;
    line-height: 37px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    background: linear-gradient(180deg, #ff8381 0%, #ff4a47 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    color: transparent;
}

.plus-benefit--eq-4 .viz-t3-card--pink .viz-t3-card__s {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: #666666;
    line-height: 40px;
}

.plus-benefit--eq-4 .viz-t3-card--grey .viz-t3-card__h {
    margin: 0 0 6px;
    font-size: 26px;
    font-weight: 600;
    line-height: 37px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    background: linear-gradient(180deg, #ae6b3d 0%, #4a2a14 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    color: transparent;
}

.plus-benefit--eq-4 .viz-t3-card--grey .viz-t3-card__s {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: #666666;
    line-height: 40px;
}

/* type5 积分倍率：左文案 + 右整图底图叠字（plusImg/dangqianjifen.png） */
.plus-benefit--eq-5 .benefit-fig-layout {
    grid-template-columns: minmax(200px, 0.32fr) minmax(260px, 0.68fr);
    gap: clamp(20px, 4vw, 44px);
    align-items: center;
    padding: clamp(20px, 2.5vw, 32px) clamp(16px, 3vw, 36px);
}

.plus-benefit--eq-5 .benefit-fig-copy {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

.plus-benefit--eq-5 .benefit-fig-lead-gold {
    margin: 0 0 12px;
    font-size: clamp(34px, 4.2vw, 48px);
    font-weight: 800;
    color: #f4ca3b;
    line-height: 1.1;
}

.plus-benefit--eq-5 .benefit-fig-title {
    margin: 0 0 14px;
    font-size: clamp(22px, 2.6vw, 28px);
    font-weight: 800;
    color: #fff;
}

.plus-benefit--eq-5 .benefit-fig-divider {
    width: 100%;
    max-width: 280px;
    margin-bottom: 14px;
    background: rgba(255, 255, 255, 0.45);
}

.plus-benefit--eq-5 .benefit-fig-desc {
    margin-bottom: 20px;
    font-size: 15px;
    line-height: 1.65;
    color: rgba(255, 255, 255, 0.88);
}

.plus-benefit--eq-5 .plus-benefit-btn {
    width: 228px;
    height: 50px;
    padding: 0;
    margin-top: 4px;
    border: none;
    border-radius: 25px;
    font-size: 20px;
    font-weight: 500;
    letter-spacing: normal;
    color: #4a2a14;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    background: linear-gradient(147deg, #fee4c8 0%, #fcc996 100%);
    box-shadow: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
}

.plus-benefit--eq-5 .benefit-fig-visual--t5 {
    width: 100%;
    justify-content: center;
}

.plus-benefit--eq-5 .viz-t5-vs {
    position: relative;
    width: 811px;
    max-width: 100%;
    aspect-ratio: 811 / 282;
    margin: 5px auto 0;
    padding: clamp(10px, 2vw, 24px) clamp(10px, 2.5vw, 28px);
    box-sizing: border-box;
    background-repeat: no-repeat;
    background-position: center center;
    background-size: 100% 100%;
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    justify-content: space-between;
    gap: clamp(8px, 2vw, 20px);
}

.plus-benefit--eq-5 .viz-t5-panel {
    position: relative;
    z-index: 1;
    flex: 1 1 0;
    min-width: 0;
    min-height: 0;
    padding: 8px 6px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    text-align: center;
    transform: translate(20px, 20px);
}

.plus-benefit--eq-5 .viz-t5-panel__val {
    margin: 10px 0 0;
    font-size: 15px;
    font-weight: 700;
    line-height: 1.45;
}

.plus-benefit--eq-5 .viz-t5-panel__val em {
    font-style: normal;
    font-weight: 900;
    font-size: 34px;
}

.plus-benefit--eq-5 .viz-t5-panel--left .viz-t5-panel__val {
    margin-left: 20px;
    color: #4e342e;
}

.plus-benefit--eq-5 .viz-t5-panel--right .viz-t5-panel__val {
    margin-left: 80px;
    color: #082a52;
}

@media (max-width: 900px) {
    .plus-benefit {
        height: auto;
    }

    .benefit-fig-layout {
        grid-template-columns: 1fr;
    }

    .benefit-fig-visual {
        order: -1;
        min-height: 140px;
    }

    .plus-benefit--eq-5 .viz-t5-vs {
        flex-wrap: wrap;
        justify-content: center;
    }

    .plus-benefit--eq-5 .viz-t5-panel {
        flex: 1 1 42%;
        max-width: 48%;
    }
}

.plus-benefit-btn {
    padding: 11px 30px;
    border: none;
    border-radius: 999px;
    font-size: 14px;
    font-weight: 700;
    letter-spacing: 0.04em;
    color: #3d2914;
    cursor: pointer;
    background: linear-gradient(180deg, #fff0dc 0%, var(--plus-gold) 100%);
    box-shadow:
        0 4px 16px rgba(0, 0, 0, 0.2),
        inset 0 1px 0 rgba(255, 255, 255, 0.35);
    transition: filter 0.2s ease, transform 0.15s ease;

    &:hover {
        filter: brightness(1.06);
    }

    &:active {
        transform: scale(0.98);
    }
}

/* —— 图1 折上折券（底图 zhekouX2） —— */
.viz-t1-coupon {
    position: relative;
    display: flex;
    align-items: stretch;
    width: 662px;
    height: 202px;
    background-size: 662px 202px;
    background-position: center;
    background-repeat: no-repeat;
    filter: drop-shadow(0 10px 28px rgba(0, 0, 0, 0.35));
}

.viz-t1-coupon__left {
    flex: 0 0 32%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 14px 10px;
    background: transparent;
    border: none;
    box-sizing: border-box;
    position: relative;
    z-index: 1;
}

.viz-t1-coupon__discount {
    color: #bb6f3a;
    font-weight: 900;
    line-height: 1;
    margin-bottom: 6px;
    text-shadow: 0 0 8px rgba(255, 255, 255, 0.9), 0 1px 2px rgba(255, 255, 255, 0.8);
}

.viz-t1-coupon__num {
    font-size: 68px;
}

.viz-t1-coupon__unit {
    font-size: 18px;
    margin-left: 2px;
}

.viz-t1-coupon__sub {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #bb6f3a;
    text-shadow: 0 0 6px rgba(255, 255, 255, 0.85);
}

.viz-t1-coupon__tear {
    flex: 0 0 10px;
    position: relative;
    z-index: 1;
    background: transparent;
    border: none;
    pointer-events: none;
}

.viz-t1-coupon__right {
    position: relative;
    z-index: 1;
    flex: 1;
    padding: 36px 14px 12px;
    background: transparent;
    border: none;
    box-sizing: border-box;
}

.viz-t1-coupon__rtitle {
    margin: 0 0 6px;
    font-size: 36px;
    font-weight: 800;
    color: #bb6f3a;
    text-shadow: 0 0 8px rgba(255, 255, 255, 0.9), 0 1px 1px rgba(255, 255, 255, 0.7);
}

.viz-t1-coupon__rsub {
    margin: 0 0 8px;
    font-size: 24px;
    line-height: 1.45;
    color: #bb6f3a;
    text-shadow: 0 0 6px rgba(255, 255, 255, 0.85);
}

.viz-t1-coupon__foot {
    margin: 0;
    font-size: 10px;
    color: #bb6f3a;
    position: absolute;
    right: 18px;
    bottom: 12px;
    text-align: right;
    text-shadow: 0 0 6px rgba(255, 255, 255, 0.8);
}

/* —— 图2 礼盒 —— */
.viz-t2-scene {
    position: relative;
    width: 100%;
    max-width: 320px;
    height: 200px;
    background-size: contain;
    background-position: center;
    background-repeat: no-repeat;
}

.viz-t2-coupon-list {
    position: absolute;
    top: calc(100% / 3 + 10%);
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
    max-width: calc(100% - 24px);
    box-sizing: border-box;
}

.viz-t2-coupon-mini {
    --viz-t2-coupon-h: 120px;
    position: relative;
    flex: 0 0 auto;
    width: 100px;
    height: 120px;
    padding: 8px 6px;
    border-radius: 6px;
    background-color: transparent;
    background-size: 100% 100%;
    background-position: center;
    background-repeat: no-repeat;
    border: none;
    text-align: center;
    box-shadow: none;
    display: flex;
    flex-direction: column;
    gap: 4px;
    justify-content: center;
    box-sizing: border-box;
}

.viz-t2-coupon-mini__p {
    font-size: 24px;
    font-weight: 900;
    color: #e26c28;
}

.viz-t2-coupon-mini__m {
    font-size: 14px;
    font-weight: 400;
    color: #c98342;
}

.viz-t2-coupon-mini__r {
    font-size: 14px;
    color: #643e2a;
    margin-top: calc(var(--viz-t2-coupon-h) * 0.1);
}

/* —— 图3 双卡 —— */
.viz-t3-cards {
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    align-items: center;
    justify-content: center;
    gap: clamp(14px, 2vw, 22px);
    width: 100%;
    max-width: min(100%, 796px);
    margin: 0 auto;
    box-sizing: border-box;
}

@media (max-width: 820px) {
    .viz-t3-cards {
        flex-wrap: wrap;
        justify-content: center;
    }
}

.viz-t3-card {
    display: flex;
    align-items: stretch;
    width: 386px;
    max-width: 100%;
    aspect-ratio: 386 / 168;
    height: auto;
    flex: 0 0 auto;
    box-sizing: border-box;
    border-radius: 0;
    padding: 12px 10px 12px 12px;
    gap: 10px;
    box-shadow: none;
    background-color: transparent;
    background-size: 100% 100%;
    background-position: center;
    background-repeat: no-repeat;
}

.viz-t3-card--pink,
.viz-t3-card--grey {
    background-color: transparent;
}

.viz-t3-card__ico {
    display: none;
}

.viz-t3-card__ico--coin::after {
    content: '';
    position: absolute;
    inset: 8px;
    border-radius: 50%;
    border: 3px solid rgba(255, 255, 255, 0.85);
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.15);
}

.viz-t3-card__ico--gem::after {
    content: '';
    position: absolute;
    left: 50%;
    top: 50%;
    width: 18px;
    height: 18px;
    transform: translate(-50%, -50%) rotate(45deg);
    background: rgba(255, 255, 255, 0.5);
    border-radius: 3px;
}

.viz-t3-card__inner {
    flex: 1;
    background: transparent;
    border-radius: 0;
    padding: 10px 12px;
    min-width: 0;
}

.viz-t3-card__h {
    margin: 0 0 6px;
    font-size: 14px;
    font-weight: 800;
    color: #c62828;
}

.viz-t3-card--grey .viz-t3-card__h {
    color: #263238;
}

.viz-t3-card__s {
    margin: 0;
    font-size: 11px;
    line-height: 1.4;
    color: #5d4037;
}

.viz-t3-card--grey .viz-t3-card__s {
    color: #455a64;
}

@media (max-width: 640px) {
    :deep(.plus-hero-shell) {
        min-height: 500px;
    }

}
</style>
