<template>
  <div class="recharge-callback">
    <el-result v-if="state === 'success'" icon="success" title="充值成功" :sub-title="successTip" />
    <el-result v-else-if="state === 'fail'" icon="error" title="支付未完成" :sub-title="errorTip" />
    <div v-else class="recharge-callback__loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p>正在确认支付结果…</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { AI_RECHARGE_DONE_EVENT, formatUsageTokens, isApiOk } from '@/utils/aiCostingBalance'

const route = useRoute()
const state = ref('loading')
const successTip = ref('Token 已到账，请返回用量费用页继续查看。')
const errorTip = ref('如已完成支付，请返回用量费用页点击「我已支付」同步。')

async function handleCallback() {
  const query = { ...route.query }
  const orderNo = String(query.mchOrderNo || query.orderNo || '').trim()
  if (!orderNo) {
    state.value = 'fail'
    errorTip.value = '缺少订单号，无法确认支付结果'
    return
  }

  try {
    const res = await request.post('/web/geo/support/recharge/sync', { ...query, orderNo })
    if (isApiOk(res) && (res.data?.fulfilled || res.data?.payStatus === 'paid')) {
      state.value = 'success'
      const tokens = res.data?.tokenAmount
      if (tokens) {
        successTip.value = `已成功充值 ${formatUsageTokens(tokens)} Token，请返回用量费用页查看余额。`
      }
      notifyOpener(res.data)
      ElMessage.success('充值成功，Token 已到账')
      setTimeout(() => window.close(), 2500)
      return
    }
    state.value = 'fail'
  } catch (e) {
    state.value = 'fail'
    errorTip.value = e?.response?.data?.message || e?.message || '确认支付失败'
  }
}

function notifyOpener(data) {
  try {
    if (window.opener && !window.opener.closed) {
      window.opener.postMessage({ type: AI_RECHARGE_DONE_EVENT, data }, window.location.origin)
    }
  } catch {
    // ignore
  }
}

onMounted(handleCallback)
</script>

<style scoped>
.recharge-callback {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.recharge-callback__loading {
  text-align: center;
  color: #606266;
}

.recharge-callback__loading p {
  margin-top: 12px;
}
</style>
