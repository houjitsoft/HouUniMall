<template>
    <div class="layout-wrapper">
        <!-- 头部 -->
        <Header />

        <!-- 主体内容：全宽，不限制 1200px -->
        <main class="main-content">
            <NuxtPage />
        </main>

        <!-- 回到顶部按钮 -->
        <Backtop />

        <!-- 底部 -->
        <Footer />
    </div>
</template>

<script setup lang="ts">
useAppMeta()
</script>

<style lang="scss" scoped>
.layout-wrapper {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #fff;
}

.main-content {
    flex: 1;
    width: 100%;
    max-width: none;
    padding: 0;
}
</style>
