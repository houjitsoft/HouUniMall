<template>
    <div
        class="left-category"
        @mouseenter="onPanelEnter"
        @mouseleave="onPanelLeave"
    >
        <div class="category-nav">
            <!-- 加载中：避免 SSR/首屏空白灰块 -->
            <div v-if="loading && !categories.length" class="category-loading" aria-busy="true">
                <div v-for="i in 10" :key="i" class="category-skeleton-row">
                    <span class="sk-icon" />
                    <span class="sk-text" />
                </div>
            </div>

            <div
                v-for="category in categories"
                :key="category.catId"
                class="category-item"
                :class="{ active: activeCategory === category.catId }"
                @mouseenter="showSubcategory(category.catId)"
            >
                <div class="category-row">
                    <span class="category-icon-wrap" aria-hidden="true">
                        <img
                            v-if="category.catPic"
                            :src="category.catPic"
                            :alt="category.catName"
                            class="category-icon"
                        />
                        <svg
                            v-else
                            class="category-icon category-icon-fallback"
                            viewBox="0 0 24 24"
                            fill="none"
                            xmlns="http://www.w3.org/2000/svg"
                        >
                            <path
                                d="M4 7h16M4 12h16M4 17h10"
                                stroke="currentColor"
                                stroke-width="1.6"
                                stroke-linecap="round"
                            />
                        </svg>
                    </span>

                    <div class="category-text">
                        <span
                            class="category-link category-link-primary"
                            @click.stop="handleClassClick(category.catId)"
                        >{{ $t(category.catName) }}</span>
                        <template
                            v-for="(item, idx) in getInlineCats(category)"
                            :key="`${item.catId}-${idx}`"
                        >
                            <span
                                class="category-divider"
                                aria-hidden="true"
                            >|</span>
                            <span
                                class="category-link category-link-secondary"
                                @click.stop="handleClassClick(item.catId)"
                            >{{ $t(item.catName) }}</span>
                        </template>
                        <span
                            v-if="hasMoreInline(category)"
                            class="category-ellipsis"
                            aria-hidden="true"
                        >...</span>
                    </div>
                </div>
            </div>

            <div v-if="!loading && !categories.length" class="category-empty">
                <span>{{ $t('分类加载失败') }}</span>
                <button type="button" class="category-retry" @click="reloadCategories">
                    {{ $t('重试') }}
                </button>
            </div>
        </div>

        <div
            v-show="activeCategory"
            class="subcategory"
            @mouseenter="onPanelEnter"
            @mouseleave="onPanelLeave"
        >
            <div class="subcategory-inner">
                <div class="subcategory-hd">
                    <span class="subcategory-hd-title">{{ $t(activeCategoryName) }}</span>
                    <span
                        class="subcategory-hd-more"
                        @click="handleClassClick(activeCategory!)"
                    >{{ $t('查看全部') }} &gt;</span>
                </div>
                <div class="subcategory-content">
                    <div
                        v-for="section in subCategoryData"
                        :key="section.secordCatId"
                        class="subcategory-section"
                    >
                        <div
                            class="section-title"
                            @click="handleClassClick(section.secordCatId)"
                        >
                            {{ $t(section.secordCatName) }}
                        </div>
                        <div class="section-items">
                            <span
                                v-for="item in section.gdsCats"
                                :key="item.catId"
                                class="section-item"
                                @click="handleClassClick(item.catId)"
                            >{{ $t(item.catName) }}</span>
                            <span
                                v-if="!section.gdsCats.length"
                                class="section-item section-item-empty"
                                @click="handleClassClick(section.secordCatId)"
                            >{{ $t('进入分类') }}</span>
                        </div>
                    </div>
                    <div v-if="!subCategoryData.length" class="subcategory-empty">
                        {{ $t('暂无子分类') }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onBeforeUnmount, onMounted, watch } from 'vue'
import type { CategoryItem, SubCategorySection } from '~/types/home'
import { resolveServerApiBase } from '~/utils/apiBase'

/**
 * 首页 Banner 左侧分类导航（京东风格）
 * - useAsyncData：SSR/首屏预取，避免预渲染空树导致长时间空白灰块
 * - 左栏：图标 + 二级名 | 分隔；悬停展开三级
 */

const INLINE_CAT_LIMIT = 2
const HIDE_DELAY_MS = 120

const route = useRoute()

const integralType = computed(() => {
    const path = route.path
    return path === '/points' || path === '/points/' ? 1 : 2
})

const activeCategory = ref<number | null>(null)
const subCategoryData = ref<SubCategorySection[]>([])

let hideTimer: ReturnType<typeof setTimeout> | null = null

type InlineCat = { catId: number; catName: string }

const extractCategoryList = (res: any): any[] => {
    if (!res) return []
    const root = res?.data && (res.data.category_lists || res.data.lists || res.data.list || res.data.children)
        ? res.data
        : res
    const list = root?.category_lists || root?.lists || root?.list || root?.children || root
    return Array.isArray(list) ? list : []
}

const normalizeTreeChildren = (list: any[]): any[] => {
    return (Array.isArray(list) ? list : [])
        .map((child: any) => {
            const catId = Number(child?.catId ?? child?.id)
            const catName = String(child?.catName ?? child?.name ?? '').trim()
            const nested = child?.children || child?.sons || []
            return {
                catId,
                catName,
                children: normalizeTreeChildren(nested),
            }
        })
        .filter((child) => Number.isFinite(child.catId) && child.catId > 0 && child.catName)
}

const mapCategories = (res: any): CategoryItem[] => {
    return extractCategoryList(res)
        .map((item: any) => ({
            catId: Number(item.catId ?? item.id),
            catName: String(item.catName ?? item.name ?? '').trim(),
            catPic: String(item.catPic ?? item.image ?? ''),
            children: normalizeTreeChildren(item.children || item.sons || []),
        }))
        .filter((item) => Number.isFinite(item.catId) && item.catId > 0 && item.catName)
}

const asyncKey = 'mall-banner-left-category'

const resolveCategoryUrl = () => {
    const runtimeConfig = useRuntimeConfig()
    // SSR/预渲染必须绝对地址；浏览器走同源相对路径，避免 CORS / Private Network Access。
    // 服务端兜底是站点绝对地址（siteUrl），不能是 loopback：构建机上没有 BFF。
    if (import.meta.server) {
        const base = resolveServerApiBase(runtimeConfig)
        return `${base.replace(/\/$/, '')}/web/mall/index/category`
    }
    return '/web/mall/index/category'
}

const fetchCategoryPayload = async () => {
    // 不走 base.service 顶层 useRequest()，避免部分上下文拿不到正确 baseURL
    const url = resolveCategoryUrl()
    const res: any = await $fetch(url, { method: 'GET' })
    if (!extractCategoryList(res).length) {
        throw new Error(`category_lists empty from ${url}`)
    }
    return res
}

const {
    data: categoryPayload,
    pending: loading,
    refresh,
    error: categoryError,
} = await useAsyncData(asyncKey, fetchCategoryPayload, {
    watch: [integralType],
    server: true,
    lazy: false,
    default: () => null,
    getCachedData(key, nuxtApp) {
        const cached = nuxtApp.payload.data[key] ?? nuxtApp.static?.data?.[key]
        if (!cached) return undefined
        if (!extractCategoryList(cached).length) return undefined
        return cached
    },
})

const categories = computed(() => mapCategories(categoryPayload.value))

watch(categoryError, (err) => {
    if (err) console.error('获取分类数据失败:', err)
})

const reloadCategories = async () => {
    await refresh()
}

// 首屏若仍为空（历史静态空 payload），挂载后再拉一次
onMounted(() => {
    if (!categories.value.length) {
        refresh()
    }
})

const activeCategoryName = computed(() => {
    const hit = categories.value.find((item) => item.catId === activeCategory.value)
    return hit?.catName || ''
})

const normalizeChildren = (category: CategoryItem): InlineCat[] => {
    const list = category.children || []
    return list
        .map((child: any) => ({
            catId: Number(child.catId ?? child.id),
            catName: String(child.catName ?? child.name ?? ''),
        }))
        .filter((child) => child.catId && child.catName)
}

const getInlineCats = (category: CategoryItem): InlineCat[] => {
    const children = normalizeChildren(category)
    return children.slice(0, INLINE_CAT_LIMIT)
}

const hasMoreInline = (category: CategoryItem): boolean => {
    return normalizeChildren(category).length > INLINE_CAT_LIMIT
}

const clearHideTimer = () => {
    if (hideTimer) {
        clearTimeout(hideTimer)
        hideTimer = null
    }
}

const showSubcategory = (categoryId: number) => {
    clearHideTimer()
    activeCategory.value = categoryId
    const category = categories.value.find((item) => item.catId === categoryId)
    subCategoryData.value = (category?.children || []).map((child: any) => ({
        secordCatId: Number(child.catId ?? child.id),
        secordCatName: child.catName ?? child.name ?? '',
        catId: Number(child.catId ?? child.id),
        gdsCats: (child.children || child.sons || []).map((leaf: any) => ({
            catId: Number(leaf.catId ?? leaf.id),
            catName: leaf.catName ?? leaf.name ?? '',
        })),
    }))
}

const onPanelEnter = () => clearHideTimer()

const onPanelLeave = () => {
    clearHideTimer()
    hideTimer = setTimeout(() => {
        activeCategory.value = null
        hideTimer = null
    }, HIDE_DELAY_MS)
}

const handleClassClick = (catId: number) => {
    if (!catId) return
    if (integralType.value === 1) {
        navigateTo(`/points/list?catId=${catId}`)
        return
    }
    navigateTo(`/product/list?catId=${catId}`)
}

onBeforeUnmount(() => clearHideTimer())
</script>

<style lang="scss" scoped>
.left-category {
    width: 100%;
    height: 100%;
    position: relative;
}

.category-nav {
    height: 414px;
    padding: 8px 0;
    overflow-y: hidden;
    background-color: var(--home-surface, #fff);
    border: 1px solid var(--home-border, #e2e8ed);
    border-radius: var(--home-card-radius, 8px);
    box-shadow: var(--home-card-shadow, 0 2px 10px rgba(23, 50, 77, 0.05));
    box-sizing: border-box;

    &:hover {
        overflow-y: auto;
    }

    .category-loading {
        padding: 4px 12px;
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .category-skeleton-row {
        display: flex;
        align-items: center;
        gap: 10px;
        height: 28px;
    }

    .sk-icon {
        width: 16px;
        height: 16px;
        border-radius: 4px;
        background: linear-gradient(90deg, #ebebeb 25%, #f5f5f5 50%, #ebebeb 75%);
        background-size: 200% 100%;
        animation: cat-sk 1.2s ease-in-out infinite;
        flex-shrink: 0;
    }

    .sk-text {
        flex: 1;
        height: 12px;
        border-radius: 4px;
        background: linear-gradient(90deg, #ebebeb 25%, #f5f5f5 50%, #ebebeb 75%);
        background-size: 200% 100%;
        animation: cat-sk 1.2s ease-in-out infinite;
    }

    .category-empty {
        padding: 48px 16px;
        text-align: center;
        color: #999;
        font-size: 13px;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
    }

    .category-retry {
        border: 1px solid #ddd;
        background: #fff;
        color: #333;
        border-radius: 4px;
        padding: 6px 14px;
        cursor: pointer;
        font-size: 12px;

        &:hover {
            border-color: var(--color, #e4393c);
            color: var(--color, #e4393c);
        }
    }

    .category-item {
        .category-row {
            display: flex;
            align-items: center;
            gap: 9px;
            min-height: 33px;
            padding: 5px 11px;
            cursor: default;
            border-left: 3px solid transparent;
            transition: background-color 0.15s ease, border-color 0.15s ease;
            color: var(--home-text, #202a33);
        }

        &:hover .category-row,
        &.active .category-row {
            background-color: var(--color-primary-soft, rgba(234, 84, 85, 0.08));
            border-left-color: var(--color);
        }

        .category-icon-wrap {
            width: 16px;
            height: 16px;
            flex-shrink: 0;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            color: var(--home-text-secondary, #66727d);
        }

        .category-icon {
            width: 16px;
            height: 16px;
            object-fit: contain;
            display: block;
        }

        .category-icon-fallback {
            width: 15px;
            height: 15px;
        }

        .category-text {
            flex: 1;
            min-width: 0;
            display: flex;
            align-items: center;
            overflow: hidden;
            white-space: nowrap;
            font-size: 13px;
            line-height: 20px;
            color: var(--home-text-secondary, #66727d);
        }

        .category-link {
            flex-shrink: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            cursor: pointer;

            &:hover {
                color: var(--color, #e4393c);
            }
        }

        .category-link-primary {
            max-width: 82px;
            color: var(--home-text, #202a33);
            font-size: 14px;
            font-weight: 600;
        }

        .category-link-secondary {
            max-width: 56px;
            color: var(--home-text-secondary, #66727d);
            font-size: 12.5px;
            font-weight: 400;
        }

        .category-divider {
            flex-shrink: 0;
            margin: 0 5px;
            color: var(--home-border-strong, #d5dee5);
            font-size: 12px;
            line-height: 20px;
        }

        .category-ellipsis {
            flex-shrink: 0;
            margin-left: 2px;
            color: #999;
            letter-spacing: 1px;
        }
    }
}

@keyframes cat-sk {
    0% { background-position: 100% 0; }
    100% { background-position: -100% 0; }
}

.subcategory {
    position: absolute;
    left: 100%;
    top: 0;
    z-index: 1000;
    width: 750px;
    min-height: 414px;
    max-height: 465px;
    background: #fff;
    border: 1px solid var(--home-border, #e2e8ed);
    border-left: none;
    border-radius: 0 var(--home-card-radius, 8px) var(--home-card-radius, 8px) 0;
    box-shadow: 8px 12px 30px rgba(23, 50, 77, 0.13);
    box-sizing: border-box;
}

.subcategory-inner {
    display: flex;
    flex-direction: column;
    height: 100%;
    max-height: 465px;
}

.subcategory-hd {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 20px 10px;
    border-bottom: 1px solid #f0f0f0;
    flex-shrink: 0;

    .subcategory-hd-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--home-text, #202a33);
    }

    .subcategory-hd-more {
        font-size: 12px;
        color: var(--home-text-muted, #8b96a0);
        cursor: pointer;

        &:hover {
            color: var(--color, #e4393c);
        }
    }
}

.subcategory-content {
    padding: 12px 20px 18px;
    overflow-y: auto;
    flex: 1;
}

.subcategory-section {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 10px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
        border-bottom: none;
    }

    .section-title {
        width: 88px;
        flex-shrink: 0;
        font-size: 13px;
        font-weight: 600;
        color: var(--home-text, #202a33);
        line-height: 22px;
        cursor: pointer;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;

        &::after {
            content: ' >';
            font-weight: 400;
            color: #bbb;
            font-size: 12px;
        }

        &:hover {
            color: var(--color, #e4393c);

            &::after {
                color: var(--color, #e4393c);
            }
        }
    }

    .section-items {
        flex: 1;
        display: flex;
        flex-wrap: wrap;
        gap: 4px 0;
        min-width: 0;
    }

    .section-item {
        font-size: 12px;
        color: var(--home-text-secondary, #66727d);
        line-height: 22px;
        cursor: pointer;
        padding: 0 10px;
        position: relative;
        white-space: nowrap;

        &:not(:last-child)::after {
            content: '';
            position: absolute;
            right: 0;
            top: 5px;
            width: 1px;
            height: 12px;
            background: #e8e8e8;
        }

        &:hover {
            color: var(--color, #e4393c);
        }

        &.section-item-empty {
            color: #999;
            padding-left: 0;

            &::after {
                display: none;
            }
        }
    }
}

.subcategory-empty {
    padding: 40px 0;
    text-align: center;
    color: #999;
    font-size: 13px;
}

@media (max-width: 1024px) {
    .category-nav {
        height: auto;
        max-height: 320px;
    }

    .subcategory {
        width: min(750px, calc(100vw - 240px));
        min-height: 280px;
    }
}
</style>
