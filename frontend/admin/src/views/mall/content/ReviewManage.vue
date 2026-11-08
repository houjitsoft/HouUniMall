<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">评论管理</span>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="评论内容">
          <el-input v-model="searchForm.content" placeholder="请输入评论内容" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="评论用户">
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
        <el-table-column prop="commentId" label="评论ID" width="80" />
        <el-table-column prop="content" label="评论内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="userName" label="评论用户" width="100" />
        <el-table-column prop="targetTitle" label="评论对象" min-width="150" show-overflow-tooltip />
        <el-table-column label="评论类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.targetType === 1 ? 'primary' : 'success'">
              {{ row.targetType === 1 ? '心得' : '选题' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞数" width="100" />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.auditStatus)">
              {{ getStatusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
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
      title="审核评论"
      width="500px"
    >
      <el-form :model="auditForm" ref="auditFormRef" label-width="80px">
        <el-form-item label="评论内容">
          <div class="comment-preview">{{ auditForm.content }}</div>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-tag :type="auditForm.auditStatus === 1 ? 'success' : 'danger'">
            {{ auditForm.auditStatus === 1 ? '通过' : '拒绝' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditRemark">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见（选填）" />
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
import { Search, Refresh, View, Check, Close, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const auditDialogVisible = ref(false)
const auditFormRef = ref(null)

const searchForm = reactive({
  content: '',
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
  commentId: null,
  content: '',
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
        commentId: 1,
        content: '这篇心得写得太好了，很有参考价值！',
        userName: '用户李四',
        targetTitle: '超值购物分享 - 这款产品真的太好用了',
        targetType: 1,
        likeCount: 23,
        auditStatus: 0,
        createTime: '2024-01-04 15:20:00'
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
  searchForm.content = ''
  searchForm.userName = ''
  searchForm.auditStatus = ''
  handleSearch()
}

const handleView = (row) => {
  ElMessage.info('查看评论详情')
}

const handleAudit = (row, status) => {
  auditForm.commentId = row.commentId
  auditForm.content = row.content
  auditForm.auditStatus = status
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
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

.comment-preview {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  line-height: 1.6;
}
</style>
