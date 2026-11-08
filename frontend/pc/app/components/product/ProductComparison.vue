<template>
    <!-- 对比栏 -->
    <div v-if="isVisible" class="product-comparison">
        <div class="comparison-container">
            <!-- 标题 -->
            <div class="comparison-title">{{ $t('商品对比') }}</div>

            <!-- 商品列表 -->
            <div class="comparison-items">
                <div v-for="item in comparisonItems" :key="item.goodsId" class="comparison-item">
                    <img :src="item.goodsImage || getDefaultImage()" :alt="item.goodsName" class="item-image" />
                    <div class="item-info">
                        <div class="item-name">{{ $t(item.goodsName || '') }}</div>
                        <div class="item-price">
                            {{ formatPriceWithSymbolSafe(item.commonPrice) }}
                        </div>
                    </div>
                    <button class="btn-remove" @click="removeItem(item.goodsId)">×</button>
                </div>
            </div>

            <!-- 操作按钮 -->
            <div class="comparison-actions">
                <button class="btn-compare" @click="handleCompare" :disabled="comparisonItems.length < 2">
                    {{ $t('对比') }}
                </button>
                <div class="action-links">
                    <a href="javascript:;" class="link-clear" @click="clearItems">{{ $t('清空对比栏') }}</a>
                    <a href="javascript:;" class="link-hide" @click="hideComparison">{{ $t('隐藏') }}</a>
                </div>
            </div>
        </div>
    </div>

    <!-- 打开对比栏按钮（当对比栏隐藏且有商品时显示） -->
    <button v-if="showOpenButton" class="open-comparison-btn" @click="showComparison">
        <span class="btn-text">{{ $t('打开对比栏') }}</span>
    </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useComparisonStore } from '~/stores/modules/comparison'
import { useMallStore } from '~/stores'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import { Toast } from '~/composables/useToast'

const comparisonStore = useComparisonStore()
const mallStore = useMallStore()

// 计算属性
const comparisonItems = computed(() => comparisonStore.getItems)
const isVisible = computed(() => comparisonStore.isVisible)

// 是否显示打开对比栏按钮（当对比栏隐藏且有商品时）
const showOpenButton = computed(() => {
    return !comparisonStore.visible && comparisonItems.value.length > 0
})

// 获取默认图片
const getDefaultImage = (): string => {
    return mallStore.getConfigValueByKey('logo_copywriting_ico_goods') || ''
}

// 移除商品（使用 store 的统一方法）
const removeItem = async (goodsId: number) => {
    await comparisonStore.removeItemWithApi(goodsId)
}

// 清空对比列表（使用 store 的统一方法）
const clearItems = async () => {
    await comparisonStore.clearItemsWithApi()
}

// 隐藏对比栏
const hideComparison = () => {
    comparisonStore.setVisible(false)
}

// 显示对比栏
const showComparison = () => {
    comparisonStore.setVisible(true)
}

// 进行对比
const handleCompare = () => {
    if (comparisonItems.value.length < 2) {
        Toast.warning('至少需要2个商品才能进行对比')
        return
    }
    // 跳转到对比页面
    const commonId = comparisonStore.currentCommonId
    if (commonId) {
        navigateTo(`/product/comparison?commonId=${commonId}`)
    } else {
        Toast.warning('')
    }
}
</script>

<style lang="scss" scoped>
.product-comparison {
    position: fixed;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    z-index: 1000;
    background-color: #fff;
    border-top: 1px solid #e8e8e8;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
    padding: 15px 20px;
    width: 100%;
    max-width: 1200px;

    .comparison-container {
        width: 100%;
        display: flex;
        align-items: center;
        gap: 20px;
    }

    .comparison-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        white-space: nowrap;
    }

    .comparison-items {
        flex: 1;
        display: flex;
        gap: 15px;
        overflow-x: auto;
        padding: 5px 0;

        &::-webkit-scrollbar {
            height: 4px;
        }

        &::-webkit-scrollbar-thumb {
            background-color: #d9d9d9;
            border-radius: 2px;
        }

        .comparison-item {
            position: relative;
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 8px;
            border: 1px solid #e8e8e8;
            border-radius: 4px;
            background-color: #fafafa;
            min-width: 200px;
            flex-shrink: 0;

            .item-image {
                width: 60px;
                height: 60px;
                object-fit: cover;
                border-radius: 4px;
            }

            .item-info {
                flex: 1;
                min-width: 0;
                width: 120px;

                .item-name {
                    font-size: 12px;
                    color: #333;
                    margin-bottom: 5px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    line-clamp: 2;
                    -webkit-box-orient: vertical;
                    line-height: 1.4;
                    max-height: calc(1.4em * 2);
                    word-break: break-word;
                    width: 100%;
                }

                .item-price {
                    font-size: 14px;
                    color: #f5222d;
                    font-weight: 500;
                }
            }

            .btn-remove {
                position: absolute;
                top: -8px;
                right: -8px;
                width: 20px;
                height: 20px;
                border-radius: 50%;
                background-color: #f5222d;
                color: #fff;
                border: none;
                cursor: pointer;
                font-size: 16px;
                line-height: 1;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: all 0.3s;

                &:hover {
                    background-color: #ff4d4f;
                    transform: scale(1.1);
                }
            }
        }
    }

    .comparison-actions {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 8px;

        .btn-compare {
            padding: 8px 24px;
            background-color: #f5222d;
            color: #fff;
            border: none;
            border-radius: 4px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s;

            &:hover:not(:disabled) {
                background-color: #ff4d4f;
            }

            &:disabled {
                opacity: 0.6;
                cursor: not-allowed;
            }
        }

        .action-links {
            display: flex;
            gap: 15px;
            font-size: 12px;

            .link-clear,
            .link-hide {
                color: #666;
                text-decoration: none;
                cursor: pointer;
                transition: color 0.3s;

                &:hover {
                    color: var(--color, #1890ff);
                }
            }
        }
    }
}

// 打开对比栏按钮
.open-comparison-btn {
    position: fixed;
    bottom: 100px;
    right: 20px;
    z-index: 1000;
    width: 60px;
    height: 120px;
    background-color: #f5f5f5;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 10px 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s;

    &:hover {
        background-color: #e8e8e8;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transform: translateY(-2px);
    }

    .btn-text {
        writing-mode: vertical-rl;
        text-orientation: upright;
        font-size: 14px;
        font-weight: 500;
        color: #f5222d;
        letter-spacing: 2px;
        line-height: 1.4;
    }
}

// 响应式设计
@media (max-width: 768px) {
    .product-comparison {
        .comparison-container {
            flex-direction: column;
            align-items: flex-start;
        }

        .comparison-items {
            width: 100%;
        }

        .comparison-actions {
            width: 100%;
            flex-direction: row;
            justify-content: space-between;
            align-items: center;
        }
    }

    .open-comparison-btn {
        bottom: 15px;
        right: 15px;
        width: 50px;
        height: 100px;

        .btn-text {
            font-size: 12px;
        }
    }
}
</style>
