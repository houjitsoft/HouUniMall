<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>欢迎加入 HouEOC 平台</p>
      </div>
      
      <!-- 注册方式选择 -->
      <div class="register-tabs">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="手机号注册" name="phone">
            <PhoneRegister />
          </el-tab-pane>
          <el-tab-pane label="邮箱注册" name="email">
            <EmailRegister />
          </el-tab-pane>
          <el-tab-pane label="企业注册" name="enterprise">
            <EnterpriseRegister />
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <div class="register-footer">
        <p>已有账号？<el-link @click="goToLogin">立即登录</el-link></p>
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
import PhoneRegister from './components/PhoneRegister.vue'
import EmailRegister from './components/EmailRegister.vue'
import EnterpriseRegister from './components/EnterpriseRegister.vue'

const router = useRouter()
const activeTab = ref('phone')

const handleTabClick = (tab) => {
  activeTab.value = tab.paneName
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 500px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 2rem;
}

.register-header p {
  color: #7f8c8d;
  margin: 0;
  font-size: 1.1rem;
}

.register-tabs {
  margin-bottom: 30px;
}

.register-footer {
  text-align: center;
  color: #7f8c8d;
}

.register-footer p {
  margin: 0;
}

@media (max-width: 768px) {
  .register-card {
    padding: 30px 20px;
  }
  
  .register-header h2 {
    font-size: 1.5rem;
  }
}
</style>
