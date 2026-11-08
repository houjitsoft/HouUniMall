<template>
  <div class="backup-schedule-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>备份计划</h2>
      <p>配置和管理自动备份计划</p>
    </div>

    <!-- 操作区域 -->
    <div class="action-section">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增计划
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedSchedules.length === 0">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </el-col>
        <el-col :span="8" class="text-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索计划名称"
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

    <!-- 计划列表 -->
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
        <el-table-column prop="name" label="计划名称" min-width="200" />
        <el-table-column prop="backupType" label="备份类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getBackupTypeTagType(row.backupType)">
              {{ getBackupTypeText(row.backupType) }}
            </el-tag>
          </template>
        </el-table-column>
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

    <!-- 计划编辑对话框 -->
    <el-dialog
      v-model="showEditDialog"
      :title="editForm.id ? '编辑计划' : '新增计划'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="120px">
        <el-form-item label="计划名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入计划名称" />
        </el-form-item>
        
        <el-form-item label="备份类型" prop="backupType">
          <el-select v-model="editForm.backupType" placeholder="请选择备份类型" style="width: 100%;">
            <el-option label="全量备份" value="full" />
            <el-option label="增量备份" value="incremental" />
            <el-option label="差异备份" value="differential" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="Cron表达式" prop="cronExpression">
          <el-input v-model="editForm.cronExpression" placeholder="请输入Cron表达式" />
          <div class="form-tip">例如: 0 0 2 * * ? (每天凌晨2点执行)</div>
        </el-form-item>
        
        <el-form-item label="备份内容" prop="backupContent">
          <el-checkbox-group v-model="editForm.backupContent">
            <el-checkbox label="database">数据库</el-checkbox>
            <el-checkbox label="files">文件</el-checkbox>
            <el-checkbox label="config">配置文件</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            placeholder="请输入计划描述"
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
    name: '每日全量备份',
    backupType: 'full',
    cronExpression: '0 0 2 * * ?',
    nextRunTime: '2024-01-02 02:00:00',
    status: 'enabled',
    createTime: '2024-01-01 09:00:00'
  },
  {
    id: 2,
    name: '每周增量备份',
    backupType: 'incremental',
    cronExpression: '0 0 3 ? * SUN',
    nextRunTime: '2024-01-07 03:00:00',
    status: 'enabled',
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 3,
    name: '每月差异备份',
    backupType: 'differential',
    cronExpression: '0 0 4 1 * ?',
    nextRunTime: '2024-02-01 04:00:00',
    status: 'disabled',
    createTime: '2024-01-01 11:00:00'
  }
])

const editForm = reactive({
  id: null,
  name: '',
  backupType: 'full',
  cronExpression: '',
  backupContent: ['database'],
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入计划名称', trigger: 'blur' }
  ],
  backupType: [
    { required: true, message: '请选择备份类型', trigger: 'change' }
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
    ElMessage.success('计划列表加载成功')
  } catch (error) {
    ElMessage.error('加载计划列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  // 重置表单
  Object.assign(editForm, {
    id: null,
    name: '',
    backupType: 'full',
    cronExpression: '',
    backupContent: ['database'],
    description: ''
  })
  showEditDialog.value = true
}

const handleEdit = (schedule) => {
  // 填充表单数据
  Object.assign(editForm, {
    id: schedule.id,
    name: schedule.name,
    backupType: schedule.backupType,
    cronExpression: schedule.cronExpression,
    backupContent: schedule.backupContent || ['database'],
    description: schedule.description
  })
  showEditDialog.value = true
}

const handleDelete = async (schedule) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除计划"${schedule.name}"吗？`,
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
    ElMessage.warning('请先选择要删除的计划')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的${selectedSchedules.value.length}个计划吗？`,
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
const getBackupTypeTagType = (type) => {
  const typeMap = {
    full: 'primary',
    incremental: 'success',
    differential: 'warning'
  }
  return typeMap[type] || 'info'
}

const getBackupTypeText = (type) => {
  const typeMap = {
    full: '全量备份',
    incremental: '增量备份',
    differential: '差异备份'
  }
  return typeMap[type] || '未知'
}

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
.backup-schedule-container {
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
