<template>
  <div class="backup-restore-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>备份恢复</h2>
      <p>从备份文件恢复系统数据</p>
    </div>

    <!-- 恢复操作区域 -->
    <div class="restore-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>恢复操作</span>
          </div>
        </template>
        
        <el-form :model="restoreForm" :rules="rules" ref="restoreFormRef" label-width="120px">
          <el-form-item label="备份文件" prop="backupFile">
            <el-select v-model="restoreForm.backupFile" placeholder="请选择要恢复的备份文件" style="width: 100%;">
              <el-option
                v-for="file in backupFiles"
                :key="file.id"
                :label="`${file.name} (${file.size})`"
                :value="file.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="恢复类型" prop="restoreType">
            <el-radio-group v-model="restoreForm.restoreType">
              <el-radio label="full">全量恢复</el-radio>
              <el-radio label="partial">部分恢复</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="恢复内容" prop="restoreContent" v-if="restoreForm.restoreType === 'partial'">
            <el-checkbox-group v-model="restoreForm.restoreContent">
              <el-checkbox label="database">数据库</el-checkbox>
              <el-checkbox label="files">文件</el-checkbox>
              <el-checkbox label="config">配置文件</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          
          <el-form-item label="恢复密码" prop="restorePassword" v-if="selectedBackupFile && selectedBackupFile.encrypted">
            <el-input v-model="restoreForm.restorePassword" placeholder="请输入备份文件密码" show-password />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleRestore" :loading="restoring">
              <el-icon><RefreshLeft /></el-icon>
              开始恢复
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 恢复进度 -->
    <div class="progress-section" v-if="restoreProgress.show">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>恢复进度</span>
          </div>
        </template>
        
        <div class="progress-info">
          <div class="progress-item">
            <div class="progress-label">总体进度:</div>
            <el-progress :percentage="restoreProgress.total" :status="restoreProgress.status" />
          </div>
          
          <div class="progress-item">
            <div class="progress-label">数据库恢复:</div>
            <el-progress :percentage="restoreProgress.database" status="success" />
          </div>
          
          <div class="progress-item">
            <div class="progress-label">文件恢复:</div>
            <el-progress :percentage="restoreProgress.files" status="warning" />
          </div>
          
          <div class="progress-item">
            <div class="progress-label">配置恢复:</div>
            <el-progress :percentage="restoreProgress.config" status="success" />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 恢复日志 -->
    <div class="log-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>恢复日志</span>
          </div>
        </template>
        
        <div class="log-content">
          <pre>{{ restoreLog }}</pre>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  RefreshLeft, Refresh 
} from '@element-plus/icons-vue'

// 数据
const restoring = ref(false)
const restoreFormRef = ref()
const restoreLog = ref('')
const selectedBackupFile = ref(null)

const restoreForm = reactive({
  backupFile: '',
  restoreType: 'full',
  restoreContent: ['database'],
  restorePassword: ''
})

const restoreProgress = reactive({
  show: false,
  total: 0,
  database: 0,
  files: 0,
  config: 0,
  status: ''
})

const backupFiles = ref([
  {
    id: 1,
    name: 'backup_20240101_full.zip',
    size: '2.5GB',
    date: '2024-01-01 02:00:00',
    encrypted: true
  },
  {
    id: 2,
    name: 'backup_20231231_incremental.zip',
    size: '1.2GB',
    date: '2023-12-31 02:00:00',
    encrypted: false
  },
  {
    id: 3,
    name: 'backup_20231230_full.zip',
    size: '2.3GB',
    date: '2023-12-30 02:00:00',
    encrypted: true
  }
])

const rules = {
  backupFile: [
    { required: true, message: '请选择备份文件', trigger: 'change' }
  ],
  restorePassword: [
    { required: true, message: '请输入备份文件密码', trigger: 'blur' }
  ]
}

// 计算属性
const selectedBackupFileComputed = computed(() => {
  return backupFiles.value.find(file => file.id === restoreForm.backupFile) || null
})

// 方法
const loadBackupFiles = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('备份文件列表加载成功')
  } catch (error) {
    ElMessage.error('加载备份文件列表失败: ' + error.message)
  }
}

const handleRestore = async () => {
  try {
    await restoreFormRef.value.validate()
    
    // 检查是否需要密码
    if (selectedBackupFileComputed.value && selectedBackupFileComputed.value.encrypted && !restoreForm.restorePassword) {
      ElMessage.warning('加密的备份文件需要输入密码')
      return
    }
    
    await ElMessageBox.confirm(
      '确定要开始恢复操作吗？这将覆盖当前数据，请确保已做好相关准备。',
      '确认恢复',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 开始恢复过程
    restoring.value = true
    restoreProgress.show = true
    restoreProgress.total = 0
    restoreProgress.database = 0
    restoreProgress.files = 0
    restoreProgress.config = 0
    restoreProgress.status = ''
    restoreLog.value = '开始恢复操作...\n'
    
    // 模拟恢复过程
    simulateRestoreProcess()
  } catch (error) {
    if (error.message) {
      ElMessage.error('恢复失败: ' + error.message)
    }
  }
}

const simulateRestoreProcess = () => {
  let progress = 0
  const interval = setInterval(() => {
    progress += Math.floor(Math.random() * 10) + 1
    if (progress > 100) progress = 100
    
    restoreProgress.total = progress
    restoreProgress.database = Math.min(progress + Math.floor(Math.random() * 20) - 10, 100)
    restoreProgress.files = Math.min(progress + Math.floor(Math.random() * 20) - 10, 100)
    restoreProgress.config = Math.min(progress + Math.floor(Math.random() * 20) - 10, 100)
    
    // 添加日志
    restoreLog.value += `恢复进度: ${progress}%\n`
    
    if (progress >= 100) {
      clearInterval(interval)
      restoreProgress.status = 'success'
      restoreLog.value += '恢复操作完成！\n'
      restoring.value = false
      ElMessage.success('恢复操作完成')
    }
  }, 500)
}

const handleReset = () => {
  restoreFormRef.value.resetFields()
  restoreProgress.show = false
  restoreLog.value = ''
  ElMessage.info('表单已重置')
}

// 生命周期
onMounted(() => {
  loadBackupFiles()
})
</script>

<style scoped>
.backup-restore-container {
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

.card-header {
  font-weight: 600;
  color: #303133;
}

.progress-info {
  padding: 20px 0;
}

.progress-item {
  margin-bottom: 20px;
}

.progress-label {
  margin-bottom: 10px;
  font-weight: 500;
  color: #606266;
}

.log-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
}
</style>
