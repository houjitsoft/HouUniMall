<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="card-title">财务统计</span></template>
      <el-empty description="功能开发中..." />
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
const loading = ref(false)
onMounted(() => { loading.value = false })
</script>
<style scoped>
.page-container { padding: 20px; }
.card-title { font-weight: bold; }
</style>
