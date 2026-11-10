<template>
  <section class="widgets-section">
    <div v-if="!isMobile && config.showTitle" class="section-head">
      <h3 class="section-title">工作台模块</h3>
      <span v-if="editMode" class="section-hint">拖拽卡片可调整顺序</span>
    </div>
    <div
      ref="gridRef"
      class="widget-grid"
      :class="{ 'is-edit': editMode }"
      :style="{ 'grid-template-columns': `repeat(${effectiveCols}, minmax(0, 1fr))` }"
    >
      <div
        v-for="item in items"
        :key="item.moduleId"
        class="widget-cell"
        :data-id="item.moduleId"
        :style="{ 'grid-column': `span ${widgetSpan(item.w)}` }"
      >
        <el-card class="dashboard-card" shadow="hover">
          <template #header>
            <div class="widget-header">
              <span class="widget-title">
                <el-icon class="widget-icon" :style="{ color: item._meta.color }">
                  <component :is="item._meta.icon" />
                </el-icon>
                {{ t(item._meta.title) }}
              </span>
              <div class="widget-actions">
                <el-tooltip v-if="editMode" :content="t('dashboard.editMode.hide')" placement="top">
                  <el-button size="small" :icon="Hide" circle @click.stop="emit('hide-item', item.moduleId)" />
                </el-tooltip>
                <el-tooltip v-if="editMode" :content="t('dashboard.editMode.drag')" placement="top">
                  <el-button size="small" :icon="Rank" circle class="drag-handle" />
                </el-tooltip>
                <el-tooltip v-else :content="t('dashboard.toolbar.open')" placement="top">
                  <el-button size="small" :icon="ArrowRight" circle @click.stop="emit('navigate', item._meta.route)" />
                </el-tooltip>
              </div>
            </div>
          </template>
          <Suspense>
            <component :is="item._meta.component" />
            <template #fallback>
              <div class="widget-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>{{ t('dashboard.toolbar.loading') }}</span>
              </div>
            </template>
          </Suspense>
        </el-card>
      </div>
      <el-empty
        v-if="items.length === 0"
        class="widget-empty"
        :image-size="80"
        :description="t('dashboard.editMode.empty')"
      />
    </div>
  </section>
</template>

<script setup>
/**
 * 工作台模块网格（唯一带 widget 级配置的区块）。
 * 卡片显隐 / 排序 / 列数沿用原有偏好体系，这里只把区块外壳抽出来，
 * 新增「显示区块标题」开关。拖拽仍由 Sortable 承担，handle 是 .drag-handle。
 */
import { computed, ref, watch, onBeforeUnmount, nextTick } from 'vue'
import { Rank, Hide, ArrowRight, Loading } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { useI18n } from '@/i18n-element'

const { t } = useI18n()

const props = defineProps({
  config: { type: Object, required: true },
  items: { type: Array, default: () => [] },
  cols: { type: Number, default: 12 },
  editMode: { type: Boolean, default: false },
  isMobile: { type: Boolean, default: false }
})

const emit = defineEmits(['hide-item', 'reorder', 'navigate'])

const effectiveCols = computed(() => props.isMobile
  ? 1
  : Math.min(24, Math.max(1, Number.isFinite(props.cols) ? Math.floor(props.cols) : 12)))

const widgetSpan = (width) => Math.min(effectiveCols.value,
  Math.max(1, Number.isFinite(width) ? Math.floor(width) : 4))

const gridRef = ref(null)
let sortable = null

const setupSortable = () => {
  if (sortable) {
    sortable.destroy()
    sortable = null
  }
  if (!props.editMode || !gridRef.value) return
  sortable = Sortable.create(gridRef.value, {
    animation: 180,
    handle: '.drag-handle',
    draggable: '.widget-cell',
    ghostClass: 'widget-ghost',
    chosenClass: 'widget-chosen',
    onEnd: () => {
      const ids = Array.from(gridRef.value.querySelectorAll('.widget-cell')).map((el) => el.dataset.id)
      emit('reorder', ids)
    }
  })
}

watch(() => [props.editMode, props.items.length], () => {
  nextTick(setupSortable)
}, { immediate: true })

onBeforeUnmount(() => {
  if (sortable) sortable.destroy()
})
</script>
