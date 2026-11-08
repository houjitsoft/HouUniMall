<template>
    <div v-if="settingStore.openMultipleTabs" class="tabsbar">
        <div class="tabsbar__scroll">
            <el-tabs
                :model-value="currentTab"
                :closable="tabsLists.length > 1"
                @tab-change="handleChange"
                @tab-remove="removeTab($event)"
            >
                <template v-for="item in tabsLists" :key="item.fullPath">
                    <el-tab-pane :label="item.title" :name="item.fullPath"></el-tab-pane>
                </template>
            </el-tabs>
        </div>
        <el-dropdown class="tabsbar__more" @command="handleCommand">
            <span class="trigger">
                <icon :size="16" name="el-icon-ArrowDown" />
            </span>
            <template #dropdown>
                <el-dropdown-menu>
                    <el-dropdown-item command="closeCurrent">关闭当前</el-dropdown-item>
                    <el-dropdown-item command="closeOther">关闭其他</el-dropdown-item>
                    <el-dropdown-item command="closeAll">关闭全部</el-dropdown-item>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
    </div>
</template>

<script setup lang="ts">
import useMultipleTabs from '@/hooks/useMultipleTabs'
import { useWatchRoute } from '@/hooks/useWatchRoute'
import useTabsStore, { getRouteParams } from '@/stores/modules/multipleTabs'
import useSettingStore from '@/stores/modules/setting'

const router = useRouter()
const tabsStore = useTabsStore()
const settingStore = useSettingStore()
const { removeOtherTab, addTab, removeAllTab, removeTab, tabsLists, currentTab } = useMultipleTabs()
useWatchRoute(() => {
    addTab()
})

const handleChange = (fullPath: any) => {
    const tabItem = tabsStore.tasMap[fullPath]
    router.push(getRouteParams(tabItem))
}

const handleCommand = (command: any) => {
    switch (command) {
        case 'closeCurrent':
            removeTab()
            break
        case 'closeOther':
            removeOtherTab()
            break
        case 'closeAll':
            removeAllTab()
            break
    }
}
</script>

<style lang="scss" scoped>
.tabsbar {
    flex: none;
    display: flex;
    align-items: center;
    height: var(--tabsbar-height);
    padding: 0 8px 0 4px;
    background-color: var(--el-bg-color);
    border-bottom: 1px solid var(--el-border-color-lighter);

    &__scroll {
        flex: 1;
        min-width: 0;
        overflow: hidden;

        :deep(.el-tabs) {
            height: var(--tabsbar-height);

            .el-tabs__header {
                margin-bottom: 0;
            }
            .el-tabs__content {
                display: none;
            }
            .el-tabs__nav-wrap::after {
                height: 0;
            }
            .el-tabs__nav-prev,
            .el-tabs__nav-next {
                height: var(--tabsbar-height);
                line-height: var(--tabsbar-height);
                color: var(--el-text-color-secondary);
            }
            .el-tabs__active-bar {
                display: none;
            }
            .el-tabs__item {
                height: 30px;
                line-height: 28px;
                padding: 0 12px !important;
                margin: 0 3px;
                border: 1px solid transparent;
                border-radius: var(--radius-pill);
                color: var(--el-text-color-regular);
                font-weight: 500;
                transition:
                    background-color var(--duration-base) var(--ease-out),
                    color var(--duration-base) var(--ease-out),
                    border-color var(--duration-base) var(--ease-out);

                &:hover {
                    background-color: var(--neutral-50);
                    color: var(--el-color-primary);
                }

                &.is-active {
                    background-color: var(--brand-50);
                    border-color: var(--brand-100);
                    color: var(--brand-600);
                    font-weight: 600;
                }

                .is-icon-close {
                    width: 14px;
                    height: 14px;
                    margin-left: 4px;
                    border-radius: 50%;
                    color: var(--el-text-color-placeholder);
                    vertical-align: -2px;
                    transition:
                        background-color var(--duration-base) var(--ease-out),
                        color var(--duration-base) var(--ease-out);
                    &:hover {
                        background-color: var(--state-danger);
                        color: #ffffff;
                    }
                }
            }
        }
    }

    &__more {
        flex: none;
        .trigger {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 30px;
            height: 30px;
            border-radius: var(--radius-sm);
            color: var(--el-text-color-secondary);
            cursor: pointer;
            transition:
                background-color var(--duration-base) var(--ease-out),
                color var(--duration-base) var(--ease-out);
            &:hover {
                background-color: var(--neutral-50);
                color: var(--el-color-primary);
            }
        }
    }
}
</style>
