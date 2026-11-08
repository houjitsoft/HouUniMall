<template>
    <div class="discount-page">
        <!-- 顶部横幅区域 -->
        <div class="discount-header">
            <div class="header-left">
                <span class="clock-icon">⏰</span>
                <span class="discount-title">{{ $t('限时折扣') }}</span>
            </div>
            <div class="header-right">
                <div class="sort-tabs">
                    <div class="sort-tab-item" :class="{ active: currentSort === 'comprehensive' }"
                        @click="handleSortChange('comprehensive')">{{ $t('综合') }}</div>
                    <div class="sort-tab-item" :class="{ active: currentSort === 'comment' }"
                        @click="handleSortChange('comment')">
                        <span class="comment-label">{{ $t('评价') }}</span>
                        <span class="comment-arrows" @click.stop>
                            <span class="comment-arrow up" :class="{ on: commentOrder === 2 }"
                                @click="setCommentOrder(2)">▲</span>
                            <span class="comment-arrow down" :class="{ on: commentOrder === 1 }"
                                @click="setCommentOrder(1)">▼</span>
                        </span>
                    </div>
                    <div class="sort-tab-item" :class="{ active: currentSort === 'price' }"
                        @click="handleSortChange('price')">
                        <span class="price-label">{{ $t('价格') }}</span>
                        <span class="price-arrows" @click.stop>
                            <span class="price-arrow up" :class="{ on: priceOrder === 2 }"
                                @click="setPriceOrder(2)">▲</span>
                            <span class="price-arrow down" :class="{ on: priceOrder === 1 }"
                                @click="setPriceOrder(1)">▼</span>
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 商品列表区域 -->
        <div class="product-list" v-if="!loading && products.length > 0">
            <div class="product-item" v-for="product in products" :key="product.goodsId"
                @click="handleProductClick(product)">
                <!-- 商品图片 -->
                <div class="product-image">
                    <img :src="product.commonImage" :alt="product.commonName" />
                    <!-- 倒计时条（固定在图片底部） -->
                    <div class="countdown-bar">
                        <span class="countdown-icon">⏰</span>
                        <span class="countdown-text">{{ $t('距离结束') }} {{ formatCountdown(product.endTime) }}</span>
                    </div>
                </div>

                <!-- 价格信息 -->
                <div class="product-price">
                    <span class="current-price" v-if="product.activityPrice">
                        {{ formatPriceWithSymbolSafe(product.activityPrice) }}
                    </span>
                    <span class="original-price" v-if="product.commonPrice">
                        {{ formatPriceWithSymbolSafe(product.commonPrice) }}
                    </span>
                </div>

                <!-- 商品名称 -->
                <div class="product-name">{{ $t(product.commonName || '') }}</div>

                <!-- 评价和销量信息 -->
                <div class="product-stats">
                    <span class="comment-count-tag">{{ product.commonEvaluate || 0 }}{{ $t('条好评') }}</span>
                    <span class="sales-text">{{ $t('销售') }}: {{ product.commonSalenum || 0 }}{{ $t('件') }}</span>
                </div>

                <!-- 店铺名称 -->
                <div class="shop-name">{{ $t(product.shopName || '') }}</div>
            </div>
        </div>

        <!-- 加载状态 -->
        <Loading v-if="loading" :text="$t('正在加载...')" />

        <!-- 空状态 -->
        <EmptyState v-if="!loading && products.length === 0" :text="$t('暂无折扣商品')" />

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="!loading && totalCount > 0">
            <div class="pagination-info">
                <UPagination v-model:page="currentPage" :total="totalCount" :items-per-page="pageSize"
                    class="pagination-right" @update:page="handlePageChange" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { queryAllZheKou } from '~/services/activity.service'
import { getToken } from '~/utils/auth'
import type { DiscountProduct } from '~/types/activity'

// 响应式数据
const products = ref<DiscountProduct[]>([])
const loading = ref(false)
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(60)
const currentSort = ref<'comprehensive' | 'comment' | 'price'>('comprehensive')
// 评价排序：1/2（与后端约定）
const commentOrder = ref<1 | 2>(2)
// 价格排序：1降序，2升序
const priceOrder = ref<1 | 2>(2)
const currentTime = ref(Date.now()) // 当前时间，用于倒计时计算

// 倒计时定时器
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 格式化倒计时
const formatCountdown = (endTime: string): string => {
    if (!endTime) return '0时0分0秒'

    const end = new Date(endTime).getTime()
    const now = currentTime.value
    const diff = Math.max(0, end - now)

    if (diff === 0) return '已结束'

    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)

    return `${hours}时${minutes}分${seconds}秒`
}

// 获取排序参数
const getSortParams = () => {
    const params: Record<string, any> = {
        page: currentPage.value,
        limit: pageSize.value
    }

    switch (currentSort.value) {
        case 'comprehensive':
            params.orderType = 3
            params.commentOrder = 1
            break
        case 'comment':
            params.orderType = 5
            params.commentOrder = commentOrder.value
            break
        case 'price':
            params.orderType = 1
            params.priceOrder = priceOrder.value // 价格排序：1降序，2升序
            break
    }

    return params
}

// 获取商品列表
const fetchProducts = async () => {
    try {
        loading.value = true
        const token = getToken()

        const params = {
            token: token || null,
            minPrice: '',
            maxPrice: '',
            ...getSortParams()
        }

        const res = await queryAllZheKou<DiscountProduct>(params)

        if (res && res.code === 1 && res.data) {
            products.value = res.data.list || []
            totalCount.value = res.data.totalCount || 0

            // 启动倒计时更新
            startCountdownTimer()
        } else {
            products.value = []
            totalCount.value = 0
        }
    } catch (error) {
        console.error('获取折扣商品失败:', error)
        products.value = []
        totalCount.value = 0
    } finally {
        loading.value = false
    }
}

// 启动倒计时定时器
const startCountdownTimer = () => {
    // 清除之前的定时器
    clearCountdownTimer()

    // 创建单个定时器更新当前时间
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
const handleSortChange = (sort: 'comprehensive' | 'comment' | 'price') => {
    if (currentSort.value === sort) {
        if (sort === 'price') {
            // 价格 Tab 再次点击：在升/降之间切换
            priceOrder.value = priceOrder.value === 2 ? 1 : 2
            currentPage.value = 1
            fetchProducts()
        } else if (sort === 'comment') {
            // 评价 Tab 再次点击：在两种顺序间切换
            commentOrder.value = commentOrder.value === 2 ? 1 : 2
            currentPage.value = 1
            fetchProducts()
        }
        return
    }
    currentSort.value = sort
    currentPage.value = 1
    fetchProducts()
}

const setCommentOrder = (order: 1 | 2) => {
    if (commentOrder.value === order && currentSort.value === 'comment') return
    commentOrder.value = order
    if (currentSort.value !== 'comment') {
        currentSort.value = 'comment'
    }
    currentPage.value = 1
    fetchProducts()
}

const setPriceOrder = (order: 1 | 2) => {
    if (priceOrder.value === order && currentSort.value === 'price') return
    priceOrder.value = order
    if (currentSort.value !== 'price') {
        currentSort.value = 'price'
    }
    currentPage.value = 1
    fetchProducts()
}

// 分页切换
const handlePageChange = (page: number) => {
    currentPage.value = page
    fetchProducts()
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 商品点击
const handleProductClick = (product: DiscountProduct) => {
    navigateTo(`/product/${product.goodsId}`)
}

// 页面加载时获取数据
onMounted(() => {
    fetchProducts()
})

// 组件卸载时清理定时器
onBeforeUnmount(() => {
    clearCountdownTimer()
})
</script>

<style lang="scss" scoped>
.discount-page {
    width: 100%;
    min-height: 100vh;
    background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
    padding: 32px 0;
    margin: 0 auto;

    .discount-header {
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

            .clock-icon {
                font-size: 28px;
                filter: drop-shadow(0 1px 4px rgba(0, 0, 0, 0.1));
                opacity: 0.95;
            }

            .discount-title {
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
                display: inline-flex;
                align-items: center;
                gap: 8px;

                &:hover {
                    color: var(--color);
                    background: color-mix(in srgb, var(--color) 10%, #fff);
                }

                &.active {
                    color: #fff;
                    background: var(--color);
                    box-shadow: 0 1px 4px color-mix(in srgb, var(--color) 30%, transparent);
                }

                .price-arrows {
                    display: inline-flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    gap: 2px;
                    line-height: 1;
                }

                .price-arrow {
                    font-size: 10px;
                    opacity: 0.55;
                    transition: opacity 0.15s ease, transform 0.15s ease;
                    user-select: none;
                }

                .price-arrow:hover {
                    opacity: 0.95;
                    transform: translateY(-0.2px);
                }

                .price-arrow.on {
                    opacity: 1;
                }

                .comment-arrows {
                    display: inline-flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    gap: 2px;
                    line-height: 1;
                }

                .comment-arrow {
                    font-size: 10px;
                    opacity: 0.55;
                    transition: opacity 0.15s ease, transform 0.15s ease;
                    user-select: none;
                }

                .comment-arrow:hover {
                    opacity: 0.95;
                    transform: translateY(-0.2px);
                }

                .comment-arrow.on {
                    opacity: 1;
                }
            }
        }
    }

    .product-list {
        display: grid;
        grid-template-columns: repeat(5, 1fr);
        gap: 20px;
        padding: 28px;
        background: #fff;
        border-radius: 0 0 16px 16px;
        box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);

        .product-item {
            display: flex;
            flex-direction: column;
            background: #fff;
            border: 1.5px solid #f0f2f5;
            border-radius: 12px;
            overflow: hidden;
            cursor: pointer;
            transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

            &::after {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                height: 3px;
                background: linear-gradient(90deg,
                        var(--color, #0a8cf0) 0%,
                        transparent 100%);
                opacity: 0;
                transition: opacity 0.3s ease;
            }

            &:hover {
                border-color: var(--color, #0a8cf0);
                box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12),
                    0 4px 12px var(--rgba2, rgba(10, 140, 240, 0.2));
                transform: translateY(-6px) scale(1.02);

                &::after {
                    opacity: 1;
                }

                .product-image {
                    &::before {
                        opacity: 1;
                    }
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

                &::before {
                    content: '';
                    position: absolute;
                    inset: 0;
                    background: linear-gradient(135deg,
                            color-mix(in srgb, var(--color) 15%, transparent) 0%,
                            transparent 100%);
                    opacity: 0;
                    transition: opacity 0.3s ease;
                    z-index: 1;
                }

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: contain;
                    transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                    position: relative;
                    z-index: 0;
                }

                &:hover img {
                    transform: scale(1.08);
                }

                .countdown-bar {
                    position: absolute;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    z-index: 2;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 6px;
                    padding: 6px 12px;
                    background: linear-gradient(180deg, transparent 0%, color-mix(in srgb, var(--color) 88%, black) 100%);
                    color: #fff;
                    font-size: 12px;
                    font-weight: 600;
                    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.4);

                    .countdown-icon {
                        font-size: 14px;
                        opacity: 0.95;
                    }

                    .countdown-text {
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        max-width: 100%;
                    }
                }
            }

            .product-stats {
                padding: 12px 14px;
                font-size: 12px;
                display: flex;
                align-items: center;
                gap: 12px;
                background: linear-gradient(180deg, #fafbfc 0%, #ffffff 100%);
                border-top: 1px solid #f0f2f5;

                .comment-count-tag {
                    display: inline-flex;
                    align-items: center;
                    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
                    color: #2e7d32;
                    padding: 4px 10px;
                    border-radius: 6px;
                    font-weight: 600;
                    box-shadow: 0 2px 4px rgba(46, 125, 50, 0.15);
                    transition: transform 0.2s ease;

                    &:hover {
                        transform: scale(1.05);
                    }
                }

                .sales-text {
                    color: #909399;
                    font-weight: 500;
                }
            }

            .product-name {
                padding: 12px 14px;
                font-size: 14px;
                color: #303133;
                line-height: 1.6;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                height: 58px;
                font-weight: 700;
                transition: color 0.2s ease;
            }

            .product-price {
                padding: 12px 14px 0;
                display: flex;
                align-items: baseline;
                gap: 12px;

                .current-price {
                    font-size: 22px;
                    font-weight: 800;
                    color: var(--color);
                    line-height: 1;
                    display: flex;
                    align-items: baseline;
                    gap: 2px;

                    .currency-symbol {
                        color: var(--color);
                        font-size: 18px;
                        font-weight: 700;
                    }
                }

                .original-price {
                    font-size: 14px;
                    color: #c0c4cc;
                    text-decoration: line-through;
                    font-weight: 400;
                    display: flex;
                    align-items: baseline;
                    gap: 2px;

                    .currency-symbol {
                        color: #c0c4cc;
                    }
                }
            }

            .shop-name {
                padding: 0 14px 14px;
                font-size: 12px;
                color: #909399;
                font-weight: 500;
                display: flex;
                align-items: center;
                gap: 4px;

                &::before {
                    content: '🏪';
                    font-size: 14px;
                }
            }
        }
    }

    .pagination-wrapper {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: 32px;
        padding: 24px;
        background: #fff;
        border-radius: 16px;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);

        .pagination-info {
            font-size: 14px;
            color: #606266;
            font-weight: 500;

            .pagination-right {
                .nuxt-ui-pagination {
                    .nuxt-ui-pagination__items {
                        gap: 10px;
                    }
                }
            }
        }
    }
}

// 响应式设计
@media (max-width: 1600px) {
    .discount-page {
        padding: 20px;
    }
}

@media (max-width: 1400px) {
    .discount-page {
        .product-list {
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
        }
    }
}

@media (max-width: 1200px) {
    .discount-page {
        padding: 16px;

        .product-list {
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
            padding: 20px;
        }

        .discount-header {
            padding: 20px 24px;

            .header-left .discount-title {
                font-size: 24px;
            }
        }
    }
}

@media (max-width: 992px) {
    .discount-page {
        .product-list {
            grid-template-columns: repeat(2, 1fr);
            gap: 16px;
        }

        .discount-header {
            flex-direction: column;
            gap: 16px;
            align-items: flex-start;
            padding: 20px;

            .header-right {
                width: 100%;
                justify-content: flex-start;
                gap: 24px;
            }
        }
    }
}

@media (max-width: 768px) {
    .discount-page {
        padding: 12px;

        .product-list {
            grid-template-columns: repeat(2, 1fr);
            gap: 12px;
            padding: 12px;
        }

        .discount-header {
            padding: 16px;
            border-radius: 8px 8px 0 0;

            .header-left {
                .clock-icon {
                    font-size: 24px;
                }

                .discount-title {
                    font-size: 20px;
                }
            }

            .header-right {
                gap: 16px;
                flex-wrap: wrap;

                .sort-tabs .sort-tab-item {
                    font-size: 14px;
                    padding: 6px 14px;
                }
            }
        }

        .pagination-wrapper {
            padding: 16px;
            justify-content: center;
        }
    }
}

@media (max-width: 480px) {
    .discount-page {
        .product-list {
            grid-template-columns: 1fr;
            gap: 12px;
        }

        .product-item {
            .product-price .current-price {
                font-size: 18px;
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

@keyframes pulse-glow {

    0%,
    100% {
        opacity: 1;
        transform: scale(1);
    }

    50% {
        opacity: 0.8;
        transform: scale(1.05);
    }
}

@keyframes shine {
    0% {
        transform: translateX(-100%) translateY(-100%) rotate(45deg);
    }

    100% {
        transform: translateX(100%) translateY(100%) rotate(45deg);
    }
}
</style>
