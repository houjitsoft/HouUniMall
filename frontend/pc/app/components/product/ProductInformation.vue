<template>
    <div class="product-information">
        <!-- 左侧图片区域 -->
        <div class="product-image">
            <!-- 放大镜组件 -->
            <MagnifyingGlass :current-image="currentImage || ''" :image-alt="productInfo.goodsName || ''"
                :product-images="productImages" :current-index="currentIndex" :common-video="commonVideo || ''"
                @image-change="handleImageChange" />

            <!-- 底部：商品编号 + 操作图标 -->
            <div class="image-actions">
                <div v-if="imageToolbarCommonId" class="image-actions-common-id">
                    {{ $t('商品编号') }}：{{ imageToolbarCommonId }}
                </div>
                <div class="image-actions-items">
                    <span class="action-item" @click="handleShare">
                        <span class="action-icon">🔗</span>
                        <span class="action-text">{{ $t('分享') }}</span>
                    </span>
                    <span class="action-item" :class="{ 'is-favorited': isFavorited, 'is-loading': favoriteLoading }"
                        @click="handleFavorite">
                        <span class="action-icon">{{ isFavorited ? '❤️' : '🤍' }}</span>
                        <span class="action-text">{{ isFavorited ? $t('已收藏') : $t('收藏') }}</span>
                    </span>
                    <span v-if="showCustomerService" class="action-item" @click="handleCustomerService">
                        <span class="action-icon">👤</span>
                        <span class="action-text">{{ $t('客服') }}</span>
                    </span>
                </div>
            </div>

            <!-- 分享弹窗（公共弹窗） -->
            <PopupModal v-model="showShareModal" title="分享到" width="480px" content-class="share-modal">
                <div class="share-options">
                    <div class="share-option" @click="handleShareToWechat">
                        <span class="share-icon">💬</span>
                        <span class="share-label">{{ $t('微信') }}</span>
                    </div>
                    <div class="share-option" @click="handleShareToOther('qq')">
                        <span class="share-icon">🏠</span>
                        <span class="share-label">{{ $t('QQ空间') }}</span>
                    </div>
                    <div class="share-option" @click="handleShareToOther('facebook')">
                        <span class="share-icon">📘</span>
                        <span class="share-label">{{ $t('脸书') }}</span>
                    </div>
                    <div class="share-option" @click="handleShareToOther('ins')">
                        <span class="share-icon">📷</span>
                        <span class="share-label">ins</span>
                    </div>
                </div>
            </PopupModal>

            <!-- 微信分享二维码弹窗（公共弹窗） -->
            <ModalQrcodeModal :visible="showShareQrcodeModal" title="微信分享" :qrcode="shareLink" width="400px"
                @close="showShareQrcodeModal = false" />

        </div>

        <!-- 右侧商品信息区域 -->
        <div class="product-details">
            <!-- 商品标题 -->
            <h1 class="product-goodsName">
                {{ $tg(productInfo.goodsName) }}
                <span v-if="productInfo.virtualGoods === 0" class="virtual-goods-tag">{{ $t('（虚拟商品）') }}</span>
            </h1>

            <!-- 价格信息 -->
            <div class="price-section" :class="{ 'has-activity': activityInfo }">
                <!-- 未登录：价格隐藏 + 登录提示 -->
                <template v-if="!isLoggedIn">
                    <div class="price-content">
                        <div class="price-main">
                            <div class="price-label">
                                <span class="label">{{ $t('现价') }}:</span>
                                <span class="price">{{ formatPriceWithSymbolSafe(productInfo.commonPrice ??
                                    productInfo.goodsPrice)
                                    }}</span>
                            </div>
                            <div class="login-tip">
                                {{ $t('您可以登录,登录即可查看更多优惠价格。') }}<a class="login-link" href="javascript:void(0)"
                                    @click.prevent="handlePriceLoginClick">{{ $t('立即登录') }}</a>
                            </div>
                        </div>
                    </div>
                </template>
                <!-- 已登录：正常价格显示 -->
                <template v-else>
                    <!-- 活动信息 -->
                    <div class="activity-section" v-if="activityInfo">
                        <div class="activity-name">
                            <svg class="activity-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <circle cx="12" cy="12" r="10" />
                                <line x1="12" y1="12" x2="12" y2="8" />
                                <line x1="12" y1="12" x2="15" y2="14" />
                            </svg>
                            <span v-if="getActivityName(activityInfo.activityStatus)" class="activity-name-text">
                                {{ getActivityName(activityInfo.activityStatus) }}
                            </span>
                            <span class="activity-status">
                                {{ activityInfo.activityStartStatus === 1 ? $t('活动进行中') : $t('活动未开始') }}
                            </span>
                        </div>
                        <div class="activity-countdown">
                            <span class="countdown-time">{{ countdownText }}</span>
                        </div>
                    </div>

                    <div class="price-content">
                        <div class="price-main">
                            <!-- 活动进行中或满减赠/满减折时显示活动价格和原价，否则显示正常价格 -->
                            <template v-if="shouldShowActivityPrice">
                                <div class="price-label">
                                    <span class="label">{{ $t('活动价') }}:</span>
                                    <span class="price">{{ formatPriceWithSymbolSafe(displayActivityPrice) }}</span>
                                    <span class="original-price">{{
                                        formatPriceWithSymbolSafe(productInfo.goodsPrice)
                                    }}</span>
                                </div>
                            </template>
                            <!-- 无活动或活动未开始时显示正常价格信息 -->
                            <template v-else>
                                <!-- 现价：始终显示 -->
                                <div class="price-label">
                                    <span class="label">{{ $t('现价') }}:</span>
                                    <span class="price">{{ formatPriceWithSymbolSafe(productInfo.goodsPrice) }}</span>
                                    <span class="original-price">{{
                                        formatPriceWithSymbolSafe(productInfo.goodsMarketPrice)
                                    }}</span>
                                </div>
                                <!-- PLUS 标识：按 plusStatus + productInfo(plusGoods/plusMember/plusDiscount) 判断 -->
                                <div v-if="plusBadge.mode === 'open'" class="plus-badge">
                                    <span class="plus-badge__icon" aria-hidden="true" />
                                    <span class="plus-badge__prefix">{{ $t('PLUS购物折上折') }}</span>
                                    <span class="plus-badge__text">{{ $t('现在开通立享特惠') }}</span>
                                </div>
                                <div v-else-if="plusBadge.mode === 'member_discount'" class="plus-badge">
                                    <span class="plus-badge__icon" aria-hidden="true" />
                                    <span class="plus-badge__prefix">{{ $t('PLUS再享') }}</span>
                                    <span class="plus-badge__text">{{ plusBadge.discountText }}{{ $t('折') }}</span>
                                </div>
                                <div v-else-if="plusBadge.mode === 'member_plain'" class="plus-badge">
                                    <span class="plus-badge__icon" aria-hidden="true" />
                                    <span class="plus-badge__prefix">{{ $t('PLUS再享') }}</span>
                                    <span class="plus-badge__text">{{ $t('会员权益') }}</span>
                                </div>
                            </template>
                            <!-- 满减赠、满减折活动促销文案 -->
                            <div v-if="productInfo.promotionContent" class="promotion-content">
                                <span class="promotion-label">{{ $t('促销') }}:</span>
                                <span class="promotion-text">{{ productInfo.promotionContent }}</span>
                            </div>
                            <!-- 阶梯价（插件 goods-tierprice）：命中规则才显示，只做展示，不参与算价 -->
                            <div v-if="tierTable && tierTable.tiers && tierTable.tiers.length" class="tier-price">
                                <div class="tier-price__head">
                                    <span class="tier-price__title">{{ $t('数量阶梯价') }}</span>
                                    <span class="tier-price__tip">{{ $t('批量采购参考价') }}</span>
                                </div>
                                <div class="tier-price__row" v-for="(t, ti) in tierTable.tiers" :key="ti">
                                    <span class="tier-price__range">{{ tierRangeText(t) }}</span>
                                    <span class="tier-price__value">{{ tierPriceText(t) }}</span>
                                </div>
                            </div>
                            <!-- 拼团信息提醒 -->
                            <div v-if="isGroupBuyGoods" class="group-buy-tip">
                                <span>{{ $t('该商品在h5端、手机端可享受拼团价格') }}</span>
                                <span class="group-buy-tip__link" @click="handleGoToH5GroupBuy">{{ $t('点击前往h5端')
                                    }}</span>
                            </div>
                            <!-- 虚拟商品有效期 -->
                            <div v-if="productInfo.virtualGoods === 0 && productInfo.periodOfValidity"
                                class="virtual-goods-tip">
                                温馨提示：商品有效兑换期至{{ productInfo.periodOfValidity }}
                            </div>
                        </div>
                        <!-- 品牌信息 -->
                        <div class="brand-section" v-if="productInfo.brandPic">
                            <div class="brand-info">
                                <img v-if="productInfo.brandPic" :src="productInfo.brandPic || ''"
                                    :alt="productInfo.brandName || ''" class="brand-logo" />
                            </div>
                        </div>
                    </div>
                </template>
            </div>
            <!-- 优惠券 -->
            <CouponSection :list="voucherList" :loading="voucherLoading" @receive="handleCouponReceive" />

            <!-- 库存信息 -->
            <div class="inventory-section">
                <template v-if="productInfo.goodsSalenum == null || productInfo.goodsSalenum === 0">
                    <span class="new-product-tag">{{ $t('新品上市') }}</span>
                </template>
                <template v-else>
                    <span class="label">{{ $t('销量') }}:</span>
                    <span class="value">{{ productInfo.goodsSalenum }}</span>
                </template>
                <span class="separator">|</span>
                <span class="label">{{ (activityInfo && activityInfo.activityStatus === 2) ? $t('秒杀库存') : $t('剩余库存')
                }}:</span>
                <span class="value">{{ displayStock }}</span>
            </div>

            <!-- 配送信息：ClientOnly 避免省市区/有货文案与 SSR 不一致导致 hydration mismatch -->
            <div class="delivery-section">
                <div class="delivery-item">
                    <span class="label">{{ $t('配送至') }}:</span>
                    <ClientOnly>
                        <div class="location-select">
                            <div class="phone-code-selector" @click="showAreaCodeModal = true">
                                {{ $t(selectedCountryName || '') }}
                                <span class="arrow-down">▼</span>
                            </div>
                            <AddressLinkage v-if="deliveryAreaReady && selectedPhoneCode === '+86'" v-model="selectedArea"
                                :show-area="3" return-type="id" />
                        </div>
                        <template #fallback>
                            <div class="location-select">
                                <div class="phone-code-selector">
                                    中国
                                    <span class="arrow-down">▼</span>
                                </div>
                            </div>
                        </template>
                    </ClientOnly>
                    <!-- 国家/地区代码选择弹窗 -->
                    <AreaCode v-if="showAreaCodeModal" @select="handleAreaCodeSelect"
                        @close="showAreaCodeModal = false" />
                    <ClientOnly>
                        <span v-if="deliveryAreaReady" class="delivery-time">
                            {{ $t(deliveryTimeText || '') }}
                        </span>
                    </ClientOnly>
                </div>
            </div>
            <!-- SKU规格选择 -->
            <div v-if="specParamList.length > 0" class="sku-section">
                <div v-for="spec in specParamList" :key="spec.paramName" class="sku-row">
                    <span class="label">{{ $t(spec.paramName) }}:</span>
                    <div class="sku-options" role="group" :aria-label="`${spec.paramName}选项`">
                        <button v-for="value in spec.paramNameValues" :key="value" type="button" class="sku-option"
                            :class="{ active: getSelectedSpecValue(spec.paramName) === value }" :disabled="specLoading"
                            @click="selectSpecValue(spec.paramName, value)">
                            {{ $t(value) }}
                        </button>
                    </div>
                </div>
            </div>

            <!-- 数量选择 -->
            <div class="minNumber-section">
                <span class="label">{{ $t('数量') }}:</span>
                <div class="minNumber-controls">
                    <button class="btn-minus" :disabled="quantity <= productInfo.minNumber" type="button"
                        @click="decreaseQuantity">
                        -
                    </button>
                    <input type="number" v-model.number="quantity" class="minNumber-input" :min="productInfo.minNumber"
                        :max="effectiveMaxQuantity" @blur="validateQuantity" />
                    <button class="btn-plus" :disabled="quantity >= effectiveMaxQuantity" type="button"
                        @click="increaseQuantity">
                        +
                    </button>
                </div>
                <div class="stock-info-wrap">
                    <span class="stock-info">
                        {{ $t('起订量') }}: {{ productInfo.minNumber }}
                    </span>
                    <span v-if="seckillLimitDisplay" class="stock-info limit-info">
                        {{ seckillLimitDisplay }}
                    </span>
                </div>
            </div>
            <!-- 质保金提示语 -->
            <div v-if="showDepositGuaranteeTip" class="deposit-guarantee-tip" role="status">
                <span class="deposit-guarantee-tip__icon" aria-hidden="true">✓</span>
                <span class="deposit-guarantee-tip__text">{{ $t('本店已交纳质保金，可放心购买！') }}</span>
            </div>

            <!-- 商品服务 -->
            <div class="service-section" v-if="productServices && productServices.length > 0">
                <span class="label">{{ $t('商品服务') }}:</span>
                <div class="service-list">
                    <div class="service-item" v-for="(service, index) in productServices" :key="index">
                        <span class="service-icon">{{ service.icon }}</span>
                        <span class="service-name">{{ $t(service.name) }}</span>
                    </div>
                </div>
            </div>

            <!-- 到货通知（插件 goods-restock）：缺货时给用户一条出路，不留死按钮 -->
            <div v-if="isOutOfStock" class="restock-box">
                <div class="restock-box__tip">{{ $t('该商品暂时缺货，留下手机号，到货后通知你') }}</div>
                <div class="restock-box__form">
                    <input v-model="restockMobile" class="restock-box__input" type="tel" maxlength="11"
                        :placeholder="$t('请输入手机号')" />
                    <button class="btn-restock" :disabled="restockSubmitting" @click="handleSubscribeRestock" type="button">
                        {{ restockSubmitting ? $t('提交中...') : $t('到货通知') }}
                    </button>
                </div>
            </div>
            <!-- 购买按钮 -->
            <div class="purchase-section">
                <button v-if="!(activityInfo && activityInfo.activityStatus === 2) && productInfo.virtualGoods !== 0"
                    class="btn-add-cart" @click="handleAddToCart" :disabled="isAddingToCart || isOutOfStock" type="button">
                    <span v-if="isAddingToCart">{{ $t('添加中...') }}</span>
                    <span v-else>{{ $t('加入购物车') }}</span>
                </button>
                <button class="btn-buy-now" @click="handleBuyNow" :disabled="isBuyingNow || isOutOfStock" type="button">
                    <span v-if="isBuyingNow">{{ $t('处理中...') }}</span>
                    <span v-else>
                        {{ activityInfo && activityInfo.activityStartStatus === 1 ? $t('立即抢购') : $t('立即购买') }}
                    </span>
                </button>
            </div>
        </div>
            <!-- 砍价入口（插件 order-bargain）：进砍价专区发起/帮砍 -->
            <div class="bargain-entry">
                <a class="bargain-entry__link" href="javascript:void(0)" @click.prevent="goBargain">
                    {{ $t('想更便宜？去砍价专区砍到底价') }} →
                </a>
            </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick, type Ref } from 'vue'
import MagnifyingGlass from './MagnifyingGlass.vue'
import ModalQrcodeModal from '~/components/Modal/QrcodeModal.vue'
import PopupModal from '~/components/Modal/PopupModal.vue'
import AddressLinkage from '../Address/AddressLinkage.vue'
import AreaCode from '../Address/AreaCode.vue'
import type { GoodsBases, GdsCommon, GoodsImage } from '~/types/goods'
import { useGoodsStore } from '~/stores/modules/goods'
import { useUserStore } from '~/stores/modules/user'
import type { GoodsList, ShopList } from '~/stores/modules/goods'
import { useAuth } from '~/composables/useAuth'
import { checkFavoriteOrNot, addFavoriteGoods, deleteFavoriteGoods } from '~/services/userms.service'
import { DEFAULT_CN_AREA_IDS } from '~/utils/visitorArea'
import { useVisitorRegion } from '~/composables/useVisitorRegion'
import { selectVcTempByShopId, queryGoodsSpecQueryNew } from '~/services/goods.service'
import { detectingImStatus } from '~/services/base.service'
import type { DetectingImStatusData } from '~/types/base'
import { queryDeliveryTime, queryTransportArea } from '~/services/business.service'
import { fetchTierTable, subscribeRestock, type TierTable } from '~/services/storefront-plugin.service'
import { Toast } from '~/composables/useToast'
import { insertShoppingCart } from '~/services/order.service'
import { seckillCheckLimit, discountCheckLimit, insertVcBase } from '~/services/activity.service'
import { ConfirmModal } from '~/composables/useConfirmModal'
import { useShareLink } from '~/composables/useShareLink'
import { usePlus, refreshPlusState } from '~/composables/usePlus'
import { IMCHAT_URL, IMAGE_URL, UCENTER_URL, UNIAPP_URL } from '~/utils/externalUrl'
import { buildConsultationChatPath } from '~/utils/imConsultation.js'
import { ucenterAuthUrl } from '~/utils/ucenterAuthUrl'

import type { ProductInformationProps, ProductCustomType } from '~/types/component'
import type { ActivityInfo } from '~/types/business'
import type { VoucherTemplateVO } from '~/types/goods'

// Props 定义
type Props = ProductInformationProps

const props = withDefaults(defineProps<Props>(), {
    goodsBases: undefined,
    gdsCommon: undefined,
    shopDetail: undefined,
    goodsImages: () => []
})

const { showPlusEntry } = usePlus()

const showDepositGuaranteeTip = computed(() => props.shopDetail?.depositStatus === 1)

const goodsBasesOverride = ref<GoodsBases | undefined>(undefined)
const goodsBasesCurrent = computed<GoodsBases | undefined>(() => goodsBasesOverride.value ?? props.goodsBases)

/**
 * 图片区底部展示的商品编号（commonId），优先 gdsCommon，其次当前规格 goodsBases。
 * @returns 无有效编号时为空字符串，供模板 v-if 隐藏
 */
const imageToolbarCommonId = computed(() => {
    const id = props.gdsCommon?.commonId ?? goodsBasesCurrent.value?.commonId
    if (id === undefined || id === null) return ''
    const s = String(id).trim()
    return s || ''
})

type SpecParamItem = {
    paramName: string
    paramNameValues: string[]
}

const specParamList = computed<SpecParamItem[]>(() => {
    const list = (props.gdsCommon as any)?.specParamList
    if (!Array.isArray(list)) return []
    return list
        .filter((it: any) => it && typeof it.paramName === 'string' && Array.isArray(it.paramNameValues))
        .map((it: any) => ({
            paramName: it.paramName,
            paramNameValues: (it.paramNameValues as any[]).map(String).filter(Boolean)
        }))
        .filter((it: SpecParamItem) => it.paramName && it.paramNameValues.length > 0)
})

const selectedSpecValues = ref<Record<string, string>>({})
const specLoading = ref(false)

/** goodsBases.specValueList → { paramName: paramValue } */
const goodsSpecValueMap = computed(() => {
    const map: Record<string, string> = {}
    const list = props.goodsBases?.specValueList
    if (!Array.isArray(list)) return map
    for (const { paramName, paramValue } of list) {
        if (paramName && paramValue != null && paramValue !== '') {
            map[paramName] = String(paramValue)
        }
    }
    return map
})

/** 用于 watch：specValueList 变化时触发重新初始化 */
const goodsSpecValueMapKey = computed(() =>
    Object.entries(goodsSpecValueMap.value)
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([k, v]) => `${k}:${v}`)
        .join('|')
)

const resolveSpecOptionValue = (
    spec: SpecParamItem,
    preserveCurrent?: string
): string => {
    if (preserveCurrent && spec.paramNameValues.includes(preserveCurrent)) return preserveCurrent
    const preferred = goodsSpecValueMap.value[spec.paramName]
    if (preferred && spec.paramNameValues.includes(preferred)) return preferred
    return spec.paramNameValues[0] ?? ''
}

const buildSelectedSpecValues = (
    list: SpecParamItem[],
    preserve?: Record<string, string>
): Record<string, string> =>
    Object.fromEntries(list.map((spec) => [spec.paramName, resolveSpecOptionValue(spec, preserve?.[spec.paramName])]))

watch(
    () => props.goodsBases,
    () => {
        goodsBasesOverride.value = undefined
    }
)

watch(
    () => [specParamList.value, props.goodsBases?.goodsId, goodsSpecValueMapKey.value] as const,
    ([list, goodsId, mapKey], prev) => {
        if (!list.length) {
            selectedSpecValues.value = {}
            return
        }
        const resetFromGoodsBases = !prev || goodsId !== prev[1] || mapKey !== prev[2]
        selectedSpecValues.value = buildSelectedSpecValues(
            list,
            resetFromGoodsBases ? undefined : selectedSpecValues.value
        )
    },
    { immediate: true }
)

const getSelectedSpecValue = (paramName: string) => {
    return selectedSpecValues.value[paramName] || ''
}

const fetchGoodsBasesBySpec = async () => {
    const commonId = props.gdsCommon?.commonId ?? goodsBasesCurrent.value?.commonId
    if (!commonId) return

    const specValues = specParamList.value
        .map((s) => ({
            paramName: s.paramName,
            paramValue: getSelectedSpecValue(s.paramName)
        }))
        .filter((row) => row.paramName && row.paramValue)

    if (specValues.length !== specParamList.value.length) return

    if (specLoading.value) return
    specLoading.value = true
    try {
        const res = await queryGoodsSpecQueryNew({
            commonId,
            specValues
        })
        if (res?.code === 1 && res.data) {
            goodsBasesOverride.value = res.data
            currentIndex.value = 0
            // 规格切换后，若库存/起订量变化，确保数量落在合法范围内
            const minQty = goodsBasesOverride.value?.moq || 1
            const maxQty = goodsBasesOverride.value?.goodsStock || 0
            if (quantity.value < minQty) quantity.value = minQty
            if (maxQty > 0 && quantity.value > maxQty) quantity.value = maxQty
        } else {
            Toast.error(res?.message || '规格切换失败，请重试')
        }
    } catch (e) {
        Toast.error('规格切换失败，请重试')
    } finally {
        specLoading.value = false
    }
}

const selectSpecValue = async (paramName: string, value: string) => {
    selectedSpecValues.value = {
        ...selectedSpecValues.value,
        [paramName]: value
    }
    await fetchGoodsBasesBySpec()
}

// 根据props数据计算商品信息
const productInfo = computed(() => {
    const gb = goodsBasesCurrent.value
    const goodsPrice = gb?.goodsPrice ?? 0
    const minNumber = gb?.moq || 1; // 最小购买量
    const maxNumber = gb?.goodsStock || 0; // 最大购买量

    return {
        ...gb,
        ...props.gdsCommon,
        minNumber,
        maxNumber,
    } as GoodsBases & GdsCommon & ProductCustomType
})

/**
 * PLUS 标识展示模式：
 * - open: 可开通提示（平台开启 PLUS 且 plusStatus=1 且 plusGoods=1 且非 plusMember）
 * - member_discount: PLUS 会员且有折扣（plusGoods=1 且 plusMember=1 且 plusDiscount 有值）
 * - member_plain: PLUS 会员无折扣（plusGoods=1 且 plusMember=1 且 plusDiscount 为空）
 * - none: 不展示（含平台未开启 PLUS）
 */
const plusBadge = computed<{
    mode: 'open' | 'member_discount' | 'member_plain' | 'none'
    discountText: string
}>(() => {
    if (!showPlusEntry.value) return { mode: 'none', discountText: '10' }

    const info = productInfo.value as ProductCustomType & {
        plusGoods?: unknown
        plusMember?: unknown
        plusDiscount?: unknown
    }
    const plusStatus = Number((props.gdsCommon as { plusStatus?: unknown } | undefined)?.plusStatus ?? 0) === 1
    const isPlusGoods = Number(info.plusGoods ?? 0) === 1
    const isPlusMember = Number(info.plusMember ?? 0) === 1
    const discountRaw = info.plusDiscount
    const hasDiscount = discountRaw != null && String(discountRaw).trim() !== ''

    if (!isPlusGoods) return { mode: 'none', discountText: '10' }
    if (isPlusMember) {
        return {
            mode: hasDiscount ? 'member_discount' : 'member_plain',
            discountText: hasDiscount ? String(discountRaw) : '10'
        }
    }
    return { mode: plusStatus ? 'open' : 'none', discountText: '10' }
})

/** 商品详情 PLUS 标签背景图 */
const plusBadgeBg = `url(${IMAGE_URL('plusImg/xiangqing1.png')})`
/** 商品详情 PLUS 标签前缀图标背景 */
const plusBadgeIconBg = `url(${IMAGE_URL('plusImg/zuanshi.png')})`

/** 从 goodsBases 解析图片列表（规格切换接口会返回 goodsImage / goodsImages） */
const extractGoodsBaseImages = (gb?: GoodsBases): string[] => {
    if (!gb) return []
    const images: string[] = []
    const raw = gb.goodsImages
    if (Array.isArray(raw)) {
        for (const item of raw) {
            const url = typeof item === 'string' ? item : (item as GoodsImage)?.imagesImage
            if (url) images.push(url)
        }
    }
    if (!images.length && gb.goodsImage) {
        images.push(gb.goodsImage)
    }
    return images
}

// 商品图片列表
const productImages = computed(() => {
    if (goodsBasesOverride.value) {
        const specImages = extractGoodsBaseImages(goodsBasesCurrent.value)
        if (specImages.length) return specImages
    }
    if (props.goodsImages?.length) {
        return props.goodsImages.map(img => img.imagesImage).filter(Boolean)
    }
    return goodsBasesCurrent.value?.goodsImage ? [goodsBasesCurrent.value.goodsImage] : []
})

// commonVideo：有值时作为大图第一张自动播放（静音）
const commonVideo = computed(() => props.gdsCommon?.commonVideo || goodsBasesCurrent.value?.commonVideo || '')

// 当前图片索引
const currentIndex = ref(0)

// 当前图片（索引 0 且有 commonVideo 时 MagnifyingGlass 内部会显示视频）
const currentImage = computed(() => {
    return productImages.value[currentIndex.value] || ''
})



// 图片切换
const handleImageChange = (index: number) => {
    currentIndex.value = index
}

// 到货通知（插件 goods-restock）：缺货时订阅
const restockMobile = ref('')
const restockSubmitting = ref(false)

const handleSubscribeRestock = async () => {
    const mobile = restockMobile.value.trim()
    if (!/^1\d{10}$/.test(mobile)) {
        Toast.error('请输入正确的手机号')
        return
    }
    const goodsId = goodsBasesCurrent.value?.goodsId
    if (!goodsId) return
    restockSubmitting.value = true
    try {
        const res = await subscribeRestock({
            goodsId,
            goodsTitle: productInfo.value.goodsName || '',
            mobile
        })
        if (res) {
            Toast.success('订阅成功，到货后通知你')
            restockMobile.value = ''
        } else {
            Toast.error('订阅失败，请稍后再试')
        }
    } finally {
        restockSubmitting.value = false
    }
}

// 砍价专区入口
const goBargain = () => {
    navigateTo('/activity/bargain')
}

// 阶梯价（插件 goods-tierprice）：只读展示，接口失败静默不显示
const tierTable = ref<TierTable | null>(null)

const tierRangeText = (t: { minQty: number; maxQty: number | null }) => {
    if (!t.maxQty) return `${t.minQty}${$t('件及以上')}`
    return `${t.minQty} - ${t.maxQty}${$t('件')}`
}

const tierPriceText = (t: { priceType: string; discountRate: number | null; fixedPrice: number | null }) => {
    if (t.priceType === 'FIXED') return `${formatPriceWithSymbolSafe(t.fixedPrice ?? 0)}/${$t('件')}`
    const rate = Number(t.discountRate ?? 1)
    const zhe = (rate * 10).toFixed((rate * 10) % 1 === 0 ? 0 : 1).replace(/\.0$/, '')
    return `${zhe}${$t('折')}`
}

const loadTierTable = async (goodsId?: number | string | null) => {
    if (!goodsId) {
        tierTable.value = null
        return
    }
    const data = await fetchTierTable(goodsId)
    tierTable.value = data && data.matched ? data : null
}

// 优惠券列表
const voucherList = ref<VoucherTemplateVO[]>([])
const voucherLoading = ref(false)

// 获取优惠券列表
const fetchVoucherList = async () => {
    const shopId = goodsBasesCurrent.value?.shopId
    const goodsId = goodsBasesCurrent.value?.goodsId
    if (!shopId || !goodsId) return
    voucherLoading.value = true
    try {
        const res = await selectVcTempByShopId({
            shopId,
            goodsId: String(goodsId)
        })
        if (res?.code === 1 && Array.isArray(res?.data)) {
            voucherList.value = res.data
        } else {
            voucherList.value = []
        }
    } catch {
        voucherList.value = []
    } finally {
        voucherLoading.value = false
    }
}

// 优惠券领取中，防止重复点击
const voucherReceiving = ref(false)

// 点击可领取优惠券：调用领取接口，成功后刷新列表
const handleCouponReceive = async (item: VoucherTemplateVO) => {
    if (voucherReceiving.value || item.receiveOrNot !== 0) return
    const voucherTId = item.voucherTId
    if (voucherTId == null) return
    voucherReceiving.value = true
    try {
        const res = await insertVcBase({ voucherTId })
        if (res?.code === 1) {
            Toast.success('领取成功')
            await fetchVoucherList()
        } else {
            Toast.error(res?.message ?? '领取失败')
        }
    } catch {
        Toast.error('领取失败')
    } finally {
        voucherReceiving.value = false
    }
}

// 商品服务
const productServices = computed(() => {
    if (!props.gdsCommon?.afterServiceList || props.gdsCommon.afterServiceList.length === 0) {
        return []
    }
    return props.gdsCommon.afterServiceList.map((service, index) => ({
        icon: '✓',
        name: service
    }))
})

// 配送地址选择 - 使用 AddressLinkage 组件
// 默认中国·北京（与现网 hou_base_district 树一致：直辖市 6331 / 北京市 1 / 东城区 32）
// 旧默认 9/1522/1703 已失效（9 现为上海市下辖市 ID），会导致一直显示「请选择」
const selectedArea = ref<string[]>([...DEFAULT_CN_AREA_IDS])

const isValidAreaIds = (provinceId: unknown, cityId: unknown, areaId: unknown) => {
    const p = Number(provinceId)
    const c = Number(cityId)
    const a = Number(areaId)
    return Number.isFinite(p) && p > 0 && Number.isFinite(c) && c > 0 && Number.isFinite(a) && a > 0
}

// 国家/地区代码选择
const showAreaCodeModal = ref(false)
const selectedCountryName = ref('中国')
const selectedPhoneCode = ref('+86') // 保留区号用于其他用途
const selectedCountryId = ref<number>(1) // 国家ID，默认1（中国）
const {
    location: visitorRegion,
    resolveAreaNames,
    setVisitorRegion,
    initializeVisitorRegion
} = useVisitorRegion()
let applyingVisitorRegion = false

// 配送时间文本
const deliveryTimeText = ref<string>('')
// 省市区联动依赖客户端区划数据，SSR/CSR 首帧不一致会 hydration mismatch
const deliveryAreaReady = ref(false)

// 获取当前可售库存（秒杀优先取活动库存）
const getAvailableStock = (): number => {
    const gb = goodsBasesCurrent.value
    if (!gb) return 0
    const activityGoodsDetail = (gb as { activityGoodsDetailDTO?: { goodsStock?: number } })
        .activityGoodsDetailDTO
    if (gb.activityStatus === 2 && activityGoodsDetail?.goodsStock != null) {
        return Number(activityGoodsDetail.goodsStock)
    }
    return Number(gb.goodsStock ?? 0)
}

/**
 * 解析可售区域接口结果。
 * true/false=明确有货/无货；null=接口失败或环境无经典轨表（mall 演示商品），调用方应回退本地库存。
 */
const parseTransportInStock = (transportResponse: any): boolean | null => {
    if (!transportResponse || transportResponse.code !== 1) {
        return null
    }
    if (transportResponse?.data && Array.isArray(transportResponse.data) &&
        transportResponse.data[0] && Array.isArray(transportResponse.data[0]) &&
        transportResponse.data[0][0] === 1) {
        return true
    }
    return false
}

const applyStockFallbackDeliveryText = () => {
    deliveryTimeText.value = getAvailableStock() > 0 ? '有货' : '缺货'
}

// 查询配送信息
const queryDeliveryInfo = async () => {
    // 只有中国才查询省市区配送信息
    if (selectedPhoneCode.value !== '+86') {
        deliveryTimeText.value = ''
        return
    }

    // 检查是否有完整的省市区信息
    if (!selectedArea.value || selectedArea.value.length < 3) {
        deliveryTimeText.value = ''
        return
    }

    const provinceId = Number(selectedArea.value[0])
    const cityId = Number(selectedArea.value[1])
    const areaId = Number(selectedArea.value[2])

    if (!provinceId || !cityId || !areaId) {
        deliveryTimeText.value = ''
        return
    }

    try {
        const goodsId = goodsBasesCurrent.value?.goodsId || 0
        const shopId = goodsBasesCurrent.value?.shopId || 0

        if (!goodsId || !shopId) {
            return
        }

        // 查询配送区域（经典轨 goods_base 在本环境可能不存在，失败时回退 mall 库存）
        const transportResponse = await queryTransportArea({
            shopGoodsIds: [[goodsId]],
            areaIds: [provinceId, cityId, areaId],
            countryId: selectedCountryId.value
        })

        const inStock = parseTransportInStock(transportResponse)
        if (inStock === null) {
            applyStockFallbackDeliveryText()
            return
        }
        if (!inStock) {
            deliveryTimeText.value = '缺货'
            return
        }

        // 有货，查询配送时间（仅中国）；配送时效接口失败仍显示有货
        try {
            const deliveryResponse = await queryDeliveryTime({
                shopId: shopId,
                provinceId: provinceId,
                cityId: cityId,
                areaId: areaId
            })
            if (deliveryResponse?.code === 1 && deliveryResponse?.data) {
                deliveryTimeText.value = `有货-${deliveryResponse.data}`
            } else {
                deliveryTimeText.value = '有货'
            }
        } catch {
            deliveryTimeText.value = '有货'
        }
    } catch (error) {
        console.warn('查询配送信息失败，已回退本地库存:', error)
        applyStockFallbackDeliveryText()
    }
}

// 查询非中国国家的配送信息（只查询是否有货，不查询配送时间）
const queryNonChinaDeliveryInfo = async (countryId: number) => {
    try {
        const goodsId = goodsBasesCurrent.value?.goodsId || 0
        const shopId = goodsBasesCurrent.value?.shopId || 0

        if (!goodsId || !shopId) {
            return
        }

        // 查询配送区域（非中国国家，省市区ID为空数组）
        const transportResponse = await queryTransportArea({
            shopGoodsIds: [[goodsId]],
            areaIds: [],
            countryId: countryId
        })

        const inStock = parseTransportInStock(transportResponse)
        if (inStock === null) {
            applyStockFallbackDeliveryText()
            return
        }
        deliveryTimeText.value = inStock ? '有货' : ''
    } catch (error) {
        console.warn('查询配送信息失败，已回退本地库存:', error)
        applyStockFallbackDeliveryText()
    }
}

// 校验商品库存是否满足购买数量
const checkGoodsStock = (scene: 'cart' | 'buy' = 'buy'): boolean => {
    const availableStock = getAvailableStock()
    if (availableStock <= 0) {
        Toast.warning(
            scene === 'cart' ? '商品库存不足，无法加入购物车' : '商品库存不足，无法购买'
        )
        return false
    }
    if (quantity.value > availableStock) {
        Toast.warning(`库存不足，最多可购买${availableStock}件`)
        return false
    }
    return true
}

// 检查是否有货（用于购买前验证）
const checkStockAvailability = async (): Promise<boolean> => {
    try {
        const goodsId = goodsBasesCurrent.value?.goodsId || 0
        const shopId = goodsBasesCurrent.value?.shopId || 0

        if (!goodsId || !shopId) {
            return false
        }
        if (!checkGoodsStock('buy')) {
            return false
        }

        // 中国：需要省市区信息
        if (selectedPhoneCode.value === '+86') {
            if (!selectedArea.value || selectedArea.value.length < 3) {
                Toast.warning('请选择完整的省市区信息')
                return false
            }

            const provinceId = Number(selectedArea.value[0])
            const cityId = Number(selectedArea.value[1])
            const areaId = Number(selectedArea.value[2])

            if (!provinceId || !cityId || !areaId) {
                Toast.warning('请选择完整的省市区信息')
                return false
            }

            // 查询配送区域；经典轨不可用时回退本地库存（mall 演示商品）
            const transportResponse = await queryTransportArea({
                shopGoodsIds: [[goodsId]],
                areaIds: [provinceId, cityId, areaId],
                countryId: selectedCountryId.value
            })

            const inStock = parseTransportInStock(transportResponse)
            if (inStock === null) {
                return checkGoodsStock('buy')
            }
            if (!inStock) {
                Toast.warning('该地区暂无库存，无法购买')
                return false
            }
            return true
        } else {
            // 非中国：只需要国家ID
            if (!selectedCountryId.value) {
                Toast.warning('请选择国家/地区')
                return false
            }

            const transportResponse = await queryTransportArea({
                shopGoodsIds: [[goodsId]],
                areaIds: [],
                countryId: selectedCountryId.value
            })

            const inStock = parseTransportInStock(transportResponse)
            if (inStock === null) {
                return checkGoodsStock('buy')
            }
            if (!inStock) {
                Toast.warning('该地区暂无库存，无法购买')
                return false
            }
            return true
        }
    } catch (error) {
        console.warn('检查可售区域失败，已回退本地库存:', error)
        return checkGoodsStock('buy')
    }
}

// 处理国家/地区代码选择
const persistCurrentRegion = async () => {
    const areaIds = selectedPhoneCode.value === '+86' && isValidAreaIds(
        selectedArea.value[0], selectedArea.value[1], selectedArea.value[2]
    ) ? selectedArea.value.map(String) : null
    const areaNames = areaIds ? await resolveAreaNames(areaIds) : null
    await setVisitorRegion({
        countryId: selectedCountryId.value,
        countryName: selectedCountryName.value,
        phoneCode: selectedPhoneCode.value,
        areaIds,
        areaNames,
        source: 'manual'
    })
}

const handleAreaCodeSelect = async (area: { name: string; code: string; pinyin: string; countryId?: number }) => {
    selectedCountryName.value = area.name
    selectedPhoneCode.value = area.code
    showAreaCodeModal.value = false

    // 更新国家ID
    if (area.countryId) {
        selectedCountryId.value = area.countryId
    }

    // 如果不是中国，清空省市区选择，只查询是否有货（不查询配送时间）
    if (area.code !== '+86') {
        selectedArea.value = []
        // 查询非中国国家的配送信息（只查询是否有货）
        if (area.countryId) {
            queryNonChinaDeliveryInfo(area.countryId)
        } else {
            deliveryTimeText.value = ''
        }
    } else {
        if (!isValidAreaIds(selectedArea.value[0], selectedArea.value[1], selectedArea.value[2])) {
            selectedArea.value = [...DEFAULT_CN_AREA_IDS]
        }
        // 如果是中国，重新查询配送信息（查询是否有货，如果有货则查询配送时间）
        queryDeliveryInfo()
    }
    await persistCurrentRegion()
}

// 数量管理
const quantity = ref(1)

// 监听productInfo变化，初始化数量
watch(() => productInfo.value.minNumber, (newMinNumber) => {
    if (newMinNumber && quantity.value < newMinNumber) {
        quantity.value = newMinNumber
    }
}, { immediate: true })

// 当商品信息加载完成后获取优惠券列表
const voucherFetchedShopId = ref<number | null>(null)
watch(
    () => goodsBasesCurrent.value?.shopId ?? null,
    (shopId) => {
        if (!shopId) {
            voucherList.value = []
            voucherFetchedShopId.value = null
            return
        }
        // 规格切换会变更 goodsId，但优惠券按店铺维度即可，避免重复请求
        if (voucherFetchedShopId.value === shopId) return
        voucherFetchedShopId.value = shopId
        fetchVoucherList()
    },
    { immediate: true }
)

// 规格切换会变更 goodsId，阶梯区间按商品维度重新拉
watch(
    () => goodsBasesCurrent.value?.goodsId ?? null,
    (goodsId, prev) => {
        if (!goodsId || goodsId === prev) return
        void loadTierTable(goodsId)
    }
)

// 减少数量
const decreaseQuantity = () => {
    if (quantity.value > productInfo.value.minNumber) {
        quantity.value--
    }
}

// 增加数量
const increaseQuantity = () => {
    if (quantity.value < effectiveMaxQuantity.value) {
        quantity.value++
    }
}

// 验证数量输入
const validateQuantity = () => {
    const min = productInfo.value.minNumber
    const max = effectiveMaxQuantity.value

    if (isNaN(quantity.value) || quantity.value < min) {
        quantity.value = min
    } else if (quantity.value > max) {
        quantity.value = max
    }
}

// 加载状态
const isAddingToCart = ref(false)
const isBuyingNow = ref(false)

// 获取 goods store
const goodsStore = useGoodsStore()
const userStore = useUserStore()

// 获取当前路由（用于登录后重定向）
const route = useRoute()

// 获取认证实例（统一管理，避免重复创建）
const { isLogin } = useAuth()

// 是否已登录（仅状态判断，不触发跳转）
// authHydrated：避免 SSR（未登录）与客户端首帧（已恢复登录）DOM 不一致导致 hydration mismatch
const authHydrated = ref(false)
const isLoggedIn = computed(() => authHydrated.value && userStore.isLogin)

// 未登录时点击「立即登录」跳转
const handlePriceLoginClick = () => {
    navigateTo(ucenterAuthUrl('login', route.fullPath), { external: true })
}

// 统一的登录检查函数
const checkLogin = (): boolean => {
    return isLogin(route.fullPath)
}

// 秒杀限购：活动状态2为秒杀，goodsUpperLimit=0不限购，goodsUpperLimit>0限购，限购时限购数量为 goodsUpperLimit
const seckillLimit = computed(() => {
    const info = activityInfo.value
    if (!info || info.activityStatus !== 2) return null
    const limit = info.goodsUpperLimit
    // goodsUpperLimit=0 或不存在时不限购
    if (limit == null || Number(limit) <= 0) return null
    return Number(limit)
})

// 限购展示文案：
// - 秒杀：goodsUpperLimit=0不限购，goodsUpperLimit>0限购 显示「限购: goodsUpperLimit件」
// - 限时折扣：goodsUpperLimit>0 显示「每人每discountUpperTime天限购goodsUpperLimit件」
const seckillLimitDisplay = computed(() => {
    const info = activityInfo.value
    if (!info) return null

    if (info.activityStatus === 2) {
        if (info.goodsUpperLimit == null || info.goodsUpperLimit === 0) return '不限购'
        const limit = info.goodsUpperLimit
        return limit != null ? `限购: ${limit}件` : '不限购'
    }

    if (info.activityStatus === 3) {
        const limit = info.goodsUpperLimit ?? 0
        const day = info.discountUpperTime ?? 0
        if (limit > 0 && day > 0) {
            return `每人每${day}天限购${limit}件`
        }
    }

    return null
})

// 剩余库存展示：秒杀商品显示秒杀库存 activityInfo.goodsStock，否则显示商品库存
const displayStock = computed(() => getAvailableStock())
const isOutOfStock = computed(() => displayStock.value <= 0)

// 有效最大数量：秒杀限购时取 goodsUpperLimit 与秒杀库存的较小值，秒杀不限购时取秒杀库存，非秒杀取商品库存
const effectiveMaxQuantity = computed(() => {
    const info = activityInfo.value
    if (info && info.activityStatus === 2) {
        const stock = displayStock.value
		 const limit = info.goodsUpperLimit
        if (limit && limit>0) {
           
            return limit != null ? Math.min(stock, limit) : stock
        }
        return stock // 不限购，根据秒杀库存
    }
    return productInfo.value.maxNumber ?? 0
})

// 公共验证逻辑：检查登录状态和商品信息
const validateBeforeAction = (): { goodsId: number; moq: number } | null => {
    // 检查登录状态
    if (!checkLogin()) {
        return null // 未登录，已自动跳转到登录页
    }

    if (!goodsBasesCurrent.value) {
        return null
    }

    const goodsId = goodsBasesCurrent.value.goodsId || 0
    const moq = goodsBasesCurrent.value.moq || productInfo.value.minNumber || 1

    if (!goodsId) {
        Toast.error('商品信息不完整')
        return null
    }

    // 秒杀限购校验：数量不能超过限购
    const limit = seckillLimit.value
    if (limit != null&&limit !== 0 && quantity.value > limit) {
        Toast.warning(`超过限购数量，每人限购${limit}件`)
        return null
    }

    return { goodsId, moq }
}

// 通用异步操作包装器（带 loading 状态和错误处理）
const withLoading = async <T>(
    loadingRef: Ref<boolean>,
    operation: () => Promise<T>,
    errorMessage: string
): Promise<T | null> => {
    try {
        loadingRef.value = true
        return await operation()
    } catch (error: any) {
        console.error(errorMessage, error)
        Toast.error(error?.message || errorMessage)
        return null
    } finally {
        loadingRef.value = false
    }
}

// 显示成功确认框的公共函数
const showSuccessModal = (message: string, navigatePath?: string) => {
    ConfirmModal.show({
        message,
        onKeepShopping: () => {
            // 再逛逛，关闭弹窗即可
        },
        onGoNow: () => {
            if (navigatePath) {
                navigateTo(navigatePath)
            }
        }
    })
}

// 处理加入购物车按钮点击
const handleAddToCart = () => {
    addToCart()
}

// 处理立即购买按钮点击
const handleBuyNow = () => {
    const userShopId = userStore.getUserInfo?.shopId != null ? String(userStore.getUserInfo.shopId) : null
    const productShopId = goodsBasesCurrent.value?.shopId != null ? String(goodsBasesCurrent.value.shopId) : null
    if (userShopId != null && productShopId != null && userShopId === productShopId) {
        Toast.warning('请勿购买自家商品！')
        return
    }
    buyNow()
}

// 限时折扣限购校验（加入购物车/下单复用）
const checkDiscountLimitBeforeSubmit = async (): Promise<boolean> => {
    const info = activityInfo.value
    // 限时折扣未开始时，不做折扣限购校验（加入购物车/立即购买均放行）
    if (!info || info.activityStatus !== 3 || info.activityStartStatus !== 1) return true
    const commonId = goodsBasesCurrent.value?.commonId ?? props.gdsCommon?.commonId
    if (!commonId) return true

    const discountLimitRes = await discountCheckLimit({
        list: [{ commonId: Number(commonId), goodsNum: Number(quantity.value) }]
    })
    if (discountLimitRes?.code !== 1) {
        Toast.error(String(discountLimitRes?.data || discountLimitRes?.message || '折扣商品限购校验失败'))
        return false
    }
    return true
}

// 加入购物车
const addToCart = async () => {
    const validated = validateBeforeAction()
    if (!validated) return

    const { goodsId, moq } = validated
    // 折扣商品：先校验限购
    if (!(await checkDiscountLimitBeforeSubmit())) return
    if (!checkGoodsStock('cart')) return

    await withLoading(
        isAddingToCart,
        async () => {
            const response = await insertShoppingCart({
                goodsId,
                goodsNum: quantity.value,
                moq
            })
            if (response?.code === 1) {
                useGoodsStore().fetchCartCount()
                showSuccessModal('立即去购物车查看', '/cart/shopping')
            } else {
                Toast.error(response?.message || '加入购物车失败，请稍后重试')
            }
        },
        '加入购物车失败，请稍后重试'
    )
}

// 构建订单商品数据
const buildGoodsItem = (): GoodsList | null => {
    if (!goodsBasesCurrent.value) {
        return null
    }

    const activity = activityInfo.value
    const useActivityPrice = activity && (
        activity.activityStartStatus === 1 ||
        MANJIAN_ACTIVITY_TYPES.includes(activity.activityStatus as number)
    )
    const singlePrice = useActivityPrice
        ? (activity?.activityPrice ?? activity?.goodsPrice ?? 0) || productInfo.value.goodsPrice || 0
        : productInfo.value.goodsPrice || 0
    const goodsTotal = singlePrice * quantity.value
    const goodsImage = currentImage.value || productImages.value[0] || goodsBasesCurrent.value.goodsImage || ''
    const goodsSpecifications = goodsBasesCurrent.value.goodsSpec || props.gdsCommon?.commonSpecValue || ''
    const activityType = activity?.activityStatus ? String(activity.activityStatus) : ''

    return {
        goodsId: String(goodsBasesCurrent.value.goodsId || ''),
        commonId: String(goodsBasesCurrent.value.commonId || ''),
        goodsImage: goodsImage,
        goodsName: goodsBasesCurrent.value.goodsName || '',
        dealerGoodsFlag: goodsBasesCurrent.value.dealerGoodsFlag || 0,
        goodsNumber: quantity.value,
        goodsSpecifications: goodsSpecifications,
        goodsCode: goodsBasesCurrent.value.goodsCode || '',
        goodsStock: goodsBasesCurrent.value.goodsStock || 0,
        singlePrice: singlePrice,
        goodsTotal: goodsTotal,
        activityType: activityType
    }
}

// 立即购买
const buyNow = async () => {
    const validated = validateBeforeAction()
    if (!validated) return

    // 秒杀/折扣商品：先校验限购
    const info = activityInfo.value
    if (info && info.activityStatus === 2) {
        const goodsId = goodsBasesCurrent.value?.goodsId
        const commonId = goodsBasesCurrent.value?.commonId ?? props.gdsCommon?.commonId
        if (goodsId && commonId) {
            const res = await seckillCheckLimit({
                goodsId: Number(goodsId),
                commonId: Number(commonId)
            })
            const data = res?.data
            if (res?.code === 2 || data == seckillLimit.value) {
                Toast.error(res?.message || '已达秒杀最大限购数量')
                return
            }
            if (res?.code !== 1) {
                Toast.error(res?.message || '秒杀限购校验失败，请稍后重试')
                return
            }
        }
    }
    // 折扣商品：先校验限购
    if (!(await checkDiscountLimitBeforeSubmit())) return

    // 检查是否有货
    const hasStock = await checkStockAvailability()
    if (!hasStock) {
        return // 无货，已提示用户
    }

    await withLoading(
        isBuyingNow,
        async () => {
            const goodsItem = buildGoodsItem()
            if (!goodsItem || !goodsBasesCurrent.value) {
                Toast.error('商品信息不完整')
                return
            }

            const shopItem: ShopList = {
                shopId: goodsBasesCurrent.value.shopId || 0,
                shopName: goodsBasesCurrent.value.shopName || '',
                shopInvoices: props.shopDetail?.shopInvoices || 0,
                invoiceActive: props.shopDetail?.invoiceActive || 0,
                goodsList: [goodsItem],
                shopTotalPrice: goodsItem.goodsTotal,
                virtualGoods: goodsBasesCurrent.value.virtualGoods
            }

            goodsStore.clearShopList()
            goodsStore.setShopList([shopItem])
            await navigateTo('/confirmOrder')
        },
        '立即购买失败，请稍后重试'
    )
}


// 收藏状态
const isFavorited = ref(false)
const favoriteLoading = ref(false)

// 查询收藏状态
const fetchFavoriteStatus = async () => {
    const commonId = props.gdsCommon?.commonId ?? goodsBasesCurrent.value?.commonId
    if (!commonId) return
    try {
        const res = await checkFavoriteOrNot({ commonId })
        isFavorited.value = res?.data === 1
    } catch {
        isFavorited.value = false
    }
}

// 当 commonId 变化时查询收藏状态（需在 fetchFavoriteStatus 定义之后）
// 禁止 immediate：SSR/首帧请求会改 DOM（收藏文案），易触发 hydration mismatch
watch(() => props.gdsCommon?.commonId ?? goodsBasesCurrent.value?.commonId, (commonId) => {
    if (!import.meta.client || !authHydrated.value) return
    if (commonId) {
        fetchFavoriteStatus()
    } else {
        isFavorited.value = false
    }
})

// 分享弹窗
const showShareModal = ref(false)
const showShareQrcodeModal = ref(false)

// 分享链接 API
const { shareLink, qzoneShareUrl, openShare } = useShareLink(() => ({
    commonId: props.gdsCommon?.commonId ?? goodsBasesCurrent.value?.commonId ?? '',
    goodsId: goodsBasesCurrent.value?.goodsId ?? '',
    title: goodsBasesCurrent.value?.goodsName ?? '商品分享',
    imageUrl: currentImage.value
}))

// 分享
const handleShare = () => {
    showShareModal.value = true
}

// 微信分享：显示二维码
const handleShareToWechat = () => {
    showShareModal.value = false
    showShareQrcodeModal.value = true
}

// 其他平台分享：新窗口打开
const handleShareToOther = (platform: 'qq' | 'facebook' | 'ins') => {
    openShare(platform)
    showShareModal.value = false
}

// 收藏/取消收藏
const handleFavorite = async () => {
    if (!checkLogin()) return
    const commonId = goodsBasesCurrent.value?.commonId ?? props.gdsCommon?.commonId
    const goodsId = goodsBasesCurrent.value?.goodsId
    if (!commonId || !goodsId) {
        Toast.warning('商品信息不完整')
        return
    }
    if (favoriteLoading.value) return
    favoriteLoading.value = true
    try {
        const isCancel = isFavorited.value
        const res = isCancel
            ? await deleteFavoriteGoods({ commonIds: [String(commonId)] })
            : await addFavoriteGoods({ commonId: String(commonId), goodsId: String(goodsId) })

        if (res?.code === 1) {
            isFavorited.value = !isCancel
            Toast.success(isCancel ? '取消收藏成功' : '收藏成功')
        } else {
            Toast.error(res?.message || '操作失败，请重试')
        }
    } catch (e) {
        Toast.error('操作失败，请重试')
    } finally {
        favoriteLoading.value = false
    }
}

// 客服配置：data == null 或 configEnable != 1 时隐藏
const customerServiceConfig = ref<DetectingImStatusData | null>(null)
const showCustomerService = computed(() => {
    const cfg = customerServiceConfig.value
    return cfg != null && cfg.configEnable === 1
})

const isGroupBuyGoods = computed(() => Number(goodsBasesCurrent.value?.activityStatus || 0) === 4)

/**
 * 跳转到 H5/手机端拼团详情页。
 * @returns void
 */
const handleGoToH5GroupBuy = () => {
    const goodsId = goodsBasesCurrent.value?.goodsId
    if (!goodsId) return
    UNIAPP_URL(`/pages/description/descrption?goodsId=${goodsId}`)
}

// 查询客服配置
const fetchCustomerServiceConfig = async () => {
    const shopId = goodsBasesCurrent.value?.shopId ?? props.shopDetail?.shopId
    if (!shopId) return
    try {
        const res = await detectingImStatus({ configKey: `customer_service_type_${shopId}` })
        customerServiceConfig.value = res?.data ?? null
    } catch {
        customerServiceConfig.value = null
    }
}

// 当 shopId 变化时查询客服配置（需在 fetchCustomerServiceConfig 定义之后）
// 禁止 immediate：客服按钮显隐变更会发生在 hydrate 窗口内
watch(() => goodsBasesCurrent.value?.shopId ?? props.shopDetail?.shopId, (shopId) => {
    if (!import.meta.client || !authHydrated.value) return
    if (shopId) {
        fetchCustomerServiceConfig()
    } else {
        customerServiceConfig.value = null
    }
})

// 客服点击：configValue 1-IM聊天 2-显示手机号 3-跳转链接
const handleCustomerService = () => {
    const cfg = customerServiceConfig.value
    const shopId = goodsBasesCurrent.value?.shopId ?? props.shopDetail?.shopId
    if (!cfg || !shopId) return
    const v = cfg.configValue
    const v2 = cfg.configValue2 ?? ''
    if (v === '1') {
        const chatPath = buildConsultationChatPath({
            shopId,
            goodsId: productInfo.value.goodsId,
            commonId: productInfo.value.commonId,
            commonName: productInfo.value.goodsName,
            commonImage: currentImage.value || productInfo.value.commonImage || productInfo.value.goodsImage,
            commonPrice: productInfo.value.commonPrice ?? productInfo.value.goodsPrice
        })
        if (chatPath) IMCHAT_URL(chatPath)
    } else if (v === '2') {
        if (v2) {
            ConfirmModal.confirm({
                title: '店铺联系电话',
                message: v2,
                confirmText: '确定'
            })
        } else {
            Toast.warning('暂无联系电话')
        }
    } else if (v === '3') {
        if (v2) {
            window.open(v2, '_blank', 'noopener,noreferrer')
        } else {
            Toast.warning('暂无客服链接')
        }
    }
}

const activityInfo = computed<ActivityInfo | null>(() => {
    const gb = goodsBasesCurrent.value
    const activityStatus = gb?.activityStatus
    const activityGoodsDetail = (gb as any)?.activityGoodsDetailDTO

    // 拼团活动（4）在商品详情页按非活动商品展示；状态0（不参加）或无活动详情同样返回 null
    if (activityStatus === 4 || activityStatus === 0 || !activityStatus || !activityGoodsDetail) {
        return null
    }

    // 直接返回 activityGoodsDetail 的原始数据，不进行映射
    return {
        ...activityGoodsDetail,
        activityStatus // 保留 activityStatus
    } as ActivityInfo
})

// 根据 activityStatus 获取活动名称
const getActivityName = (status?: number): string => {
    const activityNameMap: Record<number, string> = {
        1: '砍价',
        2: '限时秒杀',
        3: '限时折扣',
        4: '拼团',
        5: '满减赠',
        6: '满减折'
    }
    return status ? (activityNameMap[status] || '') : ''
}

// 满减赠(5)、满减折(6) 活动类型
const MANJIAN_ACTIVITY_TYPES = [5, 6]

// 是否应显示活动价：活动进行中 或 满减赠/满减折（此类活动可能无 activityStartStatus）
const shouldShowActivityPrice = computed(() => {
    const info = activityInfo.value
    if (!info) return false
    if (info.activityStartStatus === 1) return true
    if (MANJIAN_ACTIVITY_TYPES.includes(info.activityStatus as number)) return true
    return false
})

// 活动价展示值：优先 activityPrice，其次 goodsPrice，最后原价
const displayActivityPrice = computed(() => {
    const info = activityInfo.value
    if (!info) return productInfo.value.commonPrice ?? productInfo.value.goodsPrice
    const price = info.activityPrice ?? productInfo.value.goodsPrice ?? productInfo.value.commonPrice ?? productInfo.value.goodsPrice
    return price
})

// 倒计时文本
const countdownText = ref('')
// 当前剩余时间（毫秒），用于实时递减
const currentRemainingTime = ref<number | null>(null)

// 格式化倒计时
const formatCountdown = (diff: number): string => {
    if (diff <= 0) {
        return '活动已结束'
    }

    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)

    if (days > 0) {
        return `${days}天 ${hours}小时 ${minutes}分 ${seconds}秒`
    } else if (hours > 0) {
        return `${hours}小时 ${minutes}分 ${seconds}秒`
    } else if (minutes > 0) {
        return `${minutes}分 ${seconds}秒`
    } else {
        return `${seconds}秒`
    }
}

// 更新倒计时
const updateCountdown = () => {
    if (!activityInfo.value) {
        countdownText.value = ''
        currentRemainingTime.value = null
        return
    }

    const info = activityInfo.value

    // 如果 currentRemainingTime 有值，说明正在使用 overplusTime 递减
    if (currentRemainingTime.value !== null && currentRemainingTime.value > 0) {
        currentRemainingTime.value -= 1000
        countdownText.value = formatCountdown(currentRemainingTime.value)
        return
    }

    // 优先使用 overplusTime（剩余时间，单位：毫秒）
    if (info.overplusTime !== undefined && info.overplusTime > 0) {
        currentRemainingTime.value = info.overplusTime
        countdownText.value = formatCountdown(info.overplusTime)
        return
    }

    // 如果没有 overplusTime，则根据时间字段计算
    // 使用原始字段名：activityStartStatus === 1 已开始，显示结束倒计时；activityStartStatus === 2 未开始，显示开始倒计时
    const targetTime = info.activityStartStatus === 1 ? info.activityEndTime : info.activityStartTime

    if (!targetTime) {
        countdownText.value = '--'
        currentRemainingTime.value = null
        return
    }

    const target = new Date(targetTime).getTime()
    const now = Date.now()
    const diff = target - now

    countdownText.value = formatCountdown(diff)
    currentRemainingTime.value = null
}

// 定时器
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 监听活动信息变化，启动或停止倒计时
watch(activityInfo, (newVal, oldVal) => {
    if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
    }

    // 重置剩余时间
    currentRemainingTime.value = null

    if (newVal) {
        // 初始化倒计时
        updateCountdown()
        // 启动定时器，每秒更新一次
        countdownTimer = setInterval(() => {
            updateCountdown()
        }, 1000)
    } else {
        countdownText.value = ''
        currentRemainingTime.value = null
    }
}, { immediate: true })

// 秒杀限购时，数量超过限购则自动修正
watch(effectiveMaxQuantity, (newMax) => {
    if (newMax != null && quantity.value > newMax) {
        quantity.value = newMax
    }
}, { immediate: true })

const applyVisitorRegionToDelivery = async (region: typeof visitorRegion.value) => {
    if (!region) return
    const sameCountry = selectedCountryId.value === region.countryId
        && selectedPhoneCode.value === region.phoneCode
    const nextIds = Array.isArray(region.areaIds) ? region.areaIds.map(String) : []
    const sameArea = nextIds.length === selectedArea.value.length
        && nextIds.every((id, index) => id === String(selectedArea.value[index]))
    if (sameCountry && sameArea) {
        if (region.phoneCode === '+86') await queryDeliveryInfo()
        else await queryNonChinaDeliveryInfo(region.countryId)
        return
    }

    applyingVisitorRegion = true
    selectedCountryId.value = region.countryId
    selectedCountryName.value = region.countryName
    selectedPhoneCode.value = region.phoneCode
    selectedArea.value = nextIds
    await nextTick()
    applyingVisitorRegion = false

    if (region.phoneCode === '+86') {
        await queryDeliveryInfo()
    } else {
        await queryNonChinaDeliveryInfo(region.countryId)
    }
}

// 加载配送地区：手动选择 → 用户地址 → IP 定位 → 中国·北京兜底。
const loadDefaultAddress = async () => {
    const region = await initializeVisitorRegion(userStore.isLogin)
    await applyVisitorRegionToDelivery(region)
}

// 监听省市区变化，重新查询配送信息
watch(() => selectedArea.value, (newVal, oldVal) => {
    // 只有在中国且完整选择完省市区（三个级别）后才查询
    if (selectedPhoneCode.value !== '+86') {
        return
    }

    // 检查新值是否完整（三个级别都有值）
    if (!newVal || !Array.isArray(newVal) || newVal.length !== 3) {
        return
    }

    // 检查三个值是否都存在且有效
    if (!newVal[0] || !newVal[1] || !newVal[2]) {
        return
    }

    // 检查是否与旧值不同（避免重复查询）
    if (oldVal &&
        Array.isArray(oldVal) &&
        oldVal.length === 3 &&
        oldVal[0] === newVal[0] &&
        oldVal[1] === newVal[1] &&
        oldVal[2] === newVal[2]) {
        return
    }

    // 使用 nextTick 确保数据已更新
    nextTick(() => {
        queryDeliveryInfo()
        if (!applyingVisitorRegion) void persistCurrentRegion()
    })
}, { deep: true })

// 顶部切换区域时，商品详情的配送与库存查询立即跟随。
watch(visitorRegion, (region) => {
    if (deliveryAreaReady.value && region) void applyVisitorRegionToDelivery(region)
}, { deep: true })

// 组件挂载时加载默认地址，并确保 PLUS 平台开关已拉取
onMounted(async () => {
    await nextTick()
    authHydrated.value = true
    deliveryAreaReady.value = true
    // hydrate 后再拉收藏/客服，避免首帧 DOM 与 SSR 不一致
    void fetchFavoriteStatus()
    void fetchCustomerServiceConfig()
    loadDefaultAddress()
    await refreshPlusState()
    void loadTierTable(goodsBasesCurrent.value?.goodsId)
})

// 组件卸载时清除定时器
onBeforeUnmount(() => {
    if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
    }
})
</script>

<style lang="scss" scoped>
.product-information {
    display: flex;
    gap: 40px;
    padding: 24px;
    background-color: #fff;
    border-radius: 8px;

    @media (max-width: 1200px) {
        flex-direction: column;
        gap: 24px;
        padding: 20px;
    }

    @media (max-width: 768px) {
        padding: 16px;
        gap: 20px;
    }
}

.product-image {
    flex: 0 0 350px;
    position: relative;

    @media (max-width: 1200px) {
        flex: 0 0 auto;
        width: 100%;
        max-width: 500px;
        margin: 0 auto;
    }

    @media (max-width: 768px) {
        max-width: 100%;
    }

    .image-actions {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        align-items: center;
        width: 100%;
        gap: 8px 12px;
        margin-top: 17px;
        padding-top: 13px;
        border-top: 1px solid #f0f0f0;

        &:has(> .image-actions-common-id) {
            justify-content: space-between;
        }

        &:not(:has(> .image-actions-common-id)) {
            justify-content: center;
        }

        @media (max-width: 768px) {
            margin-top: 13px;
            padding-top: 10px;
        }

        .image-actions-common-id {
            flex: 1 1 auto;
            min-width: 0;
            font-size: 12px;
            color: #666;
            line-height: 1.4;
            text-align: left;
            word-break: break-all;
        }

        .image-actions-items {
            flex: 0 0 auto;
            display: flex;
            flex-wrap: wrap;
            justify-content: flex-end;
            align-items: center;
            gap: 17px;

            @media (max-width: 768px) {
                gap: 13px;
            }
        }

        .action-item {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 6px;
            cursor: pointer;
            transition: all 0.3s ease;
            padding: 7px;
            border-radius: 4px;

            &:hover {
                transform: translateY(-2px);
            }

            &:active {
                transform: translateY(0);
            }

            &.is-loading {
                pointer-events: none;
                opacity: 0.7;
            }

            &.is-favorited .action-text {
                color: var(--color);
            }

            .action-icon {
                font-size: 18px;
                line-height: 1;
                display: inline-block;
                user-select: none;
            }

            .action-text {
                font-size: 12px;
                color: #666;
                white-space: nowrap;
            }

            @media (max-width: 768px) {
                padding: 4px;

                .action-icon {
                    font-size: 18px;
                }

                .action-text {
                    font-size: 12px;
                }
            }
        }
    }

    .service-section {
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid #f0f0f0;

        @media (max-width: 768px) {
            margin-top: 16px;
            padding-top: 12px;
        }

        .label {
            font-size: 14px;
            color: #666;
            display: block;
            margin-bottom: 10px;
            font-weight: 500;
        }

        .service-list {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            justify-content: flex-start;

            @media (max-width: 768px) {
                gap: 6px;
            }

            .service-item {
                display: flex;
                align-items: center;
                gap: 4px;
                padding: 5px 6px;
                border: none;
                border-radius: 4px;
                background-color: #fff;
                cursor: pointer;
                transition: all 0.3s ease;

                &:hover {
                    background-color: color-mix(in srgb, var(--color) 8%, #fff);
                }

                .service-icon {
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    width: 20px;
                    height: 20px;
                    background-color: var(--color);
                    color: #fff;
                    border-radius: 50%;
                    font-size: 12px;
                    font-weight: bold;
                    flex-shrink: 0;

                    @media (max-width: 768px) {
                        width: 18px;
                        height: 18px;
                        font-size: 11px;
                    }
                }

                .service-name {
                    font-size: 12px;
                    color: #333;
                    white-space: nowrap;

                    @media (max-width: 768px) {
                        font-size: 11px;
                    }
                }
            }
        }
    }

}

.product-details {
    flex: 1;
    min-width: 0;

    @media (max-width: 1200px) {
        width: 100%;
    }

    .product-goodsName {
        font-size: 18px;
        font-weight: 600;
        color: #333;
        margin-bottom: 20px;
        line-height: 1.5;
        word-break: break-word;

        .virtual-goods-tag {
            margin-left: 6px;
            font-size: 14px;
            font-weight: 500;
            color: #fa8c16;
        }

        @media (max-width: 768px) {
            font-size: 17px;
            margin-bottom: 16px;
        }

        @media (max-width: 480px) {
            font-size: 16px;
            margin-bottom: 12px;
        }
    }

    .price-section {
        margin-bottom: 20px;
        padding: 0;
        background-color: #F5F5F5;
        border-radius: 6px;

        @media (max-width: 768px) {
            margin-bottom: 16px;
        }

        &.has-activity {
            border: 1px solid var(--rgba4);
        }

        .price-content {
            display: flex;
            align-items: flex-start;
            gap: 20px;
            flex-wrap: wrap;
            padding: 12px 16px;

            @media (max-width: 768px) {
                gap: 14px;
                padding: 10px 12px;
            }
        }

        .price-main {
            flex: 1;
            min-width: 0;

            .step-price-grid {
                margin-top: 10px;
                display: flex;
                align-items: stretch;
                gap: 0;
            }

            .step-price-label-col {
                display: flex;
                flex-direction: column;
                border: 1px solid #e8e8e8;
                border-radius: 4px;
                overflow: hidden;
                flex: 0 0 56px;
            }

            .step-price-label {
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 6px 10px;
                font-size: 12px;
                font-weight: 500;
                text-align: center;
                white-space: nowrap;

                &--price {
                    background: #fff;
                    color: var(--color);
                    border-bottom: 1px solid rgba(0, 0, 0, 0.08);
                }

                &--quantity {
                    background: var(--color);
                    color: #fff;
                }
            }

            .step-price-tiers {
                display: flex;
                flex-wrap: wrap;
                gap: 0;
                align-items: stretch;
            }

            .step-price-tier-col {
                display: flex;
                flex-direction: column;
                border-radius: 4px;
                overflow: hidden;
                border: 1px solid #e8e8e8;
                min-width: 100px;
            }

            .step-price-value {
                padding: 6px 10px;
                font-size: 12px;
                font-weight: 600;
                text-align: center;
                white-space: nowrap;

                &--price {
                    background: #fff;
                    color: var(--color);
                }

                &--quantity {
                    background: var(--color);
                    color: #fff;
                }
            }

            .login-tip {
                margin-top: 8px;
                font-size: 12px;
                color: #666;
                line-height: 1.5;

                .login-link {
                    color: var(--color);
                    text-decoration: none;
                    margin-left: 4px;
                    cursor: pointer;

                    &:hover {
                        text-decoration: underline;
                    }
                }

                @media (max-width: 768px) {
                    margin-top: 6px;
                    font-size: 12px;
                }
            }
        }

        .price-label {
            display: flex;
            align-items: baseline;
            gap: 6px;
            flex-wrap: wrap;

            .label {
                font-size: 14px;
                color: #666;
                font-weight: 500;
            }

            .price {
                font-size: 20px;
                font-weight: 700;
                color: var(--color);
                line-height: 1.2;

                @media (max-width: 768px) {
                    font-size: 18px;
                }

                @media (max-width: 480px) {
                    font-size: 16px;
                }

            }

            .original-price {
                font-size: 12px;
                color: #999;
                text-decoration: line-through;
                margin-left: 6px;
            }
        }

        .plus-badge {
            margin-top: 6px;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 10px;
            background-image: v-bind('plusBadgeBg');
            background-size: 100% 100%;
            background-position: center;
            background-repeat: no-repeat;
            font-size: 12px;
            line-height: 1;
            white-space: nowrap;

            &__icon {
                width: 16px;
                height: 16px;
                flex: 0 0 auto;
                background-image: v-bind('plusBadgeIconBg');
                background-size: 200%;
                background-position: center;
                background-repeat: no-repeat;
            }

            &__prefix {
                color: #fde7cb;
            }

            &__text {
                color: #333333;
                margin-left: 20px;
            }
        }

        .promotion-content {
            display: flex;
            align-items: flex-start;
            gap: 6px;
            margin-top: 8px;
            font-size: 12px;

            .promotion-label {
                color: #999;
                flex-shrink: 0;
            }

            .promotion-text {
                color: #f5222d;
                line-height: 1.5;
                white-space: pre-line;
            }
        }

        .group-buy-tip {
            margin-top: 8px;
            font-size: 12px;
            line-height: 1.5;
            color: #8c8c8c;

            &__link {
                margin-left: 6px;
                color: var(--color, #1677ff);
                cursor: pointer;
                text-decoration: underline;
            }
        }

        .virtual-goods-tip {
            margin-top: 8px;
            font-size: 12px;
            line-height: 1.5;
            color: #fa8c16;
        }

        .brand-section {
            flex-shrink: 0;
            padding: 0;
            margin: 0;
            background-color: transparent;

            @media (max-width: 768px) {
                width: 100%;
            }

            .brand-info {
                display: flex;
                flex-direction: column;
                align-items: flex-start;
                gap: 6px;

                .brand-logo {
                    width: 80px;
                    object-fit: contain;
                    margin-bottom: 2px;
                }
            }
        }
    }

    .activity-section {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin: 0;
        padding: 10px 16px;
        background: linear-gradient(180deg, #fff 0%, color-mix(in srgb, var(--color) 40%, #fff) 0%, color-mix(in srgb, var(--color) 50%, #fff) 100%);
        border-radius: 6px 6px 0 0;
        border-bottom: 1px solid color-mix(in srgb, var(--color) 12%, #fff);
        position: relative;
        overflow: hidden;

        &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 3px;
            height: 20px;
            background: var(--color);
            border-radius: 0 3px 3px 0;
        }

        @media (max-width: 768px) {
            flex-direction: column;
            align-items: flex-start;
            gap: 8px;
            padding: 8px 12px;
        }

        .activity-name {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: #666;
            flex-wrap: wrap;
            padding-left: 2px;

            .activity-icon {
                width: 16px;
                height: 16px;
                flex-shrink: 0;
                opacity: 0.95;
                color: #fff;
            }

            .activity-name-text {
                font-weight: 600;
                color: #fff;
            }

            .activity-status {
                font-size: 12px;
                color: #fff;
            }

            @media (max-width: 480px) {
                font-size: 12px;
            }
        }

        .activity-countdown {
            display: flex;
            align-items: center;
            padding: 3px 10px;
            background: color-mix(in srgb, var(--color) 8%, #fff);
            border-radius: 6px;
            border: 1px solid color-mix(in srgb, var(--color) 15%, #fff);

            .countdown-time {
                font-size: 12px;
                color: var(--color);
                font-weight: 600;
                font-variant-numeric: tabular-nums;

                @media (max-width: 480px) {
                    font-size: 11px;
                }
            }
        }
    }

    .inventory-section {
        display: flex;
        align-items: center;
        gap: 10px;
        margin: 10px 0px;
        flex-wrap: wrap;

        @media (max-width: 768px) {
            margin: 10px 0px;
        }

        .label {
            font-size: 14px;
            color: #666;
            font-weight: 500;
        }

        .value {
            font-size: 14px;
            color: var(--color);
            font-weight: 600;
        }

        .separator {
            color: #d9d9d9;
            margin: 0 4px;
        }

        .new-product-tag {
            display: inline-block;
            font-size: 12px;
            color: #fff;
            background: linear-gradient(135deg, var(--color) 0%, color-mix(in srgb, var(--color) 85%, #fff) 100%);
            padding: 2px 8px;
            border-radius: 4px;
            font-weight: 500;
        }
    }

    .delivery-section {
        margin-bottom: 0;
        padding-top: 16px;
        padding-bottom: 16px;
        background-color: #fafafa;
        border-radius: 6px;

        @media (max-width: 768px) {
            margin-bottom: 0;
            padding-top: 12px;
            padding-bottom: 12px;
        }

        .delivery-item {
            display: flex;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;

            @media (max-width: 768px) {
                flex-direction: column;
                align-items: flex-start;
                gap: 12px;
            }

            .label {
                font-size: 14px;
                color: #666;
                font-weight: 500;
                white-space: nowrap;
            }

            .location-select {
                display: flex;
                align-items: center;
                gap: 8px;
                flex: 1;
                min-width: 0;

                @media (max-width: 480px) {
                    width: 100%;
                    flex-direction: column;
                    align-items: stretch;
                }

                .phone-code-selector {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    padding: 8px 12px;
                    border: 1px solid #d9d9d9;
                    border-radius: 4px;
                    font-size: 14px;
                    color: #333;
                    background-color: #fff;
                    cursor: pointer;
                    transition: all 0.3s ease;
                    white-space: nowrap;
                    min-width: 80px;

                    &:hover {
                        border-color: var(--color);
                    }

                    .arrow-down {
                        font-size: 10px;
                        color: #999;
                        transition: transform 0.3s ease;
                    }

                    @media (max-width: 480px) {
                        width: 100%;
                    }
                }
            }

            .delivery-time {
                display: flex;
                align-items: center;
                gap: 6px;
                font-size: 14px;
                color: var(--color);
                font-weight: 500;
                white-space: nowrap;
            }
        }
    }

    .sku-section {
        margin-top: 12px;
        padding-top: 12px;
        border-top: 1px dashed #e8e8e8;

        .sku-row {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 10px 0;
            flex-wrap: wrap;

            &:first-child {
                padding-top: 0;
            }
        }

        .label {
            font-size: 14px;
            color: #666;
            font-weight: 500;
            white-space: nowrap;
            flex-shrink: 0;
        }

        .sku-options {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }

        .sku-option {
            padding: 6px 10px;
            font-size: 13px;
            line-height: 1.2;
            border-radius: 6px;
            border: 1px solid #d9d9d9;
            background: #fff;
            color: #333;
            cursor: pointer;
            transition: all 0.2s ease;

            &:hover {
                border-color: var(--color);
                color: var(--color);
            }

            &.active {
                border-color: var(--color);
                background: var(--rgba2);
                color: var(--color);
                font-weight: 600;
            }
        }
    }

    .minNumber-section {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 0;
        flex-wrap: wrap;

        @media (max-width: 768px) {
            margin-bottom: 0;
            gap: 10px;
        }

        .label {
            font-size: 14px;
            color: #666;
            font-weight: 500;
            white-space: nowrap;
        }

        .minNumber-controls {
            display: flex;
            align-items: center;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            overflow: hidden;
            background-color: #fff;

            .btn-minus,
            .btn-plus {
                width: 36px;
                height: 36px;
                display: flex;
                align-items: center;
                justify-content: center;
                border: none;
                background-color: #fafafa;
                cursor: pointer;
                font-size: 18px;
                color: #333;
                transition: all 0.3s ease;
                user-select: none;

                &:disabled {
                    opacity: 0.4;
                    cursor: not-allowed;
                    background-color: #f5f5f5;
                }

                &:hover:not(:disabled) {
                    background-color: #e6f7ff;
                    color: var(--color);
                }

                &:active:not(:disabled) {
                    background-color: #bae7ff;
                }

                @media (max-width: 480px) {
                    width: 32px;
                    height: 32px;
                    font-size: 16px;
                }
            }

            .minNumber-input {
                width: 70px;
                height: 36px;
                text-align: center;
                border: none;
                border-left: 1px solid #e8e8e8;
                border-right: 1px solid #e8e8e8;
                outline: none;
                font-size: 14px;
                background-color: #fff;
                color: #333;
                transition: border-color 0.3s ease;

                &:focus {
                    border-color: var(--color);
                }

                &::-webkit-inner-spin-button,
                &::-webkit-outer-spin-button {
                    -webkit-appearance: none;
                    appearance: none;
                    margin: 0;
                }

                &[type="number"] {
                    -moz-appearance: textfield;
                    appearance: textfield;
                }

                @media (max-width: 480px) {
                    width: 60px;
                    height: 32px;
                    font-size: 14px;
                }
            }
        }

        .stock-info-wrap {
            display: flex;
            flex-direction: column;
            gap: 4px;
        }

        .stock-info {
            font-size: 14px;
            color: #666;
            display: flex;
            align-items: center;
            gap: 4px;
            flex-wrap: wrap;

            &.limit-info {
                color: var(--color);
            }

            .minNumber-error {
                color: #f5222d;
                font-size: 12px;
            }
        }
    }

    .deposit-guarantee-tip {
        margin-top: 12px;
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 12px;
        border-radius: 6px;
        border-left: 3px solid var(--color);
        background: color-mix(in srgb, var(--color) 14%, #fff);
        color: #5c4a38;
        font-size: 13px;
        line-height: 1.5;

        .deposit-guarantee-tip__icon {
            flex-shrink: 0;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 22px;
            height: 22px;
            border-radius: 50%;
            background: var(--color);
            color: #fff;
            font-size: 12px;
            font-weight: 700;
            line-height: 1;
        }

        .deposit-guarantee-tip__text {
            flex: 1;
            font-weight: 500;
        }

        @media (max-width: 480px) {
            margin-top: 10px;
            padding: 8px 10px;
            font-size: 12px;
            gap: 8px;

            .deposit-guarantee-tip__icon {
                width: 20px;
                height: 20px;
                font-size: 11px;
            }
        }
    }

    .service-section {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid #f0f0f0;

        @media (max-width: 768px) {
            margin-top: 16px;
            padding-top: 12px;
            gap: 10px;
        }

        .label {
            font-size: 14px;
            color: #666;
            flex-shrink: 0;
            font-weight: 500;
            line-height: 32px;
        }

        .service-list {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            justify-content: flex-start;
            flex: 1;
            min-width: 0;

            @media (max-width: 768px) {
                gap: 6px;
            }

            .service-item {
                display: flex;
                align-items: center;
                gap: 4px;
                padding: 5px 6px;
                border: none;
                border-radius: 4px;
                background-color: #fff;
                cursor: pointer;
                transition: all 0.3s ease;

                &:hover {
                    background-color: rgba(82, 196, 26, 0.05);
                }

                .service-icon {
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    width: 20px;
                    height: 20px;
                    background-color: var(--color);
                    color: #fff;
                    border-radius: 50%;
                    font-size: 12px;
                    font-weight: bold;
                    flex-shrink: 0;

                    @media (max-width: 768px) {
                        width: 18px;
                        height: 18px;
                        font-size: 11px;
                    }
                }

                .service-name {
                    font-size: 12px;
                    color: #333;
                    white-space: nowrap;

                    @media (max-width: 768px) {
                        font-size: 11px;
                    }
                }
            }
        }
    }

    .purchase-section {
        display: flex;
        gap: 16px;
        justify-content: flex-start;
        margin-top: 20px;
        flex-wrap: wrap;

        @media (max-width: 768px) {
            flex-direction: column;
            gap: 12px;
        }

        .btn-add-cart,
        .btn-buy-now {
            flex: 0 0 180px;
            width: 180px;
            min-width: 180px;
            height: 48px;
            font-size: 16px;
            font-weight: 600;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            overflow: hidden;

            &:disabled {
                opacity: 0.6;
                cursor: not-allowed;
            }

            @media (max-width: 768px) {
                width: 100%;
                max-width: 180px;
                min-width: unset;
                flex: 0 0 auto;
                align-self: center;
            }

            @media (max-width: 480px) {
                height: 44px;
                font-size: 15px;
            }
        }

        .btn-add-cart {
            background-color: #fff;
            color: var(--color);
            border: 2px solid var(--color);

            &:hover:not(:disabled) {
                background-color: rgba(82, 196, 26, 0.05);
                border-color: var(--color);
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(82, 196, 26, 0.2);
            }

            &:active:not(:disabled) {
                transform: translateY(0);
            }

            &:disabled {
                border-color: #d9d9d9;
                color: #999;
            }
        }

        .btn-buy-now {
            background: linear-gradient(135deg, var(--color) 0%, var(--color) 100%);
            color: #fff;
            box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);

            &:hover:not(:disabled) {
                transform: translateY(-2px);
            }

            &:active:not(:disabled) {
                transform: translateY(0);
            }

            &:disabled {
                background: #d9d9d9;
                box-shadow: none;
            }
        }

    }
}

/* 分享/技术弹窗：宽度由 PopupModal width 传参控制，仅保留内容区样式 */
.technical-qrcode-img {
    width: 200px;
    height: 200px;
    display: block;
    margin: 0 auto 16px;
}

.technical-phone {
    font-size: 14px;
    color: #333;
}

.share-options {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    justify-content: center;
}

.share-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 24px;
    min-width: 72px;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.2s;
    border: 1px solid #f0f0f0;

    &:hover {
        background-color: #fafafa;
        border-color: #e0e0e0;
    }

    .share-icon {
        font-size: 28px;
        line-height: 1;
    }

    .share-label {
        font-size: 12px;
        color: #666;
    }
}

/* 阶梯价：只读展示，风格贴合价格区 */
.tier-price {
    margin-top: 10px;
    padding: 10px 12px;
    background: #fff7f0;
    border: 1px solid #ffd8bf;
    border-radius: 6px;

    &__head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 6px;
    }

    &__title {
        font-size: 13px;
        font-weight: 600;
        color: #ff5000;
    }

    &__tip {
        font-size: 12px;
        color: #bb8b6f;
    }

    &__row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 12px;
        color: #8a6a55;
        line-height: 1.9;
    }

    &__value {
        color: #ff5000;
        font-weight: 600;
    }
}

/* 到货通知：缺货时的订阅入口 */
.restock-box {
    margin: 12px 0;
    padding: 12px 14px;
    background: #f7f9ff;
    border: 1px solid #d6e2ff;
    border-radius: 6px;

    &__tip {
        font-size: 13px;
        color: #4a6fa5;
        margin-bottom: 8px;
    }

    &__form {
        display: flex;
        gap: 10px;
    }

    &__input {
        flex: 1;
        height: 36px;
        padding: 0 12px;
        border: 1px solid #d9e2f2;
        border-radius: 6px;
        font-size: 14px;
        outline: none;

        &:focus {
            border-color: #4a6fa5;
        }
    }
}

.btn-restock {
    height: 36px;
    padding: 0 18px;
    border: none;
    border-radius: 6px;
    background: #4a6fa5;
    color: #fff;
    font-size: 14px;
    cursor: pointer;

    &:disabled {
        background: #a9bcd8;
        cursor: not-allowed;
    }
}

/* 砍价入口 */
.bargain-entry {
    margin: 10px 0 4px;

    &__link {
        font-size: 13px;
        color: #ff5000;
        text-decoration: none;

        &:hover {
            text-decoration: underline;
        }
    }
}
</style>
