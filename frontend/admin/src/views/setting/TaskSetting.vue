<template>
  <div class="task-setting-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>任务设置</h2>
      <p>配置任务执行的相关参数和规则</p>
    </div>

    <!-- 设置表单 -->
    <el-card class="setting-card">
      <template #header>
        <div class="card-header">
          <span>任务执行配置</span>
        </div>
      </template>
      
      <el-form :model="settingForm" :rules="rules" ref="settingFormRef" label-width="150px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大并发数" prop="maxConcurrency">
              <el-input-number 
                v-model="settingForm.maxConcurrency" 
                :min="1" 
                :max="100" 
                controls-position="right"
              />
              <span class="form-tip">同时执行的最大任务数量</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务超时时间" prop="timeout">
              <el-input-number 
                v-model="settingForm.timeout" 
                :min="60" 
                :max="3600" 
                controls-position="right"
              />
              <span class="form-tip">秒</span>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="重试次数" prop="retryCount">
              <el-input-number 
                v-model="settingForm.retryCount" 
                :min="0" 
                :max="10" 
                controls-position="right"
              />
              <span class="form-tip">任务失败时的重试次数</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重试间隔" prop="retryInterval">
              <el-input-number 
                v-model="settingForm.retryInterval" 
                :min="10" 
                :max="300" 
                controls-position="right"
              />
              <span class="form-tip">秒</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="任务队列大小" prop="queueSize">
          <el-input-number 
            v-model="settingForm.queueSize" 
            :min="100" 
            :max="10000" 
            controls-position="right"
          />
          <span class="form-tip">任务队列的最大容量</span>
        </el-form-item>

        <el-form-item label="自动清理" prop="autoCleanup">
          <el-switch v-model="settingForm.autoCleanup" />
          <span class="form-tip">自动清理已完成的任务记录</span>
        </el-form-item>

        <el-form-item label="清理周期" prop="cleanupDays" v-if="settingForm.autoCleanup">
          <el-input-number 
            v-model="settingForm.cleanupDays" 
            :min="1" 
            :max="365" 
            controls-position="right"
          />
          <span class="form-tip">天（保留多少天的任务记录）</span>
        </el-form-item>

        <el-form-item label="邮件通知" prop="emailNotification">
          <el-switch v-model="settingForm.emailNotification" />
          <span class="form-tip">任务执行失败时发送邮件通知</span>
        </el-form-item>

        <el-form-item label="通知邮箱" prop="notificationEmail" v-if="settingForm.emailNotification">
          <el-input v-model="settingForm.notificationEmail" placeholder="请输入通知邮箱" />
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
  maxConcurrency: 10,
  timeout: 300,
  retryCount: 3,
  retryInterval: 60,
  queueSize: 1000,
  autoCleanup: true,
  cleanupDays: 30,
  emailNotification: false,
  notificationEmail: ''
})

const rules = {
  maxConcurrency: [
    { required: true, message: '请输入最大并发数', trigger: 'blur' }
  ],
  timeout: [
    { required: true, message: '请输入超时时间', trigger: 'blur' }
  ],
  retryCount: [
    { required: true, message: '请输入重试次数', trigger: 'blur' }
  ],
  retryInterval: [
    { required: true, message: '请输入重试间隔', trigger: 'blur' }
  ],
  queueSize: [
    { required: true, message: '请输入队列大小', trigger: 'blur' }
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
.task-setting-container {
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
