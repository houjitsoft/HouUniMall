<template>
  <div class="security-password">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>密码安全设置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" class="security-tabs">
        <!-- 密码策略选项卡 -->
        <el-tab-pane label="密码策略" name="policy">
          <el-form :model="passwordForm" :rules="rules" ref="passwordFormRef" label-width="120px">
        <el-form-item label="最小密码长度" prop="minLength">
          <el-input-number v-model="passwordForm.minLength" :min="6" :max="32" />
          <span class="form-tip">密码最小长度，建议不少于8位</span>
        </el-form-item>
        
        <el-form-item label="密码复杂度" prop="complexity">
          <el-checkbox-group v-model="passwordForm.complexity">
            <el-checkbox label="uppercase">必须包含大写字母</el-checkbox>
            <el-checkbox label="lowercase">必须包含小写字母</el-checkbox>
            <el-checkbox label="number">必须包含数字</el-checkbox>
            <el-checkbox label="special">必须包含特殊字符</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="密码有效期" prop="expireDays">
          <el-input-number v-model="passwordForm.expireDays" :min="1" :max="365" />
          <span class="form-tip">密码过期天数，0表示永不过期</span>
        </el-form-item>
        
        <el-form-item label="重复密码限制" prop="historyCount">
          <el-input-number v-model="passwordForm.historyCount" :min="0" :max="20" />
          <span class="form-tip">不能与前几次密码相同，0表示无限制</span>
        </el-form-item>
        
        <el-form-item label="登录失败锁定" prop="lockThreshold">
          <el-input-number v-model="passwordForm.lockThreshold" :min="0" :max="10" />
          <span class="form-tip">连续登录失败次数，0表示不锁定</span>
        </el-form-item>
        
        <el-form-item label="锁定时间" prop="lockDuration">
          <el-input-number v-model="passwordForm.lockDuration" :min="1" :max="1440" />
          <span class="form-tip">账户锁定时间（分钟）</span>
        </el-form-item>
        
           <el-form-item>
              <el-button type="primary" @click="saveSettings">保存设置</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- RSA密钥配置选项卡 -->
        <el-tab-pane label="RSA密钥配置" name="rsa">
          <el-form :model="rsaForm" :rules="rsaRules" ref="rsaFormRef" label-width="120px">
            <el-alert
              title="密钥配置说明"
              type="info"
              :closable="false"
              style="margin-bottom: 20px"
            >
              <template #default>
                <p><strong>RSA密钥用于前后端通信时的密码加密传输，确保密码在网络传输过程中的安全性。</strong></p>
                <p style="margin-top: 10px;">• <strong>公钥</strong>：用于前端加密密码，存储在 <code>frontend/admin/src/utils/crypto.js</code> 文件中</p>
                <p>• <strong>私钥</strong>：用于后端解密密码，存储在 <code>backend/paas/auth/private_key.pem</code> 文件中</p>
                <p style="margin-top: 10px;">• 密钥在数据库中持久化存储，配置键为 <code>rsa_public_key</code> 和 <code>rsa_private_key</code></p>
                <p style="color: #E6A23C; margin-top: 10px;">⚠️ <strong>修改密钥后的操作步骤：</strong></p>
                <p style="margin-left: 20px;">1. 点击“保存密钥配置”按钮，将密钥保存到数据库</p>
                <p style="margin-left: 20px;">2. 重启 <code>auth</code> 服务，加载新的私钥文件</p>
                <p style="margin-left: 20px;">3. 更新前端 <code>crypto.js</code> 文件中的公钥常量</p>
                <p style="margin-left: 20px;">4. 重新编译和部署前端应用</p>
              </template>
            </el-alert>
            
            <el-form-item label="公钥" prop="publicKey">
              <el-input
                v-model="rsaForm.publicKey"
                type="textarea"
                :rows="8"
                placeholder="请输入RSA公钥（PEM格式，以-----BEGIN PUBLIC KEY-----开头）"
              />
              <span class="form-tip">前端使用，用于加密密码</span>
            </el-form-item>
            
            <el-form-item label="私钥" prop="privateKey">
              <el-input
                v-model="rsaForm.privateKey"
                type="textarea"
                :rows="8"
                placeholder="请输入RSA私钥（PEM格式，支持PKCS#1和PKCS#8格式）"
                show-password
              />
              <span class="form-tip">后端使用，用于解密密码（敏感信息，请妥善保管）</span>
            </el-form-item>
            
            <el-form-item label="密钥长度">
              <el-tag type="info">{{ rsaKeyLength }}</el-tag>
              <span class="form-tip">建议使用2048位或4096位密钥</span>
            </el-form-item>
            
            <el-divider content-position="left">当前配置</el-divider>
            
            <el-form-item label="公钥状态">
              <el-tag :type="currentPublicKeyStatus.type">{{ currentPublicKeyStatus.text }}</el-tag>
              <span v-if="rsaForm.publicKey" class="form-tip">已加载 {{ rsaForm.publicKey.length }} 个字符</span>
            </el-form-item>
            
            <el-form-item label="私钥状态">
              <el-tag :type="currentPrivateKeyStatus.type">{{ currentPrivateKeyStatus.text }}</el-tag>
              <span v-if="rsaForm.privateKey" class="form-tip">已加载 {{ rsaForm.privateKey.length }} 个字符</span>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveRSAKeys" :loading="saving">
                <el-icon><Upload /></el-icon>
                保存密钥配置
              </el-button>
              <el-button @click="generateRSAKeys" :loading="generating">
                <el-icon><Refresh /></el-icon>
                自动生成密钥对
              </el-button>
              <el-button @click="testRSAEncryption" :loading="testing" type="success">
                <el-icon><CircleCheck /></el-icon>
                测试加密解密
              </el-button>
              <el-button @click="resetRSAForm">
                <el-icon><RefreshLeft /></el-icon>
                重置
              </el-button>
            </el-form-item>
            
             <el-divider content-position="left">操作提示</el-divider>
            
            <el-steps :active="2" finish-status="success" align-center>
              <el-step title="生成密钥对" description="点击'自动生成密钥对'按钮" />
              <el-step title="保存配置" description="点击'保存密钥配置'按钮" />
              <el-step title="重启服务" description="重启auth和前端服务" />
              <el-step title="测试验证" description="点击'测试加密解密'验证" />
            </el-steps>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, CircleCheck, Upload, RefreshLeft } from '@element-plus/icons-vue'
import { getRSAKeys, saveRSAKeys as saveRSAKeysAPI, generateRSAKeys as apiGenerateRSAKeys, testRSAEncryption as apiTestRSAEncryption } from '@/api/setting/rsa'

const passwordFormRef = ref()
const rsaFormRef = ref()
const activeTab = ref('policy')
const saving = ref(false)
const generating = ref(false)
const testing = ref(false)

const passwordForm = reactive({
  minLength: 8,
  complexity: ['uppercase', 'lowercase', 'number'],
  expireDays: 90,
  historyCount: 5,
  lockThreshold: 5,
  lockDuration: 30
})

const rsaForm = reactive({
  publicKey: '',
  privateKey: ''
})

const rules = {
  minLength: [
    { required: true, message: '请输入最小密码长度', trigger: 'blur' }
  ],
  expireDays: [
    { required: true, message: '请输入密码有效期', trigger: 'blur' }
  ],
  historyCount: [
    { required: true, message: '请输入重复密码限制', trigger: 'blur' }
  ],
  lockThreshold: [
    { required: true, message: '请输入登录失败锁定次数', trigger: 'blur' }
  ],
  lockDuration: [
    { required: true, message: '请输入锁定时间', trigger: 'blur' }
  ]
}

const rsaRules = {
  publicKey: [
    { required: true, message: '请输入RSA公钥', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value.includes('BEGIN PUBLIC KEY')) {
          callback(new Error('公钥格式错误，应以-----BEGIN PUBLIC KEY-----开头'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  privateKey: [
    { required: true, message: '请输入RSA私钥', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value.includes('BEGIN') || !value.includes('PRIVATE KEY')) {
          callback(new Error('私钥格式错误，应包含BEGIN PRIVATE KEY或BEGIN RSA PRIVATE KEY'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 计算密钥长度
const rsaKeyLength = computed(() => {
  if (!rsaForm.publicKey) return '未设置'
  
  try {
    // 提取 PEM 格式中的 Base64 部分
    const base64Content = rsaForm.publicKey
      .replace(/-----BEGIN PUBLIC KEY-----/g, '')
      .replace(/-----END PUBLIC KEY-----/g, '')
      .replace(/\s+/g, '')
    
    if (!base64Content) return '无法识别'
    
    // 解码 Base64 计算字节长度
    // Base64 每 4 个字符代表3个字节，去除 padding
    const paddingCount = (base64Content.match(/=/g) || []).length
    const byteLength = (base64Content.length * 3 / 4) - paddingCount
    
    // RSA 公钥 DER 编码格式中，实际模数长度需要减去头部开销
    // 对于 2048 位密钥，公钥 DER 编码后约 294 字节
    // 对于 4096 位密钥，公钥 DER 编码后约 550 字节
    let bits = 0
    if (byteLength >= 540) {
      bits = 4096
    } else if (byteLength >= 280) {
      bits = 2048
    } else if (byteLength >= 140) {
      bits = 1024
    } else {
      bits = 512
    }
    
    return `${bits} 位`
  } catch (error) {
    console.error('计算密钥长度失败:', error)
    return '无法识别'
  }
})

// 当前公钥状态
const currentPublicKeyStatus = computed(() => {
  if (rsaForm.publicKey) {
    return { type: 'success', text: '已配置' }
  }
  return { type: 'warning', text: '未配置' }
})

// 当前私钥状态
const currentPrivateKeyStatus = computed(() => {
  if (rsaForm.privateKey) {
    return { type: 'success', text: '已配置' }
  }
  return { type: 'warning', text: '未配置' }
})

const saveSettings = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    ElMessage.success('密码安全设置保存成功')
  } catch (error) {
    ElMessage.error('表单验证失败，请检查输入')
  }
}

const resetForm = () => {
  passwordFormRef.value?.resetFields()
}

// 保存RSA密钥配置
const saveRSAKeys = async () => {
  if (!rsaFormRef.value) return
  
  try {
    await rsaFormRef.value.validate()
    
    await ElMessageBox.confirm(
      '保存密钥配置后，需要重启auth服务才能生效。是否继续？',
      '确认保存',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    saving.value = true
    
    // 调用后端API保存密钥配置
    const response = await saveRSAKeysAPI({
      publicKey: rsaForm.publicKey,
      privateKey: rsaForm.privateKey
    })
    
    if (response.code === 1) {
      ElMessage.success(response.message || 'RSA密钥配置保存成功，请重启auth服务')
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('保存失败：' + (error.message || '未知错误'))
    }
  } finally {
    saving.value = false
  }
}

// 生成RSA密钥对
const generateRSAKeys = async () => {
  try {
    await ElMessageBox.confirm(
      '将自动生成新的RSA密钥对，这将覆盖当前配置。是否继续？',
      '确认生成',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    generating.value = true
    
    // 调用后端API生成密钥对
    const response = await apiGenerateRSAKeys(2048)
        
    if (response.code === 1 && response.data) {
      rsaForm.publicKey = response.data.publicKey
      rsaForm.privateKey = response.data.privateKey
      ElMessage.success('RSA密钥对生成成功，请点击“保存密钥配置”按钮保存')
    } else {
      ElMessage.error(response.message || '生成失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('生成失败：' + (error.message || '未知错误'))
    }
  } finally {
    generating.value = false
  }
}

// 测试RSA加密解密
const testRSAEncryption = async () => {
  if (!rsaForm.publicKey || !rsaForm.privateKey) {
    ElMessage.warning('请先配置公钥和私钥')
    return
  }
  
  try {
    testing.value = true
    
    // 调用后端API测试加密解密
    const response = await apiTestRSAEncryption({
      publicKey: rsaForm.publicKey,
      privateKey: rsaForm.privateKey
    })
    
    if (response.code === 1 && response.data) {
      const result = response.data
      const status = result.overallStatus === '成功' ? 'success' : 'error'
      
      ElMessageBox.alert(
        `测试结果：\n` +
        `${result.publicKeyFormat === '正确' ? '✓' : '✗'} 公钥格式: ${result.publicKeyFormat}\n` +
        `${result.privateKeyFormat === '正确' ? '✓' : '✗'} 私钥格式: ${result.privateKeyFormat}\n` +
        `${result.encryptionTest === '通过' ? '✓' : '✗'} 加密测试: ${result.encryptionTest}\n` +
        `${result.decryptionTest === '通过' ? '✓' : '✗'} 解密测试: ${result.decryptionTest}\n` +
        `${result.dataMatch === '通过' ? '✓' : '✗'} 数据匹配: ${result.dataMatch}\n` +
        `\n总体状态: ${result.overallStatus}`,
        `测试${status === 'success' ? '成功' : '失败'}`,
        {
          confirmButtonText: '确定',
          type: status
        }
      )
    } else {
      ElMessage.error(response.message || '测试失败')
    }
  } catch (error) {
    ElMessage.error('测试失败：' + (error.message || '未知错误'))
  } finally {
    testing.value = false
  }
}

// 重置RSA表单
const resetRSAForm = () => {
  rsaFormRef.value?.resetFields()
}

// 加载当前密钥配置
const loadRSAKeys = async () => {
  try {
    // 从后端加载当前密钥配置
    const response = await getRSAKeys()
    
    if (response.code === 1 && response.data) {
      rsaForm.publicKey = response.data.publicKey || ''
      rsaForm.privateKey = response.data.privateKey || ''
    }
  } catch (error) {
    console.error('加载密钥配置失败:', error)
  }
}

onMounted(() => {
  loadRSAKeys()
})
</script>

<style scoped>
.security-password {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 15px;
  color: #909399;
  font-size: 12px;
}

.security-tabs {
  margin-top: 20px;
}

.security-tabs :deep(.el-tabs__content) {
  padding-top: 20px;
}

.security-tabs :deep(.el-form-item) {
  margin-bottom: 24px;
}

.security-tabs :deep(.el-textarea__inner) {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
}

.security-tabs :deep(.el-divider__text) {
  font-weight: 600;
  color: #606266;
}

.security-tabs :deep(.el-steps) {
  margin-top: 20px;
}
</style>