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
        <el-tab-pane label="邮件配置" name="email">
          <div class="tab-content">
            <h3>邮件配置</h3>
            <p>邮件配置功能正在开发中...</p>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="时区配置" name="timezone">
          <div class="tab-content">
            <h3>时区配置</h3>
            <p>时区配置功能正在开发中...</p>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="语言配置" name="language">
          <div class="tab-content">
            <h3>语言配置</h3>
            <p>语言配置功能正在开发中...</p>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="主题配置" name="theme">
          <div class="tab-content">
            <h3>主题配置</h3>
            <p>主题配置功能正在开发中...</p>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="SEO配置" name="seo">
          <div class="tab-content">
            <h3>SEO配置</h3>
            <p>SEO配置功能正在开发中...</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('email')

const saveAllSettings = () => {
  ElMessage.success('所有设置保存成功')
}

onMounted(() => {
  console.log('网站设置页面加载完成')
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

.tab-content {
  padding: 20px;
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
