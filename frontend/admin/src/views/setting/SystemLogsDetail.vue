<template>
  <div class="system-logs-detail">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>系统日志</span>
          <div>
            <el-tag type="info" style="margin-right: 8px">数据源：ELK / Elasticsearch</el-tag>
            <el-button type="primary" @click="exportLogs">导出日志</el-button>
          </div>
        </div>
      </template>

      <el-alert
        v-if="elkError"
        type="warning"
        :closable="false"
        show-icon
        :title="elkError"
        style="margin-bottom: 12px"
      />
      <el-alert
        v-else
        type="info"
        :closable="false"
        show-icon
        title="默认查询最近 24 小时运行日志；时间范围最长 7 天，避免大范围检索影响性能。"
        style="margin-bottom: 12px"
      />
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="日志级别">
          <el-select v-model="searchForm.level" placeholder="请选择日志级别" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="DEBUG" value="DEBUG"></el-option>
            <el-option label="INFO" value="INFO"></el-option>
            <el-option label="WARN" value="WARN"></el-option>
            <el-option label="ERROR" value="ERROR"></el-option>
            <el-option label="FATAL" value="FATAL"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="服务名">
          <el-input v-model="searchForm.service" placeholder="如 oa / bff-web" clearable></el-input>
        </el-form-item>
        <el-form-item label="日志器">
          <el-input v-model="searchForm.logger" placeholder="Logger 名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable></el-input>
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
        <el-table-column prop="level" label="级别" width="80">
          <template #default="scope">
            <el-tag :type="getLevelTagType(scope.row.level)">
              {{ scope.row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="logger" label="日志器" width="250" show-overflow-tooltip></el-table-column>
        <el-table-column prop="message" label="消息" min-width="300" show-overflow-tooltip></el-table-column>
        <el-table-column prop="service" label="服务" width="120" show-overflow-tooltip></el-table-column>
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
    
    <!-- 系统日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="系统日志详情" width="85%">
      <div class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ logDetail.timestamp }}</el-descriptions-item>
          <el-descriptions-item label="级别">
            <el-tag :type="getLevelTagType(logDetail.level)">{{ logDetail.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="日志器" :span="2">{{ logDetail.logger }}</el-descriptions-item>
          <el-descriptions-item label="线程">{{ logDetail.thread }}</el-descriptions-item>
          <el-descriptions-item label="行号">{{ logDetail.lineNumber }}</el-descriptions-item>
          <el-descriptions-item label="方法名">{{ logDetail.methodName }}</el-descriptions-item>
          <el-descriptions-item label="类名">{{ logDetail.className }}</el-descriptions-item>
          <el-descriptions-item label="消息" :span="2">{{ logDetail.message }}</el-descriptions-item>
          
          <el-descriptions-item v-if="logDetail.stackTrace" label="堆栈信息" :span="2">
            <pre class="stack-trace">{{ logDetail.stackTrace }}</pre>
          </el-descriptions-item>
          
          <el-descriptions-item v-if="logDetail.throwable" label="异常信息" :span="2">
            <pre class="stack-trace">{{ logDetail.throwable }}</pre>
          </el-descriptions-item>
          
          <el-descriptions-item v-if="logDetail.mdc" label="MDC信息" :span="2">
            <pre class="json-content">{{ formatJson(logDetail.mdc) }}</pre>
          </el-descriptions-item>
          
          <el-descriptions-item label="主机名">{{ logDetail.hostName }}</el-descriptions-item>
          <el-descriptions-item label="进程ID">{{ logDetail.processId }}</el-descriptions-item>
          <el-descriptions-item label="应用名称">{{ logDetail.applicationName }}</el-descriptions-item>
          <el-descriptions-item label="版本号">{{ logDetail.version }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { elkLogAPI } from '@/api/log/index.js'
import { unwrapPage, defaultDateRange, mapElkRow } from '@/utils/logApi.js'

const searchForm = reactive({
  level: '',
  service: '',
  logger: '',
  keyword: '',
  dateRange: defaultDateRange(24)
})

const logList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const elkError = ref('')

const detailVisible = ref(false)
const logDetail = ref({})

const buildParams = () => {
  const params = {}
  if (searchForm.level)   params.level   = searchForm.level
  if (searchForm.service) params.service = searchForm.service
  if (searchForm.logger)  params.logger  = searchForm.logger
  if (searchForm.keyword) params.keyword = searchForm.keyword
  if (searchForm.dateRange?.length === 2) {
    params.startTime = searchForm.dateRange[0]
    params.endTime   = searchForm.dateRange[1]
  } else {
    const [s, e] = defaultDateRange(24)
    params.startTime = s
    params.endTime = e
  }
  return params
}

const fetchLogs = async () => {
  loading.value = true
  elkError.value = ''
  try {
    const params = buildParams()
    const page = unwrapPage(await elkLogAPI.getPage(params, currentPage.value, pageSize.value))
    if (page.elkError) elkError.value = page.elkError
    logList.value = (page.records || []).map(mapElkRow)
    total.value = page.total || 0
  } catch (error) {
    console.error('查询系统日志失败:', error)
    ElMessage.error(error.message || '查询系统日志失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const getLevelTagType = (level) => {
  const typeMap = {
    'DEBUG': '',
    'INFO': 'success',
    'WARN': 'warning',
    'ERROR': 'danger',
    'FATAL': 'danger'
  }
  return typeMap[level] || ''
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
  searchForm.level = ''
  searchForm.service = ''
  searchForm.logger = ''
  searchForm.keyword = ''
  searchForm.dateRange = defaultDateRange(24)
  currentPage.value = 1
  fetchLogs()
}

/**
 * 查看系统日志详情
 * @param {Object} row 当前行数据
 */
const viewDetail = (row) => {
  logDetail.value     = row
  detailVisible.value = true
}

/**
 * 导出系统日志（暂时展示提示）
 */
const exportLogs = () => {
  ElMessage.info('请在 Kibana 导出大批量运行日志；此处列表仅做近 7 天内快速检索')
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
.system-logs-detail {
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
