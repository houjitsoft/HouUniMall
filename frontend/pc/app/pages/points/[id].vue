<template>
    <div class="points-detail-page">
        <div class="points-detail-container">
            <!-- 加载状态 -->
            <Loading v-if="loading" :text="$t('加载中...')" />

            <!-- 错误状态 -->
            <div v-else-if="error" class="error-container">
                <div class="error-text">{{ $t(error || '') }}</div>
                <button class="retry-btn" @click="fetchGoodsDetail">{{ $t('重试') }}</button>
            </div>

            <!-- 商品信息 -->
            <div v-else-if="goodsData" class="product-main-section">
                <div class="product-layout">
                    <!-- 左侧：商品图片 -->
                    <div class="product-image-section">
                        <MagnifyingGlass :current-image="currentImage" :product-images="productImages"
                            :current-index="currentImageIndex" @image-change="handleImageChange" />
                    </div>

                    <!-- 中间：商品信息 -->
                    <div class="product-info-section">
                        <h1 class="product-title">{{ $t(goodsData.goodsBases.goodsName || '') }}&nbsp;&nbsp;&nbsp; {{
                            goodsData.goodsBases.goodsSpec || '' }}</h1>

                        <div class="price-section">
                            <div class="price-item">
                                <span class="price-label">{{ $t('兑换价') }}:</span>
                                <span class="exchange-price">
                                    <span class="integral-value">{{ goodsData.goodsBases.needIntegral || 0 }}{{ $t('积分')
                                    }}</span>
                                    <span v-if="hasAdditionalPrice" class="price-separator">+</span>
                                    <span v-if="hasAdditionalPrice" class="price-value-red">{{
                                        formatPriceWithSymbolSafe(additionalPrice) }}</span>
                                </span>
                            </div>
                            <div class="price-item">
                                <span class="price-label">{{ $t('原价') }}:</span>
                                <span class="price-value">{{
                                    formatPriceWithSymbolSafe(goodsData.goodsBases.goodsMarketPrice)
                                    }}</span>
                            </div>
                        </div>

                        <div class="info-row-group">
                            <div class="info-row">
                                <span class="info-label">{{ $t('销量') }}:</span>
                                <span class="info-value">{{ goodsData.goodsBases.goodsSalenum || 0 }}{{ $t('件')
                                }}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">{{ $t('剩余库存') }}:</span>
                                <span class="info-value">{{ goodsData.goodsBases.goodsStock || 0 }}{{ $t('件') }}</span>
                            </div>
                        </div>

                        <!-- 数量选择 -->
                        <div class="quantity-section">
                            <span class="quantity-label">{{ $t('数量') }}:</span>
                            <div class="quantity-controls">
                                <button class="quantity-btn" @click="decreaseQuantity"
                                    :disabled="quantity <= 1">-</button>
                                <input v-model.number="quantity" type="number" min="1" :max="maxQuantity"
                                    class="quantity-input" @input="handleQuantityInput" />
                                <button class="quantity-btn" @click="increaseQuantity"
                                    :disabled="quantity >= maxQuantity">+</button>
                            </div>
                            <span v-if="Number(goodsData.goodsBases?.goodsMaxSale || 0) >= 1" class="purchase-limit">
                                {{ $t('限购') }}: {{ goodsData.goodsBases.goodsMaxSale || 0 }}{{ $t('件') }}
                            </span>
                        </div>

                        <!-- 立即兑换按钮 -->
                        <button class="redeem-btn" @click="handleRedeem" :disabled="!canRedeem">
                            {{ $t('立即兑换') }}
                        </button>
                    </div>
                </div>

                <!-- 猜你想看（左） + 商品详情（右） -->
                <div class="detail-recommend-row">
                    <div class="recommend-section">
                        <h3 class="recommend-title">{{ $t('猜你想看') }}</h3>
                        <div class="recommend-list">
                            <Loading v-if="recommendLoading" compact :text="$t('加载中...')" />
                            <EmptyState v-else-if="recommendProducts.length === 0" :text="$t('暂无推荐商品')" compact />
                            <div v-else v-for="product in recommendProducts" :key="product.commonId || product.goodsId"
                                class="recommend-item" @click="handleRecommendClick(product)">
                                <div class="recommend-image">
                                    <img :src="product.commonImage || product.goodsImage"
                                        :alt="product.commonName || product.goodsName" />
                                </div>
                                <div class="recommend-info">
                                    <div class="recommend-name">{{ $t(product.commonName || product.goodsName || '') }}
                                    </div>
                                    <div class="recommend-price">{{ (product.needIntegral || 0) }}
                                        {{ $t('积分') }}
                                        <span v-if="Number(product.commonPrice) > 0">+{{
                                            formatPriceWithSymbolSafe(Number(product.commonPrice))
                                            }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="product-detail-tabs">
                        <div class="tab-header">
                            <div class="tab-item active">
                                <div class="tab-indicator"></div>
                                <span class="tab-text">{{ $t('商品详情') }}</span>
                            </div>
                        </div>
                        <div class="tab-content">
                            <Loading v-if="detailLoading" compact :text="$t('加载中...')" />
                            <div v-else-if="detailError" class="product-detail-error">{{ $t(detailError || '') }}</div>
                            <div v-else-if="commonDetailDesc?.commonBody" class="product-detail-content"
                                v-html="$t(commonDetailDesc.commonBody || '')"></div>
                            <EmptyState v-else :text="$t('暂无商品详情')" compact />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGoodsStore } from '~/stores/modules/goods'
import MagnifyingGlass from '~/components/product/MagnifyingGlass.vue'
import Loading from '~/components/Load/Loading.vue'
import { getIntegralGoodsDetail, queryGoodsFilter, getCommonDetailDesc } from '~/services/goods.service'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'
import { useAuth } from '~/composables/useAuth'
import type { GoodsFilterQueryItem } from '~/types/goods'
import type { CommonDetailDesc } from '~/types/goods'
import type { ShopList, GoodsList } from '~/types/goods'

const route = useRoute()
const router = useRouter()
const goodsStore = useGoodsStore()
const { isLogin: checkLoginRedirect } = useAuth()

// 路由参数
const goodsId = computed(() => route.params.id as string)

// 响应式数据
const loading = ref(false)
const error = ref<string | null>(null)
const goodsData = ref<any>(null)
const currentImageIndex = ref(0)
const quantity = ref(1)
const selectedSpecIndex = ref(0)
const recommendProducts = ref<GoodsFilterQueryItem[]>([])
const recommendLoading = ref(false)
const commonDetailDesc = ref<CommonDetailDesc | null>(null)
const detailLoading = ref(false)
const detailError = ref<string | null>(null)

// 计算属性
const currentImage = computed(() => {
    if (!goodsData.value) return ''
    const images = productImages.value
    return images[currentImageIndex.value] || images[0] || ''
})

const productImages = computed(() => {
    if (!goodsData.value) return []
    const images: string[] = []

    // 添加主图
    if (goodsData.value.goodsBases?.goodsImage) {
        images.push(goodsData.value.goodsBases.goodsImage)
    }

    // 添加商品图片列表
    if (goodsData.value.goodsImages && Array.isArray(goodsData.value.goodsImages)) {
        goodsData.value.goodsImages.forEach((img: any) => {
            if (img.imagesImage && !images.includes(img.imagesImage)) {
                images.push(img.imagesImage)
            }
        })
    }

    return images.length > 0 ? images : ['/placeholder-image.png']
})

const specValueList = computed(() => {
    if (!goodsData.value) return []
    return goodsData.value.goodsBases?.specValueList || []
})

const maxQuantity = computed(() => {
    if (!goodsData.value) return 1
    const stock = goodsData.value.goodsBases?.goodsStock || 0
    const maxSale = goodsData.value.goodsBases?.goodsMaxSale || 0
    if (maxSale > 0) {
        return Math.min(stock, maxSale)
    }
    return stock
})

const canRedeem = computed(() => {
    if (!goodsData.value) return false
    const stock = goodsData.value.goodsBases?.goodsStock || 0
    return stock > 0 && quantity.value > 0 && quantity.value <= maxQuantity.value
})

/**
 * 是否有额外价格（积分+价格）
 */
const hasAdditionalPrice = computed(() => {
    if (!goodsData.value) return false
    const price = goodsData.value.goodsBases?.goodsPrice
    return price !== undefined && price !== null && price !== '' && price !== '*' && Number(price) > 0
})

/**
 * 额外价格
 */
const additionalPrice = computed(() => {
    if (!goodsData.value) return 0
    const price = goodsData.value.goodsBases?.goodsPrice
    if (price === undefined || price === null || price === '' || price === '*') {
        return 0
    }
    return Number(price)
})

/**
 * 获取商品详情
 */
const fetchGoodsDetail = async () => {
    if (!goodsId.value) {
        error.value = '商品ID不存在'
        return
    }

    loading.value = true
    error.value = null

    try {
        // 将 goodsId 转换为数字或字符串
        const id = typeof goodsId.value === 'string' ? goodsId.value : String(goodsId.value)
        const response = await getIntegralGoodsDetail({ goodsId: id })

        if (response.code === 1 && response.data) {
            goodsData.value = response.data
            // 获取推荐商品
            fetchRecommendProducts()
            // 获取商品详情描述
            fetchCommonDetailDesc()
        } else {
            throw new Error(response.message || '获取商品详情失败')
        }
    } catch (err: any) {
        error.value = err.message || '加载商品详情失败，请稍后重试'
        console.error('获取商品详情失败:', err)
    } finally {
        loading.value = false
    }
}

/**
 * 获取商品详情描述
 */
const fetchCommonDetailDesc = async () => {
    if (!goodsData.value?.goodsBases?.commonId) {
        return
    }

    detailLoading.value = true
    detailError.value = null

    try {
        const response = await getCommonDetailDesc({ commonsId: goodsData.value.goodsBases.commonId })

        if (response.code === 1 && response.data) {
            commonDetailDesc.value = response.data
        } else {
            throw new Error(response.message || '获取商品详情描述失败')
        }
    } catch (err: any) {
        detailError.value = err.message || '加载商品详情描述失败，请稍后重试'
        console.error('获取商品详情描述失败:', err)
    } finally {
        detailLoading.value = false
    }
}

/**
 * 获取推荐商品列表
 */
const fetchRecommendProducts = async () => {
    if (!goodsData.value) return

    recommendLoading.value = true
    try {
        const response = await queryGoodsFilter({
            token: null,
            brandId: '',
            catId: goodsData.value.goodsBases?.catId ? String(goodsData.value.goodsBases.catId) : '',
            commonName: '',
            minPrice: '',
            maxPrice: '',
            shopSelfSupport: '',
            orderType: 5,
            commentOrder: 1,
            priceOrder: '',
            saleNum: '',
            currentPage: 1,
            pageSize: 5,
            integralGoods: 0,
            commonIsRecommend: 1
        })

        if (response.code === 1 && response.data?.list) {
            // 过滤掉当前商品
            recommendProducts.value = response.data.list.filter(
                (item: GoodsFilterQueryItem) => item.goodsId !== goodsData.value.goodsBases.goodsId
            ).slice(0, 5)
        }
    } catch (err) {
        console.error('获取推荐商品失败:', err)
    } finally {
        recommendLoading.value = false
    }
}

/**
 * 处理图片切换
 */
const handleImageChange = (index: number) => {
    currentImageIndex.value = index
}

/**
 * 选择规格
 */
const selectSpec = (index: number) => {
    selectedSpecIndex.value = index
}

/**
 * 增加数量
 */
const increaseQuantity = () => {
    if (quantity.value < maxQuantity.value) {
        quantity.value++
    }
}

/**
 * 减少数量
 */
const decreaseQuantity = () => {
    if (quantity.value > 1) {
        quantity.value--
    }
}

/**
 * 处理数量输入
 */
const handleQuantityInput = (event: Event) => {
    const target = event.target as HTMLInputElement
    let value = parseInt(target.value) || 1
    value = Math.max(1, Math.min(value, maxQuantity.value))
    quantity.value = value
}

/**
 * 处理立即兑换：未登录时跳转登录页并携带当前页回跳地址
 * @returns void
 */
const handleRedeem = () => {
    if (!checkLoginRedirect(route.fullPath)) return
    if (!canRedeem.value || !goodsData.value) return

    const goodsBases = goodsData.value.goodsBases
    const selectedSpec = specValueList.value[selectedSpecIndex.value]

    // 构建规格信息（JSON数组格式）
    let goodsSpecifications = ''
    if (selectedSpec) {
        // 将选中的规格构建成JSON数组格式
        const specArray = [{
            paramName: selectedSpec.paramName || '',
            paramValue: selectedSpec.paramValue || '',
            sort: selectedSpec.sort || 0
        }]
        goodsSpecifications = JSON.stringify(specArray)
    } else if (specValueList.value.length > 0) {
        // 如果没有选中规格，使用第一个规格
        const firstSpec = specValueList.value[0]
        const specArray = [{
            paramName: firstSpec.paramName || '',
            paramValue: firstSpec.paramValue || '',
            sort: firstSpec.sort || 0
        }]
        goodsSpecifications = JSON.stringify(specArray)
    } else if (goodsBases.specValue) {
        // 如果specValue是字符串，尝试解析或直接使用
        goodsSpecifications = goodsBases.specValue
    }

    // 构建商品信息
    const goodsListData: GoodsList = {
        goodsId: String(goodsBases.goodsId),
        commonId: String(goodsBases.commonId),
        goodsImage: goodsBases.commonImage || goodsBases.goodsImage || '',
        goodsName: goodsBases.goodsName || '',
        dealerGoodsFlag: goodsBases.dealerGoodsFlag || 0,
        goodsNumber: quantity.value,
        goodsSpecifications: goodsSpecifications,
        goodsCode: goodsBases.goodsCode || '',
        goodsStock: goodsBases.goodsStock || 0,
        singlePrice: additionalPrice.value,
        goodsTotal: additionalPrice.value * quantity.value,
        activityType: goodsBases.promotionType?.toString() || '',
        commonIsXian: goodsBases.commonIsXian || 0
    }

        // 添加积分信息（扩展字段）
        ; (goodsListData as any).needIntegral = goodsBases.needIntegral || 0
        ; (goodsListData as any).goodsPrice = additionalPrice.value > 0 ? String(additionalPrice.value) : '0'

    // 构建店铺信息
    const shopListData: ShopList = {
        shopId: goodsBases.shopId || 0,
        shopName: goodsBases.shopName || '',
        shopInvoices: 0,
        invoiceActive: 0,
        goodsList: [goodsListData],
        shopTotalPrice: additionalPrice.value * quantity.value
    }

    // 存储到 goods store
    goodsStore.setShopList([shopListData])

    // 跳转到兑换确认页面
    router.push('/points/redeem')
}

/**
 * 处理推荐商品点击
 */
const handleRecommendClick = (product: GoodsFilterQueryItem) => {
    const id = product.goodsId || product.commonId
    if (id) {
        navigateTo(`/points/${id}`)
    }
}

// 页面加载时获取商品详情
onMounted(() => {
    fetchGoodsDetail()
})

// 页面元信息（SEO + Open Graph）
const seoTitle = computed(() => goodsData.value
    ? `${goodsData.value.goodsBases.goodsName} - 积分商品详情`
    : `积分商品详情 - ${goodsId.value}`)
const seoDescription = computed(() => goodsData.value
    ? `查看积分商品 ${goodsData.value.goodsBases.goodsName} 的详细信息、规格和兑换价格`
    : `查看积分商品 ${goodsId.value} 的详细信息、规格和兑换价格`)
const ogImage = computed(() => {
    const imgs = goodsData.value?.goodsImages
    if (!imgs?.length) return undefined
    const first = imgs[0] as { imagesImage?: string }
    return first?.imagesImage || undefined
})
useHead({
    title: seoTitle,
    meta: [
        { key: 'description', name: 'description', content: seoDescription }
    ]
})
useSeoMeta({
    description: seoDescription,
    ogTitle: seoTitle,
    ogDescription: seoDescription,
    ogImage,
    ogType: 'website'
})
</script>

<style lang="scss" scoped>
.points-detail-page {
    min-height: 100vh;
    background: linear-gradient(to bottom, #f8f9fa 0%, #f5f5f5 100%);
    padding: 24px 0 40px;
}

.points-detail-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

.error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 500px;
    padding: 60px 40px;
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.error-text {
    color: var(--color);
    font-size: 16px;
    margin-bottom: 24px;
    font-weight: 500;
}

.retry-btn {
    padding: 12px 32px;
    font-size: 14px;
    color: #fff;
    background: var(--color);
    border: none;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s ease;
    font-weight: 500;
    box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 25%, transparent);

    &:hover {
        background: color-mix(in srgb, var(--color) 90%, black);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px color-mix(in srgb, var(--color) 35%, transparent);
    }

    &:active {
        transform: translateY(0);
    }
}

.product-main-section {
    background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
    border-radius: 12px;
    padding: 36px;
    margin-bottom: 24px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    transition: box-shadow 0.3s ease;

    &:hover {
        box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
    }
}

.product-layout {
    display: grid;
    grid-template-columns: 350px 1fr;
    gap: 30px;
    margin-bottom: 40px;
}

.detail-recommend-row {
    display: flex;
    gap: 24px;
    align-items: flex-start;

    .recommend-section {
        flex-shrink: 0;
        width: 280px;
    }

    .product-detail-tabs {
        flex: 1;
        min-width: 0;
    }
}

// MagnifyingGlass 组件自带样式，无需额外样式

.product-info-section {
    .product-title {
        font-size: 18px;
        font-weight: 700;
        color: #1a1a1a;
        margin-bottom: 24px;
        line-height: 1.6;
        letter-spacing: -0.3px;
    }

    .price-section {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-bottom: 24px;
        padding: 20px;
        background: linear-gradient(135deg, color-mix(in srgb, var(--color) 5%, #fff) 0%, #fff 100%);
        border-radius: 8px;
        border: 1px solid color-mix(in srgb, var(--color) 15%, #fff);

        .price-item {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 8px;

            .price-label {
                font-size: 12px;
                color: #999;
                font-weight: 500;
            }

            .price-value {
                font-size: 14px;
                color: #bbb;
                text-decoration: line-through;
                font-weight: 500;
            }

            .exchange-price {
                font-size: 22px;
                font-weight: 700;
                display: flex;
                align-items: baseline;
                gap: 6px;
                line-height: 1.2;

                .integral-value {
                    color: var(--color);
                    background: var(--color);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                    background-clip: text;
                }

                .price-separator {
                    color: var(--color);
                    font-weight: 700;
                    margin: 0 4px;
                    font-size: 22px;
                }

                .price-value-red {
                    color: var(--color);
                    background: var(--color);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                    background-clip: text;
                }
            }
        }
    }

    .info-row-group {
        display: flex;
        gap: 16px;
        margin-bottom: 16px;
    }

    .info-row {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: flex-start;
        padding: 12px 16px;
        border-radius: 6px;
        font-size: 14px;
        transition: all 0.3s ease;

        .info-label {
            color: #666;
            font-weight: 500;
        }

        .info-value {
            color: #333;
            font-weight: 600;
            font-size: 15px;
            margin-left: 10px;
        }
    }

    .quantity-section {
        display: flex;
        align-items: center;
        gap: 20px;
        padding: 20px;
        background-color: #fafafa;
        border-radius: 8px;

        .quantity-label {
            font-size: 15px;
            color: #333;
            font-weight: 600;
        }

        .quantity-controls {
            display: flex;
            align-items: center;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            overflow: hidden;
            background-color: #fff;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

            .quantity-btn {
                width: 40px;
                height: 40px;
                border: none;
                background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
                cursor: pointer;
                font-size: 20px;
                color: #333;
                font-weight: bold;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                justify-content: center;

                &:hover:not(:disabled) {
                    background: var(--color);
                    color: #fff;
                    transform: scale(1.1);
                }

                &:active:not(:disabled) {
                    transform: scale(0.95);
                }

                &:disabled {
                    opacity: 0.4;
                    cursor: not-allowed;
                    background: #f5f5f5;
                }
            }

            .quantity-input {
                width: 70px;
                height: 40px;
                border: none;
                border-left: 1px solid #e0e0e0;
                border-right: 1px solid #e0e0e0;
                text-align: center;
                font-size: 16px;
                font-weight: 600;
                color: #333;
                outline: none;
                background-color: #fff;

                &:focus {
                    background-color: color-mix(in srgb, var(--color) 5%, #fff);
                }

                &::-webkit-inner-spin-button,
                &::-webkit-outer-spin-button {
                    -webkit-appearance: none;
                    margin: 0;
                }
            }
        }

        .purchase-limit {
            font-size: 14px;
            color: var(--color);
            font-weight: 600;
        }
    }

    .redeem-btn {
        width: 70%;
        height: 52px;
        background: var(--color);
        color: #fff;
        border: none;
        border-radius: 8px;
        font-size: 18px;
        font-weight: 700;
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        margin-top: 24px;
        box-shadow: 0 4px 12px color-mix(in srgb, var(--color) 30%, transparent);
        position: relative;
        overflow: hidden;

        &::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 0;
            height: 0;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.3);
            transform: translate(-50%, -50%);
            transition: width 0.6s, height 0.6s;
        }

        &:active:not(:disabled) {
            transform: translateY(0);
            box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 30%, transparent);
        }

        &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
            background: #ccc;
            box-shadow: none;
        }
    }
}

.recommend-section {
    background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);

    .recommend-title {
        font-size: 18px;
        font-weight: 700;
        color: #1a1a1a;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 3px solid var(--color);
        position: relative;

        &::after {
            content: '';
            position: absolute;
            bottom: -3px;
            left: 0;
            width: 60px;
            height: 3px;
            background: linear-gradient(90deg, var(--color) 0%, transparent 100%);
        }
    }

    .recommend-list {
        padding-right: 8px;

        &::-webkit-scrollbar {
            width: 6px;
        }

        &::-webkit-scrollbar-track {
            background: #f1f1f1;
            border-radius: 3px;
        }

        &::-webkit-scrollbar-thumb {
            background: var(--color);
            border-radius: 3px;

            &:hover {
                background: color-mix(in srgb, var(--color) 90%, black);
            }
        }

        .recommend-loading,
        .recommend-empty {
            text-align: center;
            padding: 30px 20px;
            color: #999;
            font-size: 14px;
        }

        .recommend-item {
            display: flex;
            gap: 14px;
            padding: 14px;
            border: 2px solid #f0f0f0;
            border-radius: 8px;
            margin-bottom: 14px;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            background-color: #fff;

            &:hover {
                border-color: var(--color);
                box-shadow: 0 4px 12px color-mix(in srgb, var(--color) 15%, transparent);
                transform: translateX(4px);
            }

            .recommend-image {
                width: 90px;
                height: 90px;
                flex-shrink: 0;
                border-radius: 6px;
                overflow: hidden;
                background-color: #f5f5f5;
                border: 1px solid #eee;
                transition: transform 0.3s ease;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                    transition: transform 0.3s ease;
                }
            }

            &:hover .recommend-image {
                transform: scale(1.05);

                img {
                    transform: scale(1.1);
                }
            }

            .recommend-info {
                flex: 1;
                display: flex;
                flex-direction: column;
                justify-content: space-between;

                .recommend-name {
                    font-size: 14px;
                    color: #333;
                    line-height: 1.5;
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    line-clamp: 2;
                    -webkit-box-orient: vertical;
                    overflow: hidden;
                    margin-bottom: 10px;
                    font-weight: 500;
                }

                .recommend-price {
                    font-size: 15px;
                    color: var(--color);
                    font-weight: 700;
                }
            }
        }
    }
}

.product-detail-tabs {
    background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    overflow: hidden;

    .tab-header {
        border-bottom: 2px solid #f0f0f0;
        padding: 0 36px;
        background-color: #fff;

        .tab-item {
            display: inline-flex;
            align-items: center;
            padding: 18px 0;
            position: relative;
            cursor: pointer;
            margin-right: 40px;

            &.active {
                .tab-indicator {
                    position: absolute;
                    bottom: -2px;
                    left: 0;
                    right: 0;
                    height: 3px;
                    background: var(--color);
                    border-radius: 2px 2px 0 0;
                }

                .tab-text {
                    color: var(--color);
                    font-weight: 700;
                }
            }

            .tab-text {
                font-size: 17px;
                color: #666;
                transition: all 0.3s ease;
                font-weight: 500;

                &:hover {
                    color: var(--color);
                }
            }
        }
    }

    .tab-content {
        padding: 36px;
        min-height: 200px;
        background-color: #fff;

        .product-detail-loading,
        .product-detail-error {
            text-align: center;
            padding: 60px 40px;
            font-size: 15px;
            background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
            border-radius: 8px;
        }

        .product-detail-loading {
            color: #666;
            position: relative;
            padding-left: 60px;

            &::before {
                content: '';
                position: absolute;
                left: 50%;
                top: 50%;
                transform: translate(-50%, -50%);
                width: 24px;
                height: 24px;
                border: 3px solid var(--color);
                border-top-color: transparent;
                border-radius: 50%;
                animation: spin 0.8s linear infinite;
            }
        }

        .product-detail-error {
            color: var(--color);
        }

        .product-detail-content {
            line-height: 1.8;
            color: #333;

            :deep(img) {
                max-width: 100%;
                height: auto;
                border-radius: 8px;
                margin: 16px 0;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }

            :deep(p) {
                margin: 12px 0;
            }

            :deep(table) {
                width: 100%;
                border-collapse: collapse;
                margin: 16px 0;

                th,
                td {
                    border: 1px solid #e0e0e0;
                    padding: 12px;
                    text-align: left;
                }

                th {
                    background-color: #f8f9fa;
                    font-weight: 600;
                }
            }
        }

        .product-detail-empty {
            text-align: center;
            padding: 60px 40px;
            color: #999;
            font-size: 15px;
            background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
            border-radius: 8px;
        }
    }
}

// 响应式设计
@media (max-width: 1200px) {
    .product-layout {
        grid-template-columns: 300px 1fr;
        gap: 20px;
    }

    .detail-recommend-row .recommend-section {
        width: 250px;
    }
}

@media (max-width: 992px) {
    .product-layout {
        grid-template-columns: 1fr;
        gap: 30px;
    }

    .detail-recommend-row {
        flex-direction: column;
    }

    .detail-recommend-row .recommend-section {
        width: 100%;
    }
}

@media (max-width: 768px) {
    .points-detail-container {
        padding: 0 12px;
    }

    .product-main-section {
        padding: 20px;
    }

    .product-info-section {
        .product-title {
            font-size: 12px;
        }
    }
}
</style>