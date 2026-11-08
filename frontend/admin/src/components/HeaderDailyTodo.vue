<template>
  <div v-if="todoPanel" class="header-daily-todo" :class="{ 'header-daily-todo--floating': floating }">
    <el-tooltip content="我的每日待办" placement="bottom">
      <button
        type="button"
        class="hc-header-chip header-daily-todo__trigger"
        aria-label="我的每日待办"
        aria-haspopup="dialog"
        :aria-expanded="visible"
        @click="visible = true"
      >
        <el-icon><Finished /></el-icon>
        <span class="header-daily-todo__label">待办</span>
      </button>
    </el-tooltip>
    <el-dialog
      v-model="visible"
      title="我的每日待办"
      class="header-daily-todo-dialog"
      width="min(1100px, calc(100vw - 32px))"
      top="5vh"
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @open="handleOpen"
    >
      <p class="header-daily-todo__hint">仅自己可见，记录重要事项；关闭窗口后，未提交的内容会暂时保留。</p>
      <div v-if="loadError" class="header-daily-todo__error" role="alert">
        <p>待办面板加载失败，请重试。</p>
        <el-button @click="retry">重新加载</el-button>
      </div>
      <div v-else class="header-daily-todo__body">
        <component v-if="activated" :is="todoPanel" :key="panelKey" ref="panelRef" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onErrorCaptured, ref, watch } from 'vue'
import { Finished } from '@element-plus/icons-vue'
import { useDashboardStore } from '@/stores/dashboard'

defineProps({ floating: { type: Boolean, default: false } })
const dashboard = useDashboardStore()
const todoPanel = computed(() => dashboard.todoPanel)
const visible = ref(false)
const activated = ref(false)
const loadError = ref(false)
const panelKey = ref(0)
const panelRef = ref(null)

const handleOpen = async () => {
  const wasActivated = activated.value
  activated.value = true
  await nextTick()
  // 首次挂载由插件自行加载；再次打开刷新列表，保留当前未提交的草稿。
  if (wasActivated) await panelRef.value?.refresh?.()
}
const retry = () => {
  loadError.value = false
  panelKey.value += 1
}
onErrorCaptured(() => {
  loadError.value = true
  return false
})
watch(todoPanel, (next, previous) => {
  if (next === previous) return
  visible.value = false
  activated.value = false
  loadError.value = false
  panelKey.value += 1
})
</script>

<style scoped lang="scss">
.header-daily-todo__trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 44px;
  min-height: 44px;
  cursor: pointer;
  &:focus-visible { outline: 2px solid var(--el-color-primary); outline-offset: 2px; }
}
.header-daily-todo--floating {
  position: fixed;
  top: 12px;
  right: 20px;
  z-index: 1900;
  border-radius: 10px;
  background: var(--el-bg-color);
  box-shadow: var(--el-box-shadow-light);
}
.header-daily-todo__hint { margin: 0 0 16px; color: var(--el-text-color-secondary); font-size: 13px; }
.header-daily-todo__body {
  height: min(600px, calc(90dvh - 148px));
  min-height: 0;
  overflow: hidden;

  // 只调整顶栏弹窗中的布局，不影响 OA 页面本身。
  :deep(.daily-todo-panel) { height: 100%; min-height: 0; }
  :deep(.daily-todo-panel .sync-toolbar) { flex-shrink: 0; margin-bottom: 14px; }
  :deep(.daily-todo-panel .panel-layout) {
    grid-template-columns: minmax(340px, 2fr) minmax(0, 3fr);
    grid-template-rows: minmax(0, 1fr);
    gap: 20px;
    flex: 1;
    min-height: 0;
  }
  :deep(.daily-todo-panel .panel-compose),
  :deep(.daily-todo-panel .panel-list) { min-height: 0; }
  :deep(.daily-todo-panel .panel-compose) { overflow-y: auto; }
  :deep(.daily-todo-panel .composer-body) { padding: 20px; }
  :deep(.daily-todo-panel .composer-date.el-date-editor) { width: 100% !important; }
  :deep(.daily-todo-panel .list-scroll) {
    max-height: none;
    overflow-y: auto;
    gap: 12px;
  }
  :deep(.daily-todo-panel .list-toolbar) {
    position: sticky;
    top: 0;
    z-index: 1;
    background: var(--el-bg-color);
    padding: 4px 0 10px;
  }
  :deep(.daily-todo-panel .todo-item) { padding: 12px; }
  :deep(.daily-todo-panel .todo-actions) { gap: 2px; }
  :deep(.daily-todo-panel .todo-title) { overflow-wrap: anywhere; }
}
.header-daily-todo__error { padding: 40px 16px; text-align: center; }
@media (max-width: 768px) {
  .header-daily-todo__label { display: none; }
  .header-daily-todo__body {
    height: auto;
    max-height: calc(90dvh - 148px);
    overflow-y: auto;
    :deep(.daily-todo-panel .panel-layout) {
      grid-template-columns: minmax(0, 1fr);
      grid-template-rows: auto auto;
      gap: 14px;
    }
    :deep(.daily-todo-panel .panel-compose) { overflow: visible; }
    :deep(.daily-todo-panel .composer-body) { padding: 14px; }
    :deep(.daily-todo-panel .list-scroll) { max-height: 420px; }
  }
}

// 中等宽度的对话框也保持两栏，避免插件按视口过早切为单列。
@media (min-width: 769px) and (max-width: 1024px) {
  .header-daily-todo__body :deep(.daily-todo-panel .panel-layout) {
    grid-template-columns: minmax(300px, 2fr) minmax(0, 3fr);
  }
}
</style>
