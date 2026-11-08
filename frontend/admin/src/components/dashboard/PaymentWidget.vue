<template>
  <div class="payment-widget">
    <el-skeleton v-if="loading" :rows="3" animated />
    <template v-else>
      <div class="payment-stats">
        <div class="stat-item">
          <div class="stat-value">{{ formatMoney(stats.todayAmount) }}</div>
          <div class="stat-label">{{ t('dashboard.modules.paymentTodayAmount') }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ formatNum(stats.todayCount) }}</div>
          <div class="stat-label">{{ t('dashboard.modules.paymentTransactions') }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.successRate != null ? stats.successRate + '%' : '-' }}</div>
          <div class="stat-label">{{ t('dashboard.modules.paymentSuccessRate') }}</div>
        </div>
      </div>
      <el-empty v-if="!hasData" :image-size="60" :description="t('dashboard.modules.paymentNoData')" />
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { getPaymentOverview } from '@/api/payment/dashboard'

const loading = ref(true)
const stats = ref({ todayAmount: null, todayCount: null, successRate: null, todayRefund: null })

const hasData = computed(() => stats.value.todayAmount != null || stats.value.todayCount != null)
const formatMoney = (n) => (n == null ? '-' : '¥' + new Intl.NumberFormat().format(Math.round(n)))
const formatNum = (n) => (n == null ? '-' : new Intl.NumberFormat().format(n))

/**
 * 加载支付概览：失败则保持 null 占位，不影响整体页面
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getPaymentOverview()
    const d = res?.data || {}
    stats.value = {
      todayAmount: d.todayAmount ?? null,
      todayCount: d.todayCount ?? null,
      successRate: d.successRate ?? null,
      todayRefund: d.todayRefund ?? null
    }
  } catch (_) { /* ignore */ }
  finally { loading.value = false }
}

onMounted(loadData)
defineExpose({ refresh: loadData })
</script>

<style scoped lang="scss">
.payment-widget {
  .payment-stats { display: flex; justify-content: space-between; gap: 8px;
    .stat-item { text-align: center; flex: 1; padding: 8px;
      .stat-value { font-size: 18px; font-weight: 600; color: #303133; }
      .stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
    }
  }
}
</style>
