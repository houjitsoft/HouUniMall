<template>
  <div class="security-audit">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>审计日志</span>
          <el-button type="danger" @click="clearAuditLogs">清空日志</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="请选择操作类型" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="登录" value="login"></el-option>
            <el-option label="登出" value="logout"></el-option>
            <el-option label="创建" value="create"></el-option>
            <el-option label="修改" value="update"></el-option>
            <el-option label="删除" value="delete"></el-option>
            <el-option label="查询" value="query"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="用户">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable></el-input>
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
          <el-button type="primary" @click="searchAuditLogs">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="auditLogs" style="width: 100%" max-height="500">
        <el-table-column prop="timestamp" label="时间" width="180"></el-table-column>
        <el-table-column prop="username" label="用户" width="120"></el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="scope">
            <el-tag :type="getOperationTagType(scope.row.operationType)">
              {{ getOperationTypeName(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resource" label="资源" width="150"></el-table-column>
        <el-table-column prop="description" label="描述" min-width="200"></el-table-column>
        <el-table-column prop="ip" label="IP地址" width="150"></el-table-column>
        <el-table-column prop="result" label="结果" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.result === 'success' ? 'success' : 'danger'">
              {{ scope.row.result === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
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
    
    <!-- 审计详情对话框 -->
    <el-dialog v-model="detailVisible" title="审计详情" width="80%">
      <div class="audit-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ auditDetail.timestamp }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ auditDetail.username }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTagType(auditDetail.operationType)">
              {{ getOperationTypeName(auditDetail.operationType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="资源">{{ auditDetail.resource }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ auditDetail.ip }}</el-descriptions-item>
          <el-descriptions-item label="结果">
            <el-tag :type="auditDetail.result === 'success' ? 'success' : 'danger'">
              {{ auditDetail.result === 'success' ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ auditDetail.description }}</el-descriptions-item>
        </el-descriptions>
        
        <div v-if="auditDetail.details" class="audit-details">
          <h4>详细信息</h4>
          <pre>{{ auditDetail.details }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({
  operationType: '',
  username: '',
  dateRange: []
})

const auditLogs = ref([
  {
    id: 1,
    timestamp: '2024-01-08 14:30:15',
    username: 'admin',
    operationType: 'login',
    resource: '用户登录',
    description: '管理员登录系统',
    ip: '192.168.1.100',
    result: 'success',
    details: '{"userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"}'
  },
  {
    id: 2,
    timestamp: '2024-01-08 14:25:30',
    username: 'admin',
    operationType: 'create',
    resource: '用户管理',
    description: '创建新用户',
    ip: '192.168.1.100',
    result: 'success',
    details: '{"userId": "12345", "username": "newuser", "role": "USER"}'
  },
  {
    id: 3,
    timestamp: '2024-01-08 14:20:45',
    username: 'user1',
    operationType: 'update',
    resource: '个人资料',
    description: '修改个人信息',
    ip: '192.168.1.101',
    result: 'success',
    details: '{"field": "email", "oldValue": "old@example.com", "newValue": "new@example.com"}'
  },
  {
    id: 4,
    timestamp: '2024-01-08 14:15:20',
    username: 'admin',
    operationType: 'delete',
    resource: '用户管理',
    description: '删除用户',
    ip: '192.168.1.100',
    result: 'success',
    details: '{"userId": "67890", "username": "deleteduser"}'
  }
])

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(100)

const detailVisible = ref(false)
const auditDetail = ref({})

const getOperationTypeName = (type) => {
  const typeMap = {
    'login': '登录',
    'logout': '登出',
    'create': '创建',
    'update': '修改',
    'delete': '删除',
    'query': '查询'
  }
  return typeMap[type] || type
}

const getOperationTagType = (type) => {
  const typeMap = {
    'login': 'success',
    'logout': 'info',
    'create': 'primary',
    'update': 'warning',
    'delete': 'danger',
    'query': ''
  }
  return typeMap[type] || ''
}

const searchAuditLogs = () => {
  ElMessage.info('搜索功能开发中...')
}

const resetSearch = () => {
  searchForm.operationType = ''
  searchForm.username = ''
  searchForm.dateRange = []
}

const viewDetail = (row) => {
  auditDetail.value = row
  detailVisible.value = true
}

const clearAuditLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空审计日志吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    auditLogs.value = []
    ElMessage.success('审计日志清空成功')
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

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.security-audit {
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

.audit-detail {
  padding: 20px;
}

.audit-details {
  margin-top: 20px;
}

.audit-details h4 {
  margin-bottom: 10px;
  color: #303133;
}

.audit-details pre {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
}
</style>
