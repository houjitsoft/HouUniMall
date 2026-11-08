<template>
  <div class="api-setting-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>API设置</h2>
      <p>配置API访问和安全相关设置</p>
    </div>

    <!-- 设置表单 -->
    <el-card class="setting-card">
      <template #header>
        <div class="card-header">
          <span>API基础配置</span>
        </div>
      </template>
      
      <el-form :model="settingForm" :rules="rules" ref="settingFormRef" label-width="150px">
        <el-form-item label="API访问密钥" prop="apiKey">
          <el-input v-model="settingForm.apiKey" placeholder="请输入API访问密钥" show-password />
          <span class="form-tip">用于验证API请求的密钥</span>
        </el-form-item>
        
        <el-form-item label="请求频率限制" prop="rateLimit">
          <el-input-number 
            v-model="settingForm.rateLimit" 
            :min="1" 
            :max="10000" 
            controls-position="right"
          />
          <span class="form-tip">每分钟最大请求数</span>
        </el-form-item>
        
        <el-form-item label="IP访问限制" prop="ipRestriction">
          <el-switch v-model="settingForm.ipRestriction" />
          <span class="form-tip">是否启用IP访问限制</span>
        </el-form-item>
        
        <el-form-item label="允许的IP列表" prop="allowedIps" v-if="settingForm.ipRestriction">
          <el-input 
            v-model="settingForm.allowedIps" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入允许访问的IP地址，每行一个"
          />
        </el-form-item>
        
        <el-form-item label="API版本" prop="apiVersion">
          <el-select v-model="settingForm.apiVersion" placeholder="请选择API版本">
            <el-option label="v1" value="v1" />
            <el-option label="v2" value="v2" />
            <el-option label="v3" value="v3" />
          </el-select>
          <span class="form-tip">当前使用的API版本</span>
        </el-form-item>
        
        <el-form-item label="HTTPS强制" prop="httpsOnly">
          <el-switch v-model="settingForm.httpsOnly" />
          <span class="form-tip">是否强制使用HTTPS协议</span>
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
  apiKey: '',
  rateLimit: 1000,
  ipRestriction: false,
  allowedIps: '',
  apiVersion: 'v2',
  httpsOnly: true
})

const rules = {
  apiKey: [
    { required: true, message: '请输入API访问密钥', trigger: 'blur' }
  ],
  rateLimit: [
    { required: true, message: '请输入请求频率限制', trigger: 'blur' }
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
.api-setting-container {
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
