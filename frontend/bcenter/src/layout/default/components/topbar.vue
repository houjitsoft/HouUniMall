<template>
    <header class="topbar" :class="[`shell-${shellTheme}`, { 'is-mobile': isMobile }]">
        <div class="topbar__left">
            <fold class="topbar__btn" />
            <breadcrumb v-if="!isMobile && settingStore.showCrumb" class="topbar__crumb" />
        </div>

        <div class="topbar__right">
            <router-link
                v-if="message.title"
                to="/application/notice/notice/lists"
                class="notice"
            >
                <icon name="el-icon-Bell" :size="15" />
                <el-tag
                    v-if="message.priority == 1"
                    type="danger"
                    size="small"
                    effect="dark"
                    round
                >
                    紧急
                </el-tag>
                <el-tag
                    v-else-if="message.priority == 2"
                    type="warning"
                    size="small"
                    effect="dark"
                    round
                >
                    重要
                </el-tag>
                <span class="notice__title">{{ message.title }}</span>
            </router-link>

            <refresh v-if="!isMobile" class="topbar__btn" />
            <full-screen v-if="!isMobile" class="topbar__btn" />
            <div class="topbar__user">
                <user-drop-down />
            </div>
            <setting class="topbar__btn" />
        </div>
    </header>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import useAppStore from '@/stores/modules/app'
import useSettingStore from '@/stores/modules/setting'
import Fold from './header/fold.vue'
import Refresh from './header/refresh.vue'
import FullScreen from './header/full-screen.vue'
import UserDropDown from './header/user-drop-down.vue'
import Breadcrumb from './header/breadcrumb.vue'
import Setting from './setting/index.vue'
import { getNotice } from '@/api/notice'

const appStore = useAppStore()
const settingStore = useSettingStore()
const isMobile = computed(() => appStore.isMobile)
const shellTheme = computed(() => settingStore.sideTheme)

const message = ref<any>({})
onMounted(async () => {
    message.value = (await getNotice()) ?? {}
})
</script>

<style lang="scss" scoped>
.topbar {
    position: relative;
    z-index: 30;
    flex: none;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    height: var(--topbar-height);
    padding-right: 10px;
    box-shadow: var(--shadow-sm);

    &.shell-light {
        --bar-bg: var(--neutral-0);
        --bar-border: var(--el-border-color-light);
        --bar-text: var(--el-text-color-regular);
        --bar-text-strong: var(--el-text-color-primary);
        --bar-hover-bg: var(--neutral-50);
        --bar-active-bg: var(--brand-50);
        --bar-active-text: var(--brand-600);
        background-color: var(--bar-bg);
        border-bottom: 1px solid var(--bar-border);
    }

    &.shell-dark {
        --bar-bg: var(--shell-900);
        --bar-border: rgba(255, 255, 255, 0.1);
        --bar-text: rgba(255, 255, 255, 0.74);
        --bar-text-strong: #ffffff;
        --bar-hover-bg: rgba(255, 255, 255, 0.08);
        --bar-active-bg: rgba(255, 255, 255, 0.14);
        --bar-active-text: #ffffff;
        background-color: var(--bar-bg);
        border-bottom: 1px solid var(--bar-border);
    }

    &__left {
        display: flex;
        align-items: center;
        flex: 1;
        min-width: 0;
        padding-left: 6px;
    }

    &__crumb {
        min-width: 0;
        overflow: hidden;
        white-space: nowrap;
    }

    /* 覆盖子组件自带的 h-full / px-2，统一顶栏按钮尺寸 */
    .topbar &__btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        height: 34px;
        min-width: 34px;
        padding: 0 6px;
        margin: 0 2px;
        border-radius: var(--radius-sm);
        color: var(--bar-text);
        cursor: pointer;
        transition:
            background-color var(--duration-base) var(--ease-out),
            color var(--duration-base) var(--ease-out);
        &:hover {
            background-color: var(--bar-hover-bg);
            color: var(--bar-text-strong);
        }
    }

    &__right {
        display: flex;
        align-items: center;
        flex: none;
        gap: 2px;
    }

    &__user {
        display: flex;
        align-items: center;
        padding: 0 4px;
        color: var(--bar-text);
        :deep(.el-dropdown) {
            color: inherit;
        }
    }

    .notice {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        max-width: 260px;
        height: 34px;
        padding: 0 12px;
        margin-right: 4px;
        border-radius: var(--radius-pill);
        background-color: var(--bar-hover-bg);
        color: var(--bar-text);
        text-decoration: none;
        font-size: var(--el-font-size-small);
        transition: background-color var(--duration-base) var(--ease-out);
        &:hover {
            background-color: var(--bar-active-bg);
        }
        &__title {
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }
        :deep(.el-tag) {
            flex: none;
            height: 18px;
            padding: 0 6px;
            line-height: 18px;
            font-size: 11px;
            border: none;
        }
    }

    &.is-mobile {
        .notice {
            display: none;
        }
    }

    // 纯 CSS 兜底：小屏隐藏面包屑与公告
    @media (max-width: 768px) {
        &__crumb,
        .notice {
            display: none !important;
        }
    }
}
</style>
