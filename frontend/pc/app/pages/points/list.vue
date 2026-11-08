<template>
    <div class="points-list-page">
        <div class="points-list-container">
            <!-- 顶部导航和搜索区域 -->
            <div class="points-list-header">
                <!-- 左侧：排序选项卡 -->
                <div class="sort-tabs">
                    <div class="sort-tab" :class="{ 'sort-tab--active': currentSort === 'comprehensive' }"
                        @click="handleSortChange('comprehensive')">
                        {{ $t('综合') }}
                    </div>
                    <div class="sort-tab" :class="{ 'sort-tab--active': currentSort === 'price' }"
                        @click="handleSortChange('price')">
                        {{ $t('价格') }}
                    </div>
                </div>

                <!-- 右侧：搜索框 -->
                <div class="search-section">
                    <input v-model="searchKeyword" type="text" class="search-input" :placeholder="$t('搜索')"
                        @keyup.enter="handleSearch" />
                    <div class="search-icon" @click="handleSearch">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path
                                d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z"
                                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                                stroke-linejoin="round" />
                        </svg>
                    </div>
                </div>
            </div>

            <!-- 商品列表区域 -->
            <div class="goods-list-section">
                <!-- 加载状态 -->
                <Loading v-if="isLoading" :text="$t('加载中...')" />

                <!-- 商品网格 -->
                <div v-else-if="goodsList.length > 0" class="goods-grid">
                    <div v-for="goods in goodsList" :key="goods.goodsId || goods.commonId" class="goods-card"
                        @click="handleGoodsClick(goods)">
                        <!-- 商品图片 -->
                        <div class="goods-image">
                            <img :src="goods.commonImage" :alt="goods.commonName" @error="handleImageError" />
                        </div>

                        <!-- 商品名称 -->
                        <div class="goods-name" :title="goods.commonName">
                            {{ $t(goods.commonName || '') }}
                        </div>

                        <!-- 兑换价 -->
                        <div class="goods-exchange-price">
                            <span class="exchange-label">{{ $t('兑换价') }}:</span>
                            <span class="exchange-value">
                                {{ formatNumber(goods.needIntegral || 0) }} {{ $t('积分') }}
                                <span v-if="hasAdditionalPrice(goods)" class="price-additional">
                                    + {{ formatPriceWithSymbolSafe(getAdditionalPrice(goods)) }}
                                </span>
                            </span>
                        </div>

                        <!-- 参考价 -->
                        <div class="goods-reference-price">
                            <span class="reference-label">{{ $t('参考价') }}:</span>
                            <span class="reference-value">{{ formatPriceWithSymbolSafe(goods.commonMarketPrice)
                                }}</span>
                        </div>
                    </div>
                </div>

                <!-- 空状态 -->
                <EmptyState v-else :text="$t('抱歉，没有找到相关商品')" :description="$t('您可以尝试调整搜索关键词或筛选条件')" :icon-size="80"
                    :compact="false" />

                <!-- 分页 -->
                <div v-if="totalCount > 0 && goodsList.length > 0" class="pagination-section">
                    <UPagination v-model:page="currentPage" :total="totalCount" :items-per-page="pageSize"
                        @update:page="handlePageChange" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { queryGoodsFilter } from '~/services/goods.service'
import { formatPriceDisplay, formatNumber } from '~/composables/useFormatPrice'
import EmptyState from '~/components/Empty/EmptyState.vue'
import Loading from '~/components/Load/Loading.vue'
import type { GoodsFilterQueryItem, GoodsFilterQueryParams } from '~/types/goods'

// ==================== 路由和状态 ====================
const route = useRoute()
const router = useRouter()

// ==================== 响应式状态 ====================
// 排序状态：'comprehensive' 综合排序，'price' 价格排序
const currentSort = ref<'comprehensive' | 'price'>('comprehensive')
// 价格排序方向：1 升序，2 降序
const priceOrder = ref<1 | 2>(1)

// 分类 ID
const selectedCatId = ref<string>('')

// 搜索关键词
const searchKeyword = ref<string>('')

// 分页状态
const currentPage = ref(1)
const pageSize = ref(20)
const totalCount = ref(0)

// 商品列表和加载状态
const goodsList = ref<GoodsFilterQueryItem[]>([])
const isLoading = ref(false)

// ==================== 计算属性 ====================
/**
 * 判断商品是否有额外价格
 * @param goods 商品信息
 * @returns 是否有额外价格
 */
const hasAdditionalPrice = (goods: GoodsFilterQueryItem): boolean => {
    const price = getAdditionalPrice(goods)
    return price > 0
}

/**
 * 获取商品的额外价格
 * @param goods 商品信息
 * @returns 额外价格
 */
const getAdditionalPrice = (goods: GoodsFilterQueryItem): number => {
    const price = goods.goodsPrice || goods.commonPrice || '0'
    if (price === '*' || price === '' || price === null || price === undefined) {
        return 0
    }
    const priceNum = Number(price)
    return isNaN(priceNum) ? 0 : priceNum
}

// ==================== 工具函数 ====================
/**
 * 处理图片加载错误
 * @param e 错误事件
 */
const handleImageError = (e: Event): void => {
    const img = e.target as HTMLImageElement
    img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5pyq5Yqg6L29PC90ZXh0Pjwvc3ZnPg=='
}

// ==================== 事件处理 ====================
/**
 * 处理排序变化
 * @param sortType 排序类型
 */
const handleSortChange = (sortType: 'comprehensive' | 'price'): void => {
    if (currentSort.value === sortType && sortType === 'price') {
        // 如果已经是价格排序，切换排序方向
        priceOrder.value = priceOrder.value === 1 ? 2 : 1
    } else {
        // 切换到新的排序类型
        currentSort.value = sortType
        if (sortType === 'price') {
            priceOrder.value = 1
        }
    }
    currentPage.value = 1
    fetchGoodsList()
}

/**
 * 处理搜索
 */
const handleSearch = (): void => {
    currentPage.value = 1
    fetchGoodsList()
}

/**
 * 处理分页变化
 * @param page 页码
 */
const handlePageChange = (page: number): void => {
    currentPage.value = page
    fetchGoodsList()
    // 滚动到顶部
    if (typeof window !== 'undefined') {
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }
}

/**
 * 处理商品点击
 * @param goods 商品信息
 */
const handleGoodsClick = (goods: GoodsFilterQueryItem): void => {
    const goodsId = goods.goodsId || goods.commonId
    if (goodsId) {
        router.push(`/points/${goodsId}`)
    }
}

// ==================== API处理 ====================
/**
 * 获取商品列表
 */
const fetchGoodsList = async (): Promise<void> => {
    isLoading.value = true

    try {
        const params: GoodsFilterQueryParams = {
            token: null,
            brandId: '',
            catId: selectedCatId.value,
            commonName: searchKeyword.value || '',
            minPrice: '',
            maxPrice: '',
            shopSelfSupport: '',
            orderType: currentSort.value === 'comprehensive' ? 5 : undefined,
            commentOrder: currentSort.value === 'comprehensive' ? 1 : undefined,
            priceOrder: currentSort.value === 'price' ? String(priceOrder.value) : '',
            saleNum: '',
            currentPage: currentPage.value,
            pageSize: pageSize.value,
            integralGoods: 0, // 积分商品标识
            commonIsRecommend: undefined
        }

        const response = await queryGoodsFilter(params)

        if (response?.code === 1 && response.data) {
            goodsList.value = response.data.list || []
            totalCount.value = response.data.totalCount || 0
        } else {
            goodsList.value = []
            totalCount.value = 0
        }
    } catch (error: any) {
        console.error('获取商品列表失败:', error)
        goodsList.value = []
        totalCount.value = 0
    } finally {
        isLoading.value = false
    }
}

// ==================== 生命周期 ====================
// 监听路由参数变化
watch(() => route.query, (newQuery) => {
    // 从URL参数初始化搜索关键词
    const keyword = newQuery.keyword as string | undefined
    if (keyword !== undefined) {
        searchKeyword.value = decodeURIComponent(keyword)
    }

    // 从URL参数初始化排序
    const sort = newQuery.sort as string | undefined
    if (sort === 'price') {
        currentSort.value = 'price'
        const order = newQuery.priceOrder as string | undefined
        if (order === '2') {
            priceOrder.value = 2
        } else {
            priceOrder.value = 1
        }
    } else {
        currentSort.value = 'comprehensive'
    }

    // 从URL参数初始化分类
    const catId = (newQuery.catId || newQuery.catid) as string | undefined
    selectedCatId.value = catId || ''

    // 从URL参数初始化页码
    const page = newQuery.page as string | undefined
    if (page) {
        const pageNum = Number(page)
        if (!isNaN(pageNum) && pageNum > 0) {
            currentPage.value = pageNum
        }
    }

    fetchGoodsList()
}, { immediate: true })

// 组件挂载时获取数据
onMounted(() => {
    fetchGoodsList()
})
</script>

<style lang="scss" scoped>
.points-list-page {
    width: 100%;
    min-height: calc(100vh - 200px);
    background-color: #f5f5f5;
    padding: 20px 0 60px;

    .points-list-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }

    // 顶部导航和搜索区域
    .points-list-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 30px;
        padding: 20px;
        background-color: #fff;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

        // 排序选项卡
        .sort-tabs {
            display: flex;
            align-items: center;
            gap: 0;

            .sort-tab {
                padding: 8px 20px;
                font-size: 14px;
                color: #666;
                cursor: pointer;
                transition: all 0.3s ease;
                position: relative;
                user-select: none;

                &:hover {
                    color: var(--color);
                }

                &--active {
                    color: var(--color);
                    font-weight: 600;

                    &::after {
                        content: '';
                        position: absolute;
                        bottom: 0;
                        left: 50%;
                        transform: translateX(-50%);
                        width: 30px;
                        height: 2px;
                        background-color: var(--color);
                    }
                }
            }
        }

        // 搜索区域
        .search-section {
            display: flex;
            align-items: center;
            gap: 8px;
            position: relative;

            .search-input {
                width: 300px;
                height: 36px;
                padding: 0 40px 0 12px;
                border: 1px solid #e0e0e0;
                border-radius: 4px;
                font-size: 14px;
                outline: none;
                transition: all 0.3s ease;

                &:focus {
                    border-color: var(--color);
                    box-shadow: 0 0 0 2px color-mix(in srgb, var(--color) 15%, transparent);
                }

                &::placeholder {
                    color: #999;
                }
            }

            .search-icon {
                position: absolute;
                right: 12px;
                top: 50%;
                transform: translateY(-50%);
                width: 20px;
                height: 20px;
                color: #999;
                cursor: pointer;
                transition: color 0.3s ease;

                &:hover {
                    color: var(--color);
                }

                svg {
                    width: 100%;
                    height: 100%;
                }
            }
        }
    }

    // 商品列表区域
    .goods-list-section {
        width: 100%;

        // 商品网格
        .goods-grid {
            display: grid;
            grid-template-columns: repeat(5, 1fr);
            gap: 20px;
            margin-bottom: 40px;

            @media (max-width: 1400px) {
                grid-template-columns: repeat(4, 1fr);
            }

            @media (max-width: 1200px) {
                grid-template-columns: repeat(3, 1fr);
            }

            @media (max-width: 768px) {
                grid-template-columns: repeat(2, 1fr);
                gap: 15px;
            }

            @media (max-width: 480px) {
                grid-template-columns: 1fr;
            }

            .goods-card {
                background-color: #fff;
                border-radius: 8px;
                overflow: hidden;
                cursor: pointer;
                transition: all 0.3s ease;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
                border: 1px solid #f0f0f0;
                display: flex;
                flex-direction: column;

                &:hover {
                    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
                    transform: translateY(-4px);
                    border-color: var(--color);
                }

                // 商品图片
                .goods-image {
                    width: 100%;
                    height: 200px;
                    padding: 16px;
                    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    overflow: hidden;

                    img {
                        max-width: 100%;
                        max-height: 100%;
                        width: auto;
                        height: auto;
                        object-fit: contain;
                        transition: transform 0.3s ease;
                    }
                }

                .goods-card:hover .goods-image img {
                    transform: scale(1.05);
                }

                // 商品名称
                .goods-name {
                    padding: 12px 16px 8px;
                    font-size: 14px;
                    color: #222;
                    line-height: 1.5;
                    height: 52px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    line-clamp: 2;
                    -webkit-box-orient: vertical;
                    word-break: break-word;
                }

                // 兑换价
                .goods-exchange-price {
                    padding: 0 16px 8px;
                    display: flex;
                    align-items: baseline;
                    gap: 8px;
                    font-size: 14px;

                    .exchange-label {
                        color: #666;
                        white-space: nowrap;
                    }

                    .exchange-value {
                        color: var(--color);
                        font-weight: 600;
                        flex: 1;
                        display: flex;
                        align-items: baseline;
                        gap: 4px;
                        flex-wrap: wrap;

                        .price-additional {
                            color: var(--color);
                        }
                    }
                }

                // 参考价
                .goods-reference-price {
                    padding: 0 16px 16px;
                    display: flex;
                    align-items: baseline;
                    gap: 8px;
                    font-size: 13px;

                    .reference-label {
                        color: #999;
                        white-space: nowrap;
                    }

                    .reference-value {
                        color: #999;
                        text-decoration: line-through;
                    }
                }
            }
        }

        // 分页
        .pagination-section {
            display: flex;
            justify-content: center;
            align-items: center;
            margin-top: 40px;
            padding: 30px 0 20px;
        }
    }
}

// 动画
@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}
</style>
