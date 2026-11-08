<template>
    <div class="setting-drawer">
        <el-drawer
            v-model="showSetting"
            append-to-body
            direction="rtl"
            size="250px"
            title="主题设置"
        >
            <div class="setting-item mb-5">
                <span class="text-tx-secondary">风格设置</span>
                <div class="flex mt-4 cursor-pointer">
                    <div
                        class="style-card"
                        :class="[`style-card--${item.type}`, { 'is-active': sideTheme == item.type }]"
                        v-for="item in sideThemeList"
                        :key="item.type"
                        @click="sideTheme = item.type"
                    >
                        <span class="style-card__bar"></span>
                        <span class="style-card__rail"></span>
                        <span class="style-card__body"></span>
                        <icon
                            v-if="sideTheme == item.type"
                            class="icon-select"
                            name="el-icon-Select"
                        />
                        <span class="style-card__label">{{ item.name }}</span>
                    </div>
                </div>
            </div>
            <div class="setting-item mb-5 flex justify-between items-center">
                <span class="text-tx-secondary">主题颜色</span>
                <div>
                    <el-color-picker v-model="theme" :predefine="predefineColors" />
                </div>
            </div>
            <div class="setting-item mb-5 flex justify-between items-center">
                <span class="text-tx-secondary">开启黑暗模式</span>
                <div>
                    <el-switch :model-value="isDark" @change="toggleDark" />
                </div>
            </div>
            <div class="setting-item mb-5 flex justify-between items-center">
                <span class="text-tx-secondary">开启多页签栏</span>
                <div>
                    <el-switch
                        v-model="openMultipleTabs"
                        :active-value="true"
                        :inactive-value="false"
                    />
                </div>
            </div>
            <div class="setting-item mb-5 flex justify-between items-center">
                <span class="text-tx-secondary">只展开一个一级菜单</span>
                <div>
                    <el-switch
                        v-model="isUniqueOpened"
                        :active-value="true"
                        :inactive-value="false"
                    />
                </div>
            </div>
            <div class="setting-item mb-5 flex justify-between items-center">
                <div class="text-tx-secondary flex-none mr-3">菜单栏宽度</div>
                <div>
                    <el-input-number v-model="sideWidth" :min="180" :max="250" />
                </div>
            </div>
            <div class="setting-item mb-5 flex justify-between items-center">
                <div class="text-tx-secondary flex-none mr-3">显示LOGO</div>
                <div>
                    <el-switch v-model="showLogo" :active-value="true" :inactive-value="false" />
                </div>
            </div>
            <div class="setting-item mb-5 flex justify-between items-center">
                <div class="text-tx-secondary flex-none mr-3">显示面包屑</div>
                <div>
                    <el-switch v-model="showCrumb" :active-value="true" :inactive-value="false" />
                </div>
            </div>
            <div class="setting-item mb-5 flex justify-between items-center">
                <el-button @click="resetTheme">重置主题</el-button>
            </div>
        </el-drawer>
    </div>
</template>

<script setup lang="ts">
import useSettingStore from '@/stores/modules/setting'
import { useDark, useToggle } from '@vueuse/core'

const settingStore = useSettingStore()
const predefineColors = ref(['#ef4f55', '#d93d45', '#17855c', '#c0780e', '#3c7ca0', '#6b5bd2'])
const sideThemeList = [
    {
        type: 'dark',
        name: '沉稳墨'
    },
    {
        type: 'light',
        name: '暖砂白'
    }
]

const sideTheme = computed({
    get() {
        return settingStore.sideTheme
    },
    set(value) {
        settingStore.setSetting({
            key: 'sideTheme',
            value
        })
    }
})

const openMultipleTabs = computed({
    get() {
        return settingStore.openMultipleTabs
    },
    set(value) {
        settingStore.setSetting({
            key: 'openMultipleTabs',
            value
        })
    }
})

const isUniqueOpened = computed({
    get() {
        return settingStore.isUniqueOpened
    },
    set(value) {
        settingStore.setSetting({
            key: 'isUniqueOpened',
            value
        })
    }
})

const sideWidth = computed({
    get() {
        return settingStore.sideWidth
    },
    set(value) {
        settingStore.setSetting({
            key: 'sideWidth',
            value
        })
    }
})
const showSetting = computed({
    get() {
        return settingStore.showDrawer
    },
    set(value) {
        settingStore.setSetting({
            key: 'showDrawer',
            value
        })
    }
})
const theme = computed({
    get() {
        return settingStore.theme
    },
    set(value) {
        settingStore.setSetting({
            key: 'theme',
            value
        })
        themeChange()
    }
})

const showLogo = computed({
    get() {
        return settingStore.showLogo
    },
    set(value) {
        settingStore.setSetting({
            key: 'showLogo',
            value
        })
    }
})

const showCrumb = computed({
    get() {
        return settingStore.showCrumb
    },
    set(value) {
        settingStore.setSetting({
            key: 'showCrumb',
            value
        })
    }
})

const isDark = useDark()
const themeChange = () => {
    settingStore.setTheme(isDark.value)
}

const toggleDark = () => {
    useToggle(isDark)()
    themeChange()
}
const resetTheme = () => {
    isDark.value = false
    settingStore.resetTheme()
    themeChange()
}
</script>

<style lang="scss" scoped>
.setting-item {
    > span,
    > div:first-child {
        font-size: var(--el-font-size-base);
    }
}

.style-card {
    position: relative;
    width: 84px;
    height: 58px;
    margin-right: 12px;
    padding: 6px 6px 0;
    border: 1px solid var(--el-border-color-light);
    border-radius: var(--radius-md);
    overflow: hidden;
    display: flex;
    flex-direction: column;
    gap: 4px;
    transition:
        border-color var(--duration-base) var(--ease-out),
        box-shadow var(--duration-base) var(--ease-out);

    &__bar {
        height: 7px;
        border-radius: 2px;
    }
    &__rail {
        position: absolute;
        left: 6px;
        top: 17px;
        bottom: 14px;
        width: 16px;
        border-radius: 2px;
    }
    &__body {
        position: absolute;
        right: 6px;
        left: 26px;
        top: 17px;
        bottom: 14px;
        border-radius: 2px;
    }
    &__label {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        height: 14px;
        line-height: 14px;
        text-align: center;
        font-size: 10px;
        color: var(--el-text-color-secondary);
    }

    &--dark {
        background-color: var(--shell-800);
        .style-card__bar {
            background-color: var(--shell-900);
        }
        .style-card__rail {
            background-color: rgba(255, 255, 255, 0.14);
        }
        .style-card__body {
            background-color: rgba(255, 255, 255, 0.07);
        }
    }
    &--light {
        background-color: var(--neutral-0);
        .style-card__bar {
            background-color: var(--neutral-75);
        }
        .style-card__rail {
            background-color: var(--neutral-100);
        }
        .style-card__body {
            background-color: var(--neutral-50);
        }
    }

    &.is-active {
        border-color: var(--el-color-primary);
        box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.12);
        .style-card__label {
            color: var(--el-color-primary);
            font-weight: 600;
        }
    }
}

.icon-select {
    position: absolute;
    left: 50%;
    top: 46%;
    transform: translate(-50%, -50%);
    color: var(--el-color-primary);
    z-index: 1;
}
</style>
