<template>
  <div class="monitor-setting-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>监控设置</h2>
      <p>配置系统监控相关参数</p>
    </div>

    <!-- 设置表单 -->
    <el-card class="setting-card">
      <template #header>
        <div class="card-header">
          <span>监控基础配置</span>
        </div>
      </template>
      
      <el-form :model="settingForm" :rules="rules" ref="settingFormRef" label-width="150px">
        <el-form-item label="监控开关" prop="monitorEnabled">
          <el-switch v-model="settingForm.monitorEnabled" />
          <span class="form-tip">是否启用系统监控</span>
        </el-form-item>
        
        <el-form-item label="数据采集频率" prop="collectInterval">
          <el-input-number 
            v-model="settingForm.collectInterval" 
            :min="1" 
            :max="3600" 
            controls-position="right"
          />
          <span class="form-tip">秒（数据采集间隔时间）</span>
        </el-form-item>
        
        <el-form-item label="数据保留天数" prop="retentionDays">
          <el-input-number 
            v-model="settingForm.retentionDays" 
            :min="1" 
            :max="365" 
            controls-position="right"
          />
          <span class="form-tip">天（监控数据保留时间）</span>
        </el-form-item>
        
        <el-form-item label="告警阈值" prop="alertThreshold">
          <el-input-number 
            v-model="settingForm.alertThreshold" 
            :min="1" 
            :max="100" 
            controls-position="right"
            :step="0.1"
          />
          <span class="form-tip">%（系统资源使用率告警阈值）</span>
        </el-form-item>
        
        <el-form-item label="告警通知方式" prop="alertMethod">
          <el-checkbox-group v-model="settingForm.alertMethod">
            <el-checkbox label="email">邮件通知</el-checkbox>
            <el-checkbox label="sms">短信通知</el-checkbox>
            <el-checkbox label="webhook">Webhook通知</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="通知邮箱" prop="notificationEmail" v-if="settingForm.alertMethod.includes('email')">
          <el-input v-model="settingForm.notificationEmail" placeholder="请输入通知邮箱，多个邮箱用逗号分隔" />
        </el-form-item>
        
        <el-form-item label="Webhook地址" prop="webhookUrl" v-if="settingForm.alertMethod.includes('webhook')">
          <el-input v-model="settingForm.webhookUrl" placeholder="请输入Webhook地址" />
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
  monitorEnabled: true,
  collectInterval: 60,
  retentionDays: 30,
  alertThreshold: 80,
  alertMethod: ['email'],
  notificationEmail: '',
  webhookUrl: ''
})

const rules = {
  collectInterval: [
    { required: true, message: '请输入数据采集频率', trigger: 'blur' }
  ],
  retentionDays: [
    { required: true, message: '请输入数据保留天数', trigger: 'blur' }
  ],
  alertThreshold: [
    { required: true, message: '请输入告警阈值', trigger: 'blur' }
  ],
  notificationEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  webhookUrl: [
    { type: 'url', message: '请输入正确的URL地址', trigger: 'blur' }
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
.monitor-setting-container {
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
