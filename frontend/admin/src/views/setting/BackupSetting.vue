<template>
  <div class="backup-setting-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>备份设置</h2>
      <p>配置系统备份相关参数</p>
    </div>

    <!-- 设置表单 -->
    <el-card class="setting-card">
      <template #header>
        <div class="card-header">
          <span>备份基础配置</span>
        </div>
      </template>
      
      <el-form :model="settingForm" :rules="rules" ref="settingFormRef" label-width="150px">
        <el-form-item label="备份开关" prop="backupEnabled">
          <el-switch v-model="settingForm.backupEnabled" />
          <span class="form-tip">是否启用自动备份</span>
        </el-form-item>
        
        <el-form-item label="备份路径" prop="backupPath">
          <el-input v-model="settingForm.backupPath" placeholder="请输入备份文件存储路径" />
          <span class="form-tip">备份文件存储的目录路径</span>
        </el-form-item>
        
        <el-form-item label="保留备份数量" prop="retainCount">
          <el-input-number 
            v-model="settingForm.retainCount" 
            :min="1" 
            :max="100" 
            controls-position="right"
          />
          <span class="form-tip">保留最近的备份数量</span>
        </el-form-item>
        
        <el-form-item label="压缩格式" prop="compressFormat">
          <el-select v-model="settingForm.compressFormat" placeholder="请选择压缩格式">
            <el-option label="ZIP" value="zip" />
            <el-option label="TAR.GZ" value="tar.gz" />
            <el-option label="RAR" value="rar" />
          </el-select>
          <span class="form-tip">备份文件的压缩格式</span>
        </el-form-item>
        
        <el-form-item label="加密备份" prop="encryptBackup">
          <el-switch v-model="settingForm.encryptBackup" />
          <span class="form-tip">是否对备份文件进行加密</span>
        </el-form-item>
        
        <el-form-item label="加密密码" prop="encryptPassword" v-if="settingForm.encryptBackup">
          <el-input v-model="settingForm.encryptPassword" placeholder="请输入加密密码" show-password />
        </el-form-item>
        
        <el-form-item label="备份通知" prop="backupNotification">
          <el-switch v-model="settingForm.backupNotification" />
          <span class="form-tip">备份完成后是否发送通知</span>
        </el-form-item>
        
        <el-form-item label="通知邮箱" prop="notificationEmail" v-if="settingForm.backupNotification">
          <el-input v-model="settingForm.notificationEmail" placeholder="请输入通知邮箱，多个邮箱用逗号分隔" />
        </el-form-item>
      </el-form>

      <div class="form-actions">
        <el-button type="primary" @click="handleSave" :loading="saving">
          <el-icon><Check /></el-icon>
          保存设置
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Refresh } from '@element-plus/icons-vue'

// 数据
const saving = ref(false)
const settingFormRef = ref()

const settingForm = reactive({
  backupEnabled: true,
  backupPath: '/backup',
  retainCount: 10,
  compressFormat: 'zip',
  encryptBackup: false,
  encryptPassword: '',
  backupNotification: true,
  notificationEmail: ''
})

const rules = {
  backupPath: [
    { required: true, message: '请输入备份路径', trigger: 'blur' }
  ],
  retainCount: [
    { required: true, message: '请输入保留备份数量', trigger: 'blur' }
  ],
  notificationEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 方法
const loadSettings = async () => {
  try {
    // 模拟加载设置
    ElMessage.success('设置加载成功')
  } catch (error) {
    ElMessage.error('加载设置失败: ' + error.message)
  }
}

const handleSave = async () => {
  try {
    await settingFormRef.value.validate()
    saving.value = true
    
    // 模拟保存设置
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('设置保存成功')
  } catch (error) {
    if (error.message) {
      ElMessage.error('保存失败: ' + error.message)
    }
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  settingFormRef.value.resetFields()
  ElMessage.info('设置已重置')
}

// 生命周期
onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.backup-setting-container {
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

.setting-card {
  max-width: 800px;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}
</style>
