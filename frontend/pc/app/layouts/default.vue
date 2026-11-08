<template>
    <div class="layout-wrapper">
        <!-- 头部 -->
        <Header />

        <!-- 主体内容 -->
        <main class="main-content">
            <NuxtPage />
        </main>

        <!-- PC 右侧快捷工具栏（含回到顶部） -->
        <RightSideNavigation />

        <!-- 底部 -->
        <Footer />
    </div>
</template>

<script setup lang="ts">
useAppMeta()
</script>

<style lang="scss">
.layout-wrapper {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: var(--bg-page, #f4f5f7);
}

.main-content {
    flex: 1;
    max-width: var(--container-width, 1280px);
    width: 100%;
    margin: 0 auto;
    padding: 0;
}

// 响应式设计
@media (max-width: 768px) {
    .main-content {
        padding: 20px 15px;
    }
}
</style>
