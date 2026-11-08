<template>
    <section v-if="tabs.length" class="home-recommend-floor">
        <header class="recommend-floor__head" role="tablist">
            <button
                v-for="(tab, index) in tabs"
                :key="tab.id || index"
                type="button"
                role="tab"
                class="recommend-floor__tab"
                :class="{ active: activeTab === index }"
                :aria-selected="activeTab === index"
                @click="activeTab = index"
            >
                {{ $t(tab.title) }}
            </button>
        </header>

        <!-- 单行卡片，水平滚动 -->
        <div v-if="products.length" class="recommend-floor__list">
            <article
                v-for="(product, index) in products"
                :key="product.goodsId || product.commonId || index"
                class="recommend-goods"
                @click="goGoods(product)"
            >
                <div class="recommend-goods__media">
                    <img
                        v-if="product.commonImage || product.goodsImage"
                        :src="product.commonImage || product.goodsImage"
                        :alt="product.commonName || product.goodsName || ''"
                        loading="lazy"
                    />
                    <span v-else class="recommend-goods__empty">{{ $t('暂无图片') }}</span>
                </div>
                <div class="recommend-goods__body">
                    <h3>{{ $tg(product.commonName || product.goodsName || '') }}</h3>
                    <div class="recommend-goods__price">
                        <span v-if="product.preferentialPrice" class="tag">{{ $t('特惠价') }}</span>
                        <strong>{{ formatPriceWithSymbolSafe(product.commonPrice || product.goodsPrice) }}</strong>
                    </div>
                </div>
            </article>
        </div>
        <div v-else class="recommend-floor__empty">{{ $t('暂无商品') }}</div>
    </section>
</template>

<script setup lang="ts">
import { computed, ref, type PropType } from 'vue'
import type { TabItem } from '~/types/component'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'

const props = defineProps({
    dataInfo: {
        type: Array as PropType<TabItem[]>,
        default: () => []
    }
})

const activeTab = ref(0)

const tabs = computed(() =>
    (props.dataInfo || []).map((item: any, index: number) => ({
        id: item.pageId ?? index,
        title: item.pageTitle || `推荐${index + 1}`
    }))
)

const products = computed(() => {
    const current = props.dataInfo?.[activeTab.value] as any
    const list = current?.goods || []
    return Array.isArray(list) ? list.slice(0, 20) : []
})

const goGoods = (item: any) => {
    const id = item?.goodsId || item?.commonId
    if (id) navigateTo(`/product/${id}`)
}
</script>

<style lang="scss" scoped>
.home-recommend-floor {
    background: #fff;
    border-radius: 0;
    border: 0;
    padding: 16px 16px 20px;
    margin-bottom: 16px;
}

.recommend-floor__head {
    display: flex;
    align-items: center;
    gap: 24px;
    margin-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
}

.recommend-floor__tab {
    position: relative;
    border: 0;
    background: transparent;
    padding: 0 0 12px;
    cursor: pointer;
    font-size: 18px;
    font-weight: 600;
    color: #666;
    line-height: 1.2;
    transition: color 0.2s ease;

    &.active {
        color: #17324d;
    }

    &.active::after {
        content: '';
        position: absolute;
        left: 0;
        right: 0;
        bottom: -1px;
        height: 3px;
        border-radius: 2px 2px 0 0;
        background: var(--color, #e4393c);
    }
}

.recommend-floor__list {
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    gap: 12px;
    overflow-x: auto;
    overflow-y: hidden;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: thin;
}

.recommend-goods {
    flex: 0 0 160px;
    width: 160px;
    display: flex;
    flex-direction: column;
    min-width: 0;
    padding: 10px;
    border-radius: 8px;
    background: #fafafa;
    cursor: pointer;
    transition: background 0.15s ease;
    box-sizing: border-box;

    &:hover {
        background: #f7f9fa;
    }
}

.recommend-goods__media {
    width: 100%;
    aspect-ratio: 1 / 1;
    background: #fff;
    border-radius: 8px;
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

.recommend-goods__empty {
    color: #ccc;
    font-size: 12px;
}

.recommend-goods__body {
    min-width: 0;
    margin-top: 10px;

    h3 {
        margin: 0;
        font-size: 14px;
        font-weight: 500;
        color: #333;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        min-height: 2.8em;
    }
}

.recommend-goods__price {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;

    .tag {
        font-size: 11px;
        color: var(--color, #e4393c);
        background: color-mix(in srgb, var(--color, #e4393c) 10%, #fff);
        border-radius: 4px;
        padding: 1px 4px;
    }

    strong {
        color: var(--color-price, var(--color));
        font-size: 15px;
        font-weight: 700;
    }
}

.recommend-floor__empty {
    min-height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #bbb;
    font-size: 13px;
    background: #fafafa;
    border-radius: 8px;
}

@media (max-width: 640px) {
    .recommend-floor__head {
        gap: 18px;
    }

    .recommend-floor__tab {
        font-size: 16px;
    }

    .recommend-goods {
        flex-basis: 132px;
        width: 132px;
    }
}
</style>
