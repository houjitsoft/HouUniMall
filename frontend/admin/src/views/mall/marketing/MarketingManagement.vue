<template>
  <div class="marketing-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>营销活动</h2>
        <span class="subtitle">管理商城营销活动和优惠券</span>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="handleCreateActivity">
          创建活动
        </el-button>
        <el-button icon="Ticket" @click="handleCreateCoupon">
          新增优惠券
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalActivities }}</div>
              <div class="stat-label">活动总数</div>
            </div>
            <i class="el-icon-promotion stat-icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.activeActivities }}</div>
              <div class="stat-label">进行中</div>
            </div>
            <i class="el-icon-timer stat-icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalCoupons }}</div>
              <div class="stat-label">优惠券数</div>
            </div>
            <i class="el-icon-ticket stat-icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.usedCoupons }}</div>
              <div class="stat-label">已使用</div>
            </div>
            <i class="el-icon-check stat-icon"></i>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 功能标签页 -->
    <el-card class="tabs-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="营销活动" name="activities">
          <!-- 活动搜索 -->
          <div class="search-section">
            <el-form :model="activitySearchForm" inline>
              <el-form-item label="活动名称">
                <el-input 
                  v-model="activitySearchForm.name" 
                  placeholder="请输入活动名称"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="活动状态">
                <el-select 
                  v-model="activitySearchForm.status" 
                  placeholder="请选择状态"
                  style="width: 120px"
                >
                  <el-option label="进行中" value="1" />
                  <el-option label="已结束" value="0" />
                  <el-option label="未开始" value="2" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="searchActivities">
                  搜索
                </el-button>
                <el-button icon="Refresh" @click="resetActivitySearch">
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 活动列表 -->
          <el-table 
            v-loading="loading"
            :data="activityList" 
            style="width: 100%"
          >
            <el-table-column prop="id" label="活动ID" width="80" />
            <el-table-column label="活动封面" width="100">
              <template #default="{ row }">
                <el-image 
                  :src="row.cover" 
                  fit="cover" 
                  style="width: 60px; height: 60px; border-radius: 4px"
                />
              </template>
            </el-table-column>
            <el-table-column prop="name" label="活动名称" min-width="200" />
            <el-table-column label="活动类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ getActivityTypeText(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="活动状态" width="100">
              <template #default="{ row }">
                <el-tag 
                  :type="getActivityStatusType(row.status)"
                  size="small"
                >
                  {{ getActivityStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="活动时间" min-width="200">
              <template #default="{ row }">
                <div>开始：{{ row.startTime }}</div>
                <div>结束：{{ row.endTime }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="participantCount" label="参与人数" width="100" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button 
                  link 
                  type="primary" 
                  size="small" 
                  @click="handleEditActivity(row)"
                >
                  编辑
                </el-button>
                <el-button 
                  link 
                  type="info" 
                  size="small" 
                  @click="handleViewActivityData(row)"
                >
                  数据
                </el-button>
                <el-button 
                  v-if="row.status === 1"
                  link 
                  type="warning" 
                  size="small" 
                  @click="handleStopActivity(row)"
                >
                  停止
                </el-button>
                <el-button 
                  link 
                  type="danger" 
                  size="small" 
                  @click="handleDeleteActivity(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="优惠券管理" name="coupons">
          <!-- 优惠券搜索 -->
          <div class="search-section">
            <el-form :model="couponSearchForm" inline>
              <el-form-item label="优惠券名称">
                <el-input 
                  v-model="couponSearchForm.name" 
                  placeholder="请输入优惠券名称"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="券类型">
                <el-select 
                  v-model="couponSearchForm.type" 
                  placeholder="请选择类型"
                  style="width: 120px"
                >
                  <el-option label="满减券" value="1" />
                  <el-option label="折扣券" value="2" />
                  <el-option label="免邮券" value="3" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="searchCoupons">
                  搜索
                </el-button>
                <el-button icon="Refresh" @click="resetCouponSearch">
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 优惠券列表 -->
          <el-table 
            v-loading="loading"
            :data="couponList" 
            style="width: 100%"
          >
            <el-table-column prop="id" label="券ID" width="80" />
            <el-table-column prop="name" label="券名称" min-width="200" />
            <el-table-column label="券类型" width="100">
              <template #default="{ row }">
                <el-tag 
                  :type="getCouponTypeColor(row.type)"
                  size="small"
                >
                  {{ getCouponTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="优惠额度" width="120">
              <template #default="{ row }">
                <span v-if="row.type === 1" class="discount-amount">
                  ¥{{ row.discountAmount }}
                </span>
                <span v-else-if="row.type === 2" class="discount-amount">
                  {{ row.discountRate }}折
                </span>
                <span v-else class="discount-amount">免邮</span>
              </template>
            </el-table-column>
            <el-table-column label="使用门槛" width="120">
              <template #default="{ row }">
                <span v-if="row.minAmount > 0">
                  满¥{{ row.minAmount }}
                </span>
                <span v-else>无门槛</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalCount" label="发放数量" width="100" />
            <el-table-column prop="usedCount" label="已使用" width="80" />
            <el-table-column label="有效期" min-width="200">
              <template #default="{ row }">
                <div>开始：{{ row.startTime }}</div>
                <div>结束：{{ row.endTime }}</div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button 
                  link 
                  type="primary" 
                  size="small" 
                  @click="handleEditCoupon(row)"
                >
                  编辑
                </el-button>
                <el-button 
                  link 
                  type="success" 
                  size="small" 
                  @click="handleDistributeCoupon(row)"
                >
                  发放
                </el-button>
                <el-button 
                  link 
                  type="info" 
                  size="small" 
                  @click="handleViewCouponData(row)"
                >
                  数据
                </el-button>
                <el-button 
                  link 
                  type="danger" 
                  size="small" 
                  @click="handleDeleteCoupon(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const loading = ref(false)
const activeTab = ref('activities')

// 统计数据
const stats = reactive({
  totalActivities: 58,
  activeActivities: 12,
  totalCoupons: 236,
  usedCoupons: 89
})

// 活动搜索表单
const activitySearchForm = reactive({
  name: '',
  status: ''
})

// 优惠券搜索表单
const couponSearchForm = reactive({
  name: '',
  type: ''
})

// 活动列表
const activityList = ref([
  {
    id: 1,
    name: '双十一狂欢节',
    cover: 'https://via.placeholder.com/60x60',
    type: 1,
    status: 1,
    startTime: '2024-11-01 00:00:00',
    endTime: '2024-11-11 23:59:59',
    participantCount: 1580
  },
  {
    id: 2,
    name: '新年大促销',
    cover: 'https://via.placeholder.com/60x60',
    type: 2,
    status: 0,
    startTime: '2024-01-01 00:00:00',
    endTime: '2024-01-31 23:59:59',
    participantCount: 2340
  }
])

// 优惠券列表
const couponList = ref([
  {
    id: 1,
    name: '新人专享优惠券',
    type: 1,
    discountAmount: 50,
    minAmount: 200,
    totalCount: 1000,
    usedCount: 456,
    startTime: '2024-01-01 00:00:00',
    endTime: '2024-12-31 23:59:59'
  },
  {
    id: 2,
    name: '九折优惠券',
    type: 2,
    discountRate: 9,
    minAmount: 100,
    totalCount: 500,
    usedCount: 234,
    startTime: '2024-01-01 00:00:00',
    endTime: '2024-06-30 23:59:59'
  }
])

// 方法
const getActivityTypeText = (type) => {
  switch (type) {
    case 1: return '促销活动'
    case 2: return '秒杀活动'
    case 3: return '拼团活动'
    default: return '其他'
  }
}

const getActivityStatusType = (status) => {
  switch (status) {
    case 1: return 'success'
    case 0: return 'info'
    case 2: return 'warning'
    default: return 'info'
  }
}

const getActivityStatusText = (status) => {
  switch (status) {
    case 1: return '进行中'
    case 0: return '已结束'
    case 2: return '未开始'
    default: return '未知'
  }
}

const getCouponTypeText = (type) => {
  switch (type) {
    case 1: return '满减券'
    case 2: return '折扣券'
    case 3: return '免邮券'
    default: return '其他'
  }
}

const getCouponTypeColor = (type) => {
  switch (type) {
    case 1: return 'danger'
    case 2: return 'warning'
    case 3: return 'success'
    default: return 'info'
  }
}

const handleTabChange = (tabName) => {
  activeTab.value = tabName
}

const handleCreateActivity = () => {
  ElMessage.info('创建营销活动')
}

const handleCreateCoupon = () => {
  ElMessage.info('新增优惠券')
}

const searchActivities = () => {
  ElMessage.info('搜索营销活动')
}

const resetActivitySearch = () => {
  Object.assign(activitySearchForm, {
    name: '',
    status: ''
  })
}

const searchCoupons = () => {
  ElMessage.info('搜索优惠券')
}

const resetCouponSearch = () => {
  Object.assign(couponSearchForm, {
    name: '',
    type: ''
  })
}

const handleEditActivity = (row) => {
  ElMessage.info(`编辑活动：${row.name}`)
}

const handleViewActivityData = (row) => {
  ElMessage.info(`查看活动数据：${row.name}`)
}

const handleStopActivity = (row) => {
  ElMessageBox.confirm(
    `确定要停止活动"${row.name}"吗？`,
    '确认停止',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    row.status = 0
    ElMessage.success('活动已停止')
  })
}

const handleDeleteActivity = (row) => {
  ElMessageBox.confirm(
    `确定要删除活动"${row.name}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = activityList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      activityList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}

const handleEditCoupon = (row) => {
  ElMessage.info(`编辑优惠券：${row.name}`)
}

const handleDistributeCoupon = (row) => {
  ElMessage.info(`发放优惠券：${row.name}`)
}

const handleViewCouponData = (row) => {
  ElMessage.info(`查看优惠券数据：${row.name}`)
}

const handleDeleteCoupon = (row) => {
  ElMessageBox.confirm(
    `确定要删除优惠券"${row.name}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = couponList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      couponList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}

// 初始化
onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.marketing-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0;
  color: #303133;
}

.subtitle {
  color: #909399;
  font-size: 14px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-content {
  position: relative;
  z-index: 2;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  color: #E4E7ED;
  z-index: 1;
}

.tabs-card {
  margin-bottom: 20px;
}

.search-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.discount-amount {
  color: #f56c6c;
  font-weight: bold;
}
</style>
