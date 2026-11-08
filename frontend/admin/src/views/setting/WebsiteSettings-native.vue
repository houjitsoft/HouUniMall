<template>
  <div class="website-settings">
    <h1>网站设置</h1>
    <div class="tabs">
      <div class="tab-header">
        <button 
          v-for="tab in tabs" 
          :key="tab.name"
          :class="{ active: activeTab === tab.name }"
          @click="handleTabClick(tab)"
        >
          {{ tab.label }}
        </button>
      </div>
      <div class="tab-content">
        <div v-if="activeTab === 'email'">
          <WebsiteEmail />
        </div>
        <div v-if="activeTab === 'timezone'">
          <WebsiteTimezone />
        </div>
        <div v-if="activeTab === 'language'">
          <WebsiteLanguage />
        </div>
        <div v-if="activeTab === 'theme'">
          <WebsiteTheme />
        </div>
        <div v-if="activeTab === 'seo'">
          <WebsiteSEO />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import WebsiteEmail from './WebsiteEmail.vue'
import WebsiteTimezone from './WebsiteTimezone.vue'
import WebsiteLanguage from './WebsiteLanguage.vue'
import WebsiteTheme from './WebsiteTheme.vue'
import WebsiteSEO from './WebsiteSEO.vue'

const router = useRouter()
const activeTab = ref('email')

const tabs = [
  { name: 'email', label: '邮件配置', path: '/website/email' },
  { name: 'timezone', label: '时区配置', path: '/website/timezone' },
  { name: 'language', label: '语言配置', path: '/website/language' },
  { name: 'theme', label: '主题配置', path: '/website/theme' },
  { name: 'seo', label: 'SEO配置', path: '/website/seo' }
]

const handleTabClick = (tab) => {
  activeTab.value = tab.name
  // 不再跳转路由，只是切换选项卡内容
}
</script>

<style scoped>
.website-settings {
  padding: 20px;
}

.tabs {
  margin-top: 20px;
}

.tab-header {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.tab-header button {
  padding: 10px 20px;
  border: none;
  background: none;
  cursor: pointer;
  border-bottom: 2px solid transparent;
}

.tab-header button.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

.tab-content {
  min-height: 300px;
}

.tab-content h3 {
  margin-top: 0;
  color: #303133;
}

.tab-content p {
  color: #606266;
  margin-top: 10px;
}
</style>
