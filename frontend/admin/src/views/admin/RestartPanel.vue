<template>
  <div class="restart-panel">
    <div class="form" v-if="!running">
      <el-form label-width="120px">
        <el-form-item>
          <el-button type="primary" size="large" :loading="triggering" @click="trigger">
            一键打包并重启全部服务
          </el-button>
          <el-button @click="clearLog">清空日志</el-button>
        </el-form-item>
      </el-form>
      <el-collapse class="advanced">
        <el-collapse-item title="高级选项（一般不用改）" name="adv">
          <el-checkbox v-model="opts.skipFrontend">跳过前端构建</el-checkbox>
          <el-checkbox v-model="opts.forceFrontend">强制打包前端（忽略 git 变更检测）</el-checkbox>
          <el-checkbox v-model="opts.skipBackend">跳过后端编译</el-checkbox>
          <el-checkbox v-model="opts.backup">编译前备份 jar</el-checkbox>
          <el-checkbox v-model="opts.full">强制全量编译（不走增量）</el-checkbox>
          <el-checkbox v-model="opts.forceRestart">无新代码也重启全部 Java 服务</el-checkbox>
        </el-collapse-item>
      </el-collapse>
    </div>

    <div class="status-bar" v-if="running || finished">
      <span>任务 ID：{{ stamp }}</span>
      <span v-if="elapsed">耗时：{{ formatDuration(elapsed) }}</span>
      <span v-if="finished" class="ok">✓ 已完成</span>
    </div>

    <pre class="log-box" ref="logBoxRef">{{ logText || '（暂无日志）' }}</pre>

    <div class="hint">
      <p><strong>流程：</strong><strong>git pull</strong> → 前端 build（有变更时）→ Maven 打包 jar → 滚动重启 Java 服务。</p>
      <p>远程无新代码时仍会<strong>重建 cloud 前端</strong>，避免 Git 已更新但线上静态包未更新；后端跳过编译后重启服务。</p>
      <p><strong>502 说明：</strong>仅 bff-web 滚动重启时可能短暂 502；若整页一直 502，请执行 <code>jar\ensure-bff-gateway.ps1</code>。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted, nextTick } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const props = defineProps({
  adminToken: { type: String, required: true }
})

const emit = defineEmits(['finished', 'started'])

const opts = reactive({
  skipFrontend: false,
  forceFrontend: false,
  skipBackend: false,
  backup: false,
  full: false,
  forceRestart: false
})

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

function formatDuration(ms) {
  const s = Math.floor(ms / 1000)
  const m = Math.floor(s / 60)
  return `${m} 分 ${s % 60} 秒`
}

function hasAdvancedOpts() {
  return opts.skipFrontend || opts.forceFrontend || opts.skipBackend || opts.backup || opts.full || opts.forceRestart
}

async function trigger() {
  if (!props.adminToken?.trim()) {
    ElMessage.warning('请先在上方输入管理员令牌')
    return
  }
  triggering.value = true
  try {
    const body = hasAdvancedOpts() ? { ...opts } : {}
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
    ElMessage.success(`重启任务已下发 (${stamp.value})`)
  } catch (e) {
    const st = e.response?.status
    if (st === 502 || st === 503 || st === 504 || e.code === 'ERR_NETWORK') {
      ElMessage.error({
        message: '网关 bff-web(8391) 未运行。请执行 jar\\ensure-bff-gateway.ps1',
        duration: 12000,
        showClose: true
      })
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
          ElMessage.error('重启结束但有失败步骤，请查看日志')
        } else {
          ElMessage.success('打包重启已完成')
        }
      }
    }
  } catch (e) {
    const status = e.response?.status
    if (status === 502 || status === 503 || status === 504 || e.code === 'ERR_NETWORK') {
      const waited = Date.now() - startedAt.value
      if (waited > 120000) {
        appendSystemLine('[轮询] 超过 2 分钟仍 502，请检查 bff-web')
      } else {
        appendSystemLine('[轮询] 网关滚动重启中，约 10～30 秒 502 属正常…')
      }
    } else {
      appendSystemLine(`[轮询] 暂时无法连接 (${status || e.code || e.message})，继续重试…`)
    }
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
.advanced { margin-top: 8px; }
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
  background: #f0f9eb; border-radius: 4px; border: 1px solid #e1f3d8;
}
.hint p { margin: 6px 0; }
</style>
