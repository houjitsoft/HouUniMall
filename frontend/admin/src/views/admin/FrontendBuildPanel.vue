<template>
  <div class="frontend-panel">
    <div class="form" v-if="!running">
      <p class="intro">每次打包前会<strong>先 git pull</strong> 拉取最新代码，再执行 <code>npm run build</code> 输出到 <code>public/</code>。不会停止或重启 Java 服务。</p>

      <div class="toolbar">
        <el-button link type="primary" @click="selectAll">全选</el-button>
        <el-button link @click="selectNone">清空</el-button>
      </div>

      <el-checkbox-group v-model="selected" class="project-grid">
        <el-checkbox
          v-for="p in projects"
          :key="p.key"
          :label="p.key"
          border
          class="project-item"
        >
          <div class="project-label">{{ p.label }}</div>
          <div class="project-path">{{ p.path }}</div>
        </el-checkbox>
      </el-checkbox-group>

      <el-form label-width="0" class="actions">
        <el-button
          type="primary"
          size="large"
          :loading="triggering"
          :disabled="selected.length === 0"
          @click="triggerBuild(false)"
        >
          打包选中项目（{{ selected.length }}）
        </el-button>
        <el-button size="large" :loading="triggering" @click="triggerBuild(true)">
          打包全部已有项目
        </el-button>
        <el-button @click="clearLog">清空日志</el-button>
      </el-form>
    </div>

    <div class="status-bar" v-if="running || finished">
      <span>任务 ID：{{ stamp }}</span>
      <span v-if="elapsed">耗时：{{ formatDuration(elapsed) }}</span>
      <span v-if="finished" class="ok">✓ 已完成</span>
    </div>

    <pre class="log-box" ref="logBoxRef">{{ logText || '（暂无日志）' }}</pre>

    <div class="hint">
      <p><strong>说明：</strong>仅执行 <code>jar/frontend-build.ps1</code>（git pull + npm build），<strong>不会出现 bff-web / Maven / 停服</strong> 等后端步骤。</p>
      <p>Cloud 管理后台产物：<code>public/admin/</code>；<strong>商户端（商家中心，主站核心模块）</strong>：<code>public/bcenter/</code>。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted, nextTick } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const props = defineProps({
  adminToken: { type: String, required: true }
})

const emit = defineEmits(['finished', 'started'])

/**
 * 只保留发布机上真实存在的工程（frontend/mall/* 目录已平铺撤销，geo / pay 不存在）。
 * 商户端（bcenter）是主站下的核心模块：源码 frontend/bcenter、产物 public/bcenter。
 */
const projects = [
  { key: 'cloud', label: 'Cloud 管理后台', path: 'frontend/admin' },
  { key: 'bcenter', label: '商户端（商家中心）', path: 'frontend/bcenter' }
]

const selected = ref(['cloud'])
const triggering = ref(false)
const running = ref(false)
const finished = ref(false)
const stamp = ref('')
const offset = ref(0)
const logText = ref('')
const startedAt = ref(0)
const elapsed = ref(0)
const logBoxRef = ref(null)
let pollTimer = null
let elapsedTimer = null

/**
 * 格式化毫秒为「分 秒」
 * @param {number} ms 毫秒
 * @returns {string}
 */
function formatDuration(ms) {
  const s = Math.floor(ms / 1000)
  const m = Math.floor(s / 60)
  return `${m} 分 ${s % 60} 秒`
}

/**
 * 全选所有前端项目 key
 */
function selectAll() {
  selected.value = projects.map(p => p.key)
}

/**
 * 清空选中
 */
function selectNone() {
  selected.value = []
}

/**
 * 触发前端打包任务
 * @param {boolean} allExisting 是否打包全部已有目录（不传 frontends 列表）
 */
async function triggerBuild(allExisting) {
  if (!props.adminToken?.trim()) {
    ElMessage.warning('请先在上方输入管理员令牌')
    return
  }
  if (!allExisting && selected.value.length === 0) {
    ElMessage.warning('请至少选择一个前端项目')
    return
  }
  triggering.value = true
  try {
    const body = {
      frontendOnly: true,
      forceFrontend: true
    }
    if (!allExisting) {
      body.frontends = [...selected.value]
    }
    const { data } = await axios.post('/web/admin/restart', body, {
      headers: { 'X-Admin-Token': props.adminToken.trim() },
      timeout: 30000
    })
    if (data.code !== 200) {
      ElMessage.error(data.msg || '触发失败')
      return
    }
    stamp.value = data.data.stamp
    offset.value = 0
    logText.value = ''
    running.value = true
    finished.value = false
    emit('started')
    startedAt.value = Date.now()
    elapsedTimer = setInterval(() => { elapsed.value = Date.now() - startedAt.value }, 1000)
    startPolling()
    ElMessage.success(`前端打包任务已下发 (${stamp.value})`)
  } catch (e) {
    const st = e.response?.status
    if (st === 502 || st === 503 || st === 504 || e.code === 'ERR_NETWORK') {
      ElMessage.error('无法连接 bff-web，请检查网关是否运行')
    } else {
      ElMessage.error('触发失败：' + (e.response?.data?.message || e.message))
    }
  } finally {
    triggering.value = false
  }
}

function startPolling() {
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = setInterval(pollLog, 2000)
  pollLog()
}

async function pollLog() {
  if (!stamp.value) return
  try {
    const { data } = await axios.get('/web/admin/restart/log', {
      params: { stamp: stamp.value, offset: offset.value },
      headers: { 'X-Admin-Token': props.adminToken.trim() },
      timeout: 15000
    })
    if (data?.code === 200 && data.data) {
      const d = data.data
      if (d.text) {
        logText.value += d.text
        offset.value = d.offset
        nextTick(() => {
          if (logBoxRef.value) logBoxRef.value.scrollTop = logBoxRef.value.scrollHeight
        })
      }
      if (d.finished) {
        finished.value = true
        running.value = false
        stopTimers()
        emit('finished', d.success !== false)
        if (d.success === false) {
          ElMessage.error('打包结束但有失败步骤，请查看日志')
        } else {
          ElMessage.success('前端打包已完成')
        }
      }
    }
  } catch (e) {
    appendSystemLine(`[轮询] 暂时无法连接 (${e.response?.status || e.code || e.message})，继续重试…`)
  }
}

function appendSystemLine(line) {
  const last = logText.value.split('\n').slice(-2).join('\n')
  if (!last.includes(line)) logText.value += `\n${line}`
}

function stopTimers() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
  if (elapsedTimer) { clearInterval(elapsedTimer); elapsedTimer = null }
}

function clearLog() {
  logText.value = ''
  offset.value = 0
}

onUnmounted(() => stopTimers())
</script>

<style scoped>
.intro {
  margin: 0 0 12px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}
.project-item {
  margin: 0 !important;
  height: auto;
  padding: 10px 12px;
}
.project-item :deep(.el-checkbox__label) {
  white-space: normal;
  line-height: 1.4;
}
.project-label {
  font-weight: 600;
  color: #303133;
}
.project-path {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.actions {
  margin-bottom: 8px;
}
.status-bar {
  display: flex; gap: 24px; padding: 12px; background: #fafafa;
  border-radius: 4px; margin-bottom: 12px; font-size: 13px; color: #606266;
}
.status-bar .ok { color: #67c23a; font-weight: bold; }
.log-box {
  background: #1e1e1e; color: #dcdcdc; font-family: Consolas, monospace;
  font-size: 12px; padding: 16px; border-radius: 4px; height: 420px;
  overflow-y: auto; white-space: pre-wrap; word-break: break-all; margin: 0;
}
.hint {
  color: #606266; font-size: 13px; margin-top: 16px; padding: 12px;
  background: #fdf6ec; border-radius: 4px; border: 1px solid #faecd8;
}
.hint p { margin: 6px 0; }
</style>
