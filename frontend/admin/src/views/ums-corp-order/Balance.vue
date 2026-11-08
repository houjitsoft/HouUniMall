<template>
  <div class="ums-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>商户余额查询 (202108)</span>
          <el-button type="primary" :loading="loading" @click="query">查询可清算金额</el-button>
        </div>
      </template>
      <el-descriptions v-if="data" :column="2" border>
        <el-descriptions-item label="企业用户号">{{ data.merNo }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ data.merName }}</el-descriptions-item>
        <el-descriptions-item label="所属集团">{{ data.groupId }}</el-descriptions-item>
        <el-descriptions-item label="顶层商户">{{ data.topMer }}</el-descriptions-item>
        <el-descriptions-item label="所在层级">{{ data.merLevel }}</el-descriptions-item>
        <el-descriptions-item label="可清算金额">
          <strong>{{ formatYuan(data.canPayAmt) }}</strong>
          <span class="fen">（{{ data.canPayAmt || 0 }} 分）</span>
        </el-descriptions-item>
        <el-descriptions-item label="返回码">{{ data.respCode }}</el-descriptions-item>
        <el-descriptions-item label="返回信息">{{ data.respMsg }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="点击右上角查询" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { umsCorpApi } from '@/api/ums-corp-order'

const loading = ref(false)
const data = ref(null)

function formatYuan(fen) {
  if (fen == null || fen === '') return '¥ 0.00'
  return '¥ ' + (Number(fen) / 100).toFixed(2)
}

async function query() {
  loading.value = true
  try {
    const res = await umsCorpApi.balance({})
    if (res?.code != null && ![0,1,200].includes(Number(res.code))) {
      ElMessage.error(res.message || '查询失败')
      return
    }
    data.value = res?.data || res || {}
  } catch (e) {
    ElMessage.error(e?.message || '查询失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ums-page { padding: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; font-weight: 600; }
.fen { margin-left: 8px; color: #909399; font-size: 12px; }
</style>
