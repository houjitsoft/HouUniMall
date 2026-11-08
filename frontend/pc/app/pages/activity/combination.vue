<template>
    <div class="combination-page">
        <!-- 顶部标题区域 -->
        <div class="page-header">
            <div class="header-left">
                <span class="header-icon">🎯</span>
                <span class="header-title">{{ $t('组合促销') }}</span>
            </div>
            <div class="header-right">
                <div class="sort-tabs">
                    <div class="sort-tab-item" :class="{ active: currentSortType === 'comprehensive' }"
                        @click="handleSortChange('comprehensive')">{{ $t('综合') }}</div>
                    <div class="sort-tab-item" :class="{ active: currentSortType === 'price' }"
                        @click="handleSortChange('price')">{{ $t('价格') }}</div>
                    <div class="sort-tab-item" :class="{ active: onlyActive }"
                        @click="onlyActive = !onlyActive; handleFilterChange()">{{ $t('进行中') }}</div>
                </div>
            </div>
        </div>

        <!-- 商品列表区域 -->
        <div class="product-list" v-if="!loading && products.length > 0">
            <div class="product-item" v-for="product in products" :key="product.combinationId"
                @click="handleProductClick(product)">
                <!-- 组合类型标签 -->
                <div class="combination-tag"
                    :class="{ 'tag-fixed': product.combinationType === 1, 'tag-matching': product.combinationType === 2 }">
                    {{ product.combinationType === 1 ? $t('固定') : $t('搭配') }}
                </div>

                <!-- 商品图片 -->
                <div class="product-image">
                    <img :src="product.combinationImage" :alt="product.combinationName" />
                    <div class="goods-list-icon" v-if="product.goodsList && product.goodsList.length > 0">
                        <svg viewBox="0 0 24 24" fill="currentColor">
                            <path
                                d="M4 4h4v4H4V4zm6 0h4v4h-4V4zm6 0h4v4h-4V4zM4 10h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4zM4 16h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4z" />
                        </svg>
                    </div>
                </div>

                <!-- 价格信息 -->
                <div class="product-price">
                    <span class="current-price">
                        {{ formatPriceWithSymbolSafe(product.combinationPrice) }}
                    </span>
                    <span class="original-price" v-if="product.goodsOriginalPrice">
                        {{ formatPriceWithSymbolSafe(product.goodsOriginalPrice) }}
                    </span>
                </div>

                <!-- 商品名称 -->
                <div class="product-name">{{ $t(product.combinationName || '') }}</div>

                <!-- 倒计时 -->
                <div class="countdown-info">
                    <span class="countdown-icon">🕐</span>
                    <span class="countdown-text">{{ $t('距离结束') }} {{ formatCountdown(product.endTime) }}</span>
                </div>

                <!-- 店铺名称 -->
                <div class="shop-name">{{ $t(product.shopName || '') }}</div>
            </div>
        </div>

        <!-- 加载状态 -->
        <Loading v-if="loading" :text="$t('正在加载...')" />

        <!-- 空状态 -->
        <EmptyState v-if="!loading && products.length === 0" :text="$t('暂无组合促销商品')" />

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="!loading && totalCount > 0">
            <div class="pagination-info">
                <UPagination v-model:page="currentPage" :total="totalCount" :items-per-page="pageSize"
                    class="pagination-right" @update:page="handlePageChange" />
            </div>
        </div>

        <!-- 搭配组合弹窗 -->
        <div class="modal-overlay" v-if="showMatchingModal" @click="closeMatchingModal">
            <div class="modal-content matching-modal" @click.stop>
                <div class="modal-header">
                    <div class="modal-title">{{ $t('搭配组合') }}</div>
                    <div class="modal-close" @click="closeMatchingModal">×</div>
                </div>
                <div class="modal-body">
                    <div class="goods-list" v-if="selectedProduct && selectedProduct.goodsList">
                        <div class="goods-item" v-for="(goods, index) in selectedProduct.goodsList" :key="index">
                            <div class="goods-checkbox">
                                <input type="checkbox" :id="`goods-${index}`" :checked="matchingGoodsSelected[index]"
                                    @change="toggleMatchingGoods(index)" />
                                <label :for="`goods-${index}`"></label>
                            </div>
                            <div class="goods-image">
                                <img :src="goods.goodsImage" :alt="goods.goodsName" />
                            </div>
                            <div class="goods-info">
                                <div class="goods-name">{{ $t(goods.goodsName || '') }}</div>
                                <div class="goods-spec">{{ $t('规格') }}: {{ goods.goodsSpec }}</div>
                                <div class="goods-price">{{ formatPriceWithSymbolSafe(goods.matchingPrices ||
                                    goods.goodsPrice) }}
                                </div>
                            </div>
                            <div class="goods-quantity">
                                <button class="quantity-btn" @click="decreaseMatchingQuantity(index)"
                                    :disabled="!matchingGoodsSelected[index] || (matchingGoodsQuantity[index] ?? 0) <= 0">-</button>
                                <input type="number" v-model.number="matchingGoodsQuantity[index]"
                                    @change="updateMatchingPrice" min="0" :disabled="!matchingGoodsSelected[index]" />
                                <button class="quantity-btn" @click="increaseMatchingQuantity(index)"
                                    :disabled="!matchingGoodsSelected[index]">+</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="price-info">
                        <div class="combination-price">{{ $t('组合价格') }}: {{ formatPrice(matchingTotalPrice) }}</div>
                        <div class="saved-price" v-if="selectedProduct && matchingOriginalPrice > 0">
                            {{ $t('已为您节省') }}: {{ formatPrice(matchingOriginalPrice - matchingTotalPrice) }}
                        </div>
                    </div>
                    <button class="buy-btn" @click="handleMatchingBuy" :disabled="matchingTotalPrice <= 0">{{ $t('立即购买')
                        }}</button>
                </div>
            </div>
        </div>

        <!-- 固定组合弹窗 -->
        <div class="modal-overlay" v-if="showFixedModal" @click="closeFixedModal">
            <div class="modal-content fixed-modal" @click.stop>
                <div class="modal-header">
                    <div class="modal-title">{{ $t('固定组合') }}</div>
                    <div class="modal-close" @click="closeFixedModal">×</div>
                </div>
                <div class="modal-body">
                    <div class="goods-list" v-if="selectedProduct && selectedProduct.goodsList">
                        <div class="goods-item" v-for="(goods, index) in selectedProduct.goodsList" :key="index">
                            <div class="goods-image">
                                <img :src="goods.goodsImage" :alt="goods.goodsName" />
                            </div>
                            <div class="goods-info">
                                <div class="goods-name">{{ $t(goods.goodsName || '') }}</div>
                                <div class="goods-spec">{{ $t('规格') }}: {{ goods.goodsSpec }}</div>
                                <div class="goods-price">{{ formatPriceWithSymbolSafe(goods.goodsPrice) }}</div>
                            </div>
                            <div class="goods-quantity-label">x{{ goods.goodsNum || 1 }}</div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="quantity-control">
                        <span>{{ $t('购买数量') }}:</span>
                        <div class="quantity-selector">
                            <button class="quantity-btn" @click="decreaseFixedQuantity"
                                :disabled="fixedQuantity <= 1">-</button>
                            <input type="number" v-model.number="fixedQuantity" min="1" />
                            <button class="quantity-btn" @click="increaseFixedQuantity">+</button>
                        </div>
                    </div>
                    <div class="price-info">
                        <div class="combination-price">{{ $t('组合价格') }}: {{ formatPrice(fixedTotalPrice) }}</div>
                        <div class="saved-price" v-if="selectedProduct && fixedOriginalPrice > 0">
                            {{ $t('已为您节省') }}: {{ formatPrice((fixedOriginalPrice - fixedTotalPrice)) }}
                        </div>
                    </div>
                    <button class="buy-btn" @click="handleFixedBuy">{{ $t('立即购买') }}</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { queryCombinationPcList } from '~/services/activity.service'
import { useGoodsStore } from '~/stores/modules/goods'
import type { GoodsList, ShopList } from '~/stores/modules/goods'
import { useAuth } from '~/composables/useAuth'
import { Toast } from '~/composables/useToast'
import { formatPrice } from '~/composables/useFormatPrice'
import type { CombinationGoodsDTO, CombinationPcVO, SortType } from '~/types/activity'

// 响应式数据
const products = ref<CombinationPcVO[]>([])
const loading = ref(false)
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(60)
const currentSortType = ref<SortType>('comprehensive')
const onlyActive = ref(false)
const currentTime = ref(Date.now())

// 弹窗相关
const showMatchingModal = ref(false)
const showFixedModal = ref(false)
const selectedProduct = ref<CombinationPcVO | null>(null)

// 搭配组合弹窗数据
const matchingGoodsSelected = ref<boolean[]>([])
const matchingGoodsQuantity = ref<number[]>([])

// 固定组合弹窗数据
const fixedQuantity = ref(1)

// 倒计时定时器
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 获取 goods store
const goodsStore = useGoodsStore()

// 获取认证实例
const { isLogin } = useAuth()
const route = useRoute()

// 检查登录状态
const checkLogin = (): boolean => {
    return isLogin(route.fullPath)
}


// 格式化倒计时
const formatCountdown = (endTime?: string): string => {
    if (!endTime) return '00时00分00秒'

    const end = new Date(endTime).getTime()
    const now = currentTime.value
    const diff = Math.max(0, end - now)

    if (diff <= 0) return '00时00分00秒'

    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)

    return `${String(hours).padStart(2, '0')}时${String(minutes).padStart(2, '0')}分${String(seconds).padStart(2, '0')}秒`
}

// 启动倒计时定时器
const startCountdownTimer = () => {
    clearCountdownTimer()
    countdownTimer = setInterval(() => {
        currentTime.value = Date.now()
    }, 1000)
}

// 清除倒计时定时器
const clearCountdownTimer = () => {
    if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
    }
}

// 排序切换
const handleSortChange = (sortType: SortType) => {
    if (currentSortType.value === sortType) return
    currentSortType.value = sortType
    currentPage.value = 1
    fetchProducts()
}

// 筛选切换
const handleFilterChange = () => {
    currentPage.value = 1
    fetchProducts()
}

// 获取排序参数
const getSortParams = () => {
    const params: {
        priceDesc?: number
        timeDesc?: number
    } = {}

    if (currentSortType.value === 'price') {
        params.priceDesc = 1 // 降序
    }

    return params
}

// 获取商品列表
const fetchProducts = async () => {
    try {
        loading.value = true

        const sortParams = getSortParams()
        const params: {
            catParentId?: number
            currentPage?: number
            limit?: number
            navProperty?: number
            page?: number
            pageSize?: number
            priceDesc?: number
            status?: number
            timeDesc?: number
            [property: string]: any
        } = {
            catParentId: undefined,
            currentPage: currentPage.value,
            limit: pageSize.value,
            navProperty: undefined,
            page: currentPage.value,
            pageSize: pageSize.value,
            priceDesc: sortParams.priceDesc,
            status: onlyActive.value ? 1 : undefined,
            timeDesc: sortParams.timeDesc
        }

        const res = await queryCombinationPcList<CombinationPcVO>(params)

        if (res && res.code === 1 && res.data) {
            products.value = res.data.list || []
            totalCount.value = res.data.totalCount || 0
            startCountdownTimer()
        } else {
            products.value = []
            totalCount.value = 0
        }
    } catch (error) {
        console.error('获取组合促销商品失败:', error)
        products.value = []
        totalCount.value = 0
    } finally {
        loading.value = false
    }
}

// 分页切换
const handlePageChange = (page: number) => {
    currentPage.value = page
    fetchProducts()
    window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 商品点击
const handleProductClick = (product: CombinationPcVO) => {
    selectedProduct.value = product

    if (product.combinationType === 1) {
        // 固定组合
        fixedQuantity.value = 1
        showFixedModal.value = true
    } else if (product.combinationType === 2) {
        // 搭配组合
        if (product.goodsList && product.goodsList.length > 0) {
            matchingGoodsSelected.value = product.goodsList.map((goods, index) => {
                // 默认选中第一个商品
                return index === 0
            })
            matchingGoodsQuantity.value = product.goodsList.map((goods, index) => {
                // 默认第一个商品数量为1，其他为0
                return index === 0 ? (goods.goodsNum || 1) : 0
            })
        }
        showMatchingModal.value = true
    }
}

// 搭配组合弹窗 - 减少数量
const decreaseMatchingQuantity = (index: number) => {
    if (!matchingGoodsSelected.value[index]) return
    if (matchingGoodsQuantity.value[index] !== undefined && matchingGoodsQuantity.value[index] > 0) {
        matchingGoodsQuantity.value[index]--
        updateMatchingPrice()
    }
}

// 搭配组合弹窗 - 增加数量
const increaseMatchingQuantity = (index: number) => {
    if (!matchingGoodsSelected.value[index]) return
    if (matchingGoodsQuantity.value[index] !== undefined) {
        matchingGoodsQuantity.value[index]++
    } else {
        matchingGoodsQuantity.value[index] = 1
    }
    updateMatchingPrice()
}

// 搭配组合弹窗 - 切换选中状态
const toggleMatchingGoods = (index: number) => {
    matchingGoodsSelected.value[index] = !matchingGoodsSelected.value[index]
    if (!matchingGoodsSelected.value[index]) {
        // 取消选中时，数量设为0
        matchingGoodsQuantity.value[index] = 0
    } else {
        // 选中时，数量设为1
        matchingGoodsQuantity.value[index] = matchingGoodsQuantity.value[index] || 1
    }
    updateMatchingPrice()
}

// 搭配组合弹窗 - 更新总价
const updateMatchingPrice = () => {
    // 价格会在computed中自动更新
}

// 搭配组合弹窗 - 计算总价
const matchingTotalPrice = computed(() => {
    if (!selectedProduct.value || !selectedProduct.value.goodsList) return 0

    let total = 0
    selectedProduct.value.goodsList.forEach((goods, index) => {
        const isSelected = matchingGoodsSelected.value[index] ?? false
        const quantity = matchingGoodsQuantity.value[index] ?? 0
        if (isSelected && quantity > 0) {
            const price = goods.matchingPrices ?? goods.goodsPrice ?? 0
            total += price * quantity
        }
    })
    return total
})

// 搭配组合弹窗 - 计算原价总和
const matchingOriginalPrice = computed(() => {
    if (!selectedProduct.value || !selectedProduct.value.goodsList) return 0

    let total = 0
    selectedProduct.value.goodsList.forEach((goods, index) => {
        const isSelected = matchingGoodsSelected.value[index] ?? false
        const quantity = matchingGoodsQuantity.value[index] ?? 0
        if (isSelected && quantity > 0) {
            // 使用商品原价（goodsPrice）计算原价总和
            const originalPrice = goods.goodsPrice ?? 0
            total += originalPrice * quantity
        }
    })
    return total
})

// 搭配组合弹窗 - 关闭
const closeMatchingModal = () => {
    showMatchingModal.value = false
    selectedProduct.value = null
    matchingGoodsSelected.value = []
    matchingGoodsQuantity.value = []
}

// 搭配组合弹窗 - 购买
const handleMatchingBuy = () => {
    // 检查登录状态
    if (!checkLogin()) {
        return
    }

    // 检查是否至少选择了一个商品
    const hasSelectedGoods = matchingGoodsSelected.value.some(selected => selected === true)
    if (!hasSelectedGoods) {
        Toast.warning('请至少选择一个商品')
        return
    }

    // 检查选中的商品数量是否大于0
    let hasValidQuantity = false
    matchingGoodsSelected.value.forEach((selected, index) => {
        if (selected && (matchingGoodsQuantity.value[index] ?? 0) > 0) {
            hasValidQuantity = true
        }
    })

    if (!hasValidQuantity) {
        Toast.warning('请至少选择一个商品并设置数量')
        return
    }

    if (!selectedProduct.value || !selectedProduct.value.goodsList) {
        Toast.error('商品信息不完整')
        return
    }

    // 构建商品列表
    const goodsList: GoodsList[] = []
    selectedProduct.value.goodsList.forEach((goods, index) => {
        if (matchingGoodsSelected.value[index] && (matchingGoodsQuantity.value[index] ?? 0) > 0) {
            const quantity = matchingGoodsQuantity.value[index] ?? 1
            const price = goods.matchingPrices ?? goods.goodsPrice ?? 0

            goodsList.push({
                goodsId: String(goods.goodsId || ''),
                commonId: String(goods.commonId || ''),
                goodsImage: goods.goodsImage || '',
                goodsName: goods.goodsName || '',
                dealerGoodsFlag: 0,
                goodsNumber: quantity,
                goodsSpecifications: goods.goodsSpec || '',
                goodsCode: goods.goodsCode || '',
                goodsStock: goods.goodsStock || 0,
                singlePrice: price,
                goodsTotal: price * quantity,
                activityType: '',
                combinationType: Number(selectedProduct.value?.combinationType ?? 0),
                combinationNum: quantity,
                combinationId: Number(selectedProduct.value?.combinationId ?? 0),
                combinationGoodsNum: quantity
            })
        }
    })

    if (goodsList.length === 0) {
        Toast.warning('请至少选择一个商品')
        return
    }

    // 构建店铺列表
    const shopItem: ShopList = {
        shopId: selectedProduct.value.shopId || 0,
        shopName: selectedProduct.value.shopName || '',
        shopInvoices: selectedProduct.value.shopInvoices || 0,
        invoiceActive: selectedProduct.value.invoiceActive || 0,
        goodsList: goodsList,
        shopTotalPrice: matchingTotalPrice.value
    }

    // 设置到 store 并跳转
    goodsStore.clearShopList()
    goodsStore.setShopList([shopItem])
    closeMatchingModal()
    navigateTo('/confirmOrder')
}

// 固定组合弹窗 - 减少数量
const decreaseFixedQuantity = () => {
    if (fixedQuantity.value > 1) {
        fixedQuantity.value--
    }
}

// 固定组合弹窗 - 增加数量
const increaseFixedQuantity = () => {
    fixedQuantity.value++
}

// 固定组合弹窗 - 计算总价
const fixedTotalPrice = computed(() => {
    if (!selectedProduct.value || !selectedProduct.value.combinationPrice) return 0
    return selectedProduct.value.combinationPrice * fixedQuantity.value
})

// 固定组合弹窗 - 计算原价总和
const fixedOriginalPrice = computed(() => {
    if (!selectedProduct.value || !selectedProduct.value.goodsList) return 0

    // 计算单个组合的原价总和
    let singleOriginalPrice = 0
    selectedProduct.value.goodsList.forEach((goods) => {
        const goodsNum = goods.goodsNum || 1
        const originalPrice = goods.goodsPrice ?? 0
        singleOriginalPrice += originalPrice * goodsNum
    })

    // 乘以购买数量
    return singleOriginalPrice * fixedQuantity.value
})

// 固定组合弹窗 - 关闭
const closeFixedModal = () => {
    showFixedModal.value = false
    selectedProduct.value = null
    fixedQuantity.value = 1
}

// 固定组合弹窗 - 购买
const handleFixedBuy = () => {
    // 检查登录状态
    if (!checkLogin()) {
        return
    }

    if (!selectedProduct.value || !selectedProduct.value.goodsList || selectedProduct.value.goodsList.length === 0) {
        Toast.error('商品信息不完整')
        return
    }

    if (fixedQuantity.value <= 0) {
        Toast.warning('购买数量必须大于0')
        return
    }

    // 构建商品列表
    const goodsList: GoodsList[] = selectedProduct.value.goodsList.map((goods) => {
        const goodsNum = goods.goodsNum || 1
        const totalQuantity = goodsNum * fixedQuantity.value
        const price = goods.goodsPrice ?? 0

        return {
            goodsId: String(goods.goodsId || ''),
            commonId: String(goods.commonId || ''),
            goodsImage: goods.goodsImage || '',
            goodsName: goods.goodsName || '',
            dealerGoodsFlag: 0,
            goodsNumber: totalQuantity,
            goodsSpecifications: goods.goodsSpec || '',
            goodsCode: goods.goodsCode || '',
            goodsStock: goods.goodsStock || 0,
            singlePrice: price,
            goodsTotal: price * totalQuantity,
            activityType: '',
            combinationType: Number(selectedProduct.value?.combinationType ?? 0),
            combinationNum: fixedQuantity.value,
            combinationId: Number(selectedProduct.value?.combinationId ?? 0),
            combinationGoodsNum: fixedQuantity.value
        }
    })

    // 构建店铺列表
    const shopItem: ShopList = {
        shopId: selectedProduct.value.shopId || 0,
        shopName: selectedProduct.value.shopName || '',
        shopInvoices: selectedProduct.value.shopInvoices || 0,
        invoiceActive: selectedProduct.value.invoiceActive || 0,
        goodsList: goodsList,
        shopTotalPrice: fixedTotalPrice.value
    }

    // 设置到 store 并跳转
    goodsStore.clearShopList()
    goodsStore.setShopList([shopItem])
    closeFixedModal()
    navigateTo('/confirmOrder')
}

// 页面加载时初始化
onMounted(() => {
    fetchProducts()
})

// 组件卸载时清理定时器
onBeforeUnmount(() => {
    clearCountdownTimer()
})
</script>

<style lang="scss" scoped>
.combination-page {
    width: 100%;
    min-height: 100vh;
    background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
    padding: 32px 0;
    margin: 0 auto;

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        background: linear-gradient(135deg, #fff 0%, color-mix(in srgb, var(--color) 4%, #fff) 100%);
        padding: 20px 28px;
        border-radius: 12px 12px 0 0;
        margin-bottom: 0;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
        border-bottom: 1px solid color-mix(in srgb, var(--color) 12%, #fff);
        position: relative;
        overflow: hidden;

        &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 4px;
            height: 28px;
            background: var(--color);
            border-radius: 0 4px 4px 0;
        }

        .header-left {
            display: flex;
            align-items: center;
            gap: 14px;
            padding-left: 4px;

            .header-icon {
                font-size: 28px;
                filter: drop-shadow(0 1px 4px rgba(0, 0, 0, 0.1));
                opacity: 0.95;
            }

            .header-title {
                font-size: 22px;
                font-weight: 700;
                color: var(--color);
                letter-spacing: 0.5px;
                line-height: 1.3;
            }
        }

        .header-right {
            display: flex;
            align-items: center;
        }

        .sort-tabs {
            display: flex;
            align-items: center;
            gap: 2px;
            padding: 3px;
            background: color-mix(in srgb, var(--color) 6%, #fff);
            border-radius: 8px;
            border: 1px solid color-mix(in srgb, var(--color) 15%, #fff);

            .sort-tab-item {
                padding: 6px 16px;
                font-size: 13px;
                font-weight: 600;
                color: #606266;
                cursor: pointer;
                transition: all 0.2s ease;
                border-radius: 6px;
                position: relative;

                &:hover {
                    color: var(--color);
                    background: color-mix(in srgb, var(--color) 10%, #fff);
                }

                &.active {
                    color: #fff;
                    background: var(--color);
                    box-shadow: 0 1px 4px color-mix(in srgb, var(--color) 30%, transparent);
                }
            }
        }
    }

    .product-list {
        display: grid;
        grid-template-columns: repeat(5, 1fr);
        gap: 24px;
        padding: 32px;
        background: #fff;
        border-radius: 0 0 16px 16px;
        box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);

        .product-item {
            display: flex;
            flex-direction: column;
            background: #fff;
            border: 1.5px solid #f0f2f5;
            border-radius: 14px;
            overflow: hidden;
            cursor: pointer;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06), 0 1px 4px rgba(0, 0, 0, 0.04);

            &:hover {
                border-color: var(--color);
                box-shadow: 0 16px 40px rgba(0, 0, 0, 0.14), 0 6px 16px color-mix(in srgb, var(--color) 25%, transparent);
                transform: translateY(-8px) scale(1.02);
            }

            .combination-tag {
                position: absolute;
                top: 8px;
                left: 8px;
                padding: 4px 12px;
                font-size: 12px;
                font-weight: 700;
                color: #fff;
                border-radius: 4px;
                z-index: 10;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);

                &.tag-fixed {
                    background: var(--color);
                }

                &.tag-matching {
                    background: var(--color);
                }
            }

            .product-image {
                width: 100%;
                aspect-ratio: 1/1;
                overflow: hidden;
                display: flex;
                align-items: center;
                justify-content: center;
                background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
                position: relative;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: contain;
                    transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                }

                .goods-list-icon {
                    position: absolute;
                    bottom: 8px;
                    right: 8px;
                    width: 24px;
                    height: 24px;
                    background: rgba(0, 0, 0, 0.6);
                    border-radius: 4px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: #fff;

                    svg {
                        width: 16px;
                        height: 16px;
                    }
                }

                &:hover img {
                    transform: scale(1.08);
                }
            }

            .product-price {
                padding: 14px 16px 6px;
                display: flex;
                align-items: baseline;
                gap: 8px;

                .current-price {
                    font-size: 22px;
                    font-weight: 800;
                    color: var(--color);
                    display: flex;
                    align-items: baseline;
                    gap: 2px;

                    .currency-symbol {
                        font-size: 18px;
                        font-weight: 700;
                    }
                }

                .original-price {
                    font-size: 14px;
                    color: #909399;
                    text-decoration: line-through;
                    display: flex;
                    align-items: baseline;
                    gap: 2px;

                    .currency-symbol {
                        font-size: 12px;
                    }
                }
            }

            .product-name {
                padding: 0 16px 8px;
                font-size: 14px;
                color: #303133;
                line-height: 1.5;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                height: calc(14px * 1.5 * 2);
                font-weight: 600;
            }

            .countdown-info {
                padding: 10px 16px;
                display: flex;
                align-items: center;
                gap: 6px;
                background: linear-gradient(180deg, #fafbfc 0%, #ffffff 100%);
                border-top: 1px solid #f0f2f5;

                .countdown-icon {
                    font-size: 14px;
                    flex-shrink: 0;
                }

                .countdown-text {
                    font-size: 12px;
                    color: var(--color);
                    font-weight: 600;
                    flex: 1;
                }
            }

            .shop-name {
                padding: 0 16px 10px;
                font-size: 12px;
                color: #909399;
                font-weight: 500;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
        }
    }

    .pagination-wrapper {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: 40px;
        padding: 28px;
        background: linear-gradient(135deg, #fff 0%, #fafbfc 100%);
        border-radius: 16px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    }

    // 弹窗样式
    .modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;

        .modal-content {
            background-color: #fff;
            border-radius: 12px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
            width: 90%;
            max-width: 800px;
            max-height: 90vh;
            overflow: hidden;
            display: flex;
            flex-direction: column;

            .modal-header {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 20px 24px;
                border-bottom: 1px solid #e0e0e0;

                .modal-title {
                    font-size: 20px;
                    font-weight: 700;
                    color: #303133;
                }

                .modal-close {
                    font-size: 28px;
                    color: #909399;
                    cursor: pointer;
                    line-height: 1;
                    transition: color 0.3s;

                    &:hover {
                        color: var(--color);
                    }
                }
            }

            .modal-body {
                flex: 1;
                overflow-y: auto;
                padding: 24px;

                .goods-list {
                    .goods-item {
                        display: flex;
                        align-items: center;
                        padding: 16px 0;
                        border-bottom: 1px solid #f0f2f5;

                        &:last-child {
                            border-bottom: none;
                        }

                        .goods-checkbox {
                            margin-right: 16px;

                            input[type="checkbox"] {
                                width: 18px;
                                height: 18px;
                                cursor: pointer;
                            }
                        }

                        .goods-image {
                            width: 80px;
                            height: 80px;
                            margin-right: 16px;
                            border: 1px solid #f0f2f5;
                            border-radius: 8px;
                            overflow: hidden;
                            flex-shrink: 0;

                            img {
                                width: 100%;
                                height: 100%;
                                object-fit: contain;
                            }
                        }

                        .goods-info {
                            flex: 1;
                            margin-right: 16px;

                            .goods-name {
                                font-size: 14px;
                                color: #303133;
                                font-weight: 600;
                                margin-bottom: 8px;
                                line-height: 1.5;
                            }

                            .goods-spec {
                                font-size: 12px;
                                color: #909399;
                                margin-bottom: 8px;
                            }

                            .goods-price {
                                font-size: 16px;
                                color: var(--color);
                                font-weight: 700;
                            }
                        }

                        .goods-quantity {
                            display: flex;
                            align-items: center;
                            gap: 8px;

                            .quantity-btn {
                                width: 32px;
                                height: 32px;
                                border: 1px solid #e0e0e0;
                                background: #fff;
                                border-radius: 4px;
                                cursor: pointer;
                                font-size: 18px;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                transition: all 0.3s;

                                &:hover:not(:disabled) {
                                    border-color: var(--color);
                                    color: var(--color);
                                }

                                &:disabled {
                                    opacity: 0.5;
                                    cursor: not-allowed;
                                }
                            }

                            input[type="number"] {
                                width: 60px;
                                height: 32px;
                                border: 1px solid #e0e0e0;
                                border-radius: 4px;
                                text-align: center;
                                font-size: 14px;

                                &:disabled {
                                    background-color: #f5f5f5;
                                    cursor: not-allowed;
                                    opacity: 0.6;
                                }
                            }
                        }

                        .goods-quantity-label {
                            font-size: 14px;
                            color: #606266;
                            font-weight: 600;
                            min-width: 40px;
                            text-align: right;
                        }
                    }
                }
            }

            .modal-footer {
                padding: 20px 24px;
                border-top: 1px solid #e0e0e0;
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 24px;

                .quantity-control {
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    font-size: 14px;
                    color: #606266;

                    .quantity-selector {
                        display: flex;
                        align-items: center;
                        gap: 8px;

                        .quantity-btn {
                            width: 32px;
                            height: 32px;
                            border: 1px solid #e0e0e0;
                            background: #fff;
                            border-radius: 4px;
                            cursor: pointer;
                            font-size: 18px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            transition: all 0.3s;

                            &:hover:not(:disabled) {
                                border-color: var(--color);
                                color: var(--color);
                            }

                            &:disabled {
                                opacity: 0.5;
                                cursor: not-allowed;
                            }
                        }

                        input[type="number"] {
                            width: 60px;
                            height: 32px;
                            border: 1px solid #e0e0e0;
                            border-radius: 4px;
                            text-align: center;
                            font-size: 14px;
                        }
                    }
                }

                .price-info {
                    flex: 1;
                    display: flex;
                    flex-direction: column;
                    gap: 8px;

                    .combination-price {
                        font-size: 18px;
                        color: var(--color);
                        font-weight: 700;
                    }

                    .saved-price {
                        font-size: 14px;
                        color: #909399;
                    }
                }

                .buy-btn {
                    padding: 12px 32px;
                    background: var(--color);
                    color: #fff;
                    border: none;
                    border-radius: 8px;
                    font-size: 16px;
                    font-weight: 700;
                    cursor: pointer;
                    transition: all 0.3s;

                    &:hover:not(:disabled) {
                        background: color-mix(in srgb, var(--color) 85%, black);
                        transform: translateY(-2px);
                        box-shadow: 0 4px 12px color-mix(in srgb, var(--color) 30%, transparent);
                    }

                    &:disabled {
                        background: #ccc;
                        cursor: not-allowed;
                        opacity: 0.6;
                    }
                }
            }
        }
    }
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

// 响应式设计
@media (max-width: 1400px) {
    .combination-page {
        .product-list {
            grid-template-columns: repeat(4, 1fr);
        }
    }
}

@media (max-width: 1200px) {
    .combination-page {
        padding: 16px;

        .product-list {
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
            padding: 20px;
        }
    }
}

@media (max-width: 992px) {
    .combination-page {
        .product-list {
            grid-template-columns: repeat(2, 1fr);
        }
    }
}

@media (max-width: 768px) {
    .combination-page {
        padding: 12px;

        .page-header {
            flex-direction: column;
            gap: 16px;
            align-items: flex-start;
            padding: 20px;

            .header-right {
                flex-wrap: wrap;
            }
        }

        .product-list {
            grid-template-columns: repeat(2, 1fr);
            gap: 12px;
            padding: 12px;
        }
    }
}
</style>
