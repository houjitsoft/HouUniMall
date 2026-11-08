<template>
  <div class="distribution-withdraw">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>提现审核</span>
          <el-button @click="load">刷新</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="分销商ID">
          <el-input v-model="query.distributorId" placeholder="按分销商筛选" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过待打款" :value="1" />
            <el-option label="已驳回" :value="2" />
            <el-option label="已打款" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="withdrawNo" label="提现单号" min-width="170" show-overflow-tooltip />
        <el-table-column prop="distributorId" label="分销商ID" width="110" />
        <el-table-column prop="amount" label="提现金额" width="110" align="right" />
        <el-table-column prop="fee" label="手续费" width="100" align="right" />
        <el-table-column prop="actualAmount" label="实际到账" width="110" align="right" />
        <el-table-column label="收款账户" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ [row.bankName, row.accountName, row.accountNo].filter(Boolean).join(' / ') || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="success" @click="handleAudit(row, 1)">通过</el-button>
              <el-button link type="danger" @click="handleAudit(row, 2)">驳回</el-button>
            </template>
            <el-button v-else-if="row.status === 1" link type="primary" @click="handleAudit(row, 3)">
              标记已打款
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无提现申请" />
        </template>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :current-page="query.page"
        :page-size="query.size"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="onCurrentChange"
        @size-change="onSizeChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { distributionWithdrawApi } from '../api/distribution'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, distributorId: '', status: '' })

const statusText = (status) => {
  const map = { 0: '待审核', 1: '待打款', 2: '已驳回', 3: '已打款' }
  return map[status] ?? '-'
}
const statusTagType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'danger', 3: 'success' }
  return map[status] ?? 'info'
}

const buildParams = () => {
  const params = { page: query.page, size: query.size }
  if (query.distributorId) params.distributorId = Number(query.distributorId)
  if (query.status !== '' && query.status !== null) params.status = Number(query.status)
  return params
}

const load = async () => {
  loading.value = true
  try {
    const res = await distributionWithdrawApi.page(buildParams())
    const data = res?.data || res || {}
    list.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('提现列表加载失败')
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.page = 1
  load()
}
const reset = () => {
  query.distributorId = ''
  query.status = ''
  query.page = 1
  load()
}
const onCurrentChange = (page) => {
  query.page = page
  load()
}
const onSizeChange = (size) => {
  query.size = size
  query.page = 1
  load()
}

const handleAudit = async (row, status) => {
  const label = { 1: '通过', 2: '驳回', 3: '标记已打款' }[status]
  try {
    const { value } = await ElMessageBox.prompt(`确认${label}该提现申请？`, '审核', {
      inputPlaceholder: '审核备注（可留空）',
      inputType: 'textarea'
    })
    await distributionWithdrawApi.audit({ id: row.id, status, auditor: 'admin', remark: value || '' })
    ElMessage.success('已处理')
    load()
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.search-form {
  margin-bottom: 8px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
