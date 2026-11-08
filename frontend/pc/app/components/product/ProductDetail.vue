<template>
    <div class="product-detail">
        <div class="container">
            <div class="detail-wrapper">
                <!-- 左侧区域 -->
                <div class="left-section">
                    <!-- 店铺信息 -->
                    <div class="store-info" v-if="props.shopDetail">
                        <div class="store-header">
                            <div class="store-title-wrapper" @click="handleEnterShop">
                                <div class="store-logo" v-if="props.shopDetail.shopLogo">
                                    <img :src="props.shopDetail.shopLogo" :alt="props.shopDetail.shopName" />
                                </div>
                                <h3>{{ $t(props.shopDetail.shopName || '') }}</h3>
                            </div>
                            <span class="store-qualification" @click="handleQualificationClick">{{ $t('店铺资质') }}</span>
                        </div>
                        <div class="store-stats">
                            <div class="stat-item">
                                <span class="stat-label">{{ $t('商品数量') }}</span>
                                <span class="stat-value">{{ props.shopDetail.shopCount || 0 }}</span>
                            </div>
                            <div class="stat-item">
                                <span class="stat-label">{{ $t('收藏数') }}</span>
                                <span class="stat-value">{{ displayShopCollect }}</span>
                            </div>
                        </div>
                        <div class="store-score">
                            <div class="score-item">
                                <span class="label">{{ $t('店铺动态评分') }}</span>
                                <span class="vs">{{ $t('同行业相比') }}</span>
                            </div>
                            <div class="score-list">
                                <div class="score-row">
                                    <span class="label">{{ $t('描述相符') }}</span>
                                    <span class="score">
                                        <span class="number">5.00</span>
                                        <span class="level">{{ $t('优') }}</span>
                                    </span>
                                    <span class="percentage">100%</span>
                                </div>
                                <div class="score-row">
                                    <span class="label">{{ $t('服务态度') }}</span>
                                    <span class="score">
                                        <span class="number">5.00</span>
                                        <span class="level">{{ $t('优') }}</span>
                                    </span>
                                    <span class="percentage">100%</span>
                                </div>
                                <div class="score-row">
                                    <span class="label">{{ $t('发货速度') }}</span>
                                    <span class="score">
                                        <span class="number">5.00</span>
                                        <span class="level">{{ $t('优') }}</span>
                                    </span>
                                    <span class="percentage">100%</span>
                                </div>
                            </div>
                        </div>
                        <div class="store-actions">
                            <button class="btn-enter" @click="handleEnterShop">
                                <span class="icon-home">🏠</span>
                                {{ $t('进入店铺') }}
                            </button>
                            <button class="btn-collect" @click="handleCollectShop">
                                {{ state.isFavoriteShop ? $t('取消收藏') : $t('收藏店铺') }}({{ displayShopCollect }})
                            </button>
                        </div>
                    </div>

                    <!-- 商品推荐 -->
                    <div class="product-recommendation">
                        <div class="recommend-header">
                            <h4>{{ $t('商品排行') }}</h4>
                            <!-- 该列表已由 goods_recommend 智能体重排时标注，未启用时不显示 -->
                            <span v-if="state.agentRecommended === state.activeRecommendTab" class="agent-badge">
                                {{ $t('AI智能推荐') }}
                            </span>
                        </div>
                        <div class="recommend-tabs">
                            <div class="tab" :class="{ active: state.activeRecommendTab === 'hot' }"
                                @click="switchRecommendTab('hot')">
                                {{ $t('热销商品排行') }}
                            </div>
                            <div class="tab" :class="{ active: state.activeRecommendTab === 'favorite' }"
                                @click="switchRecommendTab('favorite')">
                                {{ $t('热门收藏排行') }}
                            </div>
                        </div>
                        <div class="recommend-list">
                            <Loading v-if="state.loadingRecommendGoods" compact :text="$t('加载中...')" />
                            <EmptyState v-else-if="currentRecommendProducts.length === 0" :text="$t('暂无商品数据')"
                                compact />
                            <div v-else class="recommend-item" v-for="(item, index) in currentRecommendProducts"
                                :key="index" @click="handleProductClick(item)">
                                <img :src="getProductImage(item.commonImage || item.goodsImage)"
                                    :alt="item.commonName || item.goodsName" class="product-image">
                                <div class="product-info">
                                    <div class="product-name">{{ $tg(item.commonName || item.goodsName || '') }}</div>
                                    <div class="product-price">{{
                                        formatPriceWithSymbolSafe(item.commonPrice ||
                                            item.goodsPrice) }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 右侧区域 -->
                <div class="right-section">
                    <!-- 商品详情 -->
                    <div class="product-content">
                        <div class="content-tabs">
                            <div class="tab" :class="{ active: state.activeContentTab === 'detail' }"
                                @click="switchContentTab('detail')">
                                {{ $t('商品详情') }}
                            </div>
                            <div class="tab" :class="{ active: state.activeContentTab === 'comment' }"
                                @click="switchContentTab('comment')">
                                {{ $t('商品评论') }}({{ state.commentTotalCount || 0 }})
                            </div>
                        </div>

                        <!-- 商品详情内容 -->
                        <div v-if="state.activeContentTab === 'detail'" class="content-detail">
                            <Loading v-if="state.loadingDesc" compact :text="$t('加载中...')" />
                            <div v-else-if="state.descError" class="error-desc">{{ state.descError }}</div>
                            <template v-else>
                                <ProductDetailContent
                                    v-if="props.categoryAttributesList && props.categoryAttributesList.length > 0"
                                    :category-attributes-list="props.categoryAttributesList" />
                                <div v-if="state.commonDetailDesc" class="detail-body"
                                    v-html="state.commonDetailDesc.commonBody"></div>
                            </template>
                        </div>

                        <!-- 商品评论内容 -->
                        <div v-else-if="state.activeContentTab === 'comment'" class="content-comment">
                            <!-- 问大家（插件 goods-qa）：买家提问 + 商家/买家回答，只读展示 + 提问入口 -->
                            <div class="qa-panel">
                                <div class="qa-panel__head">
                                    <span class="qa-panel__title">{{ $t('问大家') }}</span>
                                    <a class="qa-panel__ask" href="javascript:void(0)" @click.prevent="openAskQa">
                                        {{ $t('我要提问') }} →
                                    </a>
                                </div>
                                <div v-if="qaList.length" class="qa-list">
                                    <div v-for="q in qaList" :key="q.id" class="qa-item">
                                        <div class="qa-item__q">
                                            <span class="qa-tag qa-tag--ask">{{ $t('问') }}</span>
                                            <span class="qa-item__text">{{ q.content }}</span>
                                        </div>
                                        <div v-for="a in q.answers || []" :key="a.id" class="qa-item__a">
                                            <span class="qa-tag" :class="a.isOfficial ? 'qa-tag--shop' : 'qa-tag--ans'">
                                                {{ a.isOfficial ? $t('商家') : $t('答') }}
                                            </span>
                                            <span class="qa-item__text">{{ a.content }}</span>
                                        </div>
                                        <div v-if="!(q.answers || []).length" class="qa-item__empty">
                                            {{ $t('还没有人回答') }}
                                        </div>
                                    </div>
                                </div>
                                <div v-else class="qa-panel__empty">{{ $t('还没有人提问，来问第一个问题') }}</div>
                            </div>
                            <ProductCommentContent :commentStats="state.commentStats"
                                :productComments="state.productComments" :loadingComments="state.loadingComments"
                                :commentError="state.commentError" :currentPage="state.commentCurrentPage"
                                :pageSize="state.commentPageSize" @page-change="handleCommentPageChange" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, computed, watch, ref } from 'vue'
import type { ShopDetail, CommonDetailDesc } from '~/types/goods'
import type { CommentsEntity, PagedByComments } from '~/types/order'
import { getCommonDetailDesc, getRecommendGoods } from '~/services/goods.service'
import { runAgent } from '~/services/agent.service'
import { getComments } from '~/services/order.service'
import { checkFavoriteShopOrNot, addFavoriteShop, deleteFavoriteShop } from '~/services/userms.service'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import ProductDetailContent from './ProductDetailContent.vue'
import ProductCommentContent from './ProductCommentContent.vue'
import { formatPriceWithSymbol } from '~/composables/useFormatPrice'
import { fetchGoodsQa, askGoodsQa } from '~/services/storefront-plugin.service'
import { Toast } from '~/composables/useToast'
import type { MallStore } from '~/types/component'
import type { CategoryAttribute } from '~/types/goods'
import { useIsLogin } from '~/composables/useAuth'

const props = defineProps<{
    shopDetail: ShopDetail
    mallStore: MallStore
    commonId?: number
    goodsId?: string | number
    categoryAttributesList?: CategoryAttribute[]
    /** mall front/detail 已带回的详情 HTML，优先使用，避免再打旧 desc 接口 */
    commonBody?: string | null
}>()

const route = useRoute()

// 问大家（插件 goods-qa）：客户端拉取，失败静默
const qaList = ref<any[]>([])

const loadQa = async () => {
    const id = props.goodsId
    if (!id || !import.meta.client) return
    const data = await fetchGoodsQa(id, 1, 3)
    qaList.value = (data?.list || []) as any[]
}

const openAskQa = async () => {
    const id = props.goodsId
    if (!id) return
    const content = window.prompt('向已购买的买家提问（2-500 字）')
    if (content === null) return
    const text = content.trim()
    if (text.length < 2) {
        Toast.error('问题至少 2 个字')
        return
    }
    const res = await askGoodsQa(id, text)
    if (res) {
        Toast.success('提问成功')
        void loadQa()
    } else {
        Toast.error('提问失败，请稍后再试')
    }
}

watch(() => props.goodsId, () => { void loadQa() }, { immediate: true })

/** 店铺收藏数展示值：与 props 同步，收藏/取消成功后本地 ±1，不重复请求详情 */
const displayShopCollect = ref(0)

const state = reactive({
    // 当前选中的推荐标签
    activeRecommendTab: 'hot', // hot: 热销商品, favorite: 热门收藏
    // 当前选中的内容标签
    activeContentTab: 'detail', // detail: 商品详情, comment: 商品评论
    // 热销商品数据
    hotProducts: [] as any[],
    // 热门收藏商品数据
    favoriteProducts: [] as any[],
    // 推荐商品加载状态
    loadingRecommendGoods: false,
    // 哪一类推荐已由智能体（goods_recommend）重排，用于页面标注；空串表示未生效
    agentRecommended: '',
    // 商品详情描述数据
    commonDetailDesc: null as CommonDetailDesc | null,
    // 加载状态
    loadingDesc: false,
    // 错误信息
    descError: null as string | null,
    // 评论统计信息
    commentStats: null as PagedByComments | null,
    // 评论数据（使用接口返回的原字段）
    productComments: [] as CommentsEntity[],
    // 评论加载状态
    loadingComments: false,
    // 评论错误信息
    commentError: null as string | null,
    // 评论总数
    commentTotalCount: 0,
    // 评论当前页
    commentCurrentPage: 1,
    // 评论每页数量
    commentPageSize: 10,
    // 是否收藏店铺
    isFavoriteShop: false,
    // 查询收藏状态加载中
    loadingFavoriteStatus: false
})

// 当前推荐商品列表
const currentRecommendProducts = computed(() => {
    return state.activeRecommendTab === 'hot' ? state.hotProducts : state.favoriteProducts
})

// 切换推荐标签
const switchRecommendTab = async (tabType: string) => {
    state.activeRecommendTab = tabType
    // 切换标签时获取对应数据
    await fetchRecommendGoods(tabType)
}

// 切换内容标签
const switchContentTab = (tabType: string) => {
    state.activeContentTab = tabType
}

// 获取商品图片，如果没有则返回默认图片
const getProductImage = (image: string | undefined) => {
    if (image) {
        return image
    }
    return props.mallStore.getConfigValueByKey('logo_copywriting_ico_goods') || ''
}

/** 从智能体输出里解析「推荐优先级编号」；解析不出有效顺序时返回 null，由调用方保持原顺序 */
const parseRecommendOrder = (text: string, size: number) => {
    const nums = (String(text || '').match(/\d+/g) || [])
        .map(Number)
        .filter(n => n >= 1 && n <= size)
    const unique: number[] = []
    nums.forEach(n => {
        if (!unique.includes(n)) unique.push(n)
    })
    // 至少要给出 2 个不同编号才认为是一次有效排序，避免单个数字被误当成重排意图
    return unique.length >= 2 ? unique : null
}

/**
 * 商品智能推荐（智能体 goods_recommend）
 *
 * 把候选商品交给智能体，按它给出的优先级编号重排推荐列表。
 * 智能体未启用 / 调用失败 / 输出无法解析时一律返回原列表，页面表现完全不变。
 */
const applyAgentRecommend = async (list: any[], tabType: string) => {
    if (!list || list.length < 2) return list
    const current = (props.shopDetail as any)?.goodsName
        || (props.shopDetail as any)?.goods_name
        || '当前商品'
    const candidates = list
        .map((item, index) => `${index + 1}. ${item?.goods_name || item?.goodsName || item?.name || ''}`)
        .join('\n')

    const result = await runAgent<string>(
        'goods_recommend',
        `当前浏览商品：${current}\n候选推荐商品：\n${candidates}\n请按对该用户的推荐优先级返回编号，只输出用逗号分隔的编号。`,
        {
            bizId: (props.shopDetail as any)?.goodsId || (props.shopDetail as any)?.goods_id || '',
            subjectType: 'goods_detail_recommend',
            subjectKey: tabType,
            // 兜底：智能体不可用时保持原排序
            fallback: () => ''
        }
    )
    if (!result.byAgent || !result.data) return list

    const order = parseRecommendOrder(String(result.data), list.length)
    if (!order) return list
    const picked = order.map(n => list[n - 1]).filter(Boolean)
    const rest = list.filter((_, index) => !order.includes(index + 1))
    return [...picked, ...rest]
}

// 获取推荐商品列表
const fetchRecommendGoods = async (tabType: string) => {
    if (!props.shopDetail?.shopId) {
        return
    }

    // 如果数据已存在，不重复请求
    if (tabType === 'hot' && state.hotProducts.length > 0) {
        return
    }
    if (tabType === 'favorite' && state.favoriteProducts.length > 0) {
        return
    }

    state.loadingRecommendGoods = true

    try {
        const opType = tabType === 'hot' ? 3 : 2 // 3: 热销商品排行, 2: 热门收藏排行
        const response = await getRecommendGoods({
            shopId: props.shopDetail.shopId,
            opType,
            limitSize: 5,
            token: null
        })

        if (response.code === 1 && response.data) {
            const origin = (response.data as any[]) || []
            const sorted = await applyAgentRecommend(origin, tabType)
            // 引用未变说明智能体未生效，保持原排序
            const byAgent = sorted !== origin
            if (tabType === 'hot') {
                state.hotProducts = sorted
            } else {
                state.favoriteProducts = sorted
            }
            if (byAgent) {
                state.agentRecommended = tabType
            } else if (state.agentRecommended === tabType) {
                state.agentRecommended = ''
            }
        }
    } catch (err: any) {
        console.error('获取推荐商品失败:', err)
    } finally {
        state.loadingRecommendGoods = false
    }
}

// 进入店铺
const handleEnterShop = () => {
    if (props.shopDetail?.shopId) {
        navigateTo(`/store/${props.shopDetail.shopId}`)
    }
}

// 处理店铺资质点击（需登录）
const handleQualificationClick = () => {
    if (!props.shopDetail?.shopId) return
    const target = `/store/qualification?id=${props.shopDetail.shopId}`
    if (!useIsLogin(target)) return
    navigateTo(target)
}

// 查询是否收藏店铺
const checkFavoriteShop = async () => {
    if (!props.shopDetail?.shopId) {
        return
    }

    state.loadingFavoriteStatus = true
    try {
        const response = await checkFavoriteShopOrNot({ shopId: props.shopDetail.shopId })
        if (response.code === 1 && response.data) {
            state.isFavoriteShop = response.data === 1
        }
    } catch (err: any) {
        console.error('查询收藏状态失败:', err)
    } finally {
        state.loadingFavoriteStatus = false
    }
}

// 收藏/取消收藏店铺（需登录）
const handleCollectShop = async () => {
    if (!props.shopDetail?.shopId) {
        return
    }

    if (!useIsLogin(route.fullPath)) {
        return
    }

    const shopId = props.shopDetail.shopId

    try {
        const response = state.isFavoriteShop
            ? await deleteFavoriteShop({ shopIds: [String(shopId)] })
            : await addFavoriteShop({
                shopId,
                shopName: props.shopDetail.shopName,
                shopLogo: props.shopDetail.shopLogo || props.shopDetail.wapShopLogo || ''
            })
        if (response.code === 1) {
            if (state.isFavoriteShop) {
                displayShopCollect.value = Math.max(0, displayShopCollect.value - 1)
            } else {
                displayShopCollect.value += 1
            }
            state.isFavoriteShop = !state.isFavoriteShop
        }
    } catch (err: any) {
        console.error(state.isFavoriteShop ? '取消收藏失败' : '收藏店铺失败', err)
    }
}

// 点击商品项
const handleProductClick = (item: any) => {
    const goodsId = item.goodsId || item.commonId
    if (goodsId) {
        navigateTo(`/product/${goodsId}`)
    }
}

// 获取商品详情描述
const fetchCommonDetailDesc = async () => {
    if (!props.commonId) {
        return
    }

    if (props.commonBody) {
        state.commonDetailDesc = {
            commonId: props.commonId,
            commonBody: props.commonBody,
            commonBodyH5: null,
            commonBodyWechat: null
        } as CommonDetailDesc
        state.descError = null
        state.loadingDesc = false
        return
    }

    state.loadingDesc = true
    state.descError = null

    try {
        const response = await getCommonDetailDesc({ commonsId: props.commonId })

        if (response.code === 1 && response.data) {
            state.commonDetailDesc = response.data
        } else {
            throw new Error(response.message || '获取商品详情描述失败')
        }
    } catch (err: any) {
        state.descError = err.message || '加载商品详情描述失败，请稍后重试'
        console.error('获取商品详情描述失败:', err)
    } finally {
        state.loadingDesc = false
    }
}

// 处理文件操作（与预览文件功能相同）
// 获取商品评论列表
const fetchComments = async (evaluation: number = 1, page: number = 1) => {
    if (!props.commonId || !props.goodsId) {
        return
    }

    state.loadingComments = true
    state.commentError = null

    try {
        const response = await getComments({
            token: null,
            commonId: props.commonId,
            evaluation, // 1好评 2中评 3差评
            page,
            limit: 10,
            goodsId: String(props.goodsId)
        })

        if (response.code === 1 && response.data) {
            state.commentStats = response.data
            state.commentTotalCount = response.data.records?.totalCount || 0
            state.commentCurrentPage = response.data.records?.currPage || page
            state.commentPageSize = response.data.records?.pageSize || 10
            state.productComments = response.data.records?.list || []
        } else {
            // 未登录/服务抖动时不阻断详情页，展示空评论即可
            state.productComments = []
            state.commentTotalCount = 0
            state.commentError = null
            console.warn('获取评论未成功:', response?.message || response?.code)
        }
    } catch (err: any) {
        state.productComments = []
        state.commentTotalCount = 0
        state.commentError = null
        console.warn('获取评论失败(已降级为空列表):', err?.message || err)
    } finally {
        state.loadingComments = false
    }
}

// 处理评论分页变化
const handleCommentPageChange = (page: number) => {
    state.commentCurrentPage = page
    fetchComments(1, page) // evaluation 保持为 1（好评），只改变页码
}


// 监听 commonId 变化
watch(() => props.commonId, (newId) => {
    if (newId) {
        fetchCommonDetailDesc()
        if (props.goodsId) {
            fetchComments()
        }
    }
}, { immediate: true })

// 监听 shopDetail.shopId 变化，重新查询收藏状态和推荐商品
watch(() => props.shopDetail?.shopId, (newShopId) => {
    if (newShopId) {
        displayShopCollect.value = Number(props.shopDetail?.shopCollect ?? 0)
        checkFavoriteShop()
        fetchRecommendGoods('hot')
    }
}, { immediate: true })

defineExpose({ state })
</script>

<style lang="scss" scoped>
.product-detail {
    width: 100%;
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 0px 0 20px;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
}

.detail-wrapper {
    display: flex;
    gap: 20px;
}

.left-section {
    width: 300px;
    flex-shrink: 0;
}

.right-section {
    flex: 1;
    background-color: #fff;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    overflow: hidden;
}

// 店铺信息
.store-info {
    background-color: #fff;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    padding: 20px;
    margin-bottom: 20px;

    .store-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        gap: 10px;

        .store-title-wrapper {
            display: flex;
            align-items: center;
            gap: 8px;
            flex-wrap: nowrap;
            cursor: pointer;
            transition: opacity 0.2s;

            &:hover {
                opacity: 0.85;
            }
        }

        h3 {
            font-size: 16px;
            font-weight: 600;
            color: #333;
            margin: 0;
            line-height: 20px;
            height: 20px;
            max-width: 120px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex-shrink: 1;
            min-width: 0;
        }

        .store-qualification {
            font-size: 14px;
            color: #666;
            cursor: pointer;

            &:hover {
                color: var(--color);
            }
        }
    }

    .store-logo {
        flex-shrink: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 30px;
        height: 30px;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 2px;
            display: block;
        }
    }

    .store-stats {
        display: flex;
        justify-content: space-around;
        margin-bottom: 20px;
        padding: 15px 0;
        border-top: 1px solid #f0f0f0;
        border-bottom: 1px solid #f0f0f0;

        .stat-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 5px;

            .stat-label {
                font-size: 12px;
                color: #999;
            }

            .stat-value {
                font-size: 18px;
                font-weight: 600;
                color: #333;
            }
        }
    }

    .store-score {
        margin-bottom: 20px;

        .score-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;

            .label {
                font-size: 14px;
                color: #333;
                font-weight: 500;
            }

            .vs {
                font-size: 12px;
                color: #666;
            }
        }

        .score-list {
            .score-row {
                display: flex;
                align-items: center;
                justify-content: space-around;
                margin-bottom: 10px;

                .label {
                    width: 80px;
                    font-size: 14px;
                    color: #666;
                }

                .score {
                    display: flex;
                    align-items: center;
                    margin-right: 20px;

                    .number {
                        font-size: 14px;
                        color: #333;
                        margin-right: 5px;
                    }

                    .level {
                        font-size: 12px;
                        color: #fff;
                        background-color: var(--color);
                        padding: 2px 8px;
                        border-radius: 3px;
                    }
                }

                .percentage {
                    font-size: 12px;
                    color: #666;
                }
            }
        }
    }

    .store-actions {
        display: flex;
        gap: 10px;

        .btn-enter,
        .btn-collect {
            flex: 1;
            height: 36px;
            border: 1px solid #e8e8e8;
            border-radius: 4px;
            font-size: 14px;
            cursor: pointer;
            transition: all 0.3s;

            &:hover {
                background-color: #f5f5f5;
            }
        }

        .btn-enter {
            background-color: var(--color);
            color: #fff;
            border-color: var(--color);

            &:hover {
                background-color: #1890ff;
            }

            .icon-home {
                margin-right: 5px;
            }
        }

        .btn-collect {
            background-color: #fff;
            color: #666;
        }
    }
}

// 商品推荐
.product-recommendation {
    background-color: #fff;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    padding: 20px;

    .recommend-header {
        display: flex;
        align-items: center;
        gap: 8px;

        h4 {
            font-size: 16px;
            font-weight: 600;
            color: #333;
            margin: 0 0 15px 0;
        }

        .agent-badge {
            margin-bottom: 15px;
            padding: 2px 8px;
            font-size: 12px;
            line-height: 18px;
            color: #409eff;
            background: #ecf5ff;
            border: 1px solid #d9ecff;
            border-radius: 4px;
        }
    }

    .recommend-tabs {
        display: flex;
        margin-bottom: 15px;
        border-bottom: 1px solid #e8e8e8;

        .tab {
            padding: 8px 16px;
            font-size: 14px;
            color: #666;
            cursor: pointer;
            border-bottom: 2px solid transparent;
            margin-bottom: -1px;

            &:hover {
                color: var(--color);
            }

            &.active {
                color: var(--color);
                border-bottom-color: var(--color);
                font-weight: 500;
            }
        }
    }

    .recommend-list {
        .no-products {
            text-align: center;
            padding: 30px 0;
            color: #999;
            font-size: 14px;
        }

        .recommend-item {
            display: flex;
            gap: 10px;
            padding: 10px 0;
            border-bottom: 1px solid #f0f0f0;
            cursor: pointer;
            transition: background-color 0.3s;

            &:hover {
                background-color: #fafafa;
            }

            &:last-child {
                border-bottom: none;
            }

            .product-image {
                width: 80px;
                height: 80px;
                object-fit: cover;
                border-radius: 4px;
                flex-shrink: 0;
            }

            .product-info {
                flex: 1;
                display: flex;
                flex-direction: column;
                justify-content: center;

                .product-name {
                    font-size: 14px;
                    color: #333;
                    margin-bottom: 8px;
                    line-height: 1.4;
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    line-clamp: 2;
                    -webkit-box-orient: vertical;
                    overflow: hidden;
                }

                .product-price {
                    font-size: 16px;
                    color: var(--color);
                    font-weight: 500;

                    &.pending {
                        font-size: 14px;
                        color: var(--color);
                    }
                }
            }
        }
    }
}

// 商品详情
.product-content {
    padding: 20px;

    .content-tabs {
        display: flex;
        margin-bottom: 20px;
        border-bottom: 1px solid #e8e8e8;

        .tab {
            padding: 10px 20px;
            font-size: 16px;
            color: #666;
            cursor: pointer;
            border-bottom: 2px solid transparent;
            margin-bottom: -1px;

            &:hover {
                color: var(--color);
            }

            &.active {
                color: var(--color);
                border-bottom-color: var(--color);
                font-weight: 500;
            }
        }
    }

    .content-detail {

        .error-desc {
            padding: 40px 0;
            text-align: center;
            font-size: 14px;
            color: #666;
        }

        .error-desc {
            color: #f5222d;
        }

        .detail-body {
            padding: 20px 0;
            line-height: 1.8;
            color: #333;

            :deep(img) {
                max-width: 100%;
                height: auto;
            }

            :deep(table) {
                width: 100%;
                border-collapse: collapse;
                margin: 15px 0;
            }

            :deep(td),
            :deep(th) {
                border: 1px solid #e8e8e8;
                padding: 8px;
            }
        }

        .detail-item {
            display: flex;
            margin-bottom: 15px;

            .label {
                width: 120px;
                font-size: 14px;
                color: #666;
                font-weight: 500;
            }

            .value {
                flex: 1;
                font-size: 14px;
                color: #333;
            }
        }
    }

    .content-comment {

        .loading-comments,
        .error-comments {
            padding: 40px 0;
            text-align: center;
            font-size: 14px;
            color: #666;
        }

        .error-comments {
            color: #f5222d;
        }

        .comment-stats {
            display: flex;
            gap: 20px;
            padding: 20px;
            margin-bottom: 20px;
            background-color: #f9f9f9;
            border-radius: 4px;
            flex-wrap: wrap;

            .stats-item {
                display: flex;
                align-items: center;
                gap: 5px;

                .stats-label {
                    font-size: 14px;
                    color: #666;
                }

                .stats-value {
                    font-size: 14px;
                    color: #333;
                    font-weight: 500;
                }
            }
        }

        .no-comments {
            text-align: center;
            padding: 50px 0;
            color: #999;
            font-size: 16px;
        }

        .comments-list {
            .comment-item {
                padding: 20px 0;
                border-bottom: 1px solid #f0f0f0;

                &:last-child {
                    border-bottom: none;
                }

                .comment-header {
                    display: flex;
                    align-items: center;
                    margin-bottom: 15px;

                    .comment-avatar {
                        width: 40px;
                        height: 40px;
                        border-radius: 50%;
                        background-color: #f0f0f0;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: #666;
                        font-weight: 500;
                        font-size: 14px;
                        margin-right: 15px;
                    }
                }
            }
        }
    }

}

// 响应式设计
@media (max-width: 768px) {
    .detail-wrapper {
        flex-direction: column;
    }

    .left-section {
        width: 100%;
    }
}

/* 问大家 */
.qa-panel {
    padding: 16px 20px;
    margin-bottom: 12px;
    background: #fff;
    border: 1px solid #f2f2f2;
    border-radius: 8px;

    &__head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 10px;
    }

    &__title {
        font-size: 15px;
        font-weight: 600;
        color: #333;
    }

    &__ask {
        font-size: 13px;
        color: #999;
        text-decoration: none;

        &:hover {
            color: #ff5000;
        }
    }

    &__empty {
        font-size: 13px;
        color: #bbb;
    }
}

.qa-item {
    padding: 10px 0;
    border-bottom: 1px dashed #f5f5f5;

    &__q,
    &__a {
        display: flex;
        align-items: flex-start;
        gap: 8px;
    }

    &__a {
        margin-top: 6px;
        padding-left: 8px;
    }

    &__text {
        font-size: 13px;
        line-height: 1.6;
        color: #333;
    }

    &__a &__text {
        color: #666;
    }

    &__empty {
        margin-top: 6px;
        padding-left: 40px;
        font-size: 12px;
        color: #bbb;
    }
}

.qa-tag {
    flex: 0 0 auto;
    padding: 1px 6px;
    border-radius: 3px;
    font-size: 11px;
    color: #fff;

    &--ask {
        background: #ff5000;
    }

    &--shop {
        background: #2e7d32;
    }

    &--ans {
        background: #bbb;
    }
}
</style>