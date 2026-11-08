<template>
    <div v-if="products.length" class="discount-box">
        <div class="discount-header">
            <div class="header-left">
                <img :src="IMAGE_URL('pcImg/zhekou.png')" alt="限时折扣" class="discount-title-img" />
                <span class="discount-subtitle">{{ $t('限时狂欢，抢购正当时！') }}</span>
            </div>
            <NuxtLink to="/activity/discount" class="header-right">
                <span class="more-products">{{ $t('更多商品') }}</span>
                <span class="arrow-icon">〉</span>
            </NuxtLink>
        </div>

        <!-- 单行卡片，水平滚动 -->
        <div class="product-list">
            <div
                v-for="product in products"
                :key="product.goodsId"
                class="product-item"
                @click="handleProductClick(product)"
            >
                <div class="product-image">
                    <img :src="product.commonImage" :alt="product.commonName" />
                    <div v-if="product.tag" class="product-tag">{{ product.tag }}</div>
                </div>
                <div class="product-info">
                    <div class="product-title">{{ $tg(product.commonName || '') }}</div>
                    <div class="product-price">
                        <span class="current-price">{{ formatPriceWithSymbolSafe(product.discountPrice) }}</span>
                        <span class="original-price">{{ formatPriceWithSymbolSafe(product.commonPrice) }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDiscountProductList } from '~/services/activity.service'
import type { HomeProduct } from '~/types/home'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'
import { IMAGE_URL } from '~/utils/externalUrl'

const products = ref<HomeProduct[]>([])

const fetchDiscountProducts = async () => {
    try {
        const res = await getDiscountProductList<HomeProduct[]>({
            limitSize: 20
        })
        if (res && res.data) {
            products.value = res.data || []
        }
    } catch (error) {
        console.error('获取限时折扣商品失败:', error)
    }
}

onMounted(() => {
    fetchDiscountProducts()
})

const handleProductClick = (product: HomeProduct) => {
    navigateTo(`/product/${product.goodsId}`)
}
</script>

<style scoped lang="scss">
.discount-box {
    width: 100%;
    background-color: #fff;
    margin-bottom: 20px;
    border: 1px solid var(--home-border, #e2e8ed);
    border-radius: var(--home-card-radius, 8px);
    overflow: hidden;

    .discount-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: 50px;
        background: #17324d;
        color: white;
        padding: 0 20px;

        .header-left {
            display: flex;
            align-items: center;
            gap: 10px;

            .discount-title-img {
                height: 29.04px;
                width: auto;
                vertical-align: middle;
                margin-right: 8px;
            }

            .discount-subtitle {
                font-size: 14px;
                color: rgba(255, 255, 255, 0.9);
            }
        }

        .header-right {
            display: flex;
            align-items: center;
            gap: 15px;
            color: white;
            text-decoration: none;

            .more-products {
                font-size: 14px;
            }

            .arrow-icon {
                font-size: 14px;
            }
        }
    }

    .product-list {
        display: flex;
        flex-direction: row;
        flex-wrap: nowrap;
        gap: 12px;
        padding: 14px 16px 16px;
        overflow-x: auto;
        overflow-y: hidden;
        -webkit-overflow-scrolling: touch;
        scrollbar-width: thin;
    }

    .product-item {
        flex: 0 0 160px;
        width: 160px;
        display: flex;
        flex-direction: column;
        padding: 10px;
        background: var(--home-surface-soft, #f7f9fb);
        border: 1px solid transparent;
        border-radius: 8px;
        cursor: pointer;
        transition: background-color 0.15s ease, border-color 0.15s ease;
        box-sizing: border-box;

        &:hover {
            background-color: #fff;
            border-color: var(--home-border-strong, #d5dee5);
        }
    }

    .product-image {
        position: relative;
        width: 100%;
        aspect-ratio: 1 / 1;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #fff;
        border-radius: 8px;

        img {
            width: 100%;
            height: 100%;
            object-fit: contain;
        }

        .product-tag {
            position: absolute;
            bottom: 6px;
            right: 6px;
            background-color: var(--color, #007bff);
            color: white;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: bold;
        }
    }

    .product-info {
        min-width: 0;
        margin-top: 10px;

        .product-title {
            font-size: 14px;
            color: var(--home-text, #202a33);
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            font-weight: 500;
            min-height: 2.8em;
        }

        .product-price {
            margin-top: 8px;
            display: flex;
            align-items: baseline;
            gap: 8px;
            flex-wrap: wrap;

            .current-price {
                font-size: 16px;
                font-weight: bold;
                color: var(--color-price, var(--color));
            }

            .original-price {
                font-size: 13px;
                color: #999;
                text-decoration: line-through;
            }
        }
    }
}

@media (max-width: 640px) {
    .discount-box .product-item {
        flex-basis: 132px;
        width: 132px;
    }
}
</style>
