<template>
  <div class="forgot-password-container">
    <div class="forgot-password-wrapper">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-header">
            <div class="logo">
              <div class="logo-icon">
                <el-icon><Platform /></el-icon>
              </div>
              <h1 class="brand-name">HouUniMall</h1>
            </div>
            <p class="brand-tagline">企业级数字化管理平台</p>
          </div>
          
          <div class="help-section">
            <h2 class="help-title">找回您的密码</h2>
            <p class="help-subtitle">我们提供多种方式帮您找回密码</p>
          </div>
          
          <div class="methods-section">
            <div class="method-item">
              <el-icon class="method-icon"><Message /></el-icon>
              <span class="method-text">邮箱验证</span>
            </div>
            <div class="method-item">
              <el-icon class="method-icon"><Iphone /></el-icon>
              <span class="method-text">手机验证</span>
            </div>
            <div class="method-item">
              <el-icon class="method-icon"><UserFilled /></el-icon>
              <span class="method-text">人工客服</span>
            </div>
          </div>
        </div>
        
        <div class="background-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="form-section">
        <div class="form-container">
          <div class="form-header">
            <h2 class="form-title">重置密码</h2>
            <p class="form-subtitle">请输入注册账号，系统将自动识别手机号或邮箱</p>
          </div>

          <el-form 
            ref="forgotPasswordFormRef" 
            :model="forgotPasswordForm" 
            :rules="forgotPasswordRules" 
            class="forgot-password-form"
          >
            <el-form-item prop="account">
              <div class="account-label-row">
                <label class="input-label">{{ accountLabel }}</label>
                <button
                  v-if="!detectedAccountMode"
                  type="button"
                  class="mode-switch-btn"
                  @click="toggleAccountMode"
                >
                  切换为{{ activeAccountMode === 'phone' ? '邮箱' : '手机号' }}
                </button>
              </div>
              <el-input
                v-model="accountInput"
                :placeholder="accountPlaceholder"
                size="large"
                class="form-input"
                clearable
              >
                <template #prefix>
                  <el-icon class="input-icon">
                    <Iphone v-if="activeAccountMode === 'phone'" />
                    <Message v-else />
                  </el-icon>
                </template>
              </el-input>
              <p v-if="!forgotPasswordForm.account.trim()" class="mode-hint">
                未填写时将按您的网络位置默认使用{{ defaultAccountMode === 'phone' ? '手机号' : '邮箱' }}验证
              </p>
            </el-form-item>

            <el-form-item prop="code">
              <label class="input-label">{{ codeLabel }}</label>
              <div class="code-input-wrapper">
                <el-input
                  v-model="forgotPasswordForm.code"
                  placeholder="请输入6位验证码"
                  size="large"
                  class="form-input"
                  maxlength="6"
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Key /></el-icon>
                  </template>
                </el-input>
                <el-button
                  type="primary"
                  :disabled="codeSending || codeCountdown > 0 || !canSendCode"
                  @click="sendResetCode"
                  class="code-btn"
                >
                  {{ codeSending ? '发送中...' : (codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码') }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item prop="newPassword">
              <label class="input-label">新密码</label>
              <el-input
                v-model="forgotPasswordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                size="large"
                class="form-input"
                show-password
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <label class="input-label">确认新密码</label>
              <el-input
                v-model="forgotPasswordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                size="large"
                class="form-input"
                show-password
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                :loading="resetLoading"
                @click="handleResetPassword"
                class="reset-btn"
              >
                <span class="btn-text">重置密码</span>
                <el-icon class="btn-icon"><Right /></el-icon>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <div class="back-to-login">
              <el-link type="primary" @click="handleBackToLogin" class="back-link">
                <el-icon class="back-icon"><ArrowLeft /></el-icon>
                返回登录
              </el-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Message, 
  Key, 
  Lock, 
  Right, 
  Iphone, 
  UserFilled, 
  Platform,
  ArrowLeft
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { authService } from '@/api/auth/auth.js'
import {
  detectAccountMode,
  isEmailAccount,
  isPhoneAccount,
  guessDomesticByTimezone,
  normalizeAccountInput,
  normalizePhoneInput,
  resolveDefaultAccountMode
} from '@/utils/accountType.js'

// 路由
const router = useRouter()

// 状态管理
const authStore = useAuthStore()

// 表单引用
const forgotPasswordFormRef = ref(null)

// 忘记密码表单数据
const forgotPasswordForm = reactive({
  account: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const defaultAccountMode = ref(guessDomesticByTimezone() ? 'phone' : 'email')
const manualAccountMode = ref(null)

const detectedAccountMode = computed(() => detectAccountMode(forgotPasswordForm.account))

const activeAccountMode = computed(() => {
  if (detectedAccountMode.value) {
    return detectedAccountMode.value
  }
  if (manualAccountMode.value) {
    return manualAccountMode.value
  }
  return defaultAccountMode.value
})

const accountLabel = computed(() => {
  if (detectedAccountMode.value === 'phone') return '手机号'
  if (detectedAccountMode.value === 'email') return '邮箱地址'
  return activeAccountMode.value === 'phone' ? '手机号' : '邮箱地址'
})

const accountPlaceholder = computed(() => {
  if (activeAccountMode.value === 'phone') {
    return '请输入注册时的手机号'
  }
  return '请输入注册时的邮箱地址'
})

const codeLabel = computed(() => (
  activeAccountMode.value === 'phone' ? '手机验证码' : '邮箱验证码'
))

const accountInput = computed({
  get: () => forgotPasswordForm.account,
  set: (val) => {
    const raw = String(val ?? '')
    forgotPasswordForm.account = raw.includes('@') ? raw : normalizePhoneInput(raw)
    if (detectAccountMode(forgotPasswordForm.account)) {
      manualAccountMode.value = null
    }
  }
})

const canSendCode = computed(() => {
  const account = normalizeAccountInput(forgotPasswordForm.account)
  if (!account) return false
  const mode = detectAccountMode(account) || activeAccountMode.value
  return mode === 'phone' ? isPhoneAccount(account) : isEmailAccount(account)
})

const validateAccount = (_rule, value, callback) => {
  const account = normalizeAccountInput(value)
  if (account && account !== String(value || '')) {
    forgotPasswordForm.account = account
  }
  if (!account) {
    callback(new Error(activeAccountMode.value === 'phone' ? '请输入手机号' : '请输入邮箱地址'))
    return
  }

  const mode = detectAccountMode(account) || activeAccountMode.value
  if (mode === 'phone' && !isPhoneAccount(account)) {
    callback(new Error('请输入正确的手机号'))
    return
  }
  if (mode === 'email' && !isEmailAccount(account)) {
    callback(new Error('请输入正确的邮箱格式'))
    return
  }
  callback()
}

// 重置加载状态
const resetLoading = ref(false)
const codeCountdown = ref(0)
const codeSending = ref(false)
let codeCountdownTimer = null

// 开始验证码倒计时（先清理旧定时器，避免重复点击时叠加）
const startCodeCountdown = (seconds = 60) => {
  if (codeCountdownTimer) {
    clearInterval(codeCountdownTimer)
    codeCountdownTimer = null
  }
  codeCountdown.value = seconds
  codeCountdownTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(codeCountdownTimer)
      codeCountdownTimer = null
    }
  }, 1000)
}

// 停止倒计时并恢复按钮可点击状态（发送失败时使用）
const stopCodeCountdown = () => {
  if (codeCountdownTimer) {
    clearInterval(codeCountdownTimer)
    codeCountdownTimer = null
  }
  codeCountdown.value = 0
}

onBeforeUnmount(() => {
  if (codeCountdownTimer) {
    clearInterval(codeCountdownTimer)
    codeCountdownTimer = null
  }
})

// 忘记密码规则
const forgotPasswordRules = {
  account: [
    { required: true, validator: validateAccount, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const toggleAccountMode = () => {
  manualAccountMode.value = activeAccountMode.value === 'phone' ? 'email' : 'phone'
  if (forgotPasswordFormRef.value) {
    forgotPasswordFormRef.value.clearValidate(['account'])
  }
}

const loadDefaultAccountMode = async () => {
  try {
    const response = await authService.getClientRegion()
    if (response?.code === 1 && response.data) {
      defaultAccountMode.value = resolveDefaultAccountMode(response.data)
    }
  } catch (error) {
    defaultAccountMode.value = guessDomesticByTimezone() ? 'phone' : 'email'
    console.warn('[ForgotPassword] 获取客户端区域失败，使用时区兜底', error?.message)
  }
}

onMounted(() => {
  loadDefaultAccountMode()
})

// 发送重置验证码：点击后立即禁用按钮并开始倒计时，再发起请求，
// 避免等待后端回执期间被重复点击（重复点击还会触发运营商号码级流控，导致收不到短信）
const sendResetCode = async () => {
  if (codeSending.value || codeCountdown.value > 0) return
  try {
    await forgotPasswordFormRef.value.validateField('account')
  } catch (error) {
    // 账号校验未通过，不发送、不倒计时，由表单展示校验错误
    return
  }

  const account = normalizeAccountInput(forgotPasswordForm.account)
  const mode = detectAccountMode(account) || activeAccountMode.value

  // 请求发起前立即禁用按钮并启动倒计时，防止接口响应延迟期间重复点击
  codeSending.value = true
  startCodeCountdown(60)

  try {
    const response = await authStore.sendResetCode(account)
    if (response?.code === 1 || response?.code === 200 || response?.success) {
      ElMessage.success(
        mode === 'email' ? '验证码已发送到您的邮箱' : '验证码已发送到您的手机'
      )
    } else {
      // 业务失败（如运营商流控）：提示原因并恢复按钮，允许用户稍后重新发送。
      // 成功响应分支的拦截器不会自动弹错误提示，需由页面侧展示，
      // 避免用户不知道为何收不到验证码而反复点击耗尽号码级流控额度。
      stopCodeCountdown()
      ElMessage.error(response?.message || '验证码发送失败，请稍后重试')
    }
  } catch (error) {
    console.error('发送重置验证码失败:', error)
    // 发送失败：回滚倒计时，恢复按钮可点击状态。
    // 拦截器只在 HTTP 错误分支自动弹提示；HTTP 200 但业务码非成功的拒绝（无 response.status）
    // 需由页面侧补弹，否则用户看不到失败原因会反复点击。
    stopCodeCountdown()
    if (!error?.response?.status) {
      ElMessage.error(error?.message || '验证码发送失败，请稍后重试')
    }
  } finally {
    codeSending.value = false
  }
}

// 处理重置密码
const handleResetPassword = async () => {
  try {
    await forgotPasswordFormRef.value.validate()
    resetLoading.value = true

    const account = normalizeAccountInput(forgotPasswordForm.account)
    const response = await authStore.forgotPassword(
      account,
      forgotPasswordForm.code,
      forgotPasswordForm.newPassword
    )

    if (response?.code === 1 || response?.code === 200 || response?.success) {
      ElMessage.success('密码重置成功，请使用新密码登录')
      router.push('/login')
    } else {
      ElMessage.error(response?.message || '密码重置失败')
    }
  } catch (error) {
    console.error('重置密码失败:', error)
    ElMessage.error(error.message || '密码重置失败，请稍后重试')
  } finally {
    resetLoading.value = false
  }
}

// 返回登录
const handleBackToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-password-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.forgot-password-wrapper {
  width: 100%;
  max-width: 1200px;
  height: 700px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
  animation: slideInUp 0.8s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧品牌区域 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  color: white;
  animation: fadeInLeft 1s ease-out 0.2s both;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.brand-content {
  text-align: center;
  z-index: 2;
  position: relative;
  max-width: 400px;
}

.brand-header {
  margin-bottom: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.logo-icon .el-icon {
  font-size: 24px;
  color: white;
}

.brand-name {
  font-size: 32px;
  font-weight: 600;
  margin: 0;
  letter-spacing: -0.5px;
}

.brand-tagline {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
  font-weight: 400;
}

.help-section {
  margin-bottom: 40px;
}

.help-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 12px 0;
}

.help-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
  line-height: 1.5;
}

.methods-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.method-icon {
  font-size: 20px;
  color: #4ade80;
  flex-shrink: 0;
}

.method-text {
  font-size: 14px;
  font-weight: 500;
}

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 120px;
  height: 120px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.circle-3 {
  width: 60px;
  height: 60px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

/* 右侧表单区域 */
.form-section {
  flex: 1;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  position: relative;
  animation: fadeInRight 1s ease-out 0.4s both;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.form-container {
  width: 100%;
  max-width: 400px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.forgot-password-form {
  margin-bottom: 30px;
}

.input-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.account-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.account-label-row .input-label {
  margin-bottom: 0;
}

.mode-switch-btn {
  border: none;
  background: transparent;
  color: #667eea;
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  white-space: nowrap;
}

.mode-switch-btn:hover {
  color: #764ba2;
  text-decoration: underline;
}

.mode-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.form-input {
  height: 48px;
  border-radius: 8px;
  border: 1px solid #e1e5e9;
  transition: all 0.2s ease;
}

.form-input:hover {
  border-color: #667eea;
}

.form-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.input-icon {
  color: #999;
  font-size: 18px;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
}

.code-input-wrapper .form-input {
  flex: 1;
}

.code-btn {
  width: 120px;
  height: 48px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  transition: all 0.2s ease;
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.code-btn:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.reset-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.reset-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.btn-text {
  font-weight: 600;
}

.btn-icon {
  font-size: 16px;
  transition: transform 0.2s ease;
}

.reset-btn:hover .btn-icon {
  transform: translateX(2px);
}

.form-footer {
  text-align: center;
}

.back-to-login {
  display: flex;
  justify-content: center;
  align-items: center;
}

.back-link {
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.back-icon {
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .forgot-password-wrapper {
    max-width: 1000px;
    height: 650px;
  }
  
  .brand-section,
  .form-section {
    padding: 50px 35px;
  }
}

@media (max-width: 992px) {
  .forgot-password-wrapper {
    max-width: 900px;
    height: 600px;
  }
  
  .brand-section,
  .form-section {
    padding: 40px 30px;
  }
  
  .brand-name {
    font-size: 28px;
  }
  
  .brand-tagline {
    font-size: 15px;
  }
  
  .help-title {
    font-size: 24px;
  }
  
  .form-title {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .forgot-password-container {
    padding: 15px;
  }
  
  .forgot-password-wrapper {
    flex-direction: column;
    height: auto;
    max-width: 500px;
    min-height: 600px;
  }
  
  .brand-section {
    padding: 30px 20px;
    min-height: 280px;
  }
  
  .brand-name {
    font-size: 24px;
  }
  
  .brand-tagline {
    font-size: 14px;
  }
  
  .help-title {
    font-size: 20px;
  }
  
  .help-subtitle {
    font-size: 14px;
  }
  
  .form-section {
    padding: 30px 20px;
  }
  
  .form-title {
    font-size: 20px;
  }
  
  .form-subtitle {
    font-size: 14px;
  }
  
  .form-input {
    height: 44px;
  }
  
  .code-btn {
    height: 44px;
    font-size: 13px;
  }
  
  .reset-btn {
    height: 44px;
    font-size: 16px;
  }
}

@media (max-width: 576px) {
  .forgot-password-container {
    padding: 10px;
  }
  
  .forgot-password-wrapper {
    max-width: 100%;
    border-radius: 12px;
  }
  
  .brand-section {
    padding: 25px 15px;
    min-height: 250px;
  }
  
  .brand-name {
    font-size: 20px;
  }
  
  .brand-tagline {
    font-size: 13px;
  }
  
  .help-title {
    font-size: 18px;
  }
  
  .help-subtitle {
    font-size: 13px;
  }
  
  .form-section {
    padding: 25px 15px;
  }
  
  .form-title {
    font-size: 18px;
  }
  
  .form-subtitle {
    font-size: 13px;
  }
  
  .input-label {
    font-size: 13px;
  }
  
  .form-input {
    height: 40px;
  }
  
  .code-btn {
    height: 40px;
    font-size: 12px;
    width: 100px;
  }
  
  .reset-btn {
    height: 40px;
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .forgot-password-container {
    padding: 8px;
  }
  
  .forgot-password-wrapper {
    border-radius: 8px;
    min-height: 500px;
  }
  
  .brand-section {
    padding: 20px 12px;
    min-height: 200px;
  }
  
  .brand-name {
    font-size: 18px;
  }
  
  .brand-tagline {
    font-size: 12px;
  }
  
  .help-title {
    font-size: 16px;
  }
  
  .help-subtitle {
    font-size: 12px;
  }
  
  .form-section {
    padding: 20px 12px;
  }
  
  .form-title {
    font-size: 16px;
  }
  
  .form-subtitle {
    font-size: 12px;
  }
  
  .input-label {
    font-size: 13px;
  }
  
  .form-input {
    height: 36px;
  }
  
  .code-btn {
    height: 36px;
    font-size: 11px;
    width: 90px;
  }
  
  .reset-btn {
    height: 36px;
    font-size: 14px;
  }
}

/* 横屏适配 */
@media (max-height: 600px) and (orientation: landscape) {
  .forgot-password-wrapper {
    height: 90vh;
    max-height: 500px;
  }
  
  .brand-section {
    padding: 20px;
  }
  
  .brand-header {
    margin-bottom: 20px;
  }
  
  .brand-name {
    font-size: 20px;
  }
  
  .brand-tagline {
    font-size: 12px;
  }
  
  .help-section {
    margin-bottom: 20px;
  }
  
  .form-section {
    padding: 20px;
  }
  
  .form-title {
    font-size: 18px;
    margin-bottom: 6px;
  }
  
  .form-subtitle {
    font-size: 12px;
  }
}

/* 高分辨率屏幕优化 */
@media (min-width: 1920px) {
  .forgot-password-wrapper {
    max-width: 1400px;
    height: 800px;
  }
  
  .brand-name {
    font-size: 36px;
  }
  
  .brand-tagline {
    font-size: 18px;
  }
  
  .help-title {
    font-size: 32px;
  }
  
  .help-subtitle {
    font-size: 18px;
  }
  
  .form-section {
    padding: 80px 60px;
  }
  
  .form-title {
    font-size: 32px;
  }
  
  .form-subtitle {
    font-size: 18px;
  }
  
  .reset-btn {
    height: 52px;
    font-size: 18px;
  }
}
</style>
