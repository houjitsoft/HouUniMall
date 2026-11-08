<template>
  <section class="mall-kpi">
    <div class="mall-kpi-grid">
      <article
        v-for="card in cards"
        :key="card.key"
        class="kpi-card"
        :class="[`kpi-card--${card.theme}`, { 'is-clickable': !!card.route }]"
        @click="onCardClick(card)"
      >
        <div class="kpi-head">
          <span class="kpi-icon" aria-hidden="true">
            <el-icon><component :is="card.component" /></el-icon>
          </span>
          <span class="kpi-label">{{ t(card.title) }}</span>
        </div>
        <div class="kpi-value">{{ formatValue(card) }}</div>
        <div class="kpi-foot">
          <template v-if="card.trendKey">
            <span v-if="trendOf(card) === null" class="kpi-trend kpi-trend--flat">
              {{ t('dashboard.kpi.noCompare') }}
            </span>
            <span v-else class="kpi-trend" :class="trendOf(card) >= 0 ? 'is-up' : 'is-down'">
              <el-icon class="kpi-trend-icon">
                <component :is="trendOf(card) >= 0 ? CaretTop : CaretBottom" />
              </el-icon>
              {{ formatTrend(trendOf(card)) }}
            </span>
            <span class="kpi-compare">{{ t(card.compareKey) }}</span>
          </template>
          <span v-else class="kpi-trend kpi-trend--flat">{{ t('dashboard.kpi.todayOnly') }}</span>
        </div>
      </article>
      <el-empty
        v-if="cards.length === 0"
        :description="t('dashboard.editMode.empty')"
        class="mall-kpi-empty"
      />
    </div>
  </section>
</template>

<script setup>
/**
 * 商城核心经营指标（首屏第一屏主体）。
 *
 * 数据全部来自真实接口聚合（@/composables/useMallOverview），
 * 取不到就是 0、环比算不出来显示「—」，不用随机数造数。
 * 卡片的取舍仍可在设置抽屉里勾选，与旧的统计概览区块是同一套偏好（id 仍为 stats）。
 */
import { computed } from 'vue'
import {
  Wallet, List, Tickets, User, Money, CreditCard, RefreshLeft, TrendCharts,
  CaretTop, CaretBottom
} from '@element-plus/icons-vue'
import { useI18n } from '@/i18n-element'
import { MALL_KPI_CARDS } from '@/config/dashboard/sections'

const { t } = useI18n()

const props = defineProps({
  config: { type: Object, required: true },
  kpi: { type: Object, default: () => ({}) },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['navigate'])

const iconMap = { Wallet, List, Tickets, User, Money, CreditCard, RefreshLeft, TrendCharts }

const cards = computed(() => {
  const order = Array.isArray(props.config?.cards) ? props.config.cards : []
  return order
    .map((key) => MALL_KPI_CARDS.find((c) => c.key === key))
    .filter(Boolean)
    .map((meta) => ({ ...meta, component: iconMap[meta.icon] || Wallet }))
})

const trendOf = (card) => {
  if (!card.trendKey) return null
  const value = props.kpi?.trends?.[card.trendKey]
  return typeof value === 'number' && Number.isFinite(value) ? value : null
}

const formatNum = (n) => (n == null ? '—' : new Intl.NumberFormat().format(Math.round(n)))
const formatMoney = (n) => (n == null ? '—' : `¥${new Intl.NumberFormat().format(Math.round(n))}`)
const formatPercent = (n) => (n == null ? '—' : `${Number(n).toFixed(1)}%`)

const formatValue = (card) => {
  const value = props.kpi?.[card.field]
  if (value === undefined || value === null) return '—'
  if (card.valueType === 'money') return formatMoney(value)
  if (card.valueType === 'percent') return formatPercent(value)
  return formatNum(value)
}

const formatTrend = (value) => `${Math.abs(value).toFixed(1)}%`

const onCardClick = (card) => {
  if (!card.route) return
  emit('navigate', card.route)
}
</script>
