<template>
    <div class="feature-page">
        <div class="feature-container">
            <!-- 轮播图区域（独立于精品特色馆） -->
            <div class="carousel-wrap" v-if="slidesList.length > 0">
                <Swiper :modules="swiperModules" :slides-per-view="1" :space-between="16"
                    :loop="slidesList.length > 1"
                    :autoplay="slidesList.length > 1 ? { delay: 4000, disableOnInteraction: false } : false"
                    :pagination="{ clickable: true }" class="premium-carousel">
                    <SwiperSlide v-for="(slide, index) in slidesList" :key="index">
                        <div class="carousel-item" @click="handleSlideClick(slide)">
                            <img :src="slide.configValue" :alt="slide.configComment || `轮播图${index + 1}`"
                                class="carousel-image" />
                        </div>
                    </SwiperSlide>
                </Swiper>
            </div>

            <!-- 精品特色馆：recommendHall 数组横向排列，每项上方为标题 logo，向下渲染商品 -->
            <section class="section section--premium" v-if="recommendHallList.length > 0">
                <h2 class="section-title">{{ $t('精品特色馆') }}</h2>
                <div class="recommend-hall-row">
                    <div class="hall-column" v-for="hall in recommendHallList" :key="hall.exhibitionHallId">
                        <div class="hall-logo" v-if="hall.exhibitionHallLogo" @click="handleHallLogoClick(hall)">
                            <img :src="hall.exhibitionHallLogo" :alt="$t('馆标题')" @error="handleImageError" />
                        </div>
                        <div class="hall-goods">
                            <div class="product-card product-card--image-only"
                                v-for="item in (hall.goodsList || []).slice(0, 2)" :key="item.goodsId"
                                @click="handleProductClick(item)">
                                <div class="product-image">
                                    <img :src="item.goodsImage" :alt="item.goodsName" @error="handleImageError" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- 地方特色馆列表 -->
            <section class="section section--local" v-if="allHallList.length > 0">
                <h2 class="section-title">{{ $t('地方特色馆') }}</h2>
                <div class="local-grid">
                    <div class="local-card" v-for="hall in allHallList" :key="hall.exhibitionHallId"
                        @click="handleHallClick(hall)">
                        <div class="local-image">
                            <img :src="hall.exhibitionHallImage" :alt="hall.exhibitionHallName || '特色馆'"
                                @error="handleImageError" />
                        </div>
                        <div class="local-name" v-if="hall.exhibitionHallName">{{ $t(hall.exhibitionHallName || '') }}</div>
                    </div>
                </div>
            </section>

            <!-- 地方特色馆详情（页面内直接展示） -->
            <section ref="hallDetailRef" class="section section--hall-detail" v-if="hallDetail || hallDetailLoading">
                <template v-if="hallDetailLoading">
                    <Loading compact :text="$t('加载中...')" />
                </template>
                <template v-else-if="hallDetail">
                    <h3 class="hall-detail-title">{{ $t(hallDetail.exhibitionHallName || '特色馆') }}</h3>
                    <div class="hall-detail-desc">
                        <img v-if="hallDetail.exhibitionHallDesc1" :src="hallDetail.exhibitionHallDesc1" alt="描述1"
                            @error="handleImageError" />
                        <img v-if="hallDetail.exhibitionHallDesc2" :src="hallDetail.exhibitionHallDesc2" alt="描述2"
                            @error="handleImageError" />
                        <img v-if="hallDetail.exhibitionHallDesc3" :src="hallDetail.exhibitionHallDesc3" alt="描述3"
                            @error="handleImageError" />
                    </div>
                    <div class="hall-detail-goods" v-if="hallDetail.commonList?.length">
                        <div class="hall-detail-goods-title">{{ $t('商品列表') }}</div>
                        <div class="hall-detail-goods-grid">
                            <div class="hall-detail-product" v-for="item in hallDetail.commonList" :key="item.commonId"
                                @click="handleProductClick(item)">
                                <div class="hall-detail-product-img">
                                    <img :src="item.commonImage" :alt="item.commonName" @error="handleImageError" />
                                </div>
                                <div class="hall-detail-product-name">{{ $t(item.commonName || '') }}</div>
                                <div class="hall-detail-product-price">{{ formatPriceWithSymbolSafe(item.commonPrice) }}</div>
                                <div class="hall-detail-product-meta">{{ $t('销量') }} {{ item.commonSalenum || 0 }}{{ $t('件') }}</div>
                            </div>
                        </div>
                    </div>
                </template>
            </section>

            <!-- 加载中 -->
            <Loading v-if="loading" :text="$t('加载中...')" />

            <!-- 空状态 -->
            <EmptyState v-if="isEmpty" :text="$t('暂无特色馆数据')" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Autoplay, Pagination } from 'swiper/modules'
import 'swiper/css'
import 'swiper/css/pagination'
import { useFormatPrice } from '~/composables/useFormatPrice'
import { getExhibitionIndex } from '~/services/goods.service'
import { getExhibitionHallDetail } from '~/services/goods.service'
import type { ExhibitionSlideItem, AllHallItem, RecommendHall, HallDetailData } from '~/types/mall'

const swiperModules = [Autoplay, Pagination]

const loading = ref(true)
const hallDetailLoading = ref(false)
const hallDetail = ref<HallDetailData | null>(null)
const hallDetailRef = ref<HTMLElement | null>(null)
const exhibitionData = ref<{
    slides: ExhibitionSlideItem[] | ExhibitionSlideItem
    recommendHall: RecommendHall[] | RecommendHall | null
    allHall: AllHallItem | AllHallItem[] | null
} | null>(null)

const toArray = <T>(raw: T | T[] | null | undefined): T[] => {
    if (!raw) return []
    return Array.isArray(raw) ? raw : [raw]
}

const slidesList = computed(() => toArray(exhibitionData.value?.slides))
const recommendHallList = computed(() => toArray(exhibitionData.value?.recommendHall))
const allHallList = computed(() => toArray(exhibitionData.value?.allHall))
const isEmpty = computed(() =>
    !loading.value && !hallDetail.value && !slidesList.value.length && !recommendHallList.value.length && !allHallList.value.length
)

const { formatPrice } = useFormatPrice()

const fetchData = async () => {
    loading.value = true
    try {
        const res = await getExhibitionIndex()
        if (res?.code === 1 && res?.data) {
            exhibitionData.value = res.data as any
        }
    } catch (e) {
        console.error('特色馆数据请求失败:', e)
    } finally {
        loading.value = false
    }
}

const SLIDE_OP_ROUTES: Record<number, (v: string) => string> = {
    1: (v) => `/product/list?kws=${encodeURIComponent(v)}`,
    2: (v) => `/product/${v}`,
    6: (v) => `/product/list?catId=${v}`
}

const handleSlideClick = (slide: ExhibitionSlideItem) => {
    if (!slide.opValue) return
    const route = SLIDE_OP_ROUTES[Number(slide.opType)]
    if (route) navigateTo(route(slide.opValue))
}

const handleProductClick = (item: { goodsId: number }) => {
    navigateTo(`/product/${item.goodsId}`)
}

const fetchHallDetail = async (exhibitionHallId: number) => {
    if (hallDetailLoading.value) return
    hallDetailLoading.value = true
    hallDetail.value = null
    nextTick(() => hallDetailRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' }))
    try {
        const res = await getExhibitionHallDetail(exhibitionHallId)
        if (res?.code === 1 && res?.data) hallDetail.value = res.data
    } catch (e) {
        console.error('特色馆详情请求失败:', e)
    } finally {
        hallDetailLoading.value = false
    }
}

const handleHallLogoClick = (hall: { exhibitionHallId: number }) => {
    if (hall.exhibitionHallId) fetchHallDetail(hall.exhibitionHallId)
}

const handleHallClick = (hall: AllHallItem) => {
    if (hall.exhibitionHallId) fetchHallDetail(hall.exhibitionHallId)
}

const IMG_PLACEHOLDER = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="%23ccc" stroke-width="1.5"%3E%3Crect x="3" y="3" width="18" height="18" rx="2"/%3E%3Cpath d="M9 9h6v6H9z"/%3E%3C/svg%3E'
const handleImageError = (e: Event) => {
    const el = e.target as HTMLImageElement
    if (el) el.src = IMG_PLACEHOLDER
}

onMounted(() => {
    fetchData()
})
</script>

<style lang="scss" scoped>
.feature-page {
    min-height: 60vh;
    background: #f5f5f5;
}

.feature-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 0 24px;
}

.section {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    padding: 24px;
    margin-bottom: 24px;

    .section-title {
        font-size: 20px;
        font-weight: 700;
        color: var(--color, #333);
        margin: 0 0 20px 0;
        padding-bottom: 12px;
        border-bottom: 2px solid var(--color, #333);
    }
}

.carousel-wrap {
    margin-bottom: 24px;
    border-radius: 8px;
    overflow: hidden;
}

.premium-carousel {
    width: 100%;

    :deep(.swiper-wrapper) {
        align-items: stretch;
    }

    :deep(.swiper-slide) {
        height: auto;
    }

    .carousel-item {
        position: relative;
        width: 100%;
        aspect-ratio: 16/9;
        overflow: hidden;
        cursor: pointer;
        border-radius: 8px;
    }

    .carousel-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.35s ease;
    }

    .carousel-item:hover .carousel-image {
        transform: scale(1.03);
    }

    :deep(.swiper-pagination-bullet) {
        width: 8px;
        height: 8px;
        background: rgba(0, 0, 0, 0.2);
        opacity: 1;

        &.swiper-pagination-bullet-active {
            background: var(--color, #1890ff);
            width: 20px;
            border-radius: 4px;
        }
    }
}

/* 精品特色馆：每排 3 个 exhibitionHallLogo，每个 logo 下每排 2 个商品（仅图片） */
.recommend-hall-row {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
}

.hall-column {
    display: flex;
    flex-direction: column;
    background: #fafafa;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #eee;
}

/* logo 宽度 = 下方两个商品的总宽度，高度固定 180px */
.hall-logo {
    width: 100%;
    height: 180px;
    padding: 12px 16px;
    background: #fff;
    text-align: center;
    border-bottom: 1px solid #eee;
    box-sizing: border-box;
    display: flex;
    cursor: pointer;
    align-items: center;
    justify-content: center;

    img {
        width: 100%;
        height: 100%;
        object-fit: fill;
    }
}

.hall-goods {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    padding: 12px 16px;
    box-sizing: border-box;
}

.product-card {
    background: #fff;
    border: 1px solid #eee;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: box-shadow 0.25s ease, border-color 0.25s ease;

    &:hover {
        border-color: var(--color, #1890ff);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    &.product-card--image-only {
        .product-image {
            aspect-ratio: 1;
        }
    }
}

.product-image {
    width: 100%;
    aspect-ratio: 1;
    background: #fafafa;
    overflow: hidden;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}

/* 地方特色馆 */
.local-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
}

.local-card {
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    border: 1px solid #eee;
    transition: box-shadow 0.25s ease, border-color 0.25s ease;

    &:hover {
        border-color: var(--color, #1890ff);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
}

.local-image {
    width: 100%;
    aspect-ratio: 16/10;
    background: #fafafa;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}

.local-name {
    padding: 12px;
    font-size: 15px;
    font-weight: 600;
    color: #333;
    text-align: center;
}

/* 地方特色馆详情（页面内展示） */
.section--hall-detail {
    position: relative;
}

.hall-detail-title {
    margin: 0 0 20px 0;
    font-size: 22px;
    font-weight: 700;
    color: #333;
}

.hall-detail-desc {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 24px;

    img {
        width: 100%;
        height: auto;
        display: block;
        border-radius: 8px;
        object-fit: cover;
    }
}

.hall-detail-goods-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
}

.hall-detail-goods-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
}

.hall-detail-product {
    cursor: pointer;
    border: 1px solid #eee;
    border-radius: 8px;
    overflow: hidden;
    transition: border-color 0.2s, box-shadow 0.2s;

    &:hover {
        border-color: var(--color, #1890ff);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
}

.hall-detail-product-img {
    width: 100%;
    aspect-ratio: 1;
    background: #fafafa;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}

.hall-detail-product-name {
    padding: 8px 10px 0;
    font-size: 14px;
    color: #333;
    line-height: 1.4;
    height: 10%;
    min-height: 3.08em;
    /* 3.02em 再提高 2% */
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}

.hall-detail-product-price {
    padding: 6px 10px 0;
    font-size: 16px;
    font-weight: 600;
    color: var(--color, #e4393c);
}

.hall-detail-product-meta {
    padding: 4px 10px 10px;
    font-size: 12px;
    color: #999;
}

@media (max-width: 992px) {
    .recommend-hall-row {
        grid-template-columns: repeat(2, 1fr);
    }

    .premium-carousel {
        :deep(.swiper-slide) {
            width: 66.66% !important;
        }
    }
}

@media (max-width: 768px) {
    .recommend-hall-row {
        grid-template-columns: 1fr;
    }

    .local-grid {
        grid-template-columns: 1fr;
    }

    .premium-carousel {
        :deep(.swiper-slide) {
            width: 100% !important;
        }
    }

    .hall-detail-goods-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}
</style>
