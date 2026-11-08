<template>
    <div class="goods-grid" :class="`goods-grid--${viewMode}`">
        <GoodsCard v-for="goods in goodsList" :key="goods.goodsId || 0" :goods="goods" :mode="viewMode"
            @click="handleGoodsClick" />
    </div>
</template>

<script setup lang="ts">
import GoodsCard from './GoodsCard.vue'
import type { ArticleVO } from '~/types/goods'

/**
 * 组件 Props
 */
interface Props {
    goodsList: ArticleVO[]
    /** 展示模式：grid 大图卡片（默认），list 横向列表 */
    viewMode?: 'grid' | 'list'
}

withDefaults(defineProps<Props>(), {
    viewMode: 'grid'
})

/**
 * 组件事件
 */
const emit = defineEmits<{
    goodsClick: [goods: ArticleVO]
}>()

/**
 * 处理商品点击
 */
const handleGoodsClick = (goods: ArticleVO) => {
    emit('goodsClick', goods)
}

</script>

<style lang="scss" scoped>
.goods-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
    margin-bottom: 24px;
    width: 100%;

    @media (max-width: 1400px) {
        grid-template-columns: repeat(4, 1fr);
        gap: 14px;
    }

    @media (max-width: 1200px) {
        grid-template-columns: repeat(3, 1fr);
        gap: 12px;
    }

    @media (max-width: 968px) {
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;
    }

    @media (max-width: 640px) {
        grid-template-columns: repeat(2, 1fr);
        gap: 8px;
    }

    // 列表模式：单列横向行
    &--list {
        display: flex;
        flex-direction: column;
        grid-template-columns: none;
        gap: 16px;

        @media (max-width: 768px) {
            gap: 12px;
        }
    }
}
</style>