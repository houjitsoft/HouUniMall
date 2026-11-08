<template>
    <div class="product-list">
        <!-- 导航筛选（已选条件显示） -->
        <NavFilterBar :nav-categories="navCategories" :nav-brands="navBrands" :selected-cat-ids="selectedCatIds"
            :selected-attr-names="selectedAttrNames" :keyword="keyword" @remove-category="handleRemoveCategory"
            @select-category="handleSelectCategory" @remove-brand="handleRemoveBrand"
            @remove-brand-from-filter="handleRemoveBrandFromFilter" @remove-attr="handleRemoveAttr" />

        <!-- 筛选板块（无可用筛选时不渲染，避免空的粉色背景条） -->
        <FilterSection v-if="hasFilterData" :line-sale-categories="lineSaleCategories" :line-brands="lineBrands"
            :attrs="attrs" :selected-cat-ids="selectedCatIds" :selected-brand-ids="selectedBrandIds"
            :selected-attrs="selectedAttrs" :brand-multi-select="brandMultiSelect"
            :attr-multi-select-map="attrMultiSelectMap" @category-change="handleCategoryChange"
            @brand-change="handleBrandChange" @brand-multi-change="handleBrandMultiChange"
            @attr-change="handleAttrChange" @brand-multi-select-change="brandMultiSelect = $event"
            @attr-multi-select-change="handleAttrMultiSelectChange" />

        <!-- 排序和视图切换区域 -->
        <SortViewBar :current-sort="currentSort" :view-mode="viewMode" :total-count="totalCount"
            :price-order="priceOrder" :sale-num="saleNum" :comment-order="commentOrder"
            @sort-change="(orderType: number) => handleSortChange(orderType as OrderType)"
            @view-change="handleViewChange" />

        <!-- 商品列表区域 -->
        <div class="product-list__content">
            <!-- 加载状态 -->
            <transition name="fade" mode="out-in">
                <Loading v-if="isLoading" key="loading" :text="$t('正在加载商品...')" />
            </transition>

            <!-- 商品列表内容 -->
            <transition name="fade-slide" mode="out-in">
                <div v-if="!isLoading" key="goods-content" class="product-list__goods">
                    <!-- 商品列表：卡片（grid）/ 列表（list）两种展示 -->
                    <transition name="fade" appear>
                        <GoodsGrid v-if="goodsList.length > 0" :goods-list="goodsList" :view-mode="viewMode"
                            @goods-click="handleGoodsClick" />
                    </transition>

                    <!-- 空状态 -->
                    <EmptyState v-if="goodsList.length === 0 && !isLoading" :text="$t('抱歉，没有找到与之相关的商品')"
                        :description="$t('您可以尝试调整筛选条件或搜索关键词')" :icon-size="80" :compact="false"
                        :action-text="$t('去首页看看')" action-to="/" />

                    <!-- 分页 -->
                    <transition name="fade" appear>
                        <div v-if="totalCount > 0 && goodsList.length > 0" class="product-list__pagination">
                            <UPagination v-model:page="currentPage" :total="totalCount" :items-per-page="pageSize"
                                @update:page="handlePageChange" />
                        </div>
                    </transition>
                </div>
            </transition>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchGoodsData } from '~/services/goods.service'
import { Toast } from '~/composables/useToast'
import { useIsLogin } from '~/composables/useAuth'
import { parseAttrNameString, convertAttrNameToString, updateUrlParams } from '~/utils/productListUrl'
import { useUserStore } from '~/stores/modules/user'
import { useGoodsStore } from '~/stores/modules/goods'
import FilterSection from '~/components/product/FilterSection.vue'
import SortViewBar from '~/components/product/SortViewBar.vue'
import GoodsGrid from '~/components/product/GoodsGrid.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import Loading from '~/components/Load/Loading.vue'
import type {
    ArticleVO,
    GoodsCatResultVO,
    GoodsBrandVO,
    GoodsAttributeGroupVO,
    GoodsCategoryAttributesVO,
    GoodsList,
    ShopList
} from '~/types/goods'

// ==================== 类型定义 ====================
import type { ProductOrderType, ProductOrderDirection, ProductSearchParams } from '~/types/goods'

type OrderType = ProductOrderType
type OrderDirection = ProductOrderDirection
type SearchParams = ProductSearchParams

// ==================== 路由和状态 ====================
const route = useRoute()
const router = useRouter()

// ==================== 响应式状态 ====================
// 排序状态
const currentSort = ref<OrderType>(3) // 3: 综合排序
const priceOrder = ref<OrderDirection | undefined>(undefined)
const saleNum = ref<OrderDirection | undefined>(undefined)
const commentOrder = ref<OrderDirection | undefined>(undefined)

// 分页状态
const currentPage = ref(1)
const pageSize = ref(20)
const totalCount = ref(0)

// 商品列表和加载状态
const goodsList = ref<ArticleVO[]>([])
const isLoading = ref(false)

// 筛选条件
const lineSaleCategories = ref<GoodsCatResultVO[]>([])
const lineBrands = ref<GoodsBrandVO[]>([])
const attrs = ref<GoodsAttributeGroupVO[]>([])

// 导航筛选条件（已选中的）
const navSaleCategories = ref<GoodsCatResultVO[]>([])
const navBrands = ref<GoodsBrandVO[]>([])

// 选中的筛选条件
const selectedCatIds = ref<number[]>([])
const selectedBrandIds = ref<number[]>([])
const selectedAttrs = ref<Record<string, number[]>>({})
const selectedAttrNames = ref<Array<{ groupName: string; nameList: string[] }>>([])

// 多选模式
const brandMultiSelect = ref(false)
const attrMultiSelectMap = ref<Record<string, boolean>>({})

// 关键词和图片搜索
const keyword = ref<string>('')
const isHasImgSearch = ref(false)

// 展示模式（卡片 grid / 列表 list），默认卡片，持久化到 localStorage
const VIEW_MODE_KEY = 'pc-product-list-view-mode'
const viewMode = ref<'grid' | 'list'>('grid')
if (typeof window !== 'undefined') {
    const saved = window.localStorage.getItem(VIEW_MODE_KEY)
    if (saved === 'grid' || saved === 'list') {
        viewMode.value = saved
    }
}

/** 是否存在可展示的筛选数据（分类/品牌/属性任一非空） */
const hasFilterData = computed(() =>
    lineSaleCategories.value.length > 0 ||
    lineBrands.value.length > 0 ||
    attrs.value.length > 0
)

// ==================== SEO ====================
const currentCategoryName = computed(() => {
    if (selectedCatIds.value.length === 0) return ''
    const cat = lineSaleCategories.value.find(c => c.catId === selectedCatIds.value[0])
    return cat?.catName || ''
})
const seoTitle = computed(() => {
    if (keyword.value) return `${keyword.value} - 商品搜索`
    if (currentCategoryName.value) return `${currentCategoryName.value} - 商品列表`
    return '商品列表'
})
const seoDescription = computed(() => {
    if (keyword.value) return `搜索「${keyword.value}」相关商品，B2C 商城提供企业采购与比价服务。`
    if (currentCategoryName.value) return `${currentCategoryName.value}分类商品列表， B2C 商城企业采购与供应链服务。`
    return 'B2C 商城商品列表，企业采购、分类筛选、比价一站式服务。'
})
useHead({
    title: seoTitle,
    meta: [
        { name: 'description', content: seoDescription }
    ]
})

// ==================== 计算属性 ====================
/**
 * 导航分类（已选中的）
 * @returns 已选中的分类列表
 */
const navCategories = computed<GoodsCatResultVO[]>(() => navSaleCategories.value)

// ==================== 工具函数 ====================
/**
 * 获取商品单价
 * @param goods 商品信息
 * @returns 单价
 */
const getGoodsPrice = (goods: ArticleVO): number => {
    if (goods.activityPrice !== undefined && goods.activityPrice !== null && goods.activityPrice > 0) {
        return goods.activityPrice
    }
    if (goods.goodsPrice !== undefined && goods.goodsPrice !== null && goods.goodsPrice > 0) {
        return goods.goodsPrice
    }
    return goods.commonPrice || 0
}

/**
 * 验证商品信息
 * @param goods 商品信息
 * @returns 是否有效
 */
const validateGoods = (goods: ArticleVO): boolean => {
    const goodsId = goods.goodsId || 0
    if (!goodsId) {
        Toast.error('商品信息不完整')
        return false
    }
    return true
}

/**
 * 检查库存
 * @param goods 商品信息
 * @param quantity 数量
 * @returns 是否通过检查
 */
const checkStock = (goods: ArticleVO, quantity: number): boolean => {
    if (quantity > (goods.goodsStock || 0)) {
        Toast.error('库存不足，请选择其他商品。')
        return false
    }
    return true
}

/**
 * 检查是否购买自家商品
 * @param goods 商品信息
 * @returns 是否通过检查
 */
const checkSelfGoods = (goods: ArticleVO): boolean => {
    const userStore = useUserStore()
    const shopId = userStore.userInfo?.shopId
    if (shopId && shopId === goods.shopId) {
        Toast.error('请勿购买自家商品！')
        return false
    }
    return true
}

// ==================== URL参数处理 ====================
/**
 * 从URL参数初始化数据
 * 解析路由查询参数并设置对应的响应式状态
 */
const initFromUrlParams = (): void => {
    const query = route.query

    // 关键词
    const kws = query.kws as string | undefined
    keyword.value = kws ? decodeURIComponent(kws) : ''

    // 分类ID（兼容 catId 和 catid 两种参数名）
    const catId = (query.catId || query.catid) as string | undefined
    selectedCatIds.value = catId ? [Number(catId)] : []

    // 品牌ID
    const brand = query.brand as string | undefined
    selectedBrandIds.value = brand
        ? brand.split('-').map(id => Number(id)).filter(id => !isNaN(id))
        : []

    // 属性名称
    const attrName = query.attrName as string | undefined
    if (attrName) {
        const parsed = parseAttrNameString(decodeURIComponent(attrName))
        selectedAttrNames.value = parsed
        syncAttrNamesToIds(parsed)
    } else {
        selectedAttrNames.value = []
        selectedAttrs.value = {}
    }

    // 图片搜索
    isHasImgSearch.value = query.image === 'true'

    // 排序类型
    const orderType = query.orderType as string | undefined
    if (orderType) {
        const orderTypeNum = Number(orderType) as OrderType
        if (orderTypeNum >= 0 && orderTypeNum <= 6) {
            currentSort.value = orderTypeNum
        }
    }

    // 价格排序方向
    const priceOrderParam = query.priceOrder as string | undefined
    if (priceOrderParam) {
        const priceOrderNum = Number(priceOrderParam) as OrderDirection
        if (priceOrderNum === 1 || priceOrderNum === 2) {
            priceOrder.value = priceOrderNum
        }
    }

    // 销量排序方向
    const saleNumParam = query.saleNum as string | undefined
    if (saleNumParam) {
        const saleNumNum = Number(saleNumParam) as OrderDirection
        if (saleNumNum === 1 || saleNumNum === 2) {
            saleNum.value = saleNumNum
        }
    }

    // 评论排序方向
    const commentOrderParam = query.commentOrder as string | undefined
    if (commentOrderParam) {
        const commentOrderNum = Number(commentOrderParam) as OrderDirection
        if (commentOrderNum === 1 || commentOrderNum === 2) {
            commentOrder.value = commentOrderNum
        }
    }
}

/**
 * 处理展示模式切换（卡片 / 列表）
 * @param mode 视图模式
 */
const handleViewChange = (mode: 'grid' | 'list'): void => {
    if (viewMode.value === mode) return
    viewMode.value = mode
    if (typeof window !== 'undefined') {
        window.localStorage.setItem(VIEW_MODE_KEY, mode)
    }
}

/**
 * 同步属性名称到ID
 * 将URL中的属性名称转换为对应的属性ID
 * @param parsed 解析后的属性名称列表
 */
const syncAttrNamesToIds = (parsed: Array<{ groupName: string; nameList: string[] }>): void => {
    if (attrs.value.length === 0) {
        selectedAttrs.value = {}
        return
    }

    selectedAttrs.value = {}
    parsed.forEach(group => {
        const attrIds: number[] = []
        const attrGroup = attrs.value.find((ag: GoodsAttributeGroupVO) => ag.groupName === group.groupName)
        if (attrGroup?.goodsAttributeList) {
            group.nameList.forEach(name => {
                const attr = attrGroup.goodsAttributeList?.find(
                    (a: GoodsCategoryAttributesVO) => a.attributeValueName === name
                )
                if (attr?.id) {
                    attrIds.push(attr.id)
                }
            })
        }
        if (attrIds.length > 0) {
            selectedAttrs.value[group.groupName] = attrIds
        }
    })
}

// ==================== 筛选处理 ====================
/**
 * 处理分类变化
 */
const handleCategoryChange = (catId: number) => {
    const index = selectedCatIds.value.indexOf(catId)
    if (index > -1) {
        selectedCatIds.value.splice(index, 1)
    } else {
        selectedCatIds.value = [catId]
    }
    currentPage.value = 1
    updateUrlAndFetch()
}

/**
 * 选择分类
 * @param catId 分类ID
 */
const handleSelectCategory = (catId: number): void => {
    selectedCatIds.value = [catId]
    currentPage.value = 1
    updateUrlAndFetch()
}

/**
 * 移除分类筛选
 * @param catId 分类ID
 * @param catParentId 父分类ID
 */
const handleRemoveCategory = (catId: number, catParentId?: number): void => {
    if (catParentId === 0 || catParentId == null) {
        // 移除顶级分类，清空筛选
        selectedCatIds.value = []
        navSaleCategories.value = []
    } else if (catId === selectedCatIds.value[0]) {
        // 移除当前子分类，回退到父分类（如删除「女装」后 catId 应为 43「服饰」）
        selectedCatIds.value = [catParentId]
        navSaleCategories.value = navSaleCategories.value.filter(cat => cat.catId !== catId)
    } else {
        const index = selectedCatIds.value.indexOf(catId)
        if (index > -1) {
            selectedCatIds.value.splice(index, 1)
        }
        navSaleCategories.value = navSaleCategories.value.filter(cat => cat.catId !== catId)
    }
    currentPage.value = 1
    updateUrlAndFetch()
}

/**
 * 处理品牌变化（单选模式）
 * @param brandId 品牌ID
 */
const handleBrandChange = (brandId: number): void => {
    if (!brandMultiSelect.value && selectedBrandIds.value.length > 0 && !selectedBrandIds.value.includes(brandId)) {
        selectedBrandIds.value = [brandId]
    } else {
        const index = selectedBrandIds.value.indexOf(brandId)
        if (index > -1) {
            selectedBrandIds.value.splice(index, 1)
        } else {
            selectedBrandIds.value.push(brandId)
        }
    }
    currentPage.value = 1
    updateUrlAndFetch()
}

/**
 * 处理品牌多选变化（多选模式确认时）
 * @param brandIds 品牌ID数组
 */
const handleBrandMultiChange = (brandIds: number[]): void => {
    selectedBrandIds.value = [...brandIds]
    currentPage.value = 1
    updateUrlAndFetch()
}

/**
 * 移除品牌筛选
 */
const handleRemoveBrand = (): void => {
    selectedBrandIds.value = []
    currentPage.value = 1
    updateUrlAndFetch()
}

/**
 * 从多选品牌中移除单个品牌
 * @param brandId 品牌ID
 */
const handleRemoveBrandFromFilter = (brandId: number): void => {
    const index = selectedBrandIds.value.indexOf(brandId)
    if (index > -1) {
        selectedBrandIds.value.splice(index, 1)
        currentPage.value = 1
        updateUrlAndFetch()
    }
}

/**
 * 处理属性变化
 * @param groupName 属性组名称
 * @param attrId 属性ID
 * @param attrValueName 属性值名称
 */
const handleAttrChange = (groupName: string, attrId: number, attrValueName: string): void => {
    if (!selectedAttrs.value[groupName]) {
        selectedAttrs.value[groupName] = []
    }

    const isMultiSelect = attrMultiSelectMap.value[groupName] || false

    if (!isMultiSelect && selectedAttrs.value[groupName].length > 0 && !selectedAttrs.value[groupName].includes(attrId)) {
        selectedAttrs.value[groupName] = [attrId]
    } else {
        const index = selectedAttrs.value[groupName].indexOf(attrId)
        if (index > -1) {
            selectedAttrs.value[groupName].splice(index, 1)
        } else {
            selectedAttrs.value[groupName].push(attrId)
        }
    }

    updateSelectedAttrNames()
    currentPage.value = 1
    updateUrlAndFetch()
}

/**
 * 处理属性多选模式变化
 * @param groupName 属性组名称
 * @param value 是否多选
 */
const handleAttrMultiSelectChange = (groupName: string, value: boolean): void => {
    attrMultiSelectMap.value[groupName] = value
}

/**
 * 更新选中的属性名称列表
 * 根据选中的属性ID更新属性名称列表，用于URL参数和显示
 */
const updateSelectedAttrNames = (): void => {
    const result: Array<{ groupName: string; nameList: string[] }> = []

    Object.keys(selectedAttrs.value).forEach(groupName => {
        const attrIds = selectedAttrs.value[groupName]
        if (attrIds && attrIds.length > 0) {
            const attrGroup = attrs.value.find((ag: GoodsAttributeGroupVO) => ag.groupName === groupName)
            if (attrGroup?.goodsAttributeList) {
                const nameList = attrGroup.goodsAttributeList
                    .filter((attr: GoodsCategoryAttributesVO) => attr.id && attrIds.includes(attr.id))
                    .map((attr: GoodsCategoryAttributesVO) => attr.attributeValueName || '')
                    .filter(Boolean)

                if (nameList.length > 0) {
                    result.push({ groupName, nameList })
                }
            }
        }
    })

    selectedAttrNames.value = result
}

/**
 * 移除属性筛选
 * @param groupName 属性组名称
 */
const handleRemoveAttr = (groupName: string): void => {
    delete selectedAttrs.value[groupName]
    selectedAttrNames.value = selectedAttrNames.value.filter(item => item.groupName !== groupName)
    currentPage.value = 1
    updateUrlAndFetch()
}

// ==================== 排序处理 ====================
/**
 * 处理排序变化
 * @param orderType 排序类型
 */
const handleSortChange = (orderType: OrderType): void => {
    if (currentSort.value === orderType) {
        // 切换排序方向
        if (orderType === 1) {
            priceOrder.value = priceOrder.value === 1 ? 2 : 1
        } else if (orderType === 2) {
            saleNum.value = saleNum.value === 1 ? 2 : 1
        } else if (orderType === 5) {
            commentOrder.value = commentOrder.value === 1 ? 2 : 1
        }
    } else {
        // 切换到新的排序类型
        currentSort.value = orderType

        if (orderType === 1) {
            priceOrder.value = 1
            saleNum.value = undefined
            commentOrder.value = undefined
        } else if (orderType === 2) {
            saleNum.value = 1
            priceOrder.value = undefined
            commentOrder.value = undefined
        } else if (orderType === 5) {
            commentOrder.value = 1
            priceOrder.value = undefined
            saleNum.value = undefined
        } else {
            priceOrder.value = undefined
            saleNum.value = undefined
            commentOrder.value = undefined
        }
    }

    currentPage.value = 1
    updateUrlAndFetch()
}

// ==================== 分页处理 ====================
/**
 * 处理分页变化
 */
const handlePageChange = (page: number) => {
    currentPage.value = page
    fetchGoodsList()
    if (typeof window !== 'undefined') {
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }
}

// ==================== 商品操作 ====================

/**
 * 处理商品点击
 * @param goods 商品信息
 */
const handleGoodsClick = (goods: ArticleVO): void => {
    const goodsId = goods.goodsId || goods.commonId || 0;
    if (goodsId) {
        router.push(`/product/${goodsId}`)
    }
}

// ==================== URL和API处理 ====================
/**
 * 更新URL并获取数据
 * 根据当前筛选条件更新URL参数，watch会自动监听URL变化并获取数据
 */
const updateUrlAndFetch = (): void => {
    const params: Record<string, string | number | null | undefined> = {}

    params.kws = keyword.value || null
    params.catId = selectedCatIds.value.length > 0 ? selectedCatIds.value[0] : null
    params.brand = selectedBrandIds.value.length > 0 ? selectedBrandIds.value.join('-') : null
    params.attrName = selectedAttrNames.value.length > 0 ? convertAttrNameToString(selectedAttrNames.value) : null
    params.image = isHasImgSearch.value ? 'true' : null
    params.orderType = currentSort.value
    params.priceOrder = priceOrder.value || null
    params.saleNum = saleNum.value || null
    params.commentOrder = commentOrder.value || null

    updateUrlParams(params)
    // 注意：不在这里调用 fetchGoodsList，由 watch 监听 URL 变化后自动调用
}

/**
 * 获取商品列表
 */
let goodsFetchSeq = 0
const syncSelectedCatFromRoute = () => {
    const raw = route.query.catId ?? route.query.catid
    const catId = Number(Array.isArray(raw) ? raw[0] : raw) || 0
    if (catId > 0) {
        selectedCatIds.value = [catId]
    }
}

const fetchGoodsList = async () => {
    const seq = ++goodsFetchSeq
    isLoading.value = true
    // URL 上的 catId / kws 为准，避免并发请求在 ref 未同步时丢掉筛选条件
    syncSelectedCatFromRoute()
    const rawKws = route.query.kws
    const routeKws = Array.isArray(rawKws) ? rawKws[0] : rawKws
    if (typeof routeKws === 'string' && routeKws.trim()) {
        try {
            keyword.value = decodeURIComponent(routeKws)
        } catch {
            keyword.value = routeKws
        }
    }

    try {
        const params: SearchParams = {
            page: currentPage.value,
            limit: pageSize.value,
            orderType: currentSort.value
        }

        if (keyword.value && !isHasImgSearch.value) {
            // 后端 MallGoodsSearchDTO 主字段为 name；兼容旧 ES 字段 commonName/goodsName
            params.name = keyword.value
            params.commonName = keyword.value
            params.goodsName = keyword.value
            params.kws = keyword.value
        }

        if (isHasImgSearch.value && typeof window !== 'undefined') {
            const searchBase64Str = sessionStorage.getItem('searchBase64Str')
            if (searchBase64Str) {
                if (saleNum.value === 2) {
                    params.imagePath = searchBase64Str
                } else {
                    params.base64Str = searchBase64Str
                }
            }
        }

        if (selectedCatIds.value.length > 0) {
            params.catId = selectedCatIds.value[0]
            ;(params as any).categoryId = selectedCatIds.value[0]
        }

        if (selectedBrandIds.value.length > 0) {
            params.brandIdList = selectedBrandIds.value
        }

        if (priceOrder.value !== undefined && priceOrder.value !== null) {
            params.priceOrder = priceOrder.value as number
        }

        if (saleNum.value !== undefined && saleNum.value !== null) {
            params.saleNum = saleNum.value as number
        }

        if (commentOrder.value !== undefined && commentOrder.value !== null) {
            params.commentOrder = commentOrder.value as number
        }

        if (selectedAttrNames.value.length > 0) {
            const attrNameStr = convertAttrNameToString(selectedAttrNames.value)
            if (attrNameStr) {
                params.attrName = attrNameStr + ';'
            } else {
                params.attrName = undefined
            }
        } else {
            params.attrName = undefined
        }

        const response = await searchGoodsData(params)
        if (seq !== goodsFetchSeq) return

        if (response?.code === 1 && response.data) {
            const data = response.data
            const rawList = data.goodsList || data.lists || data.list || []
            goodsList.value = Array.isArray(rawList) ? rawList : []
            totalCount.value = Number(data.goodsCount ?? data.count ?? goodsList.value.length ?? 0)

            if (data.lineSaleCategories) {
                lineSaleCategories.value = data.lineSaleCategories
            }
            if (data.lineBrands) {
                lineBrands.value = data.lineBrands
            }
            if (data.attrs) {
                attrs.value = data.attrs
            }

            navSaleCategories.value = data.navSaleCategories && data.navSaleCategories.length > 0
                ? data.navSaleCategories
                : []
            navBrands.value = data.navBrands && data.navBrands.length > 0
                ? data.navBrands
                : []

            if (data.keyword) {
                keyword.value = data.keyword
            }

            // 已移除大图操作按钮（数量/加购），无需初始化数量状态

            // 同步属性数据
            if (attrs.value.length > 0) {
                const urlAttrName = route.query.attrName as string | undefined
                if (urlAttrName) {
                    const parsed = parseAttrNameString(decodeURIComponent(urlAttrName))
                    selectedAttrNames.value = parsed
                    syncAttrNamesToIds(parsed)
                } else {
                    updateSelectedAttrNames()
                }
            }
        } else {
            Toast.error(response?.message || '获取商品列表失败')
        }
    } catch (error: any) {
        if (seq !== goodsFetchSeq) return
        console.error('获取商品列表失败:', error)
        Toast.error(error?.message || '获取商品列表失败，请稍后重试')
    } finally {
        if (seq === goodsFetchSeq) {
            isLoading.value = false
        }
    }
}

// ==================== 生命周期 ====================
// 监听路由参数变化
watch(() => route.query, () => {
    // 每次都以 URL 为准同步筛选条件，再拉列表，避免并发请求丢掉 catId
    initFromUrlParams()
    fetchGoodsList()
}, { immediate: true })

// 展示模式（卡片/列表）由 viewMode 响应式状态与 handleViewChange 管理
</script>

<style lang="scss" scoped>
.product-list {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px 60px;
    background-color: #fff;
    min-height: calc(100vh - 200px);
    box-sizing: border-box;

    @media (max-width: 1200px) {
        padding: 0 10px 40px;
    }

    @media (max-width: 768px) {
        padding: 0 8px 30px;
    }

    // 商品内容区域
    &__content {
        width: 100%;
        animation: fadeIn 0.4s ease-in-out;
    }

    // 分页
    &__pagination {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: 40px;
        padding: 30px 0 20px;
        border-top: 1px solid #f0f0f0;
        transition: all 0.3s ease;

        &:hover {
            border-top-color: #e0e0e0;
        }

        @media (max-width: 968px) {
            margin-top: 30px;
            padding: 25px 0 15px;
        }

        @media (max-width: 768px) {
            margin-top: 20px;
            padding: 20px 0 10px;
        }
    }
}

// 过渡动画
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
    opacity: 0;
    transform: translateY(20px);
}

.fade-slide-leave-to {
    opacity: 0;
    transform: translateY(-20px);
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

// 动画
@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>