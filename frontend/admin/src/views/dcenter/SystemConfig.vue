<template>
  <div class="system-config">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 基础配置 -->
      <el-tab-pane label="基础配置" name="basic">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>系统基础配置</span>
              <el-button type="primary" @click="saveBasicConfig">保存配置</el-button>
            </div>
          </template>
          
          <el-form :model="basicConfig" :rules="basicRules" ref="basicFormRef" label-width="150px">
            <el-form-item label="系统名称" prop="systemName">
              <el-input v-model="basicConfig.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            <el-form-item label="系统版本" prop="version">
              <el-input v-model="basicConfig.version" placeholder="请输入系统版本" />
            </el-form-item>
            <el-form-item label="公司名称" prop="companyName">
              <el-input v-model="basicConfig.companyName" placeholder="请输入公司名称" />
            </el-form-item>
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="basicConfig.phone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="联系邮箱" prop="email">
              <el-input v-model="basicConfig.email" placeholder="请输入联系邮箱" />
            </el-form-item>
            <el-form-item label="系统Logo" prop="logo">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :on-success="handleLogoSuccess"
                :before-upload="beforeLogoUpload"
              >
                <img v-if="basicConfig.logo" :src="basicConfig.logo" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="系统描述" prop="description">
              <el-input type="textarea" v-model="basicConfig.description" :rows="4" placeholder="请输入系统描述" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 邮件配置 -->
      <el-tab-pane label="邮件配置" name="email">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>邮件服务配置</span>
              <el-button type="primary" @click="saveEmailConfig">保存配置</el-button>
              <el-button type="success" @click="testEmail">测试邮件</el-button>
            </div>
          </template>
          
          <el-form :model="emailConfig" :rules="emailRules" ref="emailFormRef" label-width="150px">
            <el-form-item label="SMTP服务器" prop="smtpServer">
              <el-input v-model="emailConfig.smtpServer" placeholder="请输入SMTP服务器地址" />
            </el-form-item>
            <el-form-item label="SMTP端口" prop="smtpPort">
              <el-input-number v-model="emailConfig.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="邮箱账号" prop="emailAccount">
              <el-input v-model="emailConfig.emailAccount" placeholder="请输入邮箱账号" />
            </el-form-item>
            <el-form-item label="邮箱密码" prop="emailPassword">
              <el-input v-model="emailConfig.emailPassword" type="password" placeholder="请输入邮箱密码" />
            </el-form-item>
            <el-form-item label="发件人名称" prop="senderName">
              <el-input v-model="emailConfig.senderName" placeholder="请输入发件人名称" />
            </el-form-item>
            <el-form-item label="启用SSL" prop="enableSSL">
              <el-switch v-model="emailConfig.enableSSL" />
            </el-form-item>
            <el-form-item label="测试收件人" prop="testReceiver">
              <el-input v-model="emailConfig.testReceiver" placeholder="请输入测试收件人邮箱" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 短信配置 -->
      <el-tab-pane label="短信配置" name="sms">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>短信服务配置</span>
              <el-button type="primary" @click="saveSmsConfig">保存配置</el-button>
              <el-button type="success" @click="testSms">测试短信</el-button>
            </div>
          </template>
          
          <el-form :model="smsConfig" :rules="smsRules" ref="smsFormRef" label-width="150px">
            <el-form-item label="短信服务商" prop="provider">
              <el-select v-model="smsConfig.provider" placeholder="请选择短信服务商">
                <el-option label="阿里云" value="aliyun" />
                <el-option label="腾讯云" value="tencent" />
                <el-option label="华为云" value="huawei" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item label="AccessKey" prop="accessKey">
              <el-input v-model="smsConfig.accessKey" placeholder="请输入AccessKey" />
            </el-form-item>
            <el-form-item label="SecretKey" prop="secretKey">
              <el-input v-model="smsConfig.secretKey" type="password" placeholder="请输入SecretKey" />
            </el-form-item>
            <el-form-item label="签名" prop="signature">
              <el-input v-model="smsConfig.signature" placeholder="请输入短信签名" />
            </el-form-item>
            <el-form-item label="模板ID" prop="templateId">
              <el-input v-model="smsConfig.templateId" placeholder="请输入短信模板ID" />
            </el-form-item>
            <el-form-item label="测试手机号" prop="testPhone">
              <el-input v-model="smsConfig.testPhone" placeholder="请输入测试手机号" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 支付配置 -->
      <el-tab-pane label="支付配置" name="payment">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>支付服务配置</span>
              <el-button type="primary" @click="savePaymentConfig">保存配置</el-button>
            </div>
          </template>
          
          <el-form :model="paymentConfig" :rules="paymentRules" ref="paymentFormRef" label-width="150px">
            <el-form-item label="微信支付" prop="wechatEnabled">
              <el-switch v-model="paymentConfig.wechatEnabled" />
            </el-form-item>
            <el-form-item label="微信AppID" prop="wechatAppId" v-if="paymentConfig.wechatEnabled">
              <el-input v-model="paymentConfig.wechatAppId" placeholder="请输入微信AppID" />
            </el-form-item>
            <el-form-item label="微信商户号" prop="wechatMchId" v-if="paymentConfig.wechatEnabled">
              <el-input v-model="paymentConfig.wechatMchId" placeholder="请输入微信商户号" />
            </el-form-item>
            <el-form-item label="微信密钥" prop="wechatKey" v-if="paymentConfig.wechatEnabled">
              <el-input v-model="paymentConfig.wechatKey" type="password" placeholder="请输入微信密钥" />
            </el-form-item>
            
            <el-divider />
            
            <el-form-item label="支付宝" prop="alipayEnabled">
              <el-switch v-model="paymentConfig.alipayEnabled" />
            </el-form-item>
            <el-form-item label="支付宝AppID" prop="alipayAppId" v-if="paymentConfig.alipayEnabled">
              <el-input v-model="paymentConfig.alipayAppId" placeholder="请输入支付宝AppID" />
            </el-form-item>
            <el-form-item label="支付宝私钥" prop="alipayPrivateKey" v-if="paymentConfig.alipayEnabled">
              <el-input type="textarea" v-model="paymentConfig.alipayPrivateKey" :rows="4" placeholder="请输入支付宝私钥" />
            </el-form-item>
            <el-form-item label="支付宝公钥" prop="alipayPublicKey" v-if="paymentConfig.alipayEnabled">
              <el-input type="textarea" v-model="paymentConfig.alipayPublicKey" :rows="4" placeholder="请输入支付宝公钥" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 响应式数据
const activeTab = ref('basic')
const basicFormRef = ref(null)
const emailFormRef = ref(null)
const smsFormRef = ref(null)
const paymentFormRef = ref(null)

// 基础配置
const basicConfig = reactive({
  systemName: 'HouEOC管理系统',
  version: '1.0.0',
  companyName: 'HouEOC科技有限公司',
  phone: '400-123-4567',
  email: 'support@houeoc.com',
  logo: '',
  description: 'HouEOC是一套现代化的企业管理系统，提供完整的业务流程管理解决方案。'
})

// 邮件配置
const emailConfig = reactive({
  smtpServer: 'smtp.qq.com',
  smtpPort: 587,
  emailAccount: 'support@houeoc.com',
  emailPassword: '',
  senderName: 'HouEOC系统',
  enableSSL: true,
  testReceiver: ''
})

// 短信配置
const smsConfig = reactive({
  provider: 'aliyun',
  accessKey: '',
  secretKey: '',
  signature: 'HouEOC',
  templateId: '',
  testPhone: ''
})

// 支付配置
const paymentConfig = reactive({
  wechatEnabled: true,
  wechatAppId: '',
  wechatMchId: '',
  wechatKey: '',
  alipayEnabled: true,
  alipayAppId: '',
  alipayPrivateKey: '',
  alipayPublicKey: ''
})

// 表单验证规则
const basicRules = {
  systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  version: [{ required: true, message: '请输入系统版本', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }]
}

const emailRules = {
  smtpServer: [{ required: true, message: '请输入SMTP服务器', trigger: 'blur' }],
  smtpPort: [{ required: true, message: '请输入SMTP端口', trigger: 'blur' }],
  emailAccount: [{ required: true, message: '请输入邮箱账号', trigger: 'blur' }],
  emailPassword: [{ required: true, message: '请输入邮箱密码', trigger: 'blur' }]
}

const smsRules = {
  provider: [{ required: true, message: '请选择短信服务商', trigger: 'change' }],
  accessKey: [{ required: true, message: '请输入AccessKey', trigger: 'blur' }],
  secretKey: [{ required: true, message: '请输入SecretKey', trigger: 'blur' }]
}

const paymentRules = {
  wechatAppId: [{ required: true, message: '请输入微信AppID', trigger: 'blur' }],
  alipayAppId: [{ required: true, message: '请输入支付宝AppID', trigger: 'blur' }]
}

// Logo上传
const handleLogoSuccess = (response, file) => {
  basicConfig.logo = URL.createObjectURL(file.raw)
}

const beforeLogoUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传Logo只能是JPG/PNG格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传Logo大小不能超过2MB!')
  }
  return isJPG && isLt2M
}

// 保存基础配置
const saveBasicConfig = async () => {
  if (!basicFormRef.value) return
  
  try {
    await basicFormRef.value.validate()
    ElMessage.success('基础配置保存成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 保存邮件配置
const saveEmailConfig = async () => {
  if (!emailFormRef.value) return
  
  try {
    await emailFormRef.value.validate()
    ElMessage.success('邮件配置保存成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 保存短信配置
const saveSmsConfig = async () => {
  if (!smsFormRef.value) return
  
  try {
    await smsFormRef.value.validate()
    ElMessage.success('短信配置保存成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 保存支付配置
const savePaymentConfig = async () => {
  if (!paymentFormRef.value) return
  
  try {
    await paymentFormRef.value.validate()
    ElMessage.success('支付配置保存成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 测试邮件
const testEmail = () => {
  if (!emailConfig.testReceiver) {
    ElMessage.warning('请先输入测试收件人邮箱')
    return
  }
  ElMessage.info('测试邮件发送中...')
}

// 测试短信
const testSms = () => {
  if (!smsConfig.testPhone) {
    ElMessage.warning('请先输入测试手机号')
    return
  }
  ElMessage.info('测试短信发送中...')
}
</script>

<style scoped>
.system-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 6px;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}
</style>
