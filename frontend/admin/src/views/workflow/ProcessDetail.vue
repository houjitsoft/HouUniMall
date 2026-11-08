<template>
  <div class="process-content">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
        <div class="process-info">
          <h2>{{ processConfig?.processName }}</h2>
          <p>{{ processConfig?.description }}</p>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleStartProcess">
          启动流程
        </el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="process-tabs">
      <el-tab-pane label="申请记录" name="records">
        <ProcessRecords :process-key="processKey" />
      </el-tab-pane>
      <el-tab-pane label="模板设置" name="template">
        <ProcessTemplate :process-key="processKey" />
      </el-tab-pane>
      <el-tab-pane label="规则设置" name="rules">
        <ProcessRules :process-key="processKey" />
      </el-tab-pane>
    </el-tabs>

    <!-- 启动流程对话框 -->
    <el-dialog
      v-model="startDialogVisible"
      :title="`启动${processConfig?.processName}`"
      width="600px"
    >
      <ProcessStartForm
        v-if="processConfig"
        :process="processConfig"
        @submit="handleStartSubmit"
        @cancel="startDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useWorkflowStore } from '@/stores/workflow'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import ProcessRecords from '@/components/ProcessRecords.vue'
import ProcessTemplate from '@/components/ProcessTemplate.vue'
import ProcessRules from '@/components/ProcessRules.vue'
import ProcessStartForm from '@/components/ProcessStartForm.vue'

const route = useRoute()
const workflowStore = useWorkflowStore()

const activeTab = ref('records')
const startDialogVisible = ref(false)

const processKey = computed(() => route.params.processKey)

const processConfig = computed(() => workflowStore.currentProcessConfig)

// 处理启动流程
const handleStartProcess = () => {
  startDialogVisible.value = true
}

// 处理启动提交
const handleStartSubmit = async (formData) => {
  try {
    await workflowStore.startProcess({
      processKey: processKey.value,
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
    await workflowStore.fetchProcessConfig(processKey.value)
  } catch (error) {
    ElMessage.error('获取流程配置失败')
  }
})
</script>

<style scoped>
.process-detail {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.process-info h2 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.process-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.process-tabs {
  margin-top: 16px;
}
</style>
