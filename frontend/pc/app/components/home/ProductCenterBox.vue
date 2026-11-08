<template>
    <div class="product-center-box">
        <!-- 标题区域 -->
        <div class="product-center-header">
            <h2 class="product-center-title">{{ $t('产品中心') }}</h2>
        </div>

        <!-- 产品分类网格 -->
        <div class="product-grid">
            <template v-for="(product, index) in products" :key="index">
                <div class="product-item" v-if="product.isSwitch" @click="handleProductClick(product)">
                    <div class="product-title">{{ $t(product.name || '') }}</div>
                    <div class="product-image">
                        <img :src="product.images" :alt="product.name" />
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import type { PropType } from 'vue'

/** 产品项 */
interface ProductCenterProduct {
    isSwitch?: number
    name?: string
    images?: string
    url?: string
    link?: string
}

/** 产品中心数据项 */
interface ProductCenterDataItem {
    product?: ProductCenterProduct[]
    [key: string]: any
}

const props = defineProps({
    dataInfo: {
        type: Array as PropType<ProductCenterDataItem[]>,
        default: () => []
    }
})

// 产品分类数据
const products = computed(() => props.dataInfo?.[0]?.product ?? [])

const handleProductClick = (product: ProductCenterProduct) => {
    const url = (product?.url || product?.link)?.trim()
    if (url) {
        const isExternal = /^https?:\/\//i.test(url)
        navigateTo(url, isExternal ? { external: true, open: { target: '_blank' } } : undefined)
    }
}

const state = reactive({

})

onMounted(() => {

})

defineExpose({ state })

</script>

<style lang="scss" scoped>
.product-center-box {
    width: 100%;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    padding-top: 20px;
    margin-bottom: 20px;

    .product-center-header {
        display: flex;
        align-items: center;
        justify-content: center;

        .product-center-title {
            margin: 0;
            font-size: 20px;
            font-weight: bold;
            color: var(--color);
        }
    }

    .product-grid {
        display: grid;
        grid-template-columns: repeat(6, 1fr);
        gap: 20px;
        padding: 20px;

        .product-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 15px;
            border-radius: 6px;
            background-color: #f8f9fa;
            cursor: pointer;
            transition: all 0.2s ease;

            &:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                background-color: white;
            }

            .product-image {
                width: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 10px;

                img {
                    max-width: 100%;
                    max-height: 100%;
                    object-fit: contain;
                }
            }

            .product-title {
                font-size: 16px;
                color: #333;
                text-align: center;
                font-weight: bold;
            }
        }
    }
}

// 响应式设计
@media (max-width: 1200px) {
    .product-center-box {
        .product-grid {
            grid-template-columns: repeat(4, 1fr);
        }
    }
}

@media (max-width: 992px) {
    .product-center-box {
        .product-grid {
            grid-template-columns: repeat(3, 1fr);
            gap: 15px;
            padding: 15px;
        }
    }
}

@media (max-width: 768px) {
    .product-center-box {
        .product-grid {
            grid-template-columns: repeat(2, 1fr);
            gap: 12px;
            padding: 12px;
        }
    }
}

@media (max-width: 480px) {
    .product-center-box {
        .product-grid {
            grid-template-columns: repeat(1, 1fr);
        }
    }
}
</style>