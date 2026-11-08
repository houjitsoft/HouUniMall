<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
    <!-- 步骤1：填写手机号 -->
    <template v-if="step === 1">
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="form.phone"
          placeholder="请输入手机号"
          maxlength="11"
          size="large"
        >
          <template #prefix>
            <el-icon><Phone /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="设置密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请设置登录密码（6-20位）"
          show-password
          size="large"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请再次输入密码"
          show-password
          size="large"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item prop="agreeTerms">
        <AgreementChecklist ref="agreementRef" scene="register" />
      </el-form-item>

      <el-button
        type="primary"
        size="large"
        style="width: 100%"
        :loading="loading"
        @click="sendCode"
      >
        获取验证码
      </el-button>
    </template>

    <!-- 步骤2：验证验证码 -->
    <template v-else>
      <div class="verify-tip">
        <el-icon><Message /></el-icon>
        <span>验证码已发送至 <strong>{{ form.phone }}</strong></span>
      </div>

      <el-form-item label="验证码" prop="code">
        <div class="code-input-row">
          <el-input
            v-model="form.code"
            placeholder="请输入6位验证码"
            maxlength="6"
            size="large"
            @input="autoSubmit"
          />
          <el-button
            :disabled="resendDisabled"
            size="large"
            @click="resendCode"
          >
            {{ resendText }}
          </el-button>
        </div>
      </el-form-item>

      <el-button
        type="primary"
        size="large"
        style="width: 100%; margin-top: 8px"
        :loading="loading"
        @click="handleRegister"
      >
        立即注册
      </el-button>

      <el-button
        size="large"
        style="width: 100%; margin-top: 12px"
        @click="step = 1"
      >
        返回修改
      </el-button>
    </template>
  </el-form>
</template>

<script setup>
/**
 * 手机号注册组件
 * - 步骤1：填写手机号、密码，发送验证码
 * - 步骤2：输入验证码，完成注册
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, Lock, Message } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import AgreementChecklist from '@/components/AgreementChecklist.vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const agreementRef = ref(null)
const loading = ref(false)
const step = ref(1)
const resendDisabled = ref(false)
const resendText = ref('重新发送')

const form = reactive({
  phone: '',
  password: '',
  confirmPassword: '',
  code: '',
  agreeTerms: false
})

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const validateAgree = (rule, value, callback) => {
  // 必选协议（隐私政策 / 用户服务协议 / 账号与内容合规使用承诺书）须全部勾选
  const result = agreementRef.value?.validate()
  if (result && !result.ok) {
    callback(new Error(result.message))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置登录密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位', trigger: 'blur' }
  ],
  agreeTerms: [
    { validator: validateAgree, trigger: 'change' }
  ]
}

/**
 * 发送验证码
 */
const sendCode = async () => {
  if (!formRef.value) return
  await formRef.value.validateField(['phone', 'password', 'confirmPassword', 'agreeTerms'])
    .catch(() => { throw new Error('validate') })

  try {
    loading.value = true
    const response = await authStore.sendCode(form.phone)
    if (response && (response.code === 1 || response.code === 200)) {
      ElMessage.success('验证码已发送，请查收短信')
      step.value = 2
      startResendCountdown()
    } else {
      ElMessage.error(response?.message || '验证码发送失败，请稍后重试')
    }
  } catch (err) {
    if (err?.message !== 'validate') {
      ElMessage.error(err?.message || '验证码发送失败')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 重新发送验证码
 */
const resendCode = async () => {
  try {
    const response = await authStore.sendCode(form.phone)
    if (response && (response.code === 1 || response.code === 200)) {
      ElMessage.success('验证码已重新发送')
      startResendCountdown()
    } else {
      ElMessage.error(response?.message || '发送失败，请稍后重试')
    }
  } catch (err) {
    ElMessage.error(err?.message || '发送失败')
  }
}

/**
 * 倒计时60秒
 */
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

/**
 * 验证码输入满6位自动提交
 */
const autoSubmit = (val) => {
  if (val.length === 6) handleRegister()
}

/**
 * 执行注册
 */
const handleRegister = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validateField(['code'])
    loading.value = true
    const consents = agreementRef.value?.getConsents()
    const payload = {
      userMobile: form.phone,
      phoneCode: form.code,
      password: form.password,
      registerTerminal: 'houcloud',
      agreementsSource: consents ? 'api' : 'fallback'
    }
    if (consents) {
      payload.agreements = consents
    }
    const response = await authStore.register(payload)
    if (response && (response.code === 1 || response.code === 200)) {
      ElMessage.success('注册成功！正在跳转登录...')
      setTimeout(() => router.push('/login'), 1500)
    } else {
      ElMessage.error(response?.message || '注册失败，请重试')
    }
  } catch (err) {
    ElMessage.error(err?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
.verify-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f0f9ff;
  border-radius: 8px;
  margin-bottom: 20px;
  color: #0369a1;
  font-size: 14px;
}

.code-input-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input-row .el-input {
  flex: 1;
}

.code-input-row .el-button {
  white-space: nowrap;
  min-width: 110px;
}
</style>
