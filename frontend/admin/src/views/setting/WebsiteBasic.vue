<template>
  <div class="website-basic">
    <el-card class="basic-card">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>基础配置</span>
        </div>
      </template>
      
      <el-form :model="websiteForm" label-width="140px">
        <el-form-item label="是否国际化">
          <el-switch v-model="websiteForm.internationalizationEnabled" />
          <span class="switch-hint">关闭后，右上角不显示多语言切换</span>
        </el-form-item>

        <el-form-item label="是否多组织">
          <el-switch v-model="websiteForm.multiOrganizationEnabled" />
          <span class="switch-hint">关闭后，不再强制校验 org_id 绑定</span>
        </el-form-item>

        <el-form-item label="AI 导购入口">
          <el-switch v-model="websiteForm.aiAssistantEnabled" />
          <span class="switch-hint">开启后，商城顶部显示 AI 助手入口</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
          <el-button @click="loadSettings">重载</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Monitor } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { websiteSettingApi } from '@/api/setting/website'

/**
 * 网站基本信息设置页面
 * 
 * @author Amos
 * @since 2025-01-07
 */

/**
 * 网站基础开关表单
 */
const websiteForm = ref({
  internationalizationEnabled: true,
  multiOrganizationEnabled: true,
  aiAssistantEnabled: true
})

/**
 * 从接口加载配置并同步本地缓存
 * @returns {Promise<void>} 加载完成
 */
const loadSettings = async () => {
  const res = await websiteSettingApi.getWebsiteSwitches()
  if (res?.code !== 1) {
    throw new Error(res?.message || '加载配置失败')
  }

  const data = res.data || {}
  websiteForm.value.internationalizationEnabled = data.internationalizationEnabled !== false
  websiteForm.value.multiOrganizationEnabled = data.multiOrganizationEnabled !== false
  websiteForm.value.aiAssistantEnabled = data.aiAssistantEnabled !== false

  localStorage.setItem('internationalizationEnabled', websiteForm.value.internationalizationEnabled ? '1' : '0')
  localStorage.setItem('multiOrganizationEnabled', websiteForm.value.multiOrganizationEnabled ? '1' : '0')
}

/**
 * 保存基础开关配置并同步本地缓存
 * @returns {Promise<void>} 保存完成
 */
const saveSettings = async () => {
  const payload = {
    internationalizationEnabled: !!websiteForm.value.internationalizationEnabled,
    multiOrganizationEnabled: !!websiteForm.value.multiOrganizationEnabled,
    aiAssistantEnabled: !!websiteForm.value.aiAssistantEnabled
  }

  const res = await websiteSettingApi.saveWebsiteSwitches(payload)
  if (res?.code !== 1) {
    throw new Error(res?.message || '保存配置失败')
  }

  localStorage.setItem('internationalizationEnabled', payload.internationalizationEnabled ? '1' : '0')
  localStorage.setItem('multiOrganizationEnabled', payload.multiOrganizationEnabled ? '1' : '0')

  ElMessage.success('基础配置保存成功')
}

onMounted(async () => {
  try {
    await loadSettings()
  } catch (error) {
    ElMessage.error(error.message || '加载基础配置失败')
  }
})
</script>

<style scoped>
.website-basic {
  padding: 20px;
}

.basic-card {
  max-width: 900px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 500;
}

.switch-hint {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
