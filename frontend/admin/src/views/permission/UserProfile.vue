<template>
  <div class="user-profile-container">
    <div class="user-profile-card">
      <div class="user-profile-header">
        <h2>用户信息</h2>
        <p>管理您的个人信息和账户设置</p>
      </div>
      
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="基本信息" name="basic">
          <BasicInfo />
        </el-tab-pane>
        <el-tab-pane label="安全设置" name="security">
          <SecuritySettings />
        </el-tab-pane>
        <el-tab-pane label="通知设置" name="notification">
          <NotificationSettings />
        </el-tab-pane>
        <el-tab-pane label="隐私设置" name="privacy">
          <PrivacySettings />
        </el-tab-pane>
      </el-tabs>
      
      <div class="user-profile-footer">
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import BasicInfo from '../components/BasicInfo.vue'
import SecuritySettings from '../components/SecuritySettings.vue'
import NotificationSettings from '../components/NotificationSettings.vue'
import PrivacySettings from '../components/PrivacySettings.vue'

const router = useRouter()
const activeTab = ref('basic')

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.user-profile-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.user-profile-card {
  width: 100%;
  max-width: 800px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.user-profile-header {
  text-align: center;
  margin-bottom: 30px;
}

.user-profile-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 2rem;
}

.user-profile-header p {
  color: #7f8c8d;
  margin: 0;
  font-size: 1.1rem;
}

.profile-tabs {
  margin-bottom: 30px;
}

.user-profile-footer {
  text-align: center;
}

@media (max-width: 768px) {
  .user-profile-card {
    padding: 30px 20px;
  }
  
  .user-profile-header h2 {
    font-size: 1.5rem;
  }
}
</style>
