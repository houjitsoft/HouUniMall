<template>
  <div class="verify-code-container">
    <div class="verify-code-card">
      <div class="verify-code-header">
        <h2>{{ t('permission.verifyCode.title') }}</h2>
        <p>{{ t('permission.verifyCode.sentTo', { phone: maskPhone(phone) }) }}</p>
      </div>
      
      <div class="verify-code-input">
        <el-input
          v-model="code"
          :placeholder="t('permission.verifyCode.codePlaceholder')"
          maxlength="6"
          @input="handleCodeInput"
          ref="codeInput"
          class="code-input"
        />
      </div>
      
      <div class="verify-code-actions">
        <el-button @click="resendCode" :disabled="resendDisabled" class="resend-btn">
          {{ resendText }}
        </el-button>
        <el-button @click="goBack" class="back-btn">
          {{ t('permission.verifyCode.backToEdit') }}
        </el-button>
      </div>
      
      <div class="verify-code-footer">
        <p>{{ t('permission.verifyCode.notReceived') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 从路由参数获取手机号
const phone = ref(route.query.phone || '')
const code = ref('')
const resendDisabled = ref(false)
const resendText = ref(t('permission.verifyCode.resend'))

// 自动聚焦输入框
const codeInput = ref(null)

// 处理验证码输入
const handleCodeInput = (value) => {
  if (value.length === 6) {
    verifyCode()
  }
}

// 验证验证码
const verifyCode = async () => {
  if (!code.value || code.value.length !== 6) {
    ElMessage.warning(t('permission.verifyCode.codeRequired'))
    return
  }
  
  try {
    // 这里可以调用验证码验证接口
    // const response = await authStore.verifyCode(phone.value, code.value)
    
    ElMessage.success(t('permission.verifyCode.verifySuccess'))
    // 根据业务需求跳转到相应页面
    router.push('/')
  } catch (error) {
    ElMessage.error(t('permission.verifyCode.verifyFailed'))
  }
}

// 重新发送验证码
const resendCode = async () => {
  try {
    await authStore.sendCode(phone.value)
    ElMessage.success(t('permission.verifyCode.resendSuccess'))
    startResendCountdown()
  } catch (error) {
    ElMessage.error(t('permission.verifyCode.resendFailed'))
  }
}

// 开始重新发送倒计时
const startResendCountdown = () => {
  let countdown = 60
  resendDisabled.value = true
  
  const timer = setInterval(() => {
    countdown--
    resendText.value = t('permission.verifyCode.resendCountdown', { countdown })
    
    if (countdown <= 0) {
      clearInterval(timer)
      resendDisabled.value = false
      resendText.value = t('permission.verifyCode.resend')
    }
  }, 1000)
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 手机号脱敏
const maskPhone = (phone) => {
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 生命周期
onMounted(() => {
  // 自动聚焦验证码输入框
  nextTick(() => {
    codeInput.value?.focus()
  })
})
</script>

<style scoped>
.verify-code-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.verify-code-card {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.verify-code-header {
  text-align: center;
  margin-bottom: 30px;
}

.verify-code-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 2rem;
}

.verify-code-header p {
  color: #7f8c8d;
  margin: 0;
  font-size: 1.1rem;
}

.verify-code-input {
  margin-bottom: 30px;
}

.code-input {
  font-size: 1.2rem;
  text-align: center;
  letter-spacing: 8px;
}

.verify-code-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-bottom: 30px;
}

.resend-btn {
  width: 120px;
}

.back-btn {
  width: 120px;
}

.verify-code-footer {
  text-align: center;
  color: #7f8c8d;
}

.verify-code-footer p {
  margin: 0;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .verify-code-card {
    padding: 30px 20px;
  }
  
  .verify-code-header h2 {
    font-size: 1.5rem;
  }
  
  .verify-code-actions {
    flex-direction: column;
  }
  
  .resend-btn,
  .back-btn {
    width: 100%;
  }
}
</style>
