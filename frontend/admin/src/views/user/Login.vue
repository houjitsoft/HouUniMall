<template>
  <div class="login-container">
    <!-- 引导页面 -->
    <div v-if="currentStep === 'guide'" class="guide-step">
      <div class="guide-content">
        <div class="guide-header">
          <h1 class="platform-title">HouEOC 企业级微服务架构系统</h1>
          <p class="platform-subtitle">基于现代化微服务架构，打造企业级数字化平台</p>
        </div>
        
        <!-- 平台介绍轮播 -->
        <div class="guide-carousel">
          <el-carousel :interval="4000" height="400px" indicator-position="outside">
            <el-carousel-item v-for="(feature, index) in platformFeatures" :key="index">
              <div class="carousel-item">
                <div class="feature-icon">{{ feature.icon }}</div>
                <h3 class="feature-title">{{ feature.title }}</h3>
                <p class="feature-description">{{ feature.description }}</p>
                <div class="feature-benefits">
                  <span v-for="benefit in feature.benefits" :key="benefit" class="benefit-tag">
                    {{ benefit }}
                  </span>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
        
        <!-- 客户痛点解决 -->
        <div class="pain-points">
          <h3>我们解决的核心问题</h3>
          <div class="points-grid">
            <div v-for="point in painPoints" :key="point.problem" class="point-item">
              <div class="problem">
                <i class="el-icon-warning"></i>
                <span>{{ point.problem }}</span>
              </div>
              <div class="solution">
                <i class="el-icon-check"></i>
                <span>{{ point.solution }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="guide-actions">
          <el-button type="primary" size="large" @click="startExperience">
            开始体验
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 登录页面 -->
    <div v-else-if="currentStep === 'login'" class="login-step">
      <div class="login-content">
        <div class="login-header">
          <h2>欢迎使用 HouEOC</h2>
          <p>请选择登录方式</p>
        </div>
        
        <!-- 登录方式切换 -->
        <div class="login-method-tabs">
          <el-tabs v-model="activeLoginTab" @tab-change="handleTabChange">
            <el-tab-pane label="用户名密码登录" name="username">
              <el-form 
                ref="usernameLoginForm" 
                :model="usernameLoginForm" 
                :rules="usernameLoginRules" 
                class="login-form"
                @keyup.enter="handleUsernameLogin"
              >
                <el-form-item prop="username">
                  <el-input
                    v-model="usernameLoginForm.username"
                    placeholder="请输入用户名/手机号"
                    prefix-icon="el-icon-user"
                  />
                </el-form-item>
                
                <el-form-item prop="password">
                  <el-input
                    v-model="usernameLoginForm.password"
                    type="password"
                    placeholder="请输入密码"
                    prefix-icon="el-icon-lock"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-checkbox v-model="usernameLoginForm.rememberMe">
                    记住我
                  </el-checkbox>
                </el-form-item>
                
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="loginLoading"
                    @click="handleUsernameLogin"
                    class="login-btn"
                  >
                    登录
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="手机号登录" name="phone">
              <el-form ref="phoneLoginForm" :model="phoneLoginForm" :rules="phoneLoginRules" class="login-form">
                <el-form-item prop="phone">
                  <el-input
                    v-model="phoneLoginForm.phone"
                    placeholder="请输入手机号"
                    prefix-icon="el-icon-mobile-phone"
                    @blur="checkPhoneExists"
                    @input="formatPhone"
                  />
                </el-form-item>
                
                <el-form-item prop="code">
                  <div class="code-input-group">
                    <el-input
                      v-model="phoneLoginForm.code"
                      placeholder="请输入验证码"
                      prefix-icon="el-icon-key"
                      maxlength="6"
                    />
                    <el-button
                      :disabled="codeButtonDisabled"
                      @click="sendCode"
                      class="send-code-btn"
                    >
                      {{ codeButtonText }}
                    </el-button>
                  </div>
                </el-form-item>
                
                <el-form-item>
                  <el-checkbox v-model="phoneLoginForm.agreeTerms">
                    我已阅读并同意
                    <el-link type="primary" @click="showTerms">《用户协议》</el-link>
                    和
                    <el-link type="primary" @click="showPrivacy">《隐私政策》</el-link>
                  </el-checkbox>
                </el-form-item>
                
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="loginLoading"
                    :disabled="!phoneLoginForm.agreeTerms"
                    @click="handlePhoneLogin"
                    class="login-btn"
                  >
                    {{ loginButtonText }}
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
        
        <div class="login-footer">
          <el-link @click="goBack">返回介绍</el-link>
        </div>
      </div>
    </div>
    
    <!-- 验证码验证页面 -->
    <div v-else-if="currentStep === 'verify'" class="verify-step">
      <div class="verify-content">
        <div class="verify-header">
          <h3>验证码验证</h3>
          <p>验证码已发送至 {{ maskPhone(phoneLoginForm.phone) }}</p>
        </div>
        
        <div class="verify-code-input">
          <el-input
            v-model="phoneLoginForm.code"
            placeholder="请输入6位验证码"
            maxlength="6"
            @input="autoSubmit"
            ref="codeInput"
          />
        </div>
        
        <div class="verify-actions">
          <el-button @click="resendCode" :disabled="resendDisabled">
            {{ resendText }}
          </el-button>
          <el-button @click="goBackToLogin">返回修改手机号</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

// 路由
const router = useRouter()

// 状态管理
const authStore = useAuthStore()

// 当前步骤
const currentStep = ref('guide')

// 活跃的登录标签页
const activeLoginTab = ref('username')

// 表单引用
const usernameLoginForm = ref(null)
const phoneLoginForm = ref(null)

// 用户名密码登录表单数据
const usernameLoginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 手机号登录表单数据
const phoneLoginForm = reactive({
  phone: '',
  code: '',
  agreeTerms: false
})

// 登录规则
const usernameLoginRules = {
  username: [
    { required: true, message: '请输入用户名或手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const phoneLoginRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 平台功能介绍
const platformFeatures = ref([
  {
    title: '企业级微服务架构',
    description: '基于Spring Cloud Alibaba的现代化微服务架构，支持高并发、高可用',
    icon: '🏗️',
    benefits: ['服务解耦', '独立部署', '水平扩展']
  },
  {
    title: '多端统一管理',
    description: '一套系统管理PC端、移动端、小程序等多终端应用',
    icon: '📱',
    benefits: ['统一后台', '数据同步', '体验一致']
  },
  {
    title: '行业解决方案',
    description: '提供餐饮、零售、制造、物流等多个行业的专业解决方案',
    icon: '🏪',
    benefits: ['行业定制', '快速部署', '成本优化']
  },
  {
    title: '智能数据分析',
    description: '基于AI的数据分析平台，助力企业数字化转型',
    icon: '📊',
    benefits: ['数据洞察', '智能决策', '业务优化']
  }
])

// 客户痛点解决
const painPoints = ref([
  {
    problem: '系统架构复杂，维护困难',
    solution: '微服务架构，模块化设计，易于维护和扩展'
  },
  {
    problem: '多端数据不同步',
    solution: '统一数据中台，实时数据同步，保证数据一致性'
  },
  {
    problem: '开发效率低下',
    solution: '代码生成器，一键生成CRUD，提高开发效率'
  },
  {
    problem: '系统性能瓶颈',
    solution: '分布式架构，负载均衡，支持高并发访问'
  }
])

// 状态变量
const isNewUser = ref(false)
const loginLoading = ref(false)
const codeButtonDisabled = ref(false)
const codeButtonText = ref('获取验证码')
const loginButtonText = ref('登录')
const resendDisabled = ref(false)
const resendText = ref('重新发送')

// 计算属性
const codeButtonDisabled = computed(() => {
  return !phoneLoginForm.phone || !/^1[3-9]\d{9}$/.test(phoneLoginForm.phone) || codeButtonDisabled.value
})

const loginButtonText = computed(() => {
  return isNewUser.value ? '注册并登录' : '登录'
})

// 方法
// 开始体验
const startExperience = () => {
  currentStep.value = 'login'
}

// 返回介绍
const goBack = () => {
  currentStep.value = 'guide'
}

// 标签页切换
const handleTabChange = (tabName) => {
  activeLoginTab.value = tabName
}

// 格式化手机号
const formatPhone = (value) => {
  // 只允许输入数字
  phoneLoginForm.phone = value.replace(/\D/g, '')
}

// 检查手机号是否存在
const checkPhoneExists = async () => {
  if (!/^1[3-9]\d{9}$/.test(phoneLoginForm.phone)) return
  
  try {
    const response = await authStore.checkPhone(phoneLoginForm.phone)
    isNewUser.value = !response.data.exists
  } catch (error) {
    console.error('检查手机号失败:', error)
  }
}

// 发送验证码
const sendCode = async () => {
  if (!phoneLoginForm.agreeTerms) {
    ElMessage.warning('请先同意用户协议')
    return
  }
  
  try {
    const response = await authStore.sendCode(phoneLoginForm.phone)
    
    // 检查响应状态
    if (response && (response.code === 1 || response.code === 200)) {
      ElMessage.success('验证码已发送')
      startCodeCountdown()
      currentStep.value = 'verify'
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

// 重新发送验证码
const resendCode = async () => {
  try {
    const response = await authStore.sendCode(phoneLoginForm.phone)
    
    // 检查响应状态
    if (response && (response.code === 1 || response.code === 200)) {
      ElMessage.success('验证码已重新发送')
      startResendCountdown()
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

// 重新发送倒计时
const startResendCountdown = () => {
  let countdown = 60
  resendDisabled.value = true
  
  const timer = setInterval(() => {
    countdown--
    resendText.value = `${countdown}s后重发`
    
    if (countdown <= 0) {
      clearInterval(timer)
      resendDisabled.value = false
      resendText.value = '重新发送'
    }
  }, 1000)
}

// 自动提交验证码
const autoSubmit = (value) => {
  if (value.length === 6) {
    handlePhoneLogin()
  }
}

// 处理用户名密码登录
const handleUsernameLogin = async () => {
  try {
    await usernameLoginForm.value.validate()
    loginLoading.value = true
    
    const response = await authStore.usernameLogin(usernameLoginForm.username, usernameLoginForm.password)
    
    if (response.code === 1) {
      ElMessage.success('登录成功！')
      // 登录成功后获取用户信息
      try {
        await authStore.getUserInfo()
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
      router.push('/')
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

// 处理手机号登录
const handlePhoneLogin = async () => {
  if (!phoneLoginForm.agreeTerms) {
    ElMessage.warning('请先同意用户协议')
    return
  }
  
  try {
    await phoneLoginForm.value.validate()
    loginLoading.value = true
    
    const response = await authStore.phoneLogin(phoneLoginForm.phone, phoneLoginForm.code)
    
    if (response.success) {
      ElMessage.success(isNewUser.value ? '注册成功，欢迎使用！' : '登录成功！')
      // 登录成功后获取用户信息
      try {
        await authStore.getUserInfo()
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
      router.push('/')
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

// 返回登录页面
const goBackToLogin = () => {
  currentStep.value = 'login'
}

// 显示用户协议
const showTerms = () => {
  ElMessageBox.alert('用户协议内容...', '用户协议', {
    confirmButtonText: '确定'
  })
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessageBox.alert('隐私政策内容...', '隐私政策', {
    confirmButtonText: '确定'
  })
}

// 手机号脱敏
const maskPhone = (phone) => {
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 生命周期
onMounted(() => {
  // 如果已经登录，直接跳转到首页
  if (authStore.isLoggedIn) {
    router.push('/')
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

/* 引导页面样式 */
.guide-step {
  width: 100%;
  max-width: 1200px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.guide-header {
  text-align: center;
  margin-bottom: 40px;
}

.platform-title {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 700;
}

.platform-subtitle {
  font-size: 1.2rem;
  color: #7f8c8d;
  margin: 0;
}

.guide-carousel {
  margin-bottom: 40px;
}

.carousel-item {
  text-align: center;
  padding: 40px;
}

.feature-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.feature-title {
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 15px;
}

.feature-description {
  font-size: 1.1rem;
  color: #7f8c8d;
  margin-bottom: 20px;
  line-height: 1.6;
}

.feature-benefits {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.benefit-tag {
  background: #3498db;
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 0.9rem;
}

.pain-points {
  margin-bottom: 40px;
}

.pain-points h3 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 30px;
}

.points-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.point-item {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  border-left: 4px solid #e74c3c;
}

.problem {
  color: #e74c3c;
  margin-bottom: 10px;
  font-weight: 600;
}

.solution {
  color: #27ae60;
  font-weight: 500;
}

.guide-actions {
  text-align: center;
}

/* 登录页面样式 */
.login-step {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.login-header p {
  color: #7f8c8d;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.code-input-group {
  display: flex;
  gap: 10px;
}

.send-code-btn {
  width: 120px;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 1.1rem;
}

.login-footer {
  text-align: center;
}

/* 验证页面样式 */
.verify-step {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.verify-header {
  text-align: center;
  margin-bottom: 30px;
}

.verify-header h3 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.verify-header p {
  color: #7f8c8d;
  margin: 0;
}

.verify-code-input {
  margin-bottom: 30px;
}

.verify-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .guide-step {
    padding: 20px;
  }
  
  .platform-title {
    font-size: 2rem;
  }
  
  .carousel-item {
    padding: 20px;
  }
  
  .feature-icon {
    font-size: 3rem;
  }
  
  .feature-title {
    font-size: 1.5rem;
  }
  
  .points-grid {
    grid-template-columns: 1fr;
  }
  
  .login-step,
  .verify-step {
    padding: 30px 20px;
  }
}
</style>
