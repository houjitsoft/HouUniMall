<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">心得管理</span>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="心得标题">
          <el-input v-model="searchForm.title" placeholder="请输入心得标题" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="发布用户">
          <el-input v-model="searchForm.userName" placeholder="请输入用户名" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="审核拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="moodId" label="心得ID" width="80" />
        <el-table-column label="封面图" width="100">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" :preview-src-list="[row.coverImage]" fit="cover" style="width: 60px; height: 60px;" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="心得标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userName" label="发布用户" width="100" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="likeCount" label="点赞数" width="100" />
        <el-table-column prop="commentCount" label="评论数" width="100" />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.auditStatus)">
              {{ getStatusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button text type="success" size="small" @click="handleAudit(row, 1)" v-if="row.auditStatus === 0">
              <el-icon><Check /></el-icon> 通过
            </el-button>
            <el-button text type="danger" size="small" @click="handleAudit(row, 2)" v-if="row.auditStatus === 0">
              <el-icon><Close /></el-icon> 拒绝
            </el-button>
            <el-button text type="warning" size="small" @click="handleOffline(row)" v-if="row.auditStatus === 1">
              <el-icon><Bottom /></el-icon> 下架
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="审核心得"
      width="500px"
    >
      <el-form :model="auditForm" ref="auditFormRef" label-width="80px">
        <el-form-item label="审核结果">
          <el-tag :type="auditForm.auditStatus === 1 ? 'success' : 'danger'">
            {{ auditForm.auditStatus === 1 ? '通过' : '拒绝' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditRemark">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="4" placeholder="请输入审核意见（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Check, Close, Bottom, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const auditDialogVisible = ref(false)
const auditFormRef = ref(null)

const searchForm = reactive({
  title: '',
  userName: '',
  auditStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const auditForm = reactive({
  moodId: null,
  auditStatus: 1,
  auditRemark: ''
})

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '审核通过', 2: '审核拒绝' }
  return map[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取数据
    tableData.value = [
      {
        moodId: 1,
        title: '超值购物分享 - 这款产品真的太好用了',
        userName: '用户小张',
        coverImage: '',
        viewCount: 856,
        likeCount: 123,
        commentCount: 45,
        auditStatus: 0,
        publishTime: '2024-01-04 14:30:00',
        createTime: '2024-01-04 14:30:00'
      }
    ]
    pagination.total = 1
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.userName = ''
  searchForm.auditStatus = ''
  handleSearch()
}

const handleView = (row) => {
  ElMessage.info('查看心得详情')
}

const handleAudit = (row, status) => {
  auditForm.moodId = row.moodId
  auditForm.auditStatus = status
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

const handleOffline = async (row) => {
  try {
    await ElMessageBox.confirm('确定要下架该心得吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用API下架
    ElMessage.success('下架成功')
    loadData()
  } catch {}
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该心得吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用API删除
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

const handleSubmitAudit = async () => {
  try {
    // TODO: 调用API提交审核
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: bold;
  font-size: 16px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
