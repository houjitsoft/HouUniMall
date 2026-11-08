<template>
    <div v-if="hasSelectedFilters" class="nav-filter-bar">
        <div class="nav-filter-bar__container">
            <!-- 左侧标签 -->
            <div class="nav-filter-bar__label">{{ $t('全部结果') }}:</div>

            <!-- 右侧筛选内容 -->
            <div class="nav-filter-bar__content">
                <!-- 分类导航 -->
                <template v-if="navCategories.length > 0">
                    <ul class="nav-filter-bar__list">
                        <li v-for="cat in navCategories" :key="cat.catId" class="nav-filter-bar__item"
                            :class="{ 'has-children': cat.secordNameList && cat.secordNameList.length > 0 }">
                            <div class="nav-filter-bar__item-content">
                                <span class="nav-filter-bar__item-text">{{ cat.catName }}</span>
                                <UIcon v-if="cat.secordNameList && cat.secordNameList.length > 0"
                                    name="i-heroicons-chevron-right" class="nav-filter-bar__icon-arrow" />
                                <button class="nav-filter-bar__btn-close"
                                    @click="cat.catId && handleRemoveCategory(cat.catId, cat.catParentId)"
                                    aria-label="{{ $t('移除分类筛选') }}">
                                    <UIcon name="i-heroicons-x-mark" />
                                </button>
                            </div>
                            <div v-if="cat.secordNameList && cat.secordNameList.length > 0"
                                class="nav-filter-bar__dropdown">
                                <ul class="nav-filter-bar__dropdown-list">
                                    <li v-for="subCat in cat.secordNameList" :key="subCat.catId"
                                        class="nav-filter-bar__dropdown-item"
                                        :class="{ 'is-active': subCat.catId && selectedCatIds.includes(subCat.catId) }"
                                        @click="subCat.catId && handleSelectCategory(subCat.catId)">
                                        {{ $t(subCat.catName || '') }}
                                    </li>
                                </ul>
                            </div>
                        </li>
                    </ul>
                </template>

                <!-- 品牌导航 -->
                <template v-if="navBrands.length > 0">
                    <ul class="nav-filter-bar__list">
                        <li v-if="navCategories.length > 0" class="nav-filter-bar__separator">
                            <UIcon name="i-heroicons-chevron-right" class="nav-filter-bar__icon-separator" />
                        </li>
                        <li v-if="navBrands.length === 1 && navBrands[0]" class="nav-filter-bar__item">
                            <span class="nav-filter-bar__item-label">{{ $t('品牌') }}:</span>
                            <span class="nav-filter-bar__item-value">{{ $t(navBrands[0]?.brandName || '') || ''
                                }}</span>
                            <button class="nav-filter-bar__btn-close" @click="handleRemoveBrand"
                                aria-label="{{ $t('移除品牌筛选') }}">
                                <UIcon name="i-heroicons-x-mark" />
                            </button>
                        </li>
                        <li v-else class="nav-filter-bar__item has-children">
                            <div class="nav-filter-bar__item-content">
                                <span class="nav-filter-bar__item-text">{{ $t('品牌') }}:{{ $t('多选') }}:</span>
                                <button class="nav-filter-bar__btn-close" @click="handleRemoveBrand"
                                    aria-label="移除所有品牌筛选">
                                    <UIcon name="i-heroicons-x-mark" />
                                </button>
                            </div>
                            <div class="nav-filter-bar__dropdown">
                                <ul class="nav-filter-bar__dropdown-list">
                                    <li v-for="brand in navBrands" :key="brand.brandId || 0"
                                        class="nav-filter-bar__dropdown-item"
                                        @click="brand.brandId && handleRemoveBrandFromFilter(brand.brandId)">
                                        {{ $t(brand.brandName || '') }}
                                    </li>
                                </ul>
                            </div>
                        </li>
                        <li v-if="keyword" class="nav-filter-bar__separator">
                            <UIcon name="i-heroicons-chevron-right" class="nav-filter-bar__icon-separator" />
                        </li>
                    </ul>
                </template>

                <!-- 关键词 -->
                <span v-if="keyword" class="nav-filter-bar__keyword">{{ $t(keyword || '') }}</span>
            </div>
        </div>

        <!-- 属性筛选标签 -->
        <div v-if="selectedAttrNames.length > 0" class="nav-filter-bar__attrs">
            <ul class="nav-filter-bar__attrs-list">
                <li v-for="(attrGroup, index) in selectedAttrNames" :key="index" class="nav-filter-bar__attr-item">
                    <span class="nav-filter-bar__attr-label">{{ $t(attrGroup.groupName || '') }}:</span>
                    <span v-for="name in attrGroup.nameList" :key="name" class="nav-filter-bar__attr-value">
                        {{ $t(name || '') }}
                    </span>
                    <button class="nav-filter-bar__btn-close" @click="handleRemoveAttr(attrGroup.groupName)"
                        aria-label="移除属性筛选">
                        <UIcon name="i-heroicons-x-mark" />
                    </button>
                </li>
            </ul>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { GoodsCatResultVO, GoodsBrandVO } from '~/types/goods'

/**
 * 组件 Props
 */
interface Props {
    navCategories: GoodsCatResultVO[]
    navBrands: GoodsBrandVO[]
    selectedCatIds: number[]
    selectedAttrNames: Array<{ groupName: string; nameList: string[] }>
    keyword: string
}

const props = defineProps<Props>()

/**
 * 组件事件
 */
const emit = defineEmits<{
    removeCategory: [catId: number, catParentId?: number | undefined]
    selectCategory: [catId: number]
    removeBrand: []
    removeBrandFromFilter: [brandId: number]
    removeAttr: [groupName: string]
}>()

/**
 * 是否有选中的筛选条件
 */
const hasSelectedFilters = computed(() => {
    return props.navCategories.length > 0 ||
        props.navBrands.length > 0 ||
        props.selectedAttrNames.length > 0 ||
        !!props.keyword
})

/**
 * 处理移除分类筛选
 * @param catId 分类ID
 * @param catParentId 父分类ID
 */
const handleRemoveCategory = (catId: number, catParentId?: number): void => {
    emit('removeCategory', catId, catParentId)
}

/**
 * 处理选择分类
 * @param catId 分类ID
 */
const handleSelectCategory = (catId: number): void => {
    emit('selectCategory', catId)
}

/**
 * 处理移除品牌筛选
 */
const handleRemoveBrand = (): void => {
    emit('removeBrand')
}

/**
 * 处理从多选品牌中移除单个品牌
 * @param brandId 品牌ID
 */
const handleRemoveBrandFromFilter = (brandId: number): void => {
    emit('removeBrandFromFilter', brandId)
}

/**
 * 处理移除属性筛选
 * @param groupName 属性组名称
 */
const handleRemoveAttr = (groupName: string): void => {
    emit('removeAttr', groupName)
}
</script>

<style lang="scss" scoped>
.nav-filter-bar {
    width: 100%;
    background: linear-gradient(135deg, color-mix(in srgb, var(--color) 4%, white) 0%, color-mix(in srgb, var(--color) 2%, white) 100%);
    border: 1px solid color-mix(in srgb, var(--color) 10%, transparent);
    border-radius: 8px;
    margin-top: 12px;
    margin-bottom: 12px;
    padding: 12px 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
    box-sizing: border-box;

    &__container {
        display: flex;
        align-items: flex-start;
        gap: 16px;
        margin-bottom: 8px;
    }

    // 左侧标签
    &__label {
        flex-shrink: 0;
        width: 90px;
        font-size: 13px;
        font-weight: 500;
        text-align: right;
        color: #666;
        padding-top: 4px;
        letter-spacing: 1px;
    }

    // 右侧内容区域
    &__content {
        flex: 1;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 8px;
        min-width: 0;
    }

    // 筛选列表
    &__list {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 8px;
        margin: 0;
        padding: 0;
        list-style: none;
    }

    // 筛选项
    &__item {
        position: relative;
        display: inline-flex;
        align-items: center;
        gap: 6px;
        max-width: 280px;
        min-height: 32px;
        padding: 6px 12px;
        background-color: #fff;
        border: 1px solid color-mix(in srgb, var(--color) 30%, transparent);
        border-radius: 6px;
        font-size: 13px;
        color: var(--color);
        transition: all 0.2s ease;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);

        &:hover {
            border-color: color-mix(in srgb, var(--color) 50%, transparent);
            box-shadow: 0 2px 4px color-mix(in srgb, var(--color) 12%, transparent);
            transform: translateY(-1px);
        }

        &.has-children {
            cursor: pointer;

            &:hover {
                border-bottom-left-radius: 0;
                border-bottom-right-radius: 0;
                z-index: 1000;

                .nav-filter-bar__dropdown {
                    display: block;
                    animation: fadeSlideDown 0.2s ease;
                }
            }
        }

        &-content {
            display: flex;
            align-items: center;
            gap: 6px;
            width: 100%;
            min-width: 0;
        }

        &-text {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex: 1;
            min-width: 0;
            font-weight: 500;
        }

        &-label {
            color: #666;
            font-weight: 400;
        }

        &-value {
            color: var(--color);
            font-weight: 500;
        }
    }

    // 关闭按钮
    &__btn-close {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 16px;
        height: 16px;
        padding: 0;
        background-color: transparent;
        border: none;
        border-radius: 50%;
        cursor: pointer;
        color: #999;
        transition: all 0.2s ease;
        flex-shrink: 0;

        :deep(svg) {
            width: 12px;
            height: 12px;
        }

        &:hover {
            color: var(--color);
            background-color: color-mix(in srgb, var(--color) 8%, white);
        }

        &:active {
            transform: scale(0.9);
        }
    }

    // 箭头图标
    &__icon-arrow {
        width: 12px;
        height: 12px;
        color: #999;
        flex-shrink: 0;
        transition: transform 0.2s ease;

        :deep(svg) {
            width: 12px;
            height: 12px;
        }
    }

    // 下拉菜单
    &__dropdown {
        display: none;
        position: absolute;
        top: 100%;
        left: -1px;
        right: -1px;
        z-index: 1000;
        background: #fff;
        border: 1px solid color-mix(in srgb, var(--color) 30%, transparent);
        border-top: none;
        border-radius: 0 0 6px 6px;
        padding: 8px;
        max-height: 400px;
        overflow-y: auto;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        box-sizing: border-box;

        // 自定义滚动条
        &::-webkit-scrollbar {
            width: 6px;
        }

        &::-webkit-scrollbar-track {
            background: #f5f5f5;
            border-radius: 3px;
        }

        &::-webkit-scrollbar-thumb {
            background: #ddd;
            border-radius: 3px;

            &:hover {
                background: #ccc;
            }
        }
    }

    &__dropdown-list {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        list-style: none;
        padding: 0;
        margin: 0;
    }

    &__dropdown-item {
        padding: 6px 12px;
        border-radius: 4px;
        cursor: pointer;
        font-size: 12px;
        color: #666;
        background-color: #f8f8f8;
        transition: all 0.2s ease;
        user-select: none;

        &:hover {
            background-color: color-mix(in srgb, var(--color) 5%, white);
            color: var(--color);
        }

        &.is-active {
            background-color: color-mix(in srgb, var(--color) 5%, white);
            color: var(--color);
            font-weight: 500;
        }
    }

    // 分隔符
    &__separator {
        display: flex;
        align-items: center;
        color: #ccc;
        margin: 0 4px;
    }

    &__icon-separator {
        width: 14px;
        height: 14px;
        color: #ccc;

        :deep(svg) {
            width: 14px;
            height: 14px;
        }
    }

    // 关键词
    &__keyword {
        display: inline-flex;
        align-items: center;
        padding: 6px 12px;
        background-color: #fff;
        border: 1px solid #e5e5e5;
        border-radius: 6px;
        font-size: 13px;
        color: #333;
        font-weight: 500;
    }

    // 属性筛选区域
    &__attrs {
        padding-top: 12px;
        border-top: 1px solid color-mix(in srgb, var(--color) 10%, transparent);
        margin-top: 8px;
    }

    &__attrs-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        list-style: none;
        padding: 0;
        margin: 0;
    }

    &__attr-item {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        min-height: 32px;
        padding: 6px 12px;
        background-color: #fff;
        border: 1px solid color-mix(in srgb, var(--color) 20%, transparent);
        border-radius: 6px;
        font-size: 12px;
        transition: all 0.2s ease;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);

        &:hover {
            border-color: color-mix(in srgb, var(--color) 40%, transparent);
            box-shadow: 0 2px 4px color-mix(in srgb, var(--color) 10%, transparent);
        }
    }

    &__attr-label {
        color: #666;
        font-weight: 400;
    }

    &__attr-value {
        color: var(--color);
        font-weight: 500;
    }

    // 响应式设计
    @media (max-width: 768px) {
        margin-top: 8px;
        margin-bottom: 8px;
        padding: 10px 12px;

        &__container {
            flex-direction: column;
            gap: 10px;
        }

        &__label {
            width: 100%;
            text-align: left;
            padding-top: 0;
        }

        &__content {
            width: 100%;
        }

        &__item {
            max-width: 100%;
            font-size: 12px;
            padding: 5px 10px;
            min-height: 28px;
        }

        &__dropdown {
            max-height: 300px;
        }

        &__keyword {
            font-size: 12px;
            padding: 5px 10px;
        }

        &__attr-item {
            font-size: 11px;
            padding: 5px 10px;
            min-height: 28px;
        }
    }

    @media (max-width: 480px) {
        padding: 8px 10px;

        &__label {
            font-size: 12px;
        }

        &__item {
            font-size: 11px;
            padding: 4px 8px;
        }

        &__btn-close {
            width: 14px;
            height: 14px;

            :deep(svg) {
                width: 10px;
                height: 10px;
            }
        }
    }
}

// 下拉动画
@keyframes fadeSlideDown {
    from {
        opacity: 0;
        transform: translateY(-8px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>
