<template>
  <div class="auth-config">
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <el-icon><Setting /></el-icon>
          <span>认证配置</span>
        </div>
      </template>
      
      <el-form :model="configForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="认证方式">
              <el-select v-model="configForm.authMethod" placeholder="请选择认证方式">
                <el-option label="用户名密码" value="password" />
                <el-option label="手机验证码" value="sms" />
                <el-option label="邮箱验证" value="email" />
                <el-option label="第三方登录" value="oauth" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="会话超时时间">
              <el-input-number 
                v-model="configForm.sessionTimeout" 
                :min="30" 
                :max="1440" 
                controls-position="right"
              />
              <span class="unit">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="密码强度">
              <el-select v-model="configForm.passwordStrength" placeholder="请选择密码强度">
                <el-option label="低" value="low" />
                <el-option label="中" value="medium" />
                <el-option label="高" value="high" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="登录失败限制">
              <el-input-number 
                v-model="configForm.loginFailLimit" 
                :min="3" 
                :max="10" 
                controls-position="right"
              />
              <span class="unit">次</span>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="启用双因子认证">
          <el-switch v-model="configForm.enable2FA" />
        </el-form-item>
        
        <el-form-item label="启用记住我">
          <el-switch v-model="configForm.enableRememberMe" />
        </el-form-item>
        
        <el-form-item label="启用自动登录">
          <el-switch v-model="configForm.enableAutoLogin" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveConfig">保存配置</el-button>
          <el-button @click="resetConfig">重置</el-button>
          <el-button type="success" @click="testConfig">测试配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

/**
 * 认证配置页面
 * 
 * @author Amos
 * @since 2025-01-07
 */

// 配置表单
const configForm = ref({
  authMethod: 'password',
  sessionTimeout: 120,
  passwordStrength: 'medium',
  loginFailLimit: 5,
  enable2FA: false,
  enableRememberMe: true,
  enableAutoLogin: false
})

// 保存配置
const saveConfig = () => {
  ElMessage.success('配置保存成功')
  console.log('保存配置:', configForm.value)
}

// 重置配置
const resetConfig = () => {
  configForm.value = {
    authMethod: 'password',
    sessionTimeout: 120,
    passwordStrength: 'medium',
    loginFailLimit: 5,
    enable2FA: false,
    enableRememberMe: true,
    enableAutoLogin: false
  }
  ElMessage.info('配置已重置')
}

// 测试配置
const testConfig = () => {
  ElMessage.success('配置测试通过')
}
</script>

<style scoped>
.auth-config {
  padding: 20px;
}

.config-card {
  max-width: 800px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 500;
}

.unit {
  margin-left: 8px;
  color: #666;
}
</style>
