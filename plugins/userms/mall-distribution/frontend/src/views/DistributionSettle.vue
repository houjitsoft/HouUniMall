<template>
  <div class="distribution-settle">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>佣金结算</span>
          <div>
            <el-button type="primary" :loading="running" @click="handleRun">一键结算</el-button>
            <el-button :loading="generating" @click="handleGenerate">仅生成佣金</el-button>
            <el-button :loading="settling" @click="handleSettleDue">仅结算到期</el-button>
          </div>
        </div>
      </template>

      <el-alert
        type="info"
        show-icon
        :closable="false"
        title="链路：已完成的分销订单 → 生成佣金（待结算） → T+N 到期自动结算为可提现。结算周期与层级数在「分销设置」中配置。"
        class="tip"
      />

      <div class="stats">
        <div class="stat-item">
          <div class="stat-label">待结算金额</div>
          <div class="stat-value">¥ {{ stats.pendingAmount ?? '0.00' }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">待结算笔数</div>
          <div class="stat-value">{{ stats.pendingCount ?? 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">已结算金额</div>
          <div class="stat-value success">¥ {{ stats.settledAmount ?? '0.00' }}</div>
        </div>
      </div>

      <div v-if="lastResult" class="result">
        上次执行：生成 {{ lastResult.generated ?? 0 }} 笔 / ¥{{ lastResult.generatedAmount ?? '0.00' }}，
        结算 {{ lastResult.settled ?? 0 }} 笔 / ¥{{ lastResult.settledAmount ?? '0.00' }}，
        跳过 {{ lastResult.skipped ?? 0 }} 笔
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { distributionSettleApi } from '../api/distribution'

const stats = reactive({})
const lastResult = ref(null)
const running = ref(false)
const generating = ref(false)
const settling = ref(false)

const loadStats = async () => {
  try {
    const res = await distributionSettleApi.stats()
    Object.assign(stats, res?.data || res || {})
  } catch (e) {
    ElMessage.error('结算概览加载失败')
  }
}

const runTask = async (fn, flag) => {
  flag.value = true
  try {
    const res = await fn()
    lastResult.value = res?.data || res || {}
    ElMessage.success('执行完成')
    await loadStats()
  } catch (e) {
    ElMessage.error(e?.message || '执行失败')
  } finally {
    flag.value = false
  }
}

const handleRun = () => runTask(() => distributionSettleApi.run(), running)
const handleGenerate = () => runTask(() => distributionSettleApi.generate(), generating)
const handleSettleDue = () => runTask(() => distributionSettleApi.settleDue(), settling)

onMounted(loadStats)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.tip {
  margin-bottom: 16px;
}
.stats {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}
.stat-item {
  min-width: 160px;
  padding: 14px 18px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
}
.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}
.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.stat-value.success {
  color: var(--el-color-success);
}
.result {
  margin-top: 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
