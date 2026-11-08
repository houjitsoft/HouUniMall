<template>
    <div class="product-card" :class="`product-card--${mode}`" @click="handleClick">
        <!-- 商品图片区域 -->
        <div class="product-image">
            <img :src="goods.commonImage || goods.goodsImage" :alt="goods.commonName || goods.goodsName"
                @error="handleImageError" />
            <!-- 图片占位符 -->
            <div v-if="imageError" class="image-placeholder">
                <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2" stroke-dasharray="2 2" />
                    <path d="M9 9h6v6H9z" />
                </svg>
            </div>
            <!-- 库存标签 -->
            <div v-if="goods.goodsLabelName" class="product-badge product-badge--stock product-badge-label">{{goods.goodsLabelName}}</div>
            <!-- 库存标签 -->
            <div v-if="goods.deliveryDate === 0" class="product-badge product-badge--stock">{{ $t('现货') }}</div>
            <div v-else-if="goods.avgShippingTime || goods.goodsOrderDate"
                class="product-badge product-badge--shipping">
                {{ goods.avgShippingTime || (goods.goodsOrderDate ? `${goods.goodsOrderDate}${$t('个工作日')}` : '') }}
            </div>
        </div>

        <!-- 商品信息区域 -->
        <div class="product-content">
            <!-- 商品名称 -->
            <div class="product-title" :title="goods.commonName || goods.goodsName"
                v-html="$t((goods.commonName || goods.goodsName) || '')"></div>

            <!-- 商品价格 -->
            <div class="product-price">
                <span class="price-number">{{ formatPriceWithSymbolSafe(goods.goodsPrice || goods.commonPrice) }}</span>
            </div>

            <!-- 商品元信息 -->
            <div class="product-meta">
                <span class="meta-item meta-item--sales">{{ $t('销量') }}: {{ goods.commonSalenum || goods.goodsSalenum ||
                    0
                    }}{{ $t('件') }}</span>
                <span class="meta-item meta-item--evaluate">{{ goods.commonEvaluate || goods.qty || 0 }}{{ $t('条好评')
                    }}</span>
            </div>

            <!-- 店铺名称 -->
            <div class="product-shop">{{ $t(goods.shopName || '') }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { formatPriceWithoutSymbol } from '~/composables/useFormatPrice'
import type { ArticleVO } from '~/types/goods'

/**
 * 组件 Props
 */
interface Props {
    goods: ArticleVO
    /** 展示模式：grid 大图卡片（默认），list 横向列表行 */
    mode?: 'grid' | 'list'
}

const props = withDefaults(defineProps<Props>(), {
    mode: 'grid'
})

/**
 * 组件事件
 */
const emit = defineEmits<{
    click: [goods: ArticleVO]
}>()

/**
 * 图片加载错误状态
 */
const imageError = ref(false)

/**
 * 处理商品点击
 */
const handleClick = (): void => {
    emit('click', props.goods)
}

/**
 * 处理图片加载错误
 * @param e 错误事件
 */
const handleImageError = (e: Event): void => {
    imageError.value = true
    const img = e.target as HTMLImageElement
    img.style.display = 'none'
}

</script>

<style lang="scss" scoped>
.product-card {
    width: 100%;
    background-color: var(--bg-card, #fff);
    border-radius: var(--border-radius, 4px);
    overflow: hidden;
    cursor: pointer;
    transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
    position: relative;
    box-shadow: none;
    border: 1px solid var(--border-light, #eee);
    box-sizing: border-box;
    display: flex;
    flex-direction: column;

    &:hover {
        box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.06));
        border-color: var(--color);
        transform: translateY(-2px);
    }

    .product-image {
        position: relative;
        width: 100%;
        aspect-ratio: 1 / 1;
        height: auto;
        flex-shrink: 0;
        overflow: hidden;
        padding: 0;
        background: #f7f7f7;
        display: flex;
        align-items: center;
        justify-content: center;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
            transition: transform 0.2s ease;
        }

        &:hover img {
            transform: scale(1.04);
        }

        .image-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #d0d0d0;
        }

        .product-badge {
            position: absolute;
            top: 8px;
            right: 8px;
            padding: 4px 8px;
            border-radius: var(--border-radius, 4px);
            font-size: var(--font-size-xs, 12px);
            font-weight: 500;
            color: #fff;
            z-index: 1;
            line-height: 1.2;

            &--stock {
                background-color: var(--color);
            }

            &--shipping {
                background-color: var(--color-warning, #ff9800);
            }
        }
        .product-badge-label{
            left: 8px;
            right: auto;
            width: fit-content;
        }
    }

    .product-content {
        flex: 1;
        padding: 12px;
        display: flex;
        flex-direction: column;
        min-height: 0;
        overflow: hidden;

        .product-title {
            margin: 0 0 8px;
            color: var(--text-primary, #202a33);
            font-size: var(--font-size-sm, 13px);
            line-height: 1.45;
            height: 38px;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
            word-break: break-word;
        }

        .product-price {
            margin-bottom: 8px;
            display: flex;
            align-items: baseline;
            gap: 2px;

            .price-number {
                font-size: var(--font-size-lg, 18px);
                color: var(--color-price, var(--color));
                font-weight: 700;
                font-variant-numeric: tabular-nums;
            }
        }

        .product-meta {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 8px;
            font-size: var(--font-size-xs, 12px);

            .meta-item {
                color: var(--text-secondary, #8f8f8f);

                &--sales {
                    color: var(--text-regular, #5d6872);
                }
            }
        }

        .product-shop {
            margin-top: auto;
            font-size: var(--font-size-xs, 12px);
            color: var(--text-secondary, #8f8f8f);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    }
}

/* ==================== 列表（list）模式：横向行布局 ==================== */
/* 仅通过 CSS 重排，DOM 结构与大图模式完全一致 */
.product-card--list {
    flex-direction: row;
    align-items: stretch;
    width: 100%;
    border-radius: 10px;

    &:hover {
        transform: translateY(0);
        box-shadow: var(--shadow-lg, 0 6px 20px rgba(0, 0, 0, 0.08));
    }

    .product-image {
        width: 200px;
        min-width: 200px;
        flex-shrink: 0;
        aspect-ratio: auto;
        height: auto;
        min-height: 180px;
        background: #fafafa;

        img {
            object-fit: contain;
            padding: 12px;
        }
    }

    .product-content {
        flex: 1;
        min-width: 0;
        display: grid;
        grid-template-columns: minmax(0, 1fr) auto;
        grid-template-rows: auto auto auto;
        grid-template-areas:
            "title price"
            "meta  price"
            "shop  price";
        align-content: space-between;
        column-gap: 28px;
        row-gap: 6px;
        padding: 20px 24px;

        .product-title {
            grid-area: title;
            align-self: start;
            height: auto;
            min-height: 2.9em;
            margin: 0;
            font-size: 15px;
            font-weight: 500;
            line-height: 1.45;
            -webkit-line-clamp: 2;
            line-clamp: 2;
        }

        .product-price {
            grid-area: price;
            align-self: center;
            justify-self: end;
            flex-direction: column;
            align-items: flex-end;
            gap: 0;
            margin: 0;

            .price-number {
                font-size: 22px;
            }
        }

        .product-meta {
            grid-area: meta;
            align-self: end;
            margin: 0;
            font-size: 13px;
        }

        .product-shop {
            grid-area: shop;
            align-self: start;
            margin-top: 0;
            font-size: 13px;
        }
    }

    @media (max-width: 768px) {
        .product-image {
            width: 120px;
            min-width: 120px;
            min-height: 120px;
        }

        .product-content {
            padding: 14px 16px;
            column-gap: 12px;
            grid-template-columns: minmax(0, 1fr);
            grid-template-areas:
                "title"
                "price"
                "meta"
                "shop";

            .product-price {
                justify-self: start;
                align-items: flex-start;

                .price-number {
                    font-size: 18px;
                }
            }
        }
    }
}

@media (max-width: 768px) {
    .product-card {
        .product-content {
            padding: 8px;

            .product-title {
                font-size: 12px;
                height: 36px;
            }

            .product-price .price-number {
                font-size: 16px;
            }
        }
    }
}
</style>
