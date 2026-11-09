<template>
  <div class="profile-page">
    <el-card shadow="never">
      <template #header>
        <div class="profile-header">
          <span>个人中心</span>
          <el-button type="primary" link @click="goSettings">系统设置</el-button>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="账号">{{ account || '-' }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ root ? '超级管理员' : '普通管理员' }}</el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ userId || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-alert
        class="profile-tip"
        type="info"
        :closable="false"
        title="修改密码、通知偏好等账号能力请使用「系统设置 → 安全设置」；本页仅展示当前登录身份。"
      />
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const userInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}') || {}
  } catch {
    return {}
  }
})

const account = computed(() => userInfo.value.account || userInfo.value.username || '')
const name = computed(() => userInfo.value.name || '')
const userId = computed(() => userInfo.value.id ?? userInfo.value.userId ?? '')
const root = computed(() => Number(userInfo.value.root ?? 0) === 1)

const goSettings = () => {
  router.push('/setting/system/basic').catch(() => {})
}
</script>

<style scoped>
.profile-page {
  padding: 20px;
}
.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
}
.profile-tip {
  margin-top: 16px;
}
</style>
