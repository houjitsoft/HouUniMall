<template>
  <div class="test-page">
    <h1>网站设置</h1>
    <p>这是一个测试页面</p>
    <el-button type="primary" @click="testClick">测试按钮</el-button>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const testClick = () => {
  ElMessage.success('测试成功！')
}
</script>

<style scoped>
.test-page {
  padding: 20px;
}
</style>
