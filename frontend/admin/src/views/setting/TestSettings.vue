<template>
  <div class="test-settings">
    <h1>网站设置测试页面</h1>
    <p>如果你能看到这个页面，说明路由正常工作</p>
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
.test-settings {
  padding: 20px;
}
</style>
