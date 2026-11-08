<template>
  <div class="access-logs">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>访问日志</span>
          <el-button type="danger" @click="clearAccessLogs">清空访问日志</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="IP地址">
          <el-input v-model="searchForm.ipAddress" placeholder="请输入IP地址" clearable></el-input>
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable></el-input>
        </el-form-item>
        <el-form-item label="请求路径">
          <el-input v-model="searchForm.path" placeholder="请输入请求路径" clearable></el-input>
        </el-form-item>
        <el-form-item label="响应状态">
          <el-select v-model="searchForm.status" placeholder="请选择响应状态" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="200" value="200"></el-option>
            <el-option label="404" value="404"></el-option>
            <el-option label="500" value="500"></el-option>
            <el-option label="其他" value="other"></el-option>
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
          <el-button type="primary" @click="searchLogs">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="logList" v-loading="loading" style="width: 100%" max-height="500">
        <el-table-column prop="timestamp" label="时间" width="180"></el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="140"></el-table-column>
        <el-table-column prop="userId" label="用户ID" width="120"></el-table-column>
        <el-table-column prop="userName" label="用户名" width="120"></el-table-column>
        <el-table-column prop="method" label="请求方法" width="100">
          <template #default="scope">
            <el-tag :type="getMethodTagType(scope.row.method)">
              {{ scope.row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="请求路径" min-width="200"></el-table-column>
        <el-table-column prop="status" label="响应状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseTime" label="响应时间" width="100">
          <template #default="scope">
            <span :class="getResponseTimeClass(scope.row.responseTime)">
              {{ scope.row.responseTime }}ms
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="userAgent" label="用户代理" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>
    
    <!-- 访问日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="访问日志详情" width="80%">
      <div class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ logDetail.timestamp }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ logDetail.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ logDetail.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ logDetail.userName }}</el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag :type="getMethodTagType(logDetail.method)">
              {{ logDetail.method }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应状态">
            <el-tag :type="getStatusTagType(logDetail.status)">
              {{ logDetail.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应时间">
            <span :class="getResponseTimeClass(logDetail.responseTime)">
              {{ logDetail.responseTime }}ms
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="请求大小">{{ logDetail.requestSize }} bytes</el-descriptions-item>
          <el-descriptions-item label="响应大小">{{ logDetail.responseSize }} bytes</el-descriptions-item>
          <el-descriptions-item label="请求路径" :span="2">{{ logDetail.path }}</el-descriptions-item>
          <el-descriptions-item label="完整URL" :span="2">{{ logDetail.fullUrl }}</el-descriptions-item>
          <el-descriptions-item label="用户代理" :span="2">{{ logDetail.userAgent }}</el-descriptions-item>
          <el-descriptions-item label="请求头" :span="2">
            <pre class="json-content">{{ formatJson(logDetail.requestHeaders) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="响应头" :span="2">
            <pre class="json-content">{{ formatJson(logDetail.responseHeaders) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { accessLogAPI } from '@/api/log/index.js'
import { unwrapPage, mapAccessRow, unwrap } from '@/utils/logApi.js'

const searchForm = reactive({
  ipAddress: '',
  userId: '',
  path: '',
  status: '',
  dateRange: []
})

const logList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const detailVisible = ref(false)
const logDetail = ref({})

/**
 * 根据当前搜索表单构建请求参数
 * @returns {Object} 包含 ip、path、method、statusCode、startTime、endTime 的查询条件对象
 */
const buildParams = () => {
  const params = {}
  if (searchForm.ipAddress) params.ip        = searchForm.ipAddress
  if (searchForm.userId)    params.username   = searchForm.userId
  if (searchForm.path)      params.path       = searchForm.path
  if (searchForm.status)    params.statusCode = searchForm.status
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.startTime = searchForm.dateRange[0]
    params.endTime   = searchForm.dateRange[1]
  }
  return params
}

/**
 * 分页加载访问日志列表
 * 使用当前搜索条件和分页参数进行查询
 */
const fetchLogs = async () => {
  loading.value = true
  try {
    const params = buildParams()
    const page = unwrapPage(await accessLogAPI.getPage(params, currentPage.value, pageSize.value))
    logList.value = (page.records || []).map(mapAccessRow)
    total.value = page.total || 0
  } catch (error) {
    console.error('查询访问日志失败:', error)
    ElMessage.error(error.message || '查询访问日志失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const getMethodTagType = (method) => {
  const typeMap = {
    'GET': 'success',
    'POST': 'primary',
    'PUT': 'warning',
    'DELETE': 'danger',
    'PATCH': 'info'
  }
  return typeMap[method] || ''
}

const getStatusTagType = (status) => {
  if (status >= 200 && status < 300) return 'success'
  if (status >= 300 && status < 400) return 'warning'
  if (status >= 400) return 'danger'
  return ''
}

const getResponseTimeClass = (responseTime) => {
  if (responseTime < 100) return 'fast-response'
  if (responseTime < 500) return 'normal-response'
  return 'slow-response'
}

/**
 * 将对象格式化为缩进的 JSON 字符串
 * @param {Object|string} obj 待格式化的对象
 * @returns {string} 格式化后的字符串
 */
const formatJson = (obj) => {
  if (!obj) return ''
  try {
    return typeof obj === 'string' ? obj : JSON.stringify(obj, null, 2)
  } catch {
    return String(obj)
  }
}

/**
 * 点击搜索按鈕，重置页码并重新加载
 */
const searchLogs = () => {
  currentPage.value = 1
  fetchLogs()
}

/**
 * 重置搜索表单并重新加载
 */
const resetSearch = () => {
  searchForm.ipAddress = ''
  searchForm.userId    = ''
  searchForm.path      = ''
  searchForm.status    = ''
  searchForm.dateRange = []
  currentPage.value    = 1
  fetchLogs()
}

/**
 * 查看详情
 * @param {Object} row 当前行数据
 */
const viewDetail = (row) => {
  logDetail.value     = row
  detailVisible.value = true
}

/**
 * 清空访问日志，删除超过 30 天的日志记录（二次确认）
 */
const clearAccessLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有访问日志吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    loading.value = true
    unwrap(await accessLogAPI.clean(30))
    ElMessage.success('访问日志清空成功')
    fetchLogs()
  } catch (error) {
    if (error !== 'cancel' && error?.message) {
      console.error('清空访问日志失败:', error)
      ElMessage.error('清空失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 分页尺寸变化处理
 * @param {number} val 新的每页条数
 */
const handleSizeChange = (val) => {
  pageSize.value    = val
  currentPage.value = 1
  fetchLogs()
}

/**
 * 当前页码变化处理
 * @param {number} val 新的页码
 */
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchLogs()
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.access-logs {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.log-detail {
  padding: 20px;
}

.json-content {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  max-height: 200px;
  overflow-y: auto;
}

.fast-response {
  color: #67C23A;
  font-weight: bold;
}

.normal-response {
  color: #E6A23C;
  font-weight: bold;
}

.slow-response {
  color: #F56C6C;
  font-weight: bold;
}
</style>
