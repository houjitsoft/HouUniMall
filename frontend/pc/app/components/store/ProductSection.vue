<template>
    <div class="product-section">
        <div class="section-header">
            <div class="title-wrapper">
                <span class="title-icon">✨</span>
                <h3 class="section-title">{{ $t(title || '') }}</h3>
            </div>
            <a v-if="showMore" class="more-link" @click="handleMoreClick">
                <span>{{ $t('更多') }}</span>
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6 12L10 8L6 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"
                        stroke-linejoin="round" />
                </svg>
            </a>
        </div>
        <div class="product-grid" v-if="products.length > 0">
            <ProductCard v-for="product in products" :key="product.goodsId || product.commonId" :product="product"
                @click="handleProductClick" />
        </div>
        <EmptyState v-else :text="$t('暂无商品')" :description="$t('该分类下暂时没有商品，请稍后再来查看')" />
    </div>
</template>

<script setup lang="ts">
import ProductCard from './ProductCard.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import type { GoodsBases } from '~/types/goods'

const props = defineProps<{
    title: string
    products: (GoodsBases | any)[]
    showMore?: boolean
}>()

const emit = defineEmits<{
    productClick: [product: GoodsBases | any]
    moreClick: []
}>()

// 处理商品点击
const handleProductClick = (product: GoodsBases | any) => {
    emit('productClick', product)
}

// 处理更多点击
const handleMoreClick = () => {
    emit('moreClick')
}
</script>

<style lang="scss" scoped>
.product-section {
    margin-bottom: 32px;
    padding: 24px;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;

    &:hover {
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    }

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 16px;
        border-bottom: 2px solid #f5f5f5;
        position: relative;

        &::after {
            content: '';
            position: absolute;
            bottom: -2px;
            left: 0;
            width: 60px;
            height: 2px;
            background: linear-gradient(90deg, var(--color) 0%, transparent 100%);
            border-radius: 2px;
        }

        .title-wrapper {
            display: flex;
            align-items: center;
            gap: 10px;

            .title-icon {
                font-size: 20px;
                line-height: 1;
            }

            .section-title {
                font-size: 20px;
                font-weight: 600;
                color: #333;
                margin: 0;
                background: linear-gradient(135deg, #333 0%, #666 100%);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
                background-clip: text;
            }
        }

        .more-link {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            color: #666;
            cursor: pointer;
            text-decoration: none;
            padding: 6px 12px;
            border-radius: 6px;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;

            &::before {
                content: '';
                position: absolute;
                top: 0;
                left: -100%;
                width: 100%;
                height: 100%;
                background: linear-gradient(90deg, transparent, rgba(var(--color-rgb, 0, 123, 255), 0.1), transparent);
                transition: left 0.5s ease;
            }

            &:hover {
                color: var(--color);
                background-color: rgba(var(--color-rgb, 0, 123, 255), 0.05);

                &::before {
                    left: 100%;
                }

                svg {
                    transform: translateX(2px);
                }
            }

            &:active {
                transform: scale(0.98);
            }

            span {
                font-weight: 500;
            }

            svg {
                transition: transform 0.3s ease;
                flex-shrink: 0;
            }
        }
    }

    .product-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 20px;
        animation: fadeInUp 0.5s ease-out;
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
@media (max-width: 1200px) {
    .product-section {
        padding: 20px;

        .section-header {
            margin-bottom: 18px;
            padding-bottom: 14px;

            .title-wrapper {
                .section-title {
                    font-size: 18px;
                }
            }
        }

        .product-grid {
            grid-template-columns: repeat(3, 1fr);
            gap: 18px;
        }
    }
}

@media (max-width: 968px) {
    .product-section {
        padding: 16px;
        margin-bottom: 24px;
        border-radius: 10px;

        .section-header {
            margin-bottom: 16px;
            padding-bottom: 12px;

            .title-wrapper {
                gap: 8px;

                .title-icon {
                    font-size: 18px;
                }

                .section-title {
                    font-size: 17px;
                }
            }

            .more-link {
                padding: 5px 10px;
                font-size: 13px;

                svg {
                    width: 14px;
                    height: 14px;
                }
            }
        }

        .product-grid {
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
        }
    }
}

@media (max-width: 640px) {
    .product-section {
        padding: 14px;
        margin-bottom: 20px;
        border-radius: 8px;

        .section-header {
            margin-bottom: 14px;
            padding-bottom: 10px;
            flex-wrap: wrap;
            gap: 12px;

            &::after {
                width: 40px;
            }

            .title-wrapper {
                gap: 6px;

                .title-icon {
                    font-size: 16px;
                }

                .section-title {
                    font-size: 16px;
                }
            }

            .more-link {
                padding: 4px 8px;
                font-size: 12px;
                margin-left: auto;

                svg {
                    width: 12px;
                    height: 12px;
                }
            }
        }

        .product-grid {
            grid-template-columns: repeat(2, 1fr);
            gap: 12px;
        }
    }
}

@media (max-width: 480px) {
    .product-section {
        padding: 12px;

        .product-grid {
            grid-template-columns: 1fr;
            gap: 12px;
        }
    }
}
</style>
