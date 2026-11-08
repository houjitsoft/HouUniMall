<template>
    <div class="banner-box">
        <!-- 全屏背景图层 -->
        <div class="banner-box-bg" :style="{ backgroundImage: bannerFullBgImage }"></div>
        <div class="banner-content">
            <!-- 左侧分类导航 -->
            <BannerLeftCategory class="left-category" />

            <!-- 中部主舞台：上排品牌轮播，下排四个促销模块同处一行 -->
            <div class="main-stage">
                <div class="stage-top">
                    <div class="banner-container">
                        <Swiper
                            ref="bannerSwiper"
                            :loop="banners.length > 1"
                            :autoplay="banners.length > 1 ? { delay: 5000, disableOnInteraction: false } : false"
                            :modules="[Autoplay, Pagination, Navigation]"
                            :pagination="{ clickable: true }"
                            :navigation="banners.length > 1"
                            class="banner-swiper"
                            v-if="banners.length > 0"
                        >
                            <SwiperSlide v-for="(banner, index) in banners" :key="index">
                                <div class="carousel-item" @click="handleBannerClick(banner)">
                                    <div v-if="isDemoBanner(banner.configValue)" class="discovery-hero" :class="`discovery-hero--${index % 3}`">
                                        <div class="discovery-copy"><span class="discovery-kicker">{{ $t(['生活好物精选', '发现你的新风格', '日常所需，一站选购'][index % 3]) }}</span><h1>{{ $t(['把喜欢的好物\n带进日常生活', '穿搭新灵感\n从这里开始', '认真挑选\n每一份生活所需'][index % 3]) }}</h1><span class="discovery-action">{{ $t('去逛逛') }} <span aria-hidden="true">↗</span></span></div>
                                        <div class="discovery-art" aria-hidden="true"><div class="shopping-bag shopping-bag--back"><span>U</span></div><div class="shopping-bag shopping-bag--front"><span>UniMall</span></div><span class="discovery-note">{{ $t('好物 · 好生活') }}</span></div>
                                    </div>
                                    <img v-else :src="banner.configValue" alt="" class="carousel-image" />
                                </div>
                            </SwiperSlide>
                        </Swiper>
                        <div v-else class="banner-placeholder">
                            <div class="placeholder-skeleton">
                                <div class="skeleton-shimmer"></div>
                            </div>
                            <div class="placeholder-content">
                                <div class="placeholder-spinner"></div>
                                <span class="placeholder-text">{{ $t('加载中...') }}</span>
                            </div>
                        </div>
                    </div>

                </div>

                <div class="stage-bottom">
                    <BannerPromoPanel class="subsidy-panel" :title="$t(displaySubsidyTitle)" :items="displaySubsidyProducts" :cols="4" more-link="/product/list" @select="handlePromoClick" />
                    <BannerLivePanel
                        :title="$t(displayLiveTitle)"
                        :cover-image="liveInfo.image"
                        @select="() => handlePromoClick(liveInfo)"
                    />
                    <BannerPromoPanel
                        :title="$t(displaySeckillTitle)"
                        :items="displaySeckillProducts"
                        :cols="2"
                        more-link="/activity/seckill"
                        @select="handlePromoClick"
                    />
                    <BannerPromoPanel
                        :title="$t(displayDiscountTitle)"
                        :items="displayDiscountProducts"
                        :cols="2"
                        more-link="/activity/discount"
                        @select="handlePromoClick"
                    />
                </div>
            </div>

            <!-- 右侧信息区域 -->
            <div class="right-sidebar">
                <div class="welcome-section">
                    <div class="welcome-top">
                        <div class="welcome-logo">
                            <div class="logo-icon" v-if="userAvatar">
                                <img :src="userAvatar" :alt="$t('用户头像')" class="user-logo" />
                            </div>
                            <div class="logo-icon" v-else-if="userLogo">
                                <img :src="userLogo" :alt="$t('用户logo')" class="user-logo" />
                            </div>
                            <div v-else class="visitor-avatar"><UIcon name="i-heroicons-user" /></div>
                        </div>
                        <div class="welcome-text">
                            <div class="welcome-hi">
                                <span v-if="isEnterprise" class="enterprise-badge">企</span>
                                <span>{{ $t(companyNameText) }}</span>
                            </div>
                            <template v-if="!isLogin">
                                <span class="welcome-register" @click="handleRegisterClick">{{ $t('免费注册') }}</span>
                            </template>
                            <template v-else>
                                <span class="welcome-register" @click="handleUserClick">{{ displayUserName }}</span>
                            </template>
                        </div>
                    </div>
                    <button v-if="!isLogin" class="login-main-btn" @click="handleLoginClick">{{ $t('立即登录') }}</button>
                    <button v-else class="login-main-btn" @click="handleLogoutClick">{{ $t('退出') }}</button>
                    <div v-if="showPlusEntry" class="plus-banner-entry" @click="handlePlusBannerClick">
                        <span
                            class="plus-banner-entry-icon"
                            role="presentation"
                            :style="{ backgroundImage: `url(${IMAGE_URL('plusImg/zuanshi.png')})` }"
                        />
                        <span class="plus-banner-entry-text">{{ $t(plusActionText) }}</span>
                    </div>
                </div>

                <div class="news-section">
                    <div class="section-header">
                        <h4>{{ $t('商城快报') }}</h4>
                    </div>
                    <div class="news-list">
                        <Swiper
                            ref="newsSwiper"
                            direction="vertical"
                            :loop="newsList.length > 1"
                            :autoplay="{ delay: 2000, disableOnInteraction: false, pauseOnMouseEnter: true }"
                            :slidesPerView="3"
                            :spaceBetween="0"
                            :modules="[Autoplay]"
                            class="news-swiper"
                            v-if="newsList.length > 0"
                        >
                            <SwiperSlide v-for="(news, index) in newsList" :key="index">
                                <div class="news-item" @click="handleNewsClick(news)">
                                    <span class="news-number" :class="`rank-${Math.min(index + 1, 3)}`">{{ index + 1 }}</span>
                                    <span class="news-title">{{ $t(news.informationTitle) }}</span>
                                </div>
                            </SwiperSlide>
                        </Swiper>
                        <div v-else class="news-placeholder">{{ $t('暂无新闻') }}</div>
                    </div>
                </div>

                <div class="quick-links">
                    <div class="section-header">
                        <h4>{{ $t('快捷入口') }}</h4>
                    </div>
                    <div class="quick-links-grid">
                        <div
                            class="quick-link-item"
                            v-for="(link, index) in quickLinks"
                            :key="index"
                            @click="handleQuickLinkClick(link)"
                        >
                            <img v-if="link.iconUrl" :src="link.iconUrl" class="quick-link-img" :alt="link.text" />
                            <div v-else class="quick-link-icon">{{ link.icon }}</div>
                            <div class="quick-link-text">{{ $t(link.text) }}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMallIndexData, flattenMallIndexModules, getMallHomeAggregate, queryPcIndexSlideModule, selectByConfigKey } from '~/services/base.service'
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Autoplay, Pagination, Navigation } from 'swiper/modules'
import 'swiper/css'
import 'swiper/css/pagination'
import 'swiper/css/navigation'
import { useUserStore } from '~/stores/modules/user'
import { useMallStore } from '~/stores/modules/mall'
import { useAuth } from '~/composables/useAuth'
import { usePlus } from '~/composables/usePlus'
import { IMAGE_URL, UCENTER_URL } from '~/utils/externalUrl'
import { ucenterAuthUrl } from '~/utils/ucenterAuthUrl'
import type { BannerItem, NewsItem, QuickLinkItem } from '~/types/home'
import BannerPromoPanel from '~/components/home/BannerPromoPanel.vue'
import BannerLivePanel from '~/components/home/BannerLivePanel.vue'

/** 推广区商品 */
interface PromoItem {
    image: string
    price: string
    name?: string
    label?: string
    commonId?: string
    goodsId?: string
    url?: string
    dataType?: string | number
}

interface SlideModuleData {
    title?: string
    title?: string
    type?: string | number
    data?: Array<{
        dataType?: string | number
        commonId?: string
        goodsId?: string
        images?: string
        commonImage?: string
        commonName?: string
        commonPrice?: string | number
        price?: string | number
        copywriting?: string
        labelText?: string
        url?: string
        [key: string]: any
    }>
}

const banners = ref<BannerItem[]>([])
// 只增强项目内置的抽象演示图，商家从后台上传的轮播图保持原样。
const isDemoBanner = (url: string) => /\/demo\/banner-0[123]\.svg(?:[?#]|$)/.test(url)
const newsList = ref<NewsItem[]>([])
const userLogo = ref<string>('')

const liveTitle = ref('')
const subsidyTitle = ref('')
const seckillTitle = ref('')
const discountTitle = ref('')

const liveInfo = ref<PromoItem>({ image: '', price: '' })
const subsidyProducts = ref<PromoItem[]>([])
const seckillProducts = ref<PromoItem[]>([])
const discountProducts = ref<PromoItem[]>([])

/** 各卡片按布局容量裁剪，杜绝商品把相邻卡片撑破 */
const displaySubsidyProducts = computed(() => subsidyProducts.value.slice(0, 4))
const displaySeckillProducts = computed(() => seckillProducts.value.slice(0, 2))
const displayDiscountProducts = computed(() => discountProducts.value.slice(0, 2))
const displaySubsidyTitle = computed(() => subsidyTitle.value || '国家补贴')
const displayLiveTitle = computed(() => liveTitle.value || '精选直播')
const displaySeckillTitle = computed(() => seckillTitle.value || '限时秒杀')
const displayDiscountTitle = computed(() => discountTitle.value || '满减折扣')

const userStore = useUserStore()
const mallStore = useMallStore()
const { logout } = useAuth()
const { showPlusEntry, plusActionText, plusStore } = usePlus()

const isLogin = computed(() => userStore.isLogin)

const bannerFullBgUrl = computed(() => {
    const cfg = mallStore.navLogoConfigs.find((item) => item.configKey === 'logo_copywriting_ico_pc_index')
    return cfg?.configValue || ''
})

const bannerFullBgImage = computed(() => {
    return bannerFullBgUrl.value ? `url(${bannerFullBgUrl.value})` : 'none'
})

const userAvatar = computed(() => {
    return userStore.userInfo?.userAvatar || ''
})

const displayUserName = computed(() => {
    const userInfo = userStore.userInfo
    if (!userInfo) return ''
    if (userInfo.userNickname) return userInfo.userNickname
    if (userInfo.userName) return userInfo.userName
    if (userInfo.userMobile) return userInfo.userMobile
    return '用户'
})

const isEnterprise = computed(() => {
    return userStore.userInfo?.isBusiness === 1
})

const companyNameText = computed(() => {
    const userInfo = userStore.userInfo
    if (!userInfo) {
        return 'HI～，欢迎逛商城！'
    }
    if (userInfo.isBusiness === 1) {
        if (userInfo.businessDetail?.companyName) {
            return userInfo.businessDetail.companyName
        }
        if (userInfo.companyName) {
            return userInfo.companyName
        }
    }
    return 'HI～，欢迎逛商城！'
})

const formatPrice = (price: string | number | undefined | null) => {
    if (price === '' || price === null || price === undefined) return ''
    const num = Number(price)
    if (Number.isNaN(num)) return String(price)
    return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

const getModuleTitle = (moduleData?: SlideModuleData | null, fallback = '') => {
    return (moduleData?.title || fallback || '').trim() || fallback
}

/** 解析商品类模块（国补/新品/秒杀/满减），并按容量截断 */
const mapGoodsModule = (moduleData?: SlideModuleData | null, limit = 4): PromoItem[] => {
    const list = Array.isArray(moduleData?.data) ? moduleData!.data! : []
    return list
        .filter((item) => item.images || item.commonImage || item.commonId || item.goodsId)
        .slice(0, limit)
        .map((item) => ({
            image: item.images || item.commonImage || '',
            price: formatPrice(item.commonPrice ?? item.price),
            name: item.commonName || '',
            label: item.copywriting || item.labelText || '',
            commonId: item.commonId ? String(item.commonId) : '',
            goodsId: item.goodsId ? String(item.goodsId) : '',
            dataType: item.dataType,
            url: item.url || '',
        }))
}

/** 将同一批推荐商品分区，避免多卡片显示同一组导致视觉重叠感 */
const partitionPromoItems = (items: PromoItem[]) => {
    const unique: PromoItem[] = []
    const seen = new Set<string>()
    for (const item of items) {
        const key = String(item.goodsId || item.commonId || item.image || item.name || '')
        if (!key || seen.has(key)) continue
        seen.add(key)
        unique.push(item)
    }
    return {
        subsidy: unique.slice(0, 4),
        seckill: unique.slice(4, 6),
        discount: unique.slice(6, 8),
        live: unique[2] || unique[0] || null,
    }
}

/** 解析直播模块 */
const mapLiveModule = (moduleData?: SlideModuleData | null): PromoItem => {
    const item = (Array.isArray(moduleData?.data) ? moduleData!.data! : [])[0] || {}
    return {
        image: item.images || item.commonImage || '',
        price: '',
        name: item.commonName || '',
        commonId: item.commonId ? String(item.commonId) : '',
        goodsId: item.goodsId ? String(item.goodsId) : '',
        dataType: item.dataType,
        url: item.url || '',
    }
}

interface QuickLinkExt extends QuickLinkItem {
    iconUrl?: string
}

const quickLinks = ref<QuickLinkExt[]>([
    {
        icon: '👣',
        text: '我的浏览',
        url: '/attention/footprint',
        type: 1,
        iconUrl: IMAGE_URL('index-views.png'),
    },
    {
        icon: '🔒',
        text: '账号安全',
        url: '/member/Accountsecurity',
        type: 1,
        iconUrl: IMAGE_URL('index-account.png'),
    },
    {
        icon: '🏪',
        text: '积分商城',
        url: '/points',
        type: 0,
        iconUrl: IMAGE_URL('index-points.png'),
    },
])

const handlePromoClick = (item: PromoItem) => {
    if (!item) return
    // dataType=1 或有商品 id：跳转商品详情
    const productId = item.goodsId || item.commonId
    if (productId && (Number(item.dataType) === 1 || !item.url)) {
        navigateTo(`/product/${productId}`)
        return
    }
    if (item.url) {
        if (/^https?:\/\//i.test(item.url)) {
            window.open(item.url, '_blank', 'noopener,noreferrer')
        } else {
            navigateTo(item.url)
        }
    }
}

const handleQuickLinkClick = (link: QuickLinkItem) => {
    if (link.type === 1) {
        UCENTER_URL(link.url)
    } else {
        navigateTo(link.url)
    }
}

const handleLoginClick = async () => {
    await navigateTo(ucenterAuthUrl('login'), { external: true })
}

const handleRegisterClick = async () => {
    await navigateTo(ucenterAuthUrl('register'), { external: true })
}

const handleUserClick = async () => {
    UCENTER_URL('/home')
}

const handleLogoutClick = async () => {
    await logout()
}

const handlePlusBannerClick = async () => {
    if (!userStore.isLogin) {
        await navigateTo(ucenterAuthUrl('login', '/member/plus'), { external: true })
        return
    }
    const path = plusStore.isPlusMember ? '/member/plusActivate' : '/member/plus'
    await navigateTo(path)
}

const getUserLogo = async () => {
    try {
        const response = await selectByConfigKey('photo_user_logo')
        if (response?.code === 1 && response?.data?.configValue) {
            userLogo.value = response.data.configValue
        }
    } catch (error) {
        console.error('获取用户 logo 失败:', error)
        userLogo.value = ''
    }
}

const getBannerData = async () => {
    try {
        const res = await queryPcIndexSlideModule()
        const data = res?.data || {}

        const list = data.indexSlideData || []
        banners.value = list.filter(
            (item: BannerItem) => Number(item.configEnable) === 1 && !!item.configValue
        )

        let subsidy = mapGoodsModule(data.index_slider_module1, 8)
        let live = mapLiveModule(data.index_slider_module3)
        let seckill = mapGoodsModule(data.index_slider_module4, 8)
        let discount = mapGoodsModule(data.index_slider_module5, 8)

        // 各模块若同源推荐导致商品高度重叠，则合并后重新分区
        const overlapScore = (() => {
            const sets = [seckill, discount].map(
                (arr) => new Set(arr.map((item) => String(item.goodsId || item.commonId || item.image)))
            )
            let hit = 0
            let total = 0
            for (const id of sets[0]) {
                total += 1
                if (sets[1].has(id)) hit += 1
            }
            return total ? hit / total : 1
        })()
        if (overlapScore >= 0.5 || !subsidy.length || !seckill.length || !discount.length) {
            const pool = [...subsidy, ...seckill, ...discount]
            const parts = partitionPromoItems(pool)
            subsidy = parts.subsidy.length ? parts.subsidy : subsidy.slice(0, 4)
            seckill = parts.seckill.length ? parts.seckill : subsidy.slice(0, 2)
            discount = parts.discount.length ? parts.discount : subsidy.slice(2, 4)
            if (!live.image && parts.live) live = { ...parts.live, price: '' }
        }

        subsidyTitle.value = getModuleTitle(data.index_slider_module1, '国家补贴')
        subsidyProducts.value = subsidy.slice(0, 4)
        liveTitle.value = getModuleTitle(data.index_slider_module3, '精选直播')
        liveInfo.value = live
        seckillTitle.value = getModuleTitle(data.index_slider_module4, '限时秒杀')
        seckillProducts.value = seckill.slice(0, 2)
        discountTitle.value = getModuleTitle(data.index_slider_module5, '满减折扣')
        discountProducts.value = discount.slice(0, 2)
    } catch (error) {
        console.error('获取首页轮播区模块失败:', error)
        banners.value = []
        subsidyProducts.value = []
        seckillProducts.value = []
        discountProducts.value = []
        liveInfo.value = { image: '', price: '' }
        liveTitle.value = '精选直播'
        subsidyTitle.value = '国家补贴'
        seckillTitle.value = '限时秒杀'
        discountTitle.value = '满减折扣'
    }
}

const getNewsList = async () => {
    try {
        const [res, homeRes] = await Promise.all([
            getMallIndexData(),
            getMallHomeAggregate().catch(() => null)
        ])
        const modules = flattenMallIndexModules(res)
        const notice = modules.find((item: any) => item?.name === 'notice' && item?.disabled !== 1)
        const decorateRows = notice?.content?.lists || res?.data?.article || []
        let rows = (Array.isArray(decorateRows) ? decorateRows : []).filter((item: any) => item?.title || item?.informationTitle)
        if (!rows.length) {
            const notices = (homeRes as any)?.data?.notices || []
            rows = (Array.isArray(notices) ? notices : []).map((item: any) => ({
                informationTitle: item.title,
                informationId: item.id,
                title: item.title
            }))
        }
        newsList.value = rows.map((item: any) => ({
            ...item,
            informationTitle: item.informationTitle || item.title,
            informationId: item.informationId || item.link?.query?.id || item.link?.id || item.id
        }))
    } catch (error) {
        console.error('获取推荐咨询列表失败:', error)
        newsList.value = []
    }
}

const handleBannerClick = (item: BannerItem) => {
    const routeMap: Record<number, string> = {
        1: `/product/list?kws=${item.opValue}`,
        2: `/product/${item.opValue}`,
        3: item.opValue,
        4: `/store/${item.opValue}`,
        6: `/product/list?catId=${item.opValue}`,
    }
    const route = routeMap[item.opType]
    if (route) {
        navigateTo(route)
    }
}

const handleNewsClick = (news: NewsItem) => {
    if (news.informationId) {
        navigateTo(`/information/${news.informationId}`)
    }
}

onMounted(async () => {
    await Promise.all([getBannerData(), getNewsList(), getUserLogo()])
})
</script>

<style lang="scss" scoped>
.banner-box {
    position: relative;
    background-color: var(--home-page, #f3f5f7);
    margin-bottom: 24px;
    padding-top: 18px;
    z-index: 0;

    .banner-box-bg {
        position: absolute;
        left: 50%;
        top: 0;
        transform: translateX(-50%);
        width: calc(100vw - 10px);
        height: 100%;
        background-image: v-bind(bannerFullBgImage);
        background-repeat: no-repeat;
        background-position: center center;
        background-size: cover;
        z-index: -1;
        pointer-events: none;
    }

    > * {
        position: relative;
        z-index: 1;
    }
}

.banner-content {
    display: flex;
    gap: 14px;
    align-items: stretch;
    background-color: transparent;
    border: 0;
    border-radius: 0;
    padding: 0;
    box-sizing: border-box;
}

.banner-box {
    .left-category {
        width: 240px;
        flex-shrink: 0;
        align-self: stretch;

        :deep(.category-nav) {
            height: 452px;
        }

        :deep(.subcategory) {
            height: 452px;
        }

        :deep(.category-title) {
            padding: 6px 5px;
        }
    }

    .main-stage {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .stage-top {
        display: grid;
        grid-template-columns: 1fr;
        height: 250px;
        flex-shrink: 0;
    }

    .stage-bottom {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 12px;
        height: 190px;
        flex-shrink: 0;
    }

    .subsidy-panel {
        min-width: 0;
        height: 190px;
    }

    .right-sidebar {
        width: 218px;
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        gap: 10px;
    }
}

/* 轮播 */
.banner-container {
    width: 100%;
    height: 250px;
    border: 1px solid var(--home-border, #e2e8ed);
    border-radius: 14px;
    overflow: hidden;
    flex-shrink: 0;
}

.banner-swiper {
    width: 100%;
    height: 250px;
    border-radius: 13px;
    overflow: hidden;

    .carousel-item {
        width: 100%;
        height: 100%;
        cursor: pointer;
        overflow: hidden;
    }

    .carousel-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.5s ease;
    }

    .carousel-item:hover .carousel-image {
        transform: scale(1.03);
    }

    :deep(.swiper-button-prev),
    :deep(.swiper-button-next) {
        color: #fff;
        width: 25px;
        height: 23px;
        margin-top: 0;
        top: 50%;
        transform: translateY(-50%);
        background: rgba(0, 0, 0, 0.34);

        svg {
            width: 10px;
            height: 10px;
        }

        &::after {
            display: none;
        }
    }

    :deep(.swiper-button-prev) {
        left: 0;
        border-radius: 0 12px 12px 0;
    }

    :deep(.swiper-button-next) {
        right: 0;
        border-radius: 12px 0 0 12px;
    }

    :deep(.swiper-pagination) {
        text-align: left;
        padding-left: 12px;
        bottom: 12px;
    }

    :deep(.swiper-pagination-bullet) {
        width: 8px;
        height: 8px;
        background: rgba(255, 255, 255, 0.58);
        opacity: 1;
        border-radius: 50%;
        margin: 0 2.5px !important;

        &.swiper-pagination-bullet-active {
            width: 19px;
            border-radius: 4px;
            background: #fff;
        }
    }
}

.discovery-hero {
    position: relative;
    width: 100%;
    height: 100%;
    display: grid;
    grid-template-columns: 1.15fr 0.85fr;
    overflow: hidden;
    color: #fff;
    background: #d92e39;
}

.discovery-hero--1 { background: #243b55; }
.discovery-hero--2 { background: #aa3f34; }

.discovery-copy {
    position: relative;
    z-index: 2;
    padding: 30px 20px 26px 34px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

.discovery-kicker { font-size: 12px; letter-spacing: 2px; opacity: 0.82; }
.discovery-copy h1 { margin: 10px 0 14px; white-space: pre-line; font-size: 30px; line-height: 1.22; letter-spacing: 0.5px; font-weight: 700; }
.discovery-action { margin-top: auto; padding: 8px 14px; border-radius: 999px; background: #fff; color: #20252a; font-size: 13px; font-weight: 600; }
.discovery-art { position: relative; min-width: 0; }
.discovery-art::before { content: ''; position: absolute; width: 210px; height: 210px; border-radius: 50%; top: 17px; right: 15px; background: rgba(255,255,255,.1); }
.shopping-bag { position: absolute; display: grid; place-items: center; border-radius: 10px 10px 17px 17px; box-shadow: 0 22px 35px rgba(36,14,14,.22); transform: rotate(7deg); }
.shopping-bag::before { content: ''; position: absolute; width: 42%; height: 27px; border: 4px solid currentColor; border-bottom: 0; border-radius: 30px 30px 0 0; top: -22px; }
.shopping-bag--back { width: 112px; height: 135px; right: 24px; bottom: -4px; color: rgba(255,255,255,.75); background: #f1bd72; }
.shopping-bag--front { width: 126px; height: 150px; right: 95px; bottom: -18px; color: #d92e39; background: #fff; transform: rotate(-8deg); }
.shopping-bag span { font-weight: 800; letter-spacing: -1px; }
.discovery-note { position: absolute; right: 22px; top: 25px; padding: 5px 9px; border: 1px solid rgba(255,255,255,.45); border-radius: 99px; font-size: 11px; }

.banner-placeholder {
    width: 100%;
    height: 100%;
    border-radius: calc(var(--home-card-radius, 8px) - 1px);
    background: var(--home-surface-soft, #f7f9fb);
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;

    .placeholder-skeleton {
        position: absolute;
        inset: 0;
        background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.4) 50%, transparent 100%);
        background-size: 200% 100%;
        animation: shimmer 1.5s ease-in-out infinite;
    }

    .placeholder-content {
        position: relative;
        z-index: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
    }

    .placeholder-spinner {
        width: 28px;
        height: 28px;
        border: 3px solid rgba(0, 0, 0, 0.08);
        border-top-color: var(--color, #ff3743);
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
    }

    .placeholder-text {
        font-size: 13px;
        color: #999;
    }
}

@keyframes shimmer {
    0% {
        background-position: 200% 0;
    }
    100% {
        background-position: -200% 0;
    }
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

/* 推广卡片样式已拆至 BannerPromoPanel / BannerLivePanel */

/* 右侧欢迎区 */
.welcome-section {
    background: var(--home-surface, #fff);
    border: 1px solid var(--home-border, #e2e8ed);
    border-radius: 12px;
    padding: 12px;
    box-sizing: border-box;

    .welcome-top {
        display: flex;
        align-items: flex-start;
        gap: 8px;
    }

    .welcome-logo {
        flex-shrink: 0;

        .logo-icon {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            overflow: hidden;
            background: #fff;
            border: 1px solid var(--home-border, #e2e8ed);

            .user-logo {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }
    }

    .welcome-text {
        flex: 1;
        min-width: 0;
        padding-top: 4px;
    }

    .welcome-hi {
        display: flex;
        align-items: center;
        gap: 4px;
        color: var(--home-text, #202a33);
        font-size: 14px;
        line-height: 20px;
        font-weight: 600;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;

        .enterprise-badge {
            background-color: var(--rgba4, #ff3743);
            color: #fff;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 11px;
            flex-shrink: 0;
        }
    }

    .welcome-register {
        display: inline-block;
        margin-top: 2px;
        color: var(--color);
        font-size: 13px;
        line-height: 20px;
        cursor: pointer;

        &:hover {
            color: var(--color-primary-hover, var(--color));
        }
    }

    .login-main-btn {
        display: block;
        width: 100%;
        height: 38px;
        margin-top: 5px;
        border: none;
        border-radius: 7px;
        background: var(--color);
        color: #fff;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: filter 0.2s;

        &:hover {
            filter: brightness(1.05);
        }
    }

    .plus-banner-entry {
        margin: 10px auto 0;
        width: 155px;
        height: 26px;
        border-radius: 13px;
        background: #17324d;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        cursor: pointer;
        box-sizing: border-box;

        .plus-banner-entry-icon {
            display: block;
            flex-shrink: 0;
            width: 16px;
            height: 16px;
            background-repeat: no-repeat;
            background-position: center;
            background-size: 200%;
        }

        .plus-banner-entry-text {
            color: #fff;
            font-size: 12px;
            line-height: 17px;
            white-space: nowrap;
        }

        &:hover {
            filter: brightness(1.08);
        }
    }
}

.visitor-avatar { width: 50px; height: 50px; display: grid; place-items: center; border-radius: 50%; color: var(--color, #e83943); background: color-mix(in srgb, var(--color, #e83943) 10%, white); font-size: 24px; }

.news-section {
    background: var(--home-surface, #fff);
    border: 1px solid var(--home-border, #e2e8ed);
    border-radius: var(--home-card-radius, 8px);
    padding: 10px 12px 12px;
    box-sizing: border-box;

    .section-header {
        margin-bottom: 7px;
        padding-bottom: 7px;
        border-bottom: 1px solid var(--home-border, #e2e8ed);

        h4 {
            margin: 0;
            font-size: 15px;
            font-weight: 600;
            color: var(--home-text, #202a33);
            line-height: 20px;
        }
    }

    .news-list {
        height: 72px;
        overflow: hidden;

        .news-placeholder {
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #999;
            font-size: 12px;
        }

        .news-swiper {
            height: 100%;
            width: 100%;

            .swiper-slide {
                height: 24px;
                display: flex;
                align-items: center;
            }
        }

        .news-item {
            display: flex;
            align-items: center;
            width: 100%;
            height: 100%;
            cursor: pointer;
            gap: 5px;

            &:hover .news-title {
                color: #ff3743;
            }

            .news-number {
                flex-shrink: 0;
                width: 15px;
                height: 15px;
                border-radius: 50%;
                color: #fff;
                font-size: 12px;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                line-height: 1;

                &.rank-1 {
                    background: #ff0000;
                }
                &.rank-2 {
                    background: #ff6767;
                }
                &.rank-3 {
                    background: #ff9d9d;
                }
            }

            .news-title {
                flex: 1;
                min-width: 0;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                font-size: 13px;
                color: var(--home-text-secondary, #66727d);
                line-height: 18px;
            }
        }
    }
}

.quick-links {
    background: var(--home-surface, #fff);
    border: 1px solid var(--home-border, #e2e8ed);
    border-radius: var(--home-card-radius, 8px);
    padding: 10px 12px;
    box-sizing: border-box;
    flex: 1;

    .section-header {
        margin-bottom: 10px;
        padding-bottom: 7px;
        border-bottom: 1px solid var(--home-border, #e2e8ed);

        h4 {
            margin: 0;
            font-size: 15px;
            font-weight: 600;
            color: var(--home-text, #202a33);
            line-height: 20px;
        }
    }

    .quick-links-grid {
        display: flex;
        justify-content: space-between;
        padding: 0 2px;
    }

    .quick-link-item {
        width: 48px;
        display: flex;
        flex-direction: column;
        align-items: center;
        cursor: pointer;
        transition: transform 0.2s;

        &:hover {
            transform: translateY(-2px);
        }

        .quick-link-img {
            width: 36px;
            height: 36px;
            object-fit: contain;
            padding: 4px;
            background: var(--home-surface-soft, #f7f9fb);
            border-radius: 6px;
            box-sizing: border-box;
        }

        .quick-link-icon {
            width: 36px;
            height: 36px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
        }

        .quick-link-text {
            margin-top: 4px;
            font-size: 13px;
            color: var(--home-text, #202a33);
            line-height: 18px;
            text-align: center;
            white-space: nowrap;
        }
    }
}

@media (max-width: 900px) {
    .banner-content {
        flex-wrap: wrap;
        justify-content: center;
    }

    .banner-box {
        .left-category,
        .main-stage,
        .right-sidebar {
            width: 100%;
            max-width: 100%;
        }

        .left-category {
            :deep(.category-nav) {
                height: auto;
                max-height: 320px;
            }
        }

        .stage-top,
        .stage-bottom {
            height: auto;
        }

        .stage-top {
            grid-template-columns: 1fr;
        }

        .stage-bottom {
            grid-template-columns: repeat(2, minmax(0, 1fr));
            min-height: 180px;
        }

        .subsidy-panel {
            height: 200px;
        }
    }

    .banner-container,
    .banner-swiper,
    .banner-placeholder {
        height: 220px;
    }

    .banner-swiper {
        height: 220px;
    }
}

@media (max-width: 768px) {
    .banner-box {
        .stage-bottom {
            grid-template-columns: 1fr;
        }

        .subsidy-panel {
            height: 180px;
        }
    }

    .banner-container,
    .banner-swiper,
    .banner-placeholder {
        height: 180px;
    }
}
</style>
