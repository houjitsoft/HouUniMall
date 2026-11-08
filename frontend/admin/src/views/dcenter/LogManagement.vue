<template>
  <div class="log-management">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 系统日志 -->
      <el-tab-pane label="系统日志" name="system">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>系统日志</span>
              <div class="header-actions">
                <el-button type="success" @click="exportLogs('system')">导出日志</el-button>
                <el-button type="danger" @click="clearLogs('system')">清空日志</el-button>
              </div>
            </div>
          </template>
          
          <!-- 搜索栏 -->
          <el-form :inline="true" :model="systemLogSearch" class="search-form">
            <el-form-item label="日志级别">
              <el-select v-model="systemLogSearch.level" placeholder="请选择级别" clearable>
                <el-option label="INFO" value="INFO" />
                <el-option label="WARN" value="WARN" />
                <el-option label="ERROR" value="ERROR" />
                <el-option label="DEBUG" value="DEBUG" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="systemLogSearch.dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchSystemLogs">搜索</el-button>
              <el-button @click="resetSystemLogSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 系统日志列表 -->
          <el-table :data="systemLogs" style="width: 100%" v-loading="systemLogLoading">
            <el-table-column prop="timestamp" label="时间" width="180" />
            <el-table-column prop="level" label="级别" width="100">
              <template #default="scope">
                <el-tag :type="getLogLevelType(scope.row.level)">
                  {{ scope.row.level }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="logger" label="日志器" width="150" />
            <el-table-column prop="message" label="消息" />
            <el-table-column prop="thread" label="线程" width="120" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="viewLogDetail(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="systemLogPage"
              v-model:page-size="systemLogPageSize"
              :page-sizes="[20, 50, 100, 200]"
              :total="systemLogTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSystemLogSizeChange"
              @current-change="handleSystemLogCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 操作日志 -->
      <el-tab-pane label="操作日志" name="operation">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>操作日志</span>
              <div class="header-actions">
                <el-button type="success" @click="exportLogs('operation')">导出日志</el-button>
                <el-button type="danger" @click="clearLogs('operation')">清空日志</el-button>
              </div>
            </div>
          </template>
          
          <!-- 搜索栏 -->
          <el-form :inline="true" :model="operationLogSearch" class="search-form">
            <el-form-item label="操作用户">
              <el-input v-model="operationLogSearch.username" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item label="操作类型">
              <el-select v-model="operationLogSearch.operationType" placeholder="请选择类型" clearable>
                <el-option label="登录" value="LOGIN" />
                <el-option label="新增" value="CREATE" />
                <el-option label="修改" value="UPDATE" />
                <el-option label="删除" value="DELETE" />
                <el-option label="查询" value="QUERY" />
                <el-option label="导出" value="EXPORT" />
              </el-select>
            </el-form-item>
            <el-form-item label="操作时间">
              <el-date-picker
                v-model="operationLogSearch.dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchOperationLogs">搜索</el-button>
              <el-button @click="resetOperationLogSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 操作日志列表 -->
          <el-table :data="operationLogs" style="width: 100%" v-loading="operationLogLoading">
            <el-table-column prop="timestamp" label="操作时间" width="180" />
            <el-table-column prop="username" label="操作用户" width="120" />
            <el-table-column prop="operationType" label="操作类型" width="100">
              <template #default="scope">
                <el-tag :type="getOperationTypeType(scope.row.operationType)">
                  {{ getOperationTypeText(scope.row.operationType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="module" label="操作模块" width="120" />
            <el-table-column prop="description" label="操作描述" />
            <el-table-column prop="ip" label="IP地址" width="120" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="viewLogDetail(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="operationLogPage"
              v-model:page-size="operationLogPageSize"
              :page-sizes="[20, 50, 100, 200]"
              :total="operationLogTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleOperationLogSizeChange"
              @current-change="handleOperationLogCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 错误日志 -->
      <el-tab-pane label="错误日志" name="error">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>错误日志</span>
              <div class="header-actions">
                <el-button type="success" @click="exportLogs('error')">导出日志</el-button>
                <el-button type="danger" @click="clearLogs('error')">清空日志</el-button>
              </div>
            </div>
          </template>
          
          <!-- 搜索栏 -->
          <el-form :inline="true" :model="errorLogSearch" class="search-form">
            <el-form-item label="错误类型">
              <el-select v-model="errorLogSearch.errorType" placeholder="请选择类型" clearable>
                <el-option label="系统异常" value="SYSTEM_ERROR" />
                <el-option label="业务异常" value="BUSINESS_ERROR" />
                <el-option label="网络异常" value="NETWORK_ERROR" />
                <el-option label="数据库异常" value="DATABASE_ERROR" />
              </el-select>
            </el-form-item>
            <el-form-item label="错误时间">
              <el-date-picker
                v-model="errorLogSearch.dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchErrorLogs">搜索</el-button>
              <el-button @click="resetErrorLogSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 错误日志列表 -->
          <el-table :data="errorLogs" style="width: 100%" v-loading="errorLogLoading">
            <el-table-column prop="timestamp" label="错误时间" width="180" />
            <el-table-column prop="errorType" label="错误类型" width="120">
              <template #default="scope">
                <el-tag type="danger">{{ getErrorTypeText(scope.row.errorType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMessage" label="错误信息" />
            <el-table-column prop="stackTrace" label="堆栈信息" width="100">
              <template #default="scope">
                <el-button size="small" @click="viewStackTrace(scope.row)">查看</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="user" label="触发用户" width="120" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="viewLogDetail(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="errorLogPage"
              v-model:page-size="errorLogPageSize"
              :page-sizes="[20, 50, 100, 200]"
              :total="errorLogTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleErrorLogSizeChange"
              @current-change="handleErrorLogCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="logDetailVisible"
      title="日志详情"
      width="800px"
    >
      <div v-if="currentLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ currentLog.timestamp }}</el-descriptions-item>
          <el-descriptions-item label="级别">{{ currentLog.level }}</el-descriptions-item>
          <el-descriptions-item label="日志器">{{ currentLog.logger }}</el-descriptions-item>
          <el-descriptions-item label="线程">{{ currentLog.thread }}</el-descriptions-item>
          <el-descriptions-item label="消息" :span="2">{{ currentLog.message }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="logDetailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 堆栈信息对话框 -->
    <el-dialog
      v-model="stackTraceVisible"
      title="堆栈信息"
      width="1000px"
    >
      <div v-if="currentErrorLog" class="stack-trace">
        <pre>{{ currentErrorLog.stackTrace }}</pre>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stackTraceVisible = false">关闭</el-button>
        </span>
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

// 响应式数据
const activeTab = ref('system')
const logDetailVisible = ref(false)
const stackTraceVisible = ref(false)
const currentLog = ref(null)
const currentErrorLog = ref(null)

// 系统日志相关
const systemLogLoading = ref(false)
const systemLogPage = ref(1)
const systemLogPageSize = ref(20)
const systemLogTotal = ref(0)
const systemLogSearch = reactive({
  level: '',
  dateRange: []
})

// 操作日志相关
const operationLogLoading = ref(false)
const operationLogPage = ref(1)
const operationLogPageSize = ref(20)
const operationLogTotal = ref(0)
const operationLogSearch = reactive({
  username: '',
  operationType: '',
  dateRange: []
})

// 错误日志相关
const errorLogLoading = ref(false)
const errorLogPage = ref(1)
const errorLogPageSize = ref(20)
const errorLogTotal = ref(0)
const errorLogSearch = reactive({
  errorType: '',
  dateRange: []
})

// 模拟日志数据
const systemLogs = ref([
  {
    timestamp: '2025-01-27 10:00:00',
    level: 'INFO',
    logger: 'com.houeoc.system.SystemService',
    message: '系统启动成功',
    thread: 'main'
  },
  {
    timestamp: '2025-01-27 10:01:00',
    level: 'WARN',
    logger: 'com.houeoc.system.MemoryMonitor',
    message: '内存使用率超过80%',
    thread: 'monitor-thread'
  },
  {
    timestamp: '2025-01-27 10:02:00',
    level: 'ERROR',
    logger: 'com.houeoc.system.DatabaseService',
    message: '数据库连接失败',
    thread: 'db-thread'
  }
])

const operationLogs = ref([
  {
    timestamp: '2025-01-27 10:00:00',
    username: 'admin',
    operationType: 'LOGIN',
    module: '用户管理',
    description: '用户登录系统',
    ip: '192.168.1.100'
  },
  {
    timestamp: '2025-01-27 10:01:00',
    username: 'admin',
    operationType: 'CREATE',
    module: '商品管理',
    description: '新增商品：iPhone 15 Pro',
    ip: '192.168.1.100'
  },
  {
    timestamp: '2025-01-27 10:02:00',
    username: 'manager',
    operationType: 'UPDATE',
    module: '订单管理',
    description: '修改订单状态：ORD001',
    ip: '192.168.1.101'
  }
])

const errorLogs = ref([
  {
    timestamp: '2025-01-27 10:00:00',
    errorType: 'SYSTEM_ERROR',
    errorMessage: '系统内存不足',
    stackTrace: 'java.lang.OutOfMemoryError: Java heap space\n\tat java.util.Arrays.copyOf(Arrays.java:3332)\n\tat java.lang.AbstractStringBuilder.expandCapacity(AbstractStringBuilder.java:137)\n\tat java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:121)',
    user: 'system'
  },
  {
    timestamp: '2025-01-27 10:01:00',
    errorType: 'DATABASE_ERROR',
    errorMessage: '数据库连接超时',
    stackTrace: 'java.sql.SQLException: Connection timed out\n\tat com.mysql.cj.jdbc.ConnectionImpl.connectOneTryOnly(ConnectionImpl.java:2260)\n\tat com.mysql.cj.jdbc.ConnectionImpl.createNewIO(ConnectionImpl.java:2085)',
    user: 'admin'
  }
])

// 生命周期
onMounted(() => {
  fetchSystemLogs()
  fetchOperationLogs()
  fetchErrorLogs()
})

// 获取日志级别类型
const getLogLevelType = (level) => {
  const levelMap = {
    'INFO': 'success',
    'WARN': 'warning',
    'ERROR': 'danger',
    'DEBUG': 'info'
  }
  return levelMap[level] || 'info'
}

// 获取操作类型类型
const getOperationTypeType = (type) => {
  const typeMap = {
    'LOGIN': 'success',
    'CREATE': 'primary',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'QUERY': 'info',
    'EXPORT': 'success'
  }
  return typeMap[type] || 'info'
}

// 获取操作类型文本
const getOperationTypeText = (type) => {
  const typeMap = {
    'LOGIN': '登录',
    'CREATE': '新增',
    'UPDATE': '修改',
    'DELETE': '删除',
    'QUERY': '查询',
    'EXPORT': '导出'
  }
  return typeMap[type] || type
}

// 获取错误类型文本
const getErrorTypeText = (type) => {
  const typeMap = {
    'SYSTEM_ERROR': '系统异常',
    'BUSINESS_ERROR': '业务异常',
    'NETWORK_ERROR': '网络异常',
    'DATABASE_ERROR': '数据库异常'
  }
  return typeMap[type] || type
}

// 系统日志相关方法
const fetchSystemLogs = () => {
  systemLogLoading.value = true
  setTimeout(() => {
    systemLogTotal.value = systemLogs.value.length
    systemLogLoading.value = false
  }, 500)
}

const searchSystemLogs = () => {
  systemLogPage.value = 1
  fetchSystemLogs()
}

const resetSystemLogSearch = () => {
  systemLogSearch.level = ''
  systemLogSearch.dateRange = []
  searchSystemLogs()
}

const handleSystemLogSizeChange = (val) => {
  systemLogPageSize.value = val
  fetchSystemLogs()
}

const handleSystemLogCurrentChange = (val) => {
  systemLogPage.value = val
  fetchSystemLogs()
}

// 操作日志相关方法
const fetchOperationLogs = () => {
  operationLogLoading.value = true
  setTimeout(() => {
    operationLogTotal.value = operationLogs.value.length
    operationLogLoading.value = false
  }, 500)
}

const searchOperationLogs = () => {
  operationLogPage.value = 1
  fetchOperationLogs()
}

const resetOperationLogSearch = () => {
  operationLogSearch.username = ''
  operationLogSearch.operationType = ''
  operationLogSearch.dateRange = []
  searchOperationLogs()
}

const handleOperationLogSizeChange = (val) => {
  operationLogPageSize.value = val
  fetchOperationLogs()
}

const handleOperationLogCurrentChange = (val) => {
  operationLogPage.value = val
  fetchOperationLogs()
}

// 错误日志相关方法
const fetchErrorLogs = () => {
  errorLogLoading.value = true
  setTimeout(() => {
    errorLogTotal.value = errorLogs.value.length
    errorLogLoading.value = false
  }, 500)
}

const searchErrorLogs = () => {
  errorLogPage.value = 1
  fetchErrorLogs()
}

const resetErrorLogSearch = () => {
  errorLogSearch.errorType = ''
  errorLogSearch.dateRange = []
  searchErrorLogs()
}

const handleErrorLogSizeChange = (val) => {
  errorLogPageSize.value = val
  fetchErrorLogs()
}

const handleErrorLogCurrentChange = (val) => {
  errorLogPage.value = val
  fetchErrorLogs()
}

// 通用方法
const viewLogDetail = (log) => {
  currentLog.value = log
  logDetailVisible.value = true
}

const viewStackTrace = (errorLog) => {
  currentErrorLog.value = errorLog
  stackTraceVisible.value = true
}

const exportLogs = (type) => {
  ElMessage.info(`${type}日志导出功能开发中...`)
}

const clearLogs = (type) => {
  ElMessageBox.confirm(`确定要清空${type}日志吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success(`${type}日志清空成功`)
  })
}
</script>

<style scoped>
.log-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.log-detail {
  padding: 20px 0;
}

.stack-trace {
  max-height: 400px;
  overflow-y: auto;
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
}

.stack-trace pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
}

.dialog-footer {
  text-align: right;
}
</style>
