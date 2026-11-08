<template>
  <div class="contract-sign-page">
    <!-- 手机验证码登录弹窗 -->
    <el-dialog
      v-model="loginDialogVisible"
      title="请登录"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      :width="isMobile ? '90%' : '400px'"
      :before-close="handleDialogClose"
      class="login-dialog"
    >
      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        :label-width="isMobile ? '80px' : '80px'"
        :label-position="isMobile ? 'top' : 'right'"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="loginForm.phone"
            placeholder="请输入手机号"
            maxlength="11"
            :disabled="codeCountdown > 0"
            :size="isMobile ? 'default' : 'default'"
          />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input-wrapper" :class="{ 'mobile-layout': isMobile }">
            <el-input
              v-model="loginForm.code"
              placeholder="请输入验证码"
              maxlength="6"
              :style="{ flex: 1 }"
              :size="isMobile ? 'default' : 'default'"
            />
            <el-button
              type="primary"
              :disabled="codeCountdown > 0 || !loginForm.phone"
              @click="sendLoginCode"
              :loading="sendingCode"
              :size="isMobile ? 'default' : 'default'"
              :class="{ 'mobile-code-btn': isMobile }"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}秒后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer" :class="{ 'mobile-footer': isMobile }">
          <el-button 
            @click="handleLoginCancel" 
            :disabled="loginLoading"
            :size="isMobile ? 'default' : 'default'"
            :style="isMobile ? { width: '48%' } : {}"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            @click="handleLoginSubmit" 
            :loading="loginLoading"
            :size="isMobile ? 'default' : 'default'"
            :style="isMobile ? { width: '48%' } : {}"
          >
            验证
          </el-button>
        </div>
      </template>
    </el-dialog>

    <div class="sign-container">
      <div class="sign-header">
        <h1>合同签署</h1>
        <p class="subtitle">请仔细阅读合同内容，确认无误后签署</p>
      </div>

      <div v-if="loading" class="loading-wrapper">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="error" class="error-wrapper">
        <el-alert
          :title="error"
          type="error"
          :closable="false"
          show-icon
        />
      </div>

      <!-- 未登录时显示提示 -->
      <div v-else-if="!isAuthenticated" class="auth-required-wrapper">
        <el-result
          icon="warning"
          title="需要身份验证"
          sub-title="请先完成身份验证后查看合同内容"
        />
      </div>

      <div v-else-if="contract" class="sign-content">
        <!-- 合同基本信息 -->
        <el-card class="contract-info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>合同信息</span>
            </div>
          </template>
          <el-descriptions :column="isMobile ? 1 : 2" border>
            <el-descriptions-item label="合同编号">
              {{ contract.contractNumber }}
            </el-descriptions-item>
            <el-descriptions-item label="合同名称">
              {{ contract.contractName }}
            </el-descriptions-item>
            <el-descriptions-item label="客户名称">
              {{ contract.customerName }}
            </el-descriptions-item>
            <el-descriptions-item label="合同金额">
              {{ formatAmount(contract.contractAmount) }}
            </el-descriptions-item>
            <el-descriptions-item label="合同状态">
              <el-tag :type="getStatusType(contract.status)">
                {{ getStatusText(contract.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDate(contract.createTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 合同内容 -->
        <el-card class="contract-content-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>合同内容</span>
            </div>
          </template>
          <div class="contract-content" v-html="contract.contractContent || '暂无合同内容'"></div>
        </el-card>

        <!-- 签署表单 -->
        <el-card class="sign-form-card" shadow="never" v-if="contract.status === 1">
          <template #header>
            <div class="card-header">
              <span>签署信息</span>
            </div>
          </template>
          <el-form
            :model="signForm"
            :rules="signRules"
            ref="signFormRef"
            :label-width="isMobile ? '100px' : '120px'"
            :label-position="isMobile ? 'top' : 'right'"
          >
            <el-form-item label="签署人姓名" prop="signerName">
              <el-input
                v-model="signForm.signerName"
                placeholder="请输入签署人姓名"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="签署人手机号" prop="signerPhone">
              <el-input
                v-model="signForm.signerPhone"
                placeholder="请输入签署人手机号"
                maxlength="11"
                readonly
                style="background-color: #f5f7fa;"
              />
            </el-form-item>
            <el-form-item label="签署人邮箱" prop="signerEmail">
              <el-input
                v-model="signForm.signerEmail"
                placeholder="请输入签署人邮箱（选填）"
                type="email"
              />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="signForm.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息（选填）"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="签名" required>
              <div class="signature-container">
                <canvas
                  ref="signaturePad"
                  :width="isMobile ? 300 : 600"
                  :height="isMobile ? 200 : 300"
                  class="signature-pad"
                  @mousedown="startDrawing"
                  @mousemove="draw"
                  @mouseup="stopDrawing"
                  @mouseleave="stopDrawing"
                  @touchstart="startDrawing"
                  @touchmove="draw"
                  @touchend="stopDrawing"
                ></canvas>
                <div class="signature-actions">
                  <el-button size="small" @click="clearSignature">清除</el-button>
                </div>
              </div>
              <div v-if="signatureError" class="signature-error">请先签名</div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :size="isMobile ? 'default' : 'large'"
                @click="handleSign"
                :loading="signing"
                :style="{ width: isMobile ? '100%' : '200px' }"
              >
                确认签署
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 已签署提示 -->
        <el-card class="signed-card" shadow="never" v-else-if="contract.status === 2 || contract.status === 3">
          <el-result
            icon="success"
            title="合同已签署"
            sub-title="该合同已完成签署并进入执行中，感谢您的配合！"
          />
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { contractService } from '@/api/compat/oaContract'
import { authService } from '@/api/auth/auth'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const contract = ref(null)
const signing = ref(false)
const signFormRef = ref()
const loginFormRef = ref()
const windowWidth = ref(window.innerWidth)
const signaturePad = ref(null)
const signatureError = ref(false)
const isDrawing = ref(false)
const signatureData = ref([])

// Canvas绘图相关
const startDrawing = (e) => {
  e.preventDefault()
  isDrawing.value = true
  const canvas = signaturePad.value
  const ctx = canvas.getContext('2d')
  const rect = canvas.getBoundingClientRect()
  
  let x, y
  if (e.type === 'touchstart') {
    x = e.touches[0].clientX - rect.left
    y = e.touches[0].clientY - rect.top
  } else {
    x = e.clientX - rect.left
    y = e.clientY - rect.top
  }
  
  ctx.beginPath()
  ctx.moveTo(x, y)
  signatureData.value.push({ x, y, type: 'start' })
}

const draw = (e) => {
  if (!isDrawing.value) return
  e.preventDefault()
  
  const canvas = signaturePad.value
  const ctx = canvas.getContext('2d')
  const rect = canvas.getBoundingClientRect()
  
  let x, y
  if (e.type === 'touchmove') {
    x = e.touches[0].clientX - rect.left
    y = e.touches[0].clientY - rect.top
  } else {
    x = e.clientX - rect.left
    y = e.clientY - rect.top
  }
  
  ctx.lineWidth = 2
  ctx.lineCap = 'round'
  ctx.strokeStyle = '#000000'
  ctx.lineTo(x, y)
  ctx.stroke()
  
  signatureData.value.push({ x, y, type: 'draw' })
}

const stopDrawing = () => {
  isDrawing.value = false
}

const clearSignature = () => {
  const canvas = signaturePad.value
  if (canvas) {
    const ctx = canvas.getContext('2d')
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    signatureData.value = []
    signatureError.value = false
  }
}

const validateSignature = () => {
  if (signatureData.value.length === 0) {
    signatureError.value = true
    ElMessage.warning('请先签名')
    return false
  }
  signatureError.value = false
  return true
}

// 身份验证相关
const isAuthenticated = ref(false)
const loginDialogVisible = ref(true) // 默认显示登录弹窗
const loginLoading = ref(false)
const sendingCode = ref(false)
const codeCountdown = ref(0)
let codeTimer = null

// 从 localStorage 恢复验证状态
const getStoredAuthState = () => {
  try {
    // 从路由参数获取 contractId
    const contractId = route.params.id || route.query.contractId
    if (!contractId) return null
    
    const stored = localStorage.getItem(`contract_sign_auth_${contractId}`)
    if (stored) {
      const authData = JSON.parse(stored)
      // 检查是否过期（24小时内有效）
      const now = Date.now()
      if (now - authData.timestamp < 24 * 60 * 60 * 1000) {
        return authData
      } else {
        // 过期了，清除
        localStorage.removeItem(`contract_sign_auth_${contractId}`)
      }
    }
  } catch (e) {
    console.error('读取验证状态失败:', e)
  }
  return null
}

// 登录表单
const loginForm = reactive({
  phone: '',
  code: ''
})

// 登录表单验证规则
const loginRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '请输入6位数字验证码', trigger: 'blur' }
  ]
}

// 判断是否为移动端
const isMobile = computed(() => windowWidth.value <= 768)

// 监听窗口大小变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

// 签署表单
const signForm = reactive({
  signerName: '',
  signerPhone: '',
  signerEmail: '',
  remark: ''
})

// 表单验证规则
const signRules = {
  signerName: [
    { required: true, message: '请输入签署人姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  signerPhone: [
    { required: true, message: '请输入签署人手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  signerEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 获取合同信息
const getContractInfo = async () => {
  try {
    loading.value = true
    error.value = ''
    
    const contractId = route.params.id || route.query.contractId
    if (!contractId) {
      error.value = '缺少合同ID，无法访问签署页面'
      return
    }
    
    // 获取登录时使用的手机号
    const loginPhone = loginForm.phone
    if (!loginPhone) {
      error.value = '缺少登录手机号，请重新登录'
      return
    }
    
    // 通过合同ID和手机号获取合同信息（后端会验证手机号）
    const response = await contractService.getContractForSign(contractId, loginPhone)
    
    if (response.code === 1 || response.code === 200) {
      contract.value = response.data
      
      if (contract.value.status === 2 || contract.value.status === 3) {
        ElMessage.info('该合同已完成签署')
      }
    } else {
      error.value = response.message || '获取合同信息失败'
    }
  } catch (err) {
    console.error('获取合同信息失败:', err)
    // 优先显示后端返回的错误信息
    // 注意：当 code: 0 时，request.js 会 reject，但 err.response.data 可能包含完整的响应
    let errorMessage = '获取合同信息失败，请检查链接是否正确'
    
    if (err.response && err.response.data) {
      // 后端返回了响应数据
      if (err.response.data.message) {
        errorMessage = err.response.data.message
      } else if (err.response.data.data && err.response.data.data.message) {
        errorMessage = err.response.data.data.message
      }
    } else if (err.message) {
      // 使用 reject 时传入的 message
      errorMessage = err.message
    }
    
    error.value = errorMessage
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// 发送登录验证码（合同签署专用）
const sendLoginCode = async () => {
  try {
    await loginFormRef.value.validateField('phone')
    
    sendingCode.value = true
    // 使用 'contract_sign' 类型，避免注册场景的手机号检查
    const response = await authService.sendCode(loginForm.phone, 'contract_sign')
    
    if (response.code === 1 || response.code === 200) {
      ElMessage.success('验证码已发送，请查收')
      
      // 开始倒计时
      codeCountdown.value = 60
      if (codeTimer) {
        clearInterval(codeTimer)
      }
      codeTimer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
          clearInterval(codeTimer)
          codeTimer = null
        }
      }, 1000)
    } else {
      ElMessage.error(response.message || '发送验证码失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.message || '发送验证码失败，请重试')
  } finally {
    sendingCode.value = false
  }
}

// 提交登录验证
const handleLoginSubmit = async () => {
  try {
    await loginFormRef.value.validate()
    
    loginLoading.value = true
    
    // 直接验证验证码和手机号是否匹配合同
    const contractId = route.params.id || route.query.contractId
    if (!contractId) {
      ElMessage.error('缺少合同ID')
      return
    }
    
    // 调用后端接口验证：1.验证码是否正确  2.手机号是否与合同服务商手机号匹配
    const response = await contractService.verifyContractPhone(contractId, loginForm.phone, loginForm.code)
    
    if (response.code === 1 || response.code === 200) {
      ElMessage.success('身份验证成功')
      isAuthenticated.value = true
      loginDialogVisible.value = false
      
      // 保存验证状态到localStorage（24小时内有效）
      try {
        const authData = {
          contractId: contractId,
          phone: loginForm.phone,
          timestamp: Date.now()
        }
        localStorage.setItem(`contract_sign_auth_${contractId}`, JSON.stringify(authData))
      } catch (e) {
        console.error('保存验证状态失败:', e)
      }
      
      // 自动填充手机号到签署表单
      signForm.signerPhone = loginForm.phone
      // 验证成功后获取合同信息
      await getContractInfo()
    } else {
      ElMessage.error(response.message || '验证失败，请检查验证码或手机号是否正确')
      // 验证失败时不清空表单，让用户可以重新输入
      // 不跳转，保持在当前页面
    }
  } catch (error) {
    console.error('身份验证失败:', error)
    
    // 处理HTTP错误响应
    if (error.response) {
      const status = error.response.status
      const errorData = error.response.data
      
      // 如果是401/403，说明验证失败，但不应该跳转到登录页
      if (status === 401 || status === 403) {
        ElMessage.error(errorData?.message || errorData?.data?.message || '验证失败，请检查验证码或手机号是否正确')
        // 不跳转，保持在当前页面
        return
      }
      
      // 其他错误
      ElMessage.error(errorData?.message || errorData?.data?.message || '验证失败，请重试')
    } else {
      // 网络错误或其他错误
      ElMessage.error(error.message || '验证失败，请重试')
    }
    
    // 确保不会跳转到登录页
    // 验证失败时应该保持在当前页面，让用户可以重新输入
  } finally {
    loginLoading.value = false
  }
}

// 取消登录
const handleLoginCancel = () => {
  ElMessageBox.confirm(
    '取消验证后将无法查看合同内容，确定要取消吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 可以跳转到首页或关闭页面
    router.push('/')
  }).catch(() => {
    // 用户取消，不做任何操作
  })
}

// 阻止关闭弹窗
const handleDialogClose = (done) => {
  ElMessageBox.confirm(
    '需要完成身份验证才能查看合同，确定要关闭吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    router.push('/')
  }).catch(() => {
    // 用户取消，不关闭弹窗
  })
}

// 确认签署
const handleSign = async () => {
  try {
    // 验证表单（排除signature字段）
    await signFormRef.value.validate()
    
    // 验证签名
    if (!validateSignature()) {
      return
    }
    
    await ElMessageBox.confirm(
      '确认签署后，合同状态将变更为「执行中」，是否继续？',
      '确认签署',
      {
        confirmButtonText: '确认签署',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    signing.value = true
    
    // 获取签名图片
    const canvas = signaturePad.value
    if (!canvas) {
      ElMessage.error('签名板初始化失败')
      return
    }
    
    const signatureBase64 = canvas.toDataURL('image/png')
    console.log('Signature base64:', signatureBase64.substring(0, 50) + '...')
    
    const contractId = route.params.id || route.query.contractId
    const response = await contractService.signContract(contractId, {
      signerName: signForm.signerName,
      signerPhone: signForm.signerPhone,
      signerEmail: signForm.signerEmail,
      remark: signForm.remark,
      signature: signatureBase64
    })
    
    if (response.code === 1 || response.code === 200) {
      ElMessage.success('签署成功！')
      await getContractInfo()
    } else {
      ElMessage.error(response.message || '签署失败')
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('签署失败:', err)
      ElMessage.error(err.message || '签署失败')
    }
  } finally {
    signing.value = false
  }
}

// 格式化金额
const formatAmount = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount || 0)
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  if (typeof date === 'string') {
    const dateObj = new Date(date)
    if (isNaN(dateObj.getTime())) {
      return date
    }
    return dateObj.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  }
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',     // 草稿
    1: 'warning',  // 待签署
    2: 'success',  // 已签署
    3: 'primary',  // 执行中
    4: 'success',  // 已完结
    5: 'danger',   // 已终止
    6: 'warning'   // 已中止
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '草稿',
    1: '待签署',
    2: '已签署',
    3: '执行中',
    4: '已完结',
    5: '已终止',
    6: '已中止'
  }
  return textMap[status] || '未知'
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  
  // 检查是否有保存的验证状态
  const storedAuth = getStoredAuthState()
  if (storedAuth) {
    // 恢复验证状态
    isAuthenticated.value = true
    loginDialogVisible.value = false
    // 恢复手机号到表单
    signForm.signerPhone = storedAuth.phone
    loginForm.phone = storedAuth.phone
    // 自动获取合同信息
    getContractInfo()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  // 清理倒计时定时器
  if (codeTimer) {
    clearInterval(codeTimer)
    codeTimer = null
  }
})
</script>

<style scoped>
.contract-sign-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
}

.sign-container {
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
  position: relative;
}

.sign-header {
  text-align: center;
  color: white;
  margin-bottom: 30px;
}

.sign-header h1 {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
  word-break: break-word;
}

.sign-header .subtitle {
  font-size: 16px;
  opacity: 0.9;
  word-break: break-word;
}

.loading-wrapper,
.error-wrapper {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.loading-wrapper .el-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 20px;
}

.sign-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.contract-info-card,
.contract-content-card,
.sign-form-card,
.signed-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: visible;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.contract-content {
  min-height: 200px;
  padding: 20px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: break-word;
  font-size: 14px;
}

.contract-content :deep(p) {
  margin-bottom: 10px;
  word-break: break-word;
}

.contract-content :deep(h1),
.contract-content :deep(h2),
.contract-content :deep(h3) {
  margin-top: 20px;
  margin-bottom: 10px;
  font-weight: bold;
  word-break: break-word;
}

.contract-content :deep(img) {
  max-width: 100%;
  height: auto;
}

.sign-form-card {
  margin-top: 20px;
}

.auth-required-wrapper {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 40px 20px;
  text-align: center;
}

.code-input-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 10px;
}

.code-input-wrapper :deep(.el-input) {
  flex: 1;
}

.code-input-wrapper.mobile-layout {
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
}

.code-input-wrapper.mobile-layout .mobile-code-btn {
  width: 100%;
  margin-left: 0 !important;
  min-width: auto !important;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.dialog-footer.mobile-footer {
  flex-direction: row;
  justify-content: space-between;
  width: 100%;
}

/* 签名板样式 */
.signature-container {
  width: 100%;
}

.signature-pad {
  border: 2px solid #dcdfe6;
  border-radius: 4px;
  background-color: #ffffff;
  cursor: crosshair;
  touch-action: none;
}

.signature-pad canvas {
  display: block;
  width: 100% !important;
  height: auto !important;
}

.signature-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.signature-error {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

/* 移动端样式 */
@media (max-width: 768px) {
  .contract-sign-page {
    padding: 15px 10px;
  }
  
  .sign-header {
    margin-bottom: 20px;
  }
  
  .sign-header h1 {
    font-size: 22px;
    margin-bottom: 8px;
  }
  
  .sign-header .subtitle {
    font-size: 13px;
    padding: 0 10px;
  }
  
  .sign-content {
    gap: 15px;
  }
  
  .contract-info-card,
  .contract-content-card,
  .sign-form-card,
  .signed-card {
    border-radius: 6px;
  }
  
  .card-header {
    font-size: 16px;
  }
  
  .contract-content {
    padding: 15px;
    font-size: 13px;
    min-height: 150px;
  }
  
  .loading-wrapper,
  .error-wrapper {
    padding: 40px 15px;
  }
  
  .loading-wrapper .el-icon {
    font-size: 36px;
  }
  
  /* 弹窗移动端优化 */
  :deep(.login-dialog .el-dialog) {
    max-width: 90vw;
  }
  
  :deep(.login-dialog .el-dialog__body) {
    padding: 15px;
  }
  
  :deep(.login-dialog .el-dialog__header) {
    padding: 15px 15px 10px;
  }
  
  :deep(.login-dialog .el-dialog__title) {
    font-size: 18px;
  }
  
  :deep(.login-dialog .el-dialog__footer) {
    padding: 10px 15px 15px;
  }
  
  :deep(.login-dialog .el-form-item__label) {
    font-size: 14px;
    margin-bottom: 8px;
    padding-bottom: 0;
  }
  
  :deep(.login-dialog .el-input__inner) {
    font-size: 14px;
    height: 40px;
  }
  
  :deep(.login-dialog .el-button) {
    font-size: 14px;
    padding: 10px 20px;
  }
  
  /* Element Plus 组件移动端优化 */
  :deep(.el-descriptions) {
    font-size: 13px;
  }
  
  :deep(.el-descriptions__label) {
    font-size: 13px;
    padding: 8px 10px;
  }
  
  :deep(.el-descriptions__content) {
    font-size: 13px;
    padding: 8px 10px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 14px;
    margin-bottom: 5px;
  }
  
  :deep(.el-input__inner),
  :deep(.el-textarea__inner) {
    font-size: 14px;
  }
  
  :deep(.el-button) {
    font-size: 14px;
  }
  
  /* 签名板移动端优化 */
  .signature-actions {
    flex-direction: row;
    justify-content: space-between;
  }
  
  .signature-actions .el-button {
    flex: 1;
    margin: 0;
  }
}

/* 超小屏幕（手机竖屏） */
@media (max-width: 480px) {
  .contract-sign-page {
    padding: 10px 8px;
  }
  
  .sign-header h1 {
    font-size: 20px;
  }
  
  .sign-header .subtitle {
    font-size: 12px;
  }
  
  .card-header {
    font-size: 15px;
  }
  
  .contract-content {
    padding: 12px;
    font-size: 12px;
  }
  
  /* 弹窗超小屏幕优化 */
  :deep(.login-dialog .el-dialog) {
    max-width: 95vw;
  }
  
  :deep(.login-dialog .el-dialog__body) {
    padding: 12px;
  }
  
  :deep(.login-dialog .el-dialog__header) {
    padding: 12px 12px 8px;
  }
  
  :deep(.login-dialog .el-dialog__title) {
    font-size: 16px;
  }
  
  :deep(.login-dialog .el-form-item__label) {
    font-size: 13px;
  }
  
  :deep(.login-dialog .el-input__inner) {
    font-size: 14px;
    height: 38px;
  }
  
  :deep(.login-dialog .el-button) {
    font-size: 13px;
    padding: 8px 16px;
  }
  
  :deep(.el-descriptions) {
    font-size: 12px;
  }
  
  :deep(.el-descriptions__label),
  :deep(.el-descriptions__content) {
    font-size: 12px;
    padding: 6px 8px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 13px;
  }
  
  :deep(.el-input__inner),
  :deep(.el-textarea__inner) {
    font-size: 13px;
  }
}

/* 平板样式 */
@media (min-width: 769px) and (max-width: 1024px) {
  .contract-sign-page {
    padding: 30px 15px;
  }
  
  .sign-header h1 {
    font-size: 28px;
  }
  
  .sign-header .subtitle {
    font-size: 15px;
  }
}

/* 大屏幕优化 */
@media (min-width: 1200px) {
  .sign-container {
    max-width: 1200px;
  }
}
</style>
