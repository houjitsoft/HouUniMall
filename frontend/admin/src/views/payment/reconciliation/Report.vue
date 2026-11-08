<template>
  <div class="recon-report-page">
    <el-card shadow="never">
      <template #header><span>对账报表</span></template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="reportMonth" label="报表月份" width="120" />
        <el-table-column prop="channelCode" label="渠道" width="100" />
        <el-table-column prop="totalOrders" label="总订单数" width="100" align="right" />
        <el-table-column prop="totalAmount" label="总金额" width="120" align="right" />
        <el-table-column prop="diffCount" label="差异笔数" width="100" align="right" />
        <el-table-column prop="matchRate" label="对账率" width="90" align="right">
          <template #default="{ row }">{{ row.matchRate ? row.matchRate + '%' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="160" />
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
import request from '@/utils/request.js'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/pay/recon/reportList', { page: page.current, size: page.size })
    if (res.code === 200 && res.data) { tableData.value = res.data.records || res.data.list || []; page.total = res.data.total || 0 }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

onMounted(() => loadData())
</script>

<style scoped>
.recon-report-page { padding: 16px; }
</style>
