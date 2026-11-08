<template>
    <!-- 移动端：抽屉承载完整菜单树 -->
    <el-drawer
        v-model="showDrawer"
        direction="ltr"
        :size="drawerSize"
        :with-header="false"
        class="aside-drawer"
    >
        <div class="aside-drawer__inner" :class="`shell-${shellTheme}`">
            <div class="aside-drawer__brand">
                <image-contain
                    v-if="brandLogo"
                    :width="30"
                    :height="30"
                    :src="brandLogo"
                />
                <span v-else class="logo-fallback">{{ brandInitial }}</span>
                <span class="name">{{ webName }}</span>
            </div>
            <el-scrollbar class="aside-drawer__scroll">
                <menu-tree :nodes="nodes" @navigate="closeDrawer" />
            </el-scrollbar>
        </div>
    </el-drawer>

    <!-- 桌面端：左侧边栏（完整菜单树） -->
    <aside
        v-if="!isMobile"
        class="app-aside"
        :class="[`shell-${shellTheme}`, { 'is-collapsed': isCollapsed }]"
        :style="{ width: `${asideWidth}px` }"
    >
        <div v-if="settingStore.showLogo" class="app-aside__brand">
            <image-contain
                v-if="brandLogo"
                class="brand-logo"
                :width="30"
                :height="30"
                :src="brandLogo"
            />
            <span v-else class="brand-logo brand-logo--fallback">{{ brandInitial }}</span>
            <div v-show="!isCollapsed" class="brand-text">
                <span class="name">{{ webName || '商家中心' }}</span>
                <span class="tag">BCENTER</span>
            </div>
        </div>

        <el-scrollbar class="app-aside__scroll">
            <menu-tree v-if="!isCollapsed" :nodes="nodes" />

            <!-- 折叠态：全部叶子拍平成图标列表 -->
            <ul v-else class="aside-flat">
                <li v-for="node in flatNodes" :key="node.fullPath">
                    <el-tooltip :content="node.title" placement="right" :show-after="200">
                        <app-link
                            :to="node.link"
                            class="aside-flat__link"
                            :class="{ 'is-active': isActive(node) }"
                        >
                            <span class="icon-slot">
                                <icon v-if="node.icon" :name="node.icon" :size="17" />
                                <span v-else class="dot" />
                            </span>
                        </app-link>
                    </el-tooltip>
                </li>
            </ul>
        </el-scrollbar>
    </aside>
</template>

<script setup lang="ts">
import useAppStore from '@/stores/modules/app'
import useSettingStore from '@/stores/modules/setting'
import MenuTree from './menu-tree.vue'
import { useShellMenu } from '../../composables/useShellMenu'
import { resolveBrandAsset } from '@/utils/brandAsset'

const appStore = useAppStore()
const settingStore = useSettingStore()
const { nodes, isActive, collectLeaves } = useShellMenu()

const config = computed(() => appStore.config)
const webName = computed(() => config.value.web_name ?? '')
const brandLogo = computed(() => resolveBrandAsset(config.value.shop_admin_logo))
const brandInitial = computed(() => (webName.value || '商').slice(0, 1))
const isMobile = computed(() => appStore.isMobile)
const isCollapsed = computed(() => appStore.isCollapsed)
const shellTheme = computed(() => settingStore.sideTheme)

const flatNodes = computed(() => collectLeaves(nodes.value))

const asideWidth = computed(() =>
    isCollapsed.value ? 64 : Math.min(Math.max(settingStore.sideWidth, 200), 300)
)
const drawerSize = computed(() => `${Math.min(settingStore.sideWidth + 40, 320)}px`)

const showDrawer = computed({
    get() {
        return !appStore.isCollapsed && isMobile.value
    },
    set(value) {
        appStore.toggleCollapsed(!value)
    }
})

const closeDrawer = () => {
    if (isMobile.value) {
        appStore.toggleCollapsed(true)
    }
}
</script>

<style lang="scss" scoped>
.app-aside {
    position: relative;
    z-index: 20;
    flex: none;
    display: flex;
    flex-direction: column;
    min-height: 0;
    border-right: 1px solid var(--rail-border);
    background-color: var(--rail-bg);
    transition: width var(--duration-base) var(--ease-out);

    &.shell-light {
        --rail-bg: var(--neutral-0);
        --rail-border: var(--el-border-color-light);
        --rail-brand-border: var(--el-border-color-lighter);
        --rail-brand-color: var(--el-text-color-primary);
        --rail-brand-tag-bg: var(--brand-50);
        --rail-brand-tag-color: var(--brand-600);
        --rail-text-color: var(--el-text-color-regular);
        --rail-text-hover: var(--brand-600);
        --rail-branch-active: var(--brand-600);
        --rail-hover-bg: var(--neutral-50);
        --rail-active-bg: var(--brand-50);
        --rail-active-color: var(--brand-600);
        --rail-active-bar: var(--brand-500);
        --rail-icon-bg: var(--neutral-75);
        --rail-icon-color: var(--neutral-500);
        --rail-active-icon-bg: var(--brand-500);
        --rail-active-icon-color: #ffffff;
        --rail-caption-color: var(--neutral-400);
        --rail-scroll-thumb: var(--neutral-300);
    }

    &.shell-dark {
        --rail-bg: var(--shell-800);
        --rail-border: rgba(255, 255, 255, 0.08);
        --rail-brand-border: rgba(255, 255, 255, 0.1);
        --rail-brand-color: #ffffff;
        --rail-brand-tag-bg: rgba(255, 255, 255, 0.12);
        --rail-brand-tag-color: rgba(255, 255, 255, 0.85);
        --rail-text-color: rgba(255, 255, 255, 0.72);
        --rail-text-hover: #ffffff;
        --rail-branch-active: #ffffff;
        --rail-hover-bg: rgba(255, 255, 255, 0.06);
        --rail-active-bg: rgba(255, 255, 255, 0.13);
        --rail-active-color: #ffffff;
        --rail-active-bar: var(--accent-400);
        --rail-icon-bg: rgba(255, 255, 255, 0.1);
        --rail-icon-color: rgba(255, 255, 255, 0.72);
        --rail-active-icon-bg: var(--accent-400);
        --rail-active-icon-color: var(--shell-900);
        --rail-caption-color: rgba(255, 255, 255, 0.4);
        --rail-scroll-thumb: rgba(255, 255, 255, 0.22);
    }

    &__brand {
        display: flex;
        align-items: center;
        gap: 10px;
        height: 60px;
        padding: 0 16px;
        flex: none;
        border-bottom: 1px solid var(--rail-brand-border);

        .brand-logo {
            border-radius: var(--radius-sm);
            overflow: hidden;
            flex: none;

            &--fallback {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 30px;
                height: 30px;
                background: var(--brand-500);
                color: #ffffff;
                font-size: var(--el-font-size-base);
                font-weight: 600;
            }
        }

        .brand-text {
            display: flex;
            align-items: baseline;
            gap: 6px;
            min-width: 0;
            flex: 1;

            .name {
                color: var(--rail-brand-color);
                font-size: var(--el-font-size-large);
                font-weight: 600;
                letter-spacing: -0.01em;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
            }

            .tag {
                flex: none;
                padding: 2px 6px;
                border-radius: var(--radius-xs);
                background-color: var(--rail-brand-tag-bg);
                color: var(--rail-brand-tag-color);
                font-size: 10px;
                font-weight: 700;
                letter-spacing: 0.1em;
            }
        }
    }

    &.is-collapsed {
        .app-aside__brand {
            justify-content: center;
            padding: 0;
        }
        .app-aside__scroll {
            padding: 8px 10px 16px;
        }
    }

    &__scroll {
        flex: 1;
        min-height: 0;
        padding: 8px 10px 16px;
        :deep(.el-scrollbar__thumb) {
            background-color: var(--rail-scroll-thumb);
        }
    }

    // 纯 CSS 兜底：即便 JS 的 isMobile 状态因故未更新，小屏也不允许出现桌面侧栏
    @media (max-width: 768px) {
        display: none !important;
    }
}

.aside-flat {
    list-style: none;
    margin: 0;
    padding: 0;

    &__link {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 40px;
        margin-bottom: 4px;
        border-radius: var(--radius-md);
        color: var(--rail-text-color);
        transition:
            background-color var(--duration-base) var(--ease-out),
            color var(--duration-base) var(--ease-out);

        .icon-slot {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 30px;
            height: 30px;
            border-radius: var(--radius-sm);
            background-color: var(--rail-icon-bg);
            color: var(--rail-icon-color);

            .dot {
                width: 5px;
                height: 5px;
                border-radius: 50%;
                background-color: currentColor;
                opacity: 0.6;
            }
        }

        &:hover {
            background-color: var(--rail-hover-bg);
            color: var(--rail-text-hover);
        }
        &.is-active {
            background-color: var(--rail-active-bg);
            .icon-slot {
                background-color: var(--rail-active-icon-bg);
                color: var(--rail-active-icon-color);
            }
        }
    }
}

.aside-drawer {
    :deep(.el-drawer__body) {
        padding: 0;
    }

    &__inner {
        height: 100%;
        display: flex;
        flex-direction: column;

        &.shell-light {
            --rail-text-color: var(--el-text-color-regular);
            --rail-text-hover: var(--brand-600);
            --rail-branch-active: var(--brand-600);
            --rail-hover-bg: var(--neutral-50);
            --rail-active-bg: var(--brand-50);
            --rail-active-color: var(--brand-600);
            --rail-active-bar: var(--brand-500);
            --rail-icon-bg: var(--neutral-75);
            --rail-icon-color: var(--neutral-500);
            --rail-active-icon-bg: var(--brand-500);
            --rail-active-icon-color: #ffffff;
            --rail-caption-color: var(--neutral-400);
            background-color: var(--neutral-0);
            color: var(--el-text-color-primary);
        }

        &.shell-dark {
            --rail-text-color: rgba(255, 255, 255, 0.72);
            --rail-text-hover: #ffffff;
            --rail-branch-active: #ffffff;
            --rail-hover-bg: rgba(255, 255, 255, 0.06);
            --rail-active-bg: rgba(255, 255, 255, 0.13);
            --rail-active-color: #ffffff;
            --rail-active-bar: var(--accent-400);
            --rail-icon-bg: rgba(255, 255, 255, 0.1);
            --rail-icon-color: rgba(255, 255, 255, 0.72);
            --rail-active-icon-bg: var(--accent-400);
            --rail-active-icon-color: var(--shell-900);
            --rail-caption-color: rgba(255, 255, 255, 0.4);
            background-color: var(--shell-800);
            color: #ffffff;
        }
    }

    &__brand {
        display: flex;
        align-items: center;
        gap: 10px;
        height: 60px;
        padding: 0 16px;
        flex: none;
        font-weight: 600;
        border-bottom: 1px solid rgba(128, 128, 128, 0.16);
        .name {
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }
        .logo-fallback {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 30px;
            height: 30px;
            flex: none;
            border-radius: var(--radius-sm);
            background: var(--brand-500);
            color: #ffffff;
            font-size: var(--el-font-size-base);
        }
    }

    &__scroll {
        flex: 1;
        min-height: 0;
        padding: 8px 12px 20px;
    }
}
</style>
