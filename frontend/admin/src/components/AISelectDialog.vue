<template>
  <el-dialog
    v-model="visible"
    :title="t('auth.login.aiDialogTitle')"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="ai-select-dialog"
  >
    <!-- Step 1: 行业选择 -->
    <div v-if="currentStep === 1" class="step-content">
      <div class="step-header">
        <h3>{{ t('auth.login.aiStep1Title') }}</h3>
        <p>{{ t('auth.login.aiStep1Desc') }}</p>
      </div>
      
      <div class="industry-grid">
        <div 
          v-for="industry in industries" 
          :key="industry.value"
          class="industry-card"
          :class="{ 'selected': selectedIndustry === industry.value }"
          @click="selectIndustry(industry.value)"
        >
          <div class="industry-icon">
            <el-icon><component :is="industry.icon" /></el-icon>
          </div>
          <div class="industry-name">{{ industry.name }}</div>
        </div>
      </div>
    </div>

    <!-- Step 2: 服务推荐 -->
    <div v-if="currentStep === 2" class="step-content">
      <div class="step-header">
        <h3>{{ t('auth.login.aiStep2Title') }}</h3>
        <p>{{ t('auth.login.aiStep2Desc', { industry: getIndustryName(selectedIndustry) }) }}</p>
      </div>
      
      <div class="recommended-services">
        <div 
          v-for="service in recommendedServices" 
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
          <div class="service-info">
            <div class="service-name">{{ service.name }}</div>
            <div class="service-desc">{{ service.description }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Step 3: 手机登录 -->
    <div v-if="currentStep === 3" class="step-content">
      <div class="step-header">
        <h3>{{ t('auth.login.aiStep3Title') }}</h3>
        <p>{{ t('auth.login.aiStep3Desc') }}</p>
      </div>
      
      <el-form 
        ref="phoneFormRef" 
        :model="phoneForm" 
        :rules="phoneRules"
        class="phone-form"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="phoneForm.phone"
            :placeholder="t('auth.login.phoneRequired')"
            size="large"
            :prefix-icon="Iphone"
          />
        </el-form-item>
        
        <el-form-item prop="code">
          <div class="code-input-group">
            <el-input
              v-model="phoneForm.code"
              :placeholder="t('auth.login.codeRequired')"
              size="large"
              :prefix-icon="Key"
            />
            <el-button 
              :disabled="codeCountdown > 0"
              @click="sendCode"
              class="code-button"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : t('auth.login.sendCode') }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- Step 4: 登录成功 -->
    <div v-if="currentStep === 4" class="step-content">
      <div class="success-content">
        <div class="success-icon">
          <el-icon><Check /></el-icon>
        </div>
        <h3>{{ t('auth.login.aiStep4Title') }}</h3>
        <p>{{ t('auth.login.aiStep4Desc') }}</p>
        <div class="loading-spinner">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>
      </div>
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button 
          v-if="currentStep > 1" 
          @click="prevStep"
          :disabled="loading"
        >
          {{ t('auth.login.aiPrevStep') }}
        </el-button>
        
        <el-button 
          v-if="currentStep < 4"
          type="primary" 
          @click="nextStep"
          :disabled="!canProceed"
          :loading="loading"
        >
          {{ currentStep === 3 ? t('auth.login.aiLogin') : t('auth.login.aiNextStep') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
// 导入认证服务
import { authService } from '@/api/auth/auth.js'
import { 
  Check, 
  Iphone, 
  Key, 
  Loading,
  OfficeBuilding,
  ShoppingBag,
  Monitor,
  Document,
  User,
  Setting,
  DataBoard,
  Goods,
  List,
  Wallet
} from '@element-plus/icons-vue'

// 国际化 - 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'loginSuccess'])

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const currentStep = ref(1)
const selectedIndustry = ref('')
const selectedServices = ref([])
const loading = ref(false)
const codeCountdown = ref(0)

// 行业数据
const industries = computed(() => [
  { value: 'technology', name: t('auth.login.aiIndustryTechnology'), icon: 'Monitor' },
  { value: 'finance', name: t('auth.login.aiIndustryFinance'), icon: 'Wallet' },
  { value: 'education', name: t('auth.login.aiIndustryEducation'), icon: 'Document' },
  { value: 'healthcare', name: t('auth.login.aiIndustryHealthcare'), icon: 'User' },
  { value: 'retail', name: t('auth.login.aiIndustryRetail'), icon: 'ShoppingBag' },
  { value: 'manufacturing', name: t('auth.login.aiIndustryManufacturing'), icon: 'Setting' },
  { value: 'realestate', name: t('auth.login.aiIndustryRealestate'), icon: 'OfficeBuilding' },
  { value: 'other', name: t('auth.login.aiIndustryOther'), icon: 'DataBoard' }
])

// 服务数据
const allServices = computed(() => [
  { key: 'dashboard', name: t('auth.login.aiServiceDashboard'), icon: 'DataBoard', description: t('auth.login.aiServiceDashboardDesc') },
  { key: 'oa', name: t('auth.login.aiServiceOA'), icon: 'Document', description: t('auth.login.aiServiceOADesc') },
  { key: 'goods', name: t('auth.login.aiServiceGoods'), icon: 'Goods', description: t('auth.login.aiServiceGoodsDesc') },
  { key: 'order', name: t('auth.login.aiServiceOrder'), icon: 'List', description: t('auth.login.aiServiceOrderDesc') },
  { key: 'payment', name: t('auth.login.aiServicePayment'), icon: 'Wallet', description: t('auth.login.aiServicePaymentDesc') },
  { key: 'user', name: t('auth.login.aiServiceUser'), icon: 'User', description: t('auth.login.aiServiceUserDesc') },
  { key: 'system', name: t('auth.login.aiServiceSystem'), icon: 'Setting', description: t('auth.login.aiServiceSystemDesc') }
])

// 推荐服务
const recommendedServices = computed(() => {
  const recommendations = {
    technology: ['dashboard', 'oa', 'system'],
    finance: ['dashboard', 'payment', 'user'],
    education: ['oa', 'user', 'system'],
    healthcare: ['user', 'oa', 'dashboard'],
    retail: ['goods', 'order', 'payment', 'dashboard'],
    manufacturing: ['goods', 'order', 'system'],
    realestate: ['oa', 'user', 'dashboard'],
    other: ['dashboard', 'oa', 'user']
  }
  
  const serviceKeys = recommendations[selectedIndustry.value] || ['dashboard', 'oa']
  return allServices.value.filter(service => serviceKeys.includes(service.key))
})

// 手机登录表单
const phoneForm = ref({
  phone: '',
  code: ''
})

const phoneFormRef = ref(null)

const phoneRules = computed(() => ({
  phone: [
    { required: true, message: t('auth.login.phoneRequired'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('auth.login.phoneInvalid'), trigger: 'blur' }
  ],
  code: [
    { required: true, message: t('auth.login.codeRequired'), trigger: 'blur' },
    { len: 6, message: t('auth.login.codeInvalid'), trigger: 'blur' }
  ]
}))

// 计算属性
const canProceed = computed(() => {
  switch (currentStep.value) {
    case 1:
      return selectedIndustry.value !== ''
    case 2:
      return selectedServices.value.length > 0
    case 3:
      return phoneForm.value.phone && phoneForm.value.code
    default:
      return true
  }
})

// 方法
const selectIndustry = (industry) => {
  selectedIndustry.value = industry
}

const toggleService = (serviceKey) => {
  const index = selectedServices.value.indexOf(serviceKey)
  if (index > -1) {
    selectedServices.value.splice(index, 1)
  } else {
    selectedServices.value.push(serviceKey)
  }
}

const getIndustryName = (value) => {
  const industry = industries.value.find(item => item.value === value)
  return industry ? industry.name : ''
}

const nextStep = async () => {
  if (currentStep.value === 3) {
    // 执行登录
    await handleLogin()
  } else {
    currentStep.value++
  }
}

const prevStep = () => {
  currentStep.value--
}

const sendCode = async () => {
  try {
    await phoneFormRef.value.validateField('phone')
    loading.value = true
    
    // 调用真实的发送验证码API
    const response = await authService.sendCode(phoneForm.value.phone, 'login')
    
    if (response.code === 200) {
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
    console.error('发送验证码失败:', error)
    ElMessage.error(error.message || t('auth.login.codeSendFailed'))
  } finally {
    loading.value = false
  }
}

const handleLogin = async () => {
  try {
    await phoneFormRef.value.validate()
    loading.value = true
    
    // 调用真实的手机号登录API
    const response = await authService.phoneLogin(phoneForm.value.phone, phoneForm.value.code)
    
    if (response.code === 200) {
      ElMessage.success(t('auth.login.loginSuccess'))
      currentStep.value = 4
      
      // 模拟跳转
      setTimeout(() => {
        emit('loginSuccess', {
          industry: selectedIndustry.value,
          services: selectedServices.value,
          phone: phoneForm.value.phone
        })
        visible.value = false
      }, 2000)
    } else {
      ElMessage.error(response.message || t('auth.login.loginFailedCode'))
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || t('auth.login.loginFailedCode'))
  } finally {
    loading.value = false
  }
}

// 监听对话框关闭，重置状态
watch(visible, (newVal) => {
  if (!newVal) {
    // 重置所有状态
    currentStep.value = 1
    selectedIndustry.value = ''
    selectedServices.value = []
    phoneForm.value = { phone: '', code: '' }
    codeCountdown.value = 0
    loading.value = false
  }
})
</script>

<style scoped>
.ai-select-dialog {
  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px 24px;
    margin: 0;
  }
  
  :deep(.el-dialog__title) {
    color: white;
    font-weight: 600;
  }
  
  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: white;
  }
}

.step-content {
  padding: 24px;
  min-height: 400px;
}

.step-header {
  text-align: center;
  margin-bottom: 32px;
}

.step-header h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 8px;
}

.step-header p {
  color: #666;
  font-size: 14px;
}

/* 行业选择 */
.industry-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.industry-card {
  padding: 20px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.industry-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
}

.industry-card.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.industry-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.industry-name {
  font-size: 14px;
  font-weight: 500;
}

/* 服务推荐 */
.recommended-services {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.service-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.service-card:hover {
  border-color: #667eea;
}

.service-card.selected {
  border-color: #667eea;
  background: #f0f2ff;
}

.service-checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 4px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-card.selected .service-checkbox {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.service-icon {
  font-size: 20px;
  margin-right: 12px;
  color: #667eea;
}

.service-info {
  flex: 1;
}

.service-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.service-desc {
  font-size: 12px;
  color: #666;
}

/* 手机登录 */
.phone-form {
  max-width: 400px;
  margin: 0 auto;
}

.code-input-group {
  display: flex;
  gap: 12px;
}

.code-input-group .el-input {
  flex: 1;
}

.code-button {
  width: 120px;
}

/* 成功页面 */
.success-content {
  text-align: center;
  padding: 40px 0;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  font-size: 32px;
  color: white;
}

.success-content h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 12px;
}

.success-content p {
  color: #666;
  margin-bottom: 24px;
}

.loading-spinner {
  font-size: 20px;
  color: #667eea;
}

/* 底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  padding: 16px 24px;
  border-top: 1px solid #e4e7ed;
}

/* 响应式 */
@media (max-width: 768px) {
  .industry-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .step-content {
    padding: 16px;
  }
}
</style>