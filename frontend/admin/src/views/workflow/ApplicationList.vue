<template>
  <div class="application-content">
    <div class="page-header">
      <h2>我的申请</h2>
      <p>查看和管理我提交的所有申请</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-number">{{ stats.total }}</div>
        <div class="stat-label">总申请数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ stats.pending }}</div>
        <div class="stat-label">待处理</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ stats.approved }}</div>
        <div class="stat-label">已通过</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ stats.rejected }}</div>
        <div class="stat-label">已拒绝</div>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="申请类型">
          <el-select v-model="filterForm.processKey" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="请假申请" value="leave-request" />
            <el-option label="报销申请" value="reimbursement" />
            <el-option label="费用申请" value="expenses" />
            <el-option label="出差申请" value="business-trip" />
            <el-option label="采购申请" value="procurement" />
          </el-select>
        </el-form-item>

        <el-form-item label="申请状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="待处理" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>

        <el-form-item label="提交时间">
          <el-date-picker
            v-model="filterForm.submitTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 申请列表 -->
    <div class="application-table">
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="approvalNumber" label="审批编号" width="120" />
        <el-table-column prop="processName" label="申请类型" width="120" />
        <el-table-column prop="title" label="申请标题" min-width="200" />
        <el-table-column prop="submitTime" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="completeTime" label="完成时间" width="160">
          <template #default="{ row }">
            {{ row.completeTime ? formatDateTime(row.completeTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="warning"
              size="small"
              @click="handleCancel(row)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && tableData.length === 0" description="暂无申请记录" />
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { useWorkflowStore } from '@/stores/workflow'
import { ElMessage, ElMessageBox } from 'element-plus'

const workflowStore = useWorkflowStore()

const loading = ref(false)
const tableData = ref([])

// 统计数据
const stats = reactive({
  total: 0,
  pending: 0,
  approved: 0,
  rejected: 0
})

// 筛选表单
const filterForm = reactive({
  processKey: '',
  status: '',
  submitTimeRange: []
})

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info',
    'DRAFT': 'info',
    'SUBMITTED': 'primary'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待处理',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消',
    'DRAFT': '草稿',
    'SUBMITTED': '已提交'
  }
  return textMap[status] || status
}

// 处理搜索
const handleSearch = async () => {
  pagination.current = 1
  await fetchData()
}

// 处理重置
const handleReset = () => {
  Object.assign(filterForm, {
    processKey: '',
    status: '',
    submitTimeRange: []
  })
  handleSearch()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.current = 1
  fetchData()
}

// 处理当前页变化
const handleCurrentChange = (page) => {
  pagination.current = page
  fetchData()
}

// 处理查看
const handleView = (row) => {
  console.log('查看申请:', row)
  // 跳转到申请详情页面
}

// 处理撤销
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤销此申请吗？', '确认撤销', {
      type: 'warning'
    })
    ElMessage.success('撤销成功')
    await fetchData()
  } catch (error) {
    // 用户取消
  }
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟数据
    const mockData = [
      {
        id: 1,
        approvalNumber: '202509180001',
        processName: '请假申请',
        title: '张三的请假申请',
        submitTime: '2025-09-18 09:00:00',
        completeTime: '2025-09-18 10:30:00',
        status: 'APPROVED'
      },
      {
        id: 2,
        approvalNumber: '202509180002',
        processName: '报销申请',
        title: '李四的报销申请',
        submitTime: '2025-09-18 14:00:00',
        completeTime: null,
        status: 'PENDING'
      }
    ]
    
    tableData.value = mockData
    pagination.total = mockData.length
    
    // 更新统计数据
    stats.total = mockData.length
    stats.pending = mockData.filter(item => item.status === 'PENDING').length
    stats.approved = mockData.filter(item => item.status === 'APPROVED').length
    stats.rejected = mockData.filter(item => item.status === 'REJECTED').length
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.application-list {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.filter-section {
  margin-bottom: 24px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.filter-form .el-form-item {
  margin-bottom: 16px;
}

.application-table {
  margin-bottom: 24px;
}

.pagination-section {
  text-align: right;
}
</style>
