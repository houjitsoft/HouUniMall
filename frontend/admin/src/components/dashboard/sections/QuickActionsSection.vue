<template>
  <section class="panel quick-actions">
    <div class="panel-header panel-header--row">
      <span class="section-icon section-icon--quick" aria-hidden="true">
        <el-icon><Operation /></el-icon>
      </span>
      <h3 class="section-title">{{ t('dashboard.quickActions.title') }}</h3>
    </div>
    <div class="quick-action-grid">
      <button
        v-for="act in actions"
        :key="act.key"
        type="button"
        class="quick-action-tile"
        @click="emit('navigate', act.route)"
      >
        <span class="qa-icon" :style="{ background: act.gradient || act.color }">
          <el-icon><component :is="act.component" /></el-icon>
        </span>
        <span class="qa-label">{{ t(act.title) }}</span>
      </button>
    </div>
  </section>
</template>

<script setup>
/**
 * 快捷操作。
 * 2026-09-21：六个入口改为可配置（设置抽屉里勾选 / 排序），默认全开且不删功能。
 */
import { computed } from 'vue'
import { Plus, Van, RefreshLeft, Box, Menu, Shop, Operation } from '@element-plus/icons-vue'
import { useI18n } from '@/i18n-element'
import { QUICK_ACTIONS } from '@/config/dashboard/sections'

const { t } = useI18n()

const props = defineProps({
  config: { type: Object, required: true }
})

const emit = defineEmits(['navigate'])

const iconMap = { Plus, Van, RefreshLeft, Box, Menu, Shop }

const actions = computed(() => {
  const order = Array.isArray(props.config?.actions) ? props.config.actions : []
  return order
    .map((key) => QUICK_ACTIONS.find((a) => a.key === key))
    .filter(Boolean)
    .map((meta) => ({ ...meta, component: iconMap[meta.icon] || Plus }))
})
</script>
