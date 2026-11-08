<template>
  <!-- 国补标识：仅在后端确认命中且金额校验通过时渲染，未命中整块不出现 -->
  <div v-if="visible" class="subsidy-badge" :title="title">
    <span class="subsidy-badge__tag">{{ $t('国家补贴') }}</span>
    <span class="subsidy-badge__text">{{ text }}</span>
  </div>
  <!-- 未命中但有互斥原因：解释为什么用券后没国补，而不是静默消失 -->
  <div v-else-if="hint" class="subsidy-hint">{{ hint }}</div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  exclusiveHint,
  rateText,
  shouldShowSubsidy,
  type SubsidyQuote
} from '~/utils/subsidy'

const props = defineProps<{
  /** 后端试算结果；null 表示插件未安装 */
  quote?: SubsidyQuote | null
  /** 行级商品 ID：传入时只显示该行可减金额（购物车/详情列表用） */
  goodsId?: number | null
}>()

const visible = computed(() => shouldShowSubsidy(props.quote))
const hint = computed(() => exclusiveHint(props.quote))

const amount = computed(() => {
  if (!props.quote) return 0
  const total = Number(props.quote.subsidyAmount || 0)
  if (props.goodsId === null || props.goodsId === undefined) return total
  const line = (props.quote.lines || []).find((item) => item?.goodsId === props.goodsId)
  return line ? Number(line.finalAmount || 0) : 0
})

const rate = computed(() => rateText(props.quote?.subsidyRate))

const text = computed(() => {
  const value = amount.value > 0 ? amount.value.toFixed(2) : '0.00'
  return rate.value
    ? `${$t('补贴')} ${rate.value}，${$t('本单可减')} ¥${value}`
    : `${$t('本单可减')} ¥${value}`
})

const title = computed(() => props.quote?.ruleName || $t('国家补贴'))
</script>

<style scoped lang="scss">
.subsidy-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  padding: 3px 8px;
  border: 1px solid #fecaca;
  border-radius: 4px;
  background: #fef2f2;
  font-size: 12px;
  line-height: 18px;
}

.subsidy-badge__tag {
  padding: 0 5px;
  border-radius: 3px;
  background: #dc2626;
  color: #fff;
  font-weight: 600;
}

.subsidy-badge__text {
  color: #b91c1c;
}

.subsidy-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #9ca3af;
}
</style>
