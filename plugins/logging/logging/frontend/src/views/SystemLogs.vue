<template>
  <div class="system-logs">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>{{ pageTitle }}</span>
          <el-button type="danger" @click="clearLogs">清空日志</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="日志级别">
          <el-select v-model="searchForm.level" placeholder="请选择日志级别" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="DEBUG" value="DEBUG"></el-option>
            <el-option label="INFO" value="INFO"></el-option>
            <el-option label="WARN" value="WARN"></el-option>
            <el-option label="ERROR" value="ERROR"></el-option>
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
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchLogs">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="logList" style="width: 100%" max-height="500">
        <el-table-column prop="timestamp" label="时间" width="180"></el-table-column>
        <el-table-column prop="level" label="级别" width="80">
          <template #default="scope">
            <el-tag :type="getLevelTagType(scope.row.level)">
              {{ scope.row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="logger" label="日志器" width="200"></el-table-column>
        <el-table-column prop="message" label="消息" min-width="300"></el-table-column>
        <el-table-column prop="thread" label="线程" width="120"></el-table-column>
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
    
    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailVisible" :title="`${pageTitle}详情`" width="80%">
      <div class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ logDetail.timestamp }}</el-descriptions-item>
          <el-descriptions-item label="级别">
            <el-tag :type="getLevelTagType(logDetail.level)">{{ logDetail.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="日志器">{{ logDetail.logger }}</el-descriptions-item>
          <el-descriptions-item label="线程">{{ logDetail.thread }}</el-descriptions-item>
          <el-descriptions-item label="消息" :span="2">{{ logDetail.message }}</el-descriptions-item>
        </el-descriptions>
        
        <div v-if="logDetail.stackTrace" class="stack-trace">
          <h4>堆栈信息</h4>
          <pre>{{ logDetail.stackTrace }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()

// 根据路由路径确定页面标题
const pageTitle = computed(() => {
  const path = route.path
  if (path.includes('/setting/log/analysis')) return '日志分析'
  if (path.includes('/setting/log/business')) return '业务日志'
  if (path.includes('/setting/log/access')) return '访问日志'
  if (path.includes('/setting/log/error')) return '错误日志'
  if (path.includes('/setting/log/system')) return '系统日志'
  if (path.includes('/setting/log/setting')) return '日志设置'
  return '日志管理'
})

const searchForm = reactive({
  level: '',
  dateRange: [],
  keyword: ''
})

// 根据路由路径提供不同的示例数据
const getLogData = () => {
  const path = route.path
  if (path.includes('/setting/log/analysis')) {
    return [
      {
        id: 1,
        timestamp: '2024-01-08 15:30:15',
        level: 'INFO',
        logger: 'com.houjiang.analysis.service.AnalysisService',
        message: '用户行为分析完成',
        thread: 'analysis-thread-1',
        stackTrace: ''
      },
      {
        id: 2,
        timestamp: '2024-01-08 15:29:45',
        level: 'DEBUG',
        logger: 'com.houjiang.analysis.task.DataProcessor',
        message: '处理数据批次: 1001',
        thread: 'analysis-thread-2',
        stackTrace: ''
      }
    ]
  }
  
  if (path.includes('/setting/log/business')) {
    return [
      {
        id: 1,
        timestamp: '2024-01-08 14:30:15',
        level: 'INFO',
        logger: 'com.houjiang.business.controller.OrderController',
        message: '订单创建成功',
        thread: 'business-thread-1',
        stackTrace: ''
      },
      {
        id: 2,
        timestamp: '2024-01-08 14:29:45',
        level: 'ERROR',
        logger: 'com.houjiang.business.service.PaymentService',
        message: '支付处理失败',
        thread: 'business-thread-2',
        stackTrace: 'java.lang.NullPointerException\n\tat com.houjiang.business.service.PaymentService.processPayment(PaymentService.java:45)'
      }
    ]
  }
  
  if (path.includes('/setting/log/access')) {
    return [
      {
        id: 1,
        timestamp: '2024-01-08 13:30:15',
        level: 'INFO',
        logger: 'com.houjiang.security.filter.AccessFilter',
        message: '用户 admin 访问 /api/user/list',
        thread: 'access-thread-1',
        stackTrace: ''
      },
      {
        id: 2,
        timestamp: '2024-01-08 13:29:45',
        level: 'WARN',
        logger: 'com.houjiang.security.filter.AccessFilter',
        message: '用户 guest 尝试访问受限资源 /api/admin/config',
        thread: 'access-thread-2',
        stackTrace: ''
      }
    ]
  }
  
  if (path.includes('/setting/log/error')) {
    return [
      {
        id: 1,
        timestamp: '2024-01-08 12:30:15',
        level: 'ERROR',
        logger: 'com.houjiang.exception.handler.GlobalExceptionHandler',
        message: '数据库连接异常',
        thread: 'error-thread-1',
        stackTrace: 'java.sql.SQLException: Connection timed out\n\tat com.houjiang.db.ConnectionPool.getConnection(ConnectionPool.java:120)'
      },
      {
        id: 2,
        timestamp: '2024-01-08 12:29:45',
        level: 'ERROR',
        logger: 'com.houjiang.web.controller.UserController',
        message: '用户查询失败',
        thread: 'error-thread-2',
        stackTrace: 'java.lang.NullPointerException\n\tat com.houjiang.service.UserService.getUserById(UserService.java:45)'
      }
    ]
  }
  
  // 默认返回系统日志数据
  return [
    {
      id: 1,
      timestamp: '2024-01-08 14:30:15',
      level: 'INFO',
      logger: 'com.houjiang.business.controller.UserController',
      message: '用户登录成功',
      thread: 'http-nio-8080-exec-1',
      stackTrace: ''
    },
    {
      id: 2,
      timestamp: '2024-01-08 14:29:45',
      level: 'ERROR',
      logger: 'com.houjiang.business.service.impl.OrderServiceImpl',
      message: '订单处理失败',
      thread: 'http-nio-8080-exec-2',
      stackTrace: 'java.lang.NullPointerException\n\tat com.houjiang.business.service.impl.OrderServiceImpl.processOrder(OrderServiceImpl.java:45)'
    },
    {
      id: 3,
      timestamp: '2024-01-08 14:28:30',
      level: 'WARN',
      logger: 'com.houjiang.commons.util.RedisUtil',
      message: 'Redis连接超时',
      thread: 'pool-1-thread-1',
      stackTrace: ''
    },
    {
      id: 4,
      timestamp: '2024-01-08 14:27:15',
      level: 'DEBUG',
      logger: 'com.houjiang.business.mapper.UserMapper',
      message: '执行SQL查询',
      thread: 'http-nio-8080-exec-3',
      stackTrace: ''
    }
  ]
}

const logList = ref(getLogData())

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(100)

const detailVisible = ref(false)
const logDetail = ref({})

const getLevelTagType = (level) => {
  const typeMap = {
    'DEBUG': '',
    'INFO': 'success',
    'WARN': 'warning',
    'ERROR': 'danger'
  }
  return typeMap[level] || ''
}

const searchLogs = () => {
  ElMessage.info('搜索功能开发中...')
}

const resetSearch = () => {
  searchForm.level = ''
  searchForm.dateRange = []
  searchForm.keyword = ''
}

const viewDetail = (row) => {
  logDetail.value = row
  detailVisible.value = true
}

const clearLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有日志吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    logList.value = []
    ElMessage.success('日志清空成功')
  } catch {
    ElMessage.info('已取消清空')
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  // 重新加载数据
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  // 重新加载数据
}

// 监听路由变化，更新日志数据
onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.system-logs {
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
  margin-top: 20px;
}

.stack-trace h4 {
  margin-bottom: 10px;
  color: #303133;
}

.stack-trace pre {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
}
</style>
