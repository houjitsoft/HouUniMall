<template>
  <div class="error-logs">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>错误日志</span>
          <div>
            <el-button type="primary" @click="exportErrors">导出错误报告</el-button>
            <el-button type="danger" @click="clearErrorLogs">清空错误日志</el-button>
          </div>
        </div>
      </template>
      
      <!-- 错误统计概览 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value error">{{ errorStats.totalErrors }}</div>
              <div class="stat-label">总错误数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value warning">{{ errorStats.criticalErrors }}</div>
              <div class="stat-label">严重错误</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value info">{{ errorStats.unresolvedErrors }}</div>
              <div class="stat-label">未解决错误</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value success">{{ errorStats.todayErrors }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="业务审计错误（MySQL）" name="audit" />
        <el-tab-pane label="运行错误（ELK）" name="runtime" />
      </el-tabs>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="错误级别">
          <el-select v-model="searchForm.level" placeholder="请选择错误级别" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="CRITICAL" value="CRITICAL"></el-option>
            <el-option label="ERROR" value="ERROR"></el-option>
            <el-option label="WARN" value="WARN"></el-option>
            <el-option label="INFO" value="INFO"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="错误类型">
          <el-select v-model="searchForm.type" placeholder="请选择错误类型" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="数据库错误" value="database"></el-option>
            <el-option label="网络错误" value="network"></el-option>
            <el-option label="业务逻辑错误" value="business"></el-option>
            <el-option label="系统错误" value="system"></el-option>
            <el-option label="第三方服务错误" value="external"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="服务名称">
          <el-input v-model="searchForm.service" placeholder="请输入服务名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入错误关键词" clearable></el-input>
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
        <el-table-column prop="source" label="来源" width="100">
          <template #default="scope">
            <el-tag size="small" :type="scope.row.source === 'elk' ? 'warning' : 'primary'">
              {{ scope.row.source === 'elk' ? 'ELK' : '审计库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="级别" width="100">
          <template #default="scope">
            <el-tag :type="getLevelTagType(scope.row.level)">
              {{ scope.row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="错误类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="service" label="服务名称" width="150"></el-table-column>
        <el-table-column prop="message" label="错误信息" min-width="300"></el-table-column>
        <el-table-column prop="count" label="发生次数" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.count > 1" type="warning">{{ scope.row.count }}</el-tag>
            <span v-else>{{ scope.row.count }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'RESOLVED' ? 'success' : 'danger'">
              {{ scope.row.status === 'RESOLVED' ? '已解决' : '未解决' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.status !== 'RESOLVED'"
              size="small" 
              type="success" 
              @click="markResolved(scope.row)"
            >
              标记解决
            </el-button>
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
    
    <!-- 错误详情对话框 -->
    <el-dialog v-model="detailVisible" title="错误详情" width="85%">
      <div class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ logDetail.timestamp }}</el-descriptions-item>
          <el-descriptions-item label="级别">
            <el-tag :type="getLevelTagType(logDetail.level)">{{ logDetail.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="错误类型">
            <el-tag :type="getTypeTagType(logDetail.type)">{{ getTypeName(logDetail.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="服务名称">{{ logDetail.service }}</el-descriptions-item>
          <el-descriptions-item label="发生次数">
            <el-tag v-if="logDetail.count > 1" type="warning">{{ logDetail.count }}</el-tag>
            <span v-else>{{ logDetail.count }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="logDetail.status === 'RESOLVED' ? 'success' : 'danger'">
              {{ logDetail.status === 'RESOLVED' ? '已解决' : '未解决' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="错误信息" :span="2">{{ logDetail.message }}</el-descriptions-item>
          <el-descriptions-item label="异常类" :span="2">{{ logDetail.exceptionClass }}</el-descriptions-item>
          <el-descriptions-item label="堆栈信息" :span="2">
            <pre class="stack-trace">{{ logDetail.stackTrace }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="请求信息" :span="2">
            <pre class="json-content">{{ formatJson(logDetail.requestInfo) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="环境信息" :span="2">
            <pre class="json-content">{{ formatJson(logDetail.environmentInfo) }}</pre>
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
import { errorLogAPI, elkLogAPI } from '@/api/log/index.js'
import { unwrapPage, mapAuditErrorRow, mapElkRow, defaultDateRange, unwrap } from '@/utils/logApi.js'

const activeTab = ref('audit')

const errorStats = reactive({
  totalErrors:     0,
  criticalErrors:  0,
  unresolvedErrors: 0,
  todayErrors:     0
})

const searchForm = reactive({
  level: '',
  type: '',
  service: '',
  keyword: '',
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
 * @returns {Object} 包含 module、errorType、startTime、endTime、keyword 等字段的查询条件
 */
const buildParams = () => {
  const params = {}
  if (activeTab.value === 'audit') {
    if (searchForm.type)    params.errorType = searchForm.type
    if (searchForm.service) params.module    = searchForm.service
  } else {
    params.level = searchForm.level || 'ERROR'
    if (searchForm.service) params.service = searchForm.service
  }
  if (searchForm.keyword) params.keyword = searchForm.keyword
  if (searchForm.dateRange?.length === 2) {
    params.startTime = searchForm.dateRange[0]
    params.endTime   = searchForm.dateRange[1]
  } else if (activeTab.value === 'runtime') {
    const [s, e] = defaultDateRange(24)
    params.startTime = s
    params.endTime = e
  }
  return params
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = buildParams()
    if (activeTab.value === 'runtime') {
      const page = unwrapPage(await elkLogAPI.getPage(params, currentPage.value, pageSize.value))
      logList.value = (page.records || []).map(mapElkRow)
      total.value = page.total || 0
    } else {
      const page = unwrapPage(await errorLogAPI.getPage(params, currentPage.value, pageSize.value))
      logList.value = (page.records || []).map(mapAuditErrorRow)
      total.value = page.total || 0
    }
  } catch (error) {
    console.error('查询错误日志失败:', error)
    ElMessage.error(error.message || '查询错误日志失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  currentPage.value = 1
  if (activeTab.value === 'runtime' && !searchForm.dateRange?.length) {
    searchForm.dateRange = defaultDateRange(24)
  }
  fetchLogs()
}

const getLevelTagType = (level) => {
  const typeMap = {
    'CRITICAL': 'danger',
    'ERROR': 'danger',
    'WARN': 'warning',
    'INFO': 'success'
  }
  return typeMap[level] || ''
}

const getTypeTagType = (type) => {
  const typeMap = {
    'database': 'danger',
    'network': 'warning',
    'business': 'primary',
    'system': 'info',
    'external': 'warning'
  }
  return typeMap[type] || ''
}

const getTypeName = (type) => {
  const nameMap = {
    'database': '数据库错误',
    'network': '网络错误',
    'business': '业务逻辑错误',
    'system': '系统错误',
    'external': '第三方服务错误'
  }
  return nameMap[type] || type
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
  searchForm.level     = ''
  searchForm.type      = ''
  searchForm.service   = ''
  searchForm.keyword   = ''
  searchForm.dateRange = []
  currentPage.value    = 1
  fetchLogs()
}

/**
 * 查看错误日志详情
 * @param {Object} row 当前行数据
 */
const viewDetail = (row) => {
  logDetail.value     = row
  detailVisible.value = true
}

/**
 * 标记错误为已解决（仅更新前端显示状态）
 * @param {Object} row 当前行数据
 */
const markResolved = async (row) => {
  try {
    await ElMessageBox.confirm('确定要标记此错误为已解决吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    row.status = 'RESOLVED'
    ElMessage.success('错误已标记为已解决')
  } catch {
    ElMessage.info('已取消操作')
  }
}

/**
 * 导出错误日志（暂时展示提示）
 */
const exportErrors = () => {
  ElMessage.info('导出功能尚在开发中，敢请期待')
}

/**
 * 清空错误日志，删除超过 30 天的日志记录（二次确认）
 */
const clearErrorLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有错误日志吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    loading.value = true
    unwrap(await errorLogAPI.clean(30))
    ElMessage.success('错误日志清空成功')
    fetchLogs()
  } catch (error) {
    if (error !== 'cancel' && error?.message) {
      console.error('清空错误日志失败:', error)
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
.error-logs {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-value.error {
  color: #F56C6C;
}

.stat-value.warning {
  color: #E6A23C;
}

.stat-value.info {
  color: #909399;
}

.stat-value.success {
  color: #67C23A;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.search-form {
  margin-bottom: 20px;
}

.log-detail {
  padding: 20px;
}

.stack-trace {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
  max-height: 300px;
  overflow-y: auto;
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
</style>
