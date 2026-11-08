<template>
    <div class="cart-container cart-type-shopping">
        <!-- 页面标题和Tab切换 -->
        <div class="cart-header">
            <div class="cart-tabs">
                <div class="tab-item" :class="{ active: activeGoodsTab === 'valid' }" @click="activeGoodsTab = 'valid'">
                    {{ $t('购物车') }}
                </div>
                <div class="tab-item" :class="{ active: activeGoodsTab === 'invalid' }"
                    @click="activeGoodsTab = 'invalid'">
                    {{ $t('下架商品') }}
                    <span class="tab-count" v-if="invalidGoodsCount > 0">({{ invalidGoodsCount }})</span>
                </div>
            </div>
        </div>

        <!-- 商品列表 -->
        <div class="cart-content">
            <Transition name="fade" mode="out-in">
                <Loading v-if="loading" key="loading" :text="$t('加载中...')" />
                <EmptyState v-else-if="displayShopList.length === 0" key="empty"
                    :text="activeGoodsTab === 'invalid' ? $t('暂无下架商品') : $t('购物车是空的')"
                    :action-text="activeGoodsTab === 'invalid' ? '' : $t('去逛逛')"
                    :action-to="activeGoodsTab === 'invalid' ? '' : '/product/list'" />
                <div v-else key="content">
                    <GoodsList :shop-list="displayShopList" :show-shop-header="activeGoodsTab === 'valid'"
                        :show-checkboxes="activeGoodsTab === 'valid'" @delete="deleteGoods"
                        @quantityChange="handleQuantityChange" @shopCheck="handleShopCheck" />
                </div>
            </Transition>
            <!-- 底部结算栏 -->
            <div class="cart-footer" v-if="activeGoodsTab === 'valid'">
                <div class="footer-left">
                    <label class="check-box">
                        <input type="checkbox" :checked="allChecked"
                            @change="(e) => setAllChecked((e.target as HTMLInputElement).checked)">
                        <span class="check-icon"></span>
                    </label>
                    <span class="check-text">{{ $t('全选') }}</span>

                    <a href="javascript:;" class="footer-link" :class="{ disabled: loading }" @click="deleteSelected">{{
                        $t('删除选择的商品') }}</a>
                </div>

                <div class="footer-right">
                    <div class="total-info">
                        <span class="total-text">{{ $t('已选择') }} {{ selectedCount }} {{ $t('件') }} {{ $t('商品') }}</span>
                        <!-- 国补预估：仅提示，最终金额以结算页为准（券/满减可能触发互斥） -->
                        <span v-if="subsidyDiscountAmount > 0" class="subsidy-hint">
                            {{ $t('预计国补') }}: <span class="subsidy-amount">-{{
                                formatPriceWithSymbol(subsidyDiscountAmount) }}</span>
                        </span>
                        <span class="total-price">
                            {{ $t('总价(不含运费)') }}: <span class="price">{{ formatPriceWithSymbol(totalPrice) }}</span>
                        </span>
                    </div>

                    <button class="submit-btn" :disabled="selectedCount === 0" @click="submitOrder">
                        {{ $t('去下单') }}
                    </button>
                </div>
            </div>

            <!-- 下架商品操作栏 -->
            <div class="cart-footer cart-footer--invalid" v-if="activeGoodsTab === 'invalid' && invalidGoodsCount > 0">
                <div class="footer-left">
                    <a href="javascript:;" class="footer-link" :class="{ disabled: loading }"
                        @click="deleteUnavailable">{{ $t('删除所有下架的商品') }}</a>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, computed, ref, watch } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import { queryShoppingCart, batchDeleteGoods, changeGoodsNum } from '~/services/order.service'
import { fetchSubsidyQuote } from '~/services/subsidy.service'
import { round2, shouldShowSubsidy, toAmount, type SubsidyQuote } from '~/utils/subsidy'
import { checkActivityGoodsLimit } from '~/services/activity.service'
import type { QueryShoppingCartData, QueryShoppingCartShopItem, UserShoppingCartDTO } from '~/types/order'
import { Toast } from '~/composables/useToast'
import { ConfirmModal } from '~/composables/useConfirmModal'
import { useGoodsStore } from '~/stores/modules/goods'
import type { ShopList, GoodsList } from '~/types/goods'
// 购物车价格在前端计算，不再调用算价接口
import type { CartGoods, CartShopItem } from '~/types/component'

const route = useRoute()
const cartType = computed(() => route.params.id)

// 获取 goods store
const goodsStore = useGoodsStore()

// 店铺类型定义
type ShopItem = CartShopItem

// 加载状态
const loading = ref(false)

// 购物车页不再维护接口算价结果

// Tab：正常商品 / 下架商品
const activeGoodsTab = ref<'valid' | 'invalid'>('valid')

// 收集选中商品的 goodsId
const collectSelectedGoodsIds = (filterFn?: (goods: CartGoods) => boolean): number[] => {
    const goodsIds: number[] = []
    state.shopList.forEach(shop => {
        shop.goodsList.forEach((goods: CartGoods) => {
            const shouldInclude = filterFn
                ? filterFn(goods)
                : (goods.checked === true && isValidGoods(goods) && goods.goodsId)
            if (shouldInclude && goods.goodsId) {
                goodsIds.push(goods.goodsId)
            }
        })
    })
    return goodsIds
}

// 下架商品数量（用于 Tab 展示）
const invalidGoodsCount = computed(() => {
    return state.invalidShopList.reduce((sum, shop) => sum + (shop.goodsList?.length || 0), 0)
})

// 当前展示的店铺列表（正常/下架）
const displayShopList = computed<ShopItem[]>(() => {
    return activeGoodsTab.value === 'invalid' ? state.invalidShopList : state.shopList
})

// 根据购物车类型调用对应的删除接口
const callDeleteApi = async (goodsIds: number[]) => {
    return await batchDeleteGoods({ goodsId: goodsIds })
}

// 更新删除后的本地数据
const updateDataAfterDelete = (deletedGoodsIds: number[]) => {
    state.shopList.forEach(shop => {
        shop.goodsList = shop.goodsList.filter((goods: CartGoods) => {
            return !(goods.goodsId && deletedGoodsIds.includes(goods.goodsId))
        })
        // 更新店铺选中状态
        const validGoodsList = getValidGoodsList(shop)
        if (validGoodsList.length === 0) {
            shop.checked = false
        } else {
            shop.checked = validGoodsList.every((g: CartGoods) => g.checked === true)
        }
    })
    // 过滤掉没有商品的店铺
    state.shopList = state.shopList.filter(shop => shop.goodsList.length > 0)

    // 同步下架商品列表的删除
    state.invalidShopList.forEach(shop => {
        shop.goodsList = shop.goodsList.filter((goods: CartGoods) => {
            return !(goods.goodsId && deletedGoodsIds.includes(goods.goodsId))
        })
        const validGoodsList = getValidGoodsList(shop)
        shop.checked = validGoodsList.length > 0 && validGoodsList.every((g: CartGoods) => g.checked === true)
    })
    const remaining = state.invalidShopList.filter(shop => shop.goodsList.length > 0)
    state.invalidShopList.splice(0, state.invalidShopList.length, ...remaining)
}

// 通用的删除函数
const performDelete = async (
    goodsIds: number[],
    confirmMessage: string,
    successMessage: string
) => {
    if (loading.value) return

    if (goodsIds.length === 0) {
        Toast.warning('没有可删除的商品')
        return
    }

    const confirmed = await ConfirmModal.confirm({
        title: '确认删除',
        message: confirmMessage,
        confirmText: '删除',
        cancelText: '取消'
    })

    if (!confirmed) return

    try {
        loading.value = true
        const response = await callDeleteApi(goodsIds)

        if (response.code === 1) {
            updateDataAfterDelete(goodsIds)
            useGoodsStore().fetchCartCount()
            Toast.success(successMessage)
        } else {
            Toast.error(response.message || '删除失败，请稍后重试')
        }
    } catch (error: any) {
        Toast.error(error?.message || '删除失败，请稍后重试')
        console.error('删除商品失败:', error)
    } finally {
        loading.value = false
    }
}

// 购物车数据（正常/下架分离）
const state = reactive<{
    shopList: ShopItem[]
    invalidShopList: ShopItem[]
}>({
    shopList: [],
    invalidShopList: []
})

// 判断商品是否为有效商品：正常 Tab 下不再依赖 commonState，仅排除下架标识
const isValidGoods = (goods: CartGoods): boolean => !goods.invalid

// 获取店铺的有效商品列表
const getValidGoodsList = (shop: ShopItem): CartGoods[] => {
    return shop.goodsList.filter(goods => isValidGoods(goods))
}

// 全选状态（仅只读，由 setAllChecked 统一写）
const allChecked = computed(() => {
    if (state.shopList.length === 0) return false
    return state.shopList.every(shop => {
        const validGoodsList = getValidGoodsList(shop)
        return validGoodsList.length > 0 && validGoodsList.every(goods => goods.checked === true)
    })
})

// 点击「全选」时：仅由此方法更新所有店铺与商品选中状态，避免子组件 emit 误触发 setter 导致全选被清空
const setAllChecked = (value: boolean) => {
    state.shopList.forEach(shop => {
        const validGoodsList = getValidGoodsList(shop)
        if (validGoodsList.length > 0) {
            shop.checked = value
            validGoodsList.forEach(goods => {
                goods.checked = value
            })
        }
    })
}

// 已选商品数量
const selectedCount = computed(() => {
    let count = 0
    state.shopList.forEach(shop => {
        getValidGoodsList(shop).forEach(goods => {
            // 只统计选中的商品
            if (goods.checked === true) {
                count += goods.goodsNum || 0
            }
        })
    })
    return count
})

/**
 * 国补预估（购物车只提示，不占额度）。
 * 选中商品变化时重新试算；未命中/插件未装则不展示。
 */
const subsidyQuote = ref<SubsidyQuote | null>(null)
const subsidyDiscountAmount = computed(() => {
    if (!shouldShowSubsidy(subsidyQuote.value)) return 0
    return round2(toAmount(subsidyQuote.value?.subsidyAmount))
})

const refreshSubsidy = async () => {
    try {
        const items: any[] = []
        state.shopList.forEach((shop: any) => {
            getValidGoodsList(shop).forEach((goods: any) => {
                if (goods.checked !== true) return
                items.push({
                    goodsId: Number(goods.goodsId),
                    categoryId: String(goods.categoryId ?? ''),
                    quantity: Number(goods.goodsNum ?? 1) || 1,
                    unitPrice: Number(goods.goodsPrice ?? 0)
                })
            })
        })
        if (items.length === 0) {
            subsidyQuote.value = null
            return
        }
        subsidyQuote.value = await fetchSubsidyQuote({ items, hasCoupon: false, hasPromotion: false, hasPoint: false })
    } catch (error) {
        console.warn('[national-subsidy] 购物车试算失败，按无国补处理', error)
        subsidyQuote.value = null
    }
}

// 总价（使用 getGoodsSubtotal 函数统一计算）
const totalPrice = computed(() => {    let total = 0
    state.shopList.forEach(shop => {
        getValidGoodsList(shop).forEach(goods => {
            // 只统计选中的商品
            if (goods.checked === true) {
                total += getGoodsSubtotal(goods)
            }
        })
    })
    return total
})

// 计算商品单价（购物车前端计算：goods.goodsPrice）
const getGoodsPrice = (goods: CartGoods): number => {
    return goods.goodsPrice || 0
}

// 计算商品小计：数量 * 单价
const getGoodsSubtotal = (goods: CartGoods): number => {
    const price = goods.goodsPrice || 0
    const quantity = goods.goodsNum || 0
    return price * quantity
}

// 数量变化处理（添加防抖处理）
let quantityChangeTimer: ReturnType<typeof setTimeout> | null = null

const handleQuantityChange = async (shop: ShopItem, goods: CartGoods) => {
    // 如果商品无效，不允许修改数量
    if (!isValidGoods(goods)) {
        return
    }

    // 清除之前的定时器
    if (quantityChangeTimer) {
        clearTimeout(quantityChangeTimer)
    }

    // 延迟执行，避免频繁调用接口（防抖 300ms）
    quantityChangeTimer = setTimeout(async () => {
        const newQuantity = goods.goodsNum || 1
        // 如果没有保存旧值，使用当前值作为旧值（第一次修改时）
        if (goods.oldGoodsNum === undefined) {
            goods.oldGoodsNum = newQuantity
            return // 第一次修改，只保存旧值，不调用接口
        }
        const oldQuantity = goods.oldGoodsNum

        // 验证数量范围
        const minQuantity = goods.moq || 1
        const maxQuantity = goods.goodsStock || goods.commonStock || Infinity
        const maxSale = goods.goodsMaxSale || Infinity
        const maxAllowed = Math.min(maxQuantity, maxSale)

        if (newQuantity < minQuantity) {
            Toast.warning(`数量不能少于 ${minQuantity}`)
            // 恢复原值
            goods.goodsNum = minQuantity
            return
        }

        if (newQuantity > maxAllowed) {
            Toast.warning(`数量不能超过 ${maxAllowed}`)
            // 恢复原值
            goods.goodsNum = maxAllowed
            return
        }

        // 如果数量没有变化，不需要调用接口
        if (newQuantity === oldQuantity) {
            return
        }

        // 保存旧值，用于失败时恢复
        goods.oldGoodsNum = oldQuantity

        // 购物车调用修改数量接口
        if (!goods.goodsId) {
            Toast.error('商品ID不存在')
            return
        }

        try {
            const response = await changeGoodsNum({
                goodsId: goods.goodsId,
                goodsNum: newQuantity
            })

            if (response.code === 1) {
                // 更新商品小计（优先使用价格 hyPrice）
                const price = goods.hyPrice !== undefined && goods.hyPrice !== null
                    ? goods.hyPrice
                    : (goods.goodsPrice || goods.commonPrice || 0)
                goods.goodsTotalPrice = price * newQuantity
                // 更新旧值为新值
                goods.oldGoodsNum = newQuantity
                useGoodsStore().fetchCartCount()
                // 数量修改成功后，重新计算本地价格
                recalcCartTotals()
            } else {
                // 接口失败，恢复原值
                goods.goodsNum = oldQuantity
                Toast.error(response.message || '修改数量失败，请稍后重试')
            }
        } catch (error: any) {
            // 发生错误，恢复原值
            goods.goodsNum = oldQuantity
            Toast.error(error?.message || '修改数量失败，请稍后重试')
            console.error('修改商品数量失败:', error)
        }
    }, 300)
}

// 店铺复选框变化：在父组件中同步该店铺及下属有效商品的选中状态，保证响应式更新
const handleShopCheck = (shop: ShopItem, checked: boolean) => {
    const targetShop = state.shopList.find(s => s.shopId === shop.shopId)
    if (!targetShop) return
    targetShop.checked = checked
    getValidGoodsList(targetShop).forEach((goods: CartGoods) => {
        goods.checked = checked
    })
}

// 删除单个商品（含已下架，与 GoodsList 删除按钮行为一致）
const deleteGoods = async (shop: ShopItem, goods: CartGoods) => {
    if (!goods.goodsId) {
        return
    }
    await performDelete(
        [goods.goodsId],
        `确定要删除商品"${goods.goodsName || '该商品'}"吗？`,
        '商品已删除'
    )
}

// 删除选中的商品
const deleteSelected = async () => {
    const selectedGoodsIds = collectSelectedGoodsIds()
    if (selectedGoodsIds.length === 0) {
        Toast.warning('请先选择要删除的商品')
        return
    }
    await performDelete(
        selectedGoodsIds,
        `确定要删除选中的 ${selectedGoodsIds.length} 件商品吗？`,
        `成功删除 ${selectedGoodsIds.length} 件商品`
    )
}

// 删除所有下架的商品
const deleteUnavailable = async () => {
    const unavailableGoodsIds = collectSelectedGoodsIds((goods: CartGoods) =>
        !!(goods.commonState !== 1 || goods.invalid) && !!goods.goodsId
    )
    if (unavailableGoodsIds.length === 0) {
        Toast.info('当前没有下架商品')
        return
    }
    await performDelete(
        unavailableGoodsIds,
        `确定要删除所有 ${unavailableGoodsIds.length} 件下架商品吗？`,
        `成功删除 ${unavailableGoodsIds.length} 件下架商品`
    )
}

const submitOrder = async () => {
    await handleSubmitOrder()
}

// 收集选中的有效商品（通用函数）
const collectSelectedGoods = (): Array<{ shop: ShopItem; goods: CartGoods }> => {
    const selectedGoods: Array<{ shop: ShopItem; goods: CartGoods }> = []
    state.shopList.forEach(shop => {
        getValidGoodsList(shop).forEach((goods: CartGoods) => {
            if (goods.checked === true) {
                selectedGoods.push({ shop, goods })
            }
        })
    })
    return selectedGoods
}

/** 购物车行活动状态：优先 activityGoodsDetail.activityStatus，否则 commonIsXian（与 GoodsList 一致） */
const getCartGoodsActivityStatus = (goods: CartGoods): number => {
    const fromDetail = Number((goods as { activityGoodsDetail?: { activityStatus?: number | string | null } })
        .activityGoodsDetail?.activityStatus)
    if (Number.isFinite(fromDetail) && fromDetail > 0) return fromDetail
    return Number(goods.commonIsXian || 0)
}

/** 是否限时秒杀(2)或限时折扣(3)，下单前需走活动限购校验 */
const isSeckillOrDiscountCartGoods = (goods: CartGoods): boolean => {
    const s = getCartGoodsActivityStatus(goods)
    return s === 2 || s === 3
}

// 处理下单逻辑
const handleSubmitOrder = async () => {
    if (selectedCount.value === 0) {
        Toast.warning('请先选择要下单的商品')
        return
    }

    const selectedGoods = collectSelectedGoods()

    const activityLimitList = selectedGoods
        .filter(({ goods }) => goods.goodsId != null && goods.commonId != null && isSeckillOrDiscountCartGoods(goods))
        .map(({ goods }) => ({
            commonId: Number(goods.commonId),
            goodsId: Number(goods.goodsId),
            goodsNum: Number(goods.goodsNum ?? 1)
        }))
        .filter(
            it =>
                Number.isFinite(it.commonId) &&
                Number.isFinite(it.goodsId) &&
                Number.isFinite(it.goodsNum) &&
                it.commonId > 0 &&
                it.goodsId > 0
        )

    if (activityLimitList.length > 0) {
        try {
            const limitRes = await checkActivityGoodsLimit({ list: activityLimitList })
            if (limitRes.code !== 1) {
                Toast.warning(String(limitRes.message || '活动商品校验未通过'))
                return
            }
        } catch (err) {
            console.error('活动商品限购校验失败:', err)
            Toast.warning('活动商品校验失败，请稍后重试')
            return
        }
    }

    const selectedShopList: ShopList[] = []
    const shopMap = new Map<number, ShopList>()

    selectedGoods.forEach(({ shop, goods }) => {
        if (!goods.goodsId) return

        if (!shopMap.has(shop.shopId)) {
            shopMap.set(shop.shopId, {
                shopId: shop.shopId,
                shopName: shop.shopName,
                shopInvoices: (shop as any).shopInvoices ?? 0,
                invoiceActive: (shop as any).invoiceActive ?? 0,
                goodsList: [],
                shopTotalPrice: 0,
                virtualGoods: (goods as any).virtualGoods
            })
        }

        const shopList = shopMap.get(shop.shopId)!
        shopList.goodsList.push({
            goodsId: String(goods.goodsId),
            commonId: String(goods.commonId || ''),
            goodsImage: goods.goodsImage || '',
            goodsName: goods.goodsName || '',
            dealerGoodsFlag: goods.dealerGoodsFlag || 0,
            goodsNumber: goods.goodsNum || 1,
            goodsSpecifications: goods.goodsSpecifications || goods.goodsSpecChineseValue || '',
            goodsCode: goods.goodsCode || '',
            goodsStock: goods.goodsStock || goods.commonStock || 0,
            singlePrice: getGoodsPrice(goods),
            goodsTotal: getGoodsSubtotal(goods),
            activityType: goods.activityType || '',
            commonIsXian: goods.commonIsXian || 0,
            activityGoodsDetail: goods.activityGoodsDetail || null
        })
    })

    // 计算每个店铺的总价
    shopMap.forEach(shop => {
        shop.shopTotalPrice = shop.goodsList.reduce((sum, goods) => sum + goods.goodsTotal, 0)
        selectedShopList.push(shop)
    })

    if (selectedShopList.length === 0) {
        Toast.warning('请先选择要下单的商品')
        return
    }

    goodsStore.setShopList(selectedShopList)
    navigateTo({ path: '/confirmOrder', query: { goodsCat: '1' } })
}


// 通用的商品列表处理函数（invoiceActive、shopInvoices 从接口数据传入）
const processGoodsList = (
    goodsList: Array<UserShoppingCartDTO | Record<string, any>>,
    shopMap: Map<number, ShopItem>,
    defaultShopId: number,
    defaultShopName: string,
    isInvalid: boolean = false,
    invoiceActive?: number,
    shopInvoices?: number | null
) => {
    if (!Array.isArray(goodsList)) return

    goodsList.forEach((goods: any) => {
        const shopId = goods.shopId || defaultShopId || 0
        const shopName = goods.shopName || defaultShopName || '未知店铺'

        if (!shopMap.has(shopId)) {
            shopMap.set(shopId, {
                shopId,
                shopName,
                checked: false,
                goodsList: [],
                ...(invoiceActive !== undefined && { invoiceActive }),
                ...(shopInvoices !== undefined && shopInvoices !== null && { shopInvoices })
            } as ShopItem)
        }

        const shop = shopMap.get(shopId)!
        const goodsItem = {
            ...goods,
            checked: false,
            ...(isInvalid && { invalid: true })
        }
        shop.goodsList.push(goodsItem)
    })
}

// 通用的数据转换函数（店铺维度数组）
const transformDataToShopList = (data: QueryShoppingCartShopItem | QueryShoppingCartShopItem[]): ShopItem[] => {
    const shopMap = new Map<number, ShopItem>()

    const dataArray = Array.isArray(data) ? data : [data]

    dataArray.forEach((item: QueryShoppingCartShopItem) => {
        const defaultShopId = item.shopId || 0
        const defaultShopName = item.shopName || '未知店铺'

        if (item.goodsDetailsList) {
            processGoodsList(item.goodsDetailsList, shopMap, defaultShopId, defaultShopName, false, item.invoiceActive, item.shopInvoices)
        }

        if (item.invalidGoodsDetailsList) {
            processGoodsList(item.invalidGoodsDetailsList, shopMap, defaultShopId, defaultShopName, true, item.invoiceActive, item.shopInvoices)
        }
    })

    return Array.from(shopMap.values()).map(shop => ({
        ...shop,
        goodsList: shop.goodsList.map(goods => ({ ...goods }))
    }))
}

// 将购物车数据按店铺分组（兼容新接口 data.list）
const transformCartData = (data: QueryShoppingCartData): ShopItem[] => {
    if (!data?.list) return []
    return transformDataToShopList(data.list as any)
}

// 将下架商品按店铺分组（新接口 data.invalidProductList）
const transformInvalidCartData = (data: QueryShoppingCartData): ShopItem[] => {
    const invalidList = data?.invalidProductList || []
    if (!Array.isArray(invalidList) || invalidList.length === 0) return []

    const shopMap = new Map<number, ShopItem>()
    invalidList.forEach((goods: any) => {
        const shopId = goods.shopId || 0
        const shopName = goods.shopName || '未知店铺'
        if (!shopMap.has(shopId)) {
            shopMap.set(shopId, {
                shopId,
                shopName,
                checked: false,
                goodsList: []
            } as ShopItem)
        }
        const shop = shopMap.get(shopId)!
        shop.goodsList.push({
            ...goods,
            checked: false,
            invalid: true
        })
    })
    return Array.from(shopMap.values())
}


// 通用的数据加载后处理函数
const processLoadedData = (transformedData: ShopItem[]) => {
    state.shopList.length = 0
    transformedData.forEach(shop => {
        const reactiveShop = reactive<ShopItem>({
            ...shop,
            goodsList: shop.goodsList.map((goods: UserShoppingCartDTO): CartGoods => reactive<CartGoods>({
                ...goods,
                checked: false,
                oldGoodsNum: goods.goodsNum // 保存原始数量
            }))
        })
        state.shopList.push(reactiveShop)
    })
}

const processInvalidLoadedData = (transformedData: ShopItem[]) => {
    state.invalidShopList.length = 0
    transformedData.forEach(shop => {
        const reactiveShop = reactive<ShopItem>({
            ...shop,
            goodsList: shop.goodsList.map((goods: any): CartGoods => reactive<CartGoods>({
                ...goods,
                checked: false,
                oldGoodsNum: (goods as any).goodsNum
            }))
        })
        state.invalidShopList.push(reactiveShop)
    })
}

/** 计算并写回每个商品小计、店铺总计（前端计算，不依赖算价接口） */
const recalcCartTotals = () => {
    state.shopList.forEach((shop: ShopItem) => {
        shop.goodsList.forEach((goods: CartGoods) => {
            const price = goods.goodsPrice || 0
            const qty = goods.goodsNum || 0
            goods.goodsTotalPrice = price * qty
        })
            ; (shop as any).shopTotalPrice = shop.goodsList.reduce((sum, g) => sum + (g.goodsTotalPrice || 0), 0)
    })
}

// 已移除默认地址获取逻辑：购物车算价不依赖地址

// 查询购物车数据
const fetchShoppingCart = async () => {
    try {
        loading.value = true
        const response = await queryShoppingCart()

        if (response.code === 1 && response.data) {
            const transformedData = transformCartData(response.data)
            processLoadedData(transformedData)
            const transformedInvalid = transformInvalidCartData(response.data)
            processInvalidLoadedData(transformedInvalid)
            // 数据加载完成后，前端计算价格
            recalcCartTotals()
        } else {
            state.shopList = []
            state.invalidShopList.length = 0
            console.warn('查询购物车失败:', response)
        }
    } catch (error) {
        console.error('查询购物车失败:', error)
        state.shopList = []
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    // 仅保留购物车
    if (cartType.value !== 'shopping') {
        navigateTo('/cart/shopping', { replace: true })
        return
    }
    fetchShoppingCart()
})

// 监听路由变化，切换Tab时重新加载数据
watch(() => cartType.value, (newType) => {
    if (newType !== 'shopping') {
        navigateTo('/cart/shopping', { replace: true })
        return
    }
    fetchShoppingCart()
})

// 选中商品变化时重算国补预估（勾选/取消勾选、删商品后金额都会变）
watch(() => state.shopList, () => {
    refreshSubsidy()
}, { deep: true })

defineExpose({ state })

</script>

<style lang="scss" scoped>
.cart-container {
    min-height: 100vh;
    background-color: #fff;
}

.cart-header {
    background-color: #fff;
    padding: 20px 20px 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    .cart-tabs {
        display: flex;
        gap: 8px;
        border-bottom: 1px solid #e0e0e0;
        position: relative;

        .tab-item {
            position: relative;
            padding: 14px 28px;
            font-size: 15px;
            color: #666;
            cursor: pointer;
            border-bottom: 2px solid transparent;
            margin-bottom: -1px;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            background: transparent;
            border-radius: 4px 4px 0 0;
            font-weight: 400;
            user-select: none;

            &::before {
                content: '';
                position: absolute;
                bottom: -1px;
                left: 50%;
                transform: translateX(-50%) scaleX(0);
                width: 60%;
                height: 2px;
                background: var(--color);
                transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            }

            &:hover {
                color: var(--color);
                background-color: rgba(0, 0, 0, 0.02);
            }

            &.active {
                color: var(--color);
                font-weight: 600;
                background-color: rgba(0, 0, 0, 0.01);

                &::before {
                    transform: translateX(-50%) scaleX(1);
                }

                border-bottom-color: var(--color);
            }

            .tab-count {
                margin-left: 6px;
                font-size: 13px;
                color: #999;
                font-weight: 500;
            }
        }
    }
}

.cart-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    position: relative;
}

// Tab切换过渡效果
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
    opacity: 0;
    transform: translateY(10px);
}

.fade-leave-to {
    opacity: 0;
    transform: translateY(-10px);
}

.cart-table-header {
    display: flex;
    background-color: #f8f9fa;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    padding: 12px 20px;
    margin-bottom: 20px;

    .table-col {
        display: flex;
        align-items: center;

        &.col-check {
            width: 60px;
            justify-content: center;
        }

        &.col-info {
            flex: 1;
            font-size: 14px;
            color: #333;
            font-weight: 500;
        }

        &.col-price {
            width: 120px;
            font-size: 14px;
            color: #333;
            font-weight: 500;
            justify-content: center;
        }

        &.col-quantity {
            width: 150px;
            font-size: 14px;
            color: #333;
            font-weight: 500;
            justify-content: center;
        }

        &.col-subtotal {
            width: 150px;
            font-size: 14px;
            color: #333;
            font-weight: 500;
            justify-content: center;
        }

        &.col-action {
            width: 100px;
            font-size: 14px;
            color: #333;
            font-weight: 500;
            justify-content: center;
        }
    }
}

.cart-goods-list {
    .shop-group {
        background-color: #fff;
        border-radius: 4px;
        margin-bottom: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #e0e0e0;

        .shop-header {
            display: flex;
            align-items: center;
            padding: 12px 20px;
            background-color: #f8f9fa;
            border-bottom: 1px solid #e0e0e0;

            .check-box {
                margin-right: 12px;
            }

            .shop-name {
                font-size: 14px;
                color: #333;
                font-weight: 500;
            }
        }

        .goods-list {
            .goods-item {
                display: flex;
                align-items: center;
                padding: 20px;
                border-bottom: 1px solid #e0e0e0;

                &:last-child {
                    border-bottom: none;
                }

                .goods-check {
                    width: 60px;
                    display: flex;
                    justify-content: center;
                }

                .goods-info {
                    flex: 1;
                    display: flex;
                    align-items: center;

                    .goods-image {
                        width: 80px;
                        height: 80px;
                        object-fit: cover;
                        margin-right: 16px;
                        border: 1px solid #e0e0e0;
                        border-radius: 4px;
                    }

                    .goods-details {
                        flex: 1;

                        .goods-name {
                            font-size: 14px;
                            color: #333;
                            line-height: 1.5;
                            margin-bottom: 8px;
                        }

                        .goods-sku,
                        .goods-spec {
                            font-size: 12px;
                            color: #999;
                            margin-bottom: 4px;
                        }
                    }
                }

                .goods-price {
                    width: 120px;
                    display: flex;
                    justify-content: center;

                    .price-label {
                        font-size: 12px;
                        color: #999;
                        margin-right: 4px;
                    }

                    .price-value {
                        font-size: 14px;
                        color: #333;
                    }
                }

                .goods-quantity {
                    width: 150px;
                    display: flex;
                    justify-content: center;

                    .quantity-control {
                        display: inline-flex;
                        align-items: center;

                        .quantity-btn {
                            width: 28px;
                            height: 28px;
                            border: 1px solid #e0e0e0;
                            background-color: #fff;
                            font-size: 14px;
                            cursor: pointer;
                            border-radius: 4px;

                            &:disabled {
                                opacity: 0.5;
                                cursor: not-allowed;
                            }
                        }

                        .quantity-input {
                            width: 50px;
                            height: 28px;
                            border: 1px solid #e0e0e0;
                            border-left: none;
                            border-right: none;
                            text-align: center;
                            font-size: 14px;

                            &:focus {
                                outline: none;
                                border-color: var(--color);
                            }
                        }
                    }
                }

                .goods-subtotal {
                    width: 150px;
                    display: flex;
                    justify-content: center;

                    .subtotal-value {
                        font-size: 14px;
                        color: var(--color);
                        font-weight: 500;
                    }
                }

                .goods-action {
                    width: 100px;
                    display: flex;
                    justify-content: center;

                    .delete-btn {
                        font-size: 12px;
                        color: #1890ff;
                        cursor: pointer;

                        &:hover {
                            color: #40a9ff;
                        }
                    }
                }
            }
        }
    }
}

.cart-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #fff;
    border-radius: 4px;
    padding: 16px 20px;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
    border: 1px solid #e0e0e0;

    .footer-left {
        display: flex;
        align-items: center;

        .check-box {
            margin-right: 12px;
        }

        .check-text {
            font-size: 14px;
            color: #333;
            margin-right: 24px;
        }

        .footer-link {
            font-size: 14px;
            color: var(--color);
            margin-right: 24px;
            cursor: pointer;
            transition: all 0.2s;

            &:hover:not(.disabled) {
                text-decoration: underline;
            }

            &.disabled {
                color: #ccc;
                cursor: not-allowed;
                opacity: 0.6;
                pointer-events: none;
            }
        }
    }

    .footer-right {
        display: flex;
        align-items: center;

        .total-info {
            margin-right: 32px;

            .total-text {
                display: block;
                font-size: 14px;
                color: #666;
                margin-bottom: 8px;
            }

            .total-price {
                font-size: 16px;
                color: #333;

                .price {
                    color: var(--color);
                    font-weight: 600;
                    font-size: 20px;
                }
            }

            /* 国补预估：政策类优惠单独标色，与普通优惠区分 */
            .subsidy-hint {
                font-size: 14px;
                color: #b91c1c;

                .subsidy-amount {
                    font-weight: 600;
                }
            }
        }

        .submit-btn {
            width: 120px;
            height: 40px;
            background-color: var(--color);
            color: #fff;
            border: none;
            border-radius: 4px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;

            &:disabled {
                background-color: #e0e0e0;
                cursor: not-allowed;
            }

            &:hover:not(:disabled) {
                background-color: #ff7875;
            }

        }
    }
}

.cart-footer--invalid {
    position: sticky;
    bottom: 0;
    background-color: #fff;
    border-top: 1px solid #e0e0e0;
    padding: 14px 20px;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}

// 复选框样式
.check-box {
    position: relative;
    display: inline-block;
    cursor: pointer;

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
}

// 响应式设计
@media (max-width: 768px) {
    .cart-container {
        padding: 0;
    }

    .cart-content {
        padding: 10px;
    }

    .cart-table-header {
        padding: 10px;

        .table-col {
            &.col-info {
                font-size: 12px;
            }

            &.col-price {
                width: 70px;
                font-size: 12px;
            }

            &.col-quantity {
                width: 100px;
                font-size: 12px;
            }

            &.col-subtotal {
                width: 80px;
                font-size: 12px;
            }

            &.col-action {
                width: 60px;
                font-size: 12px;
            }
        }
    }

    .cart-goods-list {
        .shop-group {
            .goods-list {
                .goods-item {
                    padding: 10px;

                    .goods-info {
                        .goods-image {
                            width: 60px;
                            height: 60px;
                        }

                        .goods-details {
                            .goods-name {
                                font-size: 12px;
                            }

                            .goods-sku,
                            .goods-spec {
                                font-size: 10px;
                            }
                        }
                    }

                    .goods-price {
                        width: 70px;
                        font-size: 12px;
                    }

                    .goods-quantity {
                        width: 100px;
                        font-size: 12px;
                    }

                    .goods-subtotal {
                        width: 80px;
                        font-size: 12px;
                    }

                    .goods-action {
                        width: 60px;
                        font-size: 12px;
                    }
                }
            }
        }
    }

    .cart-footer {
        flex-direction: column;
        align-items: flex-start;

        .footer-left {
            margin-bottom: 16px;
        }

        .footer-right {
            width: 100%;
            justify-content: space-between;

            .total-info {
                margin-right: 0;
            }

            .submit-btn {
                width: 100px;
                height: 36px;
                font-size: 13px;
            }
        }
    }
}
</style>