<template>
    <div class="goods-filter-list">
        <!-- 筛选栏 -->
        <div class="filter-bar">
            <div class="filter-header">
                <button class="back-btn" @click="handleBack">
                    <span class="back-icon">〈</span>
                </button>
                <div class="filter-title">
                    {{ showRecommendedOnly ? $t('推荐商品') : $t('全部商品') }}
                </div>
            </div>
            <div class="filter-tabs">
                <div class="filter-tab" :class="{ active: orderType === '' || orderType === undefined }"
                    @click="handleOrderChange">
                    {{ $t('综合') }}
                </div>
                <div class="filter-tab" :class="{ active: orderType === 2 && evaluate }" @click="handleEvaluateClick">
                    {{ $t('评价') }}
                    <span class="sort-icon" v-if="evaluate">
                        {{ evaluate === 1 ? '↓' : '↑' }}
                    </span>
                </div>
                <div class="filter-tab" :class="{ active: orderType === 4 && priceOrder }" @click="handlePriceClick">
                    {{ $t('价格') }}
                    <span class="sort-icon" v-if="priceOrder">
                        {{ priceOrder === 1 ? '↓' : '↑' }}
                    </span>
                </div>
                <div class="filter-tab" :class="{ active: orderType === 3 && saleNum }" @click="handleSaleNumClick">
                    {{ $t('销量') }}
                    <span class="sort-icon" v-if="saleNum">
                        {{ saleNum === 1 ? '↓' : '↑' }}
                    </span>
                </div>
            </div>
        </div>

        <!-- 商品列表 -->
        <div class="goods-section">
            <div class="goods-grid" v-if="!loading && products.length > 0">
                <ProductCard v-for="product in products" :key="`${product.goodsId || product.commonId}-${currentPage}`"
                    :product="product" @click="handleProductClick" />
            </div>

            <!-- 加载状态 -->
            <Loading v-if="loading" :text="$t('加载中...')" />

            <!-- 空状态 -->
            <EmptyState v-if="!loading && products.length === 0" :text="$t('暂无商品')"
                :description="$t('该分类下暂时没有商品，请稍后再来查看')" />

            <!-- 分页 -->
            <div class="pagination-wrapper" v-if="!loading && totalCount > 0">
                <UPagination v-model:page="currentPage" :total="totalCount" :items-per-page="pageSize"
                    @update:page="handlePageChange" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import Loading from '~/components/Load/Loading.vue'
import ProductCard from './ProductCard.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import type { ListQueryResultVO } from '~/types/store'

interface Props {
    showRecommendedOnly: boolean
    products: ListQueryResultVO[]
    loading: boolean
    totalCount: number
    pageSize: number
    orderType: string | number
    evaluate?: number
    priceOrder?: number
    saleNum?: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
    back: []
    productClick: [product: ListQueryResultVO]
    pageChange: [page: number]
    orderChange: []
    evaluateClick: []
    priceClick: []
    saleNumClick: []
}>()

const currentPage = defineModel<number>('currentPage', { required: true })

const handleBack = () => {
    emit('back')
}

const handleProductClick = (product: ListQueryResultVO) => {
    emit('productClick', product)
}

const handlePageChange = (page: number) => {
    currentPage.value = page
    emit('pageChange', page)
}

const handleOrderChange = () => {
    emit('orderChange')
}

const handleEvaluateClick = () => {
    emit('evaluateClick')
}

const handlePriceClick = () => {
    emit('priceClick')
}

const handleSaleNumClick = () => {
    emit('saleNumClick')
}
</script>

<style lang="scss" scoped>
.goods-filter-list {
    .filter-bar {
        background-color: #fff;
        border-radius: 8px;
        padding: 16px 20px;
        margin-bottom: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

        .filter-header {
            display: flex;
            align-items: center;
            gap: 16px;
            margin-bottom: 16px;
            padding-bottom: 16px;
            border-bottom: 1px solid #f0f0f0;

            .back-btn {
                display: flex;
                align-items: center;
                gap: 6px;
                padding: 5px 14px 5px 6px;
                border: 1px solid #e0e0e0;
                border-radius: 6px;
                background-color: #fff;
                color: #666;
                font-size: 14px;
                cursor: pointer;
                transition: all 0.3s ease;

                &:hover {
                    border-color: var(--color);
                    color: var(--color);
                    background-color: #f5f5f5;
                }

                .back-icon {
                    font-size: 16px;
                    font-weight: bold;
                }
            }

            .filter-title {
                font-size: 18px;
                font-weight: 600;
                color: #333;
            }
        }

        .filter-tabs {
            display: flex;
            gap: 24px;
            align-items: center;

            .filter-tab {
                display: flex;
                align-items: center;
                gap: 6px;
                padding: 8px 16px;
                border-radius: 6px;
                cursor: pointer;
                font-size: 14px;
                color: #666;
                transition: all 0.3s ease;
                user-select: none;

                &:hover {
                    background-color: #f5f5f5;
                    color: var(--color);
                }

                &.active {
                    background-color: var(--color);
                    color: #fff;

                    .sort-icon {
                        color: #fff;
                    }
                }

                .sort-icon {
                    font-size: 12px;
                    color: #999;
                }
            }
        }
    }

    .goods-section {
        .goods-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 30px;
            animation: fadeInUp 0.5s ease-out;
        }

    }

    .pagination-wrapper {
        display: flex;
        justify-content: center;
        margin-top: 40px;
        padding: 20px 0;
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 响应式设计 */
@media (max-width: 968px) {
    .goods-filter-list {
        .filter-bar {
            padding: 12px 16px;

            .filter-tabs {
                gap: 12px;
                flex-wrap: wrap;

                .filter-tab {
                    padding: 6px 12px;
                    font-size: 13px;
                }
            }
        }

        .goods-section {
            .goods-grid {
                grid-template-columns: repeat(2, 1fr);
                gap: 12px;
            }
        }
    }
}

@media (max-width: 640px) {
    .goods-filter-list {
        .goods-section {
            .goods-grid {
                grid-template-columns: 1fr;
            }

        }
    }
}
</style>
