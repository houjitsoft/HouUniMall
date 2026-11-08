<template>
  <div class="task-content">
    <div class="page-header">
      <h2>待办任务</h2>
      <p>处理需要我审批或办理的任务</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card urgent">
        <div class="stat-number">{{ stats.urgent }}</div>
        <div class="stat-label">紧急任务</div>
      </div>
      <div class="stat-card pending">
        <div class="stat-number">{{ stats.pending }}</div>
        <div class="stat-label">待处理</div>
      </div>
      <div class="stat-card today">
        <div class="stat-number">{{ stats.today }}</div>
        <div class="stat-label">今日到期</div>
      </div>
      <div class="stat-card overdue">
        <div class="stat-number">{{ stats.overdue }}</div>
        <div class="stat-label">已逾期</div>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="任务类型">
          <el-select v-model="filterForm.taskType" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="审批任务" value="APPROVAL" />
            <el-option label="办理任务" value="HANDLING" />
            <el-option label="抄送任务" value="CC" />
          </el-select>
        </el-form-item>

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

        <el-form-item label="优先级">
          <el-select v-model="filterForm.priority" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>

        <el-form-item label="创建时间">
          <el-date-picker
            v-model="filterForm.createTimeRange"
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

    <!-- 任务列表 -->
    <div class="task-table">
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="taskName" label="任务名称" min-width="200">
          <template #default="{ row }">
            <div class="task-name">
              <div class="task-title">{{ row.taskName }}</div>
              <div class="task-process">{{ row.processName }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="applicantName" label="申请人" width="120" />
        
        <el-table-column prop="applicantDepartment" label="部门" width="150" />
        
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="dueTime" label="到期时间" width="160">
          <template #default="{ row }">
            <div :class="{ 'overdue': isOverdue(row.dueTime) }">
              {{ formatDateTime(row.dueTime) }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click.stop="handleProcess(row)">
              处理
            </el-button>
            <el-button type="default" size="small" @click.stop="handleView(row)">
              查看
            </el-button>
            <el-button type="warning" size="small" @click.stop="handleDelegate(row)">
              委派
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
    <el-empty v-if="!loading && tableData.length === 0" description="暂无待办任务" />

    <!-- 任务处理对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      :title="`处理任务 - ${selectedTask?.taskName}`"
      width="600px"
    >
      <TaskProcessForm
        v-if="selectedTask"
        :task="selectedTask"
        @submit="handleProcessSubmit"
        @cancel="processDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { useWorkflowStore } from '@/stores/workflow'
import { ElMessage, ElMessageBox } from 'element-plus'
import TaskProcessForm from '@/components/TaskProcessForm.vue'

const workflowStore = useWorkflowStore()

const loading = ref(false)
const tableData = ref([])
const processDialogVisible = ref(false)
const selectedTask = ref(null)

// 统计数据
const stats = reactive({
  urgent: 0,
  pending: 0,
  today: 0,
  overdue: 0
})

// 筛选表单
const filterForm = reactive({
  taskType: '',
  processKey: '',
  priority: '',
  createTimeRange: []
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

// 判断是否逾期
const isOverdue = (dueTime) => {
  if (!dueTime) return false
  return new Date(dueTime) < new Date()
}

// 获取优先级类型
const getPriorityType = (priority) => {
  const typeMap = {
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'success'
  }
  return typeMap[priority] || 'info'
}

// 获取优先级文本
const getPriorityText = (priority) => {
  const textMap = {
    'HIGH': '高',
    'MEDIUM': '中',
    'LOW': '低'
  }
  return textMap[priority] || priority
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'PROCESSING': 'primary',
    'COMPLETED': 'success',
    'DELEGATED': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'DELEGATED': '已委派'
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
    taskType: '',
    processKey: '',
    priority: '',
    createTimeRange: []
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

// 处理行点击
const handleRowClick = (row) => {
  handleView(row)
}

// 处理任务
const handleProcess = (row) => {
  selectedTask.value = row
  processDialogVisible.value = true
}

// 处理查看
const handleView = (row) => {
  console.log('查看任务:', row)
  // 跳转到任务详情页面
}

// 处理委派
const handleDelegate = async (row) => {
  try {
    await ElMessageBox.confirm('确定要委派此任务吗？', '确认委派', {
      type: 'warning'
    })
    ElMessage.success('委派成功')
    await fetchData()
  } catch (error) {
    // 用户取消
  }
}

// 处理任务提交
const handleProcessSubmit = async (formData) => {
  try {
    await workflowStore.completeTask({
      taskId: selectedTask.value.id,
      ...formData
    })
    ElMessage.success('任务处理成功')
    processDialogVisible.value = false
    await fetchData()
  } catch (error) {
    ElMessage.error('任务处理失败: ' + error.message)
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
        taskName: '审批张三的请假申请',
        processName: '请假申请',
        applicantName: '张三',
        applicantDepartment: '厚匠科技/技术部',
        priority: 'MEDIUM',
        createTime: '2025-09-18 09:00:00',
        dueTime: '2025-09-19 18:00:00',
        status: 'PENDING'
      },
      {
        id: 2,
        taskName: '审批李四的报销申请',
        processName: '报销申请',
        applicantName: '李四',
        applicantDepartment: '厚匠科技/市场部',
        priority: 'HIGH',
        createTime: '2025-09-18 14:00:00',
        dueTime: '2025-09-18 18:00:00',
        status: 'PENDING'
      }
    ]
    
    tableData.value = mockData
    pagination.total = mockData.length
    
    // 更新统计数据
    stats.urgent = mockData.filter(item => item.priority === 'HIGH').length
    stats.pending = mockData.filter(item => item.status === 'PENDING').length
    stats.today = mockData.filter(item => {
      const today = new Date()
      const dueTime = new Date(item.dueTime)
      return dueTime.toDateString() === today.toDateString()
    }).length
    stats.overdue = mockData.filter(item => isOverdue(item.dueTime)).length
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
.task-list {
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
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  color: white;
}

.stat-card.urgent {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
}

.stat-card.pending {
  background: linear-gradient(135deg, #feca57 0%, #ff9ff3 100%);
}

.stat-card.today {
  background: linear-gradient(135deg, #48dbfb 0%, #0abde3 100%);
}

.stat-card.overdue {
  background: linear-gradient(135deg, #ff3838 0%, #c44569 100%);
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

.task-table {
  margin-bottom: 24px;
}

.task-name {
  cursor: pointer;
}

.task-title {
  font-weight: 500;
  color: #303133;
}

.task-process {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.overdue {
  color: #f56c6c;
  font-weight: 500;
}

.pagination-section {
  text-align: right;
}
</style>
