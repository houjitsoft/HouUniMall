<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="card-title">客户统计</span></template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">3,256</div>
              <div class="stat-label">总客户数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">852</div>
              <div class="stat-label">本月新增</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">2,145</div>
              <div class="stat-label">活跃客户</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">¥2.58M</div>
              <div class="stat-label">总消费额</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-area">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>客户增长趋势</template>
            <div ref="trendChart" class="chart">图表展示区域</div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>客户等级分布</template>
            <div ref="levelChart" class="chart">图表展示区域</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="charts-area">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>消费金额分布</template>
            <div ref="amountChart" class="chart">图表展示区域</div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>客户活跃度</template>
            <div ref="activeChart" class="chart">图表展示区域</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 排行榜 -->
      <el-card shadow="never" class="data-table">
        <template #header>客户消费排行</template>
        <el-table :data="tableData" border stripe>
          <el-table-column type="index" label="排名" width="80" />
          <el-table-column prop="name" label="客户名称" min-width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="totalAmount" label="消费金额(元)" width="120" sortable />
          <el-table-column prop="orderCount" label="订单数" width="100" sortable />
          <el-table-column prop="points" label="积分" width="100" />
          <el-table-column label="客户等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getLevelType(row.level)">{{ getLevelText(row.level) }}</el-tag>
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
const levelChart = ref(null)
const amountChart = ref(null)
const activeChart = ref(null)

const tableData = ref([
  { name: '张三', phone: '13800138001', totalAmount: 125800, orderCount: 356, points: 12580, level: 3 },
  { name: '李四', phone: '13800138002', totalAmount: 98560, orderCount: 245, points: 9856, level: 3 },
  { name: '王五', phone: '13800138003', totalAmount: 76520, orderCount: 198, points: 7652, level: 2 },
  { name: '赵六', phone: '13800138004', totalAmount: 65400, orderCount: 176, points: 6540, level: 2 },
  { name: '孙七', phone: '13800138005', totalAmount: 52300, orderCount: 145, points: 5230, level: 1 }
])

const getLevelType = (level) => {
  const map = { 1: '', 2: 'success', 3: 'warning' }
  return map[level] || ''
}

const getLevelText = (level) => {
  const map = { 1: '普通会员', 2: 'VIP会员', 3: 'SVIP会员' }
  return map[level] || '-'
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
