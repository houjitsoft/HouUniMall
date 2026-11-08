<template>
  <div class="ops-page">
    <div class="card">
      <div class="header">
        <div>
          <h1>Java 服务管理</h1>
          <p class="subtitle">类似 IDEA Services：查看 jar 进程、单服务启停/重启，或一键打包重启（操作前均先 git pull）</p>
        </div>
        <span class="badge" :class="statusClass">{{ statusText }}</span>
      </div>

      <el-form inline class="token-bar" @submit.prevent>
        <el-form-item label="管理员令牌">
          <el-input
            v-model="adminToken"
            type="password"
            show-password
            placeholder="默认 REPLACE_WITH_YOUR_ADMIN_TOKEN（与 bff-web restart.admin.token 一致）"
            style="width: 320px"
            @keyup.enter="onTokenEnter"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveToken">保存并加载</el-button>
        </el-form-item>
      </el-form>
      <p v-if="tokenError" class="token-error">{{ tokenError }}</p>

      <el-tabs v-model="activeTab" class="ops-tabs">
        <el-tab-pane label="服务列表" name="services">
          <ServiceManage
            ref="serviceManageRef"
            embedded
            :external-token="adminToken"
            @load-error="onLoadError"
            @load-success="tokenError = ''"
          />
        </el-tab-pane>
        <el-tab-pane label="打包重启" name="restart">
          <RestartPanel
            :admin-token="adminToken"
            @started="onRestartStarted"
            @finished="onRestartFinished"
          />
        </el-tab-pane>
        <el-tab-pane label="前端打包" name="frontend">
          <FrontendBuildPanel
            :admin-token="adminToken"
            @started="onFrontendStarted"
            @finished="onFrontendFinished"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import ServiceManage from '@/views/admin/ServiceManage.vue'
import RestartPanel from '@/views/admin/RestartPanel.vue'
import FrontendBuildPanel from '@/views/admin/FrontendBuildPanel.vue'
import { saveAdminToken, loadAdminToken } from '@/api/admin/serviceManage'

const adminToken = ref(loadAdminToken())
const tokenError = ref('')
const activeTab = ref('services')
const serviceManageRef = ref(null)
const restartRunning = ref(false)
const frontendRunning = ref(false)

const statusClass = computed(() => ((restartRunning.value || frontendRunning.value) ? 'running' : 'idle'))
const statusText = computed(() => {
  if (restartRunning.value) return '打包重启中'
  if (frontendRunning.value) return '前端打包中'
  return '就绪'
})

function saveToken() {
  if (!adminToken.value?.trim()) {
    ElMessage.warning('请输入管理员令牌')
    return
  }
  adminToken.value = adminToken.value.trim()
  saveAdminToken(adminToken.value)
  tokenError.value = ''
  ElMessage.success('令牌已保存')
  nextTick(() => serviceManageRef.value?.loadList?.())
}

function onTokenEnter() {
  saveToken()
}

function onLoadError(msg) {
  if (msg && (msg.includes('鉴权失败') || msg.includes('X-Admin-Token'))) {
    tokenError.value = '令牌错误：请在上方填写正确的 X-Admin-Token（默认 REPLACE_WITH_YOUR_ADMIN_TOKEN）。注意 /web/admin/services 是 API 地址，不能直接在浏览器打开。'
  }
}

onMounted(() => {
  if (adminToken.value?.trim()) {
    nextTick(() => serviceManageRef.value?.loadList?.())
  }
})

function onRestartStarted() {
  restartRunning.value = true
}

function onRestartFinished(success) {
  restartRunning.value = false
  if (success) {
    activeTab.value = 'services'
    setTimeout(() => serviceManageRef.value?.loadList?.(), 2000)
  }
}

function onFrontendStarted() {
  frontendRunning.value = true
}

function onFrontendFinished(success) {
  frontendRunning.value = false
  if (success) {
    ElMessage.success('前端静态资源已更新，刷新页面即可')
  }
}
</script>

<style scoped>
.ops-page {
  min-height: 100vh;
  padding: 24px;
  padding-bottom: 48px;
  background: #f0f2f5;
  box-sizing: border-box;
}
.card {
  max-width: 1400px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
  gap: 12px;
}
.header h1 {
  margin: 0 0 4px;
  font-size: 22px;
}
.subtitle {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
.badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  flex-shrink: 0;
}
.badge.idle { background: #f0f2f5; color: #606266; }
.badge.running { background: #ecf5ff; color: #409eff; }
.token-bar {
  margin-bottom: 8px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
}
.token-error {
  margin: 0 0 12px;
  padding: 8px 12px;
  background: #fef0f0;
  color: #f56c6c;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.5;
}
.ops-tabs :deep(.el-tabs__content) {
  padding-top: 8px;
}
</style>
