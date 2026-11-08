<template>
  <div class="system-cache">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>缓存管理</span>
          <el-button type="danger" @click="clearAllCache">清空所有缓存</el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="cache-card">
            <template #header>
              <span>Redis缓存</span>
            </template>
            <div class="cache-info">
              <p>状态：<el-tag type="success">正常</el-tag></p>
              <p>内存使用：<span class="highlight">128MB / 512MB</span></p>
              <p>键数量：<span class="highlight">1,234</span></p>
              <p>命中率：<span class="highlight">95.6%</span></p>
            </div>
            <el-button type="warning" @click="clearRedisCache">清空Redis缓存</el-button>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="cache-card">
            <template #header>
              <span>应用缓存</span>
            </template>
            <div class="cache-info">
              <p>状态：<el-tag type="success">正常</el-tag></p>
              <p>缓存大小：<span class="highlight">64MB</span></p>
              <p>缓存项：<span class="highlight">456</span></p>
              <p>命中率：<span class="highlight">88.2%</span></p>
            </div>
            <el-button type="warning" @click="clearAppCache">清空应用缓存</el-button>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="cache-card">
            <template #header>
              <span>静态资源缓存</span>
            </template>
            <div class="cache-info">
              <p>状态：<el-tag type="success">正常</el-tag></p>
              <p>缓存大小：<span class="highlight">256MB</span></p>
              <p>文件数量：<span class="highlight">1,890</span></p>
              <p>命中率：<span class="highlight">92.1%</span></p>
            </div>
            <el-button type="warning" @click="clearStaticCache">清空静态缓存</el-button>
          </el-card>
        </el-col>
      </el-row>
      
      <el-divider></el-divider>
      
      <h3>缓存配置</h3>
      <el-form :model="cacheConfig" label-width="150px">
        <el-form-item label="Redis过期时间">
          <el-input-number v-model="cacheConfig.redisExpire" :min="0" :max="86400" controls-position="right" />
          <span class="form-tip">（秒）</span>
        </el-form-item>
        
        <el-form-item label="应用缓存过期时间">
          <el-input-number v-model="cacheConfig.appExpire" :min="0" :max="3600" controls-position="right" />
          <span class="form-tip">（秒）</span>
        </el-form-item>
        
        <el-form-item label="静态资源缓存时间">
          <el-input-number v-model="cacheConfig.staticExpire" :min="0" :max="31536000" controls-position="right" />
          <span class="form-tip">（秒）</span>
        </el-form-item>
        
        <el-form-item label="启用缓存预热">
          <el-switch v-model="cacheConfig.preloadEnabled"></el-switch>
        </el-form-item>
        
        <el-form-item label="启用缓存压缩">
          <el-switch v-model="cacheConfig.compressionEnabled"></el-switch>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveCacheConfig">保存配置</el-button>
          <el-button @click="resetCacheConfig">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const cacheConfig = reactive({
  redisExpire: 3600,
  appExpire: 1800,
  staticExpire: 86400,
  preloadEnabled: true,
  compressionEnabled: true
})

const clearRedisCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清空Redis缓存吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('Redis缓存清空成功')
  } catch {
    ElMessage.info('已取消清空')
  }
}

const clearAppCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清空应用缓存吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('应用缓存清空成功')
  } catch {
    ElMessage.info('已取消清空')
  }
}

const clearStaticCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清空静态资源缓存吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('静态资源缓存清空成功')
  } catch {
    ElMessage.info('已取消清空')
  }
}

const clearAllCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有缓存吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    ElMessage.success('所有缓存清空成功')
  } catch {
    ElMessage.info('已取消清空')
  }
}

const saveCacheConfig = () => {
  ElMessage.success('缓存配置保存成功')
}

const resetCacheConfig = () => {
  cacheConfig.redisExpire = 3600
  cacheConfig.appExpire = 1800
  cacheConfig.staticExpire = 86400
  cacheConfig.preloadEnabled = true
  cacheConfig.compressionEnabled = true
  ElMessage.info('配置已重置')
}
</script>

<style scoped>
.system-cache {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cache-card {
  margin-bottom: 20px;
}

.cache-info p {
  margin: 10px 0;
  color: #606266;
}

.highlight {
  color: #409EFF;
  font-weight: bold;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}
</style>
