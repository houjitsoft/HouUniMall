<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="card-title">营销统计</span></template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">156</div>
              <div class="stat-label">进行中活动</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">12,580</div>
              <div class="stat-label">活动参与人次</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">¥2.58M</div>
              <div class="stat-label">营销带来销售额</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">35.6%</div>
              <div class="stat-label">营销转化率</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-area">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>营销活动趋势</template>
            <div ref="trendChart" class="chart">图表展示区域</div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>活动类型分布</template>
            <div ref="typeChart" class="chart">图表展示区域</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="charts-area">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>优惠券使用情况</template>
            <div ref="couponChart" class="chart">图表展示区域</div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>积分发放统计</template>
            <div ref="pointsChart" class="chart">图表展示区域</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 活动排行 -->
      <el-card shadow="never" class="data-table">
        <template #header>热门活动排行</template>
        <el-table :data="tableData" border stripe>
          <el-table-column type="index" label="排名" width="80" />
          <el-table-column prop="name" label="活动名称" min-width="150" />
          <el-table-column label="活动类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getTypeColor(row.type)">{{ getTypeText(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="participantCount" label="参与人数" width="100" sortable />
          <el-table-column prop="orderCount" label="订单数" width="100" sortable />
          <el-table-column prop="salesAmount" label="销售额(元)" width="120" sortable />
          <el-table-column prop="conversionRate" label="转化率" width="100">
            <template #default="{ row }">
              {{ row.conversionRate }}%
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const trendChart = ref(null)
const typeChart = ref(null)
const couponChart = ref(null)
const pointsChart = ref(null)

const tableData = ref([
  { name: '春季大促', type: 'discount', participantCount: 3256, orderCount: 1856, salesAmount: 856700, conversionRate: 57.0 },
  { name: '满200减50', type: 'fullcut', participantCount: 2145, orderCount: 1234, salesAmount: 645200, conversionRate: 57.5 },
  { name: '新人专享', type: 'coupon', participantCount: 1876, orderCount: 945, salesAmount: 523400, conversionRate: 50.4 },
  { name: '限时秒杀', type: 'seckill', participantCount: 1654, orderCount: 856, salesAmount: 456100, conversionRate: 51.8 },
  { name: '积分兑换', type: 'points', participantCount: 1432, orderCount: 723, salesAmount: 352300, conversionRate: 50.5 }
])

const getTypeColor = (type) => {
  const map = { discount: 'success', fullcut: 'primary', coupon: 'warning', seckill: 'danger', points: 'info' }
  return map[type] || ''
}

const getTypeText = (type) => {
  const map = { discount: '限时折扣', fullcut: '满减活动', coupon: '优惠券', seckill: '秒杀活动', points: '积分兑换' }
  return map[type] || '-'
}

const initCharts = () => {
  console.log('初始化图表')
}

onMounted(() => {
  initCharts()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-title { font-weight: bold; font-size: 16px; }
.stats-cards { margin-bottom: 20px; }
.stat-item { text-align: center; padding: 20px 0; }
.stat-value { font-size: 32px; font-weight: bold; color: #409EFF; margin-bottom: 10px; }
.stat-label { font-size: 14px; color: #666; }
.charts-area { margin-bottom: 20px; }
.chart { height: 300px; display: flex; align-items: center; justify-content: center; color: #999; }
.data-table { margin-top: 20px; }
</style>
