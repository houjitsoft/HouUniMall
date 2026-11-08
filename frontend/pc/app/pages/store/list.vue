<template>
    <div class="store-list-page">
        <div class="store-list-container">
            <!-- 顶部筛选和排序区域 -->
            <div class="filter-bar">
                <div class="filter-left">
                    <button class="sort-btn" :class="{ active: orderType === 1 }" @click="handleSortChange(1)">
                        {{ $t('默认排序') }}
                    </button>
                    <button class="sort-btn" :class="{ active: orderType === 2 }" @click="handleSortChange(2)">
                        {{ $t('收藏量') }}
                    </button>
                </div>
                <div class="filter-right">
                    <label class="checkbox-label">
                        <input type="checkbox" v-model="shopSelfSupport" @change="handleFilterChange" />
                        <span>{{ $t('平台自营') }}</span>
                    </label>
                </div>
            </div>

            <!-- 店铺列表 -->
            <div class="store-list" v-if="!loading && shopList.length > 0">
                <div class="store-card" v-for="shop in shopList" :key="shop.shopId">
                    <div class="store-card-content">
                        <!-- 左侧：店铺信息区域 -->
                        <div class="store-left">
                            <!-- 店铺头部信息 -->
                            <div class="store-header" @click="handleStoreClick(shop)">
                                <div class="store-logo-wrapper">
                                    <div class="store-logo">
                                        <img :src="shop.shopLogo || shop.wapShopLogo" :alt="shop.shopName" />
                                    </div>
                                </div>
                                <div class="store-info">
                                    <div class="store-name-row">
                                        <h3 class="store-name" v-html="$t(shop.shopName || '')"></h3>
                                        <div class="store-tags">
                                            <span v-if="shop.shopSelfSupport === 'true'" class="self-support-tag">{{
                                                $t('自营') }}</span>
                                            <span v-if="shop.shopLevelId === 3" class="vip-tag">SVIP</span>
                                            <span v-if="shop.shopLevelId === 2"
                                                class="vip-tag vip-tag-normal">VIP</span>
                                            <span class="fans-tag">
                                                <span class="fans-value">{{ shop.shopCollect || 0 }}</span>
                                                <span class="fans-label">{{ $t('粉丝') }}</span>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- 店铺评分区域 -->
                            <div class="store-rating-card" v-if="shop.shopEvaluationEntity">
                                <div class="rating-list">
                                    <div v-for="rating in ratingItems" :key="rating.key" class="rating-item">
                                        <div class="rating-info">
                                            <span class="rating-label">{{ rating.label }}</span>
                                            <span class="rating-score">{{ formatScore(rating.score(shop)) }}</span>
                                        </div>
                                        <div class="rating-detail">
                                            <span class="rating-level">{{ getRatingLevel(rating.score(shop)) }}</span>
                                            <span class="rating-percent">{{ rating.percent(shop) || '100%' }}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 右侧：商品列表区域 -->
                        <div class="store-right">
                            <div class="goods-list" v-if="shop.commonList && shop.commonList.length > 0">
                                <div class="goods-item" v-for="goods in shop.commonList.slice(0, 4)"
                                    :key="goods.goodsId" @click="handleGoodsClick(goods)">
                                    <div class="goods-image">
                                        <img :src="goods.commonImage" :alt="goods.commonName" />
                                    </div>
                                    <div class="goods-info">
                                        <div class="goods-name">{{ $t(goods.commonName || '') }}</div>
                                        <div class="goods-price">
                                            <span class="price-value">{{ formatPriceWithSymbolSafe(goods.commonPrice ||
                                                goods.goodsPrice) }}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 加载状态 -->
            <Loading v-if="loading" :text="$t('正在加载...')" />

            <!-- 空状态 -->
            <EmptyState v-if="!loading && shopList.length === 0" :text="$t('暂无店铺数据')"
                :description="route.query.kws ? $t('未找到相关店铺，请尝试其他关键词') : $t('当前没有可用的店铺数据')" />

            <!-- 分页 -->
            <div class="pagination-wrapper" v-if="!loading && totalCount > 0">
                <UPagination v-model:page="currentPage" :total="totalCount" :items-per-page="pageSize"
                    @update:page="handlePageChange" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { getShopList } from '~/services/goods.service'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import type {
    ShopListRequest,
    ShopListResponse,
    WebShopListVO,
    ListQueryResultVO,
    ShopEvaluationVO
} from '~/types/store'

// 响应式数据
const shopList = ref<WebShopListVO[]>([])
const loading = ref(false)
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const orderType = ref<number>(1) // 1默认排序，2收藏量排序
const shopSelfSupport = ref(false) // 平台自营筛选

// 获取路由参数
const route = useRoute()

// 获取店铺列表
const fetchShopList = async () => {
    loading.value = true

    try {
        const params: ShopListRequest = {
            page: currentPage.value,
            limit: pageSize.value,
            orderType: orderType.value,
            shopName: route.query.kws as string,
            shopSelfSupport: shopSelfSupport.value ? 'true' : undefined
        }

        const response = await getShopList(params)

        if (response.code === 1 && response.data) {
            shopList.value = response.data.list || []
            totalCount.value = response.data.totalCount || 0
        } else {
            shopList.value = []
            totalCount.value = 0
        }
    } catch (error) {
        console.error('获取店铺列表失败:', error)
        shopList.value = []
        totalCount.value = 0
    } finally {
        loading.value = false
    }
}

// 监听路由参数变化
watch(() => route.query.kws, (newKey) => {
    if (newKey) {
        // 如果有搜索关键词，重置页码并获取数据
        currentPage.value = 1
        fetchShopList()
    }
}, { immediate: true })

// 格式化评分
const formatScore = (score?: number): string => {
    if (!score) return '5.00'
    return score.toFixed(2)
}

// 评分项配置
const ratingItems = [
    {
        key: 'desc',
        label: '描述相符',
        score: (shop: WebShopListVO) => shop.shopEvaluationEntity?.avgDesccredit,
        percent: (shop: WebShopListVO) => shop.shopPercent?.desccreditPercent
    },
    {
        key: 'service',
        label: '服务态度',
        score: (shop: WebShopListVO) => shop.shopEvaluationEntity?.avgServicecredit,
        percent: (shop: WebShopListVO) => shop.shopPercent?.servicecreditPercent
    },
    {
        key: 'delivery',
        label: '发货速度',
        score: (shop: WebShopListVO) => shop.shopEvaluationEntity?.avgDeliverycredit,
        percent: (shop: WebShopListVO) => shop.shopPercent?.deliverycreditPercent
    }
]

// 获取评分等级
const getRatingLevel = (score?: number): string => {
    if (!score) return '优'
    if (score >= 4.5) return '优'
    if (score >= 4.0) return '良'
    if (score >= 3.5) return '中'
    return '差'
}

// 处理排序变化
const handleSortChange = (type: number) => {
    orderType.value = type
    currentPage.value = 1
    fetchShopList()
}

// 处理筛选变化
const handleFilterChange = () => {
    currentPage.value = 1
    fetchShopList()
}

// 处理分页变化
const handlePageChange = (page: number) => {
    currentPage.value = page
    fetchShopList()
}

// 处理店铺点击
const handleStoreClick = (shop: WebShopListVO) => {
    if (shop.shopId) {
        navigateTo(`/store/${shop.shopId}`)
    }
}

// 处理商品点击
const handleGoodsClick = (goods: ListQueryResultVO) => {
    if (goods.goodsId) {
        navigateTo(`/product/${goods.goodsId}`)
    }
}

// 页面加载时获取数据
onMounted(() => {
    fetchShopList()
})
</script>

<style lang="scss" scoped>
.store-list-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 20px 0 40px;
}

.store-list-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

/* 筛选栏 */
.filter-bar {
    background-color: #fff;
    padding: 16px 20px;
    margin-bottom: 20px;
    border-radius: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    .filter-left {
        display: flex;
        gap: 12px;

        .sort-btn {
            padding: 8px 20px;
            border: 1px solid #e0e0e0;
            background-color: #fff;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            color: #666;
            transition: all 0.3s ease;

            &:hover {
                border-color: var(--color);
                color: var(--color);
            }

            &.active {
                background-color: var(--color);
                border-color: var(--color);
                color: #fff;
            }
        }
    }

    .filter-right {
        .checkbox-label {
            display: flex;
            align-items: center;
            gap: 8px;
            cursor: pointer;
            font-size: 14px;
            color: #666;

            input[type="checkbox"] {
                width: 16px;
                height: 16px;
                cursor: pointer;
            }

            &:hover {
                color: var(--color);
            }
        }
    }
}

/* 店铺列表 */
.store-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.store-card {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    transition: box-shadow 0.3s ease;

    &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
}

.store-card-content {
    display: flex;
    gap: 24px;
}

/* 左侧店铺信息 */
.store-left {
    flex-shrink: 0;
    width: 300px;
    display: flex;
    flex-direction: column;
    gap: 20px;
}

/* 店铺头部 */
.store-header {
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 12px;
    padding: 20px;
    border: 1px solid #f0f0f0;
    transition: all 0.3s ease;
    display: flex;
    gap: 16px;
    align-items: flex-start;
    cursor: pointer;

    &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        border-color: #e0e0e0;
    }

    .store-logo-wrapper {
        flex-shrink: 0;
    }

    .store-logo {
        width: 80px;
        height: 80px;
        flex-shrink: 0;
        border-radius: 12px;
        overflow: hidden;
        background-color: #fff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        border: 2px solid #fff;
        transition: transform 0.3s ease;

        &:hover {
            transform: scale(1.05);
        }

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }

    .store-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 12px;
        min-width: 0;

        .store-name-row {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            gap: 8px;
            min-width: 0;
            width: 100%;

            .store-name {
                width: 100%;
                max-width: 100%;
                min-width: 0;
                box-sizing: border-box;
                font-size: 18px;
                font-weight: 600;
                color: #333;
                margin: 0;
                line-height: 1.4;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .store-tags {
                display: flex;
                gap: 6px;
                flex-wrap: wrap;
                align-items: center;
                max-width: 100%;
            }

            .self-support-tag,
            .vip-tag,
            .fans-tag {
                padding: 4px 10px;
                border-radius: 12px;
                font-size: 11px;
                font-weight: 600;
                letter-spacing: 0.5px;
                box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
                display: inline-flex;
                align-items: center;
                gap: 4px;
            }

            .self-support-tag {
                background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
                color: #fff;
            }

            .vip-tag {
                background: linear-gradient(135deg, #ffd700 0%, #ffc107 100%);
                color: #333;

                &.vip-tag-normal {
                    background: linear-gradient(135deg, #c0c0c0 0%, #a0a0a0 100%);
                    color: #fff;
                }
            }

            .fans-tag {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: #fff;

                .fans-value {
                    font-weight: 600;
                }

                .fans-label {
                    font-size: 10px;
                    opacity: 0.9;
                }
            }
        }
    }
}

/* 店铺评分卡片 */
.store-rating-card {
    background: #fff;
    border-radius: 12px;
    padding: 16px;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);

    .rating-list {
        display: flex;
        flex-direction: column;
        gap: 6px;
    }

    .rating-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 4px 0;

        .rating-info {
            display: flex;
            align-items: center;
            gap: 12px;
            flex: 1;

            .rating-label {
                color: #666;
                font-size: 13px;
                min-width: 70px;
            }

            .rating-score {
                color: #ff6b6b;
                font-weight: 600;
                font-size: 15px;
                min-width: 45px;
            }
        }

        .rating-detail {
            display: flex;
            align-items: center;
            gap: 8px;

            .rating-level {
                color: #52c41a;
                font-size: 12px;
                font-weight: 500;
                padding: 2px 8px;
                background-color: #f6ffed;
                border-radius: 4px;
                min-width: 28px;
                text-align: center;
            }

            .rating-percent {
                color: #999;
                font-size: 11px;
                min-width: 40px;
                text-align: right;
            }
        }
    }
}

/* 右侧商品列表 */
.store-right {
    flex: 1;
    min-width: 0;
}

.goods-list {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
}

.goods-item {
    display: flex;
    flex-direction: column;
    cursor: pointer;
    transition: transform 0.2s ease;

    &:hover {
        transform: translateY(-2px);
    }

    .goods-image {
        width: 100%;
        aspect-ratio: 1 / 1;
        border-radius: 8px;
        overflow: hidden;
        background-color: #f5f5f5;
        margin-bottom: 8px;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }

    .goods-info {
        .goods-name {
            font-size: 14px;
            color: #333;
            line-height: 1.4;
            margin-bottom: 8px;
            height: 2.8em;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .goods-price {
            display: flex;
            align-items: baseline;
            color: #ff6b6b;
            font-weight: bold;

            .price-symbol {
                font-size: 14px;
            }

            .price-value {
                font-size: 18px;
            }
        }
    }
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

/* 分页 */
.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 40px;
    padding: 20px 0;
}
</style>
