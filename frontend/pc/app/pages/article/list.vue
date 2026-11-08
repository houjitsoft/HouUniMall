<script setup lang="ts">
/**
 * 导航「头条资讯」历史路径为 /article/list，实际资讯中心为 /information。
 * 静态托管下无此页 HTML 时会回落到首页，故保留本路由并跳转。
 */
await navigateTo('/information', { replace: true, redirectCode: 301 })
</script>

<template>
    <div class="article-list-redirect">{{ $t('正在进入资讯中心...') }}</div>
</template>

<style scoped>
.article-list-redirect {
    min-height: 40vh;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #666;
    font-size: 14px;
}
</style>
