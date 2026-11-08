<template>
  <div class="workflow-design">
    <el-card class="design-card">
      <template #header>
        <div class="card-header">
          <el-icon><Setting /></el-icon>
          <span>流程设计</span>
          <div class="header-actions">
            <el-button type="primary" @click="createWorkflow">
              <el-icon><Plus /></el-icon>
              新建流程
            </el-button>
            <el-button @click="importWorkflow">
              <el-icon><Upload /></el-icon>
              导入流程
            </el-button>
            <el-button @click="exportWorkflow">
              <el-icon><Download /></el-icon>
              导出流程
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 流程列表 -->
      <div class="workflow-list">
        <el-table :data="workflowList" v-loading="loading" stripe>
          <el-table-column prop="workflowName" label="流程名称" width="200" />
          <el-table-column prop="workflowKey" label="流程标识" width="150" />
          <el-table-column prop="category" label="流程分类" width="120">
            <template #default="{ row }">
              <el-tag :type="getCategoryTagType(row.category)">
                {{ getCategoryText(row.category) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="version" label="版本" width="80" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column prop="updateTime" label="更新时间" width="160" />
          <el-table-column prop="creator" label="创建人" width="100" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="editWorkflow(row)">
                编辑
              </el-button>
              <el-button type="success" size="small" @click="publishWorkflow(row)">
                发布
              </el-button>
              <el-button type="warning" size="small" @click="copyWorkflow(row)">
                复制
              </el-button>
              <el-button type="danger" size="small" @click="deleteWorkflow(row)">
                删除
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

    <!-- 流程设计对话框 -->
    <el-dialog
      v-model="designDialogVisible"
      :title="dialogTitle"
      width="80%"
      :before-close="handleDialogClose"
    >
      <div class="workflow-designer">
        <div class="designer-toolbar">
          <el-button-group>
            <el-button @click="addStartNode">开始节点</el-button>
            <el-button @click="addTaskNode">任务节点</el-button>
            <el-button @click="addGatewayNode">网关节点</el-button>
            <el-button @click="addEndNode">结束节点</el-button>
          </el-button-group>
          <div class="toolbar-right">
            <el-button @click="saveWorkflow">保存</el-button>
            <el-button type="primary" @click="publishWorkflow">发布</el-button>
          </div>
        </div>
        
        <div class="designer-canvas" ref="designerCanvas">
          <!-- 这里可以集成流程图设计器，如 bpmn-js -->
          <div class="canvas-placeholder">
            <el-icon size="64"><Setting /></el-icon>
            <p>流程图设计器</p>
            <p>支持拖拽设计流程节点和连线</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, computed } from 'vue'
import { Setting, Plus, Upload, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 流程设计页面
 * 
 * @author Amos
 * @since 2025-01-09
 */

// 响应式数据
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const designDialogVisible = ref(false)
const dialogTitle = ref('新建流程')

// 流程列表数据
const workflowList = ref([
  {
    id: 1,
    workflowName: '请假申请流程',
    workflowKey: 'leave_request',
    category: 'hr',
    version: '1.0',
    status: 'published',
    createTime: '2025-01-09 10:00:00',
    updateTime: '2025-01-09 15:30:00',
    creator: '管理员'
  },
  {
    id: 2,
    workflowName: '采购审批流程',
    workflowKey: 'purchase_approval',
    category: 'finance',
    version: '2.1',
    status: 'draft',
    createTime: '2025-01-08 14:20:00',
    updateTime: '2025-01-09 09:15:00',
    creator: '财务主管'
  },
  {
    id: 3,
    workflowName: '合同审批流程',
    workflowKey: 'contract_approval',
    category: 'legal',
    version: '1.5',
    status: 'published',
    createTime: '2025-01-07 16:45:00',
    updateTime: '2025-01-08 11:20:00',
    creator: '法务经理'
  },
  {
    id: 4,
    workflowName: '项目立项流程',
    workflowKey: 'project_approval',
    category: 'project',
    version: '1.0',
    status: 'testing',
    createTime: '2025-01-06 09:30:00',
    updateTime: '2025-01-09 16:00:00',
    creator: '项目经理'
  },
  {
    id: 5,
    workflowName: '报销审批流程',
    workflowKey: 'expense_approval',
    category: 'finance',
    version: '3.0',
    status: 'published',
    createTime: '2025-01-05 13:15:00',
    updateTime: '2025-01-08 14:45:00',
    creator: '财务总监'
  }
])

// 计算属性
const totalCount = computed(() => workflowList.value.length)

// 获取分类标签类型
const getCategoryTagType = (category) => {
  const typeMap = {
    'hr': 'success',
    'finance': 'warning',
    'legal': 'danger',
    'project': 'primary',
    'admin': 'info'
  }
  return typeMap[category] || 'info'
}

// 获取分类文本
const getCategoryText = (category) => {
  const textMap = {
    'hr': '人事管理',
    'finance': '财务管理',
    'legal': '法务管理',
    'project': '项目管理',
    'admin': '行政管理'
  }
  return textMap[category] || category
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    'draft': 'info',
    'testing': 'warning',
    'published': 'success',
    'archived': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'draft': '草稿',
    'testing': '测试中',
    'published': '已发布',
    'archived': '已归档'
  }
  return textMap[status] || status
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 新建流程
const createWorkflow = () => {
  dialogTitle.value = '新建流程'
  designDialogVisible.value = true
}

// 导入流程
const importWorkflow = () => {
  ElMessage.info('导入流程功能开发中...')
}

// 导出流程
const exportWorkflow = () => {
  ElMessage.info('导出流程功能开发中...')
}

// 编辑流程
const editWorkflow = (row) => {
  dialogTitle.value = `编辑流程 - ${row.workflowName}`
  designDialogVisible.value = true
}

// 发布流程
const publishWorkflow = (row) => {
  ElMessageBox.confirm(
    `确定要发布流程 "${row.workflowName}" 吗？`,
    '确认发布',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    row.status = 'published'
    ElMessage.success('流程发布成功')
  }).catch(() => {
    ElMessage.info('已取消发布')
  })
}

// 复制流程
const copyWorkflow = (row) => {
  const newWorkflow = {
    ...row,
    id: Date.now(),
    workflowName: `${row.workflowName} - 副本`,
    workflowKey: `${row.workflowKey}_copy`,
    version: '1.0',
    status: 'draft',
    createTime: new Date().toLocaleString(),
    updateTime: new Date().toLocaleString(),
    creator: '当前用户'
  }
  workflowList.value.unshift(newWorkflow)
  ElMessage.success('流程复制成功')
}

// 删除流程
const deleteWorkflow = (row) => {
  ElMessageBox.confirm(
    `确定要删除流程 "${row.workflowName}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = workflowList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      workflowList.value.splice(index, 1)
      ElMessage.success('流程删除成功')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 对话框关闭处理
const handleDialogClose = (done) => {
  ElMessageBox.confirm('确定要关闭吗？未保存的更改将丢失。')
    .then(() => {
      done()
    })
    .catch(() => {
      // 取消关闭
    })
}

// 流程设计器相关方法
const addStartNode = () => {
  ElMessage.info('添加开始节点')
}

const addTaskNode = () => {
  ElMessage.info('添加任务节点')
}

const addGatewayNode = () => {
  ElMessage.info('添加网关节点')
}

const addEndNode = () => {
  ElMessage.info('添加结束节点')
}

const saveWorkflow = () => {
  ElMessage.success('流程保存成功')
  designDialogVisible.value = false
}

onMounted(() => {
})
</script>

<style scoped>
.workflow-design {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.design-card {
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

.workflow-list {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.workflow-designer {
  height: 600px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.designer-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.designer-canvas {
  height: calc(100% - 50px);
  position: relative;
  background-color: #fff;
}

.canvas-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  font-size: 14px;
}

.canvas-placeholder p {
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

:deep(.el-dialog__body) {
  padding: 0;
}
</style>
