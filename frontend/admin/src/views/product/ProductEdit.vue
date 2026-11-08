<template>
  <div class="product-edit">
    <h2>编辑商品页面</h2>
    <p>这里是编辑商品内容</p>
    <p>商品ID: {{ id }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 接收路由参数
const props = defineProps({
  id: {
    type: String,
    required: true
  }
})

// 编辑商品页面逻辑
</script>

<style scoped>
.product-edit {
  padding: 20px;
}
</style>