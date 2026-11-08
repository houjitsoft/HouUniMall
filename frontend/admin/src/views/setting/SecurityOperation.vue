<template>
  <div class="security-operation">
    <div class="page-header">
      <h2>操作日志</h2>
      <div class="header-actions">
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="danger" @click="handleClearLogs">
          <el-icon><Delete /></el-icon>
          清空日志
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="请选择操作类型" clearable>
            <el-option label="登录" value="login" />
            <el-option label="登出" value="logout" />
            <el-option label="新增" value="create" />
            <el-option label="修改" value="update" />
            <el-option label="删除" value="delete" />
            <el-option label="查询" value="query" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作日志列表 -->
    <el-card>
      <el-table
        :data="logList"
        :loading="loading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)">
              {{ getOperationTypeText(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationDesc" label="操作描述" min-width="200" />
        <el-table-column prop="requestMethod" label="请求方法" width="100">
          <template #default="scope">
            <el-tag :type="getMethodTag(scope.row.requestMethod)">
              {{ scope.row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="userAgent" label="用户代理" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operationTime" label="操作时间" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleViewDetail(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="操作日志详情"
      width="800px"
    >
      <div v-if="currentLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">
            {{ currentLog.username }}
          </el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTypeTag(currentLog.operationType)">
              {{ getOperationTypeText(currentLog.operationType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作描述">
            {{ currentLog.operationDesc }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag :type="getMethodTag(currentLog.requestMethod)">
              {{ currentLog.requestMethod }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求URL">
            {{ currentLog.requestUrl }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ currentLog.ipAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="操作时间">
            {{ currentLog.operationTime }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentLog.status === 'success' ? 'success' : 'danger'">
              {{ currentLog.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户代理" :span="2">
            {{ currentLog.userAgent }}
          </el-descriptions-item>
          <el-descriptions-item label="请求参数" :span="2">
            <pre class="json-content">{{ formatJson(currentLog.requestParams) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="响应结果" :span="2">
            <pre class="json-content">{{ formatJson(currentLog.responseResult) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Delete, Search } from '@element-plus/icons-vue'
import { operationLogAPI } from '@/api/log/index.js'
import { unwrapPage, mapOperationRow, unwrap } from '@/utils/logApi.js'

// 数据
const loading = ref(false)
const showDetailDialog = ref(false)
const currentLog = ref(null)
const logList = ref([])

const searchForm = reactive({
  username: '',
  operationType: '',
  dateRange: []
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

/**
 * 根据当前搜索表单构建请求参数
 * @returns {Object} 包含 username、operation、startTime、endTime 等字段的查询条件
 */
const buildParams = () => {
  const params = {}
  if (searchForm.username)      params.username  = searchForm.username
  if (searchForm.operationType) params.operation = searchForm.operationType
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.startTime = searchForm.dateRange[0]
    params.endTime   = searchForm.dateRange[1]
  }
  return params
}

/**
 * 分页加载操作日志列表
 * 使用当前搜索条件和分页参数进行查询
 */
const loadLogList = async () => {
  try {
    loading.value = true
    const page = unwrapPage(await operationLogAPI.getPage(buildParams(), pagination.current, pagination.size))
    logList.value = (page.records || []).map(mapOperationRow)
    pagination.total = page.total || 0
  } catch (error) {
    console.error('加载操作日志失败:', error)
    ElMessage.error(error.message || '加载操作日志失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 点击搜索按钮，重置页码并重新加载
 */
const handleSearch = () => {
  pagination.current = 1
  loadLogList()
}

/**
 * 重置搜索表单并重新加载数据
 */
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    if (key === 'dateRange') {
      searchForm[key] = []
    } else {
      searchForm[key] = ''
    }
  })
  pagination.current = 1
  loadLogList()
}

/**
 * 刷新当前列表数据
 */
const handleRefresh = () => {
  loadLogList()
}

/**
 * 清空操作日志（保留最近 30 天，二次确认）
 */
const handleClearLogs = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有操作日志吗？此操作不可恢复！',
      '确认清空',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    loading.value = true
    unwrap(await operationLogAPI.clean(30))
    ElMessage.success('清空成功')
    pagination.current = 1
    loadLogList()
  } catch (error) {
    if (error !== 'cancel' && error?.message) {
      console.error('清空日志失败:', error)
      ElMessage.error('清空失败: ' + error.message)
    }
  } finally {
    loading.value = false
  }
}

/**
 * 查看日志详情
 * @param {Object} log 日志行数据
 */
const handleViewDetail = (log) => {
  currentLog.value      = log
  showDetailDialog.value = true
}

/**
 * 分页尺寸变更处理
 * @param {number} size 新的每页条数
 */
const handleSizeChange = (size) => {
  pagination.size    = size
  pagination.current = 1
  loadLogList()
}

/**
 * 当前页码变更处理
 * @param {number} current 新的页码
 */
const handleCurrentChange = (current) => {
  pagination.current = current
  loadLogList()
}

const getOperationTypeTag = (type) => {
  const typeMap = {
    'login': 'success',
    'logout': 'info',
    'create': 'primary',
    'update': 'warning',
    'delete': 'danger',
    'query': 'info'
  }
  return typeMap[type] || 'info'
}

const getOperationTypeText = (type) => {
  const typeMap = {
    'login': '登录',
    'logout': '登出',
    'create': '新增',
    'update': '修改',
    'delete': '删除',
    'query': '查询'
  }
  return typeMap[type] || '未知'
}

const getMethodTag = (method) => {
  const methodMap = {
    'GET': 'success',
    'POST': 'primary',
    'PUT': 'warning',
    'DELETE': 'danger',
    'PATCH': 'info'
  }
  return methodMap[method] || 'info'
}

/**
 * 将对象格式化为缩进的 JSON 字符串，用于详情面板展示
 * @param {Object|string} obj 待格式化的对象
 * @returns {string} 格式化后的字符串
 */
const formatJson = (obj) => {
  if (!obj) return ''
  try {
    return typeof obj === 'string' ? obj : JSON.stringify(obj, null, 2)
  } catch (error) {
    return String(obj)
  }
}

onMounted(() => {
  loadLogList()
})
</script>

<style scoped>
.security-operation {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.log-detail {
  max-height: 500px;
  overflow-y: auto;
}

.json-content {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
