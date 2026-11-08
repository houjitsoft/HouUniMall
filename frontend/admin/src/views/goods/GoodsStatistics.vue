<template>
  <div class="goods-statistics">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品统计分析</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </div>
      </template>
      
      <!-- 统计概览 -->
      <el-row :gutter="20" class="statistics-overview">
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="item-label">商品总数</div>
              <div class="item-value">{{ statisticsData.totalGoods }}</div>
              <div class="item-trend" :class="statisticsData.totalGoodsTrend >= 0 ? 'up' : 'down'">
                <el-icon><Top v-if="statisticsData.totalGoodsTrend >= 0" /><Bottom v-else /></el-icon>
                {{ Math.abs(statisticsData.totalGoodsTrend) }}%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="item-label">上架商品</div>
              <div class="item-value">{{ statisticsData.onShelfGoods }}</div>
              <div class="item-trend" :class="statisticsData.onShelfGoodsTrend >= 0 ? 'up' : 'down'">
                <el-icon><Top v-if="statisticsData.onShelfGoodsTrend >= 0" /><Bottom v-else /></el-icon>
                {{ Math.abs(statisticsData.onShelfGoodsTrend) }}%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="item-label">本月新增</div>
              <div class="item-value">{{ statisticsData.monthlyNewGoods }}</div>
              <div class="item-trend" :class="statisticsData.monthlyNewGoodsTrend >= 0 ? 'up' : 'down'">
                <el-icon><Top v-if="statisticsData.monthlyNewGoodsTrend >= 0" /><Bottom v-else /></el-icon>
                {{ Math.abs(statisticsData.monthlyNewGoodsTrend) }}%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="item-label">总销量</div>
              <div class="item-value">{{ statisticsData.totalSales }}</div>
              <div class="item-trend" :class="statisticsData.totalSalesTrend >= 0 ? 'up' : 'down'">
                <el-icon><Top v-if="statisticsData.totalSalesTrend >= 0" /><Bottom v-else /></el-icon>
                {{ Math.abs(statisticsData.totalSalesTrend) }}%
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="chart-container">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>商品销量趋势</span>
              </div>
            </template>
            <div ref="salesChartRef" class="chart-wrapper"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>商品分类分布</span>
              </div>
            </template>
            <div ref="categoryChartRef" class="chart-wrapper"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 商品排行榜 -->
      <el-card class="rank-card">
        <template #header>
          <div class="card-header">
            <span>热销商品排行榜</span>
          </div>
        </template>
        <el-table :data="topGoodsList" style="width: 100%">
          <el-table-column type="index" label="排名" width="80" />
          <el-table-column prop="goodsName" label="商品名称" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" width="150" />
          <el-table-column prop="salesCount" label="销量" width="120" sortable>
            <template #default="scope">
              <span class="sales-count">{{ scope.row.salesCount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="salesAmount" label="销售额" width="150" sortable>
            <template #default="scope">
              <span class="sales-amount">¥{{ formatAmount(scope.row.salesAmount) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { goodsApi } from '../../api/goods/goods'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const dateRange = ref([])
const salesChartRef = ref(null)
const categoryChartRef = ref(null)
let salesChart = null
let categoryChart = null

// 统计数据
const statisticsData = reactive({
  totalGoods: 0,
  totalGoodsTrend: 0,
  onShelfGoods: 0,
  onShelfGoodsTrend: 0,
  monthlyNewGoods: 0,
  monthlyNewGoodsTrend: 0,
  totalSales: 0,
  totalSalesTrend: 0
})

// 热销商品列表
const topGoodsList = ref([])

// 生命周期
onMounted(() => {
  // 初始化图表
  initCharts()
  // 获取统计数据
  fetchStatisticsData()
  // 获取热销商品
  fetchTopGoods()
})

onBeforeUnmount(() => {
  // 销毁图表实例
  if (salesChart) {
    salesChart.dispose()
  }
  if (categoryChart) {
    categoryChart.dispose()
  }
})

// 初始化图表
const initCharts = () => {
  // 销量趋势图
  if (salesChartRef.value) {
    salesChart = echarts.init(salesChartRef.value)
    updateSalesChart()
  }
  
  // 分类分布图
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
    updateCategoryChart()
  }
}

// 更新销量趋势图
const updateSalesChart = () => {
  if (!salesChart) return
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: [120, 200, 150, 80, 70, 110, 130, 140, 180, 160, 190, 210],
      type: 'line',
      smooth: true
    }]
  }
  
  salesChart.setOption(option)
}

// 更新分类分布图
const updateCategoryChart = () => {
  if (!categoryChart) return
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: 1048, name: '服装' },
        { value: 735, name: '电子产品' },
        { value: 580, name: '家居' },
        { value: 484, name: '食品' },
        { value: 300, name: '其他' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  categoryChart.setOption(option)
}

// 获取统计数据
const fetchStatisticsData = async () => {
  loading.value = true
  try {
    const response = await goodsApi.getGoodsStatistics()
    
    if (response.code === 200 || response.success) {
      Object.assign(statisticsData, response.data || response)
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取热销商品
const fetchTopGoods = async () => {
  try {
    // 这里应该调用获取热销商品的接口
    // 暂时使用模拟数据
    topGoodsList.value = [
      { goodsName: '时尚连衣裙', categoryName: '服装', salesCount: 1234, salesAmount: 98765.43 },
      { goodsName: '智能手机', categoryName: '电子产品', salesCount: 987, salesAmount: 87654.32 },
      { goodsName: '实木餐桌', categoryName: '家居', salesCount: 654, salesAmount: 76543.21 },
      { goodsName: '有机牛奶', categoryName: '食品', salesCount: 543, salesAmount: 65432.10 },
      { goodsName: '运动鞋', categoryName: '服装', salesCount: 432, salesAmount: 54321.09 }
    ]
  } catch (error) {
    console.error('获取热销商品失败:', error)
    ElMessage.error('获取热销商品失败: ' + (error.message || '网络错误'))
  }
}

// 格式化金额
const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

// 日期改变处理
const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    // 根据选择的日期范围重新获取数据
    fetchStatisticsData()
    fetchTopGoods()
  }
}
</script>

<style scoped>
.goods-statistics {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-overview {
  margin-bottom: 20px;
}

.overview-card {
  height: 120px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.item-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.item-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.item-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
}

.item-trend.up {
  color: #67c23a;
}

.item-trend.down {
  color: #f56c6c;
}

.chart-container {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart-wrapper {
  width: 100%;
  height: 300px;
}

.rank-card {
  margin-top: 20px;
}

.sales-count {
  font-weight: bold;
  color: #409eff;
}

.sales-amount {
  font-weight: bold;
  color: #67c23a;
}
</style>
