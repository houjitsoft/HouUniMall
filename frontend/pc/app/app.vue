<template>
    <UApp>
        <!-- hydration 前固定 cn，避免 localStorage 语言在首帧改 key 导致整树 mismatch -->
        <NuxtLayout :key="layoutLocaleKey">
            <NuxtPage />
        </NuxtLayout>
    </UApp>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMallStore } from '~/stores/modules/mall'

const mallStore = useMallStore()
const localeReady = ref(false)
onMounted(() => {
    localeReady.value = true
})

// 语言切换时整树重挂载，确保全局 $t 重新求值（避免仅改 store 不刷新模板）
const layoutLocaleKey = computed(() => {
    if (!localeReady.value) return 'cn'
    return mallStore.getLanguageCode || 'cn'
})
</script>
