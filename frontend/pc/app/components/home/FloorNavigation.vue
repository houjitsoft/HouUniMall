<template>
    <!-- 完全关闭后：左侧浮起「楼层」入口，可再次打开 -->
    <button
        v-if="userClosed && floorNavList.length > 0 && scrollVisible"
        type="button"
        class="floor-nav-reopen"
        :title="$t('打开楼层导航')"
        @click="reopen"
    >
        {{ $t('楼层') }}
    </button>

    <Transition name="nav-fade">
        <div
            v-if="floorNavList.length > 0 && scrollVisible && !userClosed"
            class="floor-nav-shell"
            :class="{ 'is-collapsed': collapsed }"
        >
            <!-- 收起态：仅露出展开条 -->
            <button
                v-if="collapsed"
                type="button"
                class="floor-nav-peek"
                :title="$t('展开楼层导航')"
                @click="expand"
            >
                <span class="floor-nav-peek__arrow">›</span>
                <span class="floor-nav-peek__label">{{ $t('楼层') }}</span>
            </button>

            <aside v-else class="floor-nav-panel" aria-label="楼层导航">
                <div class="floor-nav-panel__tools">
                    <button
                        type="button"
                        class="tool-btn"
                        :title="$t('向左收起')"
                        @click="collapse"
                    >
                        ‹
                    </button>
                    <button
                        type="button"
                        class="tool-btn tool-btn--close"
                        :title="$t('关闭')"
                        @click="closeNav"
                    >
                        ×
                    </button>
                </div>
                <div
                    v-for="(nav, index) in floorNavList"
                    :key="nav.id || index"
                    class="nav-item"
                    :class="{ active: activeNavIndex === index }"
                    @click="handleNavClick(nav.id, index)"
                >
                    <span class="nav-text">{{ $t(nav.name) }}</span>
                </div>
            </aside>
        </div>
    </Transition>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import type { FloorItem, FloorNavItem } from '~/types/home'

const STORAGE_COLLAPSED = 'mall_pc_floor_nav_collapsed'
const STORAGE_CLOSED = 'mall_pc_floor_nav_closed'

const props = defineProps<{
    floorData: FloorItem[]
}>()

const emits = defineEmits<{
    (e: 'scrollToFloor', floorId: string): void
}>()

const activeNavIndex = ref(-1)
const scrollVisible = ref(false)
const collapsed = ref(false)
const userClosed = ref(false)

const readFlag = (key: string) => {
    if (!import.meta.client) return false
    try {
        return sessionStorage.getItem(key) === '1'
    } catch {
        return false
    }
}

const writeFlag = (key: string, value: boolean) => {
    if (!import.meta.client) return
    try {
        if (value) sessionStorage.setItem(key, '1')
        else sessionStorage.removeItem(key)
    } catch {
        // ignore
    }
}

const collapse = () => {
    collapsed.value = true
    writeFlag(STORAGE_COLLAPSED, true)
}

const expand = () => {
    collapsed.value = false
    writeFlag(STORAGE_COLLAPSED, false)
}

const closeNav = () => {
    userClosed.value = true
    writeFlag(STORAGE_CLOSED, true)
}

const reopen = () => {
    userClosed.value = false
    collapsed.value = false
    writeFlag(STORAGE_CLOSED, false)
    writeFlag(STORAGE_COLLAPSED, false)
}

const shouldShowFloor = (item: FloorItem): boolean => {
    if ((item as any).pageType === 9) {
        return item.data?.[0]?.visible !== false && item.data?.[0]?.visible !== 0 && item.data?.[0]?.visible !== '0'
    }
    if (item.pageType === 5) {
        return item.data?.some((floor: any) => Number(floor?.floorStatus ?? 1) === 1) === true
    }
    if (item.pageType === 6) {
        return item.data?.some((floor: any) => Number(floor?.floorStatus ?? 1) === 1) === true
    }
    return [1, 2, 3, 4, 7, 8].includes(item.pageType) && item.data?.[0]?.pageStart === 1
}

const getFloorName = (item: FloorItem): string => {
    if (item.pageType === 1) return item.data?.[0]?.pageName || '热门推荐'
    if (item.pageType === 2) return item.data?.[0]?.pageTitle || '精选榜单'
    if (item.pageType === 3) return '精选推荐'
    if (item.pageType === 4) return item.data?.[0]?.pageTitle || '特色馆'
    if (item.pageType === 5) return item.data?.[0]?.floorAdpageNameNew?.catName || '商品分类'
    if (item.pageType === 6) return item.data?.[0]?.floorAdpageNameNew?.catName || '分类馆'
    if (item.pageType === 7) return '智能推荐'
    if (item.pageType === 8) return '产品中心'
    if ((item as any).pageType === 9) return item.data?.[0]?.name || 'PC楼层'
    return ''
}

const floorNavList = computed<FloorNavItem[]>(() => {
    const navList: FloorNavItem[] = []
    props.floorData.forEach((item, index) => {
        if (!shouldShowFloor(item)) return
        const name = getFloorName(item)
        const catId = item.data?.[0]?.floorAdpageNameNew?.catId ?? index
        const catName = item.data?.[0]?.floorAdpageNameNew?.catName ?? name
        navList.push({
            id: (item as any).navId || `floor-${index}`,
            name,
            catId: Number(catId),
            catName
        })
    })
    return navList
})

const handleNavClick = (floorId: string, navIndex: number) => {
    emits('scrollToFloor', floorId)
    activeNavIndex.value = navIndex
}

let scrollTimer: number | null = null
const handleScroll = () => {
    if (typeof window === 'undefined' || typeof document === 'undefined') return
    if (scrollTimer !== null) window.clearTimeout(scrollTimer)
    scrollTimer = window.setTimeout(() => {
        const windowHeight = window.innerHeight
        const floorElements = floorNavList.value
            .map((nav) => document.getElementById(nav.id))
            .filter((element): element is HTMLElement => element !== null)
        const firstFloor = floorElements[0]
        const lastFloor = floorElements[floorElements.length - 1]

        if (!firstFloor || !lastFloor) {
            scrollVisible.value = false
            return
        }

        const firstRect = firstFloor.getBoundingClientRect()
        const lastRect = lastFloor.getBoundingClientRect()
        const hasReachedFloors = firstRect.top <= windowHeight * 0.72
        const hasLeftFloors = lastRect.bottom <= 120
        scrollVisible.value = hasReachedFloors && !hasLeftFloors

        if (!scrollVisible.value) {
            activeNavIndex.value = -1
            return
        }

        let currentIndex = -1
        let minDistance = Infinity

        floorNavList.value.forEach((nav, index) => {
            const element = document.getElementById(nav.id)
            if (!element) return
            const rect = element.getBoundingClientRect()
            if (rect.top <= 200 && rect.bottom >= 0) {
                const distance = Math.abs(rect.top - 100)
                if (distance < minDistance) {
                    minDistance = distance
                    currentIndex = index
                }
            }
        })

        if (currentIndex !== -1 && currentIndex !== activeNavIndex.value) {
            activeNavIndex.value = currentIndex
        }
    }, 100)
}

watch(
    () => props.floorData,
    () => {
        if (import.meta.client && props.floorData.length > 0) {
            nextTick(() => {
                setTimeout(() => {
                    handleScroll()
                }, 300)
            })
        }
    },
    { immediate: true }
)

onMounted(() => {
    collapsed.value = readFlag(STORAGE_COLLAPSED)
    userClosed.value = readFlag(STORAGE_CLOSED)
    window.addEventListener('scroll', handleScroll, { passive: true })
    handleScroll()
})

onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
    if (scrollTimer !== null) window.clearTimeout(scrollTimer)
})

defineExpose({
    setActiveNav: (index: number) => {
        activeNavIndex.value = index
    }
})
</script>

<style lang="scss" scoped>
.floor-nav-shell {
    position: fixed;
    left: 20px;
    top: 50%;
    transform: translateY(-50%);
    z-index: 999;
}

.floor-nav-panel {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    padding: 4px 0 10px;
    min-width: 100px;
    max-height: 80vh;
    overflow-y: auto;
    scrollbar-width: none;
    -ms-overflow-style: none;
    transition: opacity 0.25s ease, transform 0.25s ease;

    &::-webkit-scrollbar {
        display: none;
        width: 0;
        height: 0;
    }
}

.floor-nav-panel__tools {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 2px;
    padding: 4px 8px 2px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 4px;
    position: sticky;
    top: 0;
    background: #fff;
    z-index: 1;
}

.tool-btn {
    width: 22px;
    height: 22px;
    border: 0;
    border-radius: 4px;
    background: transparent;
    color: #999;
    font-size: 16px;
    line-height: 1;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 0;

    &:hover {
        background: #f5f5f5;
        color: var(--color, #ef1d2d);
    }

    &--close:hover {
        color: #333;
    }
}

.nav-item {
    padding: 12px 20px;
    cursor: pointer;
    transition: all 0.2s ease;
    position: relative;
    border-left: 3px solid transparent;

    &:hover {
        background-color: #f5f5f5;
        color: var(--color, #ef1d2d);
    }

    &.active {
        background-color: #f0f7ff;
        color: var(--color, #ef1d2d);
        border-left-color: var(--color, #ef1d2d);
        font-weight: 500;

        .nav-text {
            color: var(--color, #ef1d2d);
        }
    }

    .nav-text {
        font-size: 14px;
        color: #333;
        white-space: nowrap;
    }
}

.floor-nav-peek {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    width: 28px;
    min-height: 88px;
    padding: 10px 0;
    border: 0;
    border-radius: 0 8px 8px 0;
    background: #fff;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
    cursor: pointer;
    color: #666;

    &:hover {
        color: var(--color, #ef1d2d);
    }

    &__arrow {
        font-size: 18px;
        line-height: 1;
        font-weight: 700;
    }

    &__label {
        writing-mode: vertical-rl;
        font-size: 12px;
        letter-spacing: 2px;
    }
}

.floor-nav-reopen {
    position: fixed;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    z-index: 999;
    border: 0;
    border-radius: 0 8px 8px 0;
    background: #fff;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
    padding: 14px 8px;
    writing-mode: vertical-rl;
    letter-spacing: 2px;
    font-size: 12px;
    color: #666;
    cursor: pointer;

    &:hover {
        color: var(--color, #ef1d2d);
    }
}

@media (max-width: 1024px) {
    .floor-nav-shell {
        left: 10px;
    }

    .floor-nav-panel {
        min-width: 80px;
    }

    .nav-item {
        padding: 10px 12px;

        .nav-text {
            font-size: 12px;
        }
    }
}

@media (max-width: 768px) {
    .floor-nav-shell,
    .floor-nav-reopen {
        display: none;
    }
}

.nav-fade-enter-active,
.nav-fade-leave-active {
    transition: opacity 0.25s ease, transform 0.25s ease;
}

.nav-fade-enter-from,
.nav-fade-leave-to {
    opacity: 0;
    transform: translateY(-50%) translateX(-16px);
}

.nav-fade-enter-to,
.nav-fade-leave-from {
    opacity: 1;
    transform: translateY(-50%) translateX(0);
}
</style>
