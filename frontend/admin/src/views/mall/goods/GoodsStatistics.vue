<template>
  <div class="goods-statistics-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon size="24"><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalGoods }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon size="24"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.onlineGoods }}</div>
              <div class="stat-label">上架商品</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon size="24"><Remove /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.offlineGoods }}</div>
              <div class="stat-label">下架商品</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon size="24"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.lowStockGoods }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 分类商品统计 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>分类商品分布</span>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 商品销量趋势 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="chart-header">
              <span>商品销量趋势</span>
              <el-radio-group v-model="trendType" size="small">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 热销商品排行 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>热销商品TOP10</span>
          </template>
          <el-table :data="hotGoods" stripe size="small">
            <el-table-column label="排名" width="60">
              <template #default="{ $index }">
                <el-tag :type="$index < 3 ? 'danger' : 'info'" size="small">{{ $index + 1 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
            <el-table-column prop="sales" label="销量" width="100" />
            <el-table-column prop="amount" label="销售额" width="120">
              <template #default="{ row }">
                ¥{{ row.amount }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 新增商品统计 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>新增商品统计</span>
          </template>
          <div ref="newGoodsChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 品牌商品统计 -->
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <span>品牌商品分布</span>
          </template>
          <div ref="brandChartRef" class="chart-container" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { Goods, CircleCheck, Remove, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const trendType = ref('week')
const categoryChartRef = ref()
const trendChartRef = ref()
const newGoodsChartRef = ref()
const brandChartRef = ref()

let categoryChart = null
let trendChart = null
let newGoodsChart = null
let brandChart = null

const statistics = reactive({
  totalGoods: 0,
  onlineGoods: 0,
  offlineGoods: 0,
  lowStockGoods: 0
})

const hotGoods = ref([])

const initCategoryChart = () => {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14 } },
      data: [
        { value: 0, name: '服装鞋帽' },
        { value: 0, name: '数码电器' },
        { value: 0, name: '食品饮料' },
        { value: 0, name: '家居用品' },
        { value: 0, name: '其他' }
      ]
    }]
  }
  categoryChart.setOption(option)
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{ data: [], type: 'line', smooth: true, areaStyle: {} }]
  }
  trendChart.setOption(option)
}

const initNewGoodsChart = () => {
  if (!newGoodsChartRef.value) return
  newGoodsChart = echarts.init(newGoodsChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{ data: [], type: 'bar', itemStyle: { color: '#409eff' } }]
  }
  newGoodsChart.setOption(option)
}

const initBrandChart = () => {
  if (!brandChartRef.value) return
  brandChart = echarts.init(brandChartRef.value)
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{ data: [], type: 'bar', itemStyle: { color: '#67c23a' } }]
  }
  brandChart.setOption(option)
}

const fetchStatistics = async () => {
  // TODO: 调用API获取统计数据
  statistics.totalGoods = 0
  statistics.onlineGoods = 0
  statistics.offlineGoods = 0
  statistics.lowStockGoods = 0
}

const fetchHotGoods = async () => {
  // TODO: 调用API获取热销商品
  hotGoods.value = []
}

watch(trendType, () => {
  // TODO: 根据类型刷新趋势图
})

onMounted(() => {
  fetchStatistics()
  fetchHotGoods()
  initCategoryChart()
  initTrendChart()
  initNewGoodsChart()
  initBrandChart()
  
  window.addEventListener('resize', () => {
    categoryChart?.resize()
    trendChart?.resize()
    newGoodsChart?.resize()
    brandChart?.resize()
  })
})
</script>

<style scoped>
.goods-statistics-container {
  padding: 20px;
}
.stat-cards {
  margin-bottom: 20px;
}
.stat-card {
  height: 100px;
}
.stat-content {
  display: flex;
  align-items: center;
  height: 60px;
}
.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 15px;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}
.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}
.chart-container {
  height: 280px;
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
