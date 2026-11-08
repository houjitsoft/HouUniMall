<template>
    <div class="sort-view-bar">
        <div class="sort-view-bar__container">
            <!-- 排序标签 -->
            <div class="sort-view-bar__tabs">
                <button v-for="tab in sortTabs" :key="tab.value" class="sort-view-bar__tab" :class="{
                    'is-active': currentSort === tab.value,
                    'is-desc': getSortDirection(tab.value) === 1,
                    'is-asc': getSortDirection(tab.value) === 2
                }" @click="handleSortClick(tab.value)">
                    <span class="sort-view-bar__tab-label">{{ tab.label }}</span>
                    <span v-if="currentSort === tab.value && getSortDirection(tab.value)" class="sort-view-bar__arrow">
                        {{ getSortDirection(tab.value) === 1 ? '↓' : '↑' }}
                    </span>
                </button>
            </div>

            <!-- 视图控制和商品数量 -->
            <div class="sort-view-bar__controls">
                <!-- 视图切换 -->
                <div class="sort-view-bar__view-switch">
                    <button class="sort-view-bar__view-btn" :class="{ 'is-active': viewMode === 'grid' }"
                        @click="handleViewChange('grid')" title="卡片模式" aria-label="切换到卡片视图">
                        <svg class="sort-view-bar__view-icon" width="18" height="18" viewBox="0 0 20 20" fill="none">
                            <rect x="2" y="2" width="6" height="6" stroke="currentColor" stroke-width="1.5"
                                fill="none" />
                            <rect x="12" y="2" width="6" height="6" stroke="currentColor" stroke-width="1.5"
                                fill="none" />
                            <rect x="2" y="12" width="6" height="6" stroke="currentColor" stroke-width="1.5"
                                fill="none" />
                            <rect x="12" y="12" width="6" height="6" stroke="currentColor" stroke-width="1.5"
                                fill="none" />
                        </svg>
                    </button>
                    <button class="sort-view-bar__view-btn" :class="{ 'is-active': viewMode === 'list' }"
                        @click="handleViewChange('list')" title="列表模式" aria-label="切换到列表视图">
                        <svg class="sort-view-bar__view-icon" width="18" height="18" viewBox="0 0 20 20" fill="none">
                            <rect x="2" y="3" width="4" height="4" rx="1" stroke="currentColor" stroke-width="1.5"
                                fill="none" />
                            <rect x="9" y="4" width="9" height="2" rx="1" fill="currentColor" />
                            <rect x="2" y="13" width="4" height="4" rx="1" stroke="currentColor" stroke-width="1.5"
                                fill="none" />
                            <rect x="9" y="14" width="9" height="2" rx="1" fill="currentColor" />
                        </svg>
                    </button>
                </div>

                <!-- 商品数量 -->
                <div class="sort-view-bar__count">
                    {{ $t('共') }} <span class="sort-view-bar__count-number">{{ totalCount }}</span> {{ $t('个商品') }}
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 组件 Props
 */
interface Props {
    currentSort: number
    viewMode: 'grid' | 'list'
    totalCount: number
    priceOrder?: number // 1降序，2升序
    saleNum?: number // 1降序，2升序
    commentOrder?: number // 1降序，2升序
}

const props = defineProps<Props>()

/**
 * 组件事件
 */
const emit = defineEmits<{
    sortChange: [orderType: number]
    viewChange: [mode: 'grid' | 'list']
}>()

/**
 * 排序选项
 */
const sortTabs = [
    { label: '综合', value: 3 },
    { label: '评价', value: 5 },
    { label: '价格', value: 1 },
    { label: '销量', value: 2 }
]

/**
 * 获取排序方向
 * @param orderType 排序类型
 * @returns 1降序，2升序，undefined无方向
 */
const getSortDirection = (orderType: number): number | undefined => {
    if (orderType === 1) {
        return props.priceOrder
    } else if (orderType === 2) {
        return props.saleNum
    } else if (orderType === 5) {
        return props.commentOrder
    }
    return undefined
}

/**
 * 处理排序点击
 * @param orderType 排序类型
 */
const handleSortClick = (orderType: number): void => {
    emit('sortChange', orderType)
}

/**
 * 处理视图切换
 * @param mode 视图模式
 */
const handleViewChange = (mode: 'grid' | 'list'): void => {
    emit('viewChange', mode)
}
</script>

<style lang="scss" scoped>
.sort-view-bar {
    width: 100%;
    margin-top: 16px;
    margin-bottom: 16px;
    background-color: #fafafa;
    border-radius: 8px;
    box-sizing: border-box;

    &__container {
        display: flex;
        justify-content: space-between;
        align-items: center;
        min-height: 48px;
        padding: 0 16px;
        gap: 16px;
    }

    // 排序标签区域
    &__tabs {
        display: flex;
        align-items: center;
        gap: 0;
        flex: 1;
        overflow-x: auto;

        &::-webkit-scrollbar {
            display: none;
        }

        scrollbar-width: none;
    }

    // 排序标签
    &__tab {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        padding: 0 20px;
        height: 48px;
        min-width: 60px;
        color: #666;
        font-size: 13px;
        font-weight: 400;
        background-color: transparent;
        border: none;
        border-radius: 0;
        cursor: pointer;
        transition: all 0.2s ease;
        position: relative;
        white-space: nowrap;
        user-select: none;

        &-label {
            line-height: 1;
        }

        &:hover {
            color: #333;
            background-color: rgba(0, 0, 0, 0.02);
        }

        &.is-active {
            color: var(--color);
            font-weight: 600;
            background-color: transparent;

            &::after {
                content: '';
                position: absolute;
                bottom: 0;
                left: 20%;
                right: 20%;
                height: 3px;
                background-color: var(--color);
                border-radius: 2px 2px 0 0;
            }
        }

        // 排序箭头
        &__arrow {
            font-size: 14px;
            color: var(--color);
            font-weight: 600;
            line-height: 1;
            margin-left: 2px;
        }
    }

    // 视图控制和商品数量
    &__controls {
        display: flex;
        align-items: center;
        gap: 16px;
        flex-shrink: 0;
    }

    // 视图切换
    &__view-switch {
        display: flex;
        align-items: center;
        background-color: #fff;
        border: 1px solid #e5e5e5;
        border-radius: 6px;
        overflow: hidden;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    }

    // 视图切换按钮
    &__view-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        padding: 0;
        color: #666;
        background-color: transparent;
        border: none;
        border-right: 1px solid #e5e5e5;
        cursor: pointer;
        transition: all 0.2s ease;
        user-select: none;

        &:last-child {
            border-right: none;
        }

        &:hover {
            color: #333;
            background-color: #f8f8f8;
        }

        &.is-active {
            color: var(--color);
            background-color: color-mix(in srgb, var(--color) 5%, white);
            font-weight: 500;
        }

        &:active {
            transform: scale(0.95);
        }
    }

    // 视图图标
    &__view-icon {
        width: 18px;
        height: 18px;
        transition: all 0.2s ease;
        flex-shrink: 0;
    }

    // 商品数量
    &__count {
        font-size: 13px;
        color: #666;
        white-space: nowrap;
        font-weight: 400;

        &-number {
            color: var(--color);
            font-weight: 600;
            margin: 0 2px;
        }
    }

    // 响应式设计
    @media (max-width: 768px) {
        margin-top: 12px;
        margin-bottom: 12px;

        &__container {
            flex-wrap: wrap;
            padding: 0 12px;
            gap: 12px;
            min-height: 44px;
        }

        &__tabs {
            flex-wrap: wrap;
            gap: 4px;
        }

        &__tab {
            padding: 0 16px;
            height: 40px;
            font-size: 12px;
            min-width: 50px;

            &.is-active::after {
                left: 15%;
                right: 15%;
                height: 2px;
            }
        }

        &__controls {
            gap: 12px;
        }

        &__view-btn {
            width: 32px;
            height: 32px;
        }

        &__view-icon {
            width: 16px;
            height: 16px;
        }

        &__count {
            font-size: 12px;
        }
    }

    @media (max-width: 480px) {
        margin-top: 8px;
        margin-bottom: 8px;

        &__container {
            padding: 0 10px;
            gap: 10px;
        }

        &__tab {
            padding: 0 12px;
            height: 36px;
            font-size: 11px;
            min-width: 45px;
        }

        &__view-btn {
            width: 28px;
            height: 28px;
        }

        &__view-icon {
            width: 14px;
            height: 14px;
        }
    }
}
</style>