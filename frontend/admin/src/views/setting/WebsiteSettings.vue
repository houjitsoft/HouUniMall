<template>
  <div class="website-settings">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>网站设置</span>
          <el-button type="primary" @click="saveAllSettings">保存所有设置</el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="card" class="settings-tabs">
        <el-tab-pane label="基础配置" name="basic">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              基础配置
            </span>
          </template>
          <WebsiteBasic />
        </el-tab-pane>

        <el-tab-pane label="邮件配置" name="email">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              邮件配置
            </span>
          </template>
          <WebsiteEmail />
        </el-tab-pane>
        
        <el-tab-pane label="时区配置" name="timezone">
          <template #label>
            <span class="tab-label">
              <el-icon><Clock /></el-icon>
              时区配置
            </span>
          </template>
          <WebsiteTimezone />
        </el-tab-pane>
        
        <el-tab-pane label="语言配置" name="language">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              语言配置
            </span>
          </template>
          <WebsiteLanguage />
        </el-tab-pane>
        
        <el-tab-pane label="主题配置" name="theme">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              主题配置
            </span>
          </template>
          <WebsiteTheme />
        </el-tab-pane>
        
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Clock } from '@element-plus/icons-vue'
import WebsiteBasic from './WebsiteBasic.vue'
import WebsiteEmail from './WebsiteEmail.vue'
import WebsiteTimezone from './WebsiteTimezone.vue'
import WebsiteLanguage from './WebsiteLanguage.vue'
import WebsiteTheme from './WebsiteTheme.vue'

const route = useRoute()

// 根据路由路径确定默认激活的选项卡
const getDefaultTab = () => {
  const path = route.path
  console.log('当前路由路径:', path)
  
  if (path.includes('/basic')) return 'basic'
  if (path.includes('/email')) return 'email'
  if (path.includes('/timezone')) return 'timezone'
  if (path.includes('/language')) return 'language'
  if (path.includes('/theme')) return 'theme'
  if (path.includes('/website')) return 'basic'
  
  return 'basic' // 默认显示基础配置
}

const activeTab = ref(getDefaultTab())

const saveAllSettings = () => {
  ElMessage.success('所有设置保存成功')
}

// 监听路由变化，自动切换选项卡
watch(() => route.path, (newPath) => {
  console.log('路由变化:', newPath)
  const newTab = getDefaultTab()
  if (newTab !== activeTab.value) {
    activeTab.value = newTab
    console.log('自动切换到选项卡:', newTab)
  }
})

onMounted(() => {
  console.log('网站设置页面加载完成')
  console.log('当前激活的标签页:', activeTab.value)
  console.log('当前路由路径:', route.path)
})
</script>

<style scoped>
.website-settings {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settings-tabs {
  margin-top: 20px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 5px;
}

:deep(.el-tabs__content) {
  padding: 20px 0;
}

:deep(.el-tab-pane) {
  min-height: 400px;
}
</style>
