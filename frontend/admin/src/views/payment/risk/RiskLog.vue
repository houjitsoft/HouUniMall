<template>
  <div class="risk-log-page">
    <el-card shadow="never">
      <template #header><span>风控日志</span></template>
      <el-form :inline="true" :model="search" class="search-form">
        <el-form-item label="风险等级">
          <el-select v-model="search.riskLevel" clearable placeholder="全部" style="width:120px">
            <el-option label="高" value="high" /><el-option label="中" value="medium" /><el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="search.auditStatus" clearable placeholder="全部" style="width:120px">
            <el-option label="待审核" value="0" /><el-option label="已通过" value="1" /><el-option label="已驳回" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column prop="riskLevel" label="风险等级" width="90">
          <template #default="{ row }">
            <el-tag :type="row.riskLevel === 'high' ? 'danger' : row.riskLevel === 'medium' ? 'warning' : 'info'" size="small">{{ row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleHit" label="命中规则" min-width="140" />
        <el-table-column prop="amount" label="交易金额" width="110" align="right" />
        <el-table-column prop="auditStatus" label="审核状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'warning'" size="small">
              {{ row.auditStatus === 1 ? '通过' : row.auditStatus === 2 ? '驳回' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.auditStatus === 0" link type="success" size="small" @click="handleAudit(row, 1)">通过</el-button>
            <el-button v-if="row.auditStatus === 0" link type="danger" size="small" @click="handleAudit(row, 2)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px;display:flex;justify-content:flex-end;">
        <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
          :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" background @change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="风控日志详情" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">{{ detail.riskLevel }}</el-descriptions-item>
        <el-descriptions-item label="命中规则">{{ detail.ruleHit }}</el-descriptions-item>
        <el-descriptions-item label="交易金额">{{ detail.amount }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">{{ detail.auditStatus === 1 ? '通过' : detail.auditStatus === 2 ? '驳回' : '待审核' }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="详情" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const search = reactive({ riskLevel: '', auditStatus: '' })
const detailVisible = ref(false)
const detail = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/pay/risk/log/list', { ...search, page: page.current, size: page.size })
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || res.data.list || []
      page.total = res.data.total || 0
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleDetail = async (row) => {
  try {
    const res = await request.post('/web/pay/risk/log/detail', { id: row.id })
    detail.value = res.code === 200 && res.data ? res.data : row
  } catch (e) { detail.value = row }
  detailVisible.value = true
}

const handleAudit = async (row, status) => {
  const res = await request.post('/web/pay/risk/log/audit', { id: row.id, auditStatus: status })
  if (res.code === 200) { ElMessage.success('审核完成'); loadData() } else { ElMessage.error(res.message || '操作失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.risk-log-page { padding: 16px; }
.search-form { margin-bottom: 12px; }
</style>
