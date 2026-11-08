<template>
  <div class="process-content">
    <div class="page-header">
      <h2>流程管理</h2>
      <p>管理和配置所有工作流程</p>
    </div>

    <!-- 流程分类 -->
    <div class="process-categories">
      <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="人事流程" name="HR" />
        <el-tab-pane label="人事助手" name="HR_ASSISTANT" />
        <el-tab-pane label="高级功能" name="ADVANCED" />
        <el-tab-pane label="打卡管理" name="PUNCH" />
      </el-tabs>
    </div>

    <!-- 流程网格 -->
    <div class="process-grid" v-loading="workflowStore.processLoading">
      <div
        v-for="process in filteredProcesses"
        :key="process.processKey"
        class="process-card"
        @click="handleProcessClick(process)"
      >
        <div class="process-icon">
          <el-icon :size="32">
            <component :is="getProcessIcon(process.icon)" />
          </el-icon>
        </div>
        <div class="process-info">
          <h3>{{ process.processName }}</h3>
          <p>{{ process.description }}</p>
          <div class="process-meta">
            <el-tag :type="getStatusType(process.isActive)" size="small">
              {{ process.isActive ? '启用' : '禁用' }}
            </el-tag>
            <span class="process-sort">排序: {{ process.sortOrder }}</span>
          </div>
        </div>
        <div class="process-actions">
          <el-button type="primary" size="small" @click.stop="handleStartProcess(process)">
            启动流程
          </el-button>
          <el-button type="default" size="small" @click.stop="handleConfigProcess(process)">
            配置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!workflowStore.processLoading && filteredProcesses.length === 0" description="暂无流程" />

    <!-- 启动流程对话框 -->
    <el-dialog
      v-model="startDialogVisible"
      :title="`启动${selectedProcess?.processName}`"
      width="600px"
    >
      <ProcessStartForm
        v-if="selectedProcess"
        :process="selectedProcess"
        @submit="handleStartSubmit"
        @cancel="startDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkflowStore } from '@/stores/workflow'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  Money,
  Wallet,
  Promotion,
  ShoppingCart,
  Clock,
  Van,
  Stamp,
  House,
  TrendCharts,
  Document,
  Location
} from '@element-plus/icons-vue'
import ProcessStartForm from '@/components/ProcessStartForm.vue'

const router = useRouter()
const workflowStore = useWorkflowStore()

const activeCategory = ref('all')
const startDialogVisible = ref(false)
const selectedProcess = ref(null)

// 图标映射
const iconMap = {
  calendar: Calendar,
  money: Money,
  wallet: Wallet,
  plane: Promotion,
  'shopping-cart': ShoppingCart,
  clock: Clock,
  car: Van,
  stamp: Stamp,
  house: House,
  chart: TrendCharts,
  document: Document,
  location: Location
}

// 过滤后的流程列表
const filteredProcesses = computed(() => {
  if (activeCategory.value === 'all') {
    return workflowStore.processList
  }
  return workflowStore.processList.filter(process => process.processCategory === activeCategory.value)
})

// 获取流程图标
const getProcessIcon = (iconName) => {
  return iconMap[iconName] || Document
}

// 获取状态类型
const getStatusType = (isActive) => {
  return isActive ? 'success' : 'danger'
}

// 处理分类切换
const handleCategoryChange = (category) => {
  activeCategory.value = category
}

// 处理流程点击
const handleProcessClick = (process) => {
  router.push(`/workflow/processes/${process.processKey}`)
}

// 处理启动流程
const handleStartProcess = (process) => {
  selectedProcess.value = process
  startDialogVisible.value = true
}

// 处理配置流程
const handleConfigProcess = (process) => {
  router.push(`/workflow/processes/${process.processKey}/rules`)
}

// 处理启动提交
const handleStartSubmit = async (formData) => {
  try {
    await workflowStore.startProcess({
      processKey: selectedProcess.value.processKey,
      ...formData
    })
    ElMessage.success('流程启动成功')
    startDialogVisible.value = false
  } catch (error) {
    ElMessage.error('流程启动失败: ' + error.message)
  }
}

// 初始化
onMounted(async () => {
  try {
    await workflowStore.fetchProcessList()
  } catch (error) {
    ElMessage.error('获取流程列表失败')
  }
})
</script>

<style scoped>
.process-list {
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

.process-categories {
  margin-bottom: 24px;
}

.process-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.process-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
}

.process-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.process-icon {
  text-align: center;
  margin-bottom: 16px;
  color: #409eff;
}

.process-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.process-info p {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.process-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.process-sort {
  color: #909399;
  font-size: 12px;
}

.process-actions {
  display: flex;
  gap: 8px;
}

.process-actions .el-button {
  flex: 1;
}
</style>
