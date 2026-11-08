<template>
  <div class="permission-index">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <el-icon><Lock /></el-icon>
          <span>权限管理</span>
        </div>
      </template>
      
      <div class="welcome-content">
        <h2>欢迎使用权限管理系统</h2>
        <p>请从左侧菜单选择您要管理的功能：</p>
        
        <el-row :gutter="20" class="feature-cards">
          <el-col :span="12">
            <el-card class="feature-card" @click="navigateToManagement">
              <div class="feature-content">
                <el-icon class="feature-icon"><Lock /></el-icon>
                <h3>权限管理</h3>
                <p>查看和管理用户权限，包括权限列表、权限测试等功能</p>
                <el-button type="primary">进入管理</el-button>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card class="feature-card" @click="navigateToConfig">
              <div class="feature-content">
                <el-icon class="feature-icon"><Setting /></el-icon>
                <h3>权限配置</h3>
                <p>配置权限规则、角色权限映射等系统设置</p>
                <el-button type="primary">进入配置</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <div class="quick-stats">
          <h3>快速统计</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="总权限数" :value="totalPermissions" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="活跃用户" :value="activeUsers" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="角色数量" :value="roleCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="组织数量" :value="orgCount" />
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
import { Lock, Setting } from '@element-plus/icons-vue'

/**
 * 权限管理首页
 * 
 * @author Amos
 * @since 2025-01-07
 */

const router = useRouter()

// 统计数据
const totalPermissions = ref(156)
const activeUsers = ref(89)
const roleCount = ref(12)
const orgCount = ref(5)

// 导航到权限管理
const navigateToManagement = () => {
  router.push('/permission/management')
}

// 导航到权限配置
const navigateToConfig = () => {
  router.push('/permission/config')
}
</script>

<style scoped>
.permission-index {
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
