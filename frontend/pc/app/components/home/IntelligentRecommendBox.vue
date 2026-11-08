<template>
    <div class="intelligent-recommend-box">
        <!-- 标题部分 -->
        <div class="recommend-title">
            <h2 class="main-title">{{ $t('智能推荐') }}</h2>
        </div>

        <!-- 商品部分 -->
        <div class="products-container" ref="productsContainer">
            <div v-for="(product, index) in state.products" :key="index" class="product-item"
                @click="handleProductClick(product)">
                <div class="product-image">
                    <img :src="product.commonImage" :alt="product.commonName" />
                </div>
                <div class="product-info">
                    <div class="product-name">{{ $t(product.commonName || '') }}</div>
                    <div class="product-price">
                        <span class="price">{{ formatPriceWithSymbolSafe(product.commonPrice) }}</span>
                    </div>
                </div>
            </div>
            <!-- 触底加载观察元素 - 直接在模板中定义，避免 hydration 不匹配 -->
            <div class="intersection-sentinel"></div>
        </div>

        <!-- 加载状态 -->
        <Loading v-if="state.isLoading" compact :text="$t('正在加载...')" />

        <!-- 底部提示 -->
        <div class="bottom-tip" v-if="!state.isLoading && !state.hasMoreProducts">
            <p class="tip-text">{{ $t('已加载全部商品') }}</p>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, onMounted, ref, onUnmounted } from 'vue'
import { getIntelligentRecommendationGoods } from '~/services/goods.service'

import type { HomeProduct } from '~/types/home'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'
import Loading from '~/components/Load/Loading.vue'

const emits = defineEmits([])
const props = defineProps({

})

const productsContainer = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

const state = reactive({
    products: [] as HomeProduct[],
    isLoading: false,
    hasMoreProducts: true,
    page: 1,
    limit: 12
})

// 加载商品
const loadProducts = async () => {
    if (state.isLoading || !state.hasMoreProducts) return

    state.isLoading = true

    try {
        // 调用智能推荐商品接口
        const response = await getIntelligentRecommendationGoods({
            page: state.page,
            limit: state.limit
        })

        // 假设接口返回的数据结构为 { data: any[] }
        const newProducts = response.data || []

        if (newProducts.length > 0) {
            state.products.push(...newProducts)
            state.page++
        } else {
            // 如果返回的商品数量为0，说明已经没有更多数据了
            state.hasMoreProducts = false
        }
    } catch (error) {
        console.error('获取智能推荐商品失败:', error)
        // 可以在这里添加错误提示
    } finally {
        state.isLoading = false
    }
}

// 触底加载观察者
const setupIntersectionObserver = () => {
    if (!productsContainer.value) return

    observer = new IntersectionObserver((entries) => {
        const entry = entries[0]
        if (entry && entry.isIntersecting && state.hasMoreProducts && !state.isLoading) {
            loadProducts()
        }
    }, {
        rootMargin: '200px' // 提前200px触发加载
    })

    // 观察已在模板中定义好的 sentinel 元素
    const sentinel = productsContainer.value.querySelector('.intersection-sentinel')
    if (sentinel) {
        observer.observe(sentinel)
    }
}

// 处理点击商品
const handleProductClick = (item: HomeProduct) => {
    if (item.goodsId) {
        navigateTo(`/product/${item.goodsId}`)
    }
}

onMounted(() => {
    // 初始加载数据
    loadProducts()
    // 设置触底加载观察者
    setupIntersectionObserver()
})

onUnmounted(() => {
    if (observer) {
        observer.disconnect()
    }
})

defineExpose({ state })
</script>

<style lang="scss" scoped>
.intelligent-recommend-box {
    width: 100%;
    padding: 20px;
    margin-bottom: 20px;
    background-color: #fff;
}

/* 标题部分 */
.recommend-title {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 20px;

    .main-title {
        font-size: 24px;
        font-weight: bold;
        color: var(--color);
        margin: 0;
    }
}

/* 商品部分 */
.products-container {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 15px;
    margin-bottom: 20px;

    .intersection-sentinel {
        height: 1px;
        margin-top: 20px;
    }

    .product-item {
        background-color: #fff;
        border-radius: 8px;
        overflow: hidden;
        transition: transform 0.3s ease, box-shadow 0.3s ease;

        &:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
        }

        .product-image {
            width: 100%;
            aspect-ratio: 1 / 1;
            overflow: hidden;
            background-color: #f8f8f8;

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                transition: transform 0.3s ease;
            }
        }

        .product-info {
            padding: 10px;

            .product-name {
                font-size: 13px;
                color: #333;
                line-height: 1.4;
                margin-bottom: 8px;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                text-overflow: ellipsis;
                height: 36px;
                /* 两行文字的固定高度 */
                /* 字体大小13px * 行高1.4 * 2行 = 36.4px，取整为36px */
                /* 固定高度，确保价格对齐 */
            }

            .product-price {
                font-size: 16px;
                font-weight: bold;
                color: var(--color);

                .currency {
                    font-size: 12px;
                    margin-right: 2px;
                }

                .price {
                    font-size: 16px;
                }
            }
        }
    }
}

/* 底部提示 */
.bottom-tip {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 40px 0;

    .tip-text {
        color: #999;
        font-size: 14px;
        margin: 0;
    }
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .products-container {
        grid-template-columns: repeat(5, 1fr);
    }
}

@media (max-width: 992px) {
    .products-container {
        grid-template-columns: repeat(4, 1fr);
    }
}

@media (max-width: 768px) {
    .products-container {
        grid-template-columns: repeat(3, 1fr);
    }
}

@media (max-width: 576px) {
    .products-container {
        grid-template-columns: repeat(2, 1fr);
        gap: 10px;
    }
}
</style>