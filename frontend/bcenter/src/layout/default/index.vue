<template>
    <div class="layout-default">
        <layout-aside />

        <div class="layout-body">
            <layout-topbar />
            <layout-tabsbar />

            <div class="layout-main">
                <layout-main />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import LayoutMain from './components/main.vue'
import LayoutTopbar from './components/topbar.vue'
import LayoutTabsbar from './components/tabsbar.vue'
import LayoutAside from './components/aside/index.vue'
</script>

<style lang="scss" scoped>
.layout-default {
    display: flex;
    height: 100vh;
    width: 100%;
    background-color: var(--el-bg-color-page);
}

.layout-body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
}

.layout-main {
    flex: 1;
    min-height: 0;
}
</style>
