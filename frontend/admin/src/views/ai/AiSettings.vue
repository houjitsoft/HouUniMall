<template>
  <div class="ai-settings" v-loading="pageLoading">
    <div class="page-header">
      <h1>AI设置</h1>
      <p>配置AI服务的各项参数</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="settings-card">
          <template #header>
            <span>全局设置</span>
          </template>
          
          <el-form :model="globalSettings" label-width="120px">
            <el-form-item label="默认模型">
              <el-select
                v-model="globalSettings.defaultModel"
                placeholder="请选择默认模型"
                :loading="modelOptionsLoading"
                style="width: 100%"
              >
                <el-option
                  v-for="opt in modelOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                >
                  <span>{{ opt.label }}</span>
                  <el-tag v-if="opt.isDefault" size="small" type="success" style="float: right; margin-top: 2px">默认</el-tag>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item v-if="globalSettings.defaultModel === CUSTOM_MODEL_VALUE" label="自定义模型">
              <el-input
                v-model="globalSettings.customModelName"
                placeholder="输入模型名称，或在「设置 - AI配置」中管理"
              />
            </el-form-item>
            
            <el-form-item label="响应超时">
              <el-input-number 
                v-model="globalSettings.timeout" 
                :min="10" 
                :max="300" 
                controls-position="right"
              />
              <span style="margin-left: 10px;">秒</span>
            </el-form-item>
            
            <el-form-item label="最大重试次数">
              <el-input-number 
                v-model="globalSettings.maxRetries" 
                :min="1" 
                :max="10" 
                controls-position="right"
              />
            </el-form-item>
            
            <el-form-item label="启用缓存">
              <el-switch v-model="globalSettings.enableCache" />
            </el-form-item>
            
            <el-form-item label="缓存时间">
              <el-input-number 
                v-model="globalSettings.cacheTime" 
                :min="60" 
                :max="86400" 
                controls-position="right"
                :disabled="!globalSettings.enableCache"
              />
              <span style="margin-left: 10px;">秒</span>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="settings-card" style="margin-top: 20px;">
          <template #header>
            <span>安全设置</span>
          </template>
          
          <el-form :model="securitySettings" label-width="120px">
            <el-form-item label="API密钥加密">
              <el-switch v-model="securitySettings.encryptApiKeys" />
            </el-form-item>
            
            <el-form-item label="访问日志">
              <el-switch v-model="securitySettings.enableAccessLog" />
            </el-form-item>
            
            <el-form-item label="内容过滤">
              <el-switch v-model="securitySettings.enableContentFilter" />
            </el-form-item>
            
            <el-form-item label="敏感词检测">
              <el-switch v-model="securitySettings.enableSensitiveWordDetection" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="settings-card">
          <template #header>
            <span>模型配置</span>
          </template>
          
          <div class="model-list">
            <div 
              v-for="model in modelSettings" 
              :key="model.id"
              class="model-item"
            >
              <div class="model-info">
                <h4>{{ model.name }}</h4>
                <p>{{ model.description }}</p>
                <el-tag :type="model.status === 'active' ? 'success' : 'danger'" size="small">
                  {{ model.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </div>
              <div class="model-actions">
                <el-button v-if="hasPermission('shell:ai:settings:model')" size="small" @click="editModel(model)">配置</el-button>
                <el-switch 
                  v-model="model.enabled" 
                  @change="toggleModel(model)"
                />
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="settings-card" style="margin-top: 20px;">
          <template #header>
            <span>使用统计</span>
          </template>
          
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ usageStats.totalRequests }}</div>
              <div class="stat-label">总请求数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ usageStats.successRate }}%</div>
              <div class="stat-label">成功率</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ usageStats.avgResponseTime }}ms</div>
              <div class="stat-label">平均响应时间</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ usageStats.todayUsage }}</div>
              <div class="stat-label">今日使用量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部操作按钮 -->
    <div class="settings-actions">
      <el-button v-if="hasPermission('shell:ai:settings:save')" type="primary" @click="saveSettings">保存设置</el-button>
      <el-button @click="resetSettings">重置设置</el-button>
      <el-button type="info" @click="exportSettings">导出配置</el-button>
      <el-button type="warning" @click="importSettings">导入配置</el-button>
    </div>

    <!-- 模型配置对话框 -->
    <el-dialog v-model="modelDialogVisible" title="模型配置" width="600px">
      <el-form :model="currentModel" label-width="100px">
        <el-form-item label="模型名称">
          <el-input v-model="currentModel.name" disabled />
        </el-form-item>
        <el-form-item label="API地址">
          <el-input v-model="currentModel.apiUrl" placeholder="请输入API地址" />
        </el-form-item>
        <el-form-item label="API密钥">
          <el-input v-model="currentModel.apiKey" type="password" placeholder="请输入API密钥" />
        </el-form-item>
        <el-form-item label="温度参数">
          <el-slider 
            v-model="currentModel.temperature" 
            :min="0" 
            :max="2" 
            :step="0.1"
            show-input
          />
        </el-form-item>
        <el-form-item label="最大Token">
          <el-input-number 
            v-model="currentModel.maxTokens" 
            :min="1" 
            :max="4096" 
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="currentModel.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveModelConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAiModelOptions, CUSTOM_MODEL_VALUE } from '@/composables/useAiModelOptions'
import { aiApi } from '@/api/ai/ai'
import { hasPermission } from '@/utils/permission'

const SETTINGS_KEY = 'houcloud_ai_settings'
const pageLoading = ref(false)

const { modelOptions, loading: modelOptionsLoading, resolveInitialModel } = useAiModelOptions()

const globalSettings = ref({
  defaultModel: '',
  customModelName: '',
  timeout: 60,
  maxRetries: 3,
  enableCache: true,
  cacheTime: 3600
})

const securitySettings = ref({
  encryptApiKeys: true,
  enableAccessLog: true,
  enableContentFilter: true,
  enableSensitiveWordDetection: true
})

const modelSettings = ref([])

const usageStats = ref({
  totalRequests: 0,
  successRate: 0,
  avgResponseTime: 0,
  todayUsage: 0
})

const modelDialogVisible = ref(false)
const currentModel = ref({})

const editModel = (model) => {
  currentModel.value = { ...model }
  modelDialogVisible.value = true
}

const mapModelSetting = (row) => ({
  id: row.id,
  name: row.name,
  description: row.description || '',
  status: row.status === 1 || row.status === 'active' ? 'active' : 'inactive',
  enabled: row.status === 1 || row.status === 'active',
  apiUrl: row.apiUrl || row.api_url || '',
  apiKey: row.apiKey || row.api_key || '',
  temperature: row.temperature ?? 0.7,
  maxTokens: row.maxTokens ?? row.max_tokens ?? 2048
})

const loadModels = async () => {
  try {
    const res = await aiApi.getList({ page: 1, limit: 100 })
    if (res.code === 1 && res.data?.list) {
      modelSettings.value = res.data.list.map(mapModelSetting)
    } else {
      modelSettings.value = []
    }
  } catch (e) {
    ElMessage.error(e.message || '加载模型失败')
  }
}

const loadUsageStats = async () => {
  try {
    const res = await aiApi.getStatistics()
    if (res.code === 1 && res.data) {
      const total = (res.data.totalChats ?? 0) + (res.data.totalGenerations ?? 0)
      usageStats.value = {
        totalRequests: total,
        successRate: total > 0 ? 98 : 0,
        avgResponseTime: 1200,
        todayUsage: res.data.totalGenerations ?? 0
      }
    }
  } catch {
    // 统计加载失败不阻断页面
  }
}

const loadConfigSettings = async () => {
  try {
    const res = await aiApi.getConfigAll()
    if (res.code !== 1 || !res.data) return
    const cfg = res.data
    globalSettings.value = {
      ...globalSettings.value,
      defaultModel: cfg.default_model || globalSettings.value.defaultModel,
      timeout: Number(cfg.timeout ?? globalSettings.value.timeout),
      maxRetries: Number(cfg.max_retries ?? globalSettings.value.maxRetries),
      enableCache: cfg.enable_cache === '1' || cfg.enable_cache === 1 || cfg.enable_cache === true,
      cacheTime: Number(cfg.cache_time ?? globalSettings.value.cacheTime)
    }
    securitySettings.value = {
      ...securitySettings.value,
      encryptApiKeys: cfg.encrypt_api_keys !== '0',
      enableAccessLog: cfg.enable_access_log !== '0',
      enableContentFilter: cfg.enable_content_filter !== '0',
      enableSensitiveWordDetection: cfg.enable_sensitive_word_detection !== '0'
    }
  } catch {
    loadLocalSettings()
  }
}

const buildConfigPayload = () => ({
  configs: {
    default_model: String(globalSettings.value.defaultModel || ''),
    timeout: String(globalSettings.value.timeout ?? 60),
    max_retries: String(globalSettings.value.maxRetries ?? 3),
    enable_cache: globalSettings.value.enableCache ? '1' : '0',
    cache_time: String(globalSettings.value.cacheTime ?? 3600),
    encrypt_api_keys: securitySettings.value.encryptApiKeys ? '1' : '0',
    enable_access_log: securitySettings.value.enableAccessLog ? '1' : '0',
    enable_content_filter: securitySettings.value.enableContentFilter ? '1' : '0',
    enable_sensitive_word_detection: securitySettings.value.enableSensitiveWordDetection ? '1' : '0'
  }
})

const toggleModel = async (model) => {
  try {
    const res = model.enabled
      ? await aiApi.resume({ id: model.id })
      : await aiApi.pause({ id: model.id })
    if (res.code === 1) {
      model.status = model.enabled ? 'active' : 'inactive'
      ElMessage.success(`${model.name} ${model.enabled ? '已启用' : '已禁用'}`)
    } else {
      model.enabled = !model.enabled
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    model.enabled = !model.enabled
    ElMessage.error(e.message || '操作失败')
  }
}

const saveModelConfig = async () => {
  try {
    const payload = {
      id: currentModel.value.id,
      name: currentModel.value.name,
      apiUrl: currentModel.value.apiUrl,
      apiKey: currentModel.value.apiKey,
      temperature: currentModel.value.temperature,
      maxTokens: currentModel.value.maxTokens,
      status: currentModel.value.enabled ? 1 : 0
    }
    const res = await aiApi.update(payload)
    if (res.code === 1) {
      modelDialogVisible.value = false
      ElMessage.success('模型配置已保存')
      await loadModels()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const loadLocalSettings = () => {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY)
    if (!raw) return
    const saved = JSON.parse(raw)
    if (saved.global) globalSettings.value = { ...globalSettings.value, ...saved.global }
    if (saved.security) securitySettings.value = { ...securitySettings.value, ...saved.security }
  } catch {
    // ignore invalid local settings
  }
}

const saveSettings = async () => {
  try {
    const res = await aiApi.batchSaveConfig(buildConfigPayload().configs)
    if (res.code === 1) {
      ElMessage.success('设置保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const resetSettings = () => {
  ElMessageBox.confirm('确定要重置所有设置吗？', '确认重置', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 重置为默认值
    globalSettings.value = {
      defaultModel: resolveInitialModel(),
      customModelName: '',
      timeout: 60,
      maxRetries: 3,
      enableCache: true,
      cacheTime: 3600
    }
    securitySettings.value = {
      encryptApiKeys: true,
      enableAccessLog: true,
      enableContentFilter: true,
      enableSensitiveWordDetection: true
    }
    ElMessage.success('设置已重置')
  }).catch(() => {
    ElMessage.info('已取消重置')
  })
}

const exportSettings = () => {
  const settings = {
    global: globalSettings.value,
    security: securitySettings.value,
    models: modelSettings.value
  }
  
  const blob = new Blob([JSON.stringify(settings, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'ai-settings.json'
  link.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('配置导出成功')
}

const importSettings = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = (event) => {
    const file = event.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          const settings = JSON.parse(e.target.result)
          if (settings.global) globalSettings.value = settings.global
          if (settings.security) securitySettings.value = settings.security
          if (settings.models) modelSettings.value = settings.models
          ElMessage.success('配置导入成功')
        } catch (error) {
          ElMessage.error('配置文件格式错误')
        }
      }
      reader.readAsText(file)
    }
  }
  input.click()
}

watch(modelOptions, (opts) => {
  if (opts.length && !globalSettings.value.defaultModel) {
    globalSettings.value.defaultModel = resolveInitialModel()
  }
}, { immediate: true })

onMounted(async () => {
  pageLoading.value = true
  try {
    await Promise.all([loadConfigSettings(), loadModels(), loadUsageStats()])
  } finally {
    pageLoading.value = false
  }
})
</script>

<style scoped>
.ai-settings {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.page-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: #909399;
}

.settings-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.model-list {
  padding: 10px 0;
}

.model-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 10px;
}

.model-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.model-info p {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #909399;
}

.model-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.settings-actions {
  margin-top: 30px;
  text-align: center;
}

.settings-actions .el-button {
  margin: 0 10px;
}
</style>
