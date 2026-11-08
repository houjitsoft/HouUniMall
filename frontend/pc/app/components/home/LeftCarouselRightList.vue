<template>
    <div class="left-carousel-right-list-box">
        <div v-for="(block, blockIndex) in normalizedBlocks" :key="`block-${block.floorAdpageId || blockIndex}`"
            class="dual-block">
            <div class="block-header">
                <h2 class="block-title">{{ $t(block.floorAdpageNameNew?.catName || '分类馆') }}</h2>
                <div class="block-more" @click="handleMoreClick(block)">
                    {{ $t('更多') }} >
                </div>
            </div>

            <div class="block-content">
                <div class="left-carousel">
                    <Swiper v-if="block.adpageBannerList.length > 0" :modules="[Autoplay]"
                        :loop="block.adpageBannerList.length > 1"
                        :autoplay="block.adpageBannerList.length > 1 ? { delay: 3500, disableOnInteraction: false } : false"
                        class="goods-swiper">
                        <SwiperSlide v-for="(banner, bannerIndex) in block.adpageBannerList"
                            :key="`banner-${blockIndex}-${banner.goodsId || bannerIndex}`">
                            <div class="banner-item" @click="handleGoodsClick(banner)">
                                <img :src="banner.goodsImage" :alt="banner.goodsName || ''" />
                                <div class="banner-name">{{ $t(banner.goodsName || '') }}</div>
                            </div>
                        </SwiperSlide>
                    </Swiper>
                    <div v-else class="banner-empty-wrap">
                        <EmptyState :text="$t('暂无轮播数据')" compact :icon-size="48" />
                    </div>
                </div>

                <div class="right-goods">
                    <div class="goods-grid">
                        <div v-for="(goods, goodsIndex) in block.rightRecGoodsList.slice(0, 4)"
                            :key="`goods-${blockIndex}-${goods.goodsId || goodsIndex}`" class="goods-card"
                            @click="handleGoodsClick(goods)">
                            <div class="goods-image">
                                <img :src="goods.goodsImage" :alt="goods.goodsName || ''" />
                            </div>
                            <div class="goods-info">
                                <div class="goods-name">{{ $t(goods.goodsName || '') }}</div>
                                <div class="goods-price">
                                    <span class="current-price">{{ formatPriceWithSymbolSafe(goods.goodsPrice) }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PropType } from 'vue'
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Autoplay } from 'swiper/modules'
import 'swiper/css'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'
import EmptyState from '~/components/Empty/EmptyState.vue'

interface FloorNameInfo {
    catId?: number
    catName?: string
}

interface GoodsItem {
    goodsId?: number
    goodsName?: string
    goodsImage?: string
    goodsPrice?: number
    goodsMarketPrice?: number
}

interface LeftCarouselRightListItem {
    floorAdpageId?: number
    floorAdpageNameNew?: FloorNameInfo
    adpageBannerList?: GoodsItem[]
    rightRecGoodsList?: GoodsItem[]
}

const props = defineProps({
    dataInfo: {
        type: Array as PropType<LeftCarouselRightListItem[]>,
        default: () => []
    }
})

const normalizedBlocks = computed(() => {
    return (props.dataInfo || []).map((item) => ({
        ...item,
        adpageBannerList: item.adpageBannerList || [],
        rightRecGoodsList: item.rightRecGoodsList || []
    }))
})

/**
 * 跳转到商品详情页。
 * @param goods 商品对象，优先使用 goodsId 跳转。
 * @returns 无返回值，仅执行页面跳转。
 */
const handleGoodsClick = (goods: GoodsItem) => {
    if (!goods?.goodsId) return
    navigateTo(`/product/${goods.goodsId}`)
}

/**
 * 点击更多，按楼层分类跳转商品列表页。
 * @param block 当前楼层区块数据，包含分类 catId。
 * @returns 无返回值，仅执行页面跳转。
 */
const handleMoreClick = (block: LeftCarouselRightListItem) => {
    const catId = block.floorAdpageNameNew?.catId
    if (!catId) return
    navigateTo(`/product/list?catId=${catId}`)
}

</script>

<style lang="scss" scoped>
.left-carousel-right-list-box {
    width: 100%;
    margin-bottom: 20px;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;

    .dual-block {
        background-color: var(--background, var(--color-background, #fff));
        border-radius: 12px;
        box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
        padding: 18px;
        margin-bottom: 0;
    }

    .block-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .block-title {
            font-size: 20px;
            font-weight: bold;
            color: var(--color);
            margin: 0;
            display: flex;
            align-items: center;
            gap: 10px;

            &::before {
                content: '';
                display: inline-block;
                width: 4px;
                height: 20px;
                border-radius: 2px;
                background-color: var(--color);
            }
        }

        .block-more {
            font-size: 14px;
            color: #666;
            cursor: pointer;
        }
    }

    .block-content {
        display: grid;
        grid-template-columns: 3fr 7fr;
        gap: 14px;
        align-items: stretch;
    }

    .left-carousel {
        width: 100%;
        min-width: 0;
        background-color: color-mix(in srgb, var(--color) 5%, var(--background, var(--color-background, #fff)));
        border-radius: 8px;
        overflow: hidden;
    }

    .goods-swiper {
        width: 100%;
        height: 100%;
        min-height: 200px;
    }

    .banner-item {
        height: 100%;
        display: flex;
        flex-direction: column;
        cursor: pointer;
        background-color: color-mix(in srgb, var(--color) 5%, var(--background, var(--color-background, #fff)));

        img {
            display: block;
            width: 90%;
            max-width: 100%;
            margin: 0 auto;
            aspect-ratio: 1 / 1;
            height: auto;
            object-fit: cover;
            background-color: var(--background, var(--color-background, #fff));
        }

        .banner-name {
            font-size: 14px;
            color: var(--text, var(--color-text, #303133));
            padding: 10px;
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
    }

    .banner-empty-wrap {
        width: 100%;
        min-height: 200px;
        display: flex;
        align-items: stretch;

        :deep(.empty-state) {
            flex: 1;
            min-height: 200px;
            padding: 20px 12px;
        }
    }

    .right-goods {
        min-width: 0;
    }

    .goods-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        grid-template-rows: repeat(2, minmax(0, 1fr));
        gap: 25px 9px;
        align-items: stretch;
    }

    .goods-card {
        display: flex;
        align-items: stretch;
        gap: 10px;
        min-height: 0;
        height: 100%;
        border-radius: 10px;
        padding: 5px 6px;
        cursor: pointer;
        transition: box-shadow 0.2s ease, transform 0.2s ease;

        &:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 16px color-mix(in srgb, var(--color) 12%, transparent);
        }
    }

    .goods-image {
        /* 相对原尺寸缩小约 10%（原 clamp(72px, 28%, 100px)） */
        width: clamp(65px, 25.2%, 90px);
        flex-shrink: 0;
        align-self: center;
        aspect-ratio: 1 / 1;
        border-radius: 8px;
        overflow: hidden;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }

    .goods-info {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        min-width: 0;
        flex: 1;
        gap: 8px;
    }

    .goods-name {
        font-size: 14px;
        color: var(--text, var(--color-text, #303133));
        line-height: 1.45;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }

    .goods-price {
        display: flex;
        align-items: baseline;
        gap: 8px;

        .current-price {
            color: var(--color);
            font-size: 16px;
            font-weight: 700;
        }
    }
}

@media (max-width: 1200px) {
    .left-carousel-right-list-box {
        grid-template-columns: 1fr;
    }

    .block-content {
        grid-template-columns: 1fr;
    }

    .left-carousel {
        max-width: 280px;
        margin: 0 auto;
    }
}
</style>
