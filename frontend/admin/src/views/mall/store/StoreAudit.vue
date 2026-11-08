<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="card-title">店铺审核</span></template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="店铺名称">
          <el-input v-model="searchForm.name" placeholder="请输入店铺名称" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待审核" value="0" />
            <el-option label="审核通过" value="1" />
            <el-option label="审核拒绝" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="storeName" label="店铺名称" min-width="150" />
        <el-table-column prop="owner" label="申请人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditTime" label="审核时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" text type="success" size="small" @click="handlePass(row)">通过</el-button>
            <el-button v-if="row.status === 0" text type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
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
        @size-change="fetchData"
        @current-change="fetchData"
        class="pagination"
      />
    </el-card>

    <!-- 拒绝理由对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝理由" width="500px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝理由" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
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

const tableData = ref([
  { id: 1, storeName: '新店申请1', owner: '张三', phone: '13800138001', applyTime: '2024-01-01 10:00:00', status: 0, auditTime: '-' },
  { id: 2, storeName: '新店申请2', owner: '李四', phone: '13800138002', applyTime: '2024-01-02 11:00:00', status: 1, auditTime: '2024-01-02 15:00:00' },
  { id: 3, storeName: '新店申请3', owner: '王五', phone: '13800138003', applyTime: '2024-01-03 12:00:00', status: 2, auditTime: '2024-01-03 16:00:00' }
])

const fetchData = async () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = tableData.value.length
    loading.value = false
  }, 500)
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.status = ''
  fetchData()
}

const handlePass = (row) => {
  ElMessageBox.confirm('确认通过该店铺的审核申请？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    ElMessage.success('审核通过')
    row.status = 1
    row.auditTime = new Date().toLocaleString()
  })
}

const handleReject = (row) => {
  currentRow.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = () => {
  if (!rejectReason.value) {
    ElMessage.warning('请输入拒绝理由')
    return
  }
  currentRow.value.status = 2
  currentRow.value.auditTime = new Date().toLocaleString()
  ElMessage.success('已拒绝申请')
  rejectDialogVisible.value = false
}

const handleView = (row) => {
  ElMessage.info('查看申请详情：' + row.storeName)
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
