<template>
  <section class="panel chart-card">
    <div class="panel-header chart-header">
      <span class="section-icon section-icon--chart" aria-hidden="true">
        <el-icon><DataLine /></el-icon>
      </span>
      <h3 class="section-title">{{ t('dashboard.charts.trend') }}</h3>
      <el-radio-group :model-value="days" size="small" class="chart-range" @change="onDaysChange">
        <el-radio-button v-for="d in TREND_DAY_OPTIONS" :key="d" :label="d">
          {{ d === 7 ? t('dashboard.charts.last7') : t('dashboard.charts.last30') }}
        </el-radio-button>
      </el-radio-group>
    </div>
    <div class="chart-body">
      <v-chart v-if="rows.length > 0" class="trend-chart" :option="trendOption" autoresize />
      <div v-else class="chart-empty">
        <el-icon class="chart-empty-icon"><DataLine /></el-icon>
        <p class="chart-empty-text">{{ t('dashboard.charts.empty') }}</p>
        <p class="chart-empty-hint">{{ t('dashboard.charts.emptyHint') }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
/**
 * 商城交易趋势图。
 *
 * 数据来自 /web/mall/dashboard/sales-trend（按日聚合，后端返回近 30 天），
 * 前端按所选天数截取。没有数据就显示空态 —— 不再用 Math.random() 造数，
 * 每次刷新数字都在变的假趋势比「暂无数据」更容易误导经营判断。
 */
import { computed, ref, watch } from 'vue'
import { DataLine } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { graphic } from 'echarts/core'
import { useI18n } from '@/i18n-element'
import { TREND_SERIES, TREND_DAY_OPTIONS } from '@/config/dashboard/sections'

const { t } = useI18n()

const props = defineProps({
  config: { type: Object, required: true },
  /** 趋势明细：[{ date, amount, count }]（按日期升序） */
  trend: { type: Array, default: () => [] },
  isMobile: { type: Boolean, default: false }
})

const emit = defineEmits(['update-config'])

const days = computed(() => Number(props.config?.days) || 7)
const activeSeries = computed(() => {
  const list = Array.isArray(props.config?.series) ? props.config.series : []
  return TREND_SERIES.filter((s) => list.includes(s.key))
})

const rows = computed(() => {
  const list = Array.isArray(props.trend) ? props.trend : []
  return list.slice(-days.value)
})

const trendOption = ref({})

const renderTrend = () => {
  const data = rows.value
  if (data.length === 0) {
    trendOption.value = {}
    return
  }
  const labels = data.map((row) => {
    const date = String(row.date || '')
    return date.length >= 10 ? date.slice(5) : date
  })
  const pool = {
    revenue: data.map((row) => Number(row.amount) || 0),
    order: data.map((row) => Number(row.count) || 0)
  }
  const grid = props.isMobile
    ? { left: 8, right: 8, top: 40, bottom: 24, containLabel: true }
    : { left: 56, right: 52, top: 44, bottom: 32 }
  /* 主序列沿用 Element Plus 蓝，订单使用成功绿；常态只留线，悬浮出数据点。 */
  const palette = {
    revenue: {
      line: '#409eff',
      area: new graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(64, 158, 255, 0.2)' },
        { offset: 1, color: 'rgba(64, 158, 255, 0.015)' }
      ])
    },
    order: {
      line: '#67c23a',
      area: new graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(103, 194, 58, 0.14)' },
        { offset: 1, color: 'rgba(103, 194, 58, 0.01)' }
      ])
    }
  }
  trendOption.value = {
    animationDuration: 600,
    animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: 'rgba(15, 23, 42, 0.08)',
      borderWidth: 1,
      padding: [10, 14],
      extraCssText: 'border-radius:10px;box-shadow:0 10px 30px rgba(15,23,42,.12);',
      textStyle: { color: '#1f2937', fontSize: 12 },
      axisPointer: {
        type: 'line',
        lineStyle: { color: 'rgba(64, 158, 255, 0.36)', type: 'dashed' }
      }
    },
    legend: {
      data: activeSeries.value.map((s) => t(s.title)),
      top: 0,
      right: 0,
      itemWidth: 14,
      itemHeight: 4,
      icon: 'roundRect',
      textStyle: { color: '#6b7280', fontSize: 12 }
    },
    grid,
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLine: { lineStyle: { color: 'rgba(15, 23, 42, 0.12)' } },
      axisTick: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 11, hideOverlap: true }
    },
    yAxis: [
      {
        type: 'value',
        name: t('dashboard.charts.revenue'),
        nameTextStyle: { color: '#9ca3af', fontSize: 11, padding: [0, 0, 0, -24] },
        splitLine: { lineStyle: { type: 'dashed', color: 'rgba(15, 23, 42, 0.08)' } },
        axisLabel: { color: '#9ca3af', fontSize: 11 }
      },
      {
        type: 'value',
        name: t('dashboard.charts.order'),
        nameTextStyle: { color: '#9ca3af', fontSize: 11, padding: [0, -20, 0, 0] },
        splitLine: { show: false },
        axisLabel: { color: '#9ca3af', fontSize: 11 }
      }
    ],
    series: activeSeries.value.map((s, index) => {
      const color = palette[s.key] || palette.revenue
      return {
        name: t(s.title),
        type: 'line',
        smooth: 0.45,
        yAxisIndex: index,
        data: pool[s.key] || [],
        showSymbol: false,
        symbol: 'circle',
        symbolSize: 7,
        emphasis: { scale: 1.4 },
        lineStyle: { width: 2.5, color: color.line },
        areaStyle: s.key === 'revenue' || activeSeries.value.length === 1
          ? { color: color.area }
          : undefined,
        itemStyle: {
          color: color.line,
          borderColor: '#fff',
          borderWidth: 2
        }
      }
    })
  }
}

const onDaysChange = (value) => emit('update-config', { days: Number(value) || 7 })

watch(
  () => [
    days.value,
    rows.value.map((r) => `${r.date}-${r.amount}-${r.count}`).join(','),
    activeSeries.value.map((s) => s.key).join(','),
    props.isMobile
  ],
  () => renderTrend(),
  { immediate: true }
)

defineExpose({ refresh: renderTrend })
</script>
