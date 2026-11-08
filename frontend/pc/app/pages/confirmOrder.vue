<template>
    <div class="confirm-order-page">

        <!-- 支付方式提示语 -->
        <div v-if="showAccountTip" class="payment-tip payment-tip--account">
            <UIcon name="i-lucide-info" class="tip-icon" aria-hidden="true" />
            <span class="tip-text">{{ $t('温馨提示：多店铺下单不可使用账期支付，请留意付款方式，如需使用账期支付请分开下单') }}</span>
        </div>

        <!-- 地址选择组件：virtualGoods==0 时隐藏 -->
        <AddressSelect v-if="!isVirtualGoodsOrder" :valet-order-id="valetOrderIdFromUrl"
            @addressChange="handleAddressChange" />

        <!-- 支付方式模块：virtualGoods==0 时隐藏 -->
        <div v-if="!isVirtualGoodsOrder" class="payment-section">
            <div class="section-header">
                <span class="section-title">{{ $t('支付方式') }}</span>
            </div>
            <div class="payment-list">
                <div v-for="(payment, index) in state.paymentMethods" :key="index" v-show="getPaymentVisible(index)"
                    class="payment-item" :class="{ active: state.selectedPaymentIndex === index }"
                    @click="selectPayment(index)">
                    <span class="payment-name">{{ $t(payment.name || '') }}</span>
                    <span v-if="state.selectedPaymentIndex === index" class="payment-tag" aria-hidden="true">
                        <UIcon name="i-lucide-check" />
                    </span>
                </div>
            </div>
        </div>

        <!-- 商品信息组件（含发票弹窗） -->
        <ProductInfo ref="productInfoRef" :shop-list="state.shopList" :price-data="priceResult" :coupon-map="couponMap"
            :remark="state.remark" @update:remark="(remark) => state.remark = remark"
            @coupon-confirm="handleCouponConfirm" />
        <!-- 红包选择组件 -->
        <RedPacketSelectModal v-model="showRedPacketModal" :shop-list="redPacketShopListPayload"
            :total-price="displayPaymentTotal" :initial-red-packet-id="selectedRedPacketId"
            @confirm="handleRedPacketConfirm" />
        <!-- 订单支付区域 -->
        <div class="order-payment-section">
            <div class="discount-line redpacket-line" @click="showRedPacketModal = true">
                <span class="discount-label">{{ $t('选择红包') }}：</span>
                <span class="discount-value" v-if="selectedRedPacketPrice > 0">-{{
                    formatPriceWithSymbol(selectedRedPacketPrice) }}</span>
                <span class="discount-value" v-else>{{ $t('不使用红包') }}</span>
            </div>
            <div v-if="showPlusEntry && plusDiscountAmount > 0" class="discount-line">
                <span class="discount-label">{{ $t('Plus折扣') }}：</span>
                <span class="discount-value">-{{ formatPriceWithSymbol(plusDiscountAmount) }}</span>
            </div>
            <div v-if="memberDiscountAmount > 0" class="discount-line">
                <span class="discount-label">{{ $t('会员折扣') }}：</span>
                <span class="discount-value">-{{ formatPriceWithSymbol(memberDiscountAmount) }}</span>
            </div>
            <!-- 国补抵扣：单列展示，金额已由宿主在 totalPrice 中扣除，此处不再重复减 -->
            <div v-if="subsidyDiscountAmount > 0" class="discount-line subsidy-line">
                <span class="discount-label">{{ $t('国补抵扣') }}：</span>
                <span class="discount-value">-{{ formatPriceWithSymbol(subsidyDiscountAmount) }}</span>
                <span class="subsidy-rate" v-if="subsidyRateText">{{ subsidyRateText }}</span>
            </div>
            <div class="payment-amount">
                <span class="amount-label">{{ $t('支付金额') }}：</span>
                <span class="amount-value">{{ formatPriceWithSymbol(displayPaymentTotal) }}</span>
            </div>
            <div class="payment-actions">
                <UButton class="back-cart-btn" color="neutral" variant="outline" size="lg"
                    icon="i-lucide-arrow-left" @click="handleBackCart">
                    {{ $t('返回购物车') }}
                </UButton>
                <UButton class="submit-order-btn" color="primary" variant="solid" size="lg"
                    trailing-icon="i-lucide-arrow-right" :loading="isSubmitting"
                    :disabled="isLoadingPrice" @click="handleSubmitOrder">
                    {{ isSubmitting ? $t('提交中...') : $t('提交订单') }}
                </UButton>
            </div>
        </div>

        <!-- 无货商品弹窗 -->
        <OutOfStockModal :visible="showOutOfStockModal" :out-of-stock-list="outOfStockList"
            @cancel="handleOutOfStockCancel" @confirm="handleOutOfStockConfirm" />
    </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useGoodsStore } from '~/stores/modules/goods'
import type { ShopList, GoodsList } from '~/stores/modules/goods'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import { Toast } from '~/composables/useToast'
import { usePlus, refreshPlusState } from '~/composables/usePlus'
import { type UserAddress } from '~/types/user'
import { calculatePrice } from '~/services/order.service'
import { fetchSubsidyQuote } from '~/services/subsidy.service'
import {
    rateText,
    round2,
    shouldShowSubsidy,
    toAmount,
    verifyPayable,
    type SubsidyQuote
} from '~/utils/subsidy'
import { queryIsDelivery } from '~/services/business.service'
import { createOrders } from '~/services/order.service'
import { queryPersonalDiscount } from '~/services/userms.service'
import { useIsKeySwitch } from '~/composables/useIsKeySwitch'
import type { CalculatePriceParams, CreateOrdersParams, ShopDTO } from '~/types/business'
import OutOfStockModal from '~/components/confirmOrder/OutOfStockModal.vue'
import RedPacketSelectModal from '~/components/confirmOrder/RedPacketSelectModal.vue'
import { UCENTER_URL } from '~/utils/externalUrl'
import { getToken } from '~/utils/auth'
import { encryptCreateOrdersPayload } from '@utils/cryptoAes'
import { GOODS_OUT_OF_STOCK } from '~/utils/errorCode/bizCodes'
const route = useRoute()
const goodsStore = useGoodsStore()

// URL 携带的 quotationId（招采报价单ID）
const quotationIdFromUrl = computed(() => (route.query.quotationId as string) || '')
// URL 携带的 valetOrderId（代客下单ID）
const valetOrderIdFromUrl = computed(() => (route.query.valetOrderId as string) || '')
/** 是否从购物车进入确认订单页（URL 带 goodsCat，与创建订单参数 goodsCat 对应） */
const isFromCartCheckout = computed(() => {
    const v = route.query.goodsCat
    return v === '1' || v === 'true'
})
const { checkKeySwitch } = useIsKeySwitch()

// 支付方式显示控制：tongLian_switch==1 隐藏线下/货到付款；isDelivery==0 隐藏货到付款
const hideOfflinePayment = ref(false)
const hideCodPayment = ref(false)

const getPaymentVisible = (index: number) => {
    if (index === 0) return true // 在线支付始终显示
    if (index === 1) return !hideOfflinePayment.value // 对公转账
    if (index === 2) return !hideCodPayment.value // 货到付款
    return true
}

const state = reactive({
    selectedAddress: null as UserAddress | null,
    selectedPaymentIndex: 0,
    remark: '',
    shopList: [] as ShopList[],
    paymentMethods: [
        { name: '在线支付' },
        { name: '对公转账' },
        { name: '货到付款' }
    ]
})

// 价格计算结果
const priceResult = ref<ShopDTO | null>(null)
const isLoadingPrice = ref(false)
const isSubmitting = ref(false)

// 优惠券选择：shopId -> { voucherId, couponPrice }
const couponMap = reactive<Record<number, { voucherId: string; couponPrice?: number }>>({})
// 用户是否已确认过优惠券选择（含选择「不使用优惠券」）
const hasCouponConfirmed = ref(false)
const showRedPacketModal = ref(false)
const selectedRedPacketId = ref<number | null>(null)
const selectedRedPacketPrice = ref<number>(0)
const memberClassId = ref(0)

/** 从 queryPersonalDiscount 接口获取会员等级 ID，用于算价与下单 */
async function fetchMemberClassId(): Promise<number> {
    try {
        const res = await queryPersonalDiscount({ token: getToken() })
        if (res.code === 1 && res.data?.memberClassId != null) {
            memberClassId.value = Number(res.data.memberClassId) || 0
        }
    } catch {
        // 接口失败时保持默认值 0
    }
    return memberClassId.value
}

/** 用算价接口返回的 shippingList 同步 couponMap，保证初次进入时各店优惠券状态被记录，某店取消时不会影响其他店 */
function syncCouponMapFromPriceResult(data: ShopDTO) {
    const list = data?.shippingList
    if (!list || !Array.isArray(list)) return
    list.forEach((s: { shopId?: number; voucherId?: string; couponPrice?: number }) => {
        if (s.shopId == null) return
        const vid = s.voucherId != null && String(s.voucherId).trim() !== '' && String(s.voucherId) !== '0'
            ? String(s.voucherId)
            : null
        if (vid) {
            couponMap[s.shopId] = { voucherId: vid, couponPrice: s.couponPrice }
        } else {
            delete couponMap[s.shopId]
        }
    })
}
const productInfoRef = ref<{ getStoreInvoiceMap?: () => Record<number, import('~/types/business').InvoiceInfo | null> } | null>(null)

// 无货商品弹窗：code===15030 时显示
const showOutOfStockModal = ref(false)
const outOfStockList = ref<Array<{ goodsId: string; goodsName: string; goodsSpecifications?: string; goodsNumber: number; goodsImage?: string }>>([])
const outOfStockGoodsIds = ref<number[]>([])
const pendingRetryAction = ref<'price' | 'order' | null>(null)

// 账期提示语：在线支付时，且 priceResult.isAccount === 2 且多店铺时显示
const showAccountTip = computed(() =>
    state.selectedPaymentIndex === 0 &&
    priceResult.value?.isAccount === 2 &&
    (priceResult.value?.shippingList?.length ?? 0) > 0
)

// 虚拟商品订单：所有店铺 virtualGoods === 0 时隐藏地址、支付方式
const isVirtualGoodsOrder = computed(() => {
    const list = state.shopList
    if (!list || list.length === 0) return false
    return list.every((shop: ShopList) => shop.virtualGoods === 0)
})

/** 支付方式对应 paymentCode：在线=1，线下=2，货到付款=3 */
const paymentCode = computed(() => [1, 2, 3][state.selectedPaymentIndex] ?? 1)

// 计算总支付金额
const totalPaymentAmount = computed(() => {
    if (priceResult.value?.shippingList && priceResult.value.shippingList.length > 0) {
        return priceResult.value.shippingList.reduce((total: number, shipping: any) => {
            return total + (shipping.shopTotalPrice || 0)
        }, 0)
    }
    return state.shopList.reduce((total, shop) => total + shop.shopTotalPrice, 0)
})

const displayPaymentTotal = computed(() => {
    const totalPrice = priceResult.value?.totalPrice
    return totalPrice != null ? totalPrice : totalPaymentAmount.value
})
const plusDiscountAmount = computed(() => Number(priceResult.value?.plusgoodstotal || 0))
const memberDiscountAmount = computed(() => Number(priceResult.value?.memberDiscountAmount || 0))

// 国补抵扣：与券/满减/会员折扣并列的独立优惠行。
// 叠加顺序为「券 → 满减 → 国补」，任一优惠互斥时后端直接不命中国补，
// 因此这里只需展示后端已确认的金额，绝不在前端二次相减（会导致少收/多收）。
const subsidyQuote = ref<SubsidyQuote | null>(null)
const subsidyDiscountAmount = computed(() => {
    if (!shouldShowSubsidy(subsidyQuote.value)) return 0
    return round2(toAmount(subsidyQuote.value?.subsidyAmount))
})
const subsidyRateText = computed(() => rateText(subsidyQuote.value?.subsidyRate))
const { showPlusEntry } = usePlus()
const redPacketShopListPayload = computed(() => {
    return state.shopList.map((shop: ShopList) => {
        const shippingInfo = priceResult.value?.shippingList?.find((s: any) => s.shopId === shop.shopId)
        const coupon = couponMap[shop.shopId]
        return {
            shopId: shop.shopId,
            voucherTId: String(shippingInfo?.voucherId || coupon?.voucherId || ''),
            goodsList: shop.goodsList.map((goods: GoodsList) => ({
                goodsId: String(goods.goodsId),
                goodsPrice: getGoodsCurrentPrice(shop, goods),
                goodsNumber: goods.goodsNumber
            }))
        }
    })
})

const getGoodsCurrentPrice = (shop: ShopList, goods: GoodsList): number => {
    const shippingInfo = priceResult.value?.shippingList?.find((s: any) => s.shopId === shop.shopId)
    if (!shippingInfo?.goodsIdList || !shippingInfo?.hyPriceList) return Number(goods.singlePrice || 0)
    const idx = shippingInfo.goodsIdList.indexOf(Number(goods.goodsId))
    if (idx === -1) return Number(goods.singlePrice || 0)
    return Number(shippingInfo.hyPriceList[idx] ?? goods.singlePrice ?? 0)
}

const handleRedPacketConfirm = async (payload: { redPacketId: number | null; redPacketPrice?: number }) => {
    selectedRedPacketId.value = payload.redPacketId ?? null
    selectedRedPacketPrice.value = Number(payload.redPacketPrice || 0)
    await fetchPriceCalculation()
}

// 选择支付方式
const selectPayment = (index: number) => {
    state.selectedPaymentIndex = index
}

/** 从 15030 接口返回的 data 中解析无货商品 ID 列表 */
function parseOutOfStockGoodsIds(data: unknown): number[] {
    if (Array.isArray(data)) return data
    if (data && typeof data === 'object' && 'goodsId' in data) return (data as { goodsId?: number[] }).goodsId ?? []
    return []
}

/** 格式化收货地址字符串（计算价格、下单共用） */
function formatReceiverAddress(virtualOrder: boolean, addr: UserAddress | null): string {
    if (virtualOrder || !addr) return ''
    return `${addr.countryName || ''}-${addr.userAddressArea || ''} ${addr.userAddressAddress || ''}`.trim()
}

/**
 * 组合商品参数提取：从当前确认商品中提取组合ID与组合数量。
 * @param shopList 当前店铺列表
 * @returns combinationId / combinationGoodsNum
 */
function extractCombinationMeta(shopList: ShopList[]): { combinationId: number | null; combinationGoodsNum: number | null } {
    const goods = shopList
        .flatMap((shop) => shop.goodsList)
        .find((g) => Number(g.combinationId ?? 0) > 0)

    if (!goods) return { combinationId: null, combinationGoodsNum: null }

    const combinationId = Number(goods.combinationId ?? 0)
    const combinationGoodsNum = Number(goods.combinationNum ?? goods.combinationGoodsNum ?? 0)

    return {
        combinationId: combinationId > 0 ? combinationId : null,
        combinationGoodsNum: combinationGoodsNum > 0 ? combinationGoodsNum : null
    }
}

// 优惠券选择确认：更新 couponMap 并重新计算价格
const handleCouponConfirm = async (payload: { shopId: number; storeIndex: number; voucherId: string | null; couponPrice?: number }) => {
    hasCouponConfirmed.value = true
    if (payload.voucherId) {
        couponMap[payload.shopId] = { voucherId: payload.voucherId, couponPrice: payload.couponPrice }
    } else {
        delete couponMap[payload.shopId]
    }
    await fetchPriceCalculation()
}

// 地址变化处理
const handleAddressChange = async (address: UserAddress | null) => {
    state.selectedAddress = address
    // 当地址变化时，重新计算价格
    if (address) {
        await fetchPriceCalculation()
    }
}

/**
 * 调用计算价格接口
 */
/**
 * 拉取国补试算结果（只读，不占额度；实际占用在下单成功后由宿主调 internal/occupy 完成）。
 *
 * <p>叠加互斥的关键：把本次已选的券、已参与的满减/满折、积分抵扣状态一并上报，
 * 由后端判定国补是否命中——前端不自行判断，避免两边规则不一致。
 */
const refreshSubsidy = async (shopList: ShopList[]) => {
    try {
        const usedCoupon = Object.values(couponMap || {}).some((c) => c?.voucherId)
        const items = shopList.flatMap((shop: ShopList) =>
            (shop.goodsList || []).map((goods: GoodsList) => ({
                goodsId: Number(goods.goodsId),
                categoryId: String((goods as any).categoryId ?? ''),
                quantity: Number((goods as any).orderGoodsNum ?? goods.quantity ?? 1) || 1,
                unitPrice: Number((goods as any).goodsPrice ?? 0),
                provinceCode: provinceCodeOfAddress.value
            }))
        )
        if (items.length === 0) {
            subsidyQuote.value = null
            return
        }
        const quote = await fetchSubsidyQuote({
            provinceCode: provinceCodeOfAddress.value,
            hasCoupon: usedCoupon,
            hasPromotion: Boolean((priceResult.value as any)?.promotionAmount
                || (priceResult.value as any)?.manjianDiscount),
            hasPoint: Number((priceResult.value as any)?.totalIntegral || 0) > 0,
            items
        })
        subsidyQuote.value = quote
        // 对账自检：命中时校验「原价 − 国补 = 应付」，不一致只告警
        if (quote?.hit) {
            const base = items.reduce((sum, item) => sum + Number(item.unitPrice) * item.quantity, 0)
            const mismatch = verifyPayable(base, quote)
            if (mismatch) console.warn('[national-subsidy]', mismatch)
        }
    } catch (error) {
        // 插件未安装或接口异常：按无国补处理，不影响下单
        console.warn('[national-subsidy] 结算页试算失败，按无国补处理', error)
        subsidyQuote.value = null
    }
}

/** 收货地址所属省 code：地址对象无 code 时留空，由后端按规则判定地区是否命中。 */
const provinceCodeOfAddress = computed(() => {
    const addr: any = state.selectedAddress
    return addr?.provinceCode || addr?.areaCode || ''
})

const fetchPriceCalculation = async () => {
    const shopList = state.shopList.length > 0 ? state.shopList : goodsStore.getShopList
    if (!shopList || shopList.length === 0) {
        console.warn('购物车中没有商品，无法计算价格')
        return
    }

    const virtualOrder = shopList.every((s: ShopList) => s.virtualGoods === 0)
    if (!virtualOrder && !state.selectedAddress) {
        console.warn('未选择收货地址，无法计算价格')
        return
    }

    isLoadingPrice.value = true
    try {
        await fetchMemberClassId()
        const combinationMeta = extractCombinationMeta(shopList)
        // 构建请求参数
        const storeInformationList = shopList.map((shop: ShopList) => {
            const coupon = couponMap[shop.shopId]
            return {
                shopId: shop.shopId,
                couponId: coupon?.voucherId ? Number(coupon.voucherId) || 0 : 0,
                goodsBaseList: shop.goodsList.map((goods: GoodsList) => ({
                    goodsId: Number(goods.goodsId) as number,
                    commonId: Number(goods.commonId) as number,
                    goodsNum: goods.goodsNumber,
                    combinationType: Number(goods.combinationType ?? 0) || undefined,
                    combinationNum: Number(goods.combinationNum ?? 0) || undefined,
                    combinationId: Number(goods.combinationId ?? 0) || undefined,
                    combinationGoodsNum: Number(goods.combinationGoodsNum ?? goods.combinationNum ?? 0) || undefined
                })),
                useRebateType: 0
            }
        })

        const addr = state.selectedAddress
        const params: CalculatePriceParams = {
            redPacketId: selectedRedPacketId.value ? Number(selectedRedPacketId.value) : 0,
            orderReceiverProvinceId: virtualOrder ? 0 : (addr!.userAddressProvinceId ?? 0),
            orderReceiverCityId: virtualOrder ? 0 : (addr!.userAddressCityId ?? 0),
            orderReceiverAreaId: virtualOrder ? 0 : (addr!.userAddressAreaId ?? 0),
            countryId: virtualOrder ? 1 : (addr!.countryId || 1),
            virtualGoods: virtualOrder ? 0 : 1,
            storeInformationList,
            memberClassId: memberClassId.value,
            platForm: 1,
            businessId: null,
            goodsIds: shopList.flatMap((shop: ShopList) =>
                shop.goodsList.map((goods: GoodsList) => String(goods.goodsId))
            ),
            quotationId: quotationIdFromUrl.value ? Number(quotationIdFromUrl.value) || null : null,
            valetOrderId: valetOrderIdFromUrl.value || null,
            combinationId: combinationMeta.combinationId,
            combinationGoodsNum: combinationMeta.combinationGoodsNum,
            isAutoChoose: hasCouponConfirmed.value ? 0 : 1,
            orderReceiverName: virtualOrder ? '' : (addr!.userAddressContact || ''),
            orderReceiverContact: virtualOrder ? '' : (addr!.userAddressPhone || ''),
            orderReceiverAddress: formatReceiverAddress(virtualOrder, addr)
        }

        const response = await calculatePrice(params)

        // 宿主算价完成后同步拉国补：此时才知道最终用了哪些优惠（券/满减/积分），
        // 国补的叠加互斥必须基于这些真实状态判定，不能提前试算。
        await refreshSubsidy(shopList)

        if (response.code === 1 && response.data) {
            priceResult.value = response.data
            // 用接口返回的 shippingList 同步 couponMap，避免初次进入时 couponMap 为空导致某店取消优惠券时其他店也被清空
            syncCouponMapFromPriceResult(response.data)
        } else if (Number(response.code) === GOODS_OUT_OF_STOCK) {
            showOutOfStockModalByGoodsIds(parseOutOfStockGoodsIds(response.data), 'price')
        } else {
            Toast.error(response.message || '订单创建失败，请重试')
            console.error('计算价格失败:', response.message)
        }
    } catch (error) {
        console.error('计算价格接口调用失败:', error)
    } finally {
        isLoadingPrice.value = false
    }
}

/** 根据无货 goodsId 列表展示弹窗 */
const showOutOfStockModalByGoodsIds = (goodsIds: number[], retryAction: 'price' | 'order') => {
    const idSet = new Set(goodsIds.map(String))
    const list: typeof outOfStockList.value = []
    state.shopList.forEach((shop: ShopList) => {
        shop.goodsList.forEach((g: GoodsList) => {
            if (idSet.has(String(g.goodsId))) {
                list.push({
                    goodsId: String(g.goodsId),
                    goodsName: g.goodsName || '',
                    goodsSpecifications: g.goodsSpecifications,
                    goodsNumber: g.goodsNumber,
                    goodsImage: g.goodsImage
                })
            }
        })
    })
    outOfStockGoodsIds.value = goodsIds
    outOfStockList.value = list
    pendingRetryAction.value = retryAction
    showOutOfStockModal.value = true
}

/** 无货弹窗-取消 */
const handleOutOfStockCancel = () => {
    showOutOfStockModal.value = false
    outOfStockList.value = []
    outOfStockGoodsIds.value = []
    pendingRetryAction.value = null
}

/** 无货弹窗-确认：移除无货商品，若无剩余商品则返回上一页 */
const handleOutOfStockConfirm = () => {
    const idSet = new Set(outOfStockGoodsIds.value.map(String))
    const newShopList: ShopList[] = []

    state.shopList.forEach((shop: ShopList) => {
        const filteredGoods = shop.goodsList.filter((g) => !idSet.has(String(g.goodsId)))
        if (filteredGoods.length > 0) {
            const newShopTotal = filteredGoods.reduce((sum, g) => sum + g.goodsTotal, 0)
            newShopList.push({
                ...shop,
                goodsList: filteredGoods,
                shopTotalPrice: newShopTotal
            })
        }
    })

    state.shopList = newShopList
    goodsStore.setShopList(newShopList)
    showOutOfStockModal.value = false
    outOfStockList.value = []
    outOfStockGoodsIds.value = []
    const retry = pendingRetryAction.value
    pendingRetryAction.value = null

    if (newShopList.length === 0) {
        Toast.warning('已移除无货商品，当前无下单商品')
        useRouter().back()
        return
    }

    if (retry === 'price') {
        fetchPriceCalculation()
    } else if (retry === 'order') {
        handleSubmitOrder()
    }
}


// 返回购物车
const handleBackCart = () => {
    navigateTo('/cart/shopping')
}

// 提交订单
const handleSubmitOrder = async () => {
    // 防止重复点击
    if (isSubmitting.value) {
        return
    }

    if (state.shopList.length === 0) {
        Toast.warning('购物车中没有商品')
        return
    }

    const virtualOrder = isVirtualGoodsOrder.value
    if (!virtualOrder && !state.selectedAddress) {
        Toast.warning('请选择收货地址')
        return
    }

    const addr = state.selectedAddress
    const token = getToken()
    if (!token) {
        Toast.warning('请先登录')
        return
    }

    isSubmitting.value = true
    try {
        await fetchMemberClassId()
        const combinationMeta = extractCombinationMeta(state.shopList)
        // 构建创建订单请求参数
        const params: CreateOrdersParams = {
            redPacketId: selectedRedPacketId.value ? Number(selectedRedPacketId.value) : 0,
            orderReceiverName: virtualOrder ? '' : (addr!.userAddressContact || ''),
            orderReceiverContact: virtualOrder ? '' : (addr!.userAddressPhone || ''),
            orderReceiverAddress: formatReceiverAddress(virtualOrder, addr),
            orderReceiverProvinceId: virtualOrder ? 0 : addr!.userAddressProvinceId,
            orderReceiverCityId: virtualOrder ? 0 : addr!.userAddressCityId,
            orderReceiverAreaId: virtualOrder ? 0 : addr!.userAddressAreaId,
            countryId: virtualOrder ? 1 : (addr!.countryId || 1),
            countryName: virtualOrder ? '中国' : (addr!.countryName || '中国'),
            internationalDialingCode: virtualOrder ? '+86' : (addr!.internationalDialingCode || '+86'),
            currencySymbol: '￥',
            exchangeRate: '1',
            currencyCode: 'CNY',
            memberClassId: memberClassId.value,
            virtualGoods: virtualOrder ? 0 : 1,
            storeInformationList: state.shopList.map((shop: ShopList, index: number) => {
                const shippingInfo = priceResult.value?.shippingList?.find((s: any) => s.shopId === shop.shopId)
                const coupon = couponMap[shop.shopId]
                const invoiceMap = productInfoRef.value?.getStoreInvoiceMap?.() ?? {}
                const invoiceInfo = invoiceMap[index] ?? undefined
                return {
                    couponId: shippingInfo?.voucherId || coupon?.voucherId || '0',
                    shopId: shop.shopId,
                    invoiceId: invoiceInfo?.invoiceId ?? 0,
                    invoiceInfo,
                    useRebateType: 0,
                    buyerMessage: state.remark || '',
                    goodsBaseList: shop.goodsList.map((goods: GoodsList) => ({
                        goodsId: Number(goods.goodsId),
                        goodsName: goods.goodsName,
                        goodsNum: goods.goodsNumber,
                        combinationType: Number(goods.combinationType ?? 0) || undefined,
                        combinationNum: Number(goods.combinationNum ?? 0) || undefined,
                        combinationId: Number(goods.combinationId ?? 0) || undefined,
                        combinationGoodsNum: Number(goods.combinationGoodsNum ?? goods.combinationNum ?? 0) || undefined
                    }))
                }
            }),
            type: 1,
            platForm: 1,
            paymentCode: paymentCode.value,
            useRebateType: 0,
            goodsIds: state.shopList.flatMap((shop: ShopList) =>
                shop.goodsList.map((goods: GoodsList) => Number(goods.goodsId))
            ),
            quotationId: quotationIdFromUrl.value || null,
            valetOrderId: valetOrderIdFromUrl.value || null,
            combinationId: combinationMeta.combinationId,
            combinationGoodsNum: combinationMeta.combinationGoodsNum,
            isAutoChoose: hasCouponConfirmed.value ? 0 : 1,
            ...(isFromCartCheckout.value ? { goodsCat: true } : {})
        }

        const response = await createOrders(encryptCreateOrdersPayload({ ...params, token }))

        if (response.code === 1) {
            Toast.success('订单创建成功')
            const payId = response.data.paymentNumber
            const virtualGoodsQuery = virtualOrder ? '&virtualGoods=0' : ''
            if (state.selectedPaymentIndex === 0) {
                navigateTo(`/payment/online?payId=${payId}${virtualGoodsQuery}`)
            } else if (state.selectedPaymentIndex === 1) {
                navigateTo(`/payment/offline?payId=${payId}&orderAmount=${totalPaymentAmount.value}${virtualGoodsQuery}`)
            } else {
                UCENTER_URL('/order/list')
            }
        } else if (Number(response.code) === GOODS_OUT_OF_STOCK) {
            isSubmitting.value = false
            showOutOfStockModalByGoodsIds(parseOutOfStockGoodsIds(response.data), 'order')
        } else {
            Toast.error(response.message || '订单创建失败，请重试')
        }
    } catch (error) {
        console.error('创建订单失败:', error)
        Toast.error('订单创建失败，请重试')
    } finally {
        isSubmitting.value = false
    }
}

const initPaymentVisibility = async () => {
    const tongLianValue = await checkKeySwitch('tongLian_switch')
    if (tongLianValue === '1') {
        hideOfflinePayment.value = true
        hideCodPayment.value = true
        if (state.selectedPaymentIndex !== 0) state.selectedPaymentIndex = 0
        return
    }
    const shopList = state.shopList
    if (!shopList?.length) return
    const shopIdList = shopList.map((s: ShopList) => s.shopId)
    try {
        const res = await queryIsDelivery({ shopIdList })
        if (res?.code === 1 && res?.data?.isDelivery === 0) {
            hideCodPayment.value = true
            if (state.selectedPaymentIndex === 2) state.selectedPaymentIndex = 0
        }
    } catch {
        // 接口失败时不隐藏
    }
}

onMounted(async () => {
    const shopList = goodsStore.getShopList
    state.shopList = shopList?.length ? shopList : []
    await refreshPlusState()
    await initPaymentVisibility()
    // 虚拟商品订单无需地址，直接计算价格；否则价格计算会在地址选择时自动触发
    if (isVirtualGoodsOrder.value) {
        await fetchPriceCalculation()
    }
})
defineExpose({ state })

</script>
<style lang="scss" scoped>
.confirm-order-page {
    max-width: 1200px;
    margin: 20px auto;
    padding: 30px 20px;
    background-color: #fff;
}

/* 支付方式提示语 */
.payment-tip {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 12px 16px;
    margin-bottom: 16px;
    border-radius: 8px;
    font-size: 14px;
    line-height: 1.6;

    .tip-icon {
        flex-shrink: 0;
        width: 18px;
        height: 18px;
        margin-top: 2px;
    }

    .tip-text {
        color: #666;
    }

    &--account,
    &--installment {
        background: #fffbe6;
        border: 1px solid #ffe58f;
        color: #ad8b00;

        .tip-text {
            color: #ad8b00;
        }
    }
}

/* 统一标题样式 */
:deep(.section-header) {
    font-size: 16px;
    font-weight: bold;
    color: #333;
    background-color: #f0f2f5;
    padding: 8px 16px;
    border-radius: 4px;
}

/* 支付方式模块 */
.payment-section {
    background-color: #fff;
    border-radius: 8px;
    margin-bottom: 30px;

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
    }
}

/* 支付方式模块 */
.payment-list {
    display: flex;
    gap: 20px;

    .payment-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 24px;
        background-color: #fff;
        border: 2px solid #e0e0e0;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s ease;
        min-width: 120px;

        &:hover {
            border-color: var(--color);
        }

        &.active {
            border-color: var(--color);
            background-color: #fff0f0;
        }

        .payment-name {
            font-size: 14px;
            color: #333;
        }

        .payment-tag {
            width: 20px;
            height: 20px;
            border-radius: 50%;
            background-color: var(--color);
            color: #fff;
            padding: 3px;
            margin-left: 12px;
        }
    }
}

/* 订单支付区域 */
.order-payment-section {
    background-color: #fff;
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 20px;
    border: 1px solid #e8e8e8;

    .discount-line {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .discount-label {
            font-size: 14px;
            color: #666;
        }

        .discount-value {
            font-size: 16px;
            color: var(--color);
            font-weight: 600;
        }
    }

    /* 国补抵扣行：与其他优惠行同构，用独立色标区分政策类优惠 */
    .subsidy-line {
        .discount-label {
            color: #b91c1c;
        }

        .discount-value {
            color: #dc2626;
        }

        .subsidy-rate {
            padding: 0 5px;
            border-radius: 3px;
            background: #fef2f2;
            border: 1px solid #fecaca;
            color: #b91c1c;
            font-size: 12px;
            line-height: 18px;
        }
    }

    .payment-amount {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        flex-wrap: wrap;
        gap: 8px 24px;
        margin-bottom: 20px;

        .amount-label {
            font-size: 16px;
            color: #333;
        }

        .amount-value {
            font-size: 24px;
            font-weight: bold;
            color: var(--color);
        }
    }

    .payment-actions {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        gap: var(--space-4);
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .confirm-order-page {
        padding: 20px 15px;
    }

    .payment-list {
        flex-direction: column;
        gap: 12px;

        .payment-item {
            width: 100%;
            justify-content: center;
        }
    }

    .order-payment-section {
        padding: 16px;

        .payment-amount {
            .amount-label {
                font-size: 14px;
            }

            .amount-value {
                font-size: 20px;
            }
        }

        .payment-actions {
            flex-direction: column;
            align-items: stretch;

            .back-cart-btn,
            .submit-order-btn {
                width: 100%;
            }
        }
    }
}
</style>
