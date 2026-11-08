<template>
  <div class="member-level-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-levels">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Star /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ levelStats.total }}</div>
              <div class="stat-label">会员等级</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card active-levels">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Check /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ levelStats.active }}</div>
              <div class="stat-label">启用等级</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card highest-level">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Trophy /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ levelStats.highest }}</div>
              <div class="stat-label">最高等级</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card member-count">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ levelStats.memberCount }}</div>
              <div class="stat-label">会员总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="等级名称">
          <el-input v-model="searchForm.levelName" placeholder="请输入等级名称" clearable />
        </el-form-item>
        <el-form-item label="等级状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分范围">
          <el-input-number v-model="searchForm.minPoints" placeholder="最小积分" :min="0" />
          <span style="margin: 0 10px;">-</span>
          <el-input-number v-model="searchForm.maxPoints" placeholder="最大积分" :min="0" />
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
            新增等级
          </el-button>
          <el-button type="warning" @click="handleBatchDisable" :disabled="!selectedRows.length">
            <el-icon><Lock /></el-icon>
            批量禁用
          </el-button>
          <el-button type="success" @click="handleBatchEnable" :disabled="!selectedRows.length">
            <el-icon><Unlock /></el-icon>
            批量启用
          </el-button>
          <el-button type="info" @click="handleExportLevels">
            <el-icon><Download /></el-icon>
            导出等级
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 等级列表 -->
    <el-card class="table-card">
      <div class="table-header">
        <span class="table-title">会员等级列表</span>
        <div class="table-controls">
          <el-button size="small" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table
        :data="levelList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="等级ID" width="100" />
        <el-table-column prop="levelName" label="等级名称" width="150" />
        <el-table-column prop="levelValue" label="等级值" width="100" />
        <el-table-column prop="requiredPoints" label="所需积分" width="120">
          <template #default="{ row }">
            <span class="points">{{ row.requiredPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="discount" label="折扣率" width="100">
          <template #default="{ row }">
            <span class="discount">{{ (row.discount * 100).toFixed(1) }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="privileges" label="特权" min-width="200">
          <template #default="{ row }">
            <el-tag 
              v-for="privilege in row.privileges" 
              :key="privilege" 
              size="small" 
              class="privilege-tag"
            >
              {{ privilege }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="会员数量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="handleViewMembers(row)">
              查看会员
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

    <!-- 编辑等级对话框 -->
    <el-dialog v-model="editDialogVisible" :title="editForm.id ? '编辑等级' : '新增等级'" width="600px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="editForm.levelName" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="等级值" prop="levelValue">
          <el-input-number v-model="editForm.levelValue" :min="0" placeholder="请输入等级值" />
        </el-form-item>
        <el-form-item label="所需积分" prop="requiredPoints">
          <el-input-number v-model="editForm.requiredPoints" :min="0" placeholder="请输入所需积分" />
        </el-form-item>
        <el-form-item label="折扣率" prop="discount">
          <el-input-number v-model="editForm.discount" :min="0" :max="1" :step="0.01" placeholder="请输入折扣率(0-1)" />
        </el-form-item>
        <el-form-item label="等级图标" prop="icon">
          <el-input v-model="editForm.icon" placeholder="请输入图标URL或选择内置图标" />
        </el-form-item>
        <el-form-item label="等级颜色" prop="color">
          <el-color-picker v-model="editForm.color" />
        </el-form-item>
        <el-form-item label="特权" prop="privileges">
          <el-checkbox-group v-model="editForm.privileges">
            <el-checkbox label="免费配送">免费配送</el-checkbox>
            <el-checkbox label="优先客服">优先客服</el-checkbox>
            <el-checkbox label="生日特权">生日特权</el-checkbox>
            <el-checkbox label="专属客服">专属客服</el-checkbox>
            <el-checkbox label="积分加倍">积分加倍</el-checkbox>
            <el-checkbox label="提前购买">提前购买</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="等级描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入等级描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveLevel">确认</el-button>
      </template>
    </el-dialog>

    <!-- 等级会员列表对话框 -->
    <el-dialog v-model="memberDialogVisible" title="等级会员列表" width="1000px">
      <el-table :data="levelMembers" v-loading="memberLoading" stripe border>
        <el-table-column prop="id" label="用户ID" width="100" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column prop="upgradeTime" label="升级时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewUserDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
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
  Star,
  Check,
  Trophy,
  User
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const memberLoading = ref(false)
const selectedRows = ref([])

// 统计数据
const levelStats = reactive({
  total: 6,
  active: 5,
  highest: 'VIP钻石',
  memberCount: 15680
})

// 搜索表单
const searchForm = reactive({
  levelName: '',
  status: '',
  minPoints: null,
  maxPoints: null
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 等级列表数据
const levelList = ref([
  {
    id: 'L001',
    levelName: '普通会员',
    levelValue: 1,
    requiredPoints: 0,
    discount: 1.0,
    privileges: ['基础服务'],
    memberCount: 8500,
    status: 'active',
    icon: 'star',
    color: '#909399',
    description: '注册即可成为普通会员',
    createTime: '2023-01-01 00:00:00'
  },
  {
    id: 'L002',
    levelName: '银牌会员',
    levelValue: 2,
    requiredPoints: 1000,
    discount: 0.95,
    privileges: ['免费配送', '优先客服'],
    memberCount: 4200,
    status: 'active',
    icon: 'star',
    color: '#C0C4CC',
    description: '消费满1000积分升级',
    createTime: '2023-01-01 00:00:00'
  },
  {
    id: 'L003',
    levelName: '金牌会员',
    levelValue: 3,
    requiredPoints: 5000,
    discount: 0.9,
    privileges: ['免费配送', '优先客服', '生日特权'],
    memberCount: 2180,
    status: 'active',
    icon: 'star',
    color: '#F7BA2A',
    description: '消费满5000积分升级',
    createTime: '2023-01-01 00:00:00'
  },
  {
    id: 'L004',
    levelName: '钻石会员',
    levelValue: 4,
    requiredPoints: 10000,
    discount: 0.85,
    privileges: ['免费配送', '优先客服', '生日特权', '专属客服'],
    memberCount: 800,
    status: 'active',
    icon: 'star',
    color: '#E6A23C',
    description: '消费满10000积分升级',
    createTime: '2023-01-01 00:00:00'
  }
])

// 等级会员列表
const levelMembers = ref([
  {
    id: 'U001',
    nickname: '张三',
    phone: '13800138001',
    points: 12500,
    upgradeTime: '2024-01-15 10:30:00',
    status: 'active'
  }
])

// 对话框状态
const editDialogVisible = ref(false)
const memberDialogVisible = ref(false)

// 编辑表单
const editFormRef = ref()
const editForm = reactive({
  id: '',
  levelName: '',
  levelValue: 1,
  requiredPoints: 0,
  discount: 1.0,
  privileges: [],
  status: 'active',
  icon: '',
  color: '#409EFF',
  description: ''
})

// 表单验证规则
const editRules = {
  levelName: [
    { required: true, message: '请输入等级名称', trigger: 'blur' }
  ],
  levelValue: [
    { required: true, message: '请输入等级值', trigger: 'blur' }
  ],
  requiredPoints: [
    { required: true, message: '请输入所需积分', trigger: 'blur' }
  ],
  discount: [
    { required: true, message: '请输入折扣率', trigger: 'blur' }
  ]
}

// 方法定义
const handleSearch = () => {
  console.log('搜索等级', searchForm)
  loadLevelList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    levelName: '',
    status: '',
    minPoints: null,
    maxPoints: null
  })
  loadLevelList()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleAdd = () => {
  Object.assign(editForm, {
    id: '',
    levelName: '',
    levelValue: 1,
    requiredPoints: 0,
    discount: 1.0,
    privileges: [],
    status: 'active',
    icon: '',
    color: '#409EFF',
    description: ''
  })
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, { ...row })
  editDialogVisible.value = true
}

const handleSaveLevel = async () => {
  try {
    await editFormRef.value.validate()
    console.log('保存等级', editForm)
    ElMessage.success('等级保存成功')
    editDialogVisible.value = false
    loadLevelList()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleViewMembers = (row) => {
  console.log('查看等级会员', row)
  memberLoading.value = true
  // 模拟加载会员数据
  setTimeout(() => {
    memberLoading.value = false
    memberDialogVisible.value = true
  }, 1000)
}

const handleViewUserDetail = (row) => {
  console.log('查看用户详情', row)
  ElMessage.info('用户详情功能开发中')
}

const handleToggleStatus = (row) => {
  const action = row.status === 'active' ? '禁用' : '启用'
  ElMessageBox.confirm(`确认${action}该等级吗？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    row.status = row.status === 'active' ? 'disabled' : 'active'
    ElMessage.success(`${action}成功`)
  })
}

const handleBatchDisable = () => {
  ElMessageBox.confirm('确认批量禁用选中的等级吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量禁用成功')
    loadLevelList()
  })
}

const handleBatchEnable = () => {
  ElMessageBox.confirm('确认批量启用选中的等级吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量启用成功')
    loadLevelList()
  })
}

const handleExportLevels = () => {
  ElMessage.success('等级数据导出成功')
}

const handleRefresh = () => {
  loadLevelList()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadLevelList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadLevelList()
}

const loadLevelList = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    pagination.total = levelList.value.length
    loading.value = false
  }, 1000)
}

// 获取状态名称
const getStatusName = (status) => {
  const statusMap = {
    active: '启用',
    disabled: '禁用'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const statusMap = {
    active: 'success',
    disabled: 'danger'
  }
  return statusMap[status] || 'info'
}

// 初始化
onMounted(() => {
  loadLevelList()
})
</script>

<style scoped>
.member-level-container {
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

.total-levels .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.active-levels .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.highest-level .stat-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.member-count .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
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

.discount {
  color: #67C23A;
  font-weight: bold;
}

.privilege-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
