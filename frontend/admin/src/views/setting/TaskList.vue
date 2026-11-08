<template>
  <div class="task-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>任务列表</h2>
      <p>管理系统中的所有任务</p>
    </div>

    <!-- 搜索和操作区域 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索任务名称"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="任务状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待执行" value="pending" />
            <el-option label="执行中" value="running" />
            <el-option label="已完成" value="completed" />
            <el-option label="已失败" value="failed" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.priority" placeholder="优先级" clearable>
            <el-option label="全部" value="" />
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-col>
        <el-col :span="4">
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新建任务
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 任务列表 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="taskList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="任务名称" min-width="200" />
        <el-table-column prop="description" label="任务描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assignee" label="执行人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="任务详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentTask" class="task-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusTagType(currentTask.status)">
              {{ getStatusText(currentTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(currentTask.priority)">
              {{ getPriorityText(currentTask.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行人">{{ currentTask.assignee }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentTask.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="任务描述" :span="2">
            {{ currentTask.description }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentTask)">编辑任务</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Refresh, View, Edit, Delete 
} from '@element-plus/icons-vue'

// 数据
const loading = ref(false)
const showDetailDialog = ref(false)
const currentTask = ref(null)
const taskList = ref([])
const selectedTasks = ref([])

const searchForm = reactive({
  keyword: '',
  status: '',
  priority: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 模拟数据
const mockTasks = [
  {
    id: 1,
    name: '数据备份任务',
    description: '每日自动备份数据库',
    status: 'running',
    priority: 'high',
    assignee: '系统管理员',
    createTime: '2024-01-01 09:00:00',
    updateTime: '2024-01-01 09:30:00'
  },
  {
    id: 2,
    name: '日志清理任务',
    description: '清理30天前的系统日志',
    status: 'completed',
    priority: 'medium',
    assignee: '运维人员',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:15:00'
  },
  {
    id: 3,
    name: '邮件发送任务',
    description: '发送系统通知邮件',
    status: 'pending',
    priority: 'low',
    assignee: '邮件服务',
    createTime: '2024-01-01 11:00:00',
    updateTime: '2024-01-01 11:00:00'
  }
]

// 方法
const loadTaskList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    taskList.value = mockTasks
    pagination.total = mockTasks.length
    
    ElMessage.success('任务列表加载成功')
  } catch (error) {
    ElMessage.error('加载任务列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadTaskList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.priority = ''
  handleSearch()
}

const handleAdd = () => {
  ElMessage.info('新建任务功能开发中...')
}

const handleView = (task) => {
  currentTask.value = task
  showDetailDialog.value = true
}

const handleEdit = (task) => {
  ElMessage.info('编辑任务功能开发中...')
}

const handleDelete = async (task) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除任务"${task.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('删除成功')
    loadTaskList()
  } catch {
    ElMessage.info('已取消删除')
  }
}

const handleSelectionChange = (selection) => {
  selectedTasks.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadTaskList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadTaskList()
}

// 状态相关方法
const getStatusTagType = (status) => {
  const statusMap = {
    pending: 'warning',
    running: 'primary',
    completed: 'success',
    failed: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待执行',
    running: '执行中',
    completed: '已完成',
    failed: '已失败'
  }
  return statusMap[status] || '未知'
}

const getPriorityTagType = (priority) => {
  const priorityMap = {
    high: 'danger',
    medium: 'warning',
    low: 'success'
  }
  return priorityMap[priority] || 'info'
}

const getPriorityText = (priority) => {
  const priorityMap = {
    high: '高',
    medium: '中',
    low: '低'
  }
  return priorityMap[priority] || '未知'
}

// 生命周期
onMounted(() => {
  loadTaskList()
})
</script>

<style scoped>
.task-list-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-section {
  margin-top: 20px;
  text-align: right;
}

.task-detail {
  padding: 20px 0;
}
</style>
