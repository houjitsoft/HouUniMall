<template>
  <div class="data-analysis">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>今日访问量</span>
            </div>
          </template>
          <div class="card-content">
            <div class="number">1,234</div>
            <div class="trend up">+12.5%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>今日订单数</span>
            </div>
          </template>
          <div class="card-content">
            <div class="number">89</div>
            <div class="trend up">+8.3%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>今日销售额</span>
            </div>
          </template>
          <div class="card-content">
            <div class="number">¥12,580</div>
            <div class="trend up">+15.2%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>新增用户</span>
            </div>
          </template>
          <div class="card-content">
            <div class="number">23</div>
            <div class="trend down">-2.1%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>销售趋势</span>
              <el-select v-model="timeRange" size="small" style="width: 120px;">
                <el-option label="最近7天" value="7" />
                <el-option label="最近30天" value="30" />
                <el-option label="最近90天" value="90" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-placeholder">
              销售趋势图表（需要集成ECharts）
              <br>
              <small>显示最近{{ timeRange }}天的销售数据</small>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>用户分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-placeholder">
              用户分布饼图（需要集成ECharts）
              <br>
              <small>按地区、年龄、性别等维度统计</small>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>商品销售排行</span>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="small"
                style="width: 250px;"
              />
            </div>
          </template>
          <el-table :data="productRanking" style="width: 100%">
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column prop="name" label="商品名称" width="300" />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="sales" label="销量" width="100" />
            <el-table-column prop="revenue" label="销售额" width="120">
              <template #default="scope">
                ¥{{ scope.row.revenue }}
              </template>
            </el-table-column>
            <el-table-column prop="growth" label="增长率" width="100">
              <template #default="scope">
                <span :class="scope.row.growth >= 0 ? 'trend up' : 'trend down'">
                  {{ scope.row.growth >= 0 ? '+' : '' }}{{ scope.row.growth }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>实时监控</span>
            </div>
          </template>
          <div class="realtime-data">
            <div class="realtime-item">
              <div class="label">在线用户</div>
              <div class="value">156</div>
            </div>
            <div class="realtime-item">
              <div class="label">活跃订单</div>
              <div class="value">23</div>
            </div>
            <div class="realtime-item">
              <div class="label">系统负载</div>
              <div class="value">45%</div>
            </div>
            <div class="realtime-item">
              <div class="label">响应时间</div>
              <div class="value">120ms</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>预警信息</span>
            </div>
          </template>
          <div class="alert-list">
            <div v-for="alert in alertList" :key="alert.id" class="alert-item">
              <el-tag :type="alert.level" size="small">{{ alert.levelText }}</el-tag>
              <span class="alert-content">{{ alert.content }}</span>
              <span class="alert-time">{{ alert.time }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

// 响应式数据
const timeRange = ref('7')
const dateRange = ref([])

// 商品销售排行数据
const productRanking = ref([
  { rank: 1, name: 'iPhone 15 Pro', category: '数码产品', sales: 45, revenue: 404955, growth: 12.5 },
  { rank: 2, name: 'AirPods Pro', category: '数码产品', sales: 38, revenue: 75962, growth: 8.3 },
  { rank: 3, name: 'Nike Air Max', category: '运动鞋', sales: 32, revenue: 28768, growth: 15.2 },
  { rank: 4, name: '星巴克咖啡豆', category: '食品饮料', sales: 156, revenue: 10608, growth: -2.1 },
  { rank: 5, name: '华为Mate 60', category: '数码产品', sales: 28, revenue: 196000, growth: 25.8 }
])

// 预警信息数据
const alertList = ref([
  { id: 1, level: 'warning', levelText: '警告', content: '商品库存不足', time: '10分钟前' },
  { id: 2, level: 'info', levelText: '信息', content: '系统备份完成', time: '30分钟前' },
  { id: 3, level: 'success', levelText: '正常', content: '所有服务运行正常', time: '1小时前' },
  { id: 4, level: 'error', levelText: '错误', content: '支付接口异常', time: '2小时前' }
])
</script>

<style scoped>
.data-analysis {
  padding: 20px;
}

.card-content {
  text-align: center;
  padding: 20px 0;
}

.card-content .number {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.trend {
  font-size: 14px;
  font-weight: 500;
}

.trend.up {
  color: #67C23A;
}

.trend.down {
  color: #F56C6C;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  color: #999;
  font-size: 16px;
  text-align: center;
  padding: 40px;
  border: 2px dashed #ddd;
  border-radius: 8px;
}

.chart-placeholder small {
  color: #bbb;
  font-size: 12px;
}

.realtime-data {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 20px 0;
}

.realtime-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.realtime-item .label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.realtime-item .value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.alert-list {
  max-height: 300px;
  overflow-y: auto;
}

.alert-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-content {
  flex: 1;
  margin: 0 15px;
}

.alert-time {
  color: #999;
  font-size: 12px;
}
</style>
