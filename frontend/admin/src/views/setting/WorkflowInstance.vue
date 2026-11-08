<template>
  <div class="workflow-instance">
    <el-card class="instance-card">
      <template #header>
        <div class="card-header">
          <el-icon><List /></el-icon>
          <span>流程实例管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="startWorkflow">
              <el-icon><Plus /></el-icon>
              启动流程
            </el-button>
            <el-button @click="refreshInstances">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button @click="exportInstances">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索和过滤 -->
      <div class="search-filters">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索流程名称或实例ID"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedStatus" placeholder="选择状态" clearable @change="handleStatusFilter">
              <el-option label="全部状态" value="" />
              <el-option label="运行中" value="running" />
              <el-option label="已完成" value="completed" />
              <el-option label="已暂停" value="suspended" />
              <el-option label="已终止" value="terminated" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedWorkflow" placeholder="选择流程" clearable @change="handleWorkflowFilter">
              <el-option label="全部流程" value="" />
              <el-option label="请假申请" value="leave_request" />
              <el-option label="采购审批" value="purchase_approval" />
              <el-option label="合同审批" value="contract_approval" />
              <el-option label="项目立项" value="project_approval" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="handleDateFilter"
            />
          </el-col>
          <el-col :span="3">
            <el-button @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-col>
        </el-row>
      </div>
      
      <!-- 流程实例表格 -->
      <div class="instance-table">
        <el-table :data="filteredInstances" v-loading="loading" stripe>
          <el-table-column prop="instanceId" label="实例ID" width="120" />
          <el-table-column prop="workflowName" label="流程名称" width="150" />
          <el-table-column prop="workflowKey" label="流程标识" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="currentTask" label="当前任务" width="150" />
          <el-table-column prop="assignee" label="处理人" width="100" />
          <el-table-column prop="startTime" label="开始时间" width="160" />
          <el-table-column prop="endTime" label="结束时间" width="160" />
          <el-table-column prop="duration" label="耗时" width="100" />
          <el-table-column prop="priority" label="优先级" width="80">
            <template #default="{ row }">
              <el-tag :type="getPriorityTagType(row.priority)" size="small">
                {{ getPriorityText(row.priority) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewInstance(row)">
                查看
              </el-button>
              <el-button type="success" size="small" @click="handleTask(row)" v-if="row.status === 'running'">
                处理
              </el-button>
              <el-button type="warning" size="small" @click="suspendInstance(row)" v-if="row.status === 'running'">
                暂停
              </el-button>
              <el-button type="danger" size="small" @click="terminateInstance(row)" v-if="row.status === 'running'">
                终止
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 流程实例详情对话框 -->
    <el-dialog
      v-model="instanceDialogVisible"
      :title="`流程实例详情 - ${selectedInstance?.instanceId}`"
      width="80%"
    >
      <div class="instance-detail" v-if="selectedInstance">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="实例ID">{{ selectedInstance.instanceId }}</el-descriptions-item>
              <el-descriptions-item label="流程名称">{{ selectedInstance.workflowName }}</el-descriptions-item>
              <el-descriptions-item label="流程标识">{{ selectedInstance.workflowKey }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusTagType(selectedInstance.status)">
                  {{ getStatusText(selectedInstance.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="发起人">{{ selectedInstance.starter }}</el-descriptions-item>
              <el-descriptions-item label="当前处理人">{{ selectedInstance.assignee }}</el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ selectedInstance.startTime }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ selectedInstance.endTime || '未结束' }}</el-descriptions-item>
              <el-descriptions-item label="优先级">
                <el-tag :type="getPriorityTagType(selectedInstance.priority)">
                  {{ getPriorityText(selectedInstance.priority) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="耗时">{{ selectedInstance.duration }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="流程变量" name="variables">
            <el-table :data="selectedInstance.variables" stripe>
              <el-table-column prop="key" label="变量名" width="200" />
              <el-table-column prop="value" label="变量值" min-width="300" />
              <el-table-column prop="type" label="类型" width="100" />
            </el-table>
          </el-tab-pane>
          
          <el-tab-pane label="任务历史" name="tasks">
            <el-timeline>
              <el-timeline-item
                v-for="task in selectedInstance.taskHistory"
                :key="task.taskId"
                :timestamp="task.completeTime"
                :type="getTaskType(task.status)"
              >
                <el-card>
                  <h4>{{ task.taskName }}</h4>
                  <p>处理人：{{ task.assignee }}</p>
                  <p>状态：{{ getTaskStatusText(task.status) }}</p>
                  <p v-if="task.comment">备注：{{ task.comment }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
          
          <el-tab-pane label="流程图" name="diagram">
            <div class="process-diagram">
              <el-icon size="64"><Setting /></el-icon>
              <p>流程图展示区域</p>
              <p>支持查看流程执行路径和当前节点</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, computed } from 'vue'
import { List, Plus, Refresh, Download, Search, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 流程实例管理页面
 * 
 * @author Amos
 * @since 2025-01-09
 */

// 响应式数据
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const instanceDialogVisible = ref(false)
const activeTab = ref('basic')
const selectedInstance = ref(null)

// 搜索和过滤
const searchKeyword = ref('')
const selectedStatus = ref('')
const selectedWorkflow = ref('')
const dateRange = ref([])

// 流程实例数据
const instances = ref([
  {
    instanceId: 'WF001',
    workflowName: '请假申请流程',
    workflowKey: 'leave_request',
    status: 'running',
    currentTask: '部门经理审批',
    assignee: '张经理',
    starter: '李小明',
    startTime: '2025-01-09 09:00:00',
    endTime: null,
    duration: '2小时30分',
    priority: 'normal',
    variables: [
      { key: 'applicant', value: '李小明', type: 'String' },
      { key: 'leaveType', value: '年假', type: 'String' },
      { key: 'startDate', value: '2025-01-10', type: 'Date' },
      { key: 'endDate', value: '2025-01-12', type: 'Date' },
      { key: 'reason', value: '家庭事务', type: 'String' }
    ],
    taskHistory: [
      {
        taskId: 'T001',
        taskName: '提交申请',
        assignee: '李小明',
        status: 'completed',
        completeTime: '2025-01-09 09:00:00',
        comment: '申请年假3天'
      },
      {
        taskId: 'T002',
        taskName: '部门经理审批',
        assignee: '张经理',
        status: 'running',
        completeTime: null,
        comment: null
      }
    ]
  },
  {
    instanceId: 'WF002',
    workflowName: '采购审批流程',
    workflowKey: 'purchase_approval',
    status: 'completed',
    currentTask: '已完成',
    assignee: '系统',
    starter: '王采购',
    startTime: '2025-01-08 14:30:00',
    endTime: '2025-01-09 16:45:00',
    duration: '1天2小时15分',
    priority: 'high',
    variables: [
      { key: 'applicant', value: '王采购', type: 'String' },
      { key: 'amount', value: '50000', type: 'Number' },
      { key: 'supplier', value: 'ABC供应商', type: 'String' },
      { key: 'description', value: '办公设备采购', type: 'String' }
    ],
    taskHistory: [
      {
        taskId: 'T003',
        taskName: '提交采购申请',
        assignee: '王采购',
        status: 'completed',
        completeTime: '2025-01-08 14:30:00',
        comment: '采购办公设备'
      },
      {
        taskId: 'T004',
        taskName: '部门经理审批',
        assignee: '李经理',
        status: 'completed',
        completeTime: '2025-01-08 16:20:00',
        comment: '同意采购'
      },
      {
        taskId: 'T005',
        taskName: '财务审批',
        assignee: '陈财务',
        status: 'completed',
        completeTime: '2025-01-09 10:15:00',
        comment: '预算充足，同意'
      },
      {
        taskId: 'T006',
        taskName: '总经理审批',
        assignee: '刘总',
        status: 'completed',
        completeTime: '2025-01-09 16:45:00',
        comment: '最终批准'
      }
    ]
  },
  {
    instanceId: 'WF003',
    workflowName: '合同审批流程',
    workflowKey: 'contract_approval',
    status: 'suspended',
    currentTask: '法务审核',
    assignee: '赵法务',
    starter: '孙业务',
    startTime: '2025-01-07 10:15:00',
    endTime: null,
    duration: '2天8小时30分',
    priority: 'urgent',
    variables: [
      { key: 'applicant', value: '孙业务', type: 'String' },
      { key: 'contractType', value: '销售合同', type: 'String' },
      { key: 'client', value: 'XYZ公司', type: 'String' },
      { key: 'amount', value: '1000000', type: 'Number' }
    ],
    taskHistory: [
      {
        taskId: 'T007',
        taskName: '提交合同',
        assignee: '孙业务',
        status: 'completed',
        completeTime: '2025-01-07 10:15:00',
        comment: '提交销售合同'
      },
      {
        taskId: 'T008',
        taskName: '业务审核',
        assignee: '周经理',
        status: 'completed',
        completeTime: '2025-01-07 15:30:00',
        comment: '业务条款审核通过'
      },
      {
        taskId: 'T009',
        taskName: '法务审核',
        assignee: '赵法务',
        status: 'suspended',
        completeTime: null,
        comment: '需要补充材料'
      }
    ]
  }
])

// 计算属性
const filteredInstances = computed(() => {
  let filtered = instances.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(item => 
      item.instanceId.toLowerCase().includes(keyword) ||
      item.workflowName.toLowerCase().includes(keyword) ||
      item.starter.toLowerCase().includes(keyword)
    )
  }

  // 状态过滤
  if (selectedStatus.value) {
    filtered = filtered.filter(item => item.status === selectedStatus.value)
  }

  // 流程过滤
  if (selectedWorkflow.value) {
    filtered = filtered.filter(item => item.workflowKey === selectedWorkflow.value)
  }

  // 日期过滤
  if (dateRange.value && dateRange.value.length === 2) {
    const [startDate, endDate] = dateRange.value
    filtered = filtered.filter(item => {
      const itemDate = new Date(item.startTime)
      return itemDate >= startDate && itemDate <= endDate
    })
  }

  return filtered
})

const totalCount = computed(() => filteredInstances.value.length)

// 辅助函数
const getStatusTagType = (status) => {
  const typeMap = {
    'running': 'primary',
    'completed': 'success',
    'suspended': 'warning',
    'terminated': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'running': '运行中',
    'completed': '已完成',
    'suspended': '已暂停',
    'terminated': '已终止'
  }
  return textMap[status] || status
}

const getPriorityTagType = (priority) => {
  const typeMap = {
    'low': 'info',
    'normal': 'success',
    'high': 'warning',
    'urgent': 'danger'
  }
  return typeMap[priority] || 'info'
}

const getPriorityText = (priority) => {
  const textMap = {
    'low': '低',
    'normal': '普通',
    'high': '高',
    'urgent': '紧急'
  }
  return textMap[priority] || priority
}

const getTaskType = (status) => {
  const typeMap = {
    'completed': 'success',
    'running': 'primary',
    'suspended': 'warning',
    'terminated': 'danger'
  }
  return typeMap[status] || 'info'
}

const getTaskStatusText = (status) => {
  const textMap = {
    'completed': '已完成',
    'running': '进行中',
    'suspended': '已暂停',
    'terminated': '已终止'
  }
  return textMap[status] || status
}

// 事件处理
const handleSearch = () => {
  currentPage.value = 1
}

const handleStatusFilter = () => {
  currentPage.value = 1
}

const handleWorkflowFilter = () => {
  currentPage.value = 1
}

const handleDateFilter = () => {
  currentPage.value = 1
}

const resetFilters = () => {
  searchKeyword.value = ''
  selectedStatus.value = ''
  selectedWorkflow.value = ''
  dateRange.value = []
  currentPage.value = 1
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const startWorkflow = () => {
  ElMessage.info('启动流程功能开发中...')
}

const refreshInstances = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功')
  }, 1000)
}

const exportInstances = () => {
  ElMessage.info('导出功能开发中...')
}

const viewInstance = (row) => {
  selectedInstance.value = row
  instanceDialogVisible.value = true
}

const handleTask = (row) => {
  ElMessage.info(`处理任务: ${row.currentTask}`)
}

const suspendInstance = (row) => {
  ElMessageBox.confirm(
    `确定要暂停流程实例 "${row.instanceId}" 吗？`,
    '确认暂停',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    row.status = 'suspended'
    ElMessage.success('流程实例已暂停')
  }).catch(() => {
    ElMessage.info('已取消暂停')
  })
}

const terminateInstance = (row) => {
  ElMessageBox.confirm(
    `确定要终止流程实例 "${row.instanceId}" 吗？此操作不可撤销！`,
    '确认终止',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(() => {
    row.status = 'terminated'
    ElMessage.success('流程实例已终止')
  }).catch(() => {
    ElMessage.info('已取消终止')
  })
}

onMounted(() => {
})
</script>

<style scoped>
.workflow-instance {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.instance-card {
  max-width: 1400px;
  margin: 0 auto;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.header-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.search-filters {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.instance-table {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.instance-detail {
  max-height: 600px;
  overflow-y: auto;
}

.process-diagram {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #909399;
  font-size: 14px;
}

.process-diagram p {
  margin: 10px 0 0 0;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f8f9fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table tr:hover) {
  background-color: #f5f7fa;
}

/* 标签样式优化 */
:deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
}

/* 按钮样式优化 */
:deep(.el-button) {
  border-radius: 4px;
  font-weight: 500;
}

/* 对话框样式优化 */
:deep(.el-dialog__header) {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
}

/* 时间线样式优化 */
:deep(.el-timeline-item__content) {
  padding-bottom: 20px;
}

:deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 12px;
}
</style>
