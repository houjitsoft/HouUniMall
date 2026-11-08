<template>
  <div class="register-container">
    <div class="register-wrapper">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-header">
            <div class="logo">
              <div class="logo-icon">
                <el-icon><Platform /></el-icon>
              </div>
              <h1 class="brand-name">UniMall</h1>
            </div>
            <p class="brand-tagline">企业级数字化管理平台</p>
          </div>
          
          <div class="welcome-section">
            <h2 class="welcome-title">欢迎加入我们</h2>
            <p class="welcome-subtitle">创建您的账户，开启数字化管理之旅</p>
          </div>
          
          <div class="features-section">
            <div class="feature-item">
              <el-icon class="feature-icon"><Check /></el-icon>
              <span class="feature-text">免费试用30天</span>
            </div>
            <div class="feature-item">
              <el-icon class="feature-icon"><Check /></el-icon>
              <span class="feature-text">专业技术支持</span>
            </div>
            <div class="feature-item">
              <el-icon class="feature-icon"><Check /></el-icon>
              <span class="feature-text">数据安全保障</span>
            </div>
          </div>
        </div>
        
        <div class="background-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>

      <!-- 右侧注册表单区域 -->
      <div class="form-section">
        <div class="form-container">
          <div class="form-header">
            <h2 class="form-title">创建账户</h2>
          </div>

          <!-- 注册方式选项卡 -->
          <el-tabs v-model="activeTab" class="register-tabs" @tab-change="handleTabChange">
            <el-tab-pane label="手机号注册" name="phone">
              <template #label>
                <span class="tab-label">
                  <el-icon><Iphone /></el-icon>
                  <span>手机号注册</span>
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="邮箱注册" name="email">
              <template #label>
                <span class="tab-label">
                  <el-icon><Message /></el-icon>
                  <span>邮箱注册</span>
                </span>
              </template>
            </el-tab-pane>
          </el-tabs>

          <el-form 
            ref="registerFormRef" 
            :model="registerForm" 
            :rules="registerRules" 
            class="register-form"
            @submit.prevent="handleRegister"
          >
            <!-- 手机号注册表单 -->
            <template v-if="activeTab === 'phone'">
              <el-form-item prop="userMobile" class="reg-form-item">
                <label class="input-label">手机号</label>
                <el-input
                  v-model="registerForm.userMobile"
                  placeholder="请输入手机号"
                  size="large"
                  class="form-input"
                  maxlength="11"
                  autocomplete="tel"
                  clearable
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Iphone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="phoneCode" class="reg-form-item">
                <label class="input-label">手机验证码</label>
                <el-input
                  v-model="registerForm.phoneCode"
                  placeholder="请输入手机验证码"
                  size="large"
                  class="form-input"
                  maxlength="6"
                  autocomplete="off"
                  clearable
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Key /></el-icon>
                  </template>
                  <template #append>
                    <el-button
                      type="primary"
                      native-type="button"
                      :disabled="phoneCodeCountdown > 0 || !registerForm.userMobile"
                      @click="sendPhoneCode"
                      class="code-btn"
                    >
                      {{ phoneCodeCountdown > 0 ? `${phoneCodeCountdown}s` : '获取验证码' }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
            </template>

            <!-- 邮箱注册表单 -->
            <template v-if="activeTab === 'email'">
              <el-form-item prop="email" class="reg-form-item">
                <label class="input-label">邮箱地址</label>
                <el-input
                  v-model="registerForm.email"
                  placeholder="请输入邮箱地址"
                  size="large"
                  class="form-input"
                  autocomplete="email"
                  clearable
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="emailCode" class="reg-form-item">
                <label class="input-label">邮箱验证码</label>
                <el-input
                  v-model="registerForm.emailCode"
                  placeholder="请输入邮箱验证码"
                  size="large"
                  class="form-input"
                  maxlength="6"
                  autocomplete="off"
                  clearable
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Key /></el-icon>
                  </template>
                  <template #append>
                    <el-button
                      type="primary"
                      native-type="button"
                      :disabled="emailCodeCountdown > 0 || !registerForm.email"
                      @click="sendEmailCode"
                      class="code-btn"
                    >
                      {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s` : '获取验证码' }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
            </template>

            <el-form-item prop="password" class="reg-form-item">
              <label class="input-label">密码</label>
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                class="form-input"
                show-password
                autocomplete="new-password"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="confirmPassword" class="reg-form-item">
              <label class="input-label">确认密码</label>
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                size="large"
                class="form-input"
                show-password
                autocomplete="new-password"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="agreement" class="agreement-form-item">
              <div class="agreement-list">
                <el-checkbox
                  v-for="item in agreements"
                  :key="item.code"
                  v-model="agreementChecked[item.code]"
                  class="agreement-checkbox"
                  @change="handleAgreementChange"
                >
                  <span class="checkbox-text">
                    我已阅读并同意
                    <el-link
                      type="primary"
                      class="agreement-link"
                      @click.prevent="handleShowAgreement(item)"
                    >《{{ item.name }}》</el-link>
                    <el-tag v-if="isAgreementRequired(item)" size="small" type="danger" effect="plain" class="agreement-tag">必选</el-tag>
                  </span>
                </el-checkbox>
              </div>
              <div v-if="agreementError" class="agreement-error">{{ agreementError }}</div>
            </el-form-item>

            <el-form-item class="submit-form-item">
              <el-button 
                type="primary" 
                :loading="registerLoading"
                @click="handleRegister"
                class="register-btn"
              >
                <span class="btn-text">创建账户</span>
                <el-icon class="btn-icon"><Right /></el-icon>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <div class="login-link">
              <span class="login-text">已有账户？</span>
              <el-link type="primary" @click="handleLogin" class="login-btn">
                立即登录
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
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Message, 
  Lock, 
  Right, 
  Check, 
  Platform,
  Iphone,
  Key
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { setAuthInfo } from '@/utils/auth'
import { showServiceTermsAgreement, showPrivacyPolicyAgreement, loadAgreements, showAgreement } from '@/utils/showLegalAgreement'

// 路由
const router = useRouter()

// 状态管理
const authStore = useAuthStore()

// 表单引用
const registerFormRef = ref(null)

// 当前选中的注册方式选项卡
const activeTab = ref('phone')

// 注册表单数据
const registerForm = reactive({
  email: '',           // 邮箱
  userMobile: '',       // 手机号（对应后端userMobile）
  emailCode: '',        // 邮箱验证码
  phoneCode: '',        // 手机验证码
  password: '',         // 密码
  confirmPassword: '',  // 确认密码
  agreement: false      // 同意条款（三项协议全部勾选后为 true，仅用于表单校验）
})

// ================= 注册协议（可在后台维护，带版本号） =================
// 协议清单来自「协议定义 + 生效版本」表；加载失败时回退内置文案，不阻断注册页可用。
const agreements = ref([])
const agreementChecked = reactive({})
const agreementError = ref('')
// 协议清单是否来自后端接口：决定提交时是否回传 {code, versionNo} 供后端校验与留痕
const agreementsFromApi = ref(false)

/** 是否必选协议 */
const isAgreementRequired = (item) => Number(item?.required) === 1

/** 初始化勾选状态（合规要求默认不勾选，这里尊重后端的 defaultChecked 配置） */
const initAgreementChecked = (list) => {
  Object.keys(agreementChecked).forEach((key) => {
    delete agreementChecked[key]
  })
  list.forEach((item) => {
    agreementChecked[item.code] = Number(item.defaultChecked) === 1
  })
  syncAgreementFlag()
}

/** 同步表单校验用的总开关 */
const syncAgreementFlag = () => {
  const requiredList = agreements.value.filter(isAgreementRequired)
  registerForm.agreement = requiredList.length > 0
    ? requiredList.every((item) => !!agreementChecked[item.code])
    : true
}

/** 勾选变化：清理错误提示并即时校验 */
const handleAgreementChange = () => {
  syncAgreementFlag()
  if (registerForm.agreement) {
    agreementError.value = ''
  }
  try {
    const pending = registerFormRef.value?.validateField?.('agreement')
    if (pending && typeof pending.catch === 'function') {
      pending.catch(() => {})
    }
  } catch (error) {
    // 校验回调抛错不影响勾选交互
    console.warn('[register] 协议校验异常', error?.message || error)
  }
}

/** 查看协议正文 */
const handleShowAgreement = (item) => {
  showAgreement(item)
}

// 注册加载状态
const registerLoading = ref(false)
const emailCodeCountdown = ref(0)
const phoneCodeCountdown = ref(0)
const emailCodeTimer = ref(null)
const phoneCodeTimer = ref(null)

// 选项卡切换处理
const handleTabChange = (tabName) => {
  // 切换选项卡时清空另一个选项卡的数据
  if (tabName === 'phone') {
    registerForm.email = ''
    registerForm.emailCode = ''
    emailCodeCountdown.value = 0
    // 清除邮箱验证码倒计时
    if (emailCodeTimer.value) {
      clearInterval(emailCodeTimer.value)
      emailCodeTimer.value = null
    }
    // 确保清空邮箱相关数据
    if (registerFormRef.value) {
      registerFormRef.value.clearValidate(['email', 'emailCode'])
    }
  } else if (tabName === 'email') {
    registerForm.userMobile = ''
    registerForm.phoneCode = ''
    phoneCodeCountdown.value = 0
    // 清除手机验证码倒计时
    if (phoneCodeTimer.value) {
      clearInterval(phoneCodeTimer.value)
      phoneCodeTimer.value = null
    }
    // 确保清空手机号相关数据
    if (registerFormRef.value) {
      registerFormRef.value.clearValidate(['userMobile', 'phoneCode'])
    }
  }
  
  // 清除表单验证状态
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
}

// 注册规则（根据选项卡动态验证）
const registerRules = computed(() => {
  const rules = {
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请确认密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== registerForm.password) {
            callback(new Error('两次输入的密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],
    agreement: [
      {
        validator: (rule, value, callback) => {
          // 逐项校验：任一必选协议未勾选即阻止提交，并给出具体缺哪几项
          const missing = agreements.value
            .filter(isAgreementRequired)
            .filter((item) => !agreementChecked[item.code])
          if (missing.length > 0) {
            agreementError.value = `请阅读并勾选同意《${missing.map((item) => item.name).join('》《')}》后再提交`
            callback(new Error('请阅读并同意全部必选协议'))
            return
          }
          agreementError.value = ''
          syncAgreementFlag()
          callback()
        },
        trigger: 'change'
      }
    ]
  }
  
  // 根据当前选项卡添加相应的验证规则
  if (activeTab.value === 'phone') {
    rules.userMobile = [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
    ]
    rules.phoneCode = [
      { required: true, message: '请输入手机验证码', trigger: 'blur' },
      { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
    ]
  } else if (activeTab.value === 'email') {
    rules.email = [
      { required: true, message: '请输入邮箱地址', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ]
    rules.emailCode = [
      { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
      { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
    ]
  }
  
  return rules
})

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱地址')
    return
  }
  
  // 如果正在倒计时，不允许重复发送
  if (emailCodeCountdown.value > 0) {
    return
  }
  
  try {
    // 调用发送邮箱验证码API
    // 使用 'login' 类型而不是 'register'，避免后端检查手机号是否已注册
    const response = await authStore.sendCode(registerForm.email, 'login')
    
    // 检查响应状态
    if (response && (response.code === 1 || response.code === 200)) {
      ElMessage.success('验证码已发送到邮箱')
      
      // 清除之前的定时器
      if (emailCodeTimer.value) {
        clearInterval(emailCodeTimer.value)
      }
      
      // 开始倒计时
      emailCodeCountdown.value = 60
      emailCodeTimer.value = setInterval(() => {
        emailCodeCountdown.value--
        if (emailCodeCountdown.value <= 0) {
          clearInterval(emailCodeTimer.value)
          emailCodeTimer.value = null
        }
      }, 1000)
    } else {
      // 返回失败信息，显示后端返回的具体错误消息
      const errorMsg = response?.message || '发送验证码失败，请重试'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    // 提取真实的错误信息
    let errorMsg = '发送验证码失败，请重试'
    
    if (error.response?.data?.message) {
      // 从响应数据中提取错误消息
      errorMsg = error.response.data.message
    } else if (error.message) {
      // 从错误对象中提取消息
      errorMsg = error.message
    }
    
    ElMessage.error(errorMsg)
    // 发送失败时重置倒计时
    emailCodeCountdown.value = 0
    if (emailCodeTimer.value) {
      clearInterval(emailCodeTimer.value)
      emailCodeTimer.value = null
    }
  }
}

// 发送手机验证码
const sendPhoneCode = async () => {
  if (!registerForm.userMobile) {
    ElMessage.warning('请先输入手机号')
    return
  }
  
  // 如果正在倒计时，不允许重复发送
  if (phoneCodeCountdown.value > 0) {
    return
  }
  
  try {
    // 注册场景必须传 register：thirdparty 会在发短信前校验手机号是否已注册，已注册则返回错误、不发短信
    const response = await authStore.sendCode(registerForm.userMobile, 'register')
    
    // 检查响应状态
    if (response && (response.code === 1 || response.code === 200)) {
      ElMessage.success('验证码已发送到手机')
      
      // 清除之前的定时器
      if (phoneCodeTimer.value) {
        clearInterval(phoneCodeTimer.value)
      }
      
      // 开始倒计时
      phoneCodeCountdown.value = 60
      phoneCodeTimer.value = setInterval(() => {
        phoneCodeCountdown.value--
        if (phoneCodeCountdown.value <= 0) {
          clearInterval(phoneCodeTimer.value)
          phoneCodeTimer.value = null
        }
      }, 1000)
    } else {
      // 返回失败信息，显示后端返回的具体错误消息
      const errorMsg = response?.message || '发送验证码失败，请重试'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    // 提取真实的错误信息
    let errorMsg = '发送验证码失败，请重试'
    
    if (error.response?.data?.message) {
      // 从响应数据中提取错误消息
      errorMsg = error.response.data.message
    } else if (error.message) {
      // 从错误对象中提取消息
      errorMsg = error.message
    }
    
    ElMessage.error(errorMsg)
    // 发送失败时重置倒计时
    phoneCodeCountdown.value = 0
    if (phoneCodeTimer.value) {
      clearInterval(phoneCodeTimer.value)
      phoneCodeTimer.value = null
    }
  }
}

// 处理注册
const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    
    // 根据当前选项卡验证必填项
    if (activeTab.value === 'phone') {
      if (!registerForm.userMobile) {
        ElMessage.error('请输入手机号')
        return
      }
      if (!registerForm.phoneCode) {
        ElMessage.error('请输入手机验证码')
        return
      }
    } else if (activeTab.value === 'email') {
      if (!registerForm.email) {
        ElMessage.error('请输入邮箱地址')
        return
      }
      if (!registerForm.emailCode) {
        ElMessage.error('请输入邮箱验证码')
        return
      }
    }
    
    // 准备注册数据，只传递后端需要的参数
    const registerData = {
      email: activeTab.value === 'email' ? registerForm.email : '',
      userMobile: activeTab.value === 'phone' ? registerForm.userMobile : '',
      phoneCode: activeTab.value === 'phone' ? registerForm.phoneCode : '',
      password: registerForm.password,
      platForm: "5",  // 管理后台注册，平台类型为后台管理
      registerTerminal: "houcloud"
    }

    // 已勾选协议的编码与版本号：仅当清单来自后端接口时回传，
    // 供后端校验「版本未过期」并留痕（同意时间 / IP / UA 由后端补全）。
    // 接口不可用时降级为不上报，避免因清单缺失被后端误判为「协议已更新」。
    if (agreementsFromApi.value) {
      registerData.agreements = agreements.value
        .filter((item) => agreementChecked[item.code])
        .map((item) => ({ code: item.code, versionNo: item.versionNo }))
      registerData.agreementsSource = 'api'
    } else {
      registerData.agreementsSource = 'fallback'
    }
    
    // 调用注册API
    const response = await authStore.register(registerData)
    
    if (response.code === 1) {
      // 注册成功，获取token（后端返回的data就是token字符串）
      const token = response.data
      
      if (token) {
        // 保存token到localStorage
        setAuthInfo(token, null) // 只保存token，用户信息后续可以通过token获取
        
        // 更新authStore状态
        authStore.token = token
        authStore.isLoggedIn = true
        
        ElMessage.success('注册成功')
        
        // 跳转到首页
        setTimeout(() => {
          router.push('/')
        }, 500)
      } else {
        ElMessage.warning('注册成功，但未获取到token，请登录')
        router.push('/login')
      }
    } else {
      // 注册失败
      ElMessage.error(response.message || '注册失败，请稍后重试')
    }
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    registerLoading.value = false
  }
}

// 跳转到登录
const handleLogin = () => {
  router.push('/login')
}

// 服务条款
const handleTermsOfService = () => {
  showServiceTermsAgreement()
}

// 隐私政策
const handlePrivacyPolicy = () => {
  showPrivacyPolicyAgreement()
}

// 加载注册协议清单（含当前生效版本正文）
onMounted(async () => {
  try {
    const { agreements: list, fromApi } = await loadAgreements('register')
    agreements.value = list
    agreementsFromApi.value = fromApi
    initAgreementChecked(list)
  } catch (error) {
    console.warn('[register] 加载注册协议失败', error)
    agreements.value = []
    agreementsFromApi.value = false
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (emailCodeTimer.value) {
    clearInterval(emailCodeTimer.value)
    emailCodeTimer.value = null
  }
  if (phoneCodeTimer.value) {
    clearInterval(phoneCodeTimer.value)
    phoneCodeTimer.value = null
  }
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  /* 移动端优化 */
  -webkit-overflow-scrolling: touch;
  overflow-x: hidden;
}

.register-wrapper {
  width: 100%;
  max-width: 1200px;
  min-height: 700px;
  height: auto;
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

.welcome-section {
  margin-bottom: 40px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 12px 0;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
  line-height: 1.5;
}

.features-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.feature-icon {
  font-size: 20px;
  color: #4ade80;
  flex-shrink: 0;
}

.feature-text {
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

/* 右侧注册表单区域 */
.form-section {
  flex: 1;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 30px;
  position: relative;
  animation: fadeInRight 1s ease-out 0.4s both;
}
.reg-form-item {
  margin-bottom: 0;
}

.reg-form-item :deep(.el-form-item__error) {
  margin-top: 6px;
  line-height: 1.4;
  padding-top: 2px;
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
  margin-bottom: 8px;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 选项卡样式 */
.register-tabs {
  margin-bottom: 8px;
}

.register-tabs :deep(.el-tabs__header) {
  margin: 0 0 16px 0;
}

.register-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #e4e7ed;
  height: 1px;
}

.register-tabs :deep(.el-tabs__item) {
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
  font-size: 15px;
  font-weight: 500;
  color: #606266;
  transition: all 0.3s ease;
}

.register-tabs :deep(.el-tabs__item:hover) {
  color: #667eea;
}

.register-tabs :deep(.el-tabs__item.is-active) {
  color: #667eea;
  font-weight: 600;
}

.register-tabs :deep(.el-tabs__active-bar) {
  background-color: #667eea;
  height: 3px;
  border-radius: 2px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-label .el-icon {
  font-size: 18px;
}

.register-form {
  margin-bottom: 20px;
}

/* 统一表单项垂直节奏：避免上一项输入框与下一项标签挤在一起 */
.register-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.register-form :deep(.el-form-item.reg-form-item) {
  margin-bottom: 22px;
}

.register-form :deep(.el-form-item.agreement-form-item) {
  margin-bottom: 18px;
  margin-top: 4px;
}

.register-form :deep(.el-form-item.submit-form-item) {
  margin-bottom: 0;
  margin-top: 8px;
}

.register-form :deep(.el-form-item__content) {
  display: block;
  line-height: 1.5;
}

.register-form :deep(.el-form-item__error) {
  margin-top: 6px;
  padding-top: 2px;
  line-height: 1.4;
  font-size: 12px;
}

.input-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
  margin-top: 0;
  line-height: 1.5;
}

.form-input {
  height: 40px;
  border-radius: 6px;
  border: 1px solid #e1e5e9;
  transition: all 0.2s ease;
  /* 移动端优化 */
  -webkit-appearance: none;
  appearance: none;
  font-size: 16px; /* 防止iOS自动缩放 */
}

/* 前缀图标与文字留出内边距，避免贴边 */
.register-form :deep(.form-input .el-input__wrapper) {
  padding-left: 12px;
}

.register-form :deep(.form-input .el-input__prefix-inner > :first-child) {
  margin-right: 6px;
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

/* 验证码按钮样式 */
.code-btn {
  border-radius: 0 8px 8px 0;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 100px;
  /* 移动端优化 */
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  min-height: 40px; /* 确保触摸区域足够大 */
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.code-btn:disabled {
  background: #e1e8ed;
  color: #999;
  cursor: not-allowed;
}

/* 可选字段提示样式 */
.optional-text {
  color: #999;
  font-size: 12px;
  font-weight: normal;
}

.agreement-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.agreement-checkbox {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 0;
  margin-right: 0;
  height: auto;
  line-height: 1.6;
}

.agreement-checkbox :deep(.el-checkbox__label) {
  white-space: normal;
  line-height: 1.5;
}

.agreement-link {
  vertical-align: baseline;
}

.agreement-tag {
  margin-left: 4px;
  transform: scale(0.85);
}

.agreement-error {
  margin-top: 6px;
  font-size: 12px;
  color: #f56c6c;
  line-height: 1.5;
}

.checkbox-text {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.register-btn {
  width: 100%;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 600;
  color: white;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  /* 移动端优化 */
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  cursor: pointer;
}

.register-btn:hover {
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

.register-btn:hover .btn-icon {
  transform: translateX(2px);
}

.form-footer {
  text-align: center;
}

.login-link {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.login-text {
  font-size: 14px;
  color: #666;
}

.login-btn {
  font-size: 14px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .register-wrapper {
    max-width: 1000px;
    min-height: 650px;
    height: auto;
  }
  
  .brand-section,
  .form-section {
    padding: 50px 35px;
  }
}

@media (max-width: 992px) {
  .register-wrapper {
    max-width: 900px;
    min-height: 600px;
    height: auto;
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
  
  .welcome-title {
    font-size: 24px;
  }
  
  .form-title {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .register-container {
    padding: 0;
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  .register-wrapper {
    flex-direction: column;
    height: auto;
    max-width: 100%;
    min-height: 100vh;
    border-radius: 0;
    box-shadow: none;
  }
  
  .brand-section {
    padding: 30px 20px;
    min-height: auto;
    flex: 0 0 auto;
  }
  
  .brand-header {
    margin-bottom: 20px;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .logo {
    justify-content: center;
    margin-bottom: 10px;
  }
  
  .brand-name {
    font-size: 24px;
  }
  
  .brand-tagline {
    font-size: 14px;
    text-align: center;
  }
  
  .welcome-section {
    margin-bottom: 20px;
  }
  
  .welcome-title {
    font-size: 20px;
    text-align: center;
  }
  
  .welcome-subtitle {
    font-size: 14px;
    text-align: center;
  }
  
  .features-section {
    gap: 12px;
  }
  
  .feature-item {
    padding: 10px 14px;
    justify-content: center;
  }
  
  .feature-text {
    font-size: 13px;
  }
  
  .form-section {
    padding: 30px 20px;
    flex: 1;
  }
  
  .form-container {
    max-width: 100%;
  }
  
  .form-title {
    font-size: 20px;
  }
  
  .form-subtitle {
    font-size: 14px;
  }
  
  .register-tabs :deep(.el-tabs__item) {
    padding: 0 16px;
    height: 44px;
    line-height: 44px;
    font-size: 14px;
  }
  
  .form-input {
    height: 48px;
    font-size: 16px; /* 防止iOS自动缩放 */
  }
  
  .input-label {
    font-size: 14px;
    margin-bottom: 10px;
  }
  
  .code-btn {
    min-width: 90px;
    font-size: 13px;
    padding: 0 12px;
  }
  
  .register-btn {
    height: 48px;
    font-size: 16px;
  }
  
  .agreement-checkbox {
    margin-bottom: 20px;
  }
  
  .checkbox-text {
    font-size: 12px;
    line-height: 1.5;
  }
}

@media (max-width: 576px) {
  .register-container {
    padding: 0;
  }
  
  .register-wrapper {
    max-width: 100%;
    border-radius: 0;
    min-height: 100vh;
  }
  
  .brand-section {
    padding: 25px 15px;
    min-height: auto;
  }
  
  .brand-name {
    font-size: 22px;
  }
  
  .brand-tagline {
    font-size: 13px;
  }
  
  .welcome-title {
    font-size: 18px;
  }
  
  .welcome-subtitle {
    font-size: 13px;
  }
  
  .features-section {
    gap: 10px;
  }
  
  .feature-item {
    padding: 8px 12px;
  }
  
  .feature-text {
    font-size: 12px;
  }
  
  .form-section {
    padding: 25px 15px;
  }
  
  .form-title {
    font-size: 20px;
  }
  
  .form-subtitle {
    font-size: 13px;
  }
  
  .input-label {
    font-size: 14px;
    margin-bottom: 8px;
  }
  
  .form-input {
    height: 48px;
    font-size: 16px; /* 防止iOS自动缩放 */
  }
  
  .code-btn {
    min-width: 85px;
    font-size: 12px;
    padding: 0 10px;
  }
  
  .register-btn {
    height: 48px;
    font-size: 16px;
  }
  
  .register-tabs :deep(.el-tabs__item) {
    padding: 0 12px;
    height: 42px;
    line-height: 42px;
    font-size: 14px;
  }
  
  .tab-label .el-icon {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .register-container {
    padding: 0;
  }
  
  .register-wrapper {
    border-radius: 0;
    min-height: 100vh;
  }
  
  .brand-section {
    padding: 20px 15px;
    min-height: auto;
  }
  
  .brand-header {
    margin-bottom: 15px;
  }
  
  .logo-icon {
    width: 40px;
    height: 40px;
  }
  
  .logo-icon .el-icon {
    font-size: 20px;
  }
  
  .brand-name {
    font-size: 20px;
  }
  
  .brand-tagline {
    font-size: 12px;
  }
  
  .welcome-section {
    margin-bottom: 15px;
  }
  
  .welcome-title {
    font-size: 18px;
  }
  
  .welcome-subtitle {
    font-size: 12px;
  }
  
  .features-section {
    gap: 8px;
  }
  
  .feature-item {
    padding: 8px 10px;
  }
  
  .feature-icon {
    font-size: 18px;
  }
  
  .feature-text {
    font-size: 12px;
  }
  
  .form-section {
    padding: 20px 15px;
  }
  
  .form-title {
    font-size: 20px;
    margin-bottom: 10px;
  }
  
  .register-tabs {
    margin-bottom: 20px;
  }
  
  .register-tabs :deep(.el-tabs__item) {
    padding: 0 10px;
    height: 40px;
    line-height: 40px;
    font-size: 13px;
  }
  
  .tab-label {
    gap: 6px;
  }
  
  .tab-label .el-icon {
    font-size: 16px;
  }
  
  .input-label {
    font-size: 14px;
    margin-bottom: 8px;
  }
  
  .form-input {
    height: 48px;
    font-size: 16px; /* 防止iOS自动缩放 */
  }
  
  .input-icon {
    font-size: 16px;
  }
  
  .code-btn {
    min-width: 80px;
    font-size: 12px;
    padding: 0 8px;
  }
  
  .register-btn {
    height: 48px;
    font-size: 16px;
  }
  
  .checkbox-text {
    font-size: 12px;
  }
  
  .login-text {
    font-size: 13px;
  }
  
  .login-btn {
    font-size: 13px;
  }
}

/* 横屏适配 */
@media (max-height: 600px) and (orientation: landscape) {
  .register-wrapper {
    height: 100vh;
    max-height: 100vh;
    overflow-y: auto;
  }
  
  .brand-section {
    padding: 15px 20px;
    min-height: auto;
  }
  
  .brand-header {
    margin-bottom: 10px;
  }
  
  .brand-name {
    font-size: 18px;
  }
  
  .brand-tagline {
    font-size: 11px;
  }
  
  .welcome-section {
    margin-bottom: 10px;
  }
  
  .welcome-title {
    font-size: 16px;
  }
  
  .welcome-subtitle {
    font-size: 11px;
  }
  
  .features-section {
    gap: 6px;
  }
  
  .feature-item {
    padding: 6px 10px;
  }
  
  .feature-text {
    font-size: 11px;
  }
  
  .form-section {
    padding: 15px 20px;
  }
  
  .form-title {
    font-size: 18px;
    margin-bottom: 6px;
  }
  
  .form-subtitle {
    font-size: 12px;
  }
  
  .register-tabs {
    margin-bottom: 15px;
  }
  
  .register-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}

/* 高分辨率屏幕优化 */
@media (min-width: 1920px) {
  .register-wrapper {
    max-width: 1400px;
    height: 800px;
  }
  
  .brand-name {
    font-size: 36px;
  }
  
  .brand-tagline {
    font-size: 18px;
  }
  
  .welcome-title {
    font-size: 32px;
  }
  
  .welcome-subtitle {
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
  
  .register-btn {
    height: 52px;
    font-size: 18px;
  }
}
</style>
