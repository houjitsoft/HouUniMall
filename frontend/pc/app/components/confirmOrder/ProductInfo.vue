<template>
    <div class="product-section">
        <div class="section-header">
            <span class="section-title">{{ $t('确认商品信息') }}</span>
        </div>

        <!-- 多店铺商品信息渲染 -->
        <div class="store-list" v-if="shopList && shopList.length > 0">
            <div v-for="(store, storeIndex) in shopList" :key="storeIndex" class="store-item">
                <div class="store-info">
                    <span class="store-name">🛒 {{ $t(store.shopName || '') }}</span>
                </div>
                <div class="product-table">
                    <div class="table-header">
                        <div class="table-cell">{{ $t('商品') }}</div>
                        <div class="table-cell">{{ $t('单价') }}</div>
                        <div class="table-cell">{{ $t('数量') }}</div>
                        <div class="table-cell">{{ $t('小计') }}</div>
                    </div>
                    <div class="table-content">
                        <div class="product-item" v-for="(goods, goodsIndex) in store.goodsList" :key="goodsIndex"
                            :class="{ 'no-divider': shouldHideRowDivider(store, goodsIndex) }">
                            <div class="table-cell product-info">
                                <img :src="goods.goodsImage || 'https://via.placeholder.com/80x80'" alt="product"
                                    class="product-image">
                                <div class="product-details">
                                    <div class="product-name-wrapper">
                                        <!-- <span class="activity-badge" v-if="getActivityLabel(goods)"
                                            :class="getActivityClass(goods)">
                                            {{ getActivityLabel(goods) }}
                                        </span> -->
                                        <div class="product-name">{{ $t(goods.goodsName || '') }}</div>
                                    </div>
                                    <div v-if="goods.goodsCode" class="product-order">{{ $t('订货编号') }}：{{
                                        goods.goodsCode }}</div>
                                    <div class="product-spec">{{ $t('规格') }}：{{ goods.goodsSpecifications || '' }}</div>
                                    <div v-if="isFixedCombinationGoods(goods)" class="product-spec">
                                        {{ $t('组合内数量') }}：{{ goods.goodsNumber }}
                                    </div>
                                </div>
                            </div>
                            <div v-if="!isFixedCombinationGoods(goods) || shouldRenderMergedCell(store, goodsIndex)"
                                class="table-cell price-info"
                                :class="{ 'merged-even-center': isEvenFixedCombinationDisplayRow(store, goodsIndex) }">
                                <span class="activity-badge price-activity-badge"
                                    v-if="getPriceDataActivityLabel(store, goods)"
                                    :class="getPriceDataActivityClass(store, goods)">
                                    {{ $t(getPriceDataActivityLabel(store, goods) || '') }}
                                </span>
                                <div class="product-price">{{ formatPriceWithSymbol(getDisplaySinglePrice(store, goods)) }}
                                </div>
                            </div>
                            <div v-else class="table-cell price-info merged-placeholder"></div>

                            <div v-if="!isFixedCombinationGoods(goods) || shouldRenderMergedCell(store, goodsIndex)"
                                class="table-cell quantity"
                                :class="{ 'merged-even-center': isEvenFixedCombinationDisplayRow(store, goodsIndex) }">
                                {{ getDisplayQuantity(goods) }}
                            </div>
                            <div v-else class="table-cell quantity merged-placeholder"></div>

                            <div v-if="!isFixedCombinationGoods(goods) || shouldRenderMergedCell(store, goodsIndex)"
                                class="table-cell subtotal"
                                :class="{ 'merged-even-center': isEvenFixedCombinationDisplayRow(store, goodsIndex) }">
                                <div>{{ formatPriceWithSymbol(getDisplaySubTotal(store, goods)) }}</div>
                                <div v-if="isFixedCombinationGoods(goods) && getCombinationSavedPrice(store) > 0"
                                    class="combination-saved-price">
                                    {{ $t('已为您节省') }}: {{ formatPriceWithSymbol(getCombinationSavedPrice(store)) }}
                                </div>
                            </div>
                            <div v-else class="table-cell subtotal merged-placeholder"></div>
                        </div>
                    </div>
                </div>
                <!-- 赠品 -->
                <div v-if="getStoreGiveawayList(store).length > 0" class="giveaway-section">
                    <div class="giveaway-label">{{ $t('赠品') }}</div>
                    <div class="giveaway-list">
                        <div v-for="(item, idx) in getStoreGiveawayList(store)" :key="idx" class="giveaway-item">
                            <img :src="item.giveawayImage || 'https://via.placeholder.com/60x60'"
                                :alt="item.giveawayName" class="giveaway-image" />
                            <span class="giveaway-name">{{ $t(item.giveawayName || '') }}</span>
                        </div>
                    </div>
                </div>
                <div class="order-info">
                    <div class="order-info-left">
                        <div class="remark-section">
                            <span class="remark-label">{{ $t('备注') }}:</span>
                            <textarea class="remark-input" :placeholder="$t('限45字(定制类商品，请将购买需求在备注中做详细说明') || ''"
                                v-model="localRemark"></textarea>
                        </div>
                        <div class="tips-section">
                            <span class="tips-text">{{ $t('提示') }}: {{ $t('请填写有关支付、收货、发票方面的信息') }}</span>
                        </div>
                    </div>
                    <div class="order-info-right">
                        <div class="summary-section">
                            <div class="summary-row">
                                <span class="summary-label">{{ $t('税费') }}:</span>
                                <span class="summary-value">{{ formatPriceWithSymbol(storePriceInfoMap[storeIndex]?.tax
                                    || 0) }}</span>
                            </div>
                            <div class="summary-row">
                                <span class="summary-label">{{ $t('总额') }}:</span>
                                <span class="summary-value">{{
                                    formatPriceWithSymbol(storePriceInfoMap[storeIndex]?.totalAmount ||
                                        store.shopTotalPrice) }}</span>
                            </div>
                            <!-- 满减赠 -->
                            <div class="summary-row"
                                v-if="(storePriceInfoMap[storeIndex]?.shopManjianDiscount ?? 0) > 0">
                                <span class="summary-label">{{ $t('满减赠') }}:</span>
                                <span class="summary-value">{{
                                    formatPriceWithSymbol(storePriceInfoMap[storeIndex]?.shopManjianDiscount ?? 0)
                                }}</span>
                            </div>
                            <div class="summary-row">
                                <span class="summary-label">{{ $t('运费') }}:</span>
                                <span class="summary-value">{{
                                    formatPriceWithSymbol(storePriceInfoMap[storeIndex]?.shippingFee || 0) }}</span>
                            </div>
                            <!-- 满减折 -->
                            <div class="summary-row"
                                v-if="(storePriceInfoMap[storeIndex]?.shopManzheDiscount ?? 0) > 0">
                                <span class="summary-label">{{ $t('满减折') }}:</span>
                                <span class="summary-value">{{
                                    formatPriceWithSymbol(storePriceInfoMap[storeIndex]?.shopManzheDiscount ?? 0)
                                }}</span>
                            </div>
                            <div class="summary-row">
                                <span class="summary-label">{{ $t('本店合计') }}:</span>
                                <span class="summary-value">{{
                                    formatPriceWithSymbol(storePriceInfoMap[storeIndex]?.totalWithShipping ||
                                        store.shopTotalPrice) }}</span>
                            </div>
                        </div>
                        <div class="invoice-section" v-if="store.invoiceActive === 1">
                            <span class="invoice-label"
                                :class="{ 'is-clickable': storeInvoiceMap[storeIndex]?.invoiceHead }"
                                @click="handleInvoiceLabelClick(storeIndex)">{{ getInvoiceLabel(storeIndex) }}</span>
                            <span class="invoice-edit" @click="handleInvoiceClick(storeIndex)">{{
                                getInvoiceEditText(storeIndex) }}</span>
                        </div>
                        <div class="promotion-section" v-if="shouldShowCouponForStore(store)"
                            @click="handleCouponClick(storeIndex)">
                            <span class="promotion-label" v-if="(getStoreCouponPrice(store) ?? 0) === 0">{{ $t('选择优惠券')
                            }}</span>
                            <span class="promotion-label" v-else>{{ $t('您选择了') }}{{
                                formatPriceWithSymbol(getStoreCouponPrice(store) ?? 0) }}{{ $t('的优惠券') }}</span>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 发票弹窗 -->
            <InvoiceModal v-model="showInvoiceModal" :initial-value="storeInvoiceMap[currentInvoiceStoreIndex] ?? null"
                @confirm="handleInvoiceConfirm" />

            <!-- 优惠券选择弹窗 -->
            <CouponSelectModal v-model="showCouponModal" :shop-id="currentCouponStore ? currentCouponStore.shopId : 0"
                :goods-info="couponGoodsInfo" :initial-voucher-id="getCurrentStoreInitialVoucherId()"
                @confirm="handleCouponConfirm" />
        </div>

        <!-- 空状态提示 -->
        <EmptyState v-else :text="$t('暂无商品信息')" :description="$t('请先添加商品到购物车')" />
    </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, reactive } from 'vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import InvoiceModal from './InvoiceModal.vue'
import type { ShopList, GoodsList } from '~/types/goods'
import type { ShippingListDTO, InvoiceInfo } from '~/types/business'

/** 各店铺已选优惠券（shopId -> { voucherId, couponPrice }），用于多店铺时仅更新当前店铺、不影响其他店铺 */
const props = withDefaults(defineProps<{
    shopList?: ShopList[]
    priceData?: { shippingList?: ShippingListDTO[] } | null
    couponMap?: Record<number, { voucherId: string; couponPrice?: number }>
    remark?: string
}>(), {
    shopList: () => [],
    priceData: null,
    couponMap: () => ({}),
    remark: ''
})

const emits = defineEmits(['update:remark', 'update:invoiceInfo', 'couponConfirm'])

const localRemark = ref(props.remark)

// 发票相关：每个店铺的发票信息
const storeInvoiceMap = reactive<Record<number, InvoiceInfo | null>>({})
const showInvoiceModal = ref(false)
const currentInvoiceStoreIndex = ref(0)

// 优惠券相关
const showCouponModal = ref(false)
const currentCouponStoreIndex = ref(0)
const currentCouponStore = computed(() => props.shopList?.[currentCouponStoreIndex.value])
const couponGoodsInfo = computed(() => {
    const store = currentCouponStore.value
    if (!store?.goodsList?.length) return []
    return store.goodsList.map((g: GoodsList) => ({
        goodsId: g.goodsId,
        goodsPrice: String(getSinglePrice(store, g)),
        goodsNum: g.goodsNumber
    }))
})

const currentStoreShippingInfo = computed(() => {
    const store = currentCouponStore.value
    if (!store) return null
    return props.priceData?.shippingList?.find((s: { shopId?: number }) => s.shopId === store.shopId)
})

const invoiceTypeLabels: Record<number, string> = {
    0: '普通发票',
    1: '增值税专用发票'
}

const getInvoiceLabel = (storeIndex: number): string => {
    const info = storeInvoiceMap[storeIndex]
    if (!info?.invoiceHead) return '不开发票'
    const typeLabel = invoiceTypeLabels[info.invoiceType ?? 0] ?? '发票'
    return `${typeLabel} ${info.invoiceHead}`
}

const getInvoiceEditText = (storeIndex: number): string => {
    return storeInvoiceMap[storeIndex]?.invoiceHead ? '取消' : '开票'
}

const handleInvoiceClick = (storeIndex: number) => {
    const hasInvoice = !!storeInvoiceMap[storeIndex]?.invoiceHead
    if (hasInvoice) {
        storeInvoiceMap[storeIndex] = null
    } else {
        currentInvoiceStoreIndex.value = storeIndex
        showInvoiceModal.value = true
    }
}

const handleCouponClick = (storeIndex: number) => {
    currentCouponStoreIndex.value = storeIndex
    showCouponModal.value = true
}

const handleCouponConfirm = (payload: { voucherId: string | null; voucherPrice?: number; voucherTitle?: string }) => {
    const store = currentCouponStore.value
    if (!store) return
    emits('couponConfirm', {
        shopId: store.shopId,
        storeIndex: currentCouponStoreIndex.value,
        voucherId: payload.voucherId,
        couponPrice: payload.voucherPrice
    })
}

/** 已填写发票信息时，点击标签可编辑 */
const handleInvoiceLabelClick = (storeIndex: number) => {
    if (storeInvoiceMap[storeIndex]?.invoiceHead) {
        currentInvoiceStoreIndex.value = storeIndex
        showInvoiceModal.value = true
    }
}

const handleInvoiceConfirm = (info: InvoiceInfo) => {
    storeInvoiceMap[currentInvoiceStoreIndex.value] = info
    showInvoiceModal.value = false
}

// 供父组件获取各店铺发票信息（用于提交订单）
const getStoreInvoiceMap = () => ({ ...storeInvoiceMap })
defineExpose({ getStoreInvoiceMap })

watch(() => props.remark, (newVal) => {
    localRemark.value = newVal
})

watch(() => localRemark.value, (newVal) => {
    emits('update:remark', newVal)
})

// 根据 shopId 获取当前店铺的 shipping 信息
const getStoreShippingInfo = (store: ShopList) => {
    return props.priceData?.shippingList?.find((s: { shopId?: number }) => s.shopId === store.shopId)
}

/** 获取店铺优惠券金额（优先用 couponMap，避免某店取消优惠券后重算价格把其他店也清掉） */
const getStoreCouponPrice = (store: ShopList): number | undefined => {
    const fromMap = props.couponMap?.[store.shopId]?.couponPrice
    if (fromMap !== undefined && fromMap !== null) return fromMap
    return getStoreShippingInfo(store)?.couponPrice
}

/** 弹窗当前店铺的初始 voucherId（优先 couponMap，保证重算价格后仍显示已选） */
const getCurrentStoreInitialVoucherId = (): string | null => {
    const store = currentCouponStore.value
    if (!store) return null
    const fromMap = props.couponMap?.[store.shopId]?.voucherId
    if (fromMap != null && fromMap !== '') return fromMap
    return currentStoreShippingInfo.value?.voucherId ?? null
}

// 计算每个店铺的价格信息
const getStorePriceInfo = (store: ShopList) => {
    const shippingInfo = getStoreShippingInfo(store)
    if (!shippingInfo) {
        return null
    }
    return {
        tax: shippingInfo.shopTaxPrice || 0,
        totalAmount: shippingInfo.shopPrice || 0,
        shippingFee: shippingInfo.shipping || 0,
        totalWithShipping: shippingInfo.shopTotalPrice || 0,
        shopManjianDiscount: shippingInfo.shopManjianDiscount || 0,
        shopManzheDiscount: shippingInfo.shopManzheDiscount || 0
    }
}

// 为每个店铺创建计算属性
const storePriceInfoMap = computed(() => {
    return props.shopList.map((store) => getStorePriceInfo(store))
})

// 获取活动标识文字
const getActivityLabel = (goods: GoodsList): string => {
    const activityMap: Record<number, string> = {
        1: '会员价',
        4: '限时折扣',
        5: '限时秒杀',
        6: '满减赠',
        7: '代下单',
        8: '满减折',
        10: '组合商品'
    }
    return goods.commonIsXian ? activityMap[goods.commonIsXian] || '' : ''
}

// 获取活动标识样式类
const getActivityClass = (goods: GoodsList): string => {
    const classMap: Record<number, string> = {
        1: 'activity-member',        // 会员价
        4: 'activity-discount',      // 限时折扣
        5: 'activity-seckill',       // 限时秒杀
        6: 'activity-full-gift',     // 满减赠
        7: 'activity-proxy',         // 代下单
        8: 'activity-full-discount', // 满减折
        10: 'activity-combination'   // 组合商品
    }
    return goods.commonIsXian ? classMap[goods.commonIsXian] || '' : ''
}

// 活动类型 [4, 5, 6, 8, 10, 11] 时隐藏优惠券选择
const HIDE_COUPON_ACTIVITY_TYPES = [4, 5, 6, 8, 10, 11]

const shouldShowCouponForStore = (store: ShopList): boolean => {
    if (!store?.goodsList?.length) return true
    const hasHideActivity = store.goodsList.some((goods: GoodsList) => {
        const type = getActivityTypeFromPriceData(store, goods)
        return type !== undefined && HIDE_COUPON_ACTIVITY_TYPES.includes(type)
    })
    return !hasHideActivity
}

// 根据 priceData.stepOrHyList 获取活动类型
const getActivityTypeFromPriceData = (store: ShopList, goods: GoodsList): number | undefined => {
    const shippingList = props.priceData?.shippingList
    if (!shippingList?.length) return undefined
    const shopPriceData = shippingList.find((s: { shopId?: number }) => s.shopId === store.shopId)
    if (!shopPriceData?.goodsIdList || !shopPriceData?.stepOrHyList) return undefined
    const goodsIndex = shopPriceData.goodsIdList.indexOf(Number(goods.goodsId))
    if (goodsIndex === -1) return undefined
    const type = shopPriceData.stepOrHyList[goodsIndex]
    return type !== undefined ? type : undefined
}

// 根据 priceData 获取价格上方的活动标识文字
const getPriceDataActivityLabel = (store: ShopList, goods: GoodsList): string => {
    const activityMap: Record<number, string> = {
        1: '会员价',
        4: '限时折扣',
        5: '限时秒杀',
        6: '满减赠',
        7: '代下单',
        8: '满减折',
        10: '组合商品'
    }
    const type = getActivityTypeFromPriceData(store, goods)
    if (type) return activityMap[type] || ''

    // 组合商品兜底标签（未命中算价 stepOrHyList 时，仍按价格区活动样式展示）
    if (Number(goods.combinationId ?? 0) > 0) {
        const cType = Number(goods.combinationType ?? 0)
        if (cType === 1) return '固定组合'
        if (cType === 2) return '搭配组合'
        return '组合商品'
    }
    return ''
}

// 根据 priceData 获取价格上方的活动标识样式类
const getPriceDataActivityClass = (store: ShopList, goods: GoodsList): string => {
    const classMap: Record<number, string> = {
        1: 'activity-member',
        4: 'activity-discount',
        5: 'activity-seckill',
        6: 'activity-full-gift',
        7: 'activity-proxy',
        8: 'activity-full-discount',
        10: 'activity-combination'
    }
    const type = getActivityTypeFromPriceData(store, goods)
    if (type) return classMap[type] || ''
    if (Number(goods.combinationId ?? 0) > 0) return 'activity-combination'
    return ''
}

// 根据 priceData.hyPriceList 获取单价，无则回退 goods.singlePrice
const getSinglePrice = (store: ShopList, goods: GoodsList): number => {
    const shippingList = props.priceData?.shippingList
    if (!shippingList?.length) return goods.singlePrice
    const shopPriceData = shippingList.find((s: { shopId?: number }) => s.shopId === store.shopId)
    if (!shopPriceData?.goodsIdList || !shopPriceData?.hyPriceList) return goods.singlePrice
    const goodsIndex = shopPriceData.goodsIdList.indexOf(Number(goods.goodsId))
    if (goodsIndex === -1) return goods.singlePrice
    const price = shopPriceData.hyPriceList[goodsIndex]
    return price !== undefined ? price : goods.singlePrice
}

// 根据 priceData.subTotalList 获取小计，无则回退 goods.goodsTotal
const getSubTotal = (store: ShopList, goods: GoodsList): number => {
    const shippingList = props.priceData?.shippingList
    if (!shippingList?.length) return goods.goodsTotal
    const shopPriceData = shippingList.find((s: { shopId?: number }) => s.shopId === store.shopId)
    if (!shopPriceData?.goodsIdList || !shopPriceData?.subTotalList) return goods.goodsTotal
    const goodsIndex = shopPriceData.goodsIdList.indexOf(Number(goods.goodsId))
    if (goodsIndex === -1) return goods.goodsTotal
    const total = shopPriceData.subTotalList[goodsIndex]
    return total !== undefined ? total : goods.goodsTotal
}

/** 是否固定组合商品 */
const isFixedCombinationGoods = (goods: GoodsList): boolean => {
    return Number(goods.combinationType ?? 0) === 1 && Number(goods.combinationId ?? 0) > 0
}

/** 固定组合分组信息（同组合ID） */
const getFixedCombinationGroupInfo = (store: ShopList, goodsIndex: number): { first: number; last: number; mid: number } | null => {
    const goods = store.goodsList[goodsIndex]
    if (!goods || !isFixedCombinationGoods(goods)) return null
    const cid = Number(goods.combinationId ?? 0)
    const indices = store.goodsList
        .map((g, i) => (Number(g.combinationId ?? 0) === cid ? i : -1))
        .filter((i) => i >= 0)
    if (!indices.length) return null
    const first = indices[0]!
    const last = indices[indices.length - 1]!
    const mid = indices[Math.floor((indices.length - 1) / 2)]!
    return { first, last, mid }
}

/** 固定组合总价（优先取店铺算价结果，其次按组合内子项小计汇总） */
const getFixedCombinationTotal = (store: ShopList, goods: GoodsList): number => {
    if (!isFixedCombinationGoods(goods)) return getSubTotal(store, goods)
    const shippingInfo = props.priceData?.shippingList?.find((s: { shopId?: number }) => s.shopId === store.shopId)
    if (shippingInfo?.shopTotalPrice != null) return Number(shippingInfo.shopTotalPrice) || 0
    const combinationId = Number(goods.combinationId ?? 0)
    return store.goodsList
        .filter((g) => Number(g.combinationId ?? 0) === combinationId)
        .reduce((sum, g) => sum + getSubTotal(store, g), 0)
}

/** 展示数量：固定组合显示组合数量，否则显示商品数量 */
const getDisplayQuantity = (goods: GoodsList): number => {
    if (!isFixedCombinationGoods(goods)) return goods.goodsNumber
    const n = Number(goods.combinationNum ?? goods.combinationGoodsNum ?? 0)
    return n > 0 ? n : goods.goodsNumber
}

/** 展示单价：固定组合显示组合单价（组合总价 / 组合数量） */
const getDisplaySinglePrice = (store: ShopList, goods: GoodsList): number => {
    if (!isFixedCombinationGoods(goods)) return getSinglePrice(store, goods)
    const qty = getDisplayQuantity(goods)
    if (qty <= 0) return 0
    return getFixedCombinationTotal(store, goods) / qty
}

/** 展示小计：固定组合显示组合总价 */
const getDisplaySubTotal = (store: ShopList, goods: GoodsList): number => {
    if (!isFixedCombinationGoods(goods)) return getSubTotal(store, goods)
    return getFixedCombinationTotal(store, goods)
}

/** 固定组合仅在中间行显示金额/数量/小计 */
const shouldRenderMergedCell = (store: ShopList, goodsIndex: number): boolean => {
    const info = getFixedCombinationGroupInfo(store, goodsIndex)
    if (!info) return true
    return goodsIndex === info.mid
}

/** 固定组合分组内隐藏中间分隔线 */
const shouldHideRowDivider = (store: ShopList, goodsIndex: number): boolean => {
    const info = getFixedCombinationGroupInfo(store, goodsIndex)
    if (!info) return false
    return goodsIndex >= info.first && goodsIndex < info.last
}

/** 固定组合中间行对齐辅助 */
const isEvenFixedCombinationDisplayRow = (store: ShopList, goodsIndex: number): boolean => {
    const info = getFixedCombinationGroupInfo(store, goodsIndex)
    if (!info) return false
    return goodsIndex === info.mid && ((info.last - info.first + 1) % 2 === 0)
}

/** 固定组合节省金额（组合原价总和 - 组合总价） */
const getCombinationSavedPrice = (store: ShopList): number => {
    const fixedGoods = store.goodsList.find((g) => isFixedCombinationGoods(g))
    if (!fixedGoods) return 0
    const comboTotal = getFixedCombinationTotal(store, fixedGoods)
    const originalTotal = store.goodsList.reduce((sum, g) => sum + Number(g.goodsTotal || 0), 0)
    return Math.max(0, originalTotal - comboTotal)
}

// 获取当前店铺的赠品列表（来自 priceData.shippingList[shopId].shopGiveawayList）
const getStoreGiveawayList = (store: ShopList) => {
    const shippingList = props.priceData?.shippingList
    if (!shippingList?.length) return []
    const shopPriceData = shippingList.find((s: { shopId?: number }) => s.shopId === store.shopId)
    const list = shopPriceData?.shopGiveawayList
    return Array.isArray(list) ? list : []
}
</script>

<style lang="scss" scoped>
.product-section {
    background-color: #fff;
    border-radius: 8px;
    margin-bottom: 30px;

    .section-header {
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 2px solid #f0f0f0;

        .section-title {
            font-size: 18px;
            font-weight: 700;
            color: #262626;
            letter-spacing: 0.5px;
        }
    }

    .store-list {
        .store-item {
            margin-bottom: 30px;
            border: 1px solid #e8e8e8;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
            transition: box-shadow 0.3s ease;

            &:hover {
                box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
            }

            &:last-child {
                margin-bottom: 0;
            }
        }
    }

    .store-info {
        background: linear-gradient(135deg, #f8f9fa 0%, #f0f2f5 100%);
        padding: 14px 20px;
        border-radius: 8px;
        margin-bottom: 8px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
        transition: all 0.2s ease;

        &:hover {
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        }

        .store-name {
            font-size: 15px;
            color: #333;
            font-weight: 600;
            letter-spacing: 0.3px;
        }
    }

    .product-table {
        border: 1px solid #e8e8e8;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

        .table-header {
            display: flex;
            background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
            padding: 16px 20px;
            font-weight: 600;
            font-size: 14px;
            color: #333;
            border-bottom: 2px solid #e8e8e8;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);

            .table-cell {
                flex: 1;
                text-align: center;

                &:first-child {
                    flex: 2;
                    text-align: left;
                }
            }
        }

        .table-content {
            .product-item {
                display: flex;
                padding: 20px;
                border-bottom: 1px solid #f0f0f0;
                transition: all 0.3s ease;
                background-color: #fff;

                &.no-divider {
                    border-bottom-color: transparent;
                }

                &:last-child {
                    border-bottom: none;
                }

                &:hover {
                    background-color: #fafbfc;
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
                }

                .table-cell {
                    flex: 1;
                    text-align: center;
                    display: flex;
                    align-items: center;
                    justify-content: center;

                    &.product-info {
                        flex: 2;
                        justify-content: flex-start;
                        align-items: flex-start;
                        text-align: left;
                        min-width: 0;

                        .product-image {
                            width: 90px;
                            height: 90px;
                            margin-right: 16px;
                            object-fit: contain;
                            border: 1px solid #e8e8e8;
                            border-radius: 8px;
                            background-color: #fafafa;
                            transition: all 0.3s ease;
                            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

                            &:hover {
                                transform: scale(1.05);
                                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                            }
                        }

                        .product-details {
                            flex: 1;

                            .product-name-wrapper {
                                display: flex;
                                align-items: flex-start;
                                gap: 8px;
                                margin-bottom: 8px;
                                flex-wrap: wrap;

                                .product-name {
                                    font-size: 15px;
                                    color: #262626;
                                    flex: 1;
                                    min-width: 0;
                                    font-weight: 500;
                                    line-height: 1.5;
                                    word-break: break-word;
                                }

                                .activity-badge {
                                    display: inline-block;
                                    padding: 4px 10px;
                                    border-radius: 6px;
                                    font-size: 11px;
                                    font-weight: 600;
                                    white-space: nowrap;
                                    flex-shrink: 0;
                                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
                                    letter-spacing: 0.3px;
                                    align-self: flex-start;
                                    margin-top: 2px;

                                    &.activity-member {
                                        background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
                                        color: #fff;
                                    }

                                    &.activity-discount {
                                        background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
                                        color: #fff;
                                    }

                                    &.activity-seckill {
                                        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
                                        color: #fff;
                                    }

                                    &.activity-full-gift {
                                        background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
                                        color: #fff;
                                    }

                                    &.activity-proxy {
                                        background: linear-gradient(135deg, #eb2f96 0%, #f759ab 100%);
                                        color: #fff;
                                    }

                                    &.activity-full-discount {
                                        background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
                                        color: #fff;
                                    }

                                    &.activity-combination {
                                        background: linear-gradient(135deg, #2f54eb 0%, #597ef7 100%);
                                        color: #fff;
                                    }
                                }
                            }

                            .product-order,
                            .product-spec {
                                font-size: 12px;
                                color: #8c8c8c;
                                margin-bottom: 6px;
                                line-height: 1.6;
                            }

                            .product-combo-inner-qty {
                                font-size: 12px;
                                color: #597ef7;
                                line-height: 1.5;
                            }
                        }
                    }

                    &.price-info {
                        flex-direction: column;
                        justify-content: center;
                        align-items: center;
                        gap: 6px;

                        .activity-badge {
                            display: inline-block;
                            padding: 4px 10px;
                            border-radius: 6px;
                            font-size: 11px;
                            font-weight: 600;
                            white-space: nowrap;
                            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
                            letter-spacing: 0.3px;

                            &.activity-member {
                                background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
                                color: #fff;
                            }

                            &.activity-discount {
                                background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
                                color: #fff;
                            }

                            &.activity-seckill {
                                background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
                                color: #fff;
                            }

                            &.activity-full-gift {
                                background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
                                color: #fff;
                            }

                            &.activity-proxy {
                                background: linear-gradient(135deg, #eb2f96 0%, #f759ab 100%);
                                color: #fff;
                            }

                            &.activity-full-discount {
                                background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
                                color: #fff;
                            }

                            &.activity-combination {
                                background: linear-gradient(135deg, #2f54eb 0%, #597ef7 100%);
                                color: #fff;
                            }
                        }

                        .product-price {
                            font-size: 16px;
                            color: #262626;
                            font-weight: 700;
                            font-family: 'Arial', sans-serif;
                            letter-spacing: 0.3px;
                        }

                    }

                    &.quantity {
                        font-size: 15px;
                        color: #333;
                        font-weight: 500;
                    }

                    &.subtotal {
                        font-size: 17px;
                        color: var(--color);
                        font-weight: 700;
                        font-family: 'Arial', sans-serif;
                        letter-spacing: 0.3px;

                        .combination-saved-price {
                            margin-top: 4px;
                            font-size: 12px;
                            line-height: 1.4;
                            color: #fa541c;
                            font-weight: 500;
                        }
                    }

                    &.merged-placeholder {
                        color: #bfbfbf;
                        font-weight: 400;
                    }

                    &.merged-even-center {
                        align-self: center;
                    }
                }
            }
        }
    }

    .giveaway-section {
        margin-top: 12px;
        padding: 12px 16px;
        background-color: #fffbf0;
        border: 1px solid #ffe58f;
        border-radius: 8px;

        .giveaway-label {
            font-size: 14px;
            font-weight: 600;
            color: #333;
            margin-bottom: 10px;
        }

        .giveaway-list {
            display: flex;
            flex-wrap: wrap;
            gap: 16px;
        }

        .giveaway-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 8px;
            min-width: 0;
            max-width: 120px;

            .giveaway-image {
                width: 60px;
                height: 60px;
                object-fit: contain;
                border: 1px solid #e8e8e8;
                border-radius: 6px;
                background-color: #fff;
                flex-shrink: 0;
            }

            .giveaway-name {
                font-size: 13px;
                color: #333;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                width: 100%;
                text-align: center;
            }
        }
    }

    .order-info {
        margin-top: 20px;
        display: flex;
        gap: 20px;

        .order-info-left,
        .order-info-right {
            width: 50%;
            background-color: #f8f9fa;
            border-radius: 4px;
            padding: 20px;
            border: 1px solid #e8e8e8;
        }

        .remark-section {
            display: flex;
            align-items: flex-start;
            margin-bottom: 16px;

            .remark-label {
                font-size: 14px;
                color: #333;
                margin-right: 8px;
                margin-top: 4px;
                font-weight: 500;
            }

            .remark-input {
                flex: 1;
                min-height: 80px;
                padding: 12px;
                border: 1px solid #e8e8e8;
                border-radius: 4px;
                font-size: 14px;
                color: #333;
                resize: none;
                background-color: #fff;

                &::placeholder {
                    color: #999;
                }
            }
        }

        .invoice-section,
        .promotion-section {
            display: flex;
            justify-content: flex-end;
            margin-top: 16px;

            .invoice-label {
                font-size: 14px;
                color: #333;
                font-weight: 500;

                &.is-clickable {
                    cursor: pointer;

                    &:hover {
                        color: var(--color);
                    }
                }
            }

            .promotion-label {
                font-size: 14px;
                color: #333;
                font-weight: 500;
                cursor: pointer;

                &:hover {
                    color: var(--color);
                }
            }

            .invoice-edit {
                font-size: 14px;
                color: var(--color);
                margin-left: 12px;
                cursor: pointer;

                &:hover {
                    text-decoration: underline;
                }
            }
        }

        .promotion-section {
            cursor: pointer;
        }

        .tips-section {
            .tips-text {
                font-size: 12px;
                color: var(--color);
                line-height: 1.4;
            }
        }

        .summary-section {
            background-color: #fff;
            border-radius: 4px;
            padding: 16px;
            border: 1px solid #e8e8e8;
            display: flex;
            flex-wrap: wrap;
            gap: 16px;

            .summary-row {
                display: flex;
                align-items: center;
                width: calc(50% - 8px);
                margin-bottom: 0;

                .summary-label {
                    font-size: 14px;
                    color: #666;
                    width: 80px;
                    text-align: right;
                    margin-right: 12px;
                    flex-shrink: 0;
                }

                .summary-value {
                    font-size: 14px;
                    color: #333;
                    font-weight: 500;
                    flex: 1;
                    text-align: left;
                }
            }
        }
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .order-info {
        flex-direction: column;
        gap: 20px;

        .order-info-left,
        .order-info-right {
            width: 100%;
            padding: 16px;
        }
    }
}
</style>