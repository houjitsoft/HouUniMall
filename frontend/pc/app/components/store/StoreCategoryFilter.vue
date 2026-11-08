<template>
    <div class="store-category-filter">
        <div class="filter-title">
            <span class="title-icon">📂</span>
            <span>{{ $t('店内分类') }}</span>
        </div>

        <!-- 搜索框 -->
        <div class="search-box">
            <div class="search-input-wrapper">
                <span class="search-icon">🔍</span>
                <input v-model="searchKeyword" type="text" :placeholder="$t('搜索店内全部商品')" class="search-input"
                    @keyup.enter="handleSearch" @input="handleInput" />
                <button v-if="searchKeyword" class="clear-btn" @click="clearSearch" type="button">×</button>
            </div>
            <button class="search-btn" @click="handleSearch" :disabled="!searchKeyword.trim()">
                <span>{{ $t('搜索') }}</span>
            </button>
        </div>

        <!-- 分类列表 -->
        <div class="category-list">
            <div class="category-item" :class="{ active: selectedCategoryId === null }"
                @click="handleCategoryClick(null)">
                <span class="category-icon">
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                        <rect x="2" y="2" width="10" height="10" rx="1" stroke="currentColor" stroke-width="1.5"
                            fill="none" />
                        <path d="M5 7h4M7 5v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
                    </svg>
                </span>
                <span class="category-name">{{ $t('全部分类') }}</span>
            </div>
            <template v-for="category in categories"
                :key="category.shopGoodsCatId || category.shopGoodsCatDisplayorder">
                <div v-if="category.shopGoodsCatId !== undefined" class="category-group">
                    <div class="category-item" :class="{ active: selectedCategoryId === category.shopGoodsCatId }"
                        @click="handleCategoryClick(category.shopGoodsCatId ?? null)">
                        <span v-if="category.children && category.children.length > 0" class="category-expand"
                            @click.stop="toggleExpand(category.shopGoodsCatId!)">
                            <svg width="14" height="14" viewBox="0 0 14 14" fill="none"
                                :class="{ expanded: isExpanded(category.shopGoodsCatId!) }">
                                <path d="M5 3l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"
                                    stroke-linejoin="round" />
                            </svg>
                        </span>
                        <span v-else class="category-icon category-icon-placeholder"></span>
                        <span class="category-name">{{ $t(category.shopGoodsCatName || '') }}</span>
                        <span v-if="category.children && category.children.length > 0" class="category-count">
                            {{ category.children.length }}
                        </span>
                    </div>
                    <!-- 子分类（可折叠） -->
                    <Transition name="category-collapse">
                        <div v-if="category.children && category.children.length > 0 && isExpanded(category.shopGoodsCatId!)"
                            class="category-children">
                            <div v-for="child in category.children"
                                :key="child.shopGoodsCatId || child.shopGoodsCatDisplayorder"
                                v-show="child.shopGoodsCatId !== undefined" class="category-item category-item-child"
                                :class="{ active: selectedCategoryId === child.shopGoodsCatId }"
                                @click.stop="handleCategoryClick(child.shopGoodsCatId ?? null)">
                                <span class="category-icon category-icon-child">
                                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                                        <circle cx="6" cy="6" r="2" fill="currentColor" />
                                    </svg>
                                </span>
                                <span class="category-name">{{ $t(child.shopGoodsCatName || '') }}</span>
                            </div>
                        </div>
                    </Transition>
                </div>
            </template>
        </div>

        <!-- 空状态 -->
        <EmptyState v-if="!categories || categories.length === 0" :text="$t('暂无分类')" :icon-size="48" :compact="true" />
    </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import type { ManageShopSelfCatMenuVO } from '~/types/store'

const props = defineProps<{
    categories?: ManageShopSelfCatMenuVO[]
}>()

const emit = defineEmits<{
    categoryChange: [catId: number | null]
    search: [keyword: string]
}>()

const searchKeyword = ref('')
const selectedCategoryId = ref<number | null>(null)

// 子分类展开状态（默认全部展开）
const expandedCategoryIds = ref<Set<number>>(new Set())

const isExpanded = (catId: number) => expandedCategoryIds.value.has(catId)

const toggleExpand = (catId: number) => {
    const next = new Set(expandedCategoryIds.value)
    if (next.has(catId)) {
        next.delete(catId)
    } else {
        next.add(catId)
    }
    expandedCategoryIds.value = next
}

// 处理分类点击
const handleCategoryClick = (catId: number | null) => {
    selectedCategoryId.value = catId
    emit('categoryChange', catId)
}

// 处理搜索
const handleSearch = () => {
    if (searchKeyword.value.trim()) {
        emit('search', searchKeyword.value.trim())
    }
}

// 处理输入
const handleInput = () => {
    // 输入时自动触发搜索（可选）
    // 如果需要实时搜索，可以在这里调用 handleSearch
}

// 清空搜索
const clearSearch = () => {
    searchKeyword.value = ''
    // 清空搜索后可以触发一个空搜索或重置
}

// 监听分类变化，重置搜索；默认折叠，不预填充展开状态
watch(() => props.categories, () => {
    searchKeyword.value = ''
})
</script>

<style lang="scss" scoped>
.store-category-filter {
    background-color: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    transition: box-shadow 0.3s ease;

    &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    }

    .filter-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin-bottom: 20px;
        padding-bottom: 16px;
        border-bottom: 2px solid #f5f5f5;

        .title-icon {
            font-size: 18px;
        }
    }

    .search-box {
        display: flex;
        gap: 10px;
        margin-bottom: 20px;

        .search-input-wrapper {
            flex: 1;
            position: relative;
            display: flex;
            align-items: center;
            background-color: #f8f9fa;
            border: 1.5px solid #e9ecef;
            border-radius: 6px;
            transition: all 0.3s ease;
            overflow: hidden;

            &:focus-within {
                border-color: var(--color);
                background-color: #fff;
                box-shadow: 0 0 0 3px rgba(var(--color-rgb, 0, 123, 255), 0.1);
            }

            .search-icon {
                padding: 0 10px;
                font-size: 14px;
                color: #999;
                flex-shrink: 0;
            }

            .search-input {
                flex: 1;
                padding: 8px 6px;
                border: none;
                background: transparent;
                font-size: 14px;
                color: #333;
                outline: none;

                &::placeholder {
                    color: #999;
                }
            }

            .clear-btn {
                padding: 0 10px;
                border: none;
                background: transparent;
                color: #999;
                font-size: 18px;
                line-height: 1;
                cursor: pointer;
                transition: color 0.2s ease;

                &:hover {
                    color: #666;
                }
            }
        }

        .search-btn {
            padding: 8px 16px;
            background: linear-gradient(135deg, var(--color) 0%, var(--rgba4) 100%);
            color: #fff;
            border: none;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s ease;
            white-space: nowrap;
            box-shadow: 0 2px 8px rgba(var(--color-rgb, 0, 123, 255), 0.2);

            &:hover:not(:disabled) {
                transform: translateY(-1px);
                box-shadow: 0 4px 12px rgba(var(--color-rgb, 0, 123, 255), 0.3);
            }

            &:active:not(:disabled) {
                transform: translateY(0);
            }

            &:disabled {
                opacity: 0.5;
                cursor: not-allowed;
            }
        }
    }

    .category-list {
        display: flex;
        flex-direction: column;
        gap: 6px;
        max-height: 500px;
        overflow-y: auto;
        padding-right: 4px;

        /* 自定义滚动条 */
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
                background: #bbb;
            }
        }

        .category-group {
            display: flex;
            flex-direction: column;
            gap: 2px;
        }

        .category-expand {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 20px;
            height: 20px;
            flex-shrink: 0;
            cursor: pointer;
            color: #999;
            border-radius: 4px;
            transition: all 0.2s ease;

            &:hover {
                color: var(--color);
                background-color: rgba(var(--color-rgb, 0, 123, 255), 0.08);
            }

            svg {
                display: block;
                transition: transform 0.2s ease;

                &.expanded {
                    transform: rotate(90deg);
                }
            }
        }

        .category-icon-placeholder {
            width: 20px;
            flex-shrink: 0;
        }

        .category-children {
            display: flex;
            flex-direction: column;
            gap: 2px;
        }

        .category-item {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 12px 14px;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.2s ease;
            color: #666;
            font-size: 14px;
            position: relative;

            &:hover {
                background: linear-gradient(90deg, rgba(var(--color-rgb, 0, 123, 255), 0.08) 0%, transparent 100%);
                color: var(--color);
                transform: translateX(2px);
            }

            &.active {
                background: linear-gradient(135deg, var(--color) 0%, var(--rgba4) 100%);
                color: #fff;
                box-shadow: 0 2px 8px rgba(var(--color-rgb, 0, 123, 255), 0.25);
                transform: translateX(0);

                .category-icon {
                    color: #fff;
                }

                .category-count {
                    background-color: rgba(255, 255, 255, 0.2);
                    color: #fff;
                }
            }

            &.category-item-child {
                padding-left: 40px;
                font-size: 13px;
                margin-left: 8px;
                position: relative;

                &::before {
                    content: '';
                    position: absolute;
                    left: 20px;
                    top: 50%;
                    transform: translateY(-50%);
                    width: 12px;
                    height: 1px;
                    background-color: #e0e0e0;
                }

                &:hover::before {
                    background-color: var(--color);
                }

                &.active::before {
                    background-color: rgba(255, 255, 255, 0.5);
                }
            }

            .category-icon {
                display: flex;
                align-items: center;
                justify-content: center;
                flex-shrink: 0;
                color: #999;
                transition: color 0.2s ease;

                svg {
                    display: block;
                }

                &.category-icon-child {
                    color: #bbb;
                }
            }

            .category-name {
                flex: 1;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .category-count {
                padding: 2px 8px;
                background-color: #f0f0f0;
                border-radius: 12px;
                font-size: 12px;
                color: #999;
                min-width: 20px;
                text-align: center;
            }
        }
    }
}

/* 子分类折叠过渡动画 */
.category-collapse-enter-active,
.category-collapse-leave-active {
    transition: opacity 0.2s ease, transform 0.2s ease;
}

.category-collapse-enter-from,
.category-collapse-leave-to {
    opacity: 0;
    transform: translateY(-4px);
}

.category-collapse-enter-to,
.category-collapse-leave-from {
    opacity: 1;
    transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .store-category-filter {
        padding: 16px;

        .filter-title {
            font-size: 15px;
            margin-bottom: 16px;
            padding-bottom: 12px;
        }

        .search-box {
            flex-direction: column;
            gap: 10px;

            .search-btn {
                width: 100%;
            }
        }

        .category-list {
            max-height: 400px;
        }
    }
}
</style>
