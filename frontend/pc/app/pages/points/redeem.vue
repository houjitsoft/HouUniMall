<template>
    <div class="redeem-page">
        <div class="redeem-container">
            <!-- 地址选择组件 -->
            <AddressSelect @addressChange="handleAddressChange" />

            <!-- 支付方式 -->
            <div class="payment-section">
                <div class="section-header">
                    <span class="section-title">{{ $t('支付方式') }}</span>
                </div>
                <div class="payment-list">
                    <div v-for="(payment, index) in paymentMethods" :key="index" class="payment-item"
                        :class="{ active: selectedPaymentIndex === index }" @click="selectPayment(index)">
                        <span class="payment-name">{{ payment.name }}</span>
                        <div class="payment-tag" v-if="selectedPaymentIndex === index">✓</div>
                    </div>
                </div>
            </div>

            <!-- 确认商品信息 -->
            <div class="product-section">
                <div class="section-header">
                    <span class="section-title">{{ $t('确认商品信息') }}</span>
                </div>

                <!-- 多店铺商品信息渲染 -->
                <div class="store-list" v-if="shopList && shopList.length > 0">
                    <div v-for="(store, storeIndex) in shopList" :key="storeIndex" class="store-item">
                        <div class="product-table">
                            <div class="table-header">
                                <div class="table-cell">{{ $t('商品') }}</div>
                                <div class="table-cell">{{ $t('价格') }}</div>
                                <div class="table-cell">{{ $t('数量') }}</div>
                                <div class="table-cell">{{ $t('小计') }}</div>
                            </div>
                            <div class="table-content">
                                <div class="product-item" v-for="(goods, goodsIndex) in store.goodsList"
                                    :key="goodsIndex">
                                    <div class="table-cell product-info">
                                        <img :src="goods.goodsImage || 'https://via.placeholder.com/80x80'"
                                            alt="product" class="product-image">
                                        <div class="product-details">
                                            <div class="product-name-wrapper">
                                                <div class="product-name">{{ $t(goods.goodsName || '') }}</div>
                                            </div>
                                            <div class="product-spec" v-if="goods.goodsSpecifications">
                                                {{ $t('规格') }}：{{ formatSpecifications(goods.goodsSpecifications) }}
                                            </div>
                                        </div>
                                    </div>
                                    <div class="table-cell price-info">
                                        <div class="product-price">
                                            {{ formatNumber((goods as GoodsList & {
                                                needIntegral?: number
                                            }).needIntegral || 0) }} {{ $t('积分') }}
                                            <span v-if="hasAdditionalPrice(goods)" class="price-additional">
                                                + {{ formatPriceWithSymbolSafe(getAdditionalPrice(goods)) }}
                                            </span>
                                        </div>
                                    </div>
                                    <div class="table-cell quantity">{{ goods.goodsNumber }}</div>
                                    <div class="table-cell subtotal">
                                        {{
                                            formatNumber(
                                                ((goods as GoodsList & { needIntegral?: number }).needIntegral || 0) *
                                                goods.goodsNumber
                                            )
                                        }} {{ $t('积分') }}
                                        <span v-if="hasAdditionalPrice(goods)" class="price-additional">
                                            + {{ formatPriceWithSymbolSafe(getAdditionalPrice(goods) *
                                                goods.goodsNumber) }}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 空状态提示 -->
                <EmptyState v-else :text="$t('暂无商品信息')" :description="$t('请先添加商品到购物车')" />
            </div>

            <!-- 备注和订单总结（左右排版） -->
            <div class="remark-summary-section">
                <!-- 左侧：备注 -->
                <div class="remark-section">
                    <span class="remark-label">{{ $t('备注') }}:</span>
                    <textarea v-model="remark" class="remark-input" :placeholder="$t('限45字(定制类商品,请将购买需求在备注中做详细说明')"
                        maxlength="45"></textarea>
                    <div class="remark-tip">{{ $t('提示:请勿填写有关支付、收货方面的信息') }}</div>
                </div>

                <!-- 右侧：订单总结 -->
                <div class="order-summary">
                    <div class="summary-row">
                        <span class="summary-text">
                            {{ $t('共') }} {{ formatNumber(userIntegral) }} {{ $t('积分') }},{{ $t('本单抵扣') }} {{
                                formatNumber(totalIntegral) }} {{
                                $t('积分') }}
                        </span>
                    </div>
                    <div class="summary-row">
                        <span class="summary-label">{{ $t('订单金额') }}:</span>
                        <span class="summary-value">{{ formatPriceWithSymbolSafe(totalAmount) }}</span>
                    </div>
                    <div class="summary-row">
                        <span class="summary-label">{{ $t('支付金额') }}:</span>
                        <span class="summary-value">{{ formatPriceWithSymbolSafe(paymentAmount) }}</span>
                    </div>
                    <button class="submit-btn" :disabled="isSubmitting || !selectedAddress || isLoadingPrice"
                        @click="handleSubmitOrder">
                        {{ isSubmitting ? $t('提交中...') : $t('提交订单') }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGoodsStore } from '~/stores/modules/goods'
import { useUserStore } from '~/stores/modules/user'
import EmptyState from '~/components/Empty/EmptyState.vue'
import AddressSelect from '~/components/confirmOrder/AddressSelect.vue'
import { ConfirmModal } from '~/composables/useConfirmModal'
import { formatPriceWithSymbol, formatPriceDisplay, formatNumber } from '~/composables/useFormatPrice'
import { calculatePrice } from '~/services/order.service'
import { createOrders } from '~/services/order.service'
import { Toast } from '~/composables/useToast'
import { UCENTER_URL } from '~/utils/externalUrl'
import { getToken } from '~/utils/auth'
import { encryptCreateOrdersPayload } from '@utils/cryptoAes'
import type { UserAddress } from '~/types/user'
import type { ShopList, GoodsList } from '~/types/goods'
import type { CalculatePriceParams, CreateOrdersParams, ShopDTO } from '~/types/business'

// ==================== 路由和状态 ====================
const route = useRoute()
const router = useRouter()
const goodsStore = useGoodsStore()
const userStore = useUserStore()

// ==================== 响应式状态 ====================
// 地址和支付方式
const selectedAddress = ref<UserAddress | null>(null)
const selectedPaymentIndex = ref(0)
const paymentMethods = ref([
    { name: '在线支付' }
])

// 商品列表
const shopList = ref<ShopList[]>([])

// 备注
const remark = ref('')

// 价格计算结果
const priceResult = ref<ShopDTO | null>(null)
const isLoadingPrice = ref(false)
const isSubmitting = ref(false)

// ==================== 计算属性 ====================
/**
 * 用户积分
 */
const userIntegral = computed(() => {
    return Number(userStore.userInfo?.userIntegral || 0)
})

/**
 * 总积分（本单抵扣）
 */
const totalIntegral = computed(() => {
    return shopList.value.reduce((total, shop) => {
        return total + shop.goodsList.reduce((sum, goods) => {
            const integral = (goods as GoodsList & { needIntegral?: number }).needIntegral || 0
            return sum + integral * goods.goodsNumber
        }, 0)
    }, 0)
})

/**
 * 总金额（订单金额）
 */
const totalAmount = computed(() => {
    // 优先使用接口返回的总价，避免本地兜底金额与实际计算口径不一致
    const backendTotalPrice = priceResult.value?.totalPrice
    if (backendTotalPrice !== undefined && backendTotalPrice !== null) {
        return backendTotalPrice
    }

    if (priceResult.value?.shippingList && priceResult.value.shippingList.length > 0) {
        return priceResult.value.shippingList.reduce((total: number, shipping: any) => {
            return total + (shipping.shopTotalPrice || 0)
        }, 0)
    }
    // 如果没有价格计算结果，计算商品总价
    return shopList.value.reduce((total, shop) => {
        return total + shop.goodsList.reduce((sum, goods) => {
            const price = getAdditionalPrice(goods)
            return sum + price * goods.goodsNumber
        }, 0)
    }, 0)
})

/**
 * 支付金额
 */
const paymentAmount = computed(() => {
    // 优先使用接口计算的总价（包含运费/优惠/抵扣等），避免本地兜底金额与实际不一致
    return priceResult.value?.totalPrice ?? totalAmount.value
})

/**
 * 判断商品是否有额外价格
 */
const hasAdditionalPrice = (goods: GoodsList): boolean => {
    const price = getAdditionalPrice(goods)
    return price > 0
}

/**
 * 获取商品的额外价格
 */
const getAdditionalPrice = (goods: GoodsList): number => {
    const price = (goods as any).goodsPrice || (goods as any).singlePrice || 0
    if (price === '*' || price === '' || price === null || price === undefined) {
        return 0
    }
    const priceNum = Number(price)
    return isNaN(priceNum) ? 0 : priceNum
}

// ==================== 工具函数 ====================
/**
 * 格式化规格信息
 * @param specifications 规格信息，可能是字符串（JSON数组）或数组
 * @returns 格式化后的规格字符串
 */
const formatSpecifications = (specifications: string | any[] | undefined | null): string => {
    if (!specifications) {
        return ''
    }

    try {
        // 如果是字符串，尝试解析为JSON
        let specArray: Array<{ paramName?: string; paramValue?: string; sort?: number }> = []

        if (typeof specifications === 'string') {
            // 尝试解析JSON字符串
            specArray = JSON.parse(specifications)
        } else if (Array.isArray(specifications)) {
            specArray = specifications
        } else {
            return String(specifications)
        }

        // 如果不是数组，返回原字符串
        if (!Array.isArray(specArray)) {
            return String(specifications)
        }

        // 按sort排序，然后格式化为 "参数名: 参数值" 的形式
        const sortedSpecs = specArray
            .filter(spec => spec.paramName && spec.paramValue)
            .sort((a, b) => (a.sort || 0) - (b.sort || 0))
            .map(spec => `${spec.paramName}: ${spec.paramValue}`)

        return sortedSpecs.join(' ')
    } catch (error) {
        // 如果解析失败，返回原字符串
        console.warn('解析规格信息失败:', error)
        return typeof specifications === 'string' ? specifications : String(specifications)
    }
}

// ==================== 事件处理 ====================
/**
 * 处理地址变化
 */
const handleAddressChange = async (address: UserAddress | null) => {
    selectedAddress.value = address
    // 当地址变化时，重新计算价格
    if (address) {
        await fetchPriceCalculation()
    }
}

/**
 * 选择支付方式
 */
const selectPayment = (index: number) => {
    selectedPaymentIndex.value = index
}

/**
 * 调用计算价格接口
 */
const fetchPriceCalculation = async () => {
    if (!selectedAddress.value) {
        console.warn('未选择收货地址，无法计算价格')
        return
    }

    if (!shopList.value || shopList.value.length === 0) {
        console.warn('没有商品，无法计算价格')
        return
    }

    isLoadingPrice.value = true
    try {
        // 构建请求参数
        const storeInformationList = shopList.value.map((shop: ShopList) => ({
            shopId: shop.shopId,
            couponId: 0,
            goodsBaseList: shop.goodsList.map((goods: GoodsList) => ({
                goodsId: Number(goods.goodsId) as number,
                commonId: Number(goods.commonId) as number,
                goodsNum: goods.goodsNumber
            })),
            useRebateType: 0
        }))

        const params: CalculatePriceParams = {
            redPacketId: 0,
            orderReceiverProvinceId: selectedAddress.value.userAddressProvinceId,
            orderReceiverCityId: selectedAddress.value.userAddressCityId,
            orderReceiverAreaId: selectedAddress.value.userAddressAreaId,
            countryId: selectedAddress.value.countryId || 1,
            virtualGoods: 1,
            storeInformationList,
            memberClassId: 0,
            platForm: 1,
            businessId: null,
            goodsIds: shopList.value.flatMap((shop: ShopList) =>
                shop.goodsList.map((goods: GoodsList) => String(goods.goodsId))
            ),
            quotationId: null,
            combinationId: null,
            combinationGoodsNum: null,
            isAutoChoose: 1,
            orderReceiverName: selectedAddress.value.userAddressContact || '',
            orderReceiverContact: selectedAddress.value.userAddressPhone || '',
            orderReceiverAddress: selectedAddress.value
                ? `${selectedAddress.value.countryName || ''}-${selectedAddress.value.userAddressArea || ''} ${selectedAddress.value.userAddressAddress || ''}`.trim()
                : '',
            integralGoods: 0 // 积分商品标识：0 表示积分商品
        }

        const response = await calculatePrice(params)

        if (response.code === 1 && response.data) {
            priceResult.value = response.data
        } else {
            console.warn('计算价格失败:', response.message)
            Toast.error(response.message || '计算价格失败，请稍后重试')
            priceResult.value = null
        }
    } catch (error: any) {
        console.error('计算价格接口调用失败:', error)
        Toast.error(error?.message || '计算价格失败，请稍后重试')
        priceResult.value = null
    } finally {
        isLoadingPrice.value = false
    }
}

/**
 * 处理提交订单
 */
const handleSubmitOrder = async () => {
    if (!selectedAddress.value) {
        Toast.error('请选择收货地址')
        return
    }

    if (!shopList.value || shopList.value.length === 0) {
        Toast.error('没有商品，无法提交订单')
        return
    }

    if (totalIntegral.value > userIntegral.value) {
        Toast.error('积分不足，无法兑换')
        return
    }

    const token = getToken()
    if (!token) {
        Toast.error('请先登录')
        return
    }

    isSubmitting.value = true
    try {
        // 构建订单参数
        const storeInformationList = shopList.value.map((shop: ShopList) => ({
            shopId: shop.shopId,
            couponId: 0,
            buyerMessage: remark.value,
            goodsBaseList: shop.goodsList.map((goods: GoodsList) => ({
                goodsId: Number(goods.goodsId) as number,
                goodsName: goods.goodsName,
                goodsNum: goods.goodsNumber
            })),
            useRebateType: 0
        }))

        // 判断是否为积分商品：如果总积分大于0，则为积分商品（integralGoods = 0），否则为其他商品
        const integralGoods = totalIntegral.value > 0 ? 0 : 1

        const params: CreateOrdersParams = {
            redPacketId: 0,
            orderReceiverName: selectedAddress.value.userAddressContact || '',
            orderReceiverContact: selectedAddress.value.userAddressPhone || '',
            orderReceiverAddress: `${selectedAddress.value.countryName || ''}-${selectedAddress.value.userAddressArea || ''} ${selectedAddress.value.userAddressAddress || ''}`.trim(),
            orderReceiverProvinceId: selectedAddress.value.userAddressProvinceId,
            orderReceiverCityId: selectedAddress.value.userAddressCityId,
            orderReceiverAreaId: selectedAddress.value.userAddressAreaId,
            countryId: selectedAddress.value.countryId || 1,
            countryName: selectedAddress.value.countryName,
            internationalDialingCode: selectedAddress.value.internationalDialingCode,
            virtualGoods: 1,
            storeInformationList,
            type: selectedPaymentIndex.value === 0 ? 1 : selectedPaymentIndex.value === 1 ? 2 : 3,
            platForm: 1,
            paymentCode: selectedPaymentIndex.value === 0 ? 1 : selectedPaymentIndex.value === 1 ? 2 : 3,
            useRebateType: 0,
            goodsIds: shopList.value.flatMap((shop: ShopList) =>
                shop.goodsList.map((goods: GoodsList) => Number(goods.goodsId))
            ),
            quotationId: null,
            combinationId: null,
            combinationGoodsNum: null,
            isAutoChoose: 1,
            integralGoods: integralGoods
        }

        const response = await createOrders(encryptCreateOrdersPayload({ ...params, token }))

        if (response.code === 1) {
            // 清空购物车
            goodsStore.clearShopList()

            // 根据返回的 isIntegral 值进行处理
            const isIntegral = response.data?.isIntegral

            if (isIntegral === 1) {
                // 积分商品，使用 API 调用显示确认弹窗
                await ConfirmModal.confirm({
                    title: '兑换成功',
                    message: '兑换成功!是否前往积分列表查看~',
                    confirmText: '确认',
                    cancelText: '再逛逛',
                    onConfirm: () => {
                        UCENTER_URL('/property/integral')
                    }
                })

                // 如果用户点击确认，已经在 onConfirm 中处理了跳转
                // 如果用户点击取消，已经在 onCancel 中处理了跳转
            } else {
                // 非积分商品，跳转支付页面
                router.push(`/payment/online?payId=${response.data.paymentNumber}&integralPay=1`)
            }
        } else {
            Toast.error(response.message || '订单提交失败')
        }
    } catch (error: any) {
        console.error('提交订单失败:', error)
        Toast.error(error?.message || '订单提交失败，请稍后重试')
    } finally {
        isSubmitting.value = false
    }
}

// ==================== 生命周期 ====================
onMounted(() => {
    // 从 goods store 获取商品列表
    const goods = goodsStore.getShopList
    if (goods && goods.length > 0) {
        shopList.value = goods
    } else {
        // 如果没有商品，返回上一页
        Toast.error('没有商品信息，请重新选择')
        router.back()
    }
})
</script>

<style lang="scss" scoped>
.redeem-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 20px 0 60px;

    .redeem-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }

    // 支付方式区域
    .payment-section {
        background-color: #fff;
        border-radius: 8px;
        padding: 20px;
        margin-bottom: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

        .section-header {
            font-size: 16px;
            font-weight: bold;
            color: #333;
            background-color: #f0f2f5;
            padding: 8px 16px;
            border-radius: 4px;
            margin-bottom: 20px;

            .section-title {
                font-size: 16px;
                font-weight: bold;
                color: #333;
            }
        }

        .payment-list {
            display: flex;
            gap: 20px;

            .payment-item {
                position: relative;
                padding: 12px 24px;
                border: 2px solid #e0e0e0;
                border-radius: 4px;
                cursor: pointer;
                transition: all 0.3s ease;

                .payment-name {
                    font-size: 14px;
                    color: #666;
                }

                .payment-tag {
                    position: absolute;
                    top: -8px;
                    right: -8px;
                    width: 24px;
                    height: 24px;
                    background-color: var(--color);
                    color: #fff;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 14px;
                    font-weight: 600;
                }

                &.active {
                    border-color: var(--color);

                    .payment-name {
                        color: var(--color);
                        font-weight: 600;
                    }
                }

                &:hover {
                    border-color: var(--color);
                }
            }
        }
    }

    // 商品信息区域
    .product-section {
        background-color: #fff;
        border-radius: 8px;
        margin-bottom: 30px;

        .section-header {
            font-size: 16px;
            font-weight: bold;
            color: #333;
            background-color: #f0f2f5;
            padding: 8px 16px;
            border-radius: 4px;
            margin-bottom: 20px;

            .section-title {
                font-size: 16px;
                font-weight: bold;
                color: #333;
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
                                }

                                .product-order,
                                .product-spec {
                                    font-size: 12px;
                                    color: #8c8c8c;
                                    margin-bottom: 6px;
                                    line-height: 1.6;
                                }
                            }
                        }

                        &.price-info {
                            flex-direction: column;
                            justify-content: center;
                            align-items: center;

                            .product-price {
                                font-size: 16px;
                                color: #262626;
                                font-weight: 700;
                                font-family: 'Arial', sans-serif;
                                letter-spacing: 0.3px;
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                gap: 4px;

                                .price-additional {
                                    font-size: 14px;
                                    color: var(--color);
                                }
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
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            gap: 4px;

                            .price-additional {
                                font-size: 14px;
                            }
                        }
                    }
                }
            }
        }
    }

    // 备注和订单总结（左右排版）
    .remark-summary-section {
        display: flex;
        gap: 20px;
        margin-bottom: 20px;

        // 左侧：备注
        .remark-section {
            flex: 1;
            display: flex;
            flex-direction: column;
            gap: 8px;
            background-color: #fff;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

            .remark-label {
                font-size: 14px;
                color: #666;
            }

            .remark-input {
                width: 100%;
                min-height: 80px;
                padding: 12px;
                border: 1px solid #e0e0e0;
                border-radius: 4px;
                font-size: 14px;
                resize: vertical;
                outline: none;

                &:focus {
                    border-color: var(--color);
                }
            }

            .remark-tip {
                font-size: 12px;
                color: var(--color);
            }
        }

        // 右侧：订单总结
        .order-summary {
            flex: 0 0 400px;
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            gap: 12px;
            background-color: #fff;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

            .summary-row {
                display: flex;
                align-items: center;
                gap: 8px;

                .summary-text {
                    font-size: 14px;
                    color: #666;
                }

                .summary-label {
                    font-size: 14px;
                    color: #666;
                }

                .summary-value {
                    font-size: 16px;
                    color: var(--color);
                    font-weight: 600;
                }
            }

            .submit-btn {
                padding: 12px 48px;
                font-size: 16px;
                font-weight: 600;
                color: #fff;
                background-color: var(--color);
                border: none;
                border-radius: 4px;
                cursor: pointer;
                transition: all 0.3s ease;
                margin-top: 8px;

                &:hover:not(:disabled) {
                    background-color: color-mix(in srgb, var(--color) 90%, black);
                }

                &:disabled {
                    opacity: 0.6;
                    cursor: not-allowed;
                }
            }
        }
    }

    // 响应式设计
    @media (max-width: 968px) {
        .remark-summary-section {
            flex-direction: column;

            .order-summary {
                flex: 1;
            }
        }
    }
}
</style>
