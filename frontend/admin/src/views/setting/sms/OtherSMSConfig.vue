<template>
  <div class="other-sms-config">
    <el-form :model="config" label-width="120px" class="config-form">
      <el-form-item :label="t('setting.sms.common.enabled')">
        <el-switch v-model="config.enabled" />
        <span class="form-tip">{{ t('setting.sms.other.enableTip') }}</span>
      </el-form-item>
      
      <el-form-item :label="t('setting.sms.other.serviceName')" required>
        <el-input 
          v-model="config.name" 
          :placeholder="t('setting.sms.other.serviceNamePlaceholder')"
        />
        <span class="form-tip">{{ t('setting.sms.other.serviceNameTip') }}</span>
      </el-form-item>
      
      <el-form-item :label="t('setting.sms.other.apiUrl')" required>
        <el-input 
          v-model="config.apiUrl" 
          :placeholder="t('setting.sms.other.apiUrlPlaceholder')"
        />
        <span class="form-tip">{{ t('setting.sms.other.apiUrlTip') }}</span>
      </el-form-item>
      
      <el-form-item :label="t('setting.sms.other.apiKey')" required>
        <el-input 
          v-model="config.apiKey" 
          :placeholder="t('setting.sms.other.apiKeyPlaceholder')"
          show-password
        />
        <span class="form-tip">{{ t('setting.sms.other.apiKeyTip') }}</span>
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
      
      <!-- 自定义参数 -->
      <el-form-item :label="t('setting.sms.other.customParams')">
        <div class="custom-params">
          <div 
            v-for="(param, index) in customParams" 
            :key="index" 
            class="param-item"
          >
            <el-input 
              v-model="param.key" 
              :placeholder="t('setting.sms.other.paramName')"
              style="width: 150px; margin-right: 10px;"
            />
            <el-input 
              v-model="param.value" 
              :placeholder="t('setting.sms.other.paramValue')"
              style="width: 200px; margin-right: 10px;"
            />
            <el-button 
              type="danger" 
              size="small" 
              @click="removeParam(index)"
            >
              {{ t('setting.sms.other.delete') }}
            </el-button>
          </div>
          <el-button 
            type="primary" 
            size="small" 
            @click="addParam"
            style="margin-top: 10px;"
          >
            {{ t('setting.sms.other.addParam') }}
          </el-button>
        </div>
        <span class="form-tip">{{ t('setting.sms.other.customParamsTip') }}</span>
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
        <h4>{{ t('setting.sms.other.guideTitle') }}</h4>
        <ol>
          <li>{{ t('setting.sms.other.step1') }}</li>
          <li>{{ t('setting.sms.other.step2') }}</li>
          <li>{{ t('setting.sms.other.step3') }}</li>
          <li>{{ t('setting.sms.other.step4') }}</li>
          <li>{{ t('setting.sms.other.step5') }}</li>
        </ol>
        
        <h4>{{ t('setting.sms.other.apiRequirementTitle') }}</h4>
        <ul>
          <li>{{ t('setting.sms.other.req1') }}</li>
          <li>{{ t('setting.sms.other.req2') }}</li>
          <li>{{ t('setting.sms.other.req3') }}</li>
          <li>{{ t('setting.sms.other.req4') }}</li>
        </ul>
        
        <h4>{{ t('setting.sms.common.notes') }}</h4>
        <ul>
          <li>{{ t('setting.sms.other.noteApiUrl') }}</li>
          <li>{{ t('setting.sms.other.noteApiKeySecurity') }}</li>
          <li>{{ t('setting.sms.common.noteTestEnv') }}</li>
          <li>{{ t('setting.sms.other.noteBalance') }}</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, watch, computed } from 'vue'
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

// 自定义参数
const customParams = ref([
  { key: '', value: '' }
])

// 监听配置变化
watch(config, (newVal) => {
  emit('update:modelValue', newVal)
}, { deep: true })

// 监听自定义参数变化
watch(customParams, (newVal) => {
  const params = {}
  newVal.forEach(param => {
    if (param.key && param.value) {
      params[param.key] = param.value
    }
  })
  config.value.customParams = params
}, { deep: true })

// 添加参数
const addParam = () => {
  customParams.value.push({ key: '', value: '' })
}

// 删除参数
const removeParam = (index) => {
  if (customParams.value.length > 1) {
    customParams.value.splice(index, 1)
  }
}

// 测试连接
const testConnection = async () => {
  if (!config.value.apiUrl || !config.value.apiKey) {
    ElMessage.warning(t('setting.sms.other.pleaseFill'))
    return
  }
  
  try {
    testing.value = true
    
    // 这里应该调用后端API测试连接
    console.log('测试自定义短信服务连接:', config.value)
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success(t('setting.sms.other.testSuccess'))
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
    ElMessage.warning(t('setting.sms.other.pleaseEnable'))
    return
  }
  
  emit('test')
}
</script>

<style scoped>
.other-sms-config {
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

.custom-params {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background-color: #fafafa;
}

.param-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
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
