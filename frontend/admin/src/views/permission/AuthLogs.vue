<template>
  <div class="auth-logs">
    <el-card class="logs-card">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>认证日志</span>
          <div class="header-actions">
            <el-button type="primary" @click="refreshLogs">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button @click="exportLogs">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filterForm" inline>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
            />
          </el-form-item>
          
          <el-form-item label="操作类型">
            <el-select v-model="filterForm.operationType" placeholder="请选择操作类型">
              <el-option label="登录" value="login" />
              <el-option label="登出" value="logout" />
              <el-option label="注册" value="register" />
              <el-option label="密码重置" value="password_reset" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="请选择状态">
              <el-option label="成功" value="success" />
              <el-option label="失败" value="failed" />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="searchLogs">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 日志表格 -->
      <el-table :data="logsData" v-loading="loading" stripe>
        <el-table-column prop="timestamp" label="时间" width="180" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="operation" label="操作" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="userAgent" label="用户代理" min-width="200" />
        <el-table-column prop="message" label="详细信息" min-width="150" />
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted } from 'vue'
import { Document, Refresh, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

/**
 * 认证日志页面
 * 
 * @author Amos
 * @since 2025-01-07
 */

// 筛选表单
const filterForm = ref({
  dateRange: null,
  operationType: '',
  status: ''
})

// 日志数据
const logsData = ref([
  {
    timestamp: '2025-01-07 10:30:15',
    username: 'admin',
    operation: 'login',
    status: 'success',
    ip: '192.168.1.100',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    message: '用户登录成功'
  },
  {
    timestamp: '2025-01-07 10:25:30',
    username: 'user001',
    operation: 'login',
    status: 'failed',
    ip: '192.168.1.101',
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
    message: '密码错误'
  },
  {
    timestamp: '2025-01-07 10:20:45',
    username: 'admin',
    operation: 'logout',
    status: 'success',
    ip: '192.168.1.100',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    message: '用户登出'
  }
])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(100)
const loading = ref(false)

// 刷新日志
const refreshLogs = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('日志刷新成功')
  }, 1000)
}

// 导出日志
const exportLogs = () => {
  ElMessage.success('日志导出成功')
}

// 查询日志
const searchLogs = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 1000)
}

// 重置筛选
const resetFilter = () => {
  filterForm.value = {
    dateRange: null,
    operationType: '',
    status: ''
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  searchLogs()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  searchLogs()
}

onMounted(() => {
  refreshLogs()
})
</script>

<style scoped>
.auth-logs {
  padding: 20px;
}

.logs-card {
  max-width: 1200px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 500;
}

.header-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
