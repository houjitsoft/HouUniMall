<template>
  <section class="panel mall-alerts">
    <div class="panel-header panel-header--row">
      <span class="section-icon section-icon--alert" aria-hidden="true">
        <el-icon><WarningFilled /></el-icon>
      </span>
      <h3 class="section-title">{{ t('dashboard.alerts.title') }}</h3>
      <span v-if="total > 0" class="alerts-total">{{ t('dashboard.alerts.pendingTotal') }} {{ total }}</span>
    </div>

    <ul class="alert-list">
      <li v-for="item in items" :key="item.key" class="alert-row">
        <button type="button" class="alert-entry" :class="`alert-entry--${item.level}`" @click="emit('navigate', item.route)">
          <span class="alert-icon" :class="`alert-icon--${item.level}`" aria-hidden="true">
            <el-icon><component :is="item.component" /></el-icon>
          </span>
          <span class="alert-body">
            <span class="alert-title">{{ t(item.title) }}</span>
            <span class="alert-hint">{{ hintOf(item) }}</span>
          </span>
          <span class="alert-count" :class="countClass(item)">{{ countOf(item) }}</span>
          <el-icon class="alert-arrow"><ArrowRight /></el-icon>
        </button>
      </li>
      <li v-if="items.length === 0" class="alert-row alert-row--empty">
        {{ t('dashboard.editMode.empty') }}
      </li>
    </ul>
  </section>
</template>

<script setup>
/**
 * 商城运营待办与预警（首屏右侧）。
 *
 * 待发货 / 待付款 / 退款售后来自订单中心各状态计数；库存预警、下架与违规商品、
 * 待审入驻来自商城预警概览与商品列表聚合。数字为 0 就是 0，不做假数据。
 */
import { computed } from 'vue'
import { Van, Clock, RefreshLeft, Box, SoldOut, WarningFilled, OfficeBuilding, ArrowRight } from '@element-plus/icons-vue'
import { useI18n } from '@/i18n-element'
import { MALL_ALERT_ITEMS } from '@/config/dashboard/sections'

const { t } = useI18n()

const props = defineProps({
  config: { type: Object, required: true },
  alerts: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['navigate'])

const iconMap = { Van, Clock, RefreshLeft, Box, SoldOut, WarningFilled, OfficeBuilding }

const items = computed(() => {
  const order = Array.isArray(props.config?.items) ? props.config.items : []
  return order
    .map((key) => MALL_ALERT_ITEMS.find((i) => i.key === key))
    .filter(Boolean)
    .map((meta) => ({ ...meta, component: iconMap[meta.icon] || WarningFilled }))
})

const countOf = (item) => {
  const value = Number(props.alerts?.[item.field])
  return Number.isFinite(value) ? value : 0
}

const countClass = (item) => {
  const value = countOf(item)
  if (value <= 0) return 'is-zero'
  return item.level === 'danger' ? 'is-danger' : item.level === 'warn' ? 'is-warn' : 'is-normal'
}

const hintOf = (item) => t(`dashboard.alerts.hint.${item.key}`)

/** 只统计「需要人处理」的项（下架商品属于状态展示，不计入待办总数） */
const total = computed(() =>
  items.value
    .filter((item) => item.key !== 'offShelf')
    .reduce((acc, item) => acc + countOf(item), 0)
)
</script>
