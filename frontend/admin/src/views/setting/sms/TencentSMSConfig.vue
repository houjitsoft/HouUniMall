<template>
  <div class="tencent-sms-config">
    <el-form :model="config" label-width="120px" class="config-form">
      <el-form-item :label="t('setting.sms.common.enabled')">
        <el-switch v-model="config.enabled" />
        <span class="form-tip">{{ t('setting.sms.tencent.enableTip') }}</span>
      </el-form-item>
      
      <el-form-item label="SecretId" required>
        <el-input 
          v-model="config.secretId" 
          :placeholder="t('setting.sms.tencent.secretIdPlaceholder')"
          show-password
        />
        <span class="form-tip">{{ t('setting.sms.tencent.secretIdTip') }}</span>
      </el-form-item>
      
      <el-form-item label="SecretKey" required>
        <el-input 
          v-model="config.secretKey" 
          :placeholder="t('setting.sms.tencent.secretKeyPlaceholder')"
          show-password
        />
        <span class="form-tip">{{ t('setting.sms.tencent.secretKeyTip') }}</span>
      </el-form-item>
      
      <el-form-item :label="t('setting.sms.tencent.appId')" required>
        <el-input 
          v-model="config.appId" 
          :placeholder="t('setting.sms.tencent.appIdPlaceholder')"
        />
        <span class="form-tip">{{ t('setting.sms.tencent.appIdTip') }}</span>
      </el-form-item>
      
      <el-form-item :label="t('setting.sms.common.signature')" required>
        <el-input 
          v-model="config.signName" 
          :placeholder="t('setting.sms.common.signaturePlaceholder')"
        />
        <span class="form-tip">{{ t('setting.sms.common.signatureTip') }}</span>
      </el-form-item>
      
      <el-form-item :label="t('setting.sms.common.templateId')" required>
        <el-input 
          v-model="config.templateId" 
          :placeholder="t('setting.sms.common.templateIdPlaceholder')"
        />
        <span class="form-tip">{{ t('setting.sms.common.templateIdTip') }}</span>
      </el-form-item>
      
      <el-form-item :label="t('setting.sms.common.region')">
        <el-select v-model="config.region" :placeholder="t('setting.sms.common.regionPlaceholder')">
          <el-option :label="t('setting.sms.tencent.regionBeijing')" value="ap-beijing" />
          <el-option :label="t('setting.sms.tencent.regionShanghai')" value="ap-shanghai" />
          <el-option :label="t('setting.sms.tencent.regionGuangzhou')" value="ap-guangzhou" />
          <el-option :label="t('setting.sms.tencent.regionChengdu')" value="ap-chengdu" />
          <el-option :label="t('setting.sms.tencent.regionChongqing')" value="ap-chongqing" />
          <el-option :label="t('setting.sms.tencent.regionShenzhen')" value="ap-shenzhen-fsi" />
        </el-select>
        <span class="form-tip">{{ t('setting.sms.common.regionTip') }}</span>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="testConnection" :loading="testing">
          {{ t('setting.sms.common.testConnection') }}
        </el-button>
        <el-button type="success" @click="testSend" :loading="sending">
          {{ t('setting.sms.common.sendTestSms') }}
        </el-button>
      </el-form-item>
    </el-form>
    
    <!-- 配置说明 -->
    <el-card class="config-help" style="margin-top: 20px;">
      <template #header>
        <span>{{ t('setting.sms.common.configGuide') }}</span>
      </template>
      <div class="help-content">
        <h4>{{ t('setting.sms.tencent.guideTitle') }}</h4>
        <ol>
          <li>{{ t('setting.sms.tencent.step1Login') }} <a href="https://console.cloud.tencent.com/sms" target="_blank">{{ t('setting.sms.tencent.console') }}</a></li>
          <li>{{ t('setting.sms.tencent.step2') }}</li>
          <li>{{ t('setting.sms.tencent.step3') }}</li>
          <li>{{ t('setting.sms.common.stepSignature') }}</li>
          <li>{{ t('setting.sms.common.stepTemplate') }}</li>
        </ol>
        
        <h4>{{ t('setting.sms.common.notes') }}</h4>
        <ul>
          <li>{{ t('setting.sms.common.noteSignatureTemplate') }}</li>
          <li>{{ t('setting.sms.common.noteBalance') }}</li>
          <li>{{ t('setting.sms.common.noteTestEnv') }}</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'test'])

const config = ref({ ...props.modelValue })
const testing = ref(false)
const sending = ref(false)

// 监听配置变化
watch(config, (newVal) => {
  emit('update:modelValue', newVal)
}, { deep: true })

// 测试连接
const testConnection = async () => {
  if (!config.value.secretId || !config.value.secretKey) {
    ElMessage.warning(t('setting.sms.tencent.pleaseFill'))
    return
  }
  
  try {
    testing.value = true
    
    // 这里应该调用后端API测试连接
    console.log('测试腾讯云连接:', config.value)
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success(t('setting.sms.tencent.testSuccess'))
  } catch (error) {
    console.error('测试连接失败:', error)
    ElMessage.error(t('setting.sms.common.testFailed') + error.message)
  } finally {
    testing.value = false
  }
}

// 发送测试短信
const testSend = async () => {
  if (!config.value.enabled) {
    ElMessage.warning(t('setting.sms.tencent.pleaseEnable'))
    return
  }
  
  emit('test')
}
</script>

<style scoped>
.tencent-sms-config {
  padding: 20px;
}

.config-form {
  max-width: 600px;
}

.form-tip {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}

.config-help {
  background-color: #f8f9fa;
}

.help-content h4 {
  color: #409eff;
  margin-bottom: 10px;
}

.help-content ol,
.help-content ul {
  margin-left: 20px;
}

.help-content li {
  margin-bottom: 5px;
  line-height: 1.5;
}

.help-content a {
  color: #409eff;
  text-decoration: none;
}

.help-content a:hover {
  text-decoration: underline;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input) {
  max-width: 400px;
}

:deep(.el-select) {
  max-width: 400px;
}
</style>
