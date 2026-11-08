<template>
  <div class="purchase-pipeline">
    <el-steps :active="activeIndex" finish-status="success" align-center>
      <el-step title="支付成功" description="授权已签发" />
      <el-step title="自动换票" :description="ticketDesc" />
      <el-step title="安装进度" :description="installDesc" />
      <el-step title="健康检查" :description="healthDesc" />
    </el-steps>

    <div class="progress-block">
      <el-progress :percentage="percent" :status="progressStatus" :stroke-width="14" />
      <p class="progress-tip">{{ tip }}</p>
    </div>

    <el-alert
      v-if="licenseKey"
      type="success"
      :closable="false"
      show-icon
      class="license-alert"
      :title="`授权码 ${licenseKey}`"
      :description="instanceId ? `已绑定站点 ${instanceId}` : '已绑定本站点'"
    />

    <div v-if="failed" class="actions">
      <el-button type="primary" :loading="running" @click="$emit('retry')">重试安装</el-button>
      <el-button @click="$emit('licenses')">授权管理</el-button>
    </div>
    <div v-else-if="done" class="actions">
      <el-button type="primary" @click="$emit('manage')">去插件中心</el-button>
      <el-button @click="$emit('licenses')">授权管理</el-button>
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  phase: { type: String, default: 'paid' }, // paid | ticket | install | health | done | failed
  percent: { type: Number, default: 0 },
  tip: { type: String, default: '' },
  ticketDetail: { type: String, default: '' },
  installDetail: { type: String, default: '' },
  healthOk: { type: Boolean, default: null },
  healthDetail: { type: String, default: '' },
  licenseKey: { type: String, default: '' },
  instanceId: { type: String, default: '' },
  running: { type: Boolean, default: false },
})

defineEmits(['retry', 'licenses', 'manage'])

const phaseOrder = ['paid', 'ticket', 'install', 'health', 'done']
const activeIndex = computed(() => {
  if (props.phase === 'failed') return Math.max(1, phaseOrder.indexOf('install'))
  const idx = phaseOrder.indexOf(props.phase)
  return idx < 0 ? 0 : idx
})

const ticketDesc = computed(() => props.ticketDetail || (activeIndex.value >= 1 ? '制品令牌已签发' : '等待换票'))
const installDesc = computed(() => props.installDetail || (activeIndex.value >= 2 ? '编排安装中' : '等待安装'))
const healthDesc = computed(() => {
  if (props.healthOk === true) return props.healthDetail || '探活通过'
  if (props.healthOk === false) return props.healthDetail || '探活失败'
  return '等待检查'
})

const done = computed(() => props.phase === 'done')
const failed = computed(() => props.phase === 'failed')
const progressStatus = computed(() => {
  if (failed.value) return 'exception'
  if (done.value) return 'success'
  return undefined
})
</script>

<style scoped>
.purchase-pipeline{margin-top:8px}
.progress-block{margin:22px 0 8px}
.progress-tip{margin:10px 0 0;color:#606266;font-size:13px;text-align:center}
.license-alert{margin-top:16px}
.actions{display:flex;flex-wrap:wrap;gap:10px;justify-content:center;margin-top:18px}
</style>
