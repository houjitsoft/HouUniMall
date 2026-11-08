<template>
  <div class="order-widget">
    <el-skeleton v-if="loading" :rows="3" animated />
    <template v-else>
      <div class="order-stats">
        <div class="stat-item">
          <div class="stat-value">{{ formatNum(stats.todayOrders) }}</div>
          <div class="stat-label">{{ t('dashboard.modules.orderToday') }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ formatNum(stats.pendingOrders) }}</div>
          <div class="stat-label">{{ t('dashboard.modules.orderPending') }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ formatMoney(stats.todayRevenue) }}</div>
          <div class="stat-label">{{ t('dashboard.modules.orderRevenue') }}</div>
        </div>
      </div>
      <div class="recent-orders" v-if="recentOrders.length > 0">
        <div class="section-title">{{ t('dashboard.modules.orderRecent') }}</div>
        <div v-for="order in recentOrders" :key="order.id || order.orderNo" class="order-item">
          <div class="order-info">
            <div class="order-number">{{ order.orderNo || order.number || order.id }}</div>
            <el-tag :type="getOrderStatusType(order.status)" size="small">
              {{ t(getOrderStatusKey(order.status)) }}
            </el-tag>
          </div>
          <div class="order-amount">{{ formatMoney(order.amount || order.totalAmount) }}</div>
        </div>
      </div>
      <el-empty v-else :image-size="60" :description="t('dashboard.modules.orderNoData')" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { orderService } from '@/api/order/order'

const loading = ref(true)
const stats = ref({ todayOrders: null, pendingOrders: null, todayRevenue: null })
const recentOrders = ref([])

const formatNum = (n) => (n == null ? '-' : new Intl.NumberFormat().format(n))
const formatMoney = (n) => (n == null ? '-' : '¥' + new Intl.NumberFormat().format(Math.round(Number(n) || 0)))
const getOrderStatusKey = (s) => ({
  pending: 'dashboard.modules.orderStatusPending',
  shipped: 'dashboard.modules.orderStatusShipped',
  completed: 'dashboard.modules.orderStatusCompleted',
  cancelled: 'dashboard.modules.orderStatusCancelled'
}[s] || 'dashboard.modules.orderStatusPending')
const getOrderStatusType = (s) => ({ pending: 'warning', shipped: 'primary', completed: 'success', cancelled: 'info' }[s] || 'info')

/**
 * 加载订单统计 + 最近 5 条订单
 */
const loadData = async () => {
  loading.value = true
  try {
    const [statRes, listRes] = await Promise.all([
      orderService.getOrderStatistics ? orderService.getOrderStatistics({}).catch(() => null) : null,
      orderService.queryOrderList ? orderService.queryOrderList({ page: 1, pageSize: 5 }).catch(() => null) : null
    ])
    const sd = statRes?.data || {}
    stats.value = {
      todayOrders: sd.todayOrders ?? sd.todayCount ?? null,
      pendingOrders: sd.pendingOrders ?? sd.pendingCount ?? null,
      todayRevenue: sd.todayRevenue ?? sd.todayAmount ?? null
    }
    const records = listRes?.data?.records || listRes?.data?.list || (Array.isArray(listRes?.data) ? listRes.data : [])
    recentOrders.value = (records || []).slice(0, 5)
  } catch (_) { /* ignore */ }
  finally { loading.value = false }
}

onMounted(loadData)
defineExpose({ refresh: loadData })
</script>

<style scoped lang="scss">
.order-widget {
  .order-stats { display: flex; justify-content: space-between; gap: 8px; margin-bottom: 12px;
    .stat-item { text-align: center; flex: 1;
      .stat-value { font-size: 18px; font-weight: 600; }
      .stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
    }
  }
  .recent-orders .section-title { font-weight: 500; font-size: 13px; margin-bottom: 6px; color: #303133; }
  .order-item { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid #f0f0f0; font-size: 13px;
    &:last-child { border-bottom: none; }
    .order-info { display: flex; gap: 8px; align-items: center; flex: 1; min-width: 0;
      .order-number { color: #606266; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 140px; }
    }
    .order-amount { color: #303133; font-weight: 500; }
  }
}
</style>
