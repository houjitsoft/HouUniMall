<template>
  <el-dropdown trigger="click" :placement="placement" @command="handleCommand">
    <span class="user-info hc-header-chip hc-header-chip--user">
      <el-avatar :size="isCollapse ? 28 : 30" :src="userAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
      <span v-if="!isCollapse" class="username" :title="username || t('user.admin')">{{ username || t('user.admin') }}</span>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="profile">{{ t('user.profile') }}</el-dropdown-item>
        <el-dropdown-item command="settings">{{ t('user.settings') }}</el-dropdown-item>
        <el-dropdown-item command="logout" divided>{{ t('user.logout') }}</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { useAuthStore } from '@/stores/auth'
import { appUrl } from '@/utils/appBase'

const authStore = useAuthStore()

// 定义组件属性
const props = defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  },
  // 下拉弹出方向（侧栏底部使用时传 top）
  placement: {
    type: String,
    default: 'bottom-end'
  }
})

// 定义事件
const emit = defineEmits(['command'])

// 计算属性：用户名
const username = computed(() => {
  return authStore.userInfo?.userName || authStore.userInfo?.userMobile || ''
})

// 计算属性：用户头像
const userAvatar = computed(() => {
  return authStore.userInfo?.userAvatar || ''
})

// 用户下拉菜单处理
const handleCommand = (command) => {
  if (command === 'logout') {
    // 处理退出登录
    authStore.logout()
    // 跳转到登录页面（子路径部署下需带 /admin 前缀）
    window.location.href = appUrl('/login')
  }
  emit('command', command)
}
</script>

<style scoped>
.user-info {
  display: inline-flex;
  align-items: center;
  outline: none;
}

.username {
  margin-left: 0;
  line-height: 1;
}
</style>