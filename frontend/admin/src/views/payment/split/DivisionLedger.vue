<template>
  <div class="division-ledger">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent="onSearch">
        <el-form-item label="商户号">
          <el-input v-model="queryForm.mchNo" clearable placeholder="mchNo" style="width:160px" />
        </el-form-item>
        <el-form-item label="分账单号">
          <el-input v-model="queryForm.divisionNo" clearable placeholder="divisionNo" style="width:180px" />
        </el-form-item>
        <el-form-item label="渠道">
          <el-input v-model="queryForm.chanNo" clearable placeholder="chanNo" style="width:140px" />
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="queryForm.source" clearable placeholder="全部" style="width:120px">
            <el-option label="INTERNAL" value="INTERNAL" />
            <el-option label="OPENAPI" value="OPENAPI" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部" style="width:120px">
            <el-option label="待发起" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="成功" :value="2" />
            <el-option label="失败" :value="3" />
            <el-option label="已撤销" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="list-card" style="margin-top:12px">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column label="分账单号" prop="divisionNo" min-width="180" show-overflow-tooltip />
        <el-table-column label="商户号" prop="mchNo" width="130" show-overflow-tooltip />
        <el-table-column label="渠道" prop="chanNo" width="110" />
        <el-table-column label="保证订单号" prop="mchAssureOrderNo" min-width="150" show-overflow-tooltip />
        <el-table-column label="商户订单号" prop="mchOrderNo" min-width="140" show-overflow-tooltip />
        <el-table-column label="支付单号" prop="payOrderId" min-width="140" show-overflow-tooltip />
        <el-table-column label="分账金额(分)" prop="divisionAmount" width="120" />
        <el-table-column label="来源" prop="source" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ row.statusDesc || statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="onDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="onSearch"
          @current-change="fetchList" />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="分账台账详情" size="520px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="分账单号">{{ detail.divisionNo }}</el-descriptions-item>
        <el-descriptions-item label="商户号">{{ detail.mchNo }}</el-descriptions-item>
        <el-descriptions-item label="渠道">{{ detail.chanNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusDesc || statusLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="分账金额(分)">{{ detail.divisionAmount }}</el-descriptions-item>
        <el-descriptions-item label="订单金额(分)">{{ detail.orderAmount }}</el-descriptions-item>
        <el-descriptions-item label="assureOrderId">{{ detail.assureOrderId }}</el-descriptions-item>
        <el-descriptions-item label="错误码">{{ detail.errCode }}</el-descriptions-item>
        <el-descriptions-item label="错误信息">{{ detail.errMsg }}</el-descriptions-item>
      </el-descriptions>
      <el-table v-if="detail?.items?.length" :data="detail.items" border style="margin-top:12px" size="small">
        <el-table-column label="接收方" prop="receiverName" min-width="120" />
        <el-table-column label="接收方号" prop="receiverNo" min-width="120" />
        <el-table-column label="金额(分)" prop="amount" width="100" />
        <el-table-column label="状态" prop="state" width="80" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { divisionLedgerApi } from '@/api/payment/divisionLedger'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  mchNo: '',
  divisionNo: '',
  chanNo: '',
  source: '',
  status: undefined
})

function statusLabel(s) {
  return ({ 0: '待发起', 1: '处理中', 2: '成功', 3: '失败', 4: '已撤销' })[s] || String(s ?? '')
}
function statusTag(s) {
  return ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' })[s] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const res = await divisionLedgerApi.page({ ...queryForm })
    const data = res?.data ?? res
    tableData.value = data?.records || []
    total.value = Number(data?.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  queryForm.pageNum = 1
  fetchList()
}
function onReset() {
  queryForm.mchNo = ''
  queryForm.divisionNo = ''
  queryForm.chanNo = ''
  queryForm.source = ''
  queryForm.status = undefined
  onSearch()
}

async function onDetail(row) {
  try {
    const res = await divisionLedgerApi.query({
      divisionNo: row.divisionNo,
      mchNo: row.mchNo,
      mchAssureOrderNo: row.mchAssureOrderNo
    })
    detail.value = res?.data ?? res
    detailVisible.value = true
  } catch (e) {
    ElMessage.error(e?.message || '详情加载失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
