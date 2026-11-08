<template>
    <div class="product-ranking">
        <div class="ranking-header">
            <div class="ranking-title">{{ $t('商品排行') }} </div>
            <div v-if="qrUrl" class="mobile-entry-wrap" @mouseenter="showQr = true" @mouseleave="showQr = false">
                <span class="mobile-entry-trigger">{{ $t('手机进店') }}</span>
                <Transition name="qr-fade">
                    <div v-show="showQr" class="qr-popover">
                        <div class="qr-popover-arrow"></div>
                        <img :src="qrImageSrc" alt="手机进店二维码" class="qr-image" />
                        <div class="qr-tip">{{ $t('扫码进店') }}</div>
                    </div>
                </Transition>
            </div>
        </div>

        <!-- 标签页 -->
        <div class="ranking-tabs">
            <div class="tab-item" :class="{ active: activeTab === 'hot' }" @click="handleTabChange('hot')">
                {{ $t('热销商品') }}
            </div>
            <div class="tab-item" :class="{ active: activeTab === 'favorite' }" @click="handleTabChange('favorite')">
                {{ $t('热门收藏') }}
            </div>
        </div>

        <!-- 商品列表 -->
        <div class="ranking-list" v-if="!loading && products.length > 0">
            <div v-for="product in products" :key="product.goodsId || product.commonId" class="ranking-item"
                @click="handleProductClick(product)">
                <div class="product-image">
                    <img :src="product.commonImage" :alt="product.commonName" />
                </div>
                <div class="product-info">
                    <div class="product-name">{{ $t(product.commonName || '') }}</div>
                    <div class="product-price">
                        <span class="price-value">{{ formatPriceWithSymbolSafe(product.goodsPrice ||
                            product.commonPrice) }}</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 加载状态 -->
        <Loading v-if="loading" :text="$t('加载中...')" />

        <!-- 空状态 -->
        <EmptyState v-if="!loading && products.length === 0" :text="$t('暂无商品')" :icon-size="48" :compact="true" />
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getRecommendGoods } from '~/services/goods.service'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import type { ListQueryResultVO } from '~/types/store'

const props = defineProps<{
    shopId: number
    /** 店铺二维码 base64，用于手机进店悬浮展示 */
    qrUrl?: string
}>()

const emit = defineEmits<{
    productClick: [product: ListQueryResultVO]
}>()

const activeTab = ref<'hot' | 'favorite'>('hot')
const showQr = ref(false)
// 二维码图片地址：若 qrUrl 为纯 base64 则补全 data URL 前缀
const qrImageSrc = computed(() => {
    const url = props.qrUrl || ''
    if (!url) return ''
    if (/^data:/.test(url)) return url
    return `data:image/png;base64,${url}`
})
const products = ref<ListQueryResultVO[]>([])
const loading = ref(false)
// 缓存已获取的数据，避免重复请求
const cachedProducts = ref<{
    hot: ListQueryResultVO[] | null
    favorite: ListQueryResultVO[] | null
}>({
    hot: null,
    favorite: null
})

// 获取商品列表
const fetchProducts = async (forceRefresh = false) => {
    if (!props.shopId) return

    const cacheKey = activeTab.value
    // 如果已有缓存且不是强制刷新，直接使用缓存数据
    if (!forceRefresh && cachedProducts.value[cacheKey] !== null) {
        products.value = [...cachedProducts.value[cacheKey]!]
        return
    }

    loading.value = true
    // 切换时先清空数据，显示加载状态
    products.value = []

    try {
        const opType = activeTab.value === 'hot' ? 3 : 2 // 3: 热销商品排行, 2: 热门收藏排行

        const response = await getRecommendGoods({
            shopId: String(props.shopId),
            opType,
            limitSize: 5,
            token: null
        })

        if (response.code === 1 && response.data) {
            const data = response.data || []
            // 更新缓存
            cachedProducts.value[cacheKey] = [...data]
            // 更新显示数据
            products.value = [...data]
        } else {
            products.value = []
            cachedProducts.value[cacheKey] = []
        }
    } catch (error) {
        console.error('获取商品排行失败:', error)
        products.value = []
        cachedProducts.value[cacheKey] = []
    } finally {
        loading.value = false
    }
}

// 处理标签切换
const handleTabChange = async (tab: 'hot' | 'favorite') => {
    if (activeTab.value === tab) {
        return // 如果点击的是当前标签，不重复请求
    }

    activeTab.value = tab

    // 如果已有缓存，直接使用缓存，否则请求接口
    const cacheKey = tab
    if (cachedProducts.value[cacheKey] !== null) {
        products.value = [...cachedProducts.value[cacheKey]!]
    } else {
        await fetchProducts()
    }
}

// 处理商品点击
const handleProductClick = (product: ListQueryResultVO) => {
    emit('productClick', product)
}

// 监听 shopId 变化
watch(() => props.shopId, (newShopId, oldShopId) => {
    if (newShopId && newShopId !== oldShopId) {
        // 清空缓存
        cachedProducts.value = {
            hot: null,
            favorite: null
        }
        // 重置为热销商品标签
        activeTab.value = 'hot'
        fetchProducts(true) // 强制刷新
    }
}, { immediate: true })
</script>

<style lang="scss" scoped>
.product-ranking {
    background-color: #fff;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    .ranking-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #f0f0f0;
    }

    .ranking-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
    }

    .mobile-entry-wrap {
        position: relative;
    }

    .mobile-entry-trigger {
        font-size: 12px;
        color: var(--color);
        cursor: pointer;
        white-space: nowrap;

        &:hover {
            text-decoration: underline;
        }
    }

    .qr-popover {
        position: absolute;
        right: 0;
        bottom: calc(100% + 8px);
        width: 140px;
        padding: 10px;
        background: #fff;
        border-radius: 8px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
        border: 1px solid #f0f0f0;
        z-index: 10;
    }

    .qr-popover-arrow {
        position: absolute;
        right: 16px;
        bottom: -6px;
        width: 12px;
        height: 12px;
        background: #fff;
        border-right: 1px solid #f0f0f0;
        border-bottom: 1px solid #f0f0f0;
        transform: rotate(45deg);
    }

    .qr-image {
        display: block;
        width: 100%;
        height: auto;
        max-width: 120px;
        aspect-ratio: 1;
        object-fit: contain;
        background: #fff;
        margin: 0 auto;
    }

    .qr-tip {
        font-size: 12px;
        color: #666;
        text-align: center;
        margin-top: 6px;
    }

    .ranking-tabs {
        display: flex;
        gap: 8px;
        margin-bottom: 16px;

        .tab-item {
            flex: 1;
            padding: 8px 16px;
            text-align: center;
            border-radius: 6px;
            cursor: pointer;
            font-size: 14px;
            color: #666;
            background-color: #f5f5f5;
            transition: all 0.3s ease;

            &:hover {
                background-color: #e8e8e8;
            }

            &.active {
                background-color: var(--color);
                color: #fff;
            }
        }
    }

    .ranking-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .ranking-item {
        display: flex;
        gap: 12px;
        padding: 12px;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
            background-color: #f5f5f5;
        }

        .product-image {
            width: 60px;
            height: 60px;
            border-radius: 6px;
            overflow: hidden;
            background-color: #f5f5f5;
            flex-shrink: 0;

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }

        .product-info {
            flex: 1;
            min-width: 0;
            display: flex;
            flex-direction: column;
            justify-content: space-between;

            .product-name {
                font-size: 13px;
                color: #333;
                line-height: 1.4;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                text-overflow: ellipsis;
                margin-bottom: 6px;
            }

            .product-price {
                display: flex;
                align-items: baseline;
                color: #ff6b6b;
                font-weight: 600;

                .price-symbol {
                    font-size: 12px;
                }

                .price-value {
                    font-size: 16px;
                }
            }
        }
    }
}

.qr-fade-enter-active,
.qr-fade-leave-active {
    transition: opacity 0.2s ease;
}

.qr-fade-enter-from,
.qr-fade-leave-to {
    opacity: 0;
}
</style>
