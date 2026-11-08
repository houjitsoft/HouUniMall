<template>
  <div class="not-found">
    <el-result icon="warning" title="页面不存在" :sub-title="`路径 ${$route.fullPath} 未注册或前端未更新`">
      <template #extra>
        <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
        <el-button @click="$router.back()">返回上一页</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.not-found {
  padding: 48px 24px;
}
</style>
