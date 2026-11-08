<template>
    <section v-if="hasContent" class="home-rank-floor">
        <!-- 新品首发：单品焦点卡 -->
        <article class="rank-card rank-card--hero">
            <header class="rank-card__head">
                <h3>{{ $t(heroTitle) }}</h3>
            </header>
            <button
                v-if="heroGoods"
                type="button"
                class="rank-hero"
                @click="goGoods(heroGoods)"
            >
                <span class="rank-hero__media">
                    <img v-if="heroGoods.commonImage" :src="heroGoods.commonImage" :alt="heroGoods.commonName || ''" loading="lazy" />
                    <span v-else class="rank-media-empty">{{ $t('暂无图片') }}</span>
                </span>
                <span class="rank-hero__name">{{ $tg(heroGoods.commonName || '') }}</span>
                <span class="rank-hero__price">{{ formatPriceWithSymbolSafe(heroGoods.commonPrice) }}</span>
            </button>
            <div v-else class="rank-empty">{{ $t('暂无商品') }}</div>
        </article>

        <!-- 发现好货：左图右文列表 -->
        <article class="rank-card">
            <header class="rank-card__head">
                <h3>{{ $t(findTitle) }}</h3>
            </header>
            <div v-if="findGoods.length" class="rank-list">
                <button
                    v-for="item in findGoods"
                    :key="item.goodsId || item.commonId"
                    type="button"
                    class="rank-row"
                    @click="goGoods(item)"
                >
                    <span class="rank-row__thumb">
                        <img v-if="item.commonImage" :src="item.commonImage" :alt="item.commonName || ''" loading="lazy" />
                        <span v-else class="rank-media-empty">{{ $t('暂无') }}</span>
                    </span>
                    <span class="rank-row__body">
                        <span class="rank-row__title">{{ $tg(item.commonName || '') }}</span>
                        <span class="rank-row__price">{{ formatPriceWithSymbolSafe(item.commonPrice) }}</span>
                    </span>
                </button>
            </div>
            <div v-else class="rank-empty">{{ $t('暂无商品') }}</div>
        </article>

        <!-- 精选好店 -->
        <article class="rank-card">
            <header class="rank-card__head">
                <h3>{{ $t(shopTitle) }}</h3>
            </header>
            <div v-if="shops.length" class="rank-list">
                <button
                    v-for="shop in shops"
                    :key="shop.shopId"
                    type="button"
                    class="rank-row rank-row--shop"
                    @click="goShop(shop)"
                >
                    <span class="rank-row__body">
                        <span class="rank-row__title">{{ $t(shop.shopName || '') }}</span>
                        <span class="rank-row__meta">{{ shop.shopCollect || 0 }} {{ $t('人关注') }}</span>
                    </span>
                    <span class="rank-row__logo">
                        <img v-if="shop.commonImage" :src="shop.commonImage" :alt="shop.shopName || ''" loading="lazy" />
                        <span v-else class="rank-media-empty">{{ $t('店') }}</span>
                    </span>
                </button>
            </div>
            <div v-else class="rank-empty">{{ $t('暂无店铺') }}</div>
        </article>

        <!-- 推荐榜单 -->
        <article class="rank-card">
            <header class="rank-card__head">
                <h3>{{ $t(rankTitle) }}</h3>
            </header>
            <div v-if="rankGoods.length" class="rank-list">
                <button
                    v-for="item in rankGoods"
                    :key="item.goodsId || item.commonId"
                    type="button"
                    class="rank-row"
                    @click="goGoods(item)"
                >
                    <span class="rank-row__thumb">
                        <img v-if="item.commonImage" :src="item.commonImage" :alt="item.commonName || ''" loading="lazy" />
                        <span v-else class="rank-media-empty">{{ $t('暂无') }}</span>
                    </span>
                    <span class="rank-row__body">
                        <span class="rank-row__title">{{ $tg(item.commonName || '') }}</span>
                        <span class="rank-row__meta">
                            <em>{{ item.commonSalenum || 0 }} {{ $t('件') }}</em>
                            <em>{{ item.commonCollect || 0 }} {{ $t('人收藏') }}</em>
                        </span>
                    </span>
                </button>
            </div>
            <div v-else class="rank-empty">{{ $t('暂无商品') }}</div>
        </article>
    </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'

const props = defineProps<{
    dataInfo?: any[]
}>()

const section = (index: number) => props.dataInfo?.[index] || {}

const heroTitle = computed(() => section(0).pageTitle || '新品首发')
const findTitle = computed(() => section(1).pageTitle || '发现好货')
const shopTitle = computed(() => section(2).pageTitle || '精选好店')
const rankTitle = computed(() => section(3).pageTitle || '推荐榜单')

const heroGoods = computed(() => (section(0).goods || [])[0] || null)
const findGoods = computed(() => (section(1).goods || []).slice(0, 3))
const shops = computed(() => (section(2).shops || []).slice(0, 3))
const rankGoods = computed(() => (section(3).goods || []).slice(0, 3))

const hasContent = computed(
    () => !!(heroGoods.value || findGoods.value.length || shops.value.length || rankGoods.value.length)
)

const goGoods = (item: any) => {
    const id = item?.goodsId || item?.commonId
    if (id) navigateTo(`/product/${id}`)
}

const goShop = (item: any) => {
    if (item?.shopId) navigateTo(`/store/${item.shopId}`)
}
</script>

<style lang="scss" scoped>
.home-rank-floor {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
    margin-bottom: 18px;
}

.rank-card {
    min-width: 0;
    background: #fff;
    border-radius: 0;
    border: 0;
    padding: 14px 12px 12px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    min-height: 320px;
    overflow: hidden;
}

.rank-card__head {
    flex-shrink: 0;
    margin-bottom: 12px;

    h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 700;
    color: #17324d;
        line-height: 22px;
    }
}

.rank-hero {
    flex: 1;
    min-height: 0;
    border: 0;
    padding: 0;
    background: transparent;
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    text-align: center;
}

.rank-hero__media {
    width: 100%;
    flex: 1;
    min-height: 180px;
    border-radius: 0;
    background: #f7f8fa;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    img {
        width: 100%;
        height: 100%;
        object-fit: contain;
        transition: transform 0.25s ease;
    }
}

.rank-hero:hover .rank-hero__media img {
    transform: scale(1.03);
}

.rank-hero__name {
    margin-top: 10px;
    width: 100%;
    font-size: 13px;
    color: #333;
    line-height: 18px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.rank-hero__price {
    margin-top: 4px;
    color: #e4393c;
    font-size: 16px;
    font-weight: 700;
}

.rank-list {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    gap: 10px;
    overflow: visible;
}

.rank-row {
    flex: 0 0 auto;
    min-height: 80px;
    border: 0;
    padding: 8px;
    border-radius: 0;
    background: #f7f8fa;
    display: grid;
    grid-template-columns: 64px minmax(0, 1fr);
    gap: 10px;
    align-items: center;
    cursor: pointer;
    text-align: left;

    &--shop {
        grid-template-columns: minmax(0, 1fr) 56px;
    }
}

.rank-row__thumb,
.rank-row__logo {
    width: 64px;
    height: 64px;
    border-radius: 0;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    img {
        width: 100%;
        height: 100%;
        object-fit: contain;
    }
}

.rank-row__logo {
    width: 56px;
    height: 56px;
    border-radius: 50%;
}

.rank-row__body {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.rank-row__title {
    font-size: 13px;
    color: #222;
    line-height: 18px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.rank-row__price {
    color: #e4393c;
    font-size: 14px;
    font-weight: 700;
}

.rank-row__meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    font-size: 12px;
    color: #999;
    font-style: normal;

    em {
        font-style: normal;
    }
}

.rank-media-empty,
.rank-empty {
    color: #bbb;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.rank-empty {
    flex: 1;
    background: #fafafa;
    border-radius: 8px;
}

@media (max-width: 1024px) {
    .home-rank-floor {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

@media (max-width: 640px) {
    .home-rank-floor {
        grid-template-columns: 1fr;
    }
}
</style>
