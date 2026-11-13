<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="card-title">店铺审核</span></template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="店铺名称">
          <el-input v-model="searchForm.name" placeholder="请输入店铺名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="审核拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="fetchData">刷新</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="listError" class="search-form" type="error" :title="listError" :closable="false" show-icon />

      <!-- 数据表格：只展示 hou_shop_join_base 的真实入驻申请 -->
      <el-table :data="tableData" v-loading="loading" border stripe empty-text="暂无入驻申请">
        <el-table-column prop="joinId" label="申请ID" width="170" show-overflow-tooltip />
        <el-table-column prop="name" label="店铺名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="contact" label="申请人" width="110" />
        <el-table-column prop="mobile" label="联系电话" width="140" />
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.auditStatus)">{{ getStatusText(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核时间" width="160">
          <template #default="{ row }">{{ formatTime(row.auditTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.auditStatus === 0" text type="success" size="small" @click="handlePass(row)">通过</el-button>
            <el-button v-if="row.auditStatus === 0" text type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            <el-button text type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="fetchData"
        class="pagination"
      />
    </el-card>

    <!-- 拒绝理由对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝理由" width="500px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝理由" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mallShopApplyLists, mallShopApplyFollow } from '@/api/mall/shopAdmin'

const loading = ref(false)
const auditing = ref(false)
const listError = ref('')
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentRow = ref(null)

const searchForm = reactive({
  name: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 不设演示数据：列表完全来自后端真实入驻申请，刷新后重新查询。
const tableData = ref([])

const formatTime = (value) => {
  if (value === null || value === undefined || value === '' || Number(value) <= 0) return '-'
  const numeric = Number(value)
  const date = new Date(numeric < 1e12 ? numeric * 1000 : numeric)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const fetchData = async () => {
  loading.value = true
  listError.value = ''
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      includeApproved: 1,
      shopName: searchForm.name.trim()
    }
    if (searchForm.status !== '' && searchForm.status !== null && searchForm.status !== undefined) {
      params.auditStatus = Number(searchForm.status)
    }
    const res = await mallShopApplyLists(params)
    if (res?.code !== 1 || !res.data) throw new Error(res?.message || res?.msg || '入驻申请加载失败')
    tableData.value = res.data.lists || []
    pagination.total = Number(res.data.count || 0)
  } catch (error) {
    tableData.value = []
    pagination.total = 0
    listError.value = error?.message || '入驻申请加载失败，请刷新重试'
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.status = ''
  handleSearch()
}

const submitAudit = async (row, auditStatus, auditComment = '') => {
  if (!row?.joinId || auditing.value) return false
  auditing.value = true
  try {
    const res = await mallShopApplyFollow({ joinId: row.joinId, auditStatus, auditComment })
    if (res?.code !== 1) throw new Error(res?.message || res?.msg || '审核失败')
    ElMessage.success(auditStatus === 1 ? '审核通过' : '已拒绝申请')
    await fetchData()
    return true
  } catch (error) {
    ElMessage.error(error?.message || '审核失败')
    return false
  } finally {
    auditing.value = false
  }
}

const handlePass = (row) => {
  ElMessageBox.confirm('确认通过该店铺的入驻申请？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => submitAudit(row, 1)).catch(() => {})
}

const handleReject = (row) => {
  currentRow.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝理由')
    return
  }
  const done = await submitAudit(currentRow.value, 2, rejectReason.value.trim())
  if (done) rejectDialogVisible.value = false
}

const handleView = (row) => {
  const lines = [
    `申请ID：${row.joinId || '-'}`,
    `店铺名称：${row.name || '-'}`,
    `店铺类型：${row.typeText || '-'}`,
    `店铺等级：${row.levelName || '-'}`,
    `联系人：${row.contact || '-'}`,
    `联系电话：${row.mobile || '-'}`,
    `经营地址：${row.addressInfo || '-'}`,
    `申请时间：${formatTime(row.createTime)}`,
    `审核状态：${getStatusText(row.auditStatus)}`,
    `审核时间：${formatTime(row.auditTime)}`,
    `审核备注：${row.auditComment || '-'}`
  ]
  ElMessageBox.alert(lines.join('\n'), '入驻申请详情', { confirmButtonText: '关闭' })
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '审核通过', 2: '审核拒绝' }
  return map[status] || '-'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-title { font-weight: bold; font-size: 16px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
