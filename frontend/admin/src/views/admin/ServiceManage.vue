<template>
  <div class="service-manage-page" :class="{ standalone, embedded }">
    <div class="card" :class="{ 'card-embedded': embedded }">
      <div class="header" v-if="!embedded">
        <div>
          <h1>Java 服务管理</h1>
          <p class="subtitle">查看本机 jar 进程状态，支持单服务启停/重启（类似 IDEA Services）</p>
        </div>
        <div class="stats" v-if="summary.total">
          <el-tag type="success">运行 {{ summary.running }}</el-tag>
          <el-tag type="info">启动中 {{ summary.starting }}</el-tag>
          <el-tag type="danger">停止 {{ summary.stopped }}</el-tag>
          <span class="refresh-time" v-if="lastRefresh">刷新于 {{ lastRefresh }}</span>
        </div>
      </div>

      <el-form v-if="!embedded" inline class="token-bar" @submit.prevent>
        <el-form-item label="管理员令牌">
          <el-input
            v-model="adminToken"
            type="password"
            show-password
            placeholder="X-Admin-Token"
            style="width: 280px"
            @keyup.enter="loadList"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadList">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
          <el-button type="success" :loading="bulkLoading === 'start'" @click="handleStartAll">全部启动</el-button>
          <el-button type="danger" :loading="bulkLoading === 'stop'" @click="handleStopAll">全部停止</el-button>
          <el-link v-if="standalone" type="primary" href="/restartall" style="margin-left: 12px">一键打包重启</el-link>
        </el-form-item>
      </el-form>

      <el-input
        v-model="keyword"
        placeholder="搜索服务名"
        clearable
        class="search-input"
        :prefix-icon="Search"
      />

      <el-table
        v-loading="loading"
        :data="filteredList"
        stripe
        border
        size="small"
        style="width: 100%"
        row-key="name"
      >
        <el-table-column prop="name" label="服务" width="120" fixed />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pid" label="PID" width="90">
          <template #default="{ row }">{{ row.pid ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="端口" width="100">
          <template #default="{ row }">
            <span v-if="row.port">{{ row.port }}</span>
            <span v-else>-</span>
            <el-tag v-if="row.port" :type="row.portUp ? 'success' : 'warning'" size="small" style="margin-left: 4px">
              {{ row.portUp ? 'UP' : 'DOWN' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memoryMb" label="内存(MB)" width="100">
          <template #default="{ row }">{{ row.memoryMb ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="startTime" label="启动时间" min-width="160">
          <template #default="{ row }">{{ row.startTime ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="jarModified" label="JAR 更新时间" min-width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'running' && !isRowStarting(row)"
              type="warning"
              size="small"
              :loading="isActionLoading(row.name, 'stop')"
              @click="handleStop(row)"
            >停止</el-button>
            <el-button
              v-else-if="isRowStarting(row) || isActionLoading(row.name, 'start')"
              type="success"
              size="small"
              loading
              disabled
            >启动中</el-button>
            <el-button
              v-else
              type="success"
              size="small"
              :loading="isActionLoading(row.name, 'start')"
              @click="handleStart(row)"
            >启动</el-button>
            <el-button
              type="primary"
              size="small"
              :loading="isActionLoading(row.name, 'restart')"
              @click="handleRestart(row)"
            >重启</el-button>
            <el-button type="info" size="small" link @click="openLog(row)">日志</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="logVisible" :title="'日志 — ' + (logService?.name || '')" width="860px" top="5vh">
      <pre class="log-box">{{ logText || '（暂无日志）' }}</pre>
      <template #footer>
        <el-button @click="refreshLog" :loading="logLoading">刷新日志</el-button>
        <el-button type="primary" @click="logVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import {
  fetchServiceList,
  fetchServiceStatus,
  startService,
  stopService,
  restartService,
  fetchServiceLog,
  startAllServices,
  stopAllServices,
  saveAdminToken,
  loadAdminToken
} from '@/api/admin/serviceManage'

const props = defineProps({
  /** 独立页（/admin/services）时为 true，带全屏样式 */
  standalone: { type: Boolean, default: false },
  /** 嵌入 /restartall 时为 true，隐藏标题与令牌栏 */
  embedded: { type: Boolean, default: false },
  /** 父组件传入的管理员令牌 */
  externalToken: { type: String, default: '' }
})

const emit = defineEmits(['load-error', 'load-success'])

const adminToken = ref(loadAdminToken())
const loading = ref(false)
const bulkLoading = ref('')
/** 多服务并行操作时各自 loading（Set 存 name:action） */
const actionLoadingSet = ref(new Set())
/** 启动后待轮询直到 running 的服务名 */
const pollingNames = ref(new Set())
const serviceList = ref([])
const keyword = ref('')
const lastRefresh = ref('')
const logVisible = ref(false)
const logService = ref(null)
const logText = ref('')
const logLoading = ref(false)

let autoTimer = null
let statusPollTimer = null

/**
 * 判断某行操作是否 loading
 * @param {string} name 服务名
 * @param {string} action start|stop|restart
 * @returns {boolean}
 */
function isActionLoading(name, action) {
  return actionLoadingSet.value.has(`${name}:${action}`)
}

/**
 * 设置/清除单行操作 loading
 * @param {string} name 服务名
 * @param {string} action 操作类型
 * @param {boolean} on 是否 loading
 */
function setActionLoading(name, action, on) {
  const key = `${name}:${action}`
  const next = new Set(actionLoadingSet.value)
  if (on) next.add(key)
  else next.delete(key)
  actionLoadingSet.value = next
}

/**
 * 行是否处于启动中（含后台轮询）
 * @param {object} row 服务行
 * @returns {boolean}
 */
function isRowStarting(row) {
  return row.status === 'starting' || pollingNames.value.has(row.name)
}

/**
 * 加入启动状态轮询
 * @param {string} name 服务名
 */
function addPolling(name) {
  pollingNames.value = new Set([...pollingNames.value, name])
  ensureStatusPollTimer()
}

/**
 * 移除启动状态轮询
 * @param {string} name 服务名
 */
function removePolling(name) {
  const next = new Set(pollingNames.value)
  next.delete(name)
  pollingNames.value = next
}

/**
 * 启动后轮询单服务状态直到 running 或 stopped
 */
async function pollWatchingServices() {
  if (!tokenValue() || pollingNames.value.size === 0) return
  for (const name of [...pollingNames.value]) {
    try {
      const { data } = await fetchServiceStatus(tokenValue(), name)
      if (data.code !== 200 || !data.data) continue
      patchServiceRow(name, data.data)
      if (data.data.status === 'running') {
        removePolling(name)
        ElMessage.success(`${name} 启动成功`)
      } else if (data.data.status === 'stopped') {
        removePolling(name)
      }
    } catch {
      /* 轮询失败忽略，下次再试 */
    }
  }
}

/** 确保 3 秒轮询定时器已启动 */
function ensureStatusPollTimer() {
  if (statusPollTimer) return
  statusPollTimer = setInterval(() => {
    if (pollingNames.value.size > 0) pollWatchingServices()
  }, 3000)
}

const filteredList = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return serviceList.value
  return serviceList.value.filter(s => (s.name || '').toLowerCase().includes(k))
})

const summary = computed(() => {
  const list = serviceList.value
  return {
    total: list.length,
    running: list.filter(s => s.status === 'running').length,
    starting: list.filter(s => s.status === 'starting').length,
    stopped: list.filter(s => s.status === 'stopped').length
  }
})

function statusTag(status) {
  return { running: 'success', starting: 'warning', stopped: 'danger' }[status] || 'info'
}

function statusText(status) {
  return { running: '运行中', starting: '启动中', stopped: '已停止' }[status] || status
}

function tokenValue() {
  if (props.embedded && props.externalToken?.trim()) {
    return props.externalToken.trim()
  }
  return adminToken.value?.trim() || ''
}

function ensureToken() {
  const t = tokenValue()
  if (!t) {
    ElMessage.warning('请先在上方输入管理员令牌')
    return false
  }
  saveAdminToken(t)
  if (!props.embedded) adminToken.value = t
  return true
}

async function loadList() {
  if (!ensureToken()) return
  loading.value = true
  try {
    const { data } = await fetchServiceList(tokenValue())
    if (data.code !== 200) {
      const msg = data.msg || data.message || '加载失败'
      ElMessage.error(msg)
      emit('load-error', msg)
      return
    }
    serviceList.value = Array.isArray(data.data) ? data.data : []
    lastRefresh.value = new Date().toLocaleTimeString()
    emit('load-success')
  } catch (e) {
    const msg = e.response?.data?.message || e.message
    ElMessage.error('加载失败：' + msg)
    emit('load-error', msg)
  } finally {
    loading.value = false
  }
}

/**
 * 用接口返回的单条服务数据更新表格行，避免全量 loadList
 * @param {string} name 服务名
 * @param {object} row 最新服务状态
 */
function patchServiceRow(name, row) {
  if (!name || !row) return
  const idx = serviceList.value.findIndex(s => s.name === name)
  if (idx >= 0) {
    serviceList.value[idx] = { ...serviceList.value[idx], ...row }
  }
}

/**
 * 批量合并服务状态（全部启停接口返回 services 数组时使用）
 * @param {object[]} rows 服务状态列表
 */
function patchServiceList(rows) {
  if (!Array.isArray(rows) || rows.length === 0) return
  const map = new Map(rows.map(r => [r.name, r]))
  serviceList.value = serviceList.value.map(s => (map.has(s.name) ? { ...s, ...map.get(s.name) } : s))
}

async function handleStart(row) {
  if (!ensureToken()) return
  setActionLoading(row.name, 'start', true)
  patchServiceRow(row.name, { status: 'starting', portUp: false })
  addPolling(row.name)
  try {
    const { data } = await startService(tokenValue(), row.name)
    if (data.code === 200) {
      const svc = data.data?.service
      if (svc) patchServiceRow(row.name, svc)
      if (svc?.status === 'running') {
        removePolling(row.name)
        ElMessage.success(`${row.name} 启动成功`)
      } else {
        ElMessage.info(`${row.name} 正在启动…`)
      }
    } else {
      removePolling(row.name)
      patchServiceRow(row.name, { status: 'stopped' })
      ElMessage.error(data.msg || '启动失败')
    }
  } catch (e) {
    removePolling(row.name)
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    setActionLoading(row.name, 'start', false)
  }
}

async function handleStop(row) {
  if (!ensureToken()) return
  try {
    await ElMessageBox.confirm(`确定停止 ${row.name}？`, '确认', { type: 'warning' })
  } catch { return }
  removePolling(row.name)
  setActionLoading(row.name, 'stop', true)
  try {
    const { data } = await stopService(tokenValue(), row.name)
    if (data.code === 200) {
      ElMessage.success(data.msg || '已停止')
      patchServiceRow(row.name, data.data?.service)
    } else {
      ElMessage.error(data.msg || '停止失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    setActionLoading(row.name, 'stop', false)
  }
}

async function handleRestart(row) {
  if (!ensureToken()) return
  try {
    await ElMessageBox.confirm(
      `确定重启 ${row.name}？\n将先 git pull；若该服务有代码变更则自动 Maven 编译再重启（可能 2～5 分钟），否则约 10～60 秒。`,
      '确认',
      { type: 'warning' }
    )
  } catch { return }
  setActionLoading(row.name, 'restart', true)
  patchServiceRow(row.name, { status: 'starting', portUp: false })
  addPolling(row.name)
  try {
    const { data } = await restartService(tokenValue(), row.name)
    if (data.code === 200) {
      const rebuilt = data.data?.rebuilt === true
      const svc = data.data?.service
      if (svc) patchServiceRow(row.name, svc)
      if (svc?.status === 'running') {
        removePolling(row.name)
        ElMessage.success(rebuilt ? `${row.name} 已编译并重启` : `${row.name} 重启成功`)
      } else {
        ElMessage.info(rebuilt ? `${row.name} 已编译，正在启动…` : `${row.name} 正在重启…`)
      }
    } else {
      removePolling(row.name)
      ElMessage.error(data.msg || '重启失败')
    }
  } catch (e) {
    removePolling(row.name)
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    setActionLoading(row.name, 'restart', false)
  }
}

async function handleStartAll() {
  if (!ensureToken()) return
  try {
    await ElMessageBox.confirm('确定启动全部 jar 服务？可能需要数分钟。', '确认', { type: 'warning' })
  } catch { return }
  bulkLoading.value = 'start'
  try {
    const { data } = await startAllServices(tokenValue())
    if (data.code === 200) {
      ElMessage.success('全部启动指令已执行')
      patchServiceList(data.data?.services)
      const rows = data.data?.services
      if (Array.isArray(rows)) {
        rows.forEach(s => {
          if (s.status === 'starting') addPolling(s.name)
        })
      }
    } else {
      ElMessage.error(data.msg || '失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    bulkLoading.value = ''
  }
}

async function handleStopAll() {
  if (!ensureToken()) return
  try {
    await ElMessageBox.confirm('确定停止全部 jar 服务？网关也会停止。', '危险操作', { type: 'error' })
  } catch { return }
  bulkLoading.value = 'stop'
  try {
    const { data } = await stopAllServices(tokenValue())
    if (data.code === 200) {
      ElMessage.success('已全部停止')
      patchServiceList(data.data?.services)
    } else {
      ElMessage.error(data.msg || '失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    bulkLoading.value = ''
  }
}

async function openLog(row) {
  logService.value = row
  logVisible.value = true
  await refreshLog()
}

async function refreshLog() {
  if (!logService.value || !ensureToken()) return
  logLoading.value = true
  try {
    const { data } = await fetchServiceLog(tokenValue(), logService.value.name, 300)
    if (data.code === 200 && data.data) {
      logText.value = data.data.text || ''
    } else {
      logText.value = data.msg || '读取失败'
    }
  } catch (e) {
    logText.value = e.message
  } finally {
    logLoading.value = false
  }
}

onMounted(() => {
  if (tokenValue()) loadList()
  autoTimer = setInterval(() => {
    if (!tokenValue() || loading.value) return
    if (pollingNames.value.size > 0) return
    if (actionLoadingSet.value.size === 0) loadList()
  }, 30000)
  ensureStatusPollTimer()
})

watch(() => props.externalToken, (v, old) => {
  if (props.embedded && v?.trim() && v !== old) loadList()
})

defineExpose({ loadList })

onUnmounted(() => {
  if (autoTimer) clearInterval(autoTimer)
  if (statusPollTimer) clearInterval(statusPollTimer)
})
</script>

<style scoped>
.service-manage-page {
  padding: 16px;
}
.service-manage-page.standalone {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24px;
}
.service-manage-page.embedded {
  padding: 0;
}
.service-manage-page.embedded .card-embedded {
  box-shadow: none;
  padding: 0;
  background: transparent;
}
.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
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
.stats {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.refresh-time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}
.token-bar {
  margin-bottom: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
}
.search-input {
  margin-bottom: 12px;
  max-width: 320px;
}
.log-box {
  max-height: 55vh;
  overflow: auto;
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
