<template>
    <div class="shopping-container">
        <!-- 表头 -->
        <div class="cart-table-header">
            <div class="table-col col-check" v-if="showCheckboxes"></div>
            <div class="table-col col-info">{{ $t('商品信息') }}</div>
            <div class="table-col col-price">{{ $t('单价(元)') }}</div>
            <div class="table-col col-quantity">{{ $t('数量') }}</div>
            <div class="table-col col-subtotal">{{ $t('小计(元)') }}</div>
            <div class="table-col col-action">{{ $t('操作') }}</div>
        </div>

        <!-- 商品列表 -->
        <div class="cart-goods-list">
            <div class="shop-group" v-for="shop in shopList" :key="shop.shopId">
                <div v-if="showShopHeader" class="shop-header">
                    <label v-if="showCheckboxes" class="check-box">
                        <input type="checkbox" :checked="shop.checked === true"
                            :disabled="getValidGoodsList(shop).length === 0"
                            @change="(e) => handleShopCheck(shop, (e.target as HTMLInputElement).checked)">
                        <span class="check-icon"></span>
                    </label>
                    <span class="shop-name" @click="handleShopNameClick(shop)">{{ $t(shop.shopName || '') }}</span>
                </div>

                <div class="goods-list">
                    <div class="goods-item" :class="{ 'goods-disabled': !!goods.invalid }"
                        v-for="goods in shop.goodsList" :key="goods.goodsId">
                        <div class="goods-check" v-if="showCheckboxes">
                            <label class="check-box">
                                <input type="checkbox" :checked="goods.checked === true" :disabled="!!goods.invalid"
                                    @change="(e) => handleGoodsCheck(shop, goods, (e.target as HTMLInputElement).checked)">
                                <span class="check-icon"></span>
                            </label>
                        </div>

                        <div class="goods-info">
                            <div class="goods-image-wrap">
                                <img :src="goods.goodsImage || ''" :alt="goods.goodsName || ''" class="goods-image">
                                <div class="offline-badge" v-if="!!goods.invalid">
                                    <span class="offline-text">{{ $t('已下架') }}</span>
                                </div>
                            </div>
                            <div class="goods-details">
                                <div class="goods-name-wrapper">
                                    <!-- 活动标识（仅有效商品显示） -->
                                    <span class="activity-badge" v-if="!goods.invalid && getActivityLabel(goods)"
                                        :class="getActivityClass(goods)">
                                        {{ $t(getActivityLabel(goods)) }}
                                    </span>
                                    <div class="goods-name" :class="{ 'goods-name-disabled': !!goods.invalid }"
                                        @click="handleGoodsNameClick(goods)">
                                        {{ $t(goods.goodsName || '') || $t('商品名称') }}
                                    </div>
                                </div>
                                <div class="goods-sku" v-if="goods.goodsCode">{{ $t('订货编号') }}: {{ goods.goodsCode }}
                                </div>
                                <div class="goods-spec" v-if="goods.goodsSpecChineseValue || goods.goodsSpec">
                                    {{ $t('规格') }}: {{ goods.goodsSpecChineseValue || goods.goodsSpec }}
                                </div>
                            </div>
                        </div>

                        <div class="goods-price">
                            <div class="price-details">
                                <div class="price-item">
                                    <span class="price-value">{{ formatPriceWithSymbol(goods.goodsPrice || 0) }}</span>
                                </div>
                            </div>
                        </div>

                        <div class="goods-quantity">
                            <UInputNumber v-if="!goods.invalid" v-model="goods.goodsNum" :min="getMinQuantity(goods)"
                                :max="getMaxQuantity(goods)" @change="handleQuantityChange(shop, goods)"
                                @update:modelValue="handleQuantityChange(shop, goods)" />
                            <span v-else class="quantity-readonly">{{ goods.goodsNum ?? 0 }}</span>
                        </div>

                        <div class="goods-subtotal">
                            <span class="subtotal-value">{{ formatPriceWithSymbol(getSubtotal(goods)) }}</span>
                        </div>

                        <div class="goods-action">
                            <button type="button" class="delete-btn" @click="deleteGoods(shop, goods)">
                                {{ $t('删除') }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, nextTick } from 'vue'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import type { GoodsListProps, CartShopItem } from '~/types/component'
import type { UserShoppingCartDTO } from '~/types/order'
// 类型定义
type ShopItem = CartShopItem
type Props = GoodsListProps

const props = withDefaults(defineProps<Props>(), {
    allChecked: false,
    showShopHeader: true,
    showCheckboxes: true,
})

const emit = defineEmits<{
    'update:allChecked': [value: boolean]
    delete: [shop: ShopItem, goods: UserShoppingCartDTO]
    quantityChange: [shop: ShopItem, goods: UserShoppingCartDTO]
    shopCheck: [shop: ShopItem, checked: boolean]
}>()

type CartActivityGoods = UserShoppingCartDTO & {
    commonIsXian?: number
    activityGoodsDetail?: {
        activityStatus?: number | string | null
    } | null
}

const getActivityStatus = (goods: CartActivityGoods): number => {
    const status = Number(goods.activityGoodsDetail?.activityStatus)
    if (Number.isFinite(status) && status > 0) return status
    return Number(goods.commonIsXian || 0)
}

// 获取活动标识文字
const getActivityLabel = (goods: CartActivityGoods): string => {
    const activityMap: Record<number, string> = {
        1: '砍价',
        2: '限时秒杀',
        3: '限时折扣',
        4: '拼团',
        5: '满减赠',
        6: '满减折',
        10: '组合商品'
    }
    const status = getActivityStatus(goods)
    return status ? activityMap[status] || '' : ''
}

// 获取活动标识样式类（与 getActivityLabel 的 activityStatus 取值一致：1/2/3/4/5/6/10）
const getActivityClass = (goods: CartActivityGoods): string => {
    const classMap: Record<number, string> = {
        1: 'activity-bargain', // 砍价
        2: 'activity-seckill', // 限时秒杀
        3: 'activity-discount', // 限时折扣
        4: 'activity-group', // 拼团
        5: 'activity-full-gift', // 满减赠
        6: 'activity-full-discount', // 满减折
        10: 'activity-combination' // 组合商品
    }
    const status = getActivityStatus(goods)
    return status ? classMap[status] || '' : ''
}

// 获取小计：数量 * 价格（goods.goodsPrice）
const getSubtotal = (goods: UserShoppingCartDTO): number => {
    const price = goods.goodsPrice || 0
    const quantity = goods.goodsNum || 0
    return price * quantity
}

// 点击店铺名称跳转到店铺详情页
const handleShopNameClick = (shop: ShopItem) => {
    if (shop.shopId) {
        navigateTo(`/store/${shop.shopId}`)
    }
}

// 点击商品名称跳转到商品详情页
const handleGoodsNameClick = (goods: UserShoppingCartDTO) => {
    // 已下架商品不能点击
    if (!isValidGoods(goods) || !goods.goodsId) {
        return
    }
    navigateTo(`/product/${goods.goodsId}`)
}

// 获取最小数量
const getMinQuantity = (goods: UserShoppingCartDTO): number => {
    return goods.moq || 1
}

// 获取最大数量
const getMaxQuantity = (goods: UserShoppingCartDTO): number => {
    const stock = goods.goodsStock || goods.commonStock || Infinity
    const maxSale = goods.goodsMaxSale || Infinity
    return Math.min(stock, maxSale)
}

// “有效”仅用于勾选/全选逻辑：下架商品（goods.invalid===true）不参与
const isValidGoods = (goods: UserShoppingCartDTO & { invalid?: boolean }): boolean => !goods.invalid

// 获取店铺可参与勾选的商品列表（过滤下架）
const getValidGoodsList = (shop: ShopItem) => shop.goodsList.filter(goods => isValidGoods(goods))

// 检查店铺是否全选（只检查有效商品）
const isShopAllChecked = (shop: ShopItem): boolean => {
    const validGoodsList = getValidGoodsList(shop)
    if (validGoodsList.length === 0) return false
    // 确保所有有效商品都被选中
    return validGoodsList.every(goods => goods.checked === true)
}

// 更新店铺选中状态
const updateShopChecked = (shop: ShopItem) => {
    const newChecked = isShopAllChecked(shop)
    // 只有当状态真正改变时才更新，避免不必要的响应式触发
    if (shop.checked !== newChecked) {
        shop.checked = newChecked
    }
}

// 更新全选状态
const updateAllChecked = () => {
    if (props.shopList.length === 0) {
        emit('update:allChecked', false)
        return
    }
    // 检查所有店铺是否都全选（只检查有有效商品的店铺）
    const shopsWithValidGoods = props.shopList.filter(shop => getValidGoodsList(shop).length > 0)

    if (shopsWithValidGoods.length === 0) {
        emit('update:allChecked', false)
        return
    }

    const isAllChecked = shopsWithValidGoods.every(shop => isShopAllChecked(shop))
    emit('update:allChecked', isAllChecked)
}

// 店铺复选框变化：同步该店铺下所有有效商品的选中状态，并通知父组件
const handleShopCheck = (shop: ShopItem, checked: boolean) => {
    shop.checked = checked
    const validList = getValidGoodsList(shop)
    validList.forEach((goods) => {
        goods.checked = checked
    })
    emit('shopCheck', shop, checked)
    updateAllChecked()
}

// 商品复选框变化
const handleGoodsCheck = (shop: ShopItem, goods: UserShoppingCartDTO & { checked?: boolean }, checked: boolean) => {
    if (!isValidGoods(goods)) return
    goods.checked = checked
    nextTick(() => {
        updateShopChecked(shop)
        updateAllChecked()
    })
}

// 数量变化
const handleQuantityChange = (shop: ShopItem, goods: UserShoppingCartDTO) => {
    // 下架商品不允许修改数量
    if (!isValidGoods(goods)) {
        return
    }
    // 更新商品小计（本地预览）：数量 * 价格
    goods.goodsTotalPrice = (goods.goodsPrice || 0) * (goods.goodsNum || 0)
    // 触发事件，让父组件调用接口
    emit('quantityChange', shop, goods)
}

// 删除商品（含已下架，便于用户清理购物车）
const deleteGoods = (shop: ShopItem, goods: UserShoppingCartDTO) => {
    emit('delete', shop, goods)
}
</script>

<style lang="scss" scoped>
.shopping-container {
    .cart-table-header {
        display: grid;
        grid-template-columns: 60px 1fr 140px 150px 150px 100px;
        gap: 16px;
        align-items: center;
        background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
        border: 1px solid #e8e8e8;
        border-radius: 8px;
        padding: 18px 20px;
        margin-bottom: 20px;
        font-size: 14px;
        font-weight: 600;
        color: #333;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);

        .table-col {
            display: flex;
            align-items: center;

            &.col-check {
                justify-content: center;
            }

            &.col-price,
            &.col-quantity,
            &.col-subtotal,
            &.col-action {
                justify-content: center;
            }
        }
    }

    .cart-goods-list {
        .shop-group {
            background-color: #fff;
            border-radius: 8px;
            margin-bottom: 20px;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
            border: 1px solid #e8e8e8;
            overflow: hidden;
            transition: box-shadow 0.3s ease;

            &:hover {
                box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
            }

            .shop-header {
                display: flex;
                align-items: center;
                padding: 14px 20px;
                background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
                border-bottom: 1px solid #e8e8e8;
                transition: background 0.2s;

                &:hover {
                    background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
                }

                .check-box {
                    margin-right: 12px;
                }

                .shop-name {
                    font-size: 15px;
                    color: #333;
                    font-weight: 600;
                    letter-spacing: 0.3px;
                    cursor: pointer;
                    transition: color 0.2s;

                    &:hover {
                        color: var(--primary-color, #1890ff);
                    }
                }
            }

            .goods-list {
                .goods-item {
                    position: relative;
                    display: grid;
                    grid-template-columns: 60px 1fr 140px 150px 150px 100px;
                    gap: 16px;
                    align-items: center;
                    padding: 20px;
                    border-bottom: 1px solid #f0f0f0;
                    transition: all 0.3s ease;
                    background-color: #fff;

                    &:last-child {
                        border-bottom: none;
                    }

                    &:hover:not(.goods-disabled) {
                        background-color: #fafbfc;
                        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
                        transform: translateY(-1px);
                    }

                    &.goods-disabled {
                        // opacity: 0.65;
                        background-color: #f9f9f9;
                        cursor: not-allowed;

                        .goods-check,
                        .goods-quantity {
                            pointer-events: none;
                        }
                    }

                    .goods-check {
                        display: flex;
                        justify-content: center;
                    }

                    .goods-info {
                        display: flex;
                        align-items: center;
                        gap: 16px;
                        min-width: 0;

                        .goods-image-wrap {
                            position: relative;
                            flex-shrink: 0;
                            width: 90px;
                            height: 90px;
                            border-radius: 8px;
                            overflow: hidden;
                            border: 1px solid #e8e8e8;
                            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
                        }

                        .goods-image {
                            display: block;
                            width: 100%;
                            height: 100%;
                            object-fit: cover;
                            background-color: #fafafa;
                            transition: all 0.3s ease;

                            &:hover {
                                transform: scale(1.05);
                                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                            }
                        }

                        .offline-badge {
                            position: absolute;
                            left: 0;
                            right: 0;
                            bottom: 0;
                            z-index: 2;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 4px 6px;
                            background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.55) 40%, rgba(0, 0, 0, 0.72) 100%);
                            pointer-events: none;

                            .offline-text {
                                font-size: 11px;
                                color: #fff;
                                font-weight: 600;
                                white-space: nowrap;
                                letter-spacing: 0.5px;
                                text-shadow: 0 1px 2px rgba(0, 0, 0, 0.35);
                            }
                        }

                        .goods-details {
                            flex: 1;
                            min-width: 0;

                            .goods-name-wrapper {
                                display: flex;
                                align-items: center;
                                gap: 8px;
                                margin-bottom: 8px;
                                flex-wrap: wrap;

                                .goods-name {
                                    font-size: 14px;
                                    color: #333;
                                    line-height: 1.5;
                                    overflow: hidden;
                                    text-overflow: ellipsis;
                                    display: -webkit-box;
                                    -webkit-line-clamp: 2;
                                    line-clamp: 2;
                                    -webkit-box-orient: vertical;
                                    flex: 1;
                                    min-width: 0;
                                    cursor: pointer;
                                    transition: color 0.2s;

                                    &:hover:not(.goods-name-disabled) {
                                        color: var(--color);
                                    }

                                    &.goods-name-disabled {
                                        cursor: not-allowed;
                                        color: #999;
                                        opacity: 0.6;
                                    }
                                }

                                .activity-badge {
                                    display: inline-block;
                                    padding: 4px 10px;
                                    border-radius: 6px;
                                    font-size: 11px;
                                    font-weight: 600;
                                    white-space: nowrap;
                                    flex-shrink: 0;
                                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
                                    transition: all 0.2s ease;
                                    letter-spacing: 0.3px;

                                    &:hover {
                                        transform: translateY(-1px);
                                        box-shadow: 0 3px 6px rgba(0, 0, 0, 0.2);
                                    }

                                    &.activity-bargain {
                                        background: linear-gradient(135deg, #fa541c 0%, #ff7a45 100%);
                                        color: #fff;
                                    }

                                    &.activity-group {
                                        background: linear-gradient(135deg, #13c2c2 0%, #36cfc9 100%);
                                        color: #fff;
                                    }

                                    &.activity-member {
                                        background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
                                        color: #fff;
                                    }

                                    &.activity-discount {
                                        background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
                                        color: #fff;
                                    }

                                    &.activity-seckill {
                                        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
                                        color: #fff;
                                    }

                                    &.activity-full-gift {
                                        background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
                                        color: #fff;
                                    }

                                    &.activity-proxy {
                                        background: linear-gradient(135deg, #eb2f96 0%, #f759ab 100%);
                                        color: #fff;
                                    }

                                    &.activity-full-discount {
                                        background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
                                        color: #fff;
                                    }

                                    &.activity-combination {
                                        background: linear-gradient(135deg, #2f54eb 0%, #597ef7 100%);
                                        color: #fff;
                                    }
                                }
                            }

                            .goods-sku,
                            .goods-spec {
                                font-size: 12px;
                                color: #999;
                                margin-bottom: 4px;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                white-space: nowrap;
                            }
                        }
                    }

                    .goods-price {
                        display: flex;
                        justify-content: center;

                        .price-value {
                            font-size: 15px;
                            color: #333;
                            font-weight: 600;
                        }

                        .price-details {
                            display: flex;
                            flex-direction: column;
                            gap: 6px;
                            align-items: flex-start;
                            padding: 8px 0;

                            .price-item {
                                display: flex;
                                align-items: center;
                                gap: 6px;
                                font-size: 12px;
                                line-height: 1.5;
                                padding: 2px 0;

                                .price-label {
                                    color: #8c8c8c;
                                    white-space: nowrap;
                                    font-weight: 400;
                                    min-width: 60px;
                                }

                                .price-value {
                                    color: #262626;
                                    font-weight: 600;
                                    font-size: 13px;
                                    font-family: 'Arial', sans-serif;
                                }
                            }
                        }
                    }

                    .goods-quantity {
                        display: flex;
                        justify-content: center;
                        align-items: center;

                        .quantity-readonly {
                            font-size: 15px;
                            font-weight: 600;
                            color: #595959;
                            min-height: 32px;
                            line-height: 32px;
                        }

                        :deep(input) {
                            &:focus-visible {
                                --tw-ring-color: var(--color) !important;
                            }
                        }
                    }

                    .goods-subtotal {
                        display: flex;
                        justify-content: center;

                        .subtotal-value {
                            font-size: 16px;
                            color: var(--color);
                            font-weight: 700;
                            font-family: 'Arial', sans-serif;
                            letter-spacing: 0.3px;
                        }
                    }

                    .goods-action {
                        display: flex;
                        justify-content: center;

                        .delete-btn {
                            font-size: 13px;
                            color: #ff4d4f;
                            background: none;
                            border: none;
                            cursor: pointer;
                            padding: 6px 12px;
                            border-radius: 4px;
                            transition: all 0.2s ease;
                            font-weight: 500;

                            &:hover:not(:disabled) {
                                color: #fff;
                                background-color: #ff4d4f;
                                transform: translateY(-1px);
                                box-shadow: 0 2px 4px rgba(255, 77, 79, 0.3);
                            }

                            &:active:not(:disabled) {
                                transform: translateY(0);
                                box-shadow: 0 1px 2px rgba(255, 77, 79, 0.2);
                            }

                            &:disabled {
                                color: #d9d9d9;
                                cursor: not-allowed;
                                opacity: 0.5;
                            }
                        }
                    }
                }
            }
        }
    }
}

// 复选框样式
.check-box {
    position: relative;
    display: inline-block;
    cursor: pointer;
    user-select: none;

    input[type="checkbox"] {
        position: absolute;
        opacity: 0;
        width: 0;
        height: 0;
    }

    .check-icon {
        position: relative;
        display: inline-block;
        width: 18px;
        height: 18px;
        border: 1px solid #d9d9d9;
        border-radius: 4px;
        background-color: #fff;
        transition: all 0.2s;

        &::after {
            content: '';
            position: absolute;
            left: 5px;
            top: 2px;
            width: 4px;
            height: 8px;
            border: solid #fff;
            border-width: 0 2px 2px 0;
            transform: rotate(45deg);
            opacity: 0;
            transition: opacity 0.2s;
        }
    }

    input[type="checkbox"]:checked+.check-icon {
        background-color: var(--color);
        border-color: var(--color);

        &::after {
            opacity: 1;
        }
    }

    input[type="checkbox"]:hover+.check-icon {
        border-color: var(--color);
    }

    input[type="checkbox"]:disabled+.check-icon {
        opacity: 0.5;
        cursor: not-allowed;
    }
}

// 响应式设计
@media (max-width: 1200px) {
    .shopping-container {

        .cart-table-header,
        .goods-item {
            grid-template-columns: 50px 1fr 100px 120px 120px 80px;
            gap: 12px;
            padding: 16px;
        }
    }
}

@media (max-width: 768px) {
    .shopping-container {
        .cart-table-header {
            display: none;
        }

        .goods-item {
            grid-template-columns: 1fr;
            gap: 12px;
            padding: 16px;

            .goods-check {
                position: absolute;
                top: 16px;
                left: 16px;
            }

            .goods-info {
                padding-left: 40px;
            }

            .goods-price,
            .goods-quantity,
            .goods-subtotal,
            .goods-action {
                justify-content: flex-start;
            }
        }
    }
}
</style>
