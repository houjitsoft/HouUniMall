<template>
  <div class="task-schedule-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>任务调度</h2>
      <p>配置和管理任务的执行计划</p>
    </div>

    <!-- 操作区域 -->
    <div class="action-section">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增调度
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedSchedules.length === 0">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </el-col>
        <el-col :span="8" class="text-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索调度名称"
            clearable
            @keyup.enter="handleSearch"
            style="width: 200px; margin-right: 10px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 调度列表 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="scheduleList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="调度名称" min-width="200" />
        <el-table-column prop="taskName" label="关联任务" min-width="150" />
        <el-table-column prop="cronExpression" label="Cron表达式" min-width="150" />
        <el-table-column prop="nextRunTime" label="下次执行时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="enabled"
              inactive-value="disabled"
              @change="handleStatusChange(row)"
            />
            <el-tag :type="getStatusTagType(row.status)" style="margin-left: 10px;">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
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

    <!-- 调度编辑对话框 -->
    <el-dialog
      v-model="showEditDialog"
      :title="editForm.id ? '编辑调度' : '新增调度'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="120px">
        <el-form-item label="调度名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入调度名称" />
        </el-form-item>
        
        <el-form-item label="关联任务" prop="taskId">
          <el-select v-model="editForm.taskId" placeholder="请选择关联任务" style="width: 100%;">
            <el-option
              v-for="task in taskOptions"
              :key="task.id"
              :label="task.name"
              :value="task.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="Cron表达式" prop="cronExpression">
          <el-input v-model="editForm.cronExpression" placeholder="请输入Cron表达式" />
          <div class="form-tip">例如: 0 0 12 * * ? (每天中午12点执行)</div>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            placeholder="请输入调度描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
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
  Plus, Delete, Search, Edit 
} from '@element-plus/icons-vue'

// 数据
const loading = ref(false)
const saving = ref(false)
const showEditDialog = ref(false)
const editFormRef = ref()
const selectedSchedules = ref([])
const searchKeyword = ref('')

const scheduleList = ref([
  {
    id: 1,
    name: '每日数据备份',
    taskName: '数据备份任务',
    taskId: 1,
    cronExpression: '0 0 2 * * ?',
    nextRunTime: '2024-01-02 02:00:00',
    status: 'enabled',
    createTime: '2024-01-01 09:00:00'
  },
  {
    id: 2,
    name: '每周日志清理',
    taskName: '日志清理任务',
    taskId: 2,
    cronExpression: '0 0 3 ? * SUN',
    nextRunTime: '2024-01-07 03:00:00',
    status: 'enabled',
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 3,
    name: '每小时邮件检查',
    taskName: '邮件发送任务',
    taskId: 3,
    cronExpression: '0 0 * * * ?',
    nextRunTime: '2024-01-01 12:00:00',
    status: 'disabled',
    createTime: '2024-01-01 11:00:00'
  }
])

const taskOptions = ref([
  { id: 1, name: '数据备份任务' },
  { id: 2, name: '日志清理任务' },
  { id: 3, name: '邮件发送任务' }
])

const editForm = reactive({
  id: null,
  name: '',
  taskId: '',
  cronExpression: '',
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入调度名称', trigger: 'blur' }
  ],
  taskId: [
    { required: true, message: '请选择关联任务', trigger: 'change' }
  ],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' }
  ]
}

const pagination = reactive({
  current: 1,
  size: 20,
  total: 3
})

// 方法
const loadScheduleList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('调度列表加载成功')
  } catch (error) {
    ElMessage.error('加载调度列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  // 重置表单
  Object.assign(editForm, {
    id: null,
    name: '',
    taskId: '',
    cronExpression: '',
    description: ''
  })
  showEditDialog.value = true
}

const handleEdit = (schedule) => {
  // 填充表单数据
  Object.assign(editForm, {
    id: schedule.id,
    name: schedule.name,
    taskId: schedule.taskId,
    cronExpression: schedule.cronExpression,
    description: schedule.description
  })
  showEditDialog.value = true
}

const handleDelete = async (schedule) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除调度"${schedule.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('删除成功')
    loadScheduleList()
  } catch {
    ElMessage.info('已取消删除')
  }
}

const handleBatchDelete = async () => {
  if (selectedSchedules.value.length === 0) {
    ElMessage.warning('请先选择要删除的调度')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的${selectedSchedules.value.length}个调度吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('批量删除成功')
    loadScheduleList()
  } catch {
    ElMessage.info('已取消删除')
  }
}

const handleSave = async () => {
  try {
    await editFormRef.value.validate()
    saving.value = true
    
    // 模拟保存操作
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('保存成功')
    showEditDialog.value = false
    loadScheduleList()
  } catch (error) {
    if (error.message) {
      ElMessage.error('保存失败: ' + error.message)
    }
  } finally {
    saving.value = false
  }
}

const handleStatusChange = async (schedule) => {
  try {
    // 模拟状态更新
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('状态更新成功')
  } catch (error) {
    // 恢复原状态
    schedule.status = schedule.status === 'enabled' ? 'disabled' : 'enabled'
    ElMessage.error('状态更新失败: ' + error.message)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadScheduleList()
}

const handleSelectionChange = (selection) => {
  selectedSchedules.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadScheduleList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadScheduleList()
}

// 状态相关方法
const getStatusTagType = (status) => {
  const statusMap = {
    enabled: 'success',
    disabled: 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    enabled: '启用',
    disabled: '禁用'
  }
  return statusMap[status] || '未知'
}

// 生命周期
onMounted(() => {
  loadScheduleList()
})
</script>

<style scoped>
.task-schedule-container {
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

.action-section {
  margin-bottom: 20px;
}

.text-right {
  text-align: right;
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

.form-tip {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}
</style>
