<template>
    <div class="comparison-page">
        <div class="comparison-container">
            <!-- 页面标题 -->
            <div class="page-header">
                <h1 class="page-title">{{ $t('商品对比') }}</h1>
            </div>

            <!-- 加载状态 -->
            <Loading v-if="loading" :text="$t('加载中...')" />

            <!-- 对比表格 -->
            <div v-else class="comparison-table-wrapper">
                <table class="comparison-table">
                    <thead>
                        <tr>
                            <th class="attribute-col">{{ $t('对比项') }}</th>
                            <th v-for="(item, index) in comparisonSlots" :key="index" class="product-col">
                                <div v-if="item" class="product-header">
                                    <button class="btn-delete" @click="removeProduct(index)"
                                        :title="$t('删除')">×</button>
                                    <div class="product-header-content">
                                        <img :src="item.goodsImage || getDefaultImage()" :alt="item.goodsName"
                                            class="header-image" />
                                        <div class="header-name">{{ $t(item.goodsName || '') }}</div>
                                    </div>
                                </div>
                                <div v-else class="empty-slot">
                                    <div class="empty-text">{{ $t('暂无对比项') }}</div>
                                    <button class="btn-add" @click="showAddProductModal(index)">{{ $t('点击添加')
                                        }}</button>
                                </div>
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- 商品图片 -->
                        <tr>
                            <td class="attribute-name">{{ $t('商品图片') }}</td>
                            <td v-for="(item, index) in comparisonSlots" :key="index">
                                <div v-if="item" class="product-image-cell">
                                    <img :src="item.goodsImage || getDefaultImage()" :alt="item.goodsName"
                                        class="product-image" />
                                </div>
                                <span v-else class="empty-cell">-</span>
                            </td>
                        </tr>

                        <!-- 订货编码 -->
                        <tr>
                            <td class="attribute-name">{{ $t('订货编码') }}</td>
                            <td v-for="(item, index) in comparisonSlots" :key="index">
                                <span v-if="item" class="product-value">{{ item.goodsCode }}</span>
                                <span v-else class="empty-cell">-</span>
                            </td>
                        </tr>

                        <!-- 价格 -->
                        <tr>
                            <td class="attribute-name">{{ $t('价格') }}</td>
                            <td v-for="(item, index) in comparisonSlots" :key="index">
                                <span v-if="item" class="product-value price">
                                    {{ formatPriceWithSymbolSafe(item.goodsPrice) }}
                                </span>
                                <span v-else class="empty-cell">-</span>
                            </td>
                        </tr>

                        <!-- 起订量 -->
                        <tr>
                            <td class="attribute-name">{{ $t('起订量') }}</td>
                            <td v-for="(item, index) in comparisonSlots" :key="index">
                                <span v-if="item" class="product-value">{{ item.moq }}</span>
                                <span v-else class="empty-cell">-</span>
                            </td>
                        </tr>

                        <!-- 商品库存 -->
                        <tr>
                            <td class="attribute-name">{{ $t('商品库存') }}</td>
                            <td v-for="(item, index) in comparisonSlots" :key="index">
                                <span v-if="item" class="product-value">{{ item.goodsStock }}</span>
                                <span v-else class="empty-cell">-</span>
                            </td>
                        </tr>

                        <!-- 规格 -->
                        <tr>
                            <td class="attribute-name">{{ $t('规格') }}</td>
                            <td v-for="(item, index) in comparisonSlots" :key="index">
                                <span v-if="item" class="product-value">{{ item.goodsSpec }}</span>
                                <span v-else class="empty-cell">-</span>
                            </td>
                        </tr>

                        <!-- 品牌 -->
                        <tr>
                            <td class="attribute-name">{{ $t('品牌') }}</td>
                            <td v-for="(item, index) in comparisonSlots" :key="index">
                                <span v-if="item" class="product-value">{{ $t(item.brandName || '') }}</span>
                                <span v-else class="empty-cell">-</span>
                            </td>
                        </tr>

                        <!-- 发货日 -->
                        <tr>
                            <td class="attribute-name">{{ $t('发货日') }}</td>
                            <td v-for="(item, index) in comparisonSlots" :key="index">
                                <span v-if="item" class="product-value">{{ formatDeliveryDate(item.deliveryDate)
                                    }}</span>
                                <span v-else class="empty-cell">-</span>
                            </td>
                        </tr>

                        <!-- 操作 -->
                        <tr>
                            <td class="attribute-name">{{ $t('操作') }}</td>
                            <td v-for="(item, index) in comparisonSlots" :key="index">
                                <div v-if="item" class="action-cell">
                                    <div class="quantity-controls">
                                        <button class="btn-decrease" @click="decreaseQuantity(index)"
                                            :disabled="item.quantity <= item.moq">-</button>
                                        <input type="number" v-model.number="item.quantity" :min="item.moq"
                                            :max="item.goodsStock" class="quantity-input"
                                            @blur="validateQuantity(item)" />
                                        <button class="btn-increase" @click="increaseQuantity(index)"
                                            :disabled="item.quantity >= item.goodsStock">+</button>
                                    </div>
                                    <button class="btn-add-cart" :disabled="addingToCartMap[item.goodsId]"
                                        @click="handleAddToCart(item, index)">
                                        {{ addingToCartMap[item.goodsId] ? $t('处理中...') : $t('加入购物车') }}
                                    </button>
                                </div>
                                <span v-else class="empty-cell">-</span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import { selectGoodsContrast, type GoodsContrastItem } from '~/services/goods.service'
import { insertShoppingCart } from '~/services/order.service'
import { useBrowserId } from '~/composables/useBrowserId'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import { Toast } from '~/composables/useToast'
import { ConfirmModal } from '~/composables/useConfirmModal'
import { useIsLogin } from '~/composables/useAuth'
import { useMallStore } from '~/stores'
import { useComparisonStore } from '~/stores/modules/comparison'

const route = useRoute()
const mallStore = useMallStore()
const comparisonStore = useComparisonStore()

// 获取路由参数
const commonId = computed(() => {
    const id = route.query.commonId as string
    return id ? Number(id) : null
})

// 状态管理
const loading = ref(false)
const comparisonData = ref<GoodsContrastItem[]>([])
const addingToCartMap = ref<Record<number, boolean>>({})

// 对比槽位（最多4个）
const comparisonSlots = computed(() => {
    const slots: (GoodsContrastItem & { quantity: number } | null)[] = []
    // 填充已有商品
    comparisonData.value.forEach(item => {
        slots.push({
            ...item,
            quantity: item.goodsNum || item.moq || 1
        })
    })
    // 填充空位到4个
    while (slots.length < 4) {
        slots.push(null)
    }
    return slots
})

// 获取默认图片
const getDefaultImage = (): string => {
    return mallStore.getConfigValueByKey('logo_copywriting_ico_goods') || ''
}

// 格式化发货日期
const formatDeliveryDate = (date: number | undefined): string => {
    if (date === undefined || date === null) {
        return '--'
    }
    if (date === 0) {
        return '现货'
    }
    return `${date}天`
}

// 获取对比商品列表
const fetchComparisonData = async () => {
    if (!commonId.value) {
        Toast.error('缺少商品ID参数')
        return
    }

    loading.value = true
    try {
        const response = await selectGoodsContrast({
            commonId: String(commonId.value),
            browserId: useBrowserId()
        })

        if (response?.code === 1 && response.data?.body) {
            comparisonData.value = response.data.body.map((item: GoodsContrastItem) => ({
                ...item,
                quantity: item.goodsNum || item.moq || 1
            }))
        } else {
            Toast.error(response?.message || '获取对比数据失败')
            comparisonData.value = []
        }
    } catch (error: any) {
        console.error('获取对比数据失败:', error)
        Toast.error(error?.message || '获取对比数据失败，请稍后重试')
        comparisonData.value = []
    } finally {
        loading.value = false
    }
}

// 减少数量
const decreaseQuantity = (index: number) => {
    const item = comparisonSlots.value[index]
    if (item && item.quantity > item.moq) {
        item.quantity--
        validateQuantity(item)
    }
}

// 增加数量
const increaseQuantity = (index: number) => {
    const item = comparisonSlots.value[index]
    if (item && item.quantity < item.goodsStock) {
        item.quantity++
        validateQuantity(item)
    }
}

// 验证数量
const validateQuantity = (item: GoodsContrastItem & { quantity: number }) => {
    if (item.quantity < item.moq) {
        item.quantity = item.moq
    }
    if (item.quantity > item.goodsStock) {
        item.quantity = item.goodsStock
    }
}

// 删除商品
const removeProduct = async (index: number) => {
    const item = comparisonSlots.value[index]
    if (!item) return

    try {
        // 调用状态管理的删除方法，会同步删除状态管理中的商品
        const success = await comparisonStore.removeItemWithApi(item.goodsId)
        if (success) {
            // 同步删除本地数据
            const dataIndex = comparisonData.value.findIndex(d => d.goodsId === item.goodsId)
            if (dataIndex > -1) {
                comparisonData.value.splice(dataIndex, 1)
            }
        }
    } catch (error) {
        console.error('删除失败:', error)
    }
}

// 显示添加商品弹窗（暂时跳转到商品详情页）
const showAddProductModal = (index: number) => {
    const firstGoodsId = comparisonData.value[0]?.goodsId
    if (firstGoodsId) {
        // 跳转到商品详情页，让用户选择商品
        navigateTo(`/product/${firstGoodsId}`)
    } else {
        Toast.warning('无法添加商品')
    }
}

// 加入购物车
const handleAddToCart = async (item: GoodsContrastItem & { quantity: number }, index: number) => {
    // 检查登录状态
    if (!useIsLogin(route.fullPath)) {
        return
    }

    const goodsId = item.goodsId
    const moq = item.moq || 1
    const goodsNum = item.quantity

    // 设置加载状态
    addingToCartMap.value[goodsId] = true

    try {
        // 加入购物车
        const response = await insertShoppingCart({
            goodsId,
            goodsNum,
            moq
        })

        if (response?.code === 1) {
            useGoodsStore().fetchCartCount()
            showSuccessModal('立即去购物车查看', '/cart/shopping')
        } else {
            Toast.error(response?.message || '加入购物车失败，请稍后重试')
        }
    } catch (error: any) {
        console.error('加入失败:', error)
        Toast.error(error?.message || '加入购物车失败，请稍后重试')
    } finally {
        addingToCartMap.value[goodsId] = false
    }
}

// 显示成功确认框
const showSuccessModal = (message: string, navigatePath?: string) => {
    ConfirmModal.show({
        message,
        onKeepShopping: () => {
            // 再逛逛，关闭弹窗即可
        },
        onGoNow: () => {
            if (navigatePath) {
                navigateTo(navigatePath)
            }
        }
    })
}

// 页面加载时获取数据
onMounted(() => {
    fetchComparisonData()
})
</script>

<style lang="scss" scoped>
.comparison-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 20px 0 40px;
}

.comparison-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

.page-header {
    margin-bottom: 20px;

    .page-title {
        font-size: 24px;
        font-weight: 600;
        color: #333;
        margin: 0;
    }
}

.comparison-table-wrapper {
    background-color: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.comparison-table {
    width: 100%;
    border-collapse: collapse;

    thead {
        background-color: #f5f7fa;

        th {
            padding: 15px;
            text-align: center;
            font-size: 14px;
            font-weight: 500;
            color: #333;
            border-right: 1px solid #e8e8e8;
            border-bottom: 1px solid #e8e8e8;

            &:last-child {
                border-right: none;
            }

            &.attribute-col {
                width: 120px;
                background-color: #fafafa;
            }

            &.product-col {
                min-width: 200px;
            }
        }

        .product-header {
            position: relative;
            padding: 10px;

            .btn-delete {
                position: absolute;
                top: 5px;
                right: 5px;
                width: 24px;
                height: 24px;
                border-radius: 50%;
                background-color: #f5222d;
                color: #fff;
                border: none;
                cursor: pointer;
                font-size: 18px;
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

            .product-header-content {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 8px;
                padding-top: 20px;

                .header-image {
                    width: 80px;
                    height: 80px;
                    object-fit: cover;
                    border-radius: 4px;
                }

                .header-name {
                    font-size: 12px;
                    color: #333;
                    text-align: center;
                    line-height: 1.4;
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    line-clamp: 2;
                    -webkit-box-orient: vertical;
                    overflow: hidden;
                    word-break: break-word;
                    max-width: 150px;
                }
            }
        }

        .empty-slot {
            padding: 40px 20px;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 15px;

            .empty-text {
                font-size: 14px;
                color: #999;
            }

            .btn-add {
                padding: 8px 16px;
                background-color: #f5f5f5;
                color: #666;
                border: 1px solid #d9d9d9;
                border-radius: 4px;
                font-size: 12px;
                cursor: pointer;
                transition: all 0.3s;

                &:hover {
                    background-color: #e8e8e8;
                    border-color: var(--color, #1890ff);
                    color: var(--color, #1890ff);
                }
            }
        }
    }

    tbody {
        tr {
            border-bottom: 1px solid #f0f0f0;

            &:last-child {
                border-bottom: none;
            }

            &:hover {
                background-color: #fafbfc;
            }

            td {
                padding: 15px;
                text-align: center;
                font-size: 14px;
                color: #666;
                border-right: 1px solid #f0f0f0;

                &:last-child {
                    border-right: none;
                }

                &.attribute-name {
                    background-color: #fafafa;
                    font-weight: 500;
                    color: #333;
                    text-align: left;
                }

                .product-value {
                    color: #333;

                    &.price {
                        color: #f5222d;
                        font-weight: 500;
                    }
                }

                .empty-cell {
                    color: #999;
                }
            }
        }
    }

    .product-image-cell {
        display: flex;
        justify-content: center;
        align-items: center;

        .product-image {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 4px;
        }
    }

    .action-cell {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 10px;

        .quantity-controls {
            display: flex;
            align-items: center;
            gap: 5px;

            .btn-decrease,
            .btn-increase {
                width: 28px;
                height: 28px;
                border: 1px solid #d9d9d9;
                background-color: #fff;
                cursor: pointer;
                font-size: 16px;
                color: #666;
                border-radius: 4px;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: all 0.3s;

                &:hover:not(:disabled) {
                    border-color: var(--color);
                    color: var(--color);
                }

                &:disabled {
                    opacity: 0.5;
                    cursor: not-allowed;
                }
            }

            .quantity-input {
                width: 60px;
                height: 28px;
                border: 1px solid #d9d9d9;
                border-radius: 4px;
                text-align: center;
                font-size: 14px;
                color: #333;

                &:focus {
                    outline: none;
                    border-color: var(--color);
                    box-shadow: 0 0 0 2px rgba(10, 140, 240, 0.1);
                }
            }
        }

        .btn-add-cart {
            padding: 8px 16px;
            background-color: #f5222d;
            color: #fff;
            border: none;
            border-radius: 4px;
            font-size: 12px;
            cursor: pointer;
            transition: all 0.3s;
            width: 100%;
            max-width: 120px;

            &:hover:not(:disabled) {
                background-color: #ff4d4f;
            }

            &:disabled {
                opacity: 0.6;
                cursor: not-allowed;
            }
        }
    }
}

// 响应式设计
@media (max-width: 768px) {
    .comparison-container {
        padding: 0 12px;
    }

    .comparison-table-wrapper {
        overflow-x: auto;
    }

    .comparison-table {
        min-width: 800px;
    }
}
</style>
