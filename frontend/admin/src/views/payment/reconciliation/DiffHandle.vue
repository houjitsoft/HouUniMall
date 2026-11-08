<template>
  <div class="diff-handle-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>对账差异处理</span>
          <el-button type="primary" size="small" @click="triggerRecon">一键对账</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="search" style="margin-bottom:12px">
        <el-form-item label="差异类型">
          <el-select v-model="search.diffType" clearable placeholder="全部" style="width:140px">
            <el-option label="金额不一致" value="AMOUNT_DIFF" /><el-option label="状态不一致" value="STATUS_DIFF" />
            <el-option label="本地多出" value="LOCAL_ONLY" /><el-option label="渠道多出" value="CHANNEL_ONLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="search.handleStatus" clearable placeholder="全部" style="width:120px">
            <el-option label="待处理" value="0" /><el-option label="已处理" value="1" /><el-option label="已忽略" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="billDate" label="账单日期" width="110" />
        <el-table-column prop="channelCode" label="渠道" width="90" />
        <el-table-column prop="localOrderNo" label="本地订单号" min-width="150" />
        <el-table-column prop="localAmount" label="本地金额" width="100" align="right" />
        <el-table-column prop="channelAmount" label="渠道金额" width="100" align="right" />
        <el-table-column prop="diffType" label="差异类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.diffType === 'AMOUNT_DIFF' ? 'warning' : 'danger'" size="small">{{ row.diffType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.handleStatus === 1 ? 'success' : row.handleStatus === 2 ? 'info' : 'warning'" size="small">
              {{ row.handleStatus === 1 ? '已处理' : row.handleStatus === 2 ? '已忽略' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <template v-if="row.handleStatus === 0">
              <el-button link type="primary" size="small" @click="handleDiff(row, 1)">处理</el-button>
              <el-button link type="info" size="small" @click="handleDiff(row, 2)">忽略</el-button>
            </template>
            <span v-else style="color:#909399;font-size:12px;">已完结</span>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px;display:flex;justify-content:flex-end;">
        <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
          :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" background @change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const search = reactive({ diffType: '', handleStatus: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/pay/recon/diffList', { ...search, page: page.current, size: page.size })
    if (res.code === 200 && res.data) { tableData.value = res.data.records || res.data.list || []; page.total = res.data.total || 0 }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleDiff = async (row, status) => {
  const res = await request.post('/web/pay/recon/diffHandle', { id: row.id, handleStatus: status })
  if (res.code === 200) { ElMessage.success('处理成功'); loadData() } else { ElMessage.error(res.message || '操作失败') }
}

const triggerRecon = async () => {
  ElMessage.info('正在执行对账...')
  const res = await request.post('/web/pay/recon/trigger', {})
  if (res.code === 200) { ElMessage.success('对账完成'); loadData() } else { ElMessage.error(res.message || '对账失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.diff-handle-page { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
