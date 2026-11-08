<template>
    <div class="hot-recommend-box">
        <div class="hot-recommend-header">
            <h2 class="hot-recommend-title">{{ $t(pageName) }}</h2>
            <div class="hot-recommend-tabs">
                <span class="tab">{{ $t(pageTitle) }}</span>
            </div>
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
                </div>
                <div class="product-meta">
                    <div class="product-title">{{ $tg(product.commonName || '') }}</div>
                    <div class="product-price">
                        <span v-if="product.commonPrice" class="price-label">{{ $t('特惠价') }}</span>
                        <span v-if="product.commonPrice" class="current-price">{{
                            formatPriceWithSymbolSafe(product.commonPrice) }}</span>
                        <span
                            v-if="!product.commonPrice && product.goodsPrice"
                            class="consult-price"
                        >{{ formatPriceWithSymbolSafe(product.goodsPrice) }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { HomeProduct, CategoryDataItem } from '~/types/home'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'

defineEmits([])
const props = withDefaults(
    defineProps<{
        dataInfo: CategoryDataItem[]
    }>(),
    {
        dataInfo: () => []
    }
)

const pageTitle = computed(() => props.dataInfo[0]?.pageTitle || '好品推荐')
const pageName = computed(() => props.dataInfo[0]?.pageName || '热门推荐')
const products = computed(() => props.dataInfo[0]?.goods || [])

const handleProductClick = (product: HomeProduct) => {
    navigateTo(`/product/${product.goodsId}`)
}
</script>

<style lang="scss" scoped>
.hot-recommend-box {
    width: 100%;
    background-color: white;
    border-radius: 8px;
    overflow: hidden;
    padding-top: 20px;
    margin-bottom: 20px;

    .hot-recommend-header {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 0 20px 12px;

        .hot-recommend-title {
            margin: 0 20px 0 0;
            font-size: 20px;
            font-weight: bold;
            color: var(--color);
        }

        .hot-recommend-tabs .tab {
            font-size: 14px;
            color: #666;
        }
    }

    .product-list {
        display: flex;
        flex-direction: row;
        flex-wrap: nowrap;
        gap: 12px;
        padding: 0 20px 20px;
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
        background: #f8f9fa;
        border-radius: 8px;
        cursor: pointer;
        transition: background 0.15s ease;
        box-sizing: border-box;

        &:hover {
            background: #f0f2f4;
        }
    }

    .product-image {
        width: 100%;
        aspect-ratio: 1 / 1;
        display: flex;
        align-items: center;
        justify-content: center;
        background: white;
        border-radius: 8px;
        overflow: hidden;

        img {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
        }
    }

    .product-meta {
        min-width: 0;
        margin-top: 10px;
    }

    .product-title {
        font-size: 14px;
        color: #333;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        font-weight: 500;
        min-height: 2.8em;
    }

    .product-price {
        margin-top: 8px;
        display: flex;
        align-items: center;
        gap: 5px;
        flex-wrap: wrap;

        .price-label {
            background-color: var(--color);
            color: white;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: bold;
        }

        .current-price,
        .consult-price {
            font-size: 15px;
            color: var(--color);
            font-weight: bold;
        }
    }
}

@media (max-width: 640px) {
    .hot-recommend-box .product-item {
        flex-basis: 132px;
        width: 132px;
    }
}
</style>
