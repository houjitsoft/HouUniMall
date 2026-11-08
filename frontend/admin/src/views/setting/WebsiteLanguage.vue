<template>
  <div class="language-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>语言配置</span>
        </div>
      </template>
      
      <el-form :model="languageForm" label-width="120px">
        <el-form-item label="系统语言">
          <el-select v-model="languageForm.systemLanguage" placeholder="请选择系统语言">
            <el-option
              v-for="lang in enabledLanguageOptions"
              :key="lang.value"
              :label="lang.label"
              :value="lang.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="默认语言">
          <el-select v-model="languageForm.defaultLanguage" placeholder="请选择默认语言">
            <el-option
              v-for="lang in enabledLanguageOptions"
              :key="lang.value"
              :label="lang.label"
              :value="lang.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="多语言支持">
          <el-switch v-model="languageForm.multiLanguage" />
          <span class="form-tip">开启后用户可以选择界面语言</span>
        </el-form-item>
        
        <el-form-item label="自动检测">
          <el-switch v-model="languageForm.autoDetect" />
          <span class="form-tip">根据浏览器语言自动设置</span>
        </el-form-item>
        
        <el-form-item label="支持的语言">
          <el-checkbox-group v-model="languageForm.supportedLanguages">
            <el-checkbox
              v-for="lang in languageOptions"
              :key="lang.value"
              :label="lang.value"
            >
              {{ lang.label }}
            </el-checkbox>
          </el-checkbox-group>
          <div class="form-tip">右上角语言菜单只显示这里勾选的语言</div>
        </el-form-item>
        
        <el-form-item label="RTL支持">
          <el-switch v-model="languageForm.rtlSupport" />
          <span class="form-tip">支持从右到左的语言（如阿拉伯语）</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveLanguageConfig">保存配置</el-button>
          <el-button @click="resetLanguageConfig">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminLanguageApi } from '@/api/setting/adminLanguage'
import {
  ADMIN_LANGUAGES,
  defaultAdminLanguageConfig,
  normalizeAdminLanguageConfig,
  publishAdminLanguageConfig
} from '@/locales/adminLanguageCatalog'

const languageOptions = ADMIN_LANGUAGES
const saving = ref(false)
const savedSnapshot = ref(defaultAdminLanguageConfig())

const languageForm = reactive(defaultAdminLanguageConfig())

const enabledLanguageOptions = computed(() => (
  languageOptions.filter((lang) => languageForm.supportedLanguages.includes(lang.value))
))

function applyConfig(config) {
  const normalized = normalizeAdminLanguageConfig(config) || defaultAdminLanguageConfig()
  Object.assign(languageForm, normalized)
  languageForm.supportedLanguages = [...normalized.supportedLanguages]
  savedSnapshot.value = normalizeAdminLanguageConfig(normalized)
}

watch(() => languageForm.supportedLanguages.slice(), (supported) => {
  if (!supported.length) return
  if (!supported.includes(languageForm.systemLanguage)) {
    languageForm.systemLanguage = supported[0]
  }
  if (!supported.includes(languageForm.defaultLanguage)) {
    languageForm.defaultLanguage = supported[0]
  }
})

const loadLanguageConfig = async () => {
  const res = await adminLanguageApi.getAdminLanguage()
  if (res?.code !== 1) {
    throw new Error(res?.message || '加载语言配置失败')
  }
  applyConfig(res.data)
}

const saveLanguageConfig = async () => {
  const normalized = normalizeAdminLanguageConfig({ ...languageForm }, { useDefaultWhenEmpty: false })
  if (!normalized) {
    ElMessage.warning('请至少选择一种语言')
    return
  }
  saving.value = true
  try {
    const res = await adminLanguageApi.saveAdminLanguage(normalized)
    if (res?.code !== 1) {
      throw new Error(res?.message || '保存语言配置失败')
    }
    applyConfig(res.data || normalized)
    publishAdminLanguageConfig(savedSnapshot.value)
    ElMessage.success('语言配置保存成功')
  } catch (error) {
    ElMessage.error(error.message || '保存语言配置失败')
  } finally {
    saving.value = false
  }
}

const resetLanguageConfig = () => {
  applyConfig(savedSnapshot.value)
  ElMessage.info('已恢复为当前已保存的配置')
}

onMounted(async () => {
  try {
    await loadLanguageConfig()
  } catch (error) {
    ElMessage.error(error.message || '加载语言配置失败')
  }
})
</script>

<style scoped>
.language-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.el-checkbox-group + .form-tip {
  display: block;
  margin: 8px 0 0;
}
</style>
