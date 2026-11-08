<template>
  <div class="cache-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>缓存列表</h2>
      <p>查看和管理系统中的所有缓存项</p>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索缓存键"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.type" placeholder="缓存类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="Redis" value="redis" />
            <el-option label="应用缓存" value="app" />
            <el-option label="静态资源" value="static" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="有效" value="valid" />
            <el-option label="过期" value="expired" />
            <el-option label="即将过期" value="expiring" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-col>
        <el-col :span="4">
          <el-button type="success" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新列表
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 缓存列表 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="cacheList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="key" label="缓存键" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" label="值" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="value-preview">{{ getValuePreview(row.value) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="100">
          <template #default="{ row }">
            {{ formatSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="ttl" label="TTL" width="120">
          <template #default="{ row }">
            <span :class="getTtlClass(row.ttl)">
              {{ formatTtl(row.ttl) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="accessTime" label="最后访问" width="180" />
        <el-table-column prop="accessCount" label="访问次数" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 批量操作 -->
    <div class="batch-section" v-if="selectedCaches.length > 0">
      <el-card>
        <template #header>
          <span>批量操作 (已选择 {{ selectedCaches.length }} 项)</span>
        </template>
        <div class="batch-actions">
          <el-button type="danger" @click="batchDelete">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button type="warning" @click="batchExpire">
            <el-icon><Clock /></el-icon>
            批量过期
          </el-button>
          <el-button type="info" @click="batchRefresh">
            <el-icon><Refresh /></el-icon>
            批量刷新
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 缓存详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="缓存详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentCache" class="cache-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="缓存键">{{ currentCache.key }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(currentCache.type)">
              {{ getTypeText(currentCache.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="大小">{{ formatSize(currentCache.size) }}</el-descriptions-item>
          <el-descriptions-item label="TTL">
            <span :class="getTtlClass(currentCache.ttl)">
              {{ formatTtl(currentCache.ttl) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentCache.createTime }}</el-descriptions-item>
          <el-descriptions-item label="最后访问">{{ currentCache.accessTime }}</el-descriptions-item>
          <el-descriptions-item label="访问次数">{{ currentCache.accessCount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentCache.status)">
              {{ getStatusText(currentCache.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="缓存值" :span="2">
            <el-input
              v-model="currentCache.value"
              type="textarea"
              :rows="6"
              readonly
              class="value-display"
            />
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentCache)">编辑缓存</el-button>
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
import { 
  Search, Refresh, Delete, Clock, View, Edit 
} from '@element-plus/icons-vue'

// 数据
const loading = ref(false)
const showDetailDialog = ref(false)
const currentCache = ref(null)
const cacheList = ref([])
const selectedCaches = ref([])

const searchForm = reactive({
  keyword: '',
  type: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 模拟数据
const mockCacheList = [
  {
    key: 'user:123',
    type: 'redis',
    value: '{"id":123,"name":"张三","email":"zhangsan@example.com"}',
    size: 1024,
    ttl: 3600,
    createTime: '2024-01-01 09:00:00',
    accessTime: '2024-01-01 10:30:00',
    accessCount: 15,
    status: 'valid'
  },
  {
    key: 'session:abc123',
    type: 'redis',
    value: '{"userId":123,"loginTime":"2024-01-01 09:00:00","permissions":["read","write"]}',
    size: 512,
    ttl: 1800,
    createTime: '2024-01-01 09:00:00',
    accessTime: '2024-01-01 10:25:00',
    accessCount: 8,
    status: 'valid'
  },
  {
    key: 'config:system',
    type: 'app',
    value: '{"theme":"dark","language":"zh-CN","timezone":"Asia/Shanghai"}',
    size: 256,
    ttl: 86400,
    createTime: '2024-01-01 08:00:00',
    accessTime: '2024-01-01 10:20:00',
    accessCount: 3,
    status: 'valid'
  },
  {
    key: 'static:css:main.css',
    type: 'static',
    value: 'body{font-family:Arial,sans-serif;margin:0;padding:0;}',
    size: 2048,
    ttl: 604800,
    createTime: '2024-01-01 07:00:00',
    accessTime: '2024-01-01 10:15:00',
    accessCount: 25,
    status: 'valid'
  },
  {
    key: 'temp:upload:file123',
    type: 'redis',
    value: '{"filename":"document.pdf","size":1048576,"path":"/uploads/temp/file123.pdf"}',
    size: 128,
    ttl: 300,
    createTime: '2024-01-01 10:00:00',
    accessTime: '2024-01-01 10:00:00',
    accessCount: 1,
    status: 'expiring'
  }
]

// 方法
const loadCacheList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    cacheList.value = mockCacheList
    pagination.total = mockCacheList.length
    
    ElMessage.success('缓存列表加载成功')
  } catch (error) {
    ElMessage.error('加载缓存列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadCacheList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.type = ''
  searchForm.status = ''
  handleSearch()
}

const handleRefresh = () => {
  loadCacheList()
}

const handleView = (cache) => {
  currentCache.value = cache
  showDetailDialog.value = true
}

const handleEdit = (cache) => {
  ElMessage.info('编辑缓存功能开发中...')
}

const handleDelete = async (cache) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除缓存"${cache.key}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('删除成功')
    loadCacheList()
  } catch {
    ElMessage.info('已取消删除')
  }
}

const handleSelectionChange = (selection) => {
  selectedCaches.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadCacheList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadCacheList()
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCaches.value.length} 个缓存吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('批量删除成功')
    loadCacheList()
  } catch {
    ElMessage.info('已取消批量删除')
  }
}

const batchExpire = () => {
  ElMessage.info('批量过期功能开发中...')
}

const batchRefresh = () => {
  ElMessage.info('批量刷新功能开发中...')
}

// 工具方法
const getTypeTagType = (type) => {
  const typeMap = {
    redis: 'danger',
    app: 'warning',
    static: 'info'
  }
  return typeMap[type] || 'info'
}

const getTypeText = (type) => {
  const typeMap = {
    redis: 'Redis',
    app: '应用缓存',
    static: '静态资源'
  }
  return typeMap[type] || '未知'
}

const getStatusTagType = (status) => {
  const statusMap = {
    valid: 'success',
    expired: 'danger',
    expiring: 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    valid: '有效',
    expired: '已过期',
    expiring: '即将过期'
  }
  return statusMap[status] || '未知'
}

const getValuePreview = (value) => {
  if (typeof value === 'string' && value.length > 50) {
    return value.substring(0, 50) + '...'
  }
  return value
}

const formatSize = (size) => {
  if (size < 1024) {
    return size + 'B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(1) + 'KB'
  } else {
    return (size / (1024 * 1024)).toFixed(1) + 'MB'
  }
}

const formatTtl = (ttl) => {
  if (ttl === -1) {
    return '永不过期'
  } else if (ttl < 60) {
    return ttl + '秒'
  } else if (ttl < 3600) {
    return Math.floor(ttl / 60) + '分钟'
  } else if (ttl < 86400) {
    return Math.floor(ttl / 3600) + '小时'
  } else {
    return Math.floor(ttl / 86400) + '天'
  }
}

const getTtlClass = (ttl) => {
  if (ttl === -1) {
    return 'ttl-permanent'
  } else if (ttl < 300) {
    return 'ttl-expiring'
  } else if (ttl < 3600) {
    return 'ttl-short'
  } else {
    return 'ttl-normal'
  }
}

// 生命周期
onMounted(() => {
  loadCacheList()
})
</script>

<style scoped>
.cache-list-container {
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

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-section {
  margin-top: 20px;
  text-align: right;
}

.batch-section {
  margin-bottom: 20px;
}

.batch-actions .el-button {
  margin-right: 10px;
}

.cache-detail {
  padding: 20px 0;
}

.value-preview {
  font-family: monospace;
  font-size: 12px;
}

.value-display {
  font-family: monospace;
  font-size: 12px;
}

.ttl-permanent {
  color: #67c23a;
  font-weight: bold;
}

.ttl-expiring {
  color: #f56c6c;
  font-weight: bold;
}

.ttl-short {
  color: #e6a23c;
}

.ttl-normal {
  color: #606266;
}
</style>
