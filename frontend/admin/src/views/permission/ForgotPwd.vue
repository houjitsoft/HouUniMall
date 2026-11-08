<template>
  <div class="forgot-pwd-container">
    <div class="forgot-pwd-card">
      <div class="forgot-pwd-header">
        <h2>忘记密码</h2>
        <p>我们将通过手机号帮您重置密码</p>
      </div>
      
      <el-steps :active="currentStep" finish-status="success" class="steps">
        <el-step title="验证手机号" />
        <el-step title="重置密码" />
        <el-step title="完成" />
      </el-steps>
      
      <!-- 步骤1：验证手机号 -->
      <div v-if="currentStep === 0" class="step-content">
        <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" class="form">
          <el-form-item prop="phone">
            <el-input
              v-model="phoneForm.phone"
              placeholder="请输入手机号"
              prefix-icon="el-icon-mobile-phone"
              @input="formatPhone"
            />
          </el-form-item>
          
          <el-form-item prop="code">
            <div class="code-input-group">
              <el-input
                v-model="phoneForm.code"
                placeholder="请输入验证码"
                prefix-icon="el-icon-key"
                maxlength="6"
              />
              <el-button
                :disabled="isCodeButtonDisabled"
                @click="sendCode"
                class="send-code-btn"
              >
                {{ codeButtonText }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              :loading="verifyLoading"
              @click="verifyPhone"
              class="submit-btn"
            >
              验证手机号
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 步骤2：重置密码 -->
      <div v-else-if="currentStep === 1" class="step-content">
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" class="form">
          <el-form-item prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              prefix-icon="el-icon-lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              prefix-icon="el-icon-lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              :loading="resetLoading"
              @click="resetPassword"
              class="submit-btn"
            >
              重置密码
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 步骤3：完成 -->
      <div v-else-if="currentStep === 2" class="step-content">
        <div class="success-content">
          <div class="success-icon">
            <i class="el-icon-success"></i>
          </div>
          <h3>密码重置成功</h3>
          <p>您的密码已经成功重置，请使用新密码登录</p>
          <el-button type="primary" @click="goToLogin" class="login-btn">
            立即登录
          </el-button>
        </div>
      </div>
      
      <div class="forgot-pwd-footer">
        <el-link @click="goToLogin">返回登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 当前步骤
const currentStep = ref(0)

// 表单引用
const phoneFormRef = ref(null)
const passwordFormRef = ref(null)

// 手机号表单数据
const phoneForm = reactive({
  phone: '',
  code: ''
})

// 密码表单数据
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 状态变量
const verifyLoading = ref(false)
const resetLoading = ref(false)
const codeButtonDisabled = ref(false)
const codeButtonText = ref('获取验证码')

// 计算属性
const isCodeButtonDisabled = computed(() => {
  return !phoneForm.phone || !/^1[3-9]\d{9}$/.test(phoneForm.phone) || codeButtonDisabled.value
})

// 方法
// 格式化手机号
const formatPhone = (value) => {
  phoneForm.phone = value.replace(/\D/g, '')
}

// 发送验证码
const sendCode = async () => {
  try {
    const response = await authStore.sendCode(phoneForm.phone, 'reset')
    
    // 检查响应状态
    if (response && (response.code === 1 || response.code === 200)) {
      ElMessage.success('验证码已发送')
      startCodeCountdown()
    } else {
      // 显示后端返回的具体错误消息
      const errorMsg = response?.message || '验证码发送失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    // 提取真实的错误信息
    let errorMsg = '验证码发送失败'
    
    if (error.response?.data?.message) {
      errorMsg = error.response.data.message
    } else if (error.message) {
      errorMsg = error.message
    }
    
    ElMessage.error(errorMsg)
  }
}

// 开始验证码倒计时
const startCodeCountdown = () => {
  let countdown = 60
  codeButtonDisabled.value = true
  
  const timer = setInterval(() => {
    countdown--
    codeButtonText.value = `${countdown}s后重发`
    
    if (countdown <= 0) {
      clearInterval(timer)
      codeButtonDisabled.value = false
      codeButtonText.value = '获取验证码'
    }
  }, 1000)
}

// 验证手机号
const verifyPhone = async () => {
  try {
    await phoneFormRef.value.validate()
    verifyLoading.value = true
    
    // 这里可以调用验证码验证接口
    // const response = await authStore.verifyCode(phoneForm.phone, phoneForm.code)
    
    ElMessage.success('手机号验证成功')
    currentStep.value = 1
  } catch (error) {
    ElMessage.error('验证失败，请检查验证码')
  } finally {
    verifyLoading.value = false
  }
}

// 重置密码
const resetPassword = async () => {
  try {
    await passwordFormRef.value.validate()
    resetLoading.value = true
    
    const response = await authStore.forgotPassword(
      phoneForm.phone,
      phoneForm.code,
      passwordForm.newPassword
    )
    
    if (response.success) {
      ElMessage.success('密码重置成功')
      currentStep.value = 2
    }
  } catch (error) {
    ElMessage.error('密码重置失败')
  } finally {
    resetLoading.value = false
  }
}

// 跳转登录
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-pwd-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.forgot-pwd-card {
  width: 100%;
  max-width: 500px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.forgot-pwd-header {
  text-align: center;
  margin-bottom: 30px;
}

.forgot-pwd-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 2rem;
}

.forgot-pwd-header p {
  color: #7f8c8d;
  margin: 0;
  font-size: 1.1rem;
}

.steps {
  margin-bottom: 30px;
}

.step-content {
  margin-bottom: 30px;
}

.form {
  margin-bottom: 20px;
}

.code-input-group {
  display: flex;
  gap: 10px;
}

.send-code-btn {
  width: 120px;
}

.submit-btn {
  width: 100%;
  height: 45px;
  font-size: 1.1rem;
}

.success-content {
  text-align: center;
  padding: 40px 0;
}

.success-icon {
  font-size: 4rem;
  color: #67c23a;
  margin-bottom: 20px;
}

.success-content h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 1.5rem;
}

.success-content p {
  color: #7f8c8d;
  margin-bottom: 30px;
}

.login-btn {
  width: 200px;
  height: 45px;
  font-size: 1.1rem;
}

.forgot-pwd-footer {
  text-align: center;
  color: #7f8c8d;
}

@media (max-width: 768px) {
  .forgot-pwd-card {
    padding: 30px 20px;
  }
  
  .forgot-pwd-header h2 {
    font-size: 1.5rem;
  }
  
  .success-icon {
    font-size: 3rem;
  }
}
</style>
