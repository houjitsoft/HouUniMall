<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
    <!-- 步骤1：填写企业信息 -->
    <template v-if="step === 1">
      <el-form-item label="联系人手机号" prop="phone">
        <el-input
          v-model="form.phone"
          placeholder="请输入联系人手机号"
          maxlength="11"
          size="large"
        >
          <template #prefix><el-icon><Phone /></el-icon></template>
        </el-input>
      </el-form-item>

      <el-form-item label="企业名称" prop="companyName">
        <el-input
          v-model="form.companyName"
          placeholder="请输入企业全称"
          size="large"
        >
          <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
        </el-input>
      </el-form-item>

      <el-form-item label="统一社会信用代码" prop="taxId">
        <el-input
          v-model="form.taxId"
          placeholder="请输入18位统一社会信用代码"
          maxlength="18"
          size="large"
        />
      </el-form-item>

      <el-form-item label="营业执照编号" prop="businessLicense">
        <el-input
          v-model="form.businessLicense"
          placeholder="请输入营业执照编号（选填）"
          size="large"
        />
      </el-form-item>

      <el-form-item label="登录密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请设置登录密码（6-20位）"
          show-password
          size="large"
        >
          <template #prefix><el-icon><Lock /></el-icon></template>
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
          <template #prefix><el-icon><Lock /></el-icon></template>
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

    <!-- 步骤2：验证手机验证码 -->
    <template v-else>
      <div class="verify-tip">
        <el-icon><Message /></el-icon>
        <span>验证码已发送至 <strong>{{ form.phone }}</strong></span>
      </div>

      <div class="company-summary">
        <div class="summary-item">
          <span class="label">企业名称：</span>
          <span class="value">{{ form.companyName }}</span>
        </div>
        <div class="summary-item">
          <span class="label">信用代码：</span>
          <span class="value">{{ form.taxId }}</span>
        </div>
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
        提交注册申请
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
 * 企业注册组件
 * - 步骤1：填写企业信息、联系人手机号、密码
 * - 步骤2：输入手机验证码，完成企业注册
 * 后端接口：/web/auth/register (businessegInsert)
 * 参数：userMobile, phoneCode, password, companyName, taxId, businessLicense
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, Lock, Message, OfficeBuilding } from '@element-plus/icons-vue'
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
  companyName: '',
  taxId: '',
  businessLicense: '',
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
    { required: true, message: '请输入联系人手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  companyName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' },
    { min: 2, max: 50, message: '企业名称长度为 2-50 位', trigger: 'blur' }
  ],
  taxId: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { len: 18, message: '统一社会信用代码为18位', trigger: 'blur' }
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
  agreeTerms: [{ validator: validateAgree, trigger: 'change' }]
}

/**
 * 发送手机验证码
 */
const sendCode = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validateField([
      'phone', 'companyName', 'taxId', 'password', 'confirmPassword', 'agreeTerms'
    ])
  } catch {
    return
  }
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
    ElMessage.error(err?.message || '验证码发送失败')
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
 * 执行企业注册
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
      companyName: form.companyName,
      taxId: form.taxId,
      businessLicense: form.businessLicense || '',
      registerTerminal: 'houcloud',
      agreementsSource: consents ? 'api' : 'fallback'
    }
    if (consents) {
      payload.agreements = consents
    }
    const response = await authStore.register(payload)
    if (response && (response.code === 1 || response.code === 200)) {
      ElMessage.success('企业注册申请已提交，请等待审核！')
      setTimeout(() => router.push('/login'), 2000)
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
  margin-bottom: 16px;
  color: #0369a1;
  font-size: 14px;
}

.company-summary {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
}

.summary-item {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 14px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-item .label {
  color: #64748b;
  min-width: 90px;
}

.summary-item .value {
  color: #1e293b;
  font-weight: 500;
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
