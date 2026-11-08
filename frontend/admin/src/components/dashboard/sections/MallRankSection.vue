<template>
  <section class="panel mall-rank">
    <div class="panel-header panel-header--row">
      <span class="section-icon section-icon--rank" aria-hidden="true">
        <el-icon><Trophy /></el-icon>
      </span>
      <h3 class="section-title">{{ t('dashboard.rank.title') }}</h3>
      <el-radio-group
        v-if="modes.length > 1"
        :model-value="activeMode"
        size="small"
        class="rank-switch"
        @change="onModeChange"
      >
        <el-radio-button v-for="m in modes" :key="m" :label="m">
          {{ t(`dashboard.rank.mode.${m}`) }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <div class="rank-body">
      <ol v-if="rows.length > 0" class="rank-list">
        <li v-for="(row, index) in rows" :key="row.id ?? row.name" class="rank-row">
          <span class="rank-no" :class="`rank-no--${index + 1}`">{{ index + 1 }}</span>
          <span class="rank-main">
            <span class="rank-name" :title="row.name">{{ row.name }}</span>
            <span class="rank-bar" aria-hidden="true">
              <span class="rank-bar-fill" :style="{ width: barWidth(row) }" />
            </span>
          </span>
          <span class="rank-meta">
            <span class="rank-sales">{{ formatNum(row.sales) }}</span>
            <span class="rank-unit">{{ t(activeMode === 'goods' ? 'dashboard.rank.unit.sales' : 'dashboard.rank.unit.sales') }}</span>
          </span>
        </li>
      </ol>
      <el-empty v-else :description="t('dashboard.rank.empty')" class="rank-empty" />
    </div>
  </section>
</template>

<script setup>
/**
 * 热销商品 / 品类排行（首屏底部）。
 *
 * 商品与品类都来自 /web/mall/goods/list 的真实销量聚合：
 * 商品按销量（真实 + 虚拟）排序，品类按商品所挂最末级分类聚合销量。
 */
import { computed, ref, watch } from 'vue'
import { Trophy } from '@element-plus/icons-vue'
import { useI18n } from '@/i18n-element'
import { RANK_MODES } from '@/config/dashboard/sections'

const { t } = useI18n()

const props = defineProps({
  config: { type: Object, required: true },
  rank: { type: Object, default: () => ({}) }
})

const modes = computed(() => {
  const list = Array.isArray(props.config?.modes) ? props.config.modes : []
  const valid = RANK_MODES.filter((m) => list.includes(m))
  return valid.length ? valid : [...RANK_MODES]
})

const limit = computed(() => Number(props.config?.limit) || 8)

const activeMode = ref(modes.value[0])
watch(modes, (list) => {
  if (!list.includes(activeMode.value)) activeMode.value = list[0]
})

const rows = computed(() => {
  const source = activeMode.value === 'category'
    ? (props.rank?.categoryTop || [])
    : (props.rank?.goodsTop || [])
  return source.slice(0, limit.value)
})

const maxSales = computed(() => rows.value.reduce((max, row) => Math.max(max, Number(row?.sales) || 0), 0))

const barWidth = (row) => {
  if (!maxSales.value) return '0%'
  const ratio = (Number(row?.sales) || 0) / maxSales.value
  return `${Math.max(4, Math.round(ratio * 100))}%`
}

const formatNum = (n) => new Intl.NumberFormat().format(Number(n) || 0)

/** 切换维度只影响本区块展示，不写偏好（避免一次点击就触发整表保存） */
const onModeChange = (value) => {
  activeMode.value = value
}
</script>
