<template>
    <div class="full-gift-fold-page">
        <!-- 顶部标题与排序 -->
        <div class="page-header">
            <div class="header-left">
                <span class="header-icon">🎁</span>
                <span class="header-title">{{ $t('满减赠折') }}</span>
            </div>
            <div class="header-right">
                <div class="sort-tabs">
                    <div class="sort-tab-item" :class="{ active: currentSortType === 'comprehensive' }"
                        @click="handleSortChange('comprehensive')">{{ $t('综合') }}</div>
                    <div class="sort-tab-item" :class="{ active: currentSortType === 'comment' }"
                        @click="handleSortChange('comment')">{{ $t('评价') }}</div>
                    <div class="sort-tab-item" :class="{ active: currentSortType === 'price' }"
                        @click="handleSortChange('price')">{{ $t('价格') }}</div>
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
                </div>

                <!-- 价格信息 -->
                <div class="product-price">
                    <span class="price-text">
                        {{ formatPriceWithSymbolSafe(product.commonPrice) }}
                    </span>
                </div>

                <!-- 商品名称 -->
                <div class="product-name">{{ $t(product.commonName || '') }}</div>

                <!-- 销售数量 -->
                <div class="product-sales">
                    {{ $t('销售') }}: {{ product.commonSalenum || 0 }}{{ $t('件') }}
                </div>

                <!-- 店铺名称 -->
                <div class="shop-name">{{ $t(product.shopName || '') }}</div>

                <!-- 活动倒计时 -->
                <div class="countdown-info">
                    <span class="countdown-icon">🕐</span>
                    <span class="countdown-text">{{ $t('距结束') }}: {{ formatCountdown(product.endTime) }}</span>
                </div>
            </div>
        </div>

        <!-- 加载状态 -->
        <Loading v-if="loading" :text="$t('正在加载...')" />

        <!-- 空状态 -->
        <EmptyState v-if="!loading && products.length === 0" :text="$t('暂无商品')" />

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
import { queryAllManJianZengZhe } from '~/services/activity.service'
import type { ManJianZengZheProduct, SortType } from '~/types/activity'

// 响应式数据
const products = ref<ManJianZengZheProduct[]>([])
const loading = ref(false)
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(60)
const currentSortType = ref<SortType>('comprehensive')
const currentTime = ref(Date.now()) // 当前时间，用于倒计时计算

// 倒计时定时器
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 格式化倒计时
const formatCountdown = (endTime: string): string => {
    if (!endTime) return '00天 00时 00分 00秒'

    const end = new Date(endTime).getTime()
    const now = currentTime.value
    const diff = Math.max(0, end - now)

    if (diff <= 0) return '00天 00时 00分 00秒'

    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)

    return `${String(days).padStart(2, '0')}天 ${String(hours).padStart(2, '0')}时 ${String(minutes).padStart(2, '0')}分 ${String(seconds).padStart(2, '0')}秒`
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

    // 根据排序类型设置请求参数
    fetchProducts()
}

// 获取排序参数
const getSortParams = () => {
    const params: {
        orderType?: number
        priceOrder?: string
        saleNum?: string
        commentOrder?: number
    } = {}

    switch (currentSortType.value) {
        case 'comprehensive':
            params.orderType = 5
            params.commentOrder = 1
            break
        case 'comment':
            params.commentOrder = 1
            break
        case 'price':
            params.priceOrder = 'asc'
            break
    }

    return params
}

// 获取商品列表
const fetchProducts = async () => {
    try {
        loading.value = true

        const sortParams = getSortParams()
        const params: {
            minPrice: string
            maxPrice: string
            orderType: number
            priceOrder: string
            saleNum: string
            page: number
            limit: number
            commentOrder: number
        } = {
            minPrice: '',
            maxPrice: '',
            orderType: sortParams.orderType ?? 5,
            priceOrder: sortParams.priceOrder ?? '',
            saleNum: sortParams.saleNum ?? '',
            page: currentPage.value,
            limit: pageSize.value,
            commentOrder: sortParams.commentOrder ?? 1
        }

        const res = await queryAllManJianZengZhe<ManJianZengZheProduct>(params)

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
        console.error('获取满减赠折商品失败:', error)
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
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 商品点击
const handleProductClick = (product: ManJianZengZheProduct) => {
    navigateTo(`/product/${product.goodsId}`)
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
.full-gift-fold-page {
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
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06),
                0 1px 4px rgba(0, 0, 0, 0.04);

            &::after {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                height: 4px;
                background: linear-gradient(90deg,
                        var(--color) 0%,
                        color-mix(in srgb, var(--color) 60%, transparent) 50%,
                        transparent 100%);
                opacity: 0;
                transition: opacity 0.4s ease;
            }

            &:hover {
                border-color: var(--color);
                box-shadow: 0 16px 40px rgba(0, 0, 0, 0.14),
                    0 6px 16px color-mix(in srgb, var(--color) 25%, transparent);
                transform: translateY(-8px) scale(1.02);

                &::after {
                    opacity: 1;
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
                flex-shrink: 0;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: contain;
                    transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                    display: block;
                }

                &:hover {
                    img {
                        transform: scale(1.08);
                    }
                }
            }

            .product-price {
                padding: 14px 16px 6px;
                display: flex;
                align-items: baseline;
                min-height: 40px;

                .price-text {
                    font-size: 22px;
                    font-weight: 800;
                    color: var(--color);
                    line-height: 1.2;
                    display: flex;
                    align-items: baseline;
                    gap: 2px;

                    .currency-symbol {
                        color: var(--color);
                        font-size: 18px;
                        font-weight: 700;
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
                height: calc(14px * 1.5 * 2); // 两行字体高度：字体大小 * 行高 * 行数
                font-weight: 600;
                transition: color 0.3s ease;
                word-break: break-word;
                text-overflow: ellipsis;

                &:hover {
                    color: var(--color);
                }
            }

            .product-sales {
                padding: 0 16px 6px;
                font-size: 12px;
                color: #909399;
                font-weight: 500;
                line-height: 1.5;
            }

            .shop-name {
                padding: 0 16px 6px;
                font-size: 12px;
                color: #909399;
                font-weight: 500;
                line-height: 1.5;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .countdown-info {
                padding: 10px 16px;
                display: flex;
                align-items: center;
                gap: 6px;
                background: linear-gradient(180deg, #fafbfc 0%, #ffffff 100%);
                border-top: 1px solid #f0f2f5;
                margin-top: auto;
                min-height: 36px;

                .countdown-icon {
                    font-size: 14px;
                    flex-shrink: 0;
                }

                .countdown-text {
                    font-size: 12px;
                    color: var(--color);
                    font-weight: 600;
                    flex: 1;
                    line-height: 1.4;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                }
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
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06),
            0 1px 3px rgba(0, 0, 0, 0.04);

        .pagination-info {
            font-size: 14px;
            color: #606266;
            font-weight: 500;

            .pagination-right {
                .nuxt-ui-pagination {
                    .nuxt-ui-pagination__items {
                        gap: 12px;
                    }
                }
            }
        }
    }
}

// 响应式设计
@media (max-width: 1600px) {
    .full-gift-fold-page {
        padding: 20px;
    }
}

@media (max-width: 1400px) {
    .full-gift-fold-page {
        .product-list {
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
        }
    }
}

@media (max-width: 1200px) {
    .full-gift-fold-page {
        padding: 16px;

        .product-list {
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
            padding: 20px;
        }

        .page-header {
            padding: 20px 24px;

            .header-left .header-title {
                font-size: 24px;
            }
        }
    }
}

@media (max-width: 992px) {
    .full-gift-fold-page {
        .product-list {
            grid-template-columns: repeat(2, 1fr);
            gap: 16px;
        }

        .page-header {
            flex-direction: column;
            gap: 16px;
            align-items: flex-start;
            padding: 20px;
        }
    }
}

@media (max-width: 768px) {
    .full-gift-fold-page {
        padding: 12px;

        .product-list {
            grid-template-columns: repeat(2, 1fr);
            gap: 12px;
            padding: 12px;
        }

        .page-header {
            padding: 16px;
            border-radius: 8px 8px 0 0;

            .header-left {
                .header-icon {
                    font-size: 24px;
                }

                .header-title {
                    font-size: 20px;
                }
            }
        }

        .sort-tabs {
            .sort-tab-item {
                padding: 12px 20px;
                font-size: 14px;
            }
        }

        .pagination-wrapper {
            padding: 16px;
        }
    }
}

@media (max-width: 480px) {
    .full-gift-fold-page {
        .product-list {
            grid-template-columns: 1fr;
            gap: 12px;
        }

        .product-item {
            .product-price .price-text {
                font-size: 18px;
            }

            .product-name {
                height: calc(14px * 1.5 * 2); // 保持两行高度
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
</style>
