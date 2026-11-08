<template>
    <div class="product-card" @click="handleClick">
        <div class="product-image">
            <img :src="product.commonImage || product.goodsImage" :alt="product.commonName || product.goodsName" />
        </div>
        <div class="product-info">
            <div class="product-price">
                <span class="price-value">{{ formatPriceWithSymbolSafe(product.goodsPrice || product.commonPrice) }}</span>
            </div>
            <div class="product-name">{{ $t(product.commonName || product.goodsName || '') }}</div>
            <div class="product-sales" v-if="product.commonSalenum !== undefined">
                {{ $t('销量') }}: {{ product.commonSalenum }}
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import type { GoodsBases } from '~/types/goods'

const props = defineProps<{
    product: GoodsBases | any
}>()

const emit = defineEmits<{
    click: [product: GoodsBases | any]
}>()

// 处理点击
const handleClick = () => {
    emit('click', props.product)
}
</script>

<style lang="scss" scoped>
.product-card {
    display: flex;
    flex-direction: column;
    cursor: pointer;
    transition: transform 0.2s ease;
    border-radius: 8px;
    overflow: hidden;
    background-color: #fff;

    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .product-image {
        width: 100%;
        aspect-ratio: 1 / 1;
        background-color: #f5f5f5;
        overflow: hidden;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }

    .product-info {
        padding: 12px;
        display: flex;
        flex-direction: column;
        gap: 8px;

        .product-price {
            display: flex;
            align-items: baseline;
            color: var(--color-price, var(--color));
            font-weight: 600;

            .price-symbol {
                font-size: 14px;
            }

            .price-value {
                font-size: 18px;
            }
        }

        .product-name {
            font-size: 14px;
            color: #333;
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
            height: 2.8em;
        }

        .product-sales {
            font-size: 12px;
            color: #999;
        }
    }
}
</style>
