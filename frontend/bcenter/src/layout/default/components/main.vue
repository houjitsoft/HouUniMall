<template>
    <main class="main-wrap">
        <el-scrollbar>
            <div class="main-inner">
                <router-view v-if="isRouteShow" v-slot="{ Component, route }">
                    <keep-alive :include="includeList" :max="20">
                        <component :is="Component" :key="route.fullPath" />
                    </keep-alive>
                </router-view>
            </div>
        </el-scrollbar>
    </main>
</template>

<script setup lang="ts">
import useAppStore from '@/stores/modules/app'
import useTabsStore from '@/stores/modules/multipleTabs'
import useSettingStore from '@/stores/modules/setting'
const appStore = useAppStore()
const tabsStore = useTabsStore()
const settingStore = useSettingStore()
const isRouteShow = computed(() => appStore.isRouteShow)
const includeList = computed(() => (settingStore.openMultipleTabs ? tabsStore.getCacheTabList : []))
</script>

<style lang="scss" scoped>
.main-wrap {
    height: 100%;
    background-color: var(--el-bg-color-page);
}

.main-inner {
    padding: var(--space-page);
}

@media (max-width: 768px) {
    .main-inner {
        padding: 12px;
    }
}
</style>
