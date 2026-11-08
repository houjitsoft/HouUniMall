<template>
  <div class="erp-shipment-widget">
    <template v-if="pluginEnabled && loaded">
      <div class="stat-grid">
        <div class="stat">
          <div class="stat-label">待审核销售单</div>
          <div class="stat-value">{{ fmt(pendingExamine) }}</div>
        </div>
        <div class="stat">
          <div class="stat-label">待发货</div>
          <div class="stat-value">{{ fmt(deliveryPending) }}</div>
        </div>
        <div class="stat">
          <div class="stat-label">库存预警</div>
          <div class="stat-value" :style="{ color: stockWarn > 0 ? '#f56c6c' : '#67c23a' }">{{ fmt(stockWarn) }}</div>
        </div>
        <div class="stat">
          <div class="stat-label">本月销售(元)</div>
          <div class="stat-value">¥{{ fmtMoney(monthSales) }}</div>
        </div>
      </div>
      <div class="widget-footer">
        <el-button link type="primary" @click="goErp">进入 ERP 概览 →</el-button>
      </div>
    </template>
    <el-skeleton v-else-if="pluginEnabled" :rows="3" animated />
    <el-empty v-else :image-size="48" description="ERP 插件未安装" />
  </div>
</template>

<script setup>
/**
 * ERP 概览 Widget（替换原“功能开发中”占位）
 * 展示 ERP 关键待办与销售概况，点击进入插件路由 /plugin/erp/dashboard
 */
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { usePluginStore } from '@/stores/plugin'
import request from '@/utils/request'

const router = useRouter()
const pluginStore = usePluginStore()
const loaded = ref(false)
const data = ref({})
const pluginEnabled = computed(() => pluginStore.loadedCodes.includes('erp'))

const pendingExamine = computed(() => Number(data.value.pendingExamine) || 0)
const deliveryPending = computed(() => (data.value.pendingItems || []).filter(i => (i.title || '').includes('待发货')).length > 0
  ? Number((data.value.pendingItems || []).find(i => (i.title || '').includes('待发货')).desc.replace(/[^0-9]/g, '') || 0)
  : 0)
const stockWarn = computed(() => (data.value.stockWarnings || []).length)
const monthSales = computed(() => Number(data.value.monthSales) || 0)

const fmt = (v) => Number(v || 0).toLocaleString('zh-CN')
const fmtMoney = (v) => Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const goErp = () => router.push('/plugin/erp/dashboard')

const load = async () => {
  if (!pluginEnabled.value) return
  loaded.value = false
  try {
    data.value = (await request.get('/web/plugin/erp/dashboard'))?.data || {}
  } catch (e) {
    console.warn('ERP Widget 加载失败', e)
  } finally {
    loaded.value = true
  }
}

watch(pluginEnabled, (enabled) => {
  if (enabled) load()
}, { immediate: true })
</script>

<style scoped lang="scss">
.erp-shipment-widget {
  padding: 4px 2px;
}
.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 14px;
}
.stat {
  .stat-label { font-size: 12px; color: #909399; }
  .stat-value { font-size: 18px; font-weight: 600; color: #303133; margin-top: 2px; }
}
.widget-footer {
  margin-top: 10px;
  border-top: 1px dashed #ebeef5;
  padding-top: 8px;
  text-align: right;
}
</style>
