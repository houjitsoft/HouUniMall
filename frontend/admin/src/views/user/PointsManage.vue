<template>
  <div class="points-manage-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-points">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Coin /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ pointsStats.total }}</div>
              <div class="stat-label">总积分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card today-earned">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ pointsStats.todayEarned }}</div>
              <div class="stat-label">今日获得</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card today-consumed">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><ShoppingCart /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ pointsStats.todayConsumed }}</div>
              <div class="stat-label">今日消费</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card active-users">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ pointsStats.activeUsers }}</div>
              <div class="stat-label">活跃用户</div>
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
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.actionType" placeholder="请选择操作类型" clearable>
            <el-option label="获得积分" value="earn" />
            <el-option label="消费积分" value="consume" />
            <el-option label="管理员调整" value="admin_adjust" />
            <el-option label="积分过期" value="expire" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分范围">
          <el-input-number v-model="searchForm.minPoints" placeholder="最小积分" />
          <span style="margin: 0 10px;">-</span>
          <el-input-number v-model="searchForm.maxPoints" placeholder="最大积分" />
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

    <!-- 操作按钮区域 -->
    <el-card class="action-card">
      <el-row :gutter="10">
        <el-col :span="24">
          <el-button type="primary" @click="handleAdjustPoints">
            <el-icon><Edit /></el-icon>
            积分调整
          </el-button>
          <el-button type="success" @click="handleBatchGrant">
            <el-icon><Present /></el-icon>
            批量赠送
          </el-button>
          <el-button type="warning" @click="handlePointsRule">
            <el-icon><Setting /></el-icon>
            积分规则
          </el-button>
          <el-button type="info" @click="handleExportRecords">
            <el-icon><Download /></el-icon>
            导出记录
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 积分记录列表 -->
    <el-card class="table-card">
      <div class="table-header">
        <span class="table-title">积分记录</span>
        <div class="table-controls">
          <el-button size="small" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table
        :data="pointsList"
        v-loading="loading"
        stripe
        border
      >
        <el-table-column prop="id" label="记录ID" width="120" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="nickname" label="用户昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="actionType" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getActionTypeTagType(row.actionType)">
              {{ getActionTypeName(row.actionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分变动" width="120">
          <template #default="{ row }">
            <span :class="getPointsClass(row.actionType)">
              {{ row.actionType === 'earn' ? '+' : '-' }}{{ Math.abs(row.points) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="currentPoints" label="当前积分" width="120">
          <template #default="{ row }">
            <span class="current-points">{{ row.currentPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="变动原因" min-width="180" />
        <el-table-column prop="relatedOrder" label="关联订单" width="150" />
        <el-table-column prop="operator" label="操作员" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleRevert(row)"
              v-if="row.actionType === 'admin_adjust'"
            >
              撤销
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

    <!-- 积分调整对话框 -->
    <el-dialog v-model="adjustDialogVisible" title="积分调整" width="500px">
      <el-form :model="adjustForm" :rules="adjustRules" ref="adjustFormRef" label-width="100px">
        <el-form-item label="用户查找" prop="userQuery">
          <el-input 
            v-model="adjustForm.userQuery" 
            placeholder="请输入用户昵称或手机号"
            @input="handleUserSearch"
          />
        </el-form-item>
        <el-form-item label="选择用户" prop="userId" v-if="searchUsers.length">
          <el-select v-model="adjustForm.userId" placeholder="请选择用户">
            <el-option 
              v-for="user in searchUsers" 
              :key="user.id" 
              :label="`${user.nickname} (${user.phone})`" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调整类型" prop="adjustType">
          <el-radio-group v-model="adjustForm.adjustType">
            <el-radio label="increase">增加积分</el-radio>
            <el-radio label="decrease">减少积分</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分数量" prop="points">
          <el-input-number v-model="adjustForm.points" :min="1" placeholder="请输入积分数量" />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="adjustForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAdjust">确认调整</el-button>
      </template>
    </el-dialog>

    <!-- 批量赠送对话框 -->
    <el-dialog v-model="grantDialogVisible" title="批量积分赠送" width="600px">
      <el-form :model="grantForm" :rules="grantRules" ref="grantFormRef" label-width="100px">
        <el-form-item label="赠送对象" prop="targetType">
          <el-radio-group v-model="grantForm.targetType">
            <el-radio label="all">全部用户</el-radio>
            <el-radio label="level">指定会员等级</el-radio>
            <el-radio label="selected">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="会员等级" prop="memberLevel" v-if="grantForm.targetType === 'level'">
          <el-select v-model="grantForm.memberLevel" placeholder="请选择会员等级">
            <el-option label="普通会员" value="normal" />
            <el-option label="银牌会员" value="silver" />
            <el-option label="金牌会员" value="gold" />
            <el-option label="钻石会员" value="diamond" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分数量" prop="points">
          <el-input-number v-model="grantForm.points" :min="1" placeholder="请输入积分数量" />
        </el-form-item>
        <el-form-item label="赠送原因" prop="reason">
          <el-input
            v-model="grantForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入赠送原因"
          />
        </el-form-item>
        <el-form-item label="有效期" prop="expireType">
          <el-radio-group v-model="grantForm.expireType">
            <el-radio label="never">永不过期</el-radio>
            <el-radio label="days">指定天数</el-radio>
            <el-radio label="date">指定日期</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="过期天数" prop="expireDays" v-if="grantForm.expireType === 'days'">
          <el-input-number v-model="grantForm.expireDays" :min="1" placeholder="请输入过期天数" />
        </el-form-item>
        <el-form-item label="过期日期" prop="expireDate" v-if="grantForm.expireType === 'date'">
          <el-date-picker
            v-model="grantForm.expireDate"
            type="date"
            placeholder="请选择过期日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grantDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmGrant">确认赠送</el-button>
      </template>
    </el-dialog>

    <!-- 积分规则对话框 -->
    <el-dialog v-model="ruleDialogVisible" title="积分规则设置" width="800px">
      <el-form :model="ruleForm" label-width="150px">
        <el-form-item label="注册赠送积分">
          <el-input-number v-model="ruleForm.registerPoints" :min="0" />
        </el-form-item>
        <el-form-item label="签到赠送积分">
          <el-input-number v-model="ruleForm.checkinPoints" :min="0" />
        </el-form-item>
        <el-form-item label="消费积分比例">
          <el-input-number v-model="ruleForm.consumeRatio" :min="0" :max="1" :step="0.01" />
          <span class="form-tip">每消费1元获得积分数</span>
        </el-form-item>
        <el-form-item label="积分抵现比例">
          <el-input-number v-model="ruleForm.cashRatio" :min="0" :max="1" :step="0.01" />
          <span class="form-tip">1积分抵现金额</span>
        </el-form-item>
        <el-form-item label="积分过期天数">
          <el-input-number v-model="ruleForm.expireDays" :min="0" />
          <span class="form-tip">0表示永不过期</span>
        </el-form-item>
        <el-form-item label="最低使用积分">
          <el-input-number v-model="ruleForm.minUsePoints" :min="0" />
        </el-form-item>
        <el-form-item label="单次最大使用">
          <el-input-number v-model="ruleForm.maxUsePoints" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRule">保存规则</el-button>
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
  Edit,
  Present,
  Setting,
  Download,
  Coin,
  TrendCharts,
  ShoppingCart,
  User
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)

// 统计数据
const pointsStats = reactive({
  total: '2,856,420',
  todayEarned: '15,680',
  todayConsumed: '8,920',
  activeUsers: '3,240'
})

// 搜索表单
const searchForm = reactive({
  nickname: '',
  phone: '',
  actionType: '',
  minPoints: null,
  maxPoints: null,
  dateRange: []
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 积分记录列表数据
const pointsList = ref([
  {
    id: 'P001',
    userId: 'U001',
    nickname: '张三',
    phone: '13800138001',
    actionType: 'earn',
    points: 100,
    currentPoints: 2350,
    reason: '购买商品获得积分',
    relatedOrder: 'ORD001',
    operator: '系统',
    createTime: '2024-01-20 14:30:00'
  },
  {
    id: 'P002',
    userId: 'U002',
    nickname: '李四',
    phone: '13800138002',
    actionType: 'consume',
    points: 50,
    currentPoints: 1230,
    reason: '积分抵扣',
    relatedOrder: 'ORD002',
    operator: '系统',
    createTime: '2024-01-20 13:45:00'
  },
  {
    id: 'P003',
    userId: 'U001',
    nickname: '张三',
    phone: '13800138001',
    actionType: 'admin_adjust',
    points: 200,
    currentPoints: 2550,
    reason: '管理员调整',
    relatedOrder: '',
    operator: '管理员',
    createTime: '2024-01-20 12:30:00'
  }
])

// 搜索用户列表
const searchUsers = ref([])

// 对话框状态
const adjustDialogVisible = ref(false)
const grantDialogVisible = ref(false)
const ruleDialogVisible = ref(false)

// 调整表单
const adjustFormRef = ref()
const adjustForm = reactive({
  userQuery: '',
  userId: '',
  adjustType: 'increase',
  points: 0,
  reason: ''
})

// 赠送表单
const grantFormRef = ref()
const grantForm = reactive({
  targetType: 'all',
  memberLevel: '',
  points: 0,
  reason: '',
  expireType: 'never',
  expireDays: 30,
  expireDate: ''
})

// 规则表单
const ruleForm = reactive({
  registerPoints: 100,
  checkinPoints: 10,
  consumeRatio: 0.01,
  cashRatio: 0.01,
  expireDays: 365,
  minUsePoints: 100,
  maxUsePoints: 10000
})

// 表单验证规则
const adjustRules = {
  userId: [
    { required: true, message: '请选择用户', trigger: 'change' }
  ],
  points: [
    { required: true, message: '请输入积分数量', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' }
  ]
}

const grantRules = {
  points: [
    { required: true, message: '请输入积分数量', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入赠送原因', trigger: 'blur' }
  ]
}

// 方法定义
const handleSearch = () => {
  console.log('搜索积分记录', searchForm)
  loadPointsList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    nickname: '',
    phone: '',
    actionType: '',
    minPoints: null,
    maxPoints: null,
    dateRange: []
  })
  loadPointsList()
}

const handleAdjustPoints = () => {
  Object.assign(adjustForm, {
    userQuery: '',
    userId: '',
    adjustType: 'increase',
    points: 0,
    reason: ''
  })
  searchUsers.value = []
  adjustDialogVisible.value = true
}

const handleUserSearch = (query) => {
  if (query.length >= 2) {
    // 模拟用户搜索
    searchUsers.value = [
      { id: 'U001', nickname: '张三', phone: '13800138001' },
      { id: 'U002', nickname: '李四', phone: '13800138002' }
    ].filter(user => 
      user.nickname.includes(query) || user.phone.includes(query)
    )
  } else {
    searchUsers.value = []
  }
}

const handleConfirmAdjust = async () => {
  try {
    await adjustFormRef.value.validate()
    console.log('确认积分调整', adjustForm)
    ElMessage.success('积分调整成功')
    adjustDialogVisible.value = false
    loadPointsList()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleBatchGrant = () => {
  Object.assign(grantForm, {
    targetType: 'all',
    memberLevel: '',
    points: 0,
    reason: '',
    expireType: 'never',
    expireDays: 30,
    expireDate: ''
  })
  grantDialogVisible.value = true
}

const handleConfirmGrant = async () => {
  try {
    await grantFormRef.value.validate()
    console.log('确认批量赠送', grantForm)
    ElMessage.success('积分赠送成功')
    grantDialogVisible.value = false
    loadPointsList()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handlePointsRule = () => {
  ruleDialogVisible.value = true
}

const handleSaveRule = () => {
  console.log('保存积分规则', ruleForm)
  ElMessage.success('积分规则保存成功')
  ruleDialogVisible.value = false
}

const handleExportRecords = () => {
  ElMessage.success('积分记录导出成功')
}

const handleViewDetail = (row) => {
  console.log('查看详情', row)
  ElMessage.info('积分详情功能开发中')
}

const handleRevert = (row) => {
  ElMessageBox.confirm('确认撤销该积分调整吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('积分调整撤销成功')
    loadPointsList()
  })
}

const handleRefresh = () => {
  loadPointsList()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadPointsList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadPointsList()
}

const loadPointsList = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    pagination.total = pointsList.value.length
    loading.value = false
  }, 1000)
}

// 获取操作类型名称
const getActionTypeName = (type) => {
  const typeMap = {
    earn: '获得积分',
    consume: '消费积分',
    admin_adjust: '管理员调整',
    expire: '积分过期'
  }
  return typeMap[type] || type
}

// 获取操作类型标签类型
const getActionTypeTagType = (type) => {
  const typeMap = {
    earn: 'success',
    consume: 'warning',
    admin_adjust: 'primary',
    expire: 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取积分样式类
const getPointsClass = (type) => {
  return type === 'earn' ? 'points-earn' : 'points-consume'
}

// 初始化
onMounted(() => {
  loadPointsList()
})
</script>

<style scoped>
.points-manage-container {
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

.total-points .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.today-earned .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.today-consumed .stat-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.active-users .stat-icon {
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

.points-earn {
  color: #67C23A;
  font-weight: bold;
}

.points-consume {
  color: #F56C6C;
  font-weight: bold;
}

.current-points {
  color: #E6A23C;
  font-weight: bold;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
