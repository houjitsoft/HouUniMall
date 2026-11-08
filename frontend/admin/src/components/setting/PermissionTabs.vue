<template>
  <div class="permission-tabs">
    <div
      v-for="tab in tabs"
      :key="tab.key"
      class="tab-item"
      :class="{ active: activeTab === tab.key }"
      @click="handleTabClick(tab.key)"
    >
      <div v-if="activeTab === tab.key" class="tab-dot" />
      {{ tab.label }}
      <el-icon class="close-icon" @click.stop="handleClose(tab.key)"><Close /></el-icon>
    </div>
    <el-icon class="more-icon"><ArrowDown /></el-icon>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Close, ArrowDown } from '@element-plus/icons-vue'
import { PERMISSION_TAB_ROUTES, getActivePermissionTab } from '@/utils/permissionTabRoutes'

const tabs = [
  { key: 'workbench', label: '工作台' },
  { key: 'menu', label: '菜单' },
  { key: 'role', label: '角色' },
  { key: 'member', label: '会员管理' },
  { key: 'admin', label: '管理员' }
]

const route = useRoute()
const router = useRouter()
const activeTab = computed(() => getActivePermissionTab(route.path))

const handleTabClick = (key) => {
  const path = PERMISSION_TAB_ROUTES[key]
  if (!path) return
  if (route.path === path) return
  router.push(path).catch((err) => {
    if (err?.name !== 'NavigationDuplicated') {
      console.error('权限标签跳转失败:', err)
    }
  })
}

const handleClose = (key) => {
  if (key === activeTab.value) {
    router.push('/dashboard').catch(() => {})
  }
}
</script>

<style scoped>
.permission-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 16px;
  height: 40px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  overflow-x: auto;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  border-radius: 4px 4px 0 0;
  white-space: nowrap;
  user-select: none;
}

.tab-item:hover {
  color: #409eff;
  background: #f5f7fa;
}

.tab-item.active {
  color: #409eff;
  background: #ecf5ff;
  border-bottom: 2px solid #409eff;
}

.tab-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
}

.close-icon {
  font-size: 12px;
  color: #909399;
}

.close-icon:hover {
  color: #f56c6c;
}

.more-icon {
  margin-left: auto;
  color: #909399;
  flex-shrink: 0;
}
</style>
