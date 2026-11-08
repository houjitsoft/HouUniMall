<template>
  <div class="user-manage-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-users">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.total }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card active-users">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Check /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.active }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card new-users">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Plus /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.newToday }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card vip-users">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Star /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.vip }}</div>
              <div class="stat-label">VIP用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="用户昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入用户昵称" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="用户状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
            <el-option label="待审核" value="pending" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="searchForm.memberLevel" placeholder="请选择会员等级" clearable>
            <el-option label="普通用户" value="normal" />
            <el-option label="银牌会员" value="silver" />
            <el-option label="金牌会员" value="gold" />
            <el-option label="钻石会员" value="diamond" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册时间">
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

    <!-- 操作按钮区域 -->
    <el-card class="action-card">
      <el-row :gutter="10">
        <el-col :span="24">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
          <el-button type="warning" @click="handleBatchDisable" :disabled="!selectedRows.length">
            <el-icon><Lock /></el-icon>
            批量禁用
          </el-button>
          <el-button type="success" @click="handleBatchEnable" :disabled="!selectedRows.length">
            <el-icon><Unlock /></el-icon>
            批量启用
          </el-button>
          <el-button type="info" @click="handleExportUsers">
            <el-icon><Download /></el-icon>
            导出用户
          </el-button>
          <el-button type="primary" @click="handleImportUsers">
            <el-icon><Upload /></el-icon>
            导入用户
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <div class="table-header">
        <span class="table-title">用户列表</span>
        <div class="table-controls">
          <el-button size="small" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table
        :data="userList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="用户ID" width="100" />
        <el-table-column prop="avatar" label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="50">
              <el-icon><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="memberLevel" label="会员等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getMemberLevelTagType(row.memberLevel)">
              {{ getMemberLevelName(row.memberLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100">
          <template #default="{ row }">
            <span class="points">{{ row.points || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginTime" label="最后登录" width="180" />
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button 
              :type="row.status === 'active' ? 'warning' : 'success'" 
              size="small" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="800px">
      <div class="user-detail" v-if="currentUser">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-avatar :src="currentUser.avatar" :size="100">
              <el-icon><User /></el-icon>
            </el-avatar>
          </el-col>
          <el-col :span="18">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
              <el-descriptions-item label="会员等级">
                <el-tag :type="getMemberLevelTagType(currentUser.memberLevel)">
                  {{ getMemberLevelName(currentUser.memberLevel) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="积分">{{ currentUser.points || 0 }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusTagType(currentUser.status)">
                  {{ getStatusName(currentUser.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="性别">{{ currentUser.gender || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="生日">{{ currentUser.birthday || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="地区">{{ currentUser.location || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="最后登录" :span="2">{{ currentUser.loginTime }}</el-descriptions-item>
              <el-descriptions-item label="注册时间" :span="2">{{ currentUser.createTime }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="editDialogVisible" :title="editForm.id ? '编辑用户' : '新增用户'" width="600px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入用户昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="会员等级" prop="memberLevel">
          <el-select v-model="editForm.memberLevel" placeholder="请选择会员等级">
            <el-option label="普通用户" value="normal" />
            <el-option label="银牌会员" value="silver" />
            <el-option label="金牌会员" value="gold" />
            <el-option label="钻石会员" value="diamond" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input-number v-model="editForm.points" :min="0" placeholder="请输入积分" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio label="active">正常</el-radio>
            <el-radio label="disabled">禁用</el-radio>
            <el-radio label="pending">待审核</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio label="male">男</el-radio>
            <el-radio label="female">女</el-radio>
            <el-radio label="unknown">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="editForm.birthday"
            type="date"
            placeholder="请选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="地区" prop="location">
          <el-input v-model="editForm.location" placeholder="请输入地区" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">确认</el-button>
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
  Search,
  Refresh,
  Plus,
  Lock,
  Unlock,
  Download,
  Upload,
  User,
  Check,
  Star
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const selectedRows = ref([])

// 统计数据
const userStats = reactive({
  total: 15680,
  active: 12456,
  newToday: 156,
  vip: 2340
})

// 搜索表单
const searchForm = reactive({
  nickname: '',
  phone: '',
  email: '',
  status: '',
  memberLevel: '',
  dateRange: []
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 用户列表数据
const userList = ref([
  {
    id: 'U001',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    nickname: '张三',
    phone: '13800138001',
    email: 'zhangsan@example.com',
    memberLevel: 'gold',
    points: 2350,
    status: 'active',
    gender: 'male',
    birthday: '1990-05-15',
    location: '北京市朝阳区',
    loginTime: '2024-01-20 14:30:00',
    createTime: '2023-08-15 10:20:00'
  },
  {
    id: 'U002',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    nickname: '李四',
    phone: '13800138002',
    email: 'lisi@example.com',
    memberLevel: 'silver',
    points: 1280,
    status: 'active',
    gender: 'female',
    birthday: '1995-03-22',
    location: '上海市浦东新区',
    loginTime: '2024-01-20 13:45:00',
    createTime: '2023-09-20 16:30:00'
  }
])

// 对话框状态
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)

// 当前选中的用户
const currentUser = ref(null)

// 编辑表单
const editFormRef = ref()
const editForm = reactive({
  id: '',
  nickname: '',
  phone: '',
  email: '',
  memberLevel: 'normal',
  points: 0,
  status: 'active',
  gender: 'unknown',
  birthday: '',
  location: ''
})

// 表单验证规则
const editRules = {
  nickname: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 方法定义
const handleSearch = () => {
  console.log('搜索用户', searchForm)
  loadUserList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    nickname: '',
    phone: '',
    email: '',
    status: '',
    memberLevel: '',
    dateRange: []
  })
  loadUserList()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleAdd = () => {
  Object.assign(editForm, {
    id: '',
    nickname: '',
    phone: '',
    email: '',
    memberLevel: 'normal',
    points: 0,
    status: 'active',
    gender: 'unknown',
    birthday: '',
    location: ''
  })
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, { ...row })
  editDialogVisible.value = true
}

const handleSaveUser = async () => {
  try {
    await editFormRef.value.validate()
    console.log('保存用户', editForm)
    ElMessage.success('用户保存成功')
    editDialogVisible.value = false
    loadUserList()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleViewDetail = (row) => {
  currentUser.value = row
  detailDialogVisible.value = true
}

const handleToggleStatus = (row) => {
  const action = row.status === 'active' ? '禁用' : '启用'
  ElMessageBox.confirm(`确认${action}该用户吗？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    row.status = row.status === 'active' ? 'disabled' : 'active'
    ElMessage.success(`${action}成功`)
  })
}

const handleBatchDisable = () => {
  ElMessageBox.confirm('确认批量禁用选中的用户吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量禁用成功')
    loadUserList()
  })
}

const handleBatchEnable = () => {
  ElMessageBox.confirm('确认批量启用选中的用户吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量启用成功')
    loadUserList()
  })
}

const handleExportUsers = () => {
  ElMessage.success('用户数据导出成功')
}

const handleImportUsers = () => {
  ElMessage.success('用户数据导入功能开发中')
}

const handleRefresh = () => {
  loadUserList()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadUserList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadUserList()
}

const loadUserList = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    pagination.total = userList.value.length
    loading.value = false
  }, 1000)
}

// 获取会员等级名称
const getMemberLevelName = (level) => {
  const levelMap = {
    normal: '普通用户',
    silver: '银牌会员',
    gold: '金牌会员',
    diamond: '钻石会员'
  }
  return levelMap[level] || level
}

// 获取会员等级标签类型
const getMemberLevelTagType = (level) => {
  const levelMap = {
    normal: 'info',
    silver: '',
    gold: 'warning',
    diamond: 'success'
  }
  return levelMap[level] || 'info'
}

// 获取状态名称
const getStatusName = (status) => {
  const statusMap = {
    active: '正常',
    disabled: '禁用',
    pending: '待审核'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const statusMap = {
    active: 'success',
    disabled: 'danger',
    pending: 'warning'
  }
  return statusMap[status] || 'info'
}

// 初始化
onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-manage-container {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 15px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.total-users .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.active-users .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.new-users .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.vip-users .stat-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.search-card, .action-card, .table-card {
  margin-bottom: 20px;
}

.search-form .el-form-item {
  margin-bottom: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.points {
  color: #E6A23C;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.user-detail {
  margin-bottom: 20px;
}
</style>
