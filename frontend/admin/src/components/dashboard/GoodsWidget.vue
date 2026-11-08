<template>
  <div class="goods-widget">
    <el-skeleton v-if="loading" :rows="3" animated />
    <template v-else>
      <div class="goods-stats">
        <div class="stat-item">
          <div class="stat-value">{{ formatNum(stats.totalGoods) }}</div>
          <div class="stat-label">{{ t('dashboard.modules.goodsTotal') }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ formatNum(stats.onSale) }}</div>
          <div class="stat-label">{{ t('dashboard.modules.goodsOnSale') }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ formatNum(stats.lowStock) }}</div>
          <div class="stat-label">{{ t('dashboard.modules.goodsLowStock') }}</div>
        </div>
      </div>
      <div class="top-goods" v-if="topGoods.length > 0">
        <div class="section-title">{{ t('dashboard.modules.goodsTopSales') }}</div>
        <div v-for="(g, idx) in topGoods" :key="g.id || idx" class="goods-item">
          <span class="rank">{{ idx + 1 }}</span>
          <span class="name">{{ g.goodsName || g.name || '-' }}</span>
          <span class="sales">{{ formatNum(g.salesCount || g.sales) }}</span>
        </div>
      </div>
      <el-empty v-else :image-size="60" :description="t('dashboard.modules.goodsNoData')" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { goodsStatisticsService } from '@/api/goods/goods'

const loading = ref(true)
const stats = ref({ totalGoods: null, onSale: null, lowStock: null })
const topGoods = ref([])

const formatNum = (n) => (n == null ? '-' : new Intl.NumberFormat().format(n))

/**
 * 加载商品统计 + 热销榜
 */
const loadData = async () => {
  loading.value = true
  try {
    const [overviewRes, topRes] = await Promise.all([
      goodsStatisticsService.getGoodsOverview().catch(() => null),
      goodsStatisticsService.getTopGoods().catch(() => null)
    ])
    const d = overviewRes?.data || {}
    stats.value = {
      totalGoods: d.totalGoods ?? d.total ?? null,
      onSale: d.onSale ?? d.saleCount ?? null,
      lowStock: d.lowStock ?? d.lowStockCount ?? null
    }
    const list = topRes?.data?.records || topRes?.data || []
    topGoods.value = (Array.isArray(list) ? list : []).slice(0, 5)
  } catch (_) { /* ignore */ }
  finally { loading.value = false }
}

onMounted(loadData)
defineExpose({ refresh: loadData })
</script>

<style scoped lang="scss">
.goods-widget {
  .goods-stats { display: flex; justify-content: space-between; gap: 8px; margin-bottom: 12px;
    .stat-item { flex: 1; text-align: center;
      .stat-value { font-size: 18px; font-weight: 600; }
      .stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
    }
  }
  .top-goods .section-title { font-weight: 500; font-size: 13px; margin-bottom: 6px; color: #303133; }
  .goods-item { display: flex; gap: 8px; padding: 6px 0; font-size: 13px; border-bottom: 1px solid #f0f0f0;
    &:last-child { border-bottom: none; }
    .rank { width: 20px; color: #e6a23c; font-weight: 600; }
    .name { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .sales { color: #606266; }
  }
}
</style>
