<template>
    <div class="store-detail-page">
        <div class="store-detail-container">
            <!-- 顶部：店铺头部和横幅 -->
            <StoreTopSection :shop-detail="shopDetail" :is-favorite="isFavorite"
                :banner-image="shopHeaderInfo?.shopBanner" :loading-favorite="loadingFavorite"
                @qualification-click="handleQualificationClick" @follow-click="handleFollowClick" />

            <!-- 轮播区域 -->
            <div class="store-carousel"
                v-if="shopIndexInfo?.shopBase?.shopSlideurlList && shopIndexInfo.shopBase.shopSlideurlList.length > 0">
                <Swiper :modules="swiperModules" :slides-per-view="1" :space-between="0"
                    :loop="shopIndexInfo.shopBase.shopSlideurlList.length > 1"
                    :autoplay="{ delay: 3000, disableOnInteraction: false, pauseOnMouseEnter: true }"
                    :pagination="{ clickable: true }" class="carousel-swiper">
                    <SwiperSlide v-for="(slide, index) in shopIndexInfo.shopBase.shopSlideurlList" :key="index">
                        <div class="carousel-item" @click="handleSlideClick(slide)">
                            <img :src="slide.shopSlideUrl || slide.url" :alt="`轮播图${index + 1}`" />
                        </div>
                    </SwiperSlide>
                </Swiper>
            </div>

            <!-- 主体内容区域 -->
            <div class="store-content">
                <!-- 左侧边栏 -->
                <div class="store-sidebar">
                    <StoreCategoryFilter :categories="categories" @category-change="handleCategoryChange"
                        @search="handleSearch" />
                    <ProductRanking :shop-id="shopId" :qr-url="shopHeaderInfo?.qrUrl"
                        @product-click="handleProductClick" />
                </div>

                <!-- 右侧主内容 -->
                <div class="store-main">
                    <!-- 初始商品展示区域 -->
                    <template v-if="!showFilterBar">
                        <ProductSection v-if="!selectedCategoryId && !searchKeyword" :title="$t('推荐商品')"
                            :products="recommendedProducts" :show-more="true" @product-click="handleProductClick"
                            @more-click="handleMoreClick('recommended')" />
                        <ProductSection :title="'全部商品'" :products="allProducts" :show-more="true"
                            @product-click="handleProductClick" @more-click="handleMoreClick('all')" />
                    </template>

                    <!-- 筛选栏和展开的商品列表（点击更多/搜索/分类后显示） -->
                    <GoodsFilterList v-if="showFilterBar" :show-recommended-only="showRecommendedOnly"
                        :products="expandedGoodsList" :loading="loadingExpanded" :total-count="expandedTotalCount"
                        v-model:current-page="expandedCurrentPage" :page-size="expandedPageSize" :order-type="orderType"
                        :evaluate="evaluate" :price-order="priceOrder" :sale-num="saleNum" @back="handleBack"
                        @product-click="handleProductClick" @page-change="handleExpandedPageChange"
                        @order-change="handleOrderChange" @evaluate-click="handleEvaluateClick"
                        @price-click="handlePriceClick" @sale-num-click="handleSaleNumClick" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Autoplay, Pagination } from 'swiper/modules'
import 'swiper/css'
import 'swiper/css/pagination'
import { getShopHeaderInfo, getShopIndexInfo } from '~/services/business.service'
import { getShopGoodsListFilter } from '~/services/business.service'
import type { ShopDetail } from '~/types/goods'
import type {
    ListQueryResultVO,
    ShopHeaderInfo,
    WebShopIndexVO,
    ShopSlideUrlVO,
    WebShopGoodsListDTO,
    ManageShopSelfCatMenuVO
} from '~/types/store'
import StoreTopSection from '~/components/store/StoreTopSection.vue'
import StoreCategoryFilter from '~/components/store/StoreCategoryFilter.vue'
import ProductRanking from '~/components/store/ProductRanking.vue'
import ProductSection from '~/components/store/ProductSection.vue'
import GoodsFilterList from '~/components/store/GoodsFilterList.vue'
import { addFavoriteShop, deleteFavoriteShop } from '~/services/userms.service'
import { useUserStore } from '~/stores/modules/user'
import { useIsLogin } from '~/composables/useAuth'
import { ucenterAuthUrl } from '~/utils/ucenterAuthUrl'

// 获取路由参数
const route = useRoute()
const shopId = computed(() => {
    const id = route.params.id as string
    return id ? Number(id) : 0
})

// 响应式数据
const shopDetail = ref<ShopDetail>({
    invoiceActive: 0,
    shopCollect: 0,
    shopCount: 0,
    shopId: shopId.value,
    shopInvoices: 0,
    shopLogo: '',
    shopName: '',
    shopSelfSupport: false,
    userName: '',
    wapShopLogo: '',
    shopSlide: ''
})
const shopHeaderInfo = ref<ShopHeaderInfo | null>(null)
const shopIndexInfo = ref<WebShopIndexVO | null>(null)
const categories = ref<ManageShopSelfCatMenuVO[]>([])
const recommendedProducts = ref<ListQueryResultVO[]>([])
const allProducts = ref<ListQueryResultVO[]>([])
const loadingFavorite = ref(false)

// 用户状态
const userStore = useUserStore()
const selectedCategoryId = ref<number | null>(null)
const searchKeyword = ref('')
const isFavorite = ref(false)
const loading = ref(false)

// 筛选栏相关状态
const showFilterBar = ref(false)
const showRecommendedOnly = ref(false)
const expandedGoodsList = ref<ListQueryResultVO[]>([])
const expandedCurrentPage = ref(1)
const expandedPageSize = ref(16)
const expandedTotalCount = ref(0)
const loadingExpanded = ref(false)

// 筛选参数
const orderType = ref<string | number>('') // ''综合，1新品，2收藏量，3销售量，4价格
const evaluate = ref<number | undefined>(undefined) // 1降序，2升序
const priceOrder = ref<number | undefined>(undefined) // 1降序，2升序
const saleNum = ref<number | undefined>(undefined) // 1降序，2升序

// Swiper 模块
const swiperModules = [Autoplay, Pagination]

// 重置筛选参数
const resetFilterParams = () => {
    orderType.value = ''
    evaluate.value = undefined
    priceOrder.value = undefined
    saleNum.value = undefined
    expandedCurrentPage.value = 1
}

// 获取当前商品类型
const getCurrentGoodsType = (): 'recommended' | 'all' => {
    return showRecommendedOnly.value ? 'recommended' : 'all'
}

// 获取店铺头部信息
const fetchShopHeaderInfo = async () => {
    if (!shopId.value) return

    loading.value = true
    try {
        const response = await getShopHeaderInfo(shopId.value)

        if (response.code === 1 && response.data) {
            // 直接使用接口字段，取消字段映射
            shopHeaderInfo.value = response.data

            // 更新 shopDetail - 直接使用接口字段名
            if (response.data.shopId !== undefined) shopDetail.value.shopId = response.data.shopId
            if (response.data.shopLogo !== undefined) {
                shopDetail.value.shopLogo = response.data.shopLogo
                shopDetail.value.wapShopLogo = response.data.shopLogo
            }
            if (response.data.shopName !== undefined) shopDetail.value.shopName = response.data.shopName
            if (response.data.shopcollect !== undefined) shopDetail.value.shopCollect = response.data.shopcollect
            if (response.data.shopSelfSupport !== undefined) {
                shopDetail.value.shopSelfSupport = typeof response.data.shopSelfSupport === 'string'
                    ? response.data.shopSelfSupport === 'true'
                    : response.data.shopSelfSupport
            }

            // 更新关注状态：type === 1 为已关注，否则未关注
            isFavorite.value = Number(response.data.type) === 1
        }
    } catch (error) {
        console.error('获取店铺头部信息失败:', error)
    } finally {
        loading.value = false
    }
}

// 获取店铺首页信息
const fetchShopIndexInfo = async () => {
    if (!shopId.value) return

    try {
        const response = await getShopIndexInfo(shopId.value)

        if (response.code === 1 && response.data) {
            shopIndexInfo.value = response.data

            // 更新分类数据
            if (response.data.shopSeltCatList) {
                categories.value = response.data.shopSeltCatList
            }

            // 更新店铺信息到 shopDetail
            if (response.data.shopBase) {
                shopDetail.value = {
                    ...shopDetail.value,
                    shopId: response.data.shopBase.shopId || shopDetail.value.shopId,
                    shopLogo: response.data.shopBase.shopLogo || shopDetail.value.shopLogo,
                    shopName: response.data.shopBase.shopName || shopDetail.value.shopName,
                    shopCollect: response.data.shopBase.shopCollect || shopDetail.value.shopCollect,
                    shopSlide: response.data.shopBase.shopSlide || response.data.shopBase.shopBanner || shopDetail.value.shopSlide,
                    wapShopLogo: response.data.shopBase.wapShopLogo || response.data.shopBase.shopLogo || shopDetail.value.wapShopLogo
                }
            }
        }
    } catch (error) {
        console.error('获取店铺首页信息失败:', error)
    }
}

// 获取推荐商品
const fetchRecommendedProducts = async () => {
    if (!shopId.value) return

    try {
        const response = await getShopGoodsListFilter({
            shopId: shopId.value,
            commonIsRecommend: 1,
            page: 1,
            limit: 8
        })

        if (response.code === 1 && response.data) {
            recommendedProducts.value = (response.data as any).list?.slice(0, 8) || []
        } else {
            recommendedProducts.value = []
        }
    } catch (error) {
        console.error('获取推荐商品失败:', error)
        recommendedProducts.value = []
    }
}

// 获取全部商品列表（初始加载）
const fetchAllProducts = async () => {
    if (!shopId.value) return

    try {
        const params: WebShopGoodsListDTO = {
            shopId: shopId.value,
            page: 1,
            limit: 8
        }
        const response = await getShopGoodsListFilter(params)

        if (response.code === 1 && response.data) {
            allProducts.value = (response.data as any).list || []
        } else {
            allProducts.value = []
        }
    } catch (error) {
        console.error('获取商品列表失败:', error)
        allProducts.value = []
    }
}

// 处理轮播图点击
const handleSlideClick = (slide: ShopSlideUrlVO) => {
    if (!slide) return

    switch (slide.opType) {
        case 1: // 新品
        case 2: // 商品编号
            if (slide.commonIdAndGoodsId?.goodsId) {
                navigateTo(`/product/${slide.commonIdAndGoodsId.goodsId}`)
            }
            break
        case 3: // 链接
            if (slide.url) {
                window.open(slide.url, '_blank')
            }
            break
        case 4: // 店铺
            if (slide.shopId) {
                navigateTo(`/store/${slide.shopId}`)
            }
            break
        case 6: // 分类id
            if (slide.catId) {
                navigateTo(`/product/list?catId=${slide.catId}`)
            }
            break
        case 7: // 不跳转
            if (slide.url && /^https?:\/\//i.test(slide.url)) {
                window.open(slide.url, '_blank', 'noopener,noreferrer')
            }
            break
        case 8: // 活动页
            if (slide.activityPage) {
                navigateTo(slide.activityPage)
            }
            break
        default:
            if (slide.url) {
                window.open(slide.url, '_blank')
            }
    }
}

// 处理分类变化
const handleCategoryChange = async (catId: number | null) => {
    selectedCategoryId.value = catId
    searchKeyword.value = ''
    showFilterBar.value = true
    showRecommendedOnly.value = false
    resetFilterParams()
    await fetchExpandedGoodsList('all')
}

// 处理搜索
const handleSearch = async (keyword: string) => {
    searchKeyword.value = keyword
    selectedCategoryId.value = null
    showFilterBar.value = true
    showRecommendedOnly.value = false
    resetFilterParams()
    await fetchExpandedGoodsList('all')
}

// 处理商品点击
const handleProductClick = (product: ListQueryResultVO) => {
    if (product.goodsId) {
        navigateTo(`/product/${product.goodsId}`)
    }
}

// 处理更多点击
const handleMoreClick = async (type: 'recommended' | 'all') => {
    showFilterBar.value = true
    showRecommendedOnly.value = type === 'recommended'
    resetFilterParams()
    await fetchExpandedGoodsList(type)
}

// 获取展开的商品列表
const fetchExpandedGoodsList = async (type: 'recommended' | 'all') => {
    if (!shopId.value) return

    loadingExpanded.value = true
    try {
        const params: WebShopGoodsListDTO = {
            shopId: shopId.value,
            ...(type === 'recommended' && { commonIsRecommend: 1 }),
            page: expandedCurrentPage.value,
            limit: expandedPageSize.value,
            commonName: searchKeyword.value || undefined,
            shopCatId: selectedCategoryId.value ? String(selectedCategoryId.value) : undefined,
            orderType: orderType.value === '' || orderType.value === undefined
                ? undefined
                : (typeof orderType.value === 'string' ? Number(orderType.value) : orderType.value) as number,
            evaluate: evaluate.value,
            priceOrder: priceOrder.value,
            saleNum: saleNum.value
        }

        const response = await getShopGoodsListFilter(params)

        if (response.code === 1 && response.data) {
            expandedGoodsList.value = (response.data as any).list || []
            expandedTotalCount.value = (response.data as any).totalCount || 0
        } else {
            expandedGoodsList.value = []
            expandedTotalCount.value = 0
        }
    } catch (error) {
        console.error('获取展开商品列表失败:', error)
        expandedGoodsList.value = []
        expandedTotalCount.value = 0
    } finally {
        loadingExpanded.value = false
    }
}

// 处理综合排序
const handleOrderChange = () => {
    orderType.value = ''
    evaluate.value = undefined
    priceOrder.value = undefined
    saleNum.value = undefined
    expandedCurrentPage.value = 1
    fetchExpandedGoodsList(getCurrentGoodsType())
}

// 处理排序切换（评价/价格/销量）
const handleSortClick = (sortType: 'evaluate' | 'priceOrder' | 'saleNum') => {
    // 根据排序类型设置 orderType：2收藏量，3销售量，4价格
    if (sortType === 'evaluate') {
        orderType.value = 2 // 收藏量
    } else if (sortType === 'priceOrder') {
        orderType.value = 4 // 价格
    } else if (sortType === 'saleNum') {
        orderType.value = 3 // 销售量
    }

    // 获取当前排序值
    const currentValue = sortType === 'evaluate'
        ? evaluate.value
        : sortType === 'priceOrder'
            ? priceOrder.value
            : saleNum.value

    // 重置其他排序
    if (sortType !== 'evaluate') evaluate.value = undefined
    if (sortType !== 'priceOrder') priceOrder.value = undefined
    if (sortType !== 'saleNum') saleNum.value = undefined

    // 切换当前排序
    if (sortType === 'evaluate') {
        evaluate.value = currentValue === 1 ? 2 : 1
    } else if (sortType === 'priceOrder') {
        priceOrder.value = currentValue === 1 ? 2 : 1
    } else if (sortType === 'saleNum') {
        saleNum.value = currentValue === 1 ? 2 : 1
    }

    expandedCurrentPage.value = 1
    fetchExpandedGoodsList(getCurrentGoodsType())
}

// 处理评价排序
const handleEvaluateClick = () => handleSortClick('evaluate')

// 处理价格排序
const handlePriceClick = () => handleSortClick('priceOrder')

// 处理销量排序
const handleSaleNumClick = () => handleSortClick('saleNum')

// 处理展开列表的分页变化
const handleExpandedPageChange = (page: number) => {
    expandedCurrentPage.value = page
    fetchExpandedGoodsList(getCurrentGoodsType())
}

// 处理返回按钮点击
const handleBack = () => {
    showFilterBar.value = false
    showRecommendedOnly.value = false
    expandedGoodsList.value = []
    expandedTotalCount.value = 0
    searchKeyword.value = ''
    selectedCategoryId.value = null
    resetFilterParams()
    // 重新获取初始商品列表
    fetchRecommendedProducts()
    fetchAllProducts()
}

// 处理店铺资质点击（需登录）
const handleQualificationClick = () => {
    if (!shopId.value) return
    const target = `/store/qualification?id=${shopId.value}`
    if (!useIsLogin(target)) return
    navigateTo(target)
}

// 处理关注点击
const handleFollowClick = async () => {
    // 判断是否登录
    if (!userStore.isLogin) {
        // 未登录，跳转到登录页
        navigateTo(ucenterAuthUrl('login', route.fullPath), { external: true })
        return
    }

    if (!shopDetail.value?.shopId) {
        return
    }

    loadingFavorite.value = true

    try {
        // 如果已关注，直接取消关注
        if (isFavorite.value) {
            const response = await deleteFavoriteShop({
                shopIds: [String(shopDetail.value.shopId)]
            })

            if (response.code === 1) {
                isFavorite.value = false
                if (shopHeaderInfo.value) {
                    shopHeaderInfo.value.type = 2
                    shopHeaderInfo.value.shopcollect = Math.max(0, (shopHeaderInfo.value.shopcollect || 0) - 1)
                    shopDetail.value.shopCollect = shopHeaderInfo.value.shopcollect
                }
            }
        } else {
            const response = await addFavoriteShop({
                shopId: shopDetail.value.shopId,
                shopName: shopDetail.value.shopName,
                shopLogo: shopDetail.value.shopLogo || shopDetail.value.wapShopLogo || ''
            })

            if (response.code === 1) {
                isFavorite.value = true
                if (shopHeaderInfo.value) {
                    shopHeaderInfo.value.type = 1
                    shopHeaderInfo.value.shopcollect = (shopHeaderInfo.value.shopcollect || 0) + 1
                    shopDetail.value.shopCollect = shopHeaderInfo.value.shopcollect
                }
            }
        }
    } catch (error) {
        console.error('操作失败:', error)
    } finally {
        loadingFavorite.value = false
    }
}

// 页面加载时获取数据
onMounted(async () => {
    await Promise.all([
        fetchShopHeaderInfo(),
        fetchShopIndexInfo(),
        fetchRecommendedProducts(),
        fetchAllProducts()
    ])
})
</script>

<style lang="scss" scoped>
.store-detail-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 20px 0 40px;
}

.store-detail-container {
    max-width: 1200px;
    margin: 0 auto;
}


.store-carousel {
    margin-bottom: 20px;
    border-radius: 8px;
    overflow: hidden;
    background-color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    .carousel-swiper {
        width: 100%;
        height: 400px;

        .carousel-item {
            width: 100%;
            height: 100%;
            cursor: pointer;
            transition: transform 0.3s ease;

            &:hover {
                transform: scale(1.02);
            }

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }
    }
}

.store-content {
    display: flex;
    gap: 20px;
    align-items: flex-start;
}

.store-sidebar {
    width: 280px;
    flex-shrink: 0;
}

.store-main {
    flex: 1;
    min-width: 0;
}



/* 响应式 */
@media (max-width: 968px) {
    .store-detail-container {
        padding: 0 16px;
    }

    .store-content {
        flex-direction: column;
        gap: 16px;
    }

    .store-sidebar {
        width: 100%;
    }
}

@media (max-width: 640px) {
    .store-carousel .carousel-swiper {
        height: 200px;
    }
}
</style>
