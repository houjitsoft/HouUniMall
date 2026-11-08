<template>
    <template v-if="packages?.length">
        <article v-for="p in packages" :key="p.thaliId ?? `t-${p.thaliType}`"
            :class="[rootClass, getVariantClass(p), { 'is-active': isActive(p) }]" role="button" tabindex="0"
            @click="emit('select', p)" @keydown.enter.prevent="emit('select', p)">
            <span v-if="Number(p.thaliRecommend) === 1" class="plus-package-card__ribbon" :style="recommendRibbonStyle">
                <span class="plus-package-card__ribbon-text">{{ $t(p.recommend || '超值推荐') }}</span>
            </span>

            <div style="display: flex; justify-content: center;">
                <img :src="IMAGE_URL('plusImg/bagzuanX2.png')" alt="" width="85" height="80" />
            </div>

            <div class="plus-package-card__title-wrap">
                <component :is="titleTag" :class="titleClass">
                    {{ getTitle(p) }}
                </component>
            </div>

            <div :class="priceWrapClass">
                <span class="price-now" :style="{ color: priceNowColor }">
                    {{ formatPriceWithSymbolSafe(p.thaliPrice) }}<span class="price-unit">{{ getUnit(p) }}</span>
                </span>
                <span v-if="p.thaliOldPrice != null" class="price-old">
                    {{ formatPriceWithSymbolSafe(p.thaliOldPrice) }}
                </span>
            </div>

            <button v-if="showOpen" type="button" :class="openBtnClass" @click.stop="emit('open', p)">
                {{ openText }}
            </button>

            <span v-if="showCheck && isActive(p)" :class="checkClass" aria-hidden="true">
                <img :src="IMAGE_URL('plusImg/xuanzeX2.png')" alt="" width="66" height="66" />
            </span>
        </article>
    </template>
</template>

<script setup lang="ts">
import type { PlusMemberPackageDatum } from '~/types/plus'

type TitleTag = 'h2' | 'h3'

const props = withDefaults(
    defineProps<{
        /** 列表模式：由组件内部渲染 v-for（用于 plusRecharge） */
        packages?: PlusMemberPackageDatum[]
        /** 选中 key：优先 id-<thaliId> 否则 t-<thaliType> */
        activeKey?: string
        /** 列表模式下，variant class 的前缀（如 plus-recharge__card--） */
        variantPrefix?: string
        /** 列表模式下标题风格：plus(季卡) / recharge(季度卡) */
        titleStyle?: 'plus' | 'recharge'

        /** 可选：自定义计算逻辑（不传则走组件默认） */
        getVariantClass?: (pkg: PlusMemberPackageDatum) => string
        getTitle?: (pkg: PlusMemberPackageDatum) => string
        getUnit?: (pkg: PlusMemberPackageDatum) => string
        isActive?: (pkg: PlusMemberPackageDatum) => boolean

        titleTag?: TitleTag
        showOpen?: boolean
        openText?: string
        showCheck?: boolean
    }>(),
    {
        variantPrefix: '',
        titleStyle: 'recharge',
        titleTag: 'h2',
        showOpen: false,
        openText: '立即开通',
        showCheck: false,
    }
)

const emit = defineEmits<{
    (e: 'select', pkg: PlusMemberPackageDatum): void
    (e: 'open', pkg: PlusMemberPackageDatum): void
}>()

const recommendRibbonStyle = {
    background: 'transparent',
    backgroundImage: `url(${IMAGE_URL('plusImg/tuijian.png')})`,
    backgroundSize: '100% 100%',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat',
    width: '110px',
    height: '110px',
} as Record<string, string>

const priceNowColor = '#e85a4a'

const rootClass = 'plus-card'
const titleClass = 'plus-card-title'
const priceWrapClass = 'plus-card-price'
const openBtnClass = 'plus-card-btn'
const checkClass = 'plus-card-check'

function getVariantClass(pkg: PlusMemberPackageDatum): string {
    if (props.getVariantClass) return props.getVariantClass(pkg)
    if (!props.variantPrefix) return ''
    const t = Number(pkg.thaliType)
    if (t === 3) return `${props.variantPrefix}year`
    if (t === 2) return `${props.variantPrefix}season`
    return `${props.variantPrefix}month`
}

function getTitle(pkg: PlusMemberPackageDatum): string {
    if (props.getTitle) return props.getTitle(pkg)
    const t = Number(pkg.thaliType)
    if (t === 1) return 'PLUS会员月度卡'
    if (t === 2) return props.titleStyle === 'plus' ? 'PLUS会员季卡' : 'PLUS会员季度卡'
    if (t === 3) return 'PLUS会员年度卡'
    return 'PLUS会员'
}

function getUnit(pkg: PlusMemberPackageDatum): string {
    if (props.getUnit) return props.getUnit(pkg)
    const t = Number(pkg.thaliType)
    if (t === 1) return '/月'
    if (t === 2) return '/季'
    if (t === 3) return '/年'
    return ''
}

function packageKey(pkg: PlusMemberPackageDatum): string {
    const id = pkg.thaliId != null ? Number(pkg.thaliId) : Number.NaN
    if (Number.isFinite(id) && id > 0) return `id-${id}`
    const t = pkg.thaliType != null ? Number(pkg.thaliType) : 0
    return `t-${t}`
}

function isActive(pkg: PlusMemberPackageDatum): boolean {
    if (props.isActive) return props.isActive(pkg)
    if (props.activeKey) return packageKey(pkg) === props.activeKey
    return false
}

function formatMoney(v: number | null | undefined): string {
    if (v == null || Number.isNaN(Number(v))) return '--'
    return Number(v).toFixed(2)
}
</script>

<style scoped>
.plus-card {
    position: relative;
    width: 384px;
    max-width: 100%;
    height: auto;
    max-height: 339px;
    padding: 32px 28px 24px;
    border-radius: var(--plus-radius-lg, 16px);
    box-sizing: border-box;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    cursor: pointer;
    outline: none;
    transition:
        box-shadow 0.28s cubic-bezier(0.4, 0, 0.2, 1),
        transform 0.28s cubic-bezier(0.4, 0, 0.2, 1),
        border-color 0.2s ease;
}

.plus-card {
    box-shadow: none;
}

.plus-card:hover {
    box-shadow: none;
    transform: translateY(-2px);
}

.plus-card.is-active:hover {
    box-shadow: none;
    transform: translateY(-3px);
}

.plus-card.is-active {
    box-shadow: none;
    transform: translateY(-4px);
    border-color: #ff9b21;
    border-width: 3px;
}

.plus-card:focus-visible {
    box-shadow: 0 0 0 3px rgba(232, 192, 120, 0.55);
}

.plus-card--month {
    background: linear-gradient(165deg, #fffdfb 0%, #f7efe4 55%, #efe4d6 100%);
    border: 1px solid rgba(201, 160, 99, 0.42);
}

.plus-card--season {
    background: linear-gradient(165deg, #f4f7fc 0%, #e6ecf6 50%, #dce4f0 100%);
    border: 1px solid rgba(90, 110, 140, 0.28);
}

.plus-card--year {
    background: linear-gradient(165deg, #3d4658 0%, #2a3142 55%, #1f2533 100%);
    border: 1px solid rgba(255, 255, 255, 0.1);
}

.plus-card-title {
    margin: 0 0 16px;
    font-size: 24px;
    font-weight: 600;
    line-height: 1.3;
    text-align: center;
    letter-spacing: 0.04em;
}

.plus-card--month .plus-card-title {
    color: #ae6941;
}

.plus-card--season .plus-card-title {
    color: #333333;
}

.plus-card--year .plus-card-title {
    color: #ffd785;
}

.plus-card-price {
    display: flex;
    align-items: baseline;
    justify-content: center;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 20px;
}

.price-now {
    font-size: 26px;
    font-weight: 700;
}

.price-unit {
    font-size: 15px;
    font-weight: 600;
}

.price-old {
    font-size: 14px;
    color: #b0b0b0;
    text-decoration: line-through;
}

.plus-card--season .price-now,
.plus-card--season .price-now {
    color: #d84315;
}

.plus-card--year .price-now,
.plus-card--year .price-now {
    color: #ff8a65;
}

.plus-card-btn {
    width: 100%;
    margin-top: auto;
    padding: 13px 22px;
    border: none;
    border-radius: 999px;
    font-size: 15px;
    font-weight: 700;
    letter-spacing: 0.04em;
    color: #4a3318;
    cursor: pointer;
    background: linear-gradient(147deg, #fee4c8 0%, #fcc996 100%);
    box-shadow:
        0 4px 14px rgba(180, 130, 70, 0.28),
        inset 0 2px 6px rgba(255, 255, 255, 0.55),
        inset 0 -2px 8px rgba(120, 80, 40, 0.12);
    transition:
        filter 0.2s ease,
        transform 0.18s ease,
        box-shadow 0.2s ease;
}

.plus-card-btn:hover {
    filter: brightness(1.04);
    box-shadow:
        0 6px 18px rgba(180, 130, 70, 0.32),
        inset 0 2px 6px rgba(255, 255, 255, 0.6),
        inset 0 -2px 8px rgba(120, 80, 40, 0.1);
}

.plus-card-btn:active {
    transform: scale(0.98);
}

.plus-card--year .plus-card-btn {
    color: #3d2914;
}

.plus-card-check {
    position: absolute;
    right: 0;
    bottom: 0;
    width: 66px;
    height: 66px;
    line-height: 0;
    filter: drop-shadow(0 4px 10px rgba(0, 0, 0, 0.24));
}

.plus-card-check img {
    display: block;
    width: 66px;
    height: 66px;
}

.plus-package-card__ribbon {
    position: absolute;
    top: 0;
    right: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    pointer-events: none;
}

.plus-package-card__ribbon-text {
    transform: rotate(45deg);
    margin-bottom: 20px;
    margin-left: 25px;
    font-size: 18px;
    font-weight: 400;
    color: #4a2a14ff;
    text-align: center;
    letter-spacing: 0.06em;
    white-space: nowrap;
}

.plus-package-card__title-wrap {
    margin-top: 20px;
}
</style>
