<template>
    <div class="promo-panel" :class="{ 'is-clickable': !!moreLink }" @click="onPanelClick">
        <div class="promo-panel__head">
            <h3 class="promo-panel__title">{{ title || fallbackTitle }}</h3>
            <span v-if="moreLink" class="promo-panel__more">{{ $t('更多') }}</span>
        </div>
        <div v-if="items.length" class="promo-panel__grid" :class="`cols-${cols}`">
            <button
                v-for="(item, index) in items"
                :key="item.goodsId || item.commonId || index"
                type="button"
                class="promo-panel__item"
                @click.stop="emit('select', item)"
            >
                <span class="promo-panel__thumb">
                    <img v-if="item.image" :src="item.image" :alt="item.name || item.price || ''" loading="lazy" />
                    <span v-else class="promo-panel__thumb-empty">{{ $t('暂无图片') }}</span>
                </span>
                <span v-if="item.price" class="promo-panel__price">¥{{ item.price }}</span>
                <span v-if="item.label" class="promo-panel__label">{{ item.label }}</span>
            </button>
        </div>
        <div v-else class="promo-panel__empty">{{ $t('暂无商品') }}</div>
    </div>
</template>

<script setup lang="ts">
export interface PromoPanelItem {
    image: string
    price: string
    name?: string
    label?: string
    commonId?: string
    goodsId?: string
    url?: string
    dataType?: string | number
}

const props = withDefaults(
    defineProps<{
        title?: string
        fallbackTitle?: string
        items?: PromoPanelItem[]
        cols?: 1 | 2 | 4
        moreLink?: string
    }>(),
    {
        title: '',
        fallbackTitle: '',
        items: () => [],
        cols: 2,
        moreLink: '',
    }
)

const emit = defineEmits<{
    select: [item: PromoPanelItem]
}>()

const onPanelClick = () => {
    if (props.moreLink) navigateTo(props.moreLink)
}
</script>

<style lang="scss" scoped>
.promo-panel {
    min-width: 0;
    height: 100%;
    background: var(--home-surface, #fff);
    border: 1px solid var(--home-border, #e2e8ed);
    border-radius: var(--home-card-radius, 8px);
    padding: 11px 10px 12px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    &.is-clickable {
        cursor: pointer;
    }
}

.promo-panel__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 8px;
    flex-shrink: 0;
}

.promo-panel__title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: var(--home-text, #202a33);
    line-height: 20px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.promo-panel__more {
    flex-shrink: 0;
    font-size: 13px;
    color: var(--home-text-muted, #8b96a0);
}

.promo-panel__grid {
    flex: 1;
    min-height: 0;
    display: grid;
    gap: 6px;
    align-content: center;

    &.cols-1 {
        grid-template-columns: 1fr;
    }

    &.cols-2 {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    &.cols-4 {
        grid-template-columns: repeat(4, minmax(0, 1fr));
    }
}

.promo-panel__item {
    min-width: 0;
    border: 0;
    padding: 3px;
    background: transparent;
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    border-radius: 6px;
    transition: background-color 0.15s ease;

    &:hover {
        background: var(--home-surface-soft, #f7f9fb);
    }
}

.promo-panel__thumb {
    width: 100%;
    aspect-ratio: 1;
    max-height: 88px;
    border-radius: 6px;
    background: var(--home-surface-soft, #f7f9fb);
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

.promo-panel__item:hover .promo-panel__thumb img {
    transform: scale(1.04);
}

.promo-panel__thumb-empty {
    font-size: 11px;
    color: #bbb;
}

.promo-panel__price {
    margin-top: 4px;
    color: #e4393c;
    font-size: 14px;
    font-weight: 600;
    line-height: 18px;
    white-space: nowrap;
}

.promo-panel__label {
    color: #e4393c;
    font-size: 11px;
    line-height: 16px;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.promo-panel__empty {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #bbb;
    font-size: 12px;
    background: #fff;
    border-radius: 6px;
}
</style>
