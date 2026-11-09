<template>
  <div class="login-container" :class="{ 'login-container--mobile': isMobile }">
    <!-- 多语言切换按钮 -->
    <div class="language-switch-container">
      <LanguageSwitch />
    </div>

    <!-- 移动端 AI 入口（独立于 brand-section，避免被隐藏） -->
    <div v-if="isMobile" class="ai-fab">
      <img src="@/assets/images/icons/xiaojiang.gif" alt="小匠AI" class="ai-fab-icon" />
      <button type="button" class="ai-fab-btn" @click="handleAISelect">
        {{ t('auth.login.aiSelect') }}
      </button>
    </div>

    <AISelectDialog
      v-model="showAIDialog"
      @login-success="handleLoginSuccess"
    />
    
    <div class="login-wrapper">
      <!-- 左侧服务选择区域（仅桌面端） -->
      <div v-if="!isMobile" class="brand-section">
        <div class="ai-select-button-container">
          <img src="@/assets/images/icons/xiaojiang.gif" alt="小匠AI" class="ai-icon" />
          <el-button 
            type="primary" 
            size="small" 
            class="ai-select-button"
            @click="handleAISelect"
          >
            {{ t('auth.login.aiSelect') }}
          </el-button>
        </div>
        
        <div class="brand-content">
          <div class="brand-header">
            <div class="logo">
              <div class="logo-icon">
                <el-icon><Platform /></el-icon>
              </div>
              <h1 class="brand-name">HouUniMall</h1>
            </div>
            <p class="brand-tagline">{{ t('auth.login.platformTagline') }}</p>
          </div>
          
          <div class="services-section">
            <h3 class="services-title">{{ t('auth.login.selectServices') }}</h3>
            <div class="services-grid">
              <div 
                v-for="service in displayedServices" 
                :key="service.key"
                class="service-card"
                :class="{ 'selected': selectedServices.includes(service.key) }"
                @click="toggleService(service.key)"
              >
                <div class="service-checkbox">
                  <el-icon v-if="selectedServices.includes(service.key)"><Check /></el-icon>
                </div>
                <div class="service-icon">
                  <el-icon><component :is="service.icon" /></el-icon>
                </div>
                <div class="service-name">{{ service.name }}</div>
              </div>
              
              <!-- 更多按钮 -->
              <div 
                class="service-card more-card"
                @click="showMoreServicesDialog = true"
              >
                <div class="service-icon">
                  <el-icon><More /></el-icon>
                </div>
                <div class="service-name">{{ t('auth.login.moreServices') }}</div>
              </div>
            </div>
          </div>
          
          <div class="services-footer">
            <p class="services-tip">{{ t('auth.login.servicesTip') }}</p>
          </div>
        </div>
        
        <div class="background-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>

      <!-- 登录表单区域 -->
      <div class="form-section">
        <div class="form-container">
          <div v-if="isMobile" class="mobile-hero">
            <div class="mobile-logo">
              <span class="mobile-logo-icon"><el-icon><Platform /></el-icon></span>
              <span class="mobile-logo-text">HouUniMall</span>
            </div>
            <h2 class="mobile-welcome">{{ t('auth.login.welcomeLogin') }}</h2>
            <p class="mobile-sub">{{ t('auth.login.enterAccountInfo') }}</p>
          </div>

          <div v-else class="form-header">
            <h2 class="form-title">{{ t('auth.login.welcomeLogin') }}</h2>
            <p class="form-subtitle">{{ t('auth.login.enterAccountInfo') }}</p>
          </div>

          <div v-if="loginError" class="login-error-banner" role="alert">
            <el-icon class="login-error-icon"><Warning /></el-icon>
            <span class="login-error-text">{{ loginError }}</span>
            <button type="button" class="login-error-close" aria-label="关闭" @click="loginError = ''">×</button>
          </div>

          <el-form 
            ref="loginFormRef" 
            :model="loginForm" 
            :rules="loginRules" 
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <label v-if="!isMobile" class="input-label">{{ t('auth.login.username') }}</label>
              <el-input
                v-model="loginForm.username"
                :placeholder="t('auth.login.username')"
                size="large"
                class="form-input"
                @input="loginError = ''"
              >
                <template #prefix>
                  <el-icon class="input-icon"><UserFilled /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password">
              <label v-if="!isMobile" class="input-label">{{ t('auth.login.password') }}</label>
              <el-input
                v-model="loginForm.password"
                type="password"
                :placeholder="t('auth.login.password')"
                size="large"
                class="form-input"
                show-password
                @input="loginError = ''"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="captcha">
              <label v-if="!isMobile" class="input-label">图形验证码</label>
              <div class="captcha-row">
                <el-input
                  v-model="loginForm.captcha"
                  placeholder="请输入验证码"
                  size="large"
                  class="form-input captcha-input"
                  maxlength="6"
                  @input="loginError = ''"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Key /></el-icon>
                  </template>
                </el-input>
                <img
                  v-if="captchaImage"
                  :src="captchaImage"
                  class="captcha-img"
                  title="点击刷新验证码"
                  alt="图形验证码"
                  @click="loadCaptcha"
                />
                <div v-else class="captcha-img captcha-img--loading" title="点击重试" @click="loadCaptcha">
                  <el-icon><Refresh /></el-icon>
                </div>
              </div>
            </el-form-item>

            <div class="form-options" :class="{ 'form-options--mobile': isMobile }">
              <el-checkbox v-model="loginForm.rememberMe" class="remember-checkbox">
                <span class="checkbox-text">{{ t('auth.login.rememberMe') }}</span>
              </el-checkbox>
              <div class="links-row">
                <button type="button" class="text-link" @click.prevent="handleRegister">
                  {{ t('auth.login.registerNow') }}
                </button>
                <button type="button" class="text-link" @click.prevent="handleForgotPassword">
                  {{ t('auth.login.forgotPassword') }}
                </button>
              </div>
            </div>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loginLoading"
                native-type="submit"
                class="login-btn"
              >
                <span class="btn-text">{{ t('auth.login.login') }}</span>
                <el-icon class="btn-icon"><Right /></el-icon>
              </el-button>
            </el-form-item>

            <div class="divider">
              <span class="divider-text">{{ t('auth.login.or') }}</span>
            </div>

            <div class="alternative-login">
              <el-button 
                @click="phoneLoginVisible = true" 
                class="alt-btn phone-btn"
              >
                <el-icon class="alt-icon"><Iphone /></el-icon>
                <span class="alt-text">{{ t('auth.login.phoneLogin') }}</span>
              </el-button>
              <!-- 第三方登录方式由插件通过扩展点注入（微信快捷登录等），宿主不内置具体实现 -->
              <component
                v-for="provider in loginProviders"
                :key="provider.code"
                :is="provider.entry"
              />
            </div>
          </el-form>

          <div class="form-footer">
            <div class="security-info">
              <div class="security-item">
                <el-icon class="security-icon"><Warning /></el-icon>
                <span class="security-text">{{ t('auth.login.sslEncryption') }}</span>
              </div>
              <div class="security-item">
                <el-icon class="security-icon"><Check /></el-icon>
                <span class="security-text">{{ t('auth.login.privacyProtection') }}</span>
              </div>
            </div>
            <p v-if="isMobile" class="mobile-app-tagline">{{ t('auth.login.platformTagline') }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 手机号登录弹窗 -->
    <el-dialog
      v-model="phoneLoginVisible"
      :title="t('auth.login.phoneLogin')"
      :width="isMobile ? '92%' : '450px'"
      :fullscreen="isMobile"
      class="phone-dialog"
      :class="{ 'phone-dialog--mobile': isMobile }"
    >
      <el-form 
        ref="phoneFormRef" 
        :model="phoneForm" 
        :rules="phoneRules"
        class="phone-form"
      >
        <el-form-item prop="phone">
          <label class="input-label">{{ t('auth.login.phone') }}</label>
          <el-input
            v-model="phoneForm.phone"
            :placeholder="t('auth.login.phoneRequired')"
            size="large"
            class="form-input"
            maxlength="11"
          >
            <template #prefix>
              <el-icon class="input-icon"><Iphone /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="code">
          <label class="input-label">{{ t('auth.login.code') }}</label>
          <div class="code-input-wrapper">
            <el-input
              v-model="phoneForm.code"
              :placeholder="t('auth.login.codeRequired')"
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
              :disabled="codeCountdown > 0"
              @click="sendPhoneCode"
              class="code-btn"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : t('auth.login.sendCode') }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="phoneLoginVisible = false" class="cancel-btn">{{ t('auth.login.cancel') }}</el-button>
        <el-button 
          type="primary" 
          :loading="phoneLoginLoading"
          @click="handlePhoneLoginSubmit"
          class="confirm-btn"
        >
          {{ t('auth.login.login') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 更多服务选择弹窗 -->
    <el-dialog
      v-model="showMoreServicesDialog"
      :title="t('auth.login.selectServices')"
      width="800px"
      :close-on-click-modal="false"
      class="more-services-dialog"
    >
      <div class="dialog-content">
        <div class="dialog-description">
          <p>{{ t('auth.login.servicesTip') }}</p>
        </div>
        
        <div class="services-grid-dialog">
          <div 
            v-for="service in allServices" 
            :key="service.key"
            class="service-card-dialog"
            :class="{ 'selected': selectedServices.includes(service.key) }"
            @click="toggleService(service.key)"
          >
            <div class="service-checkbox-dialog">
              <el-icon v-if="selectedServices.includes(service.key)"><Check /></el-icon>
            </div>
            <div class="service-icon-dialog">
              <el-icon><component :is="service.icon" /></el-icon>
            </div>
            <div class="service-name-dialog">{{ service.name }}</div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showMoreServicesDialog = false" class="cancel-btn">{{ t('auth.login.cancel') }}</el-button>
          <el-button 
            type="primary" 
            @click="confirmServiceSelection"
            class="confirm-btn"
          >
            {{ t('auth.login.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AISelectDialog from '@/components/AISelectDialog.vue'
import { 
  Message, 
  Lock, 
  Right, 
  Iphone, 
  UserFilled, 
  Warning, 
  Check, 
  Platform,
  TrendCharts,
  Link,
  Key,
  Document,
  ShoppingCart,
  User,
  Setting,
  DataBoard,
  ChatDotRound,
  Monitor,
  Goods,
  List,
  Bell,
  Calendar,
  Location,
  Wallet,
  Money,
  House,
  Tools,
  More,
  Loading,
  Refresh
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
// 使用auth模块本地的LanguageSwitch组件
import LanguageSwitch from '@/components/LanguageSwitch.vue'
// 导入认证服务
import { authService } from '@/api/auth/auth.js'
// 导入认证工具函数
import { setAuthInfo } from '@/utils/auth'
import { appUrl } from '@/utils/appBase'
import { listLoginProviders } from '@/extensions/loginProviders'
import { loadLoginStagePlugins } from '@/utils/loginPluginLoader'
import { showServiceTermsAgreement, showPrivacyPolicyAgreement } from '@/utils/showLegalAgreement'

// 国际化 - 使用统一的 Element Plus 国际化系统
import i18nElement, { setLocale, getLocale, useI18n } from '@/i18n-element'
const { t } = useI18n()

// locale 使用 computed，响应式
const locale = computed({
  get: () => getLocale(),
  set: (value) => {
    setLocale(value)
  }
})

// tm 函数 - 获取指定键的所有层级（业务文本）
const tm = (key) => {
  const currentLocale = getLocale()
  const messages = i18nElement.businessMessages[currentLocale] || i18nElement.businessMessages['zh-CN'] || {}
  const keys = key.split('.')
  let result = messages
  for (const k of keys) {
    if (result && typeof result === 'object') {
      result = result[k]
    } else {
      return {}
    }
  }
  return result || {}
}

// 路由
const router = useRouter()

// 状态管理
const authStore = useAuthStore()

// 表单引用
const loginFormRef = ref(null)
const phoneFormRef = ref(null)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
})

// 图形验证码（新版 auth 对后台登录强制校验：code + sign 一起提交）
const captchaImage = ref('')
const captchaSign = ref('')

const loadCaptcha = async () => {
  try {
    const data = await authService.getCaptcha()
    captchaImage.value = data.image
    captchaSign.value = data.sign
    loginForm.captcha = ''
  } catch (error) {
    console.error('获取图形验证码失败:', error)
    captchaImage.value = ''
  }
}

// 手机号登录表单数据
const phoneForm = reactive({
  phone: '',
  code: ''
})

// 选中的服务
const selectedServices = ref([])

// AI选择对话框
const showAIDialog = ref(false)

// 主服务列表
const services = ref([])

// 初始化服务列表
const initServices = () => {
  // 检查t函数是否可用，如果不可用则延迟执行
  if (typeof t !== 'function') {
    setTimeout(() => {
      initServices()
    }, 100)
    return
  }
  
  services.value = [
    {
      key: 'dashboard',
      name: t('auth.login.dashboard'),
      icon: 'DataBoard'
    },
    {
      key: 'auth',
      name: t('auth.login.authCenter'),
      icon: 'User'
    },
    {
      key: 'oa',
      name: t('auth.login.oa'),
      icon: 'Document'
    },
    {
      key: 'crm',
      name: t('auth.login.crm'),
      icon: 'User'
    },
    {
      key: 'mall',
      name: t('auth.login.mall'),
      icon: 'ShoppingCart'
    },
    {
      key: 'goods',
      name: t('auth.login.goods'),
      icon: 'Goods'
    },
    {
      key: 'order',
      name: t('auth.login.order'),
      icon: 'List'
    },
    {
      key: 'payment',
      name: t('auth.login.payment'),
      icon: 'Wallet'
    },
    {
      key: 'user',
      name: t('auth.login.user'),
      icon: 'User'
    },
    {
      key: 'activity',
      name: t('auth.login.activity'),
      icon: 'Bell'
    },
    {
      key: 'cms',
      name: t('auth.login.cms'),
      icon: 'Document'
    },
    {
      key: 'dcenter',
      name: t('auth.login.dcenter'),
      icon: 'TrendCharts'
    },
    {
      key: 'im',
      name: t('auth.login.im'),
      icon: 'ChatDotRound'
    },
    {
      key: 'lowcode',
      name: t('auth.login.lowcode'),
      icon: 'Setting'
    },
    {
      key: 'mes',
      name: t('auth.login.mes'),
      icon: 'Monitor'
    },
    {
      key: 'orderfood',
      name: t('auth.login.orderfood'),
      icon: 'Location'
    },
    {
      key: 'rider',
      name: t('auth.login.rider'),
      icon: 'User'
    },
    {
      key: 'supplier',
      name: t('auth.login.supplier'),
      icon: 'User'
    },
    {
      key: 'project',
      name: t('auth.login.project'),
      icon: 'Calendar'
    },
    {
      key: 'erp',
      name: t('auth.login.erp'),
      icon: 'Setting'
    }
  ]
}

// 显示的服务 (默认8个)
const displayedServices = ref([])
// 隐藏的服务
const hiddenServices = ref([])

// 是否显示更多服务弹窗
const showMoreServicesDialog = ref(false)

// 所有服务（用于弹窗显示）
const allServices = computed(() => services.value)

// 登录规则
const loginRules = computed(() => ({
  username: [
    { required: true, message: t('auth.login.usernameRequired'), trigger: 'blur' },
    { min: 2, message: t('auth.login.usernameMinLength'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('auth.login.passwordRequired'), trigger: 'blur' },
    { min: 6, message: t('auth.login.passwordMinLength'), trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' }
  ]
}))

// 手机号登录规则
const phoneRules = computed(() => ({
  phone: [
    { required: true, message: t('auth.login.phoneRequired'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('auth.login.phoneInvalid'), trigger: 'blur' }
  ],
  code: [
    { required: true, message: t('auth.login.codeRequired'), trigger: 'blur' },
    { pattern: /^\d{6}$/, message: t('auth.login.codeInvalid'), trigger: 'blur' }
  ]
}))

// 状态变量
const MOBILE_BP = 768
const isMobile = ref(false)
const loginError = ref('')
const loginLoading = ref(false)
const phoneLoginVisible = ref(false)
const phoneLoginLoading = ref(false)
const codeCountdown = ref(0)
const countdownTimer = ref(null)

// 插件通过扩展点注入的第三方登录方式（微信扫码等），未装载任何插件时为空数组
const loginProviders = computed(() => listLoginProviders())

const updateDevice = () => {
  isMobile.value = window.innerWidth < MOBILE_BP
}

const normalizeLoginError = (error, fallback) => {
  const raw = error?.message || error?.response?.data?.message || fallback
  if (!raw) return fallback
  return String(raw)
    .replace(/^服务器错误\s*\(undefined\):\s*/i, '')
    .replace(/^服务器错误\s*\(\d+\):\s*/i, '')
    .trim() || fallback
}

// 在组件挂载时初始化服务列表
onMounted(() => {
  updateDevice()
  window.addEventListener('resize', updateDevice, { passive: true })
  // 拉取图形验证码（后台登录强制校验）
  loadCaptcha()
  // 初始化服务列表
  nextTick(() => {
    initServices()
    // 初始化显示的服务
    displayedServices.value = services.value.slice(0, 8)
    hiddenServices.value = services.value.slice(8)
  })
  
  // 检查是否有保存的服务选择
  const savedServices = localStorage.getItem('selectedServices')
  if (savedServices) {
    selectedServices.value = JSON.parse(savedServices)
  }
  
  // 监听语言变更事件
  window.addEventListener('language-changed', handleLanguageChange)
  window.addEventListener('language-reloaded', handleLanguageChange)

  // 装载声明了登录方式的插件（未登录也可用），装载结果写入扩展点注册表
  loadLoginStagePlugins()
})

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', updateDevice)
  // 清理倒计时定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  
  // 移除事件监听器
  window.removeEventListener('language-changed', handleLanguageChange)
  window.removeEventListener('language-reloaded', handleLanguageChange)
})

// 处理语言变更
const handleLanguageChange = () => {
  // 重新初始化服务列表以更新翻译
  initServices()
  displayedServices.value = services.value.slice(0, 8)
  hiddenServices.value = services.value.slice(8)
}

// 切换服务选择
const toggleService = (serviceKey) => {
  const index = selectedServices.value.indexOf(serviceKey)
  if (index > -1) {
    selectedServices.value.splice(index, 1)
  } else {
    selectedServices.value.push(serviceKey)
  }
  
  // 保存到localStorage
  localStorage.setItem('selectedServices', JSON.stringify(selectedServices.value))
}

// 确认服务选择
const confirmServiceSelection = () => {
  // 保存到localStorage
  localStorage.setItem('selectedServices', JSON.stringify(selectedServices.value))
  ElMessage.success(t('auth.login.serviceSelectionSaved'))
  showMoreServicesDialog.value = false
}

// 处理登录
const handleLogin = async () => {
  try {
    loginError.value = ''
    await loginFormRef.value.validate()
    loginLoading.value = true
    
    const response = await authStore.usernameLogin(loginForm.username, loginForm.password, {
      code: loginForm.captcha,
      sign: captchaSign.value
    })

    if (response.code === 1) {
      if (!isMobile.value) {
        ElMessage.success(t('auth.login.loginSuccess'))
      }
      router.push('/')
    } else {
      const msg = response.message || t('auth.login.loginFailed')
      loginError.value = msg
      if (!isMobile.value) ElMessage.error(msg)
      // 验证码一次性使用，失败后必须换新码
      loadCaptcha()
    }
  } catch (error) {
    console.error('登录失败:', error)
    const msg = normalizeLoginError(error, t('auth.login.loginFailed'))
    loginError.value = msg
    if (!isMobile.value) ElMessage.error(msg)
    // 验证码一次性使用，失败后必须换新码
    loadCaptcha()
  } finally {
    loginLoading.value = false
  }
}

// 处理手机号登录
const handlePhoneLogin = () => {
  phoneLoginVisible.value = true
}

// 处理注册
const handleRegister = (event) => {
  if (event) {
    event.preventDefault()
    event.stopPropagation()
  }
  console.log('点击注册按钮')
  // 跳转到注册页面
  router.push('/register').catch(err => {
    // 忽略重复导航错误
    if (err.name !== 'NavigationDuplicated') {
      console.error('路由跳转失败:', err)
    }
  })
}

// 处理忘记密码
const handleForgotPassword = () => {
  console.log('点击忘记密码按钮')
  // 跳转到忘记密码页面
  router.push('/forgot-password')
}

// 测试点击
const testClick = () => {
  console.log('测试按钮被点击')
  ElMessage.success(t('auth.login.testButtonSuccess'))
}

// 隐私政策（与注册页一致：读 base 配置并弹窗）
const handlePrivacyPolicy = () => {
  showPrivacyPolicyAgreement()
}

// 服务条款
const handleTermsOfService = () => {
  showServiceTermsAgreement()
}

// 处理AI智能选择
const handleAISelect = () => {
  showAIDialog.value = true
}

// 处理AI登录成功
const handleLoginSuccess = (data) => {
  console.log('AI登录成功:', data)
  
  // 更新选中的服务
  selectedServices.value = data.services
  
  // 跳转到首页（子路径部署下需带 /admin 前缀，否则会跳到站点根路径）
  setTimeout(() => {
    window.location.href = appUrl('/dashboard')
  }, 100)
  
  ElMessage.success(t('auth.login.registerSuccess'))
}

// 发送手机验证码
const sendPhoneCode = async () => {
  try {
    await phoneFormRef.value.validateField('phone')
    
    // 调用发送验证码API
    const response = await authStore.sendCode(phoneForm.phone, 'login')
    
    if (response.success || response.code === 1) {
      ElMessage.success(t('auth.login.codeSent'))
      
      // 开始倒计时
      codeCountdown.value = 60
      const timer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(response.message || t('auth.login.codeSendFailed'))
    }
  } catch (error) {
    console.error('发送手机验证码失败:', error)
    ElMessage.error(error.message || t('auth.login.codeSendFailedRetry'))
  }
}

// 处理手机号登录提交
const handlePhoneLoginSubmit = async () => {
  try {
    await phoneFormRef.value.validate()
    phoneLoginLoading.value = true
    
    // 调用authStore的手机号登录方法
    const response = await authStore.phoneLogin(phoneForm.phone, phoneForm.code)
    
    if (response.code === 1) {
      ElMessage.success(t('auth.login.loginSuccess'))
      // authStore.phoneLogin已经处理了token和userInfo的存储
      phoneLoginVisible.value = false
      // 跳转到首页
      router.push('/')
    } else {
      ElMessage.error(response.message || t('auth.login.loginFailedCode'))
    }
  } catch (error) {
    console.error('手机号登录失败:', error)
    ElMessage.error(error.message || t('auth.login.loginFailedCode'))
  } finally {
    phoneLoginLoading.value = false
  }
}

</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  position: relative;
}

.language-switch-container {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 1000;
}

.login-wrapper {
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

/* 左侧服务选择区域 */
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

/* AI选择按钮 - 位于左下角 */
.ai-select-button-container {
  position: absolute;
  bottom: 24px;
  left: 24px;
  transform: none;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 0px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
}

.ai-select-button-container:hover {
  background: linear-gradient(135deg, #ffffff 0%, #d7d7d7 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px #ffffff;
}

.ai-select-button {
  /* background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); */
  background: #F4F4F4;
  border: none;
  border-radius: 0px 8px 8px 0px;
  padding: 16px 8px;
  color: #ff6b6b;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
  transition: all 0.3s ease;
  font-size: 15px;
  height: 50px;
  width: 130px;
}

.ai-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px 0px 0px 8px;
  flex-shrink: 0;
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
  margin-bottom: 30px;
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

.services-section {
  margin-bottom: 60px;
}

.services-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: white;
}

.services-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
  justify-content: center;
}

.service-card {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
  backdrop-filter: blur(5px);
  position: relative;
  min-width: 120px;
  flex: 0 0 auto;
  animation: fadeInUp 0.6s ease-out both;
}

.service-card:nth-child(1) { animation-delay: 0.1s; }
.service-card:nth-child(2) { animation-delay: 0.2s; }
.service-card:nth-child(3) { animation-delay: 0.3s; }
.service-card:nth-child(4) { animation-delay: 0.4s; }
.service-card:nth-child(5) { animation-delay: 0.5s; }
.service-card:nth-child(6) { animation-delay: 0.6s; }
.service-card:nth-child(7) { animation-delay: 0.7s; }
.service-card:nth-child(8) { animation-delay: 0.8s; }
.service-card:nth-child(9) { animation-delay: 0.9s; }

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.service-card:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.service-card.selected {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.2);
}

.service-card.more-card {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
  backdrop-filter: blur(5px);
  position: relative;
  min-width: 120px;
  flex: 0 0 auto;
}

.service-card.more-card:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.service-checkbox {
  position: absolute;
  top: 10px;
  left: 10px;
  width: 20px;
  height: 20px;
  border: 2px solid white;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
}

.service-card.selected .service-checkbox {
  background: rgba(255, 255, 255, 0.3);
}

.service-checkbox .el-icon {
  font-size: 14px;
  color: white;
}

.service-icon {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 10px;
}

.service-icon .el-icon {
  font-size: 28px;
  color: white;
}

.service-name {
  font-size: 14px;
  font-weight: 500;
  color: white;
  opacity: 0.9;
  line-height: 1.4;
}

.services-footer {
  position: absolute;
  right: 24px;
  bottom: 24px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  text-align: right;
  margin-top: 0;
  z-index: 3;
}

.services-tip {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  line-height: 1;
}

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 150px;
  height: 150px;
  bottom: -75px;
  left: -75px;
}

.circle-3 {
  width: 100px;
  height: 100px;
  top: 50%;
  right: -50px;
}

/* 更多服务弹窗样式 */
.more-services-dialog {
  border-radius: 16px;
}

.more-services-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.more-services-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 24px;
  margin: 0;
}

.more-services-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.more-services-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 18px;
}

.more-services-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.more-services-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.dialog-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dialog-description {
  text-align: center;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.dialog-description p {
  margin: 0;
}

.services-grid-dialog {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;
}

.service-card-dialog {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid #e1e5e9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
  position: relative;
}

.service-card-dialog:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.service-card-dialog.selected {
  border-color: #667eea;
  background: #f8f9ff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.service-checkbox-dialog {
  width: 20px;
  height: 20px;
  border: 2px solid #e1e5e9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.service-card-dialog.selected .service-checkbox-dialog {
  border-color: #667eea;
  background: #667eea;
}

.service-checkbox-dialog .el-icon {
  font-size: 14px;
  color: white;
}

.service-icon-dialog {
  width: 40px;
  height: 40px;
  background: #f0f0f0;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.service-card-dialog.selected .service-icon-dialog {
  background: #e1f3d8;
}

.service-icon-dialog .el-icon {
  font-size: 20px;
  color: #333;
  transition: all 0.2s ease;
}

.service-card-dialog.selected .service-icon-dialog .el-icon {
  color: #67c23a;
}

.service-name-dialog {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  flex: 1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 500;
}

.confirm-btn {
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.confirm-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.refresh-btn {
  background: linear-gradient(135deg, #07c160 0%, #00a854 100%);
  border: none;
  color: white;
}

.refresh-btn:hover {
  background: linear-gradient(135deg, #06ad56 0%, #00994a 100%);
}

/* 右侧登录表单区域 */
.form-section {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
}

.form-container {
  width: 100%;
  max-width: 400px;
}

.form-header {
  text-align: center;
  margin-bottom: 10px;
}

.form-title {
  font-size: 28px;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.form-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
  font-weight: 400;
}

.login-form {
  width: 100%;
}

.input-label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-input {
  width: 100%;
}

.form-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e1e5e9;
  transition: all 0.2s ease;
  background: #fafbfc;
  box-shadow: none;
}

.form-input :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
  background: white;
}

.form-input :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-icon {
  font-size: 16px;
  color: #667eea;
}

/* 图形验证码行：输入框 + 可点击刷新的验证码图 */
.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.captcha-row .captcha-input {
  flex: 1;
}

.captcha-img {
  height: 44px;
  min-width: 120px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #e4e7ed;
  background: #fff;
  object-fit: contain;
  flex-shrink: 0;
}

.captcha-img--loading {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 20px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
}

.remember-checkbox {
  color: #666;
}

.checkbox-text {
  font-size: 14px;
}

.links-row {
  display: flex;
  gap: 20px;
  align-items: center;
}

.register-link,
.forgot-link {
  font-size: 14px;
  color: #667eea;
  text-decoration: none;
  transition: color 0.2s ease;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  position: relative;
  z-index: 10;
}

.link-icon {
  font-size: 16px;
}

.register-link:hover,
.forgot-link:hover {
  color: #5a6fd8;
}

.login-btn {
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

.login-btn:hover {
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

.login-btn:hover .btn-icon {
  transform: translateX(2px);
}

.divider {
  text-align: center;
  position: relative;
  margin: 30px 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e1e5e9;
}

.divider-text {
  background: white;
  padding: 0 16px;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.alternative-login {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.alt-btn {
  flex: 1;
  height: 44px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  background: white;
  color: #333;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 手机号登录独占一行：第三方入口是圆形图标，挤在一行会被压变形 */
.alt-btn.phone-btn {
  flex: 1 1 100%;
}

/* 第三方登录入口（unilogin 插件）改成 40×40 品牌色圆形图标 */
.alt-btn.unilogin-btn {
  flex: 0 0 auto;
}

.alt-btn:hover {
  border-color: #667eea;
  color: #667eea;
  background: #f8f9ff;
}

.alt-icon {
  font-size: 16px;
}

.alt-text {
  font-size: 14px;
}

.form-footer {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.security-info {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.security-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 12px;
}

.security-icon {
  font-size: 14px;
  color: #667eea;
}

.security-text {
  font-weight: 500;
}

/* 内联错误提示 */
.login-error-banner {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  font-size: 14px;
  line-height: 1.45;
}

.login-error-icon {
  flex-shrink: 0;
  margin-top: 2px;
  font-size: 18px;
}

.login-error-text {
  flex: 1;
}

.login-error-close {
  flex-shrink: 0;
  border: none;
  background: transparent;
  color: #b91c1c;
  font-size: 20px;
  line-height: 1;
  padding: 0 2px;
  cursor: pointer;
  opacity: 0.7;
}

.text-link {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 14px;
  color: #6366f1;
  font-weight: 500;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.text-link:active {
  opacity: 0.75;
}

/* 移动端 App 级布局 */
.login-container--mobile {
  min-height: 100dvh;
  padding: 0;
  align-items: stretch;
  background: linear-gradient(165deg, #eef2ff 0%, #f8fafc 42%, #f1f5f9 100%);
}

.login-container--mobile .language-switch-container {
  top: max(12px, env(safe-area-inset-top));
  right: 14px;
}

.login-container--mobile .login-wrapper {
  flex-direction: column;
  width: 100%;
  max-width: none;
  min-height: 100dvh;
  height: auto;
  border-radius: 0;
  box-shadow: none;
  background: transparent;
  animation: none;
  overflow: visible;
  display: flex;
}

.login-container--mobile .form-section {
  flex: 1;
  order: 1;
  min-height: 0;
  padding: max(16px, env(safe-area-inset-top)) 20px 20px;
  padding-bottom: calc(88px + env(safe-area-inset-bottom, 0px));
  justify-content: flex-start;
  background: transparent;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.login-container--mobile .form-container {
  max-width: 440px;
  margin: 0 auto;
  width: 100%;
}

.login-container--mobile .mobile-hero {
  text-align: center;
  margin-bottom: 22px;
}

.login-container--mobile .mobile-logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.login-container--mobile .mobile-logo-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.28);
}

.login-container--mobile .mobile-logo-icon .el-icon {
  font-size: 22px;
}

.login-container--mobile .mobile-logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.02em;
}

.login-container--mobile .mobile-welcome {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.03em;
}

.login-container--mobile .mobile-sub {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

.login-container--mobile .login-form {
  padding: 22px 18px 18px;
  background: #fff;
  border-radius: 20px;
  box-shadow:
    0 4px 6px rgba(15, 23, 42, 0.04),
    0 16px 40px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.login-container--mobile .login-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.login-container--mobile .form-input :deep(.el-input__wrapper) {
  min-height: 52px;
  padding: 0 14px;
  border-radius: 14px;
  border: 1.5px solid #e2e8f0;
  background: #f8fafc;
  box-shadow: none;
}

.login-container--mobile .form-input :deep(.el-input__wrapper.is-focus) {
  border-color: #6366f1;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.12);
}

.login-container--mobile .form-input :deep(.el-input__inner) {
  font-size: 16px;
  height: 50px;
}

.login-container--mobile .form-options--mobile {
  flex-direction: column;
  align-items: stretch;
  gap: 14px;
  margin: 8px 0 18px;
}

.login-container--mobile .form-options--mobile .links-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.login-container--mobile .login-btn {
  height: 52px;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 600;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 55%, #7c3aed 100%);
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.32);
  border: none;
}

.login-container--mobile .login-btn:active {
  transform: scale(0.98);
}

.login-container--mobile .divider {
  margin: 22px 0 18px;
}

.login-container--mobile .divider-text {
  background: #fff;
  color: #94a3b8;
  font-size: 13px;
}

.login-container--mobile .alternative-login {
  flex-direction: row;
  gap: 10px;
}

.login-container--mobile .alt-btn {
  height: 48px;
  border-radius: 14px;
  border: 1.5px solid #e2e8f0;
  background: #f8fafc;
  font-size: 13px;
}

.login-container--mobile .alt-btn:active {
  background: #f1f5f9;
  transform: scale(0.98);
}

.login-container--mobile .form-footer {
  margin-top: 20px;
  padding-top: 0;
  border-top: none;
}

.login-container--mobile .security-info {
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14px 18px;
}

.login-container--mobile .security-item {
  font-size: 11px;
  color: #94a3b8;
}

.login-container--mobile .mobile-app-tagline {
  margin: 16px 0 0;
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
}

/* 移动端 AI 悬浮入口 */
.ai-fab {
  position: fixed;
  left: 16px;
  bottom: calc(16px + env(safe-area-inset-bottom, 0px));
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 0;
  border-radius: 999px;
  overflow: hidden;
  box-shadow:
    0 4px 12px rgba(79, 70, 229, 0.25),
    0 8px 24px rgba(15, 23, 42, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.6);
  background: #fff;
  -webkit-tap-highlight-color: transparent;
}

.ai-fab:active {
  transform: scale(0.97);
}

.ai-fab-icon {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  object-fit: cover;
}

.ai-fab-btn {
  border: none;
  background: #fff;
  color: #4f46e5;
  font-size: 13px;
  font-weight: 600;
  padding: 0 16px 0 4px;
  height: 44px;
  cursor: pointer;
  white-space: nowrap;
}

.login-container--mobile .login-error-banner {
  margin-bottom: 14px;
  border-radius: 14px;
  font-size: 13px;
}

.phone-dialog--mobile :deep(.el-dialog) {
  border-radius: 0;
  margin: 0;
}

.phone-dialog--mobile :deep(.el-dialog__header) {
  padding-top: max(20px, env(safe-area-inset-top));
}

.phone-dialog--mobile :deep(.el-dialog__body) {
  padding: 16px 20px;
}

.phone-dialog--mobile .form-input :deep(.el-input__wrapper) {
  min-height: 50px;
  border-radius: 14px;
}

/* 手机号登录弹窗 */
.phone-dialog :deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.phone-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 24px;
}

.phone-dialog :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

.phone-form {
  padding: 20px 0;
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
  height: 40px;
  border-radius: 8px;
  font-size: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-weight: 500;
}

.cancel-btn {
  border-radius: 8px;
  border: 1px solid #e1e5e9;
  color: #666;
  font-weight: 500;
}

.confirm-btn {
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .login-wrapper {
    max-width: 1000px;
    height: 650px;
  }
  
  .brand-section,
  .form-section {
    padding: 35px;
  }
  
  .services-grid {
    gap: 8px;
  }
  
  .service-card {
    min-width: 110px;
  }
}

@media (max-width: 992px) {
  .login-wrapper {
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
  
  .services-title {
    font-size: 18px;
  }
  
  .services-grid {
    gap: 8px;
  }
  
  .service-card {
    min-width: 100px;
    padding: 8px;
  }
  
  .service-name {
    font-size: 12px;
  }
  
  .form-title {
    font-size: 24px;
  }
  
  .form-subtitle {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  /* 移动端由 .login-container--mobile 统一处理，此处仅保留非 JS 检测场景的回退 */
  .login-container:not(.login-container--mobile) .login-wrapper {
    flex-direction: column-reverse;
    height: auto;
    min-height: 100dvh;
    max-width: none;
    width: 100%;
    border-radius: 0;
    box-shadow: none;
  }
}

@media (max-width: 576px) {
  .login-container:not(.login-container--mobile) {
    padding: 0;
  }
  
  .login-container:not(.login-container--mobile) .login-wrapper {
    max-width: 100%;
    border-radius: 0;
  }
  
  .login-container:not(.login-container--mobile) .brand-section {
    padding: 16px 14px 20px;
  }
  
  .login-container:not(.login-container--mobile) .brand-name {
    font-size: 18px;
  }
  
  .login-container:not(.login-container--mobile) .brand-tagline {
    font-size: 12px;
  }
  
  .login-container:not(.login-container--mobile) .form-section {
    padding: 20px 16px 12px;
  }
  
  .login-container:not(.login-container--mobile) .form-title {
    font-size: 18px;
  }
  
  .login-container:not(.login-container--mobile) .form-subtitle {
    font-size: 13px;
  }
  
  .login-container:not(.login-container--mobile) .form-input {
    height: 42px;
  }
  
  .login-container:not(.login-container--mobile) .login-btn {
    height: 42px;
    font-size: 15px;
  }

  .login-container--mobile .mobile-welcome {
    font-size: 22px;
  }

  .login-container--mobile .login-form {
    padding: 18px 16px 16px;
    border-radius: 18px;
  }
}

@media (max-width: 480px) {
  .login-container:not(.login-container--mobile) {
    padding: 0;
  }
  
  .login-container:not(.login-container--mobile) .login-wrapper {
    border-radius: 0;
    min-height: 100dvh;
  }
  
  .login-container:not(.login-container--mobile) .brand-section {
    padding: 14px 12px 18px;
  }
  
  .login-container:not(.login-container--mobile) .logo-icon {
    width: 32px;
    height: 32px;
  }
  
  .login-container:not(.login-container--mobile) .logo-icon .el-icon {
    font-size: 16px;
  }
  
  .login-container:not(.login-container--mobile) .brand-name {
    font-size: 16px;
  }
  
  .login-container:not(.login-container--mobile) .form-section {
    padding: 16px 14px 10px;
  }
  
  .login-container:not(.login-container--mobile) .form-title {
    font-size: 16px;
  }
  
  .login-container:not(.login-container--mobile) .form-subtitle {
    font-size: 12px;
  }
  
  .login-container:not(.login-container--mobile) .input-label {
    font-size: 13px;
  }
  
  .login-container:not(.login-container--mobile) .form-input {
    height: 40px;
  }
  
  .login-container:not(.login-container--mobile) .login-btn {
    height: 40px;
    font-size: 14px;
  }
  
  .login-container:not(.login-container--mobile) .phone-login-btn {
    font-size: 12px;
  }

  .login-container--mobile .mobile-welcome {
    font-size: 20px;
  }

  .login-container--mobile .mobile-logo-text {
    font-size: 20px;
  }

  .ai-fab-icon {
    width: 40px;
    height: 40px;
  }

  .ai-fab-btn {
    height: 40px;
    font-size: 12px;
    padding: 0 12px 0 2px;
  }
}

/* 横屏适配 */
@media (max-height: 600px) and (orientation: landscape) {
  .login-wrapper {
    height: 90vh;
    max-height: 500px;
  }
  
  .brand-section {
    padding: 20px;
  }
  
  .brand-header {
    margin-bottom: 30px;
  }
  
  .brand-name {
    font-size: 20px;
  }
  
  .brand-tagline {
    font-size: 12px;
  }
  
  .services-section {
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
  .login-wrapper {
    max-width: 1400px;
    height: 800px;
  }
  
  .brand-name {
    font-size: 36px;
  }
  
  .brand-tagline {
    font-size: 18px;
  }
  
  .services-title {
    font-size: 22px;
  }
  
  .service-card {
    min-width: 140px;
    padding: 12px;
  }
  
  .service-name {
    font-size: 14px;
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
  
  .login-btn {
    height: 52px;
    font-size: 18px;
  }
  
  .alt-btn {
    height: 48px;
  }
  
  .alt-text {
    font-size: 16px;
  }
  
  .ai-select-button {
    height: 55px;
    width: 140px;
    font-size: 16px;
  }
  
  .ai-icon {
    width: 55px;
    height: 55px;
  }
}

/* 平板横屏适配 */
@media (max-width: 1200px) and (orientation: landscape) {
  .services-footer {
    height: 45px;
  }

  .ai-select-button-container {
    bottom: 20px;
    left: 20px;
  }
  
  .ai-select-button {
    height: 45px;
    width: 120px;
    font-size: 14px;
    padding: 12px 6px;
  }
  
  .ai-icon {
    width: 45px;
    height: 45px;
  }
}

/* 手机横屏适配 */
@media (max-width: 992px) and (orientation: landscape) {
  .services-footer {
    height: 40px;
  }

  .ai-select-button-container {
    bottom: 16px;
    left: 16px;
  }
  
  .ai-select-button {
    height: 40px;
    width: 110px;
    font-size: 13px;
    padding: 10px 5px;
  }
  
  .ai-icon {
    width: 40px;
    height: 40px;
  }
}

/* 小屏幕设备适配 - 由 login-container--mobile 统一处理 */
@media (max-width: 768px) {
  .login-container--mobile .mobile-welcome {
    font-size: 22px;
  }

  .login-container--mobile .login-form {
    padding: 18px 16px 16px;
    border-radius: 18px;
  }
}

/* 超小屏幕设备适配（桌面 brand 区域 AI 按钮） */
@media (max-width: 576px) {
  .login-container:not(.login-container--mobile) .services-footer {
    right: 12px;
    bottom: 12px;
    height: 40px;
  }

  .login-container:not(.login-container--mobile) .ai-select-button-container {
    bottom: 12px;
    left: 12px;
  }
  
  .login-container:not(.login-container--mobile) .ai-select-button {
    height: 40px;
    width: 100px;
    font-size: 12px;
    padding: 10px 5px;
  }
  
  .login-container:not(.login-container--mobile) .ai-icon {
    width: 40px;
    height: 40px;
  }
}

/* 极小屏幕设备适配（桌面 brand 区域 AI 按钮） */
@media (max-width: 480px) {
  .login-container:not(.login-container--mobile) .services-footer {
    right: 10px;
    bottom: 10px;
    height: 36px;
  }

  .login-container:not(.login-container--mobile) .services-tip {
    font-size: 12px;
  }

  .login-container:not(.login-container--mobile) .ai-select-button-container {
    bottom: 10px;
    left: 10px;
  }
  
  .login-container:not(.login-container--mobile) .ai-select-button {
    height: 36px;
    width: 90px;
    font-size: 11px;
    padding: 8px 4px;
  }
  
  .login-container:not(.login-container--mobile) .ai-icon {
    width: 36px;
    height: 36px;
  }
}
</style>