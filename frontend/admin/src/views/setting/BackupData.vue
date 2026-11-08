<template>
  <div class="backup-data-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>数据备份</h2>
      <p>执行和管理数据备份操作</p>
    </div>

    <!-- 备份操作区域 -->
    <div class="backup-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>立即备份</span>
          </div>
        </template>
        
        <el-form :model="backupForm" :rules="rules" ref="backupFormRef" label-width="120px">
          <el-form-item label="备份名称" prop="backupName">
            <el-input v-model="backupForm.backupName" placeholder="请输入备份名称" />
          </el-form-item>
          
          <el-form-item label="备份类型" prop="backupType">
            <el-radio-group v-model="backupForm.backupType">
              <el-radio label="full">全量备份</el-radio>
              <el-radio label="incremental">增量备份</el-radio>
              <el-radio label="differential">差异备份</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="备份内容" prop="backupContent">
            <el-checkbox-group v-model="backupForm.backupContent">
              <el-checkbox label="database">数据库</el-checkbox>
              <el-checkbox label="files">文件</el-checkbox>
              <el-checkbox label="config">配置文件</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          
          <el-form-item label="压缩加密" prop="compressEncrypt">
            <el-switch v-model="backupForm.compressEncrypt" />
            <span class="form-tip">是否对备份文件进行压缩和加密</span>
          </el-form-item>
          
          <el-form-item label="加密密码" prop="encryptPassword" v-if="backupForm.compressEncrypt">
            <el-input v-model="backupForm.encryptPassword" placeholder="请输入加密密码" show-password />
          </el-form-item>
          
          <el-form-item label="描述" prop="description">
            <el-input 
              v-model="backupForm.description" 
              type="textarea" 
              placeholder="请输入备份描述"
              :rows="3"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleBackup" :loading="backingUp">
              <el-icon><Document /></el-icon>
              开始备份
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 备份进度 -->
    <div class="progress-section" v-if="backupProgress.show">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>备份进度</span>
          </div>
        </template>
        
        <div class="progress-info">
          <div class="progress-item">
            <div class="progress-label">总体进度:</div>
            <el-progress :percentage="backupProgress.total" :status="backupProgress.status" />
          </div>
          
          <div class="progress-item">
            <div class="progress-label">数据库备份:</div>
            <el-progress :percentage="backupProgress.database" status="success" />
          </div>
          
          <div class="progress-item">
            <div class="progress-label">文件备份:</div>
            <el-progress :percentage="backupProgress.files" status="warning" />
          </div>
          
          <div class="progress-item">
            <div class="progress-label">配置备份:</div>
            <el-progress :percentage="backupProgress.config" status="success" />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 备份历史 -->
    <div class="history-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>备份历史</span>
            <el-button type="primary" @click="loadBackupHistory">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>
        
        <el-table :data="backupHistory" stripe border style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="备份名称" min-width="200" />
          <el-table-column prop="backupType" label="备份类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getBackupTypeTagType(row.backupType)">
                {{ getBackupTypeText(row.backupType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="size" label="文件大小" width="120" />
          <el-table-column prop="duration" label="耗时" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleDownload(row)">
                下载
              </el-button>
              <el-button type="warning" size="small" @click="handleRestore(row)">
                恢复
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, Refresh 
} from '@element-plus/icons-vue'

// 数据
const backingUp = ref(false)
const backupFormRef = ref()

const backupForm = reactive({
  backupName: '',
  backupType: 'full',
  backupContent: ['database', 'files', 'config'],
  compressEncrypt: true,
  encryptPassword: '',
  description: ''
})

const backupProgress = reactive({
  show: false,
  total: 0,
  database: 0,
  files: 0,
  config: 0,
  status: ''
})

const backupHistory = ref([
  {
    id: 1,
    name: '每日全量备份',
    backupType: 'full',
    size: '2.5GB',
    duration: '15分钟',
    status: 'success',
    createTime: '2024-01-01 02:00:00'
  },
  {
    id: 2,
    name: '每周增量备份',
    backupType: 'incremental',
    size: '1.2GB',
    duration: '8分钟',
    status: 'success',
    createTime: '2023-12-31 02:00:00'
  },
  {
    id: 3,
    name: '手动备份',
    backupType: 'full',
    size: '2.3GB',
    duration: '14分钟',
    status: 'failed',
    createTime: '2023-12-30 10:00:00'
  }
])

const rules = {
  backupName: [
    { required: true, message: '请输入备份名称', trigger: 'blur' }
  ],
  encryptPassword: [
    { required: true, message: '请输入加密密码', trigger: 'blur' }
  ]
}

// 方法
const loadBackupHistory = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('备份历史加载成功')
  } catch (error) {
    ElMessage.error('加载备份历史失败: ' + error.message)
  }
}

const handleBackup = async () => {
  try {
    await backupFormRef.value.validate()
    
    // 检查是否需要密码
    if (backupForm.compressEncrypt && !backupForm.encryptPassword) {
      ElMessage.warning('启用压缩加密时需要输入密码')
      return
    }
    
    // 开始备份过程
    backingUp.value = true
    backupProgress.show = true
    backupProgress.total = 0
    backupProgress.database = 0
    backupProgress.files = 0
    backupProgress.config = 0
    backupProgress.status = ''
    
    // 模拟备份过程
    simulateBackupProcess()
  } catch (error) {
    if (error.message) {
      ElMessage.error('备份失败: ' + error.message)
    }
  }
}

const simulateBackupProcess = () => {
  let progress = 0
  const interval = setInterval(() => {
    progress += Math.floor(Math.random() * 10) + 1
    if (progress > 100) progress = 100
    
    backupProgress.total = progress
    backupProgress.database = Math.min(progress + Math.floor(Math.random() * 20) - 10, 100)
    backupProgress.files = Math.min(progress + Math.floor(Math.random() * 20) - 10, 100)
    backupProgress.config = Math.min(progress + Math.floor(Math.random() * 20) - 10, 100)
    
    if (progress >= 100) {
      clearInterval(interval)
      backupProgress.status = 'success'
      backingUp.value = false
      ElMessage.success('备份操作完成')
      
      // 添加到备份历史
      const newBackup = {
        id: backupHistory.value.length + 1,
        name: backupForm.backupName || `备份_${new Date().getTime()}`,
        backupType: backupForm.backupType,
        size: '2.1GB',
        duration: '12分钟',
        status: 'success',
        createTime: new Date().toLocaleString()
      }
      backupHistory.value.unshift(newBackup)
    }
  }, 500)
}

const handleReset = () => {
  backupFormRef.value.resetFields()
  backupProgress.show = false
  ElMessage.info('表单已重置')
}

const handleDownload = (backup) => {
  ElMessage.info(`开始下载备份: ${backup.name}`)
  // 模拟下载操作
}

const handleRestore = async (backup) => {
  try {
    await ElMessageBox.confirm(
      `确定要从备份"${backup.name}"恢复数据吗？这将覆盖当前数据，请确保已做好相关准备。`,
      '确认恢复',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('开始恢复操作')
    // 跳转到恢复页面
    // router.push('/setting/backup/restore')
  } catch {
    ElMessage.info('已取消恢复')
  }
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
    success: 'success',
    failed: 'danger',
    pending: 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    success: '成功',
    failed: '失败',
    pending: '待处理'
  }
  return statusMap[status] || '未知'
}

// 生命周期
onMounted(() => {
  loadBackupHistory()
})
</script>

<style scoped>
.backup-data-container {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
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
</style>
