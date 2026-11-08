<template>
  <div class="cache-clear-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>缓存清理</h2>
      <p>清理系统中的各种缓存数据</p>
    </div>

    <!-- 缓存清理操作 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="clear-card">
          <template #header>
            <div class="card-header">
              <span>Redis缓存</span>
              <el-tag type="success">正常</el-tag>
            </div>
          </template>
          <div class="cache-info">
            <p>内存使用：<span class="highlight">128MB / 512MB</span></p>
            <p>键数量：<span class="highlight">1,234</span></p>
            <p>命中率：<span class="highlight">95.6%</span></p>
          </div>
          <div class="clear-actions">
            <el-button type="warning" @click="clearRedisCache" :loading="clearing.redis">
              <el-icon><Delete /></el-icon>
              清空Redis缓存
            </el-button>
            <el-button type="info" @click="clearRedisByPattern">
              <el-icon><Filter /></el-icon>
              按模式清理
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="clear-card">
          <template #header>
            <div class="card-header">
              <span>应用缓存</span>
              <el-tag type="success">正常</el-tag>
            </div>
          </template>
          <div class="cache-info">
            <p>缓存大小：<span class="highlight">64MB</span></p>
            <p>缓存项：<span class="highlight">456</span></p>
            <p>命中率：<span class="highlight">88.2%</span></p>
          </div>
          <div class="clear-actions">
            <el-button type="warning" @click="clearAppCache" :loading="clearing.app">
              <el-icon><Delete /></el-icon>
              清空应用缓存
            </el-button>
            <el-button type="info" @click="clearAppByType">
              <el-icon><Filter /></el-icon>
              按类型清理
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="clear-card">
          <template #header>
            <div class="card-header">
              <span>静态资源缓存</span>
              <el-tag type="success">正常</el-tag>
            </div>
          </template>
          <div class="cache-info">
            <p>缓存大小：<span class="highlight">256MB</span></p>
            <p>文件数量：<span class="highlight">1,890</span></p>
            <p>命中率：<span class="highlight">92.1%</span></p>
          </div>
          <div class="clear-actions">
            <el-button type="warning" @click="clearStaticCache" :loading="clearing.static">
              <el-icon><Delete /></el-icon>
              清空静态缓存
            </el-button>
            <el-button type="info" @click="clearStaticByExt">
              <el-icon><Filter /></el-icon>
              按扩展名清理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 批量操作 -->
    <el-card class="batch-card">
      <template #header>
        <span>批量操作</span>
      </template>
      
      <div class="batch-actions">
        <el-button type="danger" @click="clearAllCache" :loading="clearing.all">
          <el-icon><Delete /></el-icon>
          清空所有缓存
        </el-button>
        <el-button type="primary" @click="clearExpiredCache">
          <el-icon><Clock /></el-icon>
          清理过期缓存
        </el-button>
        <el-button type="success" @click="optimizeCache">
          <el-icon><MagicStick /></el-icon>
          优化缓存
        </el-button>
        <el-button type="info" @click="refreshCacheStats">
          <el-icon><Refresh /></el-icon>
          刷新统计
        </el-button>
      </div>
    </el-card>

    <!-- 清理历史 -->
    <el-card class="history-card">
      <template #header>
        <span>清理历史</span>
      </template>
      
      <el-table :data="clearHistory" stripe>
        <el-table-column prop="time" label="清理时间" width="180" />
        <el-table-column prop="type" label="缓存类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作类型" width="120" />
        <el-table-column prop="result" label="清理结果" width="120">
          <template #default="{ row }">
            <el-tag :type="row.result === '成功' ? 'success' : 'danger'">
              {{ row.result }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="details" label="详细信息" />
      </el-table>
    </el-card>

    <!-- 按模式清理对话框 -->
    <el-dialog v-model="showPatternDialog" title="按模式清理Redis缓存" width="500px">
      <el-form :model="patternForm" label-width="100px">
        <el-form-item label="键模式">
          <el-input v-model="patternForm.pattern" placeholder="例如: user:*" />
          <div class="form-tip">支持通配符 * 和 ?</div>
        </el-form-item>
        <el-form-item label="确认模式">
          <el-input v-model="patternForm.confirmPattern" placeholder="请再次输入模式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPatternDialog = false">取消</el-button>
        <el-button type="danger" @click="executePatternClear">确认清理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Filter, Clock, MagicStick, Refresh } from '@element-plus/icons-vue'

// 数据
const clearing = reactive({
  redis: false,
  app: false,
  static: false,
  all: false
})

const showPatternDialog = ref(false)
const patternForm = reactive({
  pattern: '',
  confirmPattern: ''
})

const clearHistory = ref([
  {
    time: '2024-01-01 10:30:00',
    type: 'Redis缓存',
    operation: '全部清理',
    result: '成功',
    details: '清理了1,234个键，释放128MB内存'
  },
  {
    time: '2024-01-01 09:15:00',
    type: '应用缓存',
    operation: '按类型清理',
    result: '成功',
    details: '清理了用户相关缓存，释放32MB内存'
  },
  {
    time: '2024-01-01 08:00:00',
    type: '静态资源',
    operation: '过期清理',
    result: '成功',
    details: '清理了过期的CSS和JS文件'
  }
])

// 方法
const clearRedisCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清空Redis缓存吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    clearing.redis = true
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    ElMessage.success('Redis缓存清空成功')
    addClearHistory('Redis缓存', '全部清理', '成功', '清理了1,234个键，释放128MB内存')
  } catch {
    ElMessage.info('已取消清空')
  } finally {
    clearing.redis = false
  }
}

const clearAppCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清空应用缓存吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    clearing.app = true
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    ElMessage.success('应用缓存清空成功')
    addClearHistory('应用缓存', '全部清理', '成功', '清理了456个缓存项，释放64MB内存')
  } catch {
    ElMessage.info('已取消清空')
  } finally {
    clearing.app = false
  }
}

const clearStaticCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清空静态资源缓存吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    clearing.static = true
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('静态资源缓存清空成功')
    addClearHistory('静态资源', '全部清理', '成功', '清理了1,890个文件，释放256MB内存')
  } catch {
    ElMessage.info('已取消清空')
  } finally {
    clearing.static = false
  }
}

const clearAllCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有缓存吗？此操作不可恢复！', '严重警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    clearing.all = true
    await new Promise(resolve => setTimeout(resolve, 3000))
    
    ElMessage.success('所有缓存清空成功')
    addClearHistory('所有缓存', '全部清理', '成功', '清理了所有类型的缓存，释放448MB内存')
  } catch {
    ElMessage.info('已取消清空')
  } finally {
    clearing.all = false
  }
}

const clearExpiredCache = async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('过期缓存清理完成')
    addClearHistory('所有缓存', '过期清理', '成功', '清理了过期的缓存项')
  } catch (error) {
    ElMessage.error('清理失败: ' + error.message)
  }
}

const optimizeCache = async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('缓存优化完成')
    addClearHistory('所有缓存', '优化', '成功', '优化了缓存结构，提升了性能')
  } catch (error) {
    ElMessage.error('优化失败: ' + error.message)
  }
}

const refreshCacheStats = () => {
  ElMessage.success('缓存统计已刷新')
}

const clearRedisByPattern = () => {
  showPatternDialog.value = true
}

const clearAppByType = () => {
  ElMessage.info('按类型清理功能开发中...')
}

const clearStaticByExt = () => {
  ElMessage.info('按扩展名清理功能开发中...')
}

const executePatternClear = async () => {
  if (patternForm.pattern !== patternForm.confirmPattern) {
    ElMessage.error('两次输入的模式不一致')
    return
  }
  
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(`按模式 "${patternForm.pattern}" 清理完成`)
    addClearHistory('Redis缓存', '按模式清理', '成功', `清理了匹配 "${patternForm.pattern}" 的键`)
    showPatternDialog.value = false
    patternForm.pattern = ''
    patternForm.confirmPattern = ''
  } catch (error) {
    ElMessage.error('清理失败: ' + error.message)
  }
}

const addClearHistory = (type, operation, result, details) => {
  const now = new Date()
  const timeStr = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).replace(/\//g, '-')
  
  clearHistory.value.unshift({
    time: timeStr,
    type,
    operation,
    result,
    details
  })
}

const getTypeTagType = (type) => {
  const typeMap = {
    'Redis缓存': 'danger',
    '应用缓存': 'warning',
    '静态资源': 'info',
    '所有缓存': 'success'
  }
  return typeMap[type] || 'info'
}

// 生命周期
onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.cache-clear-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.clear-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cache-info p {
  margin: 10px 0;
  color: #606266;
}

.highlight {
  color: #409EFF;
  font-weight: bold;
}

.clear-actions {
  margin-top: 15px;
}

.clear-actions .el-button {
  margin-right: 10px;
  margin-bottom: 10px;
}

.batch-card {
  margin-bottom: 20px;
}

.batch-actions .el-button {
  margin-right: 15px;
  margin-bottom: 10px;
}

.history-card {
  margin-bottom: 20px;
}

.form-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}
</style>
