<template>
  <div class="auth-index">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <el-icon><Key /></el-icon>
          <span>认证服务</span>
        </div>
      </template>
      
      <div class="welcome-content">
        <h2>欢迎使用认证服务</h2>
        <p>请从左侧菜单选择您要管理的功能：</p>
        
        <el-row :gutter="20" class="feature-cards">
          <el-col :span="6">
            <el-card class="feature-card" @click="navigateToPermission">
              <div class="feature-content">
                <el-icon class="feature-icon"><Lock /></el-icon>
                <h3>权限管理</h3>
                <p>查看和管理用户权限</p>
                <el-button type="primary">进入管理</el-button>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="feature-card" @click="navigateToUserProfile">
              <div class="feature-content">
                <el-icon class="feature-icon"><User /></el-icon>
                <h3>用户管理</h3>
                <p>管理用户信息和设置</p>
                <el-button type="primary">进入管理</el-button>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="feature-card" @click="navigateToConfig">
              <div class="feature-content">
                <el-icon class="feature-icon"><Setting /></el-icon>
                <h3>认证配置</h3>
                <p>配置认证相关设置</p>
                <el-button type="primary">进入配置</el-button>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="feature-card" @click="navigateToLogs">
              <div class="feature-content">
                <el-icon class="feature-icon"><Document /></el-icon>
                <h3>日志查看</h3>
                <p>查看认证相关日志</p>
                <el-button type="primary">查看日志</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <div class="quick-stats">
          <h3>快速统计</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="在线用户" :value="onlineUsers" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="今日登录" :value="todayLogins" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="权限数量" :value="permissionCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="角色数量" :value="roleCount" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Key, Lock, User, Setting, Document } from '@element-plus/icons-vue'

/**
 * 认证服务首页
 * 
 * @author Amos
 * @since 2025-01-07
 */

const router = useRouter()

// 统计数据
const onlineUsers = ref(156)
const todayLogins = ref(89)
const permissionCount = ref(45)
const roleCount = ref(12)

// 导航到权限管理
const navigateToPermission = () => {
  router.push('/permission/management')
}

// 导航到用户管理
const navigateToUserProfile = () => {
  router.push('/user-profile')
}

// 导航到认证配置
const navigateToConfig = () => {
  router.push('/config')
}

// 导航到日志查看
const navigateToLogs = () => {
  router.push('/logs')
}
</script>

<style scoped>
.auth-index {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 500;
}

.welcome-content h2 {
  color: #409EFF;
  margin-bottom: 10px;
}

.welcome-content p {
  color: #666;
  margin-bottom: 30px;
}

.feature-cards {
  margin-bottom: 40px;
}

.feature-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 200px;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.feature-content {
  text-align: center;
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.feature-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 15px;
}

.feature-content h3 {
  margin: 10px 0;
  color: #333;
}

.feature-content p {
  color: #666;
  margin-bottom: 20px;
  flex-grow: 1;
}

.quick-stats {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.quick-stats h3 {
  margin-bottom: 20px;
  color: #333;
}
</style>
