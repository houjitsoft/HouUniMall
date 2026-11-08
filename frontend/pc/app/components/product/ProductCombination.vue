<template>
    <div v-if="combinationList.length > 0" class="product-combination">
        <h3 class="section-title">{{ $t('组合商品') }}</h3>

        <div v-for="item in combinationList" :key="item.combinationId" class="combination-block">
            <!-- 左侧：类型标签 + 商品轮播 -->
            <div class="combination-left">
                <span class="type-badge" :class="item.combinationType === 1 ? 'type-fixed' : 'type-matching'">
                    {{ item.combinationType === 1 ? $t('固定') : $t('搭配') }}
                </span>
                <div class="goods-carousel">
                    <div class="carousel-inner">
                        <template v-for="(goods, idx) in item.goodsList" :key="goods.goodsId || idx">
                            <div class="goods-card"
                                :class="{ 'card-selected': item.combinationType === 2 && isMatchingSelected(item, idx) }">
                                <div class="goods-image-wrap"
                                    @click="item.combinationType === 2 ? toggleMatchingSelected(item, idx) : goGoodsDetail(goods.goodsId)">
                                    <div class="goods-image">
                                        <img :src="goods.goodsImage" :alt="goods.goodsName" @error="handleImgError" />
                                    </div>
                                    <!-- 搭配组合：勾选角标 -->
                                    <div v-if="item.combinationType === 2" class="goods-check-badge"
                                        :class="{ selected: isMatchingSelected(item, idx) }">
                                        <span v-if="isMatchingSelected(item, idx)" class="check-icon">✓</span>
                                    </div>
                                </div>
                                <div class="goods-name">{{ $t(goods.goodsName || '') }}</div>
                                <div class="goods-spec">{{ $t('规格') }}: {{ goods.goodsSpec || '-' }}</div>
                                <!-- 固定组合：显示数量 -->
                                <div v-if="item.combinationType === 1" class="goods-qty">{{ $t('数量') }}: {{
                                    goods.goodsNum || 1 }}
                                </div>
                                <!-- 搭配组合：数量选择器（勾选后才能选数量，最大 goodsStock） -->
                                <div v-else class="goods-qty-control">
                                    <button type="button" class="qty-btn"
                                        :disabled="!isMatchingSelected(item, idx) || getMatchingQty(item, idx) <= 0"
                                        @click="decreaseMatchingQty(item, idx)">-</button>
                                    <input type="number" class="qty-input" :value="getMatchingQty(item, idx)" :min="0"
                                        :max="getGoodsStock(item, idx)" :disabled="!isMatchingSelected(item, idx)"
                                        @input="onMatchingQtyInput(item, idx, $event)" />
                                    <button type="button" class="qty-btn"
                                        :disabled="!isMatchingSelected(item, idx) || getMatchingQty(item, idx) >= getGoodsStock(item, idx)"
                                        @click="increaseMatchingQty(item, idx)">+</button>
                                </div>
                            </div>
                            <span v-if="idx < (item.goodsList?.length || 0) - 1" class="plus-symbol">+</span>
                        </template>
                    </div>
                    <button v-if="canScrollLeft(item)" type="button" class="carousel-arrow arrow-left"
                        @click="scrollCarousel(item, -1)">‹</button>
                    <button v-if="canScrollRight(item)" type="button" class="carousel-arrow arrow-right"
                        @click="scrollCarousel(item, 1)">›</button>
                </div>
            </div>

            <!-- 右侧：价格与下单 -->
            <div class="combination-right">
                <div class="price-row price-row--combo">
                    {{ $t('组合价格') }}: {{ formatPriceWithSymbol(getComboPrice(item)) }}
                </div>
                <div v-if="getOriginalPrice(item) > 0" class="price-row price-row--original">
                    {{ $t('原价') }}: {{ formatPriceWithSymbol(getOriginalPrice(item)) }}
                </div>
                <!-- 固定组合：数量选择，最大为商品中最小库存量 -->
                <div v-if="item.combinationType === 1" class="qty-row">
                    <button type="button" class="qty-btn" :disabled="getFixedQty(item) <= 1"
                        @click="decreaseFixedQty(item)">-</button>
                    <input type="number" class="qty-input" :value="getFixedQty(item)" :min="1"
                        :max="getFixedMaxQty(item)" @input="onFixedQtyInput(item, $event)" />
                    <button type="button" class="qty-btn" :disabled="getFixedQty(item) >= getFixedMaxQty(item)"
                        @click="increaseFixedQty(item)">+</button>
                </div>
                <button type="button" class="btn-order" @click="handleOneClickOrder(item)">{{ $t('一键下单') }}</button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { queryCombinationBaseByPcDetails } from '~/services/activity.service'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import { useGoodsStore } from '~/stores/modules/goods'
import { useAuth } from '~/composables/useAuth'
import { Toast } from '~/composables/useToast'
import type { CombinationBaseVO, CombinationGoodsDTO } from '~/types/activity'
import type { GoodsList, ShopList } from '~/types/goods'

const props = defineProps<{
    goodsId: number
}>()

const route = useRoute()
const goodsStore = useGoodsStore()
const { isLogin } = useAuth()

const combinationList = ref<CombinationBaseVO[]>([])
const loading = ref(false)

// 固定组合数量：combinationId -> quantity
const fixedQuantities = ref<Record<number, number>>({})
// 搭配组合勾选：combinationId -> goodsIndex -> selected
const matchingSelected = ref<Record<number, Record<number, boolean>>>({})
// 搭配组合数量：combinationId -> goodsIndex -> quantity
const matchingQuantities = ref<Record<number, Record<number, number>>>({})

const checkLogin = (): boolean => isLogin(route.fullPath)

const fetchCombinations = async () => {
    if (!props.goodsId) return
    loading.value = true
    try {
        const res = await queryCombinationBaseByPcDetails<CombinationBaseVO[]>({ goodsId: props.goodsId })
        const list = Array.isArray(res?.data) ? res.data : [] as CombinationBaseVO[]
        if (res?.code === 1 && list.length > 0) {
            combinationList.value = list.filter((item: CombinationBaseVO) => item.combinationStatus === 1)
            initQuantities()
        } else {
            combinationList.value = []
        }
    } catch (e) {
        console.error('获取组合商品失败', e)
        combinationList.value = []
    } finally {
        loading.value = false
    }
}

const initQuantities = () => {
    const fixed: Record<number, number> = {}
    const matching: Record<number, Record<number, number>> = {}
    const selected: Record<number, Record<number, boolean>> = {}
    combinationList.value.forEach(item => {
        if (item.combinationType === 1) {
            fixed[item.combinationId!] = 1
        } else if (item.combinationType === 2 && item.goodsList?.length) {
            const selMap: Record<number, boolean> = {}
            const qtyMap: Record<number, number> = {}
            item.goodsList.forEach((g, i) => {
                selMap[i] = i === 0
                qtyMap[i] = i === 0 ? Math.min(g.goodsNum || 1, g.goodsStock ?? 999) : 0
            })
            selected[item.combinationId!] = selMap
            matching[item.combinationId!] = qtyMap
        }
    })
    fixedQuantities.value = fixed
    matchingSelected.value = selected
    matchingQuantities.value = matching
}

watch(() => props.goodsId, (id) => {
    if (id) fetchCombinations()
}, { immediate: true })

const getFixedQty = (item: CombinationBaseVO) =>
    fixedQuantities.value[item.combinationId!] ?? 1

// 固定组合最大数量：取商品中最小库存量
const getFixedMaxQty = (item: CombinationBaseVO): number => {
    const list = item.goodsList
    if (!list?.length) return 999
    const minStock = Math.min(...list.map(g => g.goodsStock ?? 999))
    return Math.max(1, minStock)
}

const getMatchingQty = (item: CombinationBaseVO, idx: number) =>
    matchingQuantities.value[item.combinationId!]?.[idx] ?? 0

const isMatchingSelected = (item: CombinationBaseVO, idx: number) =>
    matchingSelected.value[item.combinationId!]?.[idx] ?? false

const getGoodsStock = (item: CombinationBaseVO, idx: number) =>
    item.goodsList?.[idx]?.goodsStock ?? 999

const toggleMatchingSelected = (item: CombinationBaseVO, idx: number) => {
    const map = matchingSelected.value[item.combinationId!] ?? {}
    const next = !(map[idx] ?? false)
    matchingSelected.value[item.combinationId!] = { ...map, [idx]: next }
    const qtyMap = matchingQuantities.value[item.combinationId!] ?? {}
    const stock = getGoodsStock(item, idx)
    matchingQuantities.value[item.combinationId!] = { ...qtyMap, [idx]: next ? Math.min(1, stock) : 0 }
}

const decreaseFixedQty = (item: CombinationBaseVO) => {
    const q = getFixedQty(item)
    if (q > 1) fixedQuantities.value[item.combinationId!] = q - 1
}

const increaseFixedQty = (item: CombinationBaseVO) => {
    const max = getFixedMaxQty(item)
    const q = Math.min(max, (getFixedQty(item) || 1) + 1)
    fixedQuantities.value[item.combinationId!] = q
}

const onFixedQtyInput = (item: CombinationBaseVO, e: Event) => {
    const v = parseInt((e.target as HTMLInputElement).value, 10)
    if (!isNaN(v) && v >= 1) {
        const max = getFixedMaxQty(item)
        fixedQuantities.value[item.combinationId!] = Math.min(max, v)
    }
}

const decreaseMatchingQty = (item: CombinationBaseVO, idx: number) => {
    if (!isMatchingSelected(item, idx)) return
    const map = matchingQuantities.value[item.combinationId!] ?? {}
    const v = Math.max(0, (map[idx] ?? 0) - 1)
    matchingQuantities.value[item.combinationId!] = { ...map, [idx]: v }
}

const increaseMatchingQty = (item: CombinationBaseVO, idx: number) => {
    if (!isMatchingSelected(item, idx)) return
    const map = matchingQuantities.value[item.combinationId!] ?? {}
    const max = getGoodsStock(item, idx)
    const v = Math.min(max, (map[idx] ?? 0) + 1)
    matchingQuantities.value[item.combinationId!] = { ...map, [idx]: v }
}

const onMatchingQtyInput = (item: CombinationBaseVO, idx: number, e: Event) => {
    if (!isMatchingSelected(item, idx)) return
    const v = parseInt((e.target as HTMLInputElement).value, 10)
    if (!isNaN(v) && v >= 0) {
        const max = getGoodsStock(item, idx)
        const map = matchingQuantities.value[item.combinationId!] ?? {}
        matchingQuantities.value[item.combinationId!] = { ...map, [idx]: Math.min(max, v) }
    }
}

const getComboPrice = (item: CombinationBaseVO): number => {
    if (item.combinationType === 1) {
        return (item.combinationPrice ?? 0) * getFixedQty(item)
    }
    let total = 0
    item.goodsList?.forEach((g, i) => {
        const qty = getMatchingQty(item, i)
        total += (g.matchingPrices ?? g.goodsPrice ?? 0) * qty
    })
    return total
}

const getOriginalPrice = (item: CombinationBaseVO): number => {
    if (item.combinationType === 1) {
        let single = 0
        item.goodsList?.forEach(g => {
            single += (g.goodsPrice ?? 0) * (g.goodsNum || 1)
        })
        return single * getFixedQty(item)
    }
    let total = 0
    item.goodsList?.forEach((g, i) => {
        const qty = getMatchingQty(item, i)
        total += (g.goodsPrice ?? 0) * qty
    })
    return total
}

const handleOneClickOrder = (item: CombinationBaseVO) => {
    if (!checkLogin()) return

    if (item.combinationType === 1) {
        if (!item.goodsList?.length) {
            Toast.error('商品信息不完整')
            return
        }
        const qty = getFixedQty(item)
        const goodsList: GoodsList[] = item.goodsList.map((g: CombinationGoodsDTO) => ({
            goodsId: String(g.goodsId || ''),
            commonId: String(g.commonId || ''),
            goodsImage: g.goodsImage || '',
            goodsName: g.goodsName || '',
            dealerGoodsFlag: 0,
            goodsNumber: (g.goodsNum || 1) * qty,
            goodsSpecifications: g.goodsSpec || '',
            goodsCode: g.goodsCode || '',
            goodsStock: g.goodsStock || 0,
            singlePrice: g.goodsPrice ?? 0,
            goodsTotal: (g.goodsPrice ?? 0) * (g.goodsNum || 1) * qty,
            activityType: '',
            combinationType: Number(item.combinationType ?? 0),
            combinationNum: qty,
            combinationId: Number(item.combinationId ?? 0),
            combinationGoodsNum: qty
        }))
        const shopId = item.goodsList?.[0]?.shopId ?? 0
        const shopName = '' // API 可能无 shopName，确认订单页可再补
        goodsStore.clearShopList()
        goodsStore.setShopList([{
            shopId,
            shopName,
            shopInvoices: 0,
            invoiceActive: 0,
            goodsList,
            shopTotalPrice: getComboPrice(item)
        }])
        navigateTo('/confirmOrder')
    } else {
        const hasAny = item.goodsList?.some((_, i) => getMatchingQty(item, i) > 0)
        if (!hasAny) {
            Toast.warning('请至少选择一个商品并设置数量')
            return
        }
        const goodsList: GoodsList[] = []
        item.goodsList?.forEach((g, i) => {
            const qty = getMatchingQty(item, i)
            if (qty > 0) {
                const price = g.matchingPrices ?? g.goodsPrice ?? 0
                goodsList.push({
                    goodsId: String(g.goodsId || ''),
                    commonId: String(g.commonId || ''),
                    goodsImage: g.goodsImage || '',
                    goodsName: g.goodsName || '',
                    dealerGoodsFlag: 0,
                    goodsNumber: qty,
                    goodsSpecifications: g.goodsSpec || '',
                    goodsCode: g.goodsCode || '',
                    goodsStock: g.goodsStock || 0,
                    singlePrice: price,
                    goodsTotal: price * qty,
                    activityType: '',
                    combinationType: Number(item.combinationType ?? 0),
                    combinationNum: qty,
                    combinationId: Number(item.combinationId ?? 0),
                    combinationGoodsNum: qty
                })
            }
        })
        if (goodsList.length === 0) {
            Toast.warning('请至少选择一个商品')
            return
        }
        const shopId = item.goodsList?.[0]?.shopId ?? 0
        goodsStore.clearShopList()
        goodsStore.setShopList([{
            shopId,
            shopName: '',
            shopInvoices: 0,
            invoiceActive: 0,
            goodsList,
            shopTotalPrice: getComboPrice(item)
        }])
        navigateTo('/confirmOrder')
    }
}

const goGoodsDetail = (goodsId?: number) => {
    if (goodsId) navigateTo(`/product/${goodsId}`)
}

const handleImgError = (e: Event) => {
    const el = e.target as HTMLImageElement
    if (el) el.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120" viewBox="0 0 24 24" fill="%23ddd"><rect width="24" height="24" rx="2"/></svg>'
}

// 简单横向滚动（可选，暂未实现）
const canScrollLeft = (_item: CombinationBaseVO) => false
const canScrollRight = (_item: CombinationBaseVO) => false
const scrollCarousel = (_item: CombinationBaseVO, _dir: number) => { }
</script>

<style lang="scss" scoped>
.product-combination {
    padding: 24px;
    background: #fff;
}

.section-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0 0 24px 0;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
}

.combination-block {
    display: flex;
    align-items: stretch;
    gap: 24px;
    padding: 24px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
        border-bottom: none;
        padding-bottom: 0;
    }
}

.combination-left {
    flex: 1;
    display: flex;
    align-items: flex-start;
    gap: 16px;
    min-width: 0;
}

.type-badge {
    flex-shrink: 0;
    padding: 6px 14px;
    font-size: 13px;
    font-weight: 600;
    border-radius: 6px;
    line-height: 1.4;

    &.type-fixed {
        background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
        color: var(--color, #1890ff);
    }

    &.type-matching {
        background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
        color: #52c41a;
    }
}

.goods-carousel {
    flex: 1;
    position: relative;
    overflow-x: auto;
    overflow-y: hidden;
    scrollbar-width: thin;
    scrollbar-color: #d9d9d9 transparent;

    &::-webkit-scrollbar {
        height: 6px;
    }

    &::-webkit-scrollbar-thumb {
        background: #d9d9d9;
        border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
        background: #f5f5f5;
        border-radius: 3px;
    }
}

.carousel-inner {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 4px 0;
}

.goods-card {
    flex-shrink: 0;
    width: 135px;
    padding: 12px;
    border: 1px solid #e8e8e8;
    border-radius: 10px;
    background: #fff;
    transition: all 0.2s ease;

    &:hover {
        border-color: #d9d9d9;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    &.card-selected {
        border-color: var(--color, #1890ff);
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.15);
    }

    .goods-image-wrap {
        position: relative;
        cursor: pointer;
        border-radius: 8px;
        overflow: hidden;
    }

    .goods-image {
        width: 100%;
        aspect-ratio: 1;
        border-radius: 8px;
        overflow: hidden;
        background: #fafafa;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }

    .goods-check-badge {
        position: absolute;
        top: 8px;
        left: 8px;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: rgba(0, 0, 0, 0.25);
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s;

        .check-icon {
            color: transparent;
            font-size: 14px;
            font-weight: bold;
        }

        &.selected {
            background: #52c41a;

            .check-icon {
                color: #fff;
            }
        }
    }

    .goods-name {
        font-size: 13px;
        color: #333;
        margin-top: 10px;
        line-height: 1.4;
        height: 2.8em;
        text-align: left;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .goods-spec {
        font-size: 12px;
        color: #999;
        margin-top: 6px;
        text-align: left;
    }

    .goods-qty {
        font-size: 12px;
        color: #666;
        margin-top: 6px;
        text-align: left;
    }

    .goods-qty-control {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        margin-top: 10px;

        input:disabled {
            background: #f5f5f5;
            color: #bfbfbf;
            cursor: not-allowed;
        }
    }
}

.plus-symbol {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    padding-top: 60px;
    font-size: 22px;
    color: #bfbfbf;
    font-weight: 300;
}

.qty-btn {
    width: 28px;
    height: 28px;
    border: 1px solid #d9d9d9;
    background: #fff;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    line-height: 1;
    transition: all 0.2s;

    &:hover:not(:disabled) {
        border-color: var(--color, #1890ff);
        color: var(--color, #1890ff);
    }

    &:disabled {
        opacity: 0.4;
        cursor: not-allowed;
    }
}

.qty-input {
    width: 44px;
    height: 28px;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    text-align: center;
    font-size: 13px;
}

.combination-right {
    flex-shrink: 0;
    width: 220px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 16px;
}

.price-row {
    font-size: 14px;
    color: #333;

    &--combo {
        font-size: 18px;
        font-weight: 600;
        color: var(--color, #1890ff);
    }

    &--original {
        font-size: 13px;
        color: #999;
        text-decoration: line-through;
    }
}

.qty-row {
    display: flex;
    align-items: center;
    gap: 10px;
}

.btn-order {
    padding: 12px 24px;
    font-size: 15px;
    font-weight: 600;
    color: #fff;
    background: var(--color, #1890ff);
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        opacity: 0.9;
        transform: translateY(-1px);
    }

    &:active {
        transform: translateY(0);
    }
}

.carousel-arrow {
    display: none;
}

@media (max-width: 768px) {
    .product-combination {
        padding: 16px;
    }

    .combination-block {
        flex-direction: column;
        padding: 20px 0;
    }

    .combination-left {
        flex-direction: column;
    }

    .combination-right {
        width: 100%;
        align-items: stretch;
    }

    .goods-card {
        width: 117px;
    }
}
</style>
