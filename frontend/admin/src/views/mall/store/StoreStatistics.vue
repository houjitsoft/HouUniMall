<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="card-title">店铺统计</span></template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">128</div>
              <div class="stat-label">总店铺数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">95</div>
              <div class="stat-label">营业中</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">23</div>
              <div class="stat-label">待审核</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">10</div>
              <div class="stat-label">已关闭</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-area">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>店铺趋势</template>
            <div ref="trendChart" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>店铺等级分布</template>
            <div ref="levelChart" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 数据表格 -->
      <el-card shadow="never" class="data-table">
        <template #header>店铺排行</template>
        <el-table :data="tableData" border stripe>
          <el-table-column type="index" label="排名" width="80" />
          <el-table-column prop="name" label="店铺名称" min-width="150" />
          <el-table-column prop="sales" label="销售额(元)" width="120" sortable />
          <el-table-column prop="orders" label="订单数" width="100" sortable />
          <el-table-column prop="goods" label="商品数" width="100" />
          <el-table-column label="店铺等级" width="100">
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

const tableData = ref([
  { name: '旗舰专卖店', sales: 1258900, orders: 3256, goods: 128, level: 3 },
  { name: '优品生活馆', sales: 856700, orders: 2145, goods: 96, level: 2 },
  { name: '数码城', sales: 645200, orders: 1876, goods: 75, level: 1 },
  { name: '时尚衣橱', sales: 523400, orders: 1654, goods: 112, level: 2 },
  { name: '美食汇', sales: 456100, orders: 1432, goods: 88, level: 1 }
])

const getLevelType = (level) => {
  const map = { 1: '', 2: 'success', 3: 'warning' }
  return map[level] || ''
}

const getLevelText = (level) => {
  const map = { 1: '普通店铺', 2: '优质店铺', 3: '旗舰店' }
  return map[level] || '-'
}

const initCharts = () => {
  // 这里可以集成 ECharts 或其他图表库
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
