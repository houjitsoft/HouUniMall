<template>
  <div class="order-analysis-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>订单分析</h2>
        <span class="subtitle">深度分析订单数据，发现业务洞察</span>
      </div>
      <div class="header-right">
        <el-button icon="Download" @click="handleExportAnalysis">
          导出分析报告
        </el-button>
        <el-button icon="Share" @click="handleShareAnalysis">
          分享分析
        </el-button>
      </div>
    </div>

    <!-- 分析维度选择 -->
    <el-card class="analysis-filter">
      <el-tabs v-model="activeAnalysis" @tab-change="handleAnalysisChange">
        <el-tab-pane label="用户行为分析" name="user">
          <div class="filter-content">
            <el-form :model="userAnalysisForm" inline>
              <el-form-item label="分析周期">
                <el-date-picker
                  v-model="userAnalysisForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 300px"
                />
              </el-form-item>
              <el-form-item label="用户分群">
                <el-select 
                  v-model="userAnalysisForm.userGroup" 
                  placeholder="选择用户群体"
                  style="width: 150px"
                >
                  <el-option label="全部用户" value="all" />
                  <el-option label="新用户" value="new" />
                  <el-option label="老用户" value="old" />
                  <el-option label="活跃用户" value="active" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="analyzeUserBehavior">
                  开始分析
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="商品销售分析" name="product">
          <div class="filter-content">
            <el-form :model="productAnalysisForm" inline>
              <el-form-item label="分析周期">
                <el-date-picker
                  v-model="productAnalysisForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 300px"
                />
              </el-form-item>
              <el-form-item label="商品分类">
                <el-select 
                  v-model="productAnalysisForm.category" 
                  placeholder="选择分类"
                  style="width: 150px"
                >
                  <el-option label="全部分类" value="all" />
                  <el-option label="电子产品" value="electronics" />
                  <el-option label="服装鞋帽" value="clothing" />
                  <el-option label="家居用品" value="home" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="analyzeProductSales">
                  开始分析
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="时间趋势分析" name="trend">
          <div class="filter-content">
            <el-form :model="trendAnalysisForm" inline>
              <el-form-item label="分析周期">
                <el-date-picker
                  v-model="trendAnalysisForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 300px"
                />
              </el-form-item>
              <el-form-item label="时间粒度">
                <el-select 
                  v-model="trendAnalysisForm.granularity" 
                  placeholder="选择粒度"
                  style="width: 120px"
                >
                  <el-option label="按小时" value="hour" />
                  <el-option label="按天" value="day" />
                  <el-option label="按周" value="week" />
                  <el-option label="按月" value="month" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="analyzeTrend">
                  开始分析
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 分析结果展示 -->
    <div v-if="activeAnalysis === 'user'" class="analysis-results">
      <!-- 用户行为分析结果 -->
      <el-row :gutter="16">
        <el-col :span="8">
          <el-card class="insight-card">
            <template #header>
              <span>用户活跃度</span>
            </template>
            <div class="insight-content">
              <div class="insight-value">{{ userInsights.activeRate }}%</div>
              <div class="insight-desc">用户活跃率较上周提升了8.5%</div>
              <div ref="userActiveChart" class="mini-chart"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="insight-card">
            <template #header>
              <span>复购率</span>
            </template>
            <div class="insight-content">
              <div class="insight-value">{{ userInsights.repurchaseRate }}%</div>
              <div class="insight-desc">复购率稳定在35%左右</div>
              <div ref="repurchaseChart" class="mini-chart"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="insight-card">
            <template #header>
              <span>用户留存</span>
            </template>
            <div class="insight-content">
              <div class="insight-value">{{ userInsights.retentionRate }}%</div>
              <div class="insight-desc">7日留存率有所上升</div>
              <div ref="retentionChart" class="mini-chart"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 用户行为路径分析 -->
      <el-card class="behavior-analysis" style="margin-top: 16px">
        <template #header>
          <div class="card-header">
            <span>用户行为路径分析</span>
            <el-button text @click="viewUserJourney">查看详细路径</el-button>
          </div>
        </template>
        <div ref="behaviorFlowChart" class="large-chart"></div>
      </el-card>
    </div>

    <div v-if="activeAnalysis === 'product'" class="analysis-results">
      <!-- 商品销售分析结果 -->
      <el-row :gutter="16">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span>热销商品TOP10</span>
            </template>
            <el-table :data="topProducts" style="width: 100%">
              <el-table-column prop="rank" label="排名" width="60" />
              <el-table-column prop="name" label="商品名称" min-width="200" />
              <el-table-column prop="sales" label="销量" width="80" />
              <el-table-column label="销售额" width="100">
                <template #default="{ row }">
                  <span class="amount">¥{{ row.revenue }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span>商品销售趋势</span>
            </template>
            <div ref="productTrendChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分类销售分析 -->
      <el-card class="category-analysis" style="margin-top: 16px">
        <template #header>
          <span>分类销售分析</span>
        </template>
        <div ref="categoryChart" class="large-chart"></div>
      </el-card>
    </div>

    <div v-if="activeAnalysis === 'trend'" class="analysis-results">
      <!-- 时间趋势分析结果 -->
      <el-card class="trend-analysis">
        <template #header>
          <div class="card-header">
            <span>订单时间趋势分析</span>
            <div class="trend-indicators">
              <el-tag type="success">上升趋势</el-tag>
              <el-tag type="warning">周期性波动</el-tag>
            </div>
          </div>
        </template>
        <div ref="trendAnalysisChart" class="large-chart"></div>
      </el-card>

      <!-- 周期性分析 -->
      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="8">
          <el-card class="cycle-card">
            <template #header>
              <span>日内高峰分析</span>
            </template>
            <div class="cycle-content">
              <div class="peak-time">
                <div class="time">14:00 - 16:00</div>
                <div class="desc">订单高峰期</div>
              </div>
              <div class="peak-time">
                <div class="time">20:00 - 22:00</div>
                <div class="desc">次高峰期</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="cycle-card">
            <template #header>
              <span>周度规律分析</span>
            </template>
            <div class="cycle-content">
              <div class="peak-time">
                <div class="time">周五 - 周日</div>
                <div class="desc">订单活跃期</div>
              </div>
              <div class="peak-time">
                <div class="time">周二 - 周三</div>
                <div class="desc">订单低谷期</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="cycle-card">
            <template #header>
              <span>月度趋势分析</span>
            </template>
            <div class="cycle-content">
              <div class="peak-time">
                <div class="time">月初 & 月末</div>
                <div class="desc">订单集中期</div>
              </div>
              <div class="peak-time">
                <div class="time">月中</div>
                <div class="desc">相对平缓期</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分析报告生成 -->
    <el-card class="report-section" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>智能分析报告</span>
          <el-button type="primary" @click="generateReport">
            生成完整报告
          </el-button>
        </div>
      </template>
      
      <div class="report-content">
        <div class="analysis-summary">
          <h4>核心发现</h4>
          <ul>
            <li>订单量呈现稳定上升趋势，周环比增长12.5%</li>
            <li>用户复购率保持在35%的良好水平</li>
            <li>电子产品类目销售表现突出，占总销售额的45%</li>
            <li>下午14-16点和晚上20-22点为订单高峰时段</li>
          </ul>
        </div>
        
        <div class="recommendations">
          <h4>优化建议</h4>
          <ul>
            <li>在订单高峰期增加客服人员，提升服务质量</li>
            <li>针对低活跃用户推出定向营销活动</li>
            <li>加强电子产品类目的库存管理和营销投入</li>
            <li>优化周二、周三的促销策略，激活订单需求</li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const activeAnalysis = ref('user')

// 表单数据
const userAnalysisForm = reactive({
  dateRange: [
    new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000),
    new Date()
  ],
  userGroup: 'all'
})

const productAnalysisForm = reactive({
  dateRange: [
    new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000),
    new Date()
  ],
  category: 'all'
})

const trendAnalysisForm = reactive({
  dateRange: [
    new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000),
    new Date()
  ],
  granularity: 'day'
})

// 分析洞察数据
const userInsights = reactive({
  activeRate: 68.5,
  repurchaseRate: 35.2,
  retentionRate: 72.8
})

// 热销商品数据
const topProducts = ref([
  { rank: 1, name: '苹果iPhone 15 Pro Max', sales: 1250, revenue: '12,487,500' },
  { rank: 2, name: '华为Mate60 Pro', sales: 980, revenue: '7,840,200' },
  { rank: 3, name: '小米14 Ultra', sales: 756, revenue: '5,288,400' },
  { rank: 4, name: 'iPad Pro 12.9英寸', sales: 543, revenue: '4,887,000' },
  { rank: 5, name: '戴森吹风机', sales: 432, revenue: '1,728,000' }
])

// 图表引用
const userActiveChart = ref(null)
const repurchaseChart = ref(null)
const retentionChart = ref(null)
const behaviorFlowChart = ref(null)
const productTrendChart = ref(null)
const categoryChart = ref(null)
const trendAnalysisChart = ref(null)

// 方法
const handleAnalysisChange = (tabName) => {
  activeAnalysis.value = tabName
  initChartsForCurrentTab()
}

const analyzeUserBehavior = () => {
  ElMessage.success('用户行为分析完成')
  initChartsForCurrentTab()
}

const analyzeProductSales = () => {
  ElMessage.success('商品销售分析完成')
  initChartsForCurrentTab()
}

const analyzeTrend = () => {
  ElMessage.success('时间趋势分析完成')
  initChartsForCurrentTab()
}

const handleExportAnalysis = () => {
  ElMessage.info('导出分析报告')
}

const handleShareAnalysis = () => {
  ElMessage.info('分享分析结果')
}

const viewUserJourney = () => {
  ElMessage.info('查看用户行为路径详情')
}

const generateReport = () => {
  ElMessage.success('正在生成完整分析报告...')
}

// 初始化当前标签页的图表
const initChartsForCurrentTab = () => {
  nextTick(() => {
    if (activeAnalysis.value === 'user') {
      initUserCharts()
    } else if (activeAnalysis.value === 'product') {
      initProductCharts()
    } else if (activeAnalysis.value === 'trend') {
      initTrendCharts()
    }
  })
}

const initUserCharts = () => {
  if (userActiveChart.value) {
    userActiveChart.value.innerHTML = '<div style="height: 60px; display: flex; align-items: center; justify-content: center; color: #909399; font-size: 12px;">活跃度趋势</div>'
  }
  if (repurchaseChart.value) {
    repurchaseChart.value.innerHTML = '<div style="height: 60px; display: flex; align-items: center; justify-content: center; color: #909399; font-size: 12px;">复购率趋势</div>'
  }
  if (retentionChart.value) {
    retentionChart.value.innerHTML = '<div style="height: 60px; display: flex; align-items: center; justify-content: center; color: #909399; font-size: 12px;">留存率趋势</div>'
  }
  if (behaviorFlowChart.value) {
    behaviorFlowChart.value.innerHTML = '<div style="height: 400px; display: flex; align-items: center; justify-content: center; color: #909399;">用户行为路径图</div>'
  }
}

const initProductCharts = () => {
  if (productTrendChart.value) {
    productTrendChart.value.innerHTML = '<div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #909399;">商品销售趋势图</div>'
  }
  if (categoryChart.value) {
    categoryChart.value.innerHTML = '<div style="height: 400px; display: flex; align-items: center; justify-content: center; color: #909399;">分类销售分析图</div>'
  }
}

const initTrendCharts = () => {
  if (trendAnalysisChart.value) {
    trendAnalysisChart.value.innerHTML = '<div style="height: 400px; display: flex; align-items: center; justify-content: center; color: #909399;">时间趋势分析图</div>'
  }
}

// 初始化
onMounted(() => {
  initChartsForCurrentTab()
})
</script>

<style scoped>
.order-analysis-container {
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

.analysis-filter {
  margin-bottom: 20px;
}

.filter-content {
  padding-top: 16px;
}

.analysis-results {
  margin-bottom: 20px;
}

.insight-card {
  height: 180px;
}

.insight-content {
  text-align: center;
}

.insight-value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.insight-desc {
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}

.mini-chart {
  height: 60px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.chart-container {
  height: 300px;
}

.large-chart {
  height: 400px;
  background-color: #f8f9fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trend-indicators {
  display: flex;
  gap: 8px;
}

.cycle-card {
  height: 160px;
}

.cycle-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.peak-time {
  text-align: center;
}

.peak-time .time {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.peak-time .desc {
  color: #909399;
  font-size: 14px;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}

.report-section {
  margin-top: 20px;
}

.report-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}

.analysis-summary h4,
.recommendations h4 {
  color: #303133;
  margin-bottom: 16px;
}

.analysis-summary ul,
.recommendations ul {
  padding-left: 20px;
}

.analysis-summary li,
.recommendations li {
  margin-bottom: 8px;
  color: #606266;
  line-height: 1.6;
}
</style>