<template>
  <div class="user-behavior-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-visits">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><View /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ behaviorStats.totalVisits }}</div>
              <div class="stat-label">总访问量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card today-visits">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ behaviorStats.todayVisits }}</div>
              <div class="stat-label">今日访问</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card active-users">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ behaviorStats.activeUsers }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card avg-duration">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Clock /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ behaviorStats.avgDuration }}</div>
              <div class="stat-label">平均停留</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 行为分析图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header">
            <span class="chart-title">用户行为趋势</span>
            <el-select v-model="trendPeriod" size="small" @change="loadTrendChart">
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
              <el-option label="最近90天" value="90d" />
            </el-select>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header">
            <span class="chart-title">行为类型分布</span>
            <el-button size="small" @click="loadBehaviorChart">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          <div ref="behaviorChartRef" class="chart-container"></div>
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
        <el-form-item label="行为类型">
          <el-select v-model="searchForm.behaviorType" placeholder="请选择行为类型" clearable>
            <el-option label="页面访问" value="page_view" />
            <el-option label="商品浏览" value="product_view" />
            <el-option label="搜索" value="search" />
            <el-option label="加入购物车" value="add_cart" />
            <el-option label="下单" value="place_order" />
            <el-option label="支付" value="payment" />
            <el-option label="评价" value="review" />
            <el-option label="分享" value="share" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="searchForm.deviceType" placeholder="请选择设备类型" clearable>
            <el-option label="PC" value="pc" />
            <el-option label="手机" value="mobile" />
            <el-option label="平板" value="tablet" />
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
          <el-button type="primary" @click="handleAnalyzeUser">
            <el-icon><DataAnalysis /></el-icon>
            用户画像分析
          </el-button>
          <el-button type="success" @click="handleBehaviorReport">
            <el-icon><Document /></el-icon>
            行为报告
          </el-button>
          <el-button type="warning" @click="handleHeatmap">
            <el-icon><Grid /></el-icon>
            热力图分析
          </el-button>
          <el-button type="info" @click="handleExportBehavior">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 行为记录列表 -->
    <el-card class="table-card">
      <div class="table-header">
        <span class="table-title">用户行为记录</span>
        <div class="table-controls">
          <el-button size="small" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table
        :data="behaviorList"
        v-loading="loading"
        stripe
        border
      >
        <el-table-column prop="id" label="记录ID" width="120" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="nickname" label="用户昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="behaviorType" label="行为类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getBehaviorTypeTagType(row.behaviorType)">
              {{ getBehaviorTypeName(row.behaviorType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标类型" width="100" />
        <el-table-column prop="targetId" label="目标ID" width="120" />
        <el-table-column prop="targetName" label="目标名称" min-width="150" />
        <el-table-column prop="deviceType" label="设备类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getDeviceTypeName(row.deviceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="userAgent" label="浏览器" min-width="200" />
        <el-table-column prop="duration" label="停留时长" width="100">
          <template #default="{ row }">
            <span v-if="row.duration">{{ formatDuration(row.duration) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button type="info" size="small" @click="handleViewPath(row)">
              访问路径
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

    <!-- 行为详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="行为详情" width="800px">
      <div class="behavior-detail" v-if="currentBehavior">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录ID">{{ currentBehavior.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentBehavior.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户昵称">{{ currentBehavior.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentBehavior.phone }}</el-descriptions-item>
          <el-descriptions-item label="行为类型">
            <el-tag :type="getBehaviorTypeTagType(currentBehavior.behaviorType)">
              {{ getBehaviorTypeName(currentBehavior.behaviorType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="目标类型">{{ currentBehavior.targetType }}</el-descriptions-item>
          <el-descriptions-item label="目标ID">{{ currentBehavior.targetId }}</el-descriptions-item>
          <el-descriptions-item label="目标名称">{{ currentBehavior.targetName }}</el-descriptions-item>
          <el-descriptions-item label="设备类型">
            <el-tag size="small">{{ getDeviceTypeName(currentBehavior.deviceType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentBehavior.ip }}</el-descriptions-item>
          <el-descriptions-item label="地理位置">{{ currentBehavior.location || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="推荐来源">{{ currentBehavior.referrer || '直接访问' }}</el-descriptions-item>
          <el-descriptions-item label="停留时长" :span="2">
            {{ currentBehavior.duration ? formatDuration(currentBehavior.duration) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="操作时间" :span="2">{{ currentBehavior.createTime }}</el-descriptions-item>
          <el-descriptions-item label="浏览器信息" :span="2">{{ currentBehavior.userAgent }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 扩展信息 -->
        <div class="extended-info" v-if="currentBehavior.extendedData">
          <h4>扩展信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item 
              v-for="(value, key) in currentBehavior.extendedData" 
              :key="key" 
              :label="key"
            >
              {{ value }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>

    <!-- 用户画像分析对话框 -->
    <el-dialog v-model="portraitDialogVisible" title="用户画像分析" width="1000px">
      <div class="user-portrait">
        <el-form :model="portraitForm" :inline="true" class="portrait-form">
          <el-form-item label="选择用户">
            <el-input v-model="portraitForm.userQuery" placeholder="请输入用户昵称或手机号" @input="handlePortraitUserSearch" />
          </el-form-item>
          <el-form-item v-if="portraitUsers.length">
            <el-select v-model="portraitForm.userId" placeholder="请选择用户" @change="handleLoadPortrait">
              <el-option 
                v-for="user in portraitUsers" 
                :key="user.id" 
                :label="`${user.nickname} (${user.phone})`" 
                :value="user.id" 
              />
            </el-select>
          </el-form-item>
        </el-form>
        
        <div v-if="userPortrait" class="portrait-content">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="portrait-card">
                <h4>基本信息</h4>
                <p>用户ID：{{ userPortrait.userId }}</p>
                <p>昵称：{{ userPortrait.nickname }}</p>
                <p>会员等级：{{ userPortrait.memberLevel }}</p>
                <p>注册时间：{{ userPortrait.registerTime }}</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="portrait-card">
                <h4>行为特征</h4>
                <p>访问频次：{{ userPortrait.visitFrequency }}</p>
                <p>活跃度：{{ userPortrait.activity }}</p>
                <p>偏好类别：{{ userPortrait.preferCategory }}</p>
                <p>购买力：{{ userPortrait.purchasePower }}</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="portrait-card">
                <h4>设备偏好</h4>
                <p>常用设备：{{ userPortrait.preferDevice }}</p>
                <p>浏览器：{{ userPortrait.preferBrowser }}</p>
                <p>访问时段：{{ userPortrait.activeTime }}</p>
                <p>地理位置：{{ userPortrait.location }}</p>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Refresh,
  DataAnalysis,
  Document,
  Grid,
  Download,
  View,
  TrendCharts,
  User,
  Clock
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const trendChartRef = ref()
const behaviorChartRef = ref()
const trendPeriod = ref('7d')

// 统计数据
const behaviorStats = reactive({
  totalVisits: '1,256,820',
  todayVisits: '15,680',
  activeUsers: '8,420',
  avgDuration: '5分38秒'
})

// 搜索表单
const searchForm = reactive({
  nickname: '',
  phone: '',
  behaviorType: '',
  deviceType: '',
  dateRange: []
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 行为记录列表数据
const behaviorList = ref([
  {
    id: 'B001',
    userId: 'U001',
    nickname: '张三',
    phone: '13800138001',
    behaviorType: 'page_view',
    targetType: 'page',
    targetId: 'home',
    targetName: '首页',
    deviceType: 'mobile',
    ip: '192.168.1.1',
    userAgent: 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)',
    duration: 180,
    location: '北京市',
    referrer: 'https://www.google.com',
    createTime: '2024-01-20 14:30:00',
    extendedData: {
      screenSize: '375x812',
      network: '4G',
      carrier: '中国移动'
    }
  },
  {
    id: 'B002',
    userId: 'U002',
    nickname: '李四',
    phone: '13800138002',
    behaviorType: 'product_view',
    targetType: 'product',
    targetId: 'P001',
    targetName: 'iPhone 15 Pro',
    deviceType: 'pc',
    ip: '192.168.1.2',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    duration: 120,
    location: '上海市',
    referrer: 'https://www.baidu.com',
    createTime: '2024-01-20 13:45:00'
  }
])

// 对话框状态
const detailDialogVisible = ref(false)
const portraitDialogVisible = ref(false)

// 当前选中的行为记录
const currentBehavior = ref(null)

// 用户画像相关
const portraitForm = reactive({
  userQuery: '',
  userId: ''
})

const portraitUsers = ref([])
const userPortrait = ref(null)

// 方法定义
const handleSearch = () => {
  console.log('搜索行为记录', searchForm)
  loadBehaviorList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    nickname: '',
    phone: '',
    behaviorType: '',
    deviceType: '',
    dateRange: []
  })
  loadBehaviorList()
}

const handleAnalyzeUser = () => {
  Object.assign(portraitForm, {
    userQuery: '',
    userId: ''
  })
  portraitUsers.value = []
  userPortrait.value = null
  portraitDialogVisible.value = true
}

const handlePortraitUserSearch = (query) => {
  if (query.length >= 2) {
    // 模拟用户搜索
    portraitUsers.value = [
      { id: 'U001', nickname: '张三', phone: '13800138001' },
      { id: 'U002', nickname: '李四', phone: '13800138002' }
    ].filter(user => 
      user.nickname.includes(query) || user.phone.includes(query)
    )
  } else {
    portraitUsers.value = []
  }
}

const handleLoadPortrait = (userId) => {
  // 模拟加载用户画像
  userPortrait.value = {
    userId: userId,
    nickname: '张三',
    memberLevel: '金牌会员',
    registerTime: '2023-08-15',
    visitFrequency: '每日访问',
    activity: '高活跃',
    preferCategory: '数码产品',
    purchasePower: '高',
    preferDevice: '移动端',
    preferBrowser: 'Safari',
    activeTime: '晚上8-10点',
    location: '北京市'
  }
}

const handleBehaviorReport = () => {
  ElMessage.success('行为报告生成成功')
}

const handleHeatmap = () => {
  ElMessage.success('热力图分析功能开发中')
}

const handleExportBehavior = () => {
  ElMessage.success('行为数据导出成功')
}

const handleViewDetail = (row) => {
  currentBehavior.value = row
  detailDialogVisible.value = true
}

const handleViewPath = (row) => {
  console.log('查看访问路径', row)
  ElMessage.info('访问路径功能开发中')
}

const handleRefresh = () => {
  loadBehaviorList()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadBehaviorList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadBehaviorList()
}

const loadBehaviorList = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    pagination.total = behaviorList.value.length
    loading.value = false
  }, 1000)
}

const loadTrendChart = () => {
  // 模拟加载趋势图表
  console.log('加载趋势图表', trendPeriod.value)
}

const loadBehaviorChart = () => {
  // 模拟加载行为分布图表
  console.log('加载行为分布图表')
}

// 获取行为类型名称
const getBehaviorTypeName = (type) => {
  const typeMap = {
    page_view: '页面访问',
    product_view: '商品浏览',
    search: '搜索',
    add_cart: '加入购物车',
    place_order: '下单',
    payment: '支付',
    review: '评价',
    share: '分享'
  }
  return typeMap[type] || type
}

// 获取行为类型标签类型
const getBehaviorTypeTagType = (type) => {
  const typeMap = {
    page_view: 'info',
    product_view: 'primary',
    search: 'success',
    add_cart: 'warning',
    place_order: 'danger',
    payment: 'success',
    review: 'primary',
    share: 'info'
  }
  return typeMap[type] || 'info'
}

// 获取设备类型名称
const getDeviceTypeName = (type) => {
  const typeMap = {
    pc: 'PC',
    mobile: '手机',
    tablet: '平板'
  }
  return typeMap[type] || type
}

// 格式化时长
const formatDuration = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainingSeconds = seconds % 60
  
  if (hours > 0) {
    return `${hours}时${minutes}分${remainingSeconds}秒`
  } else if (minutes > 0) {
    return `${minutes}分${remainingSeconds}秒`
  } else {
    return `${remainingSeconds}秒`
  }
}

// 初始化
onMounted(() => {
  loadBehaviorList()
  nextTick(() => {
    loadTrendChart()
    loadBehaviorChart()
  })
})
</script>

<style scoped>
.user-behavior-container {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.chart-row {
  margin-bottom: 20px;
}

.stat-card, .chart-card {
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

.total-visits .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.today-visits .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.active-users .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.avg-duration .stat-icon {
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

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.chart-container {
  height: 300px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
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

.pagination {
  margin-top: 20px;
  text-align: right;
}

.behavior-detail {
  margin-bottom: 20px;
}

.extended-info {
  margin-top: 30px;
}

.extended-info h4 {
  margin-bottom: 15px;
  color: #303133;
}

.user-portrait {
  min-height: 400px;
}

.portrait-form {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.portrait-content {
  margin-top: 20px;
}

.portrait-card {
  margin-bottom: 20px;
}

.portrait-card h4 {
  margin-bottom: 15px;
  color: #303133;
  font-size: 16px;
}

.portrait-card p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
}
</style>
