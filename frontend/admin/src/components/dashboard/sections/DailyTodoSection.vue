<template>
  <section class="panel daily-todo-card">
    <div class="panel-header daily-todo-header">
      <div class="daily-todo-title-block">
        <span class="section-icon section-icon--todo" aria-hidden="true">
          <el-icon><Finished /></el-icon>
        </span>
        <div>
          <h3 class="section-title">{{ t('dashboard.dailyTodo.title') }}</h3>
          <p class="daily-todo-sub">{{ t('dashboard.dailyTodo.subtitle') }}</p>
        </div>
      </div>
      <!--
        2026-09-21：原先指向 /plugin/oa/todo，该路由从未注册（点进去必然 404）；
        临时记事本并入每日待办后，整块功能落在 OA 工作台，故改指真实路由。
      -->
      <el-button v-if="config.showMoreLink" class="more-btn" round @click="emit('navigate', '/plugin/oa/apps')">
        {{ t('dashboard.more') }}
        <el-icon class="more-btn-icon"><ArrowRight /></el-icon>
      </el-button>
    </div>
    <!--
      OA 插件注册了 dailyTodo 面板时渲染插件面板
      （单一待办登记区 + 待办列表；「记速记」已于 2026-10-19 下线），
      未安装插件时才回落到提示文案。
      compact 由首页根据是否和 hero 并排自动传入。
    -->
    <component
      v-if="todoPanel"
      :is="todoPanel"
      :compact="compact"
      @changed="emit('changed')"
    />
    <div v-else class="core-dashboard-note">OA 待办、合同和财务功能安装对应插件后可用。</div>
  </section>
</template>

<script setup>
/**
 * 我的每日待办（每人独立登记）—— 首页唯一入口，待办与随手记同处一块面板。
 * 面板本体由 OA 插件通过 dashboardPanels.dailyTodo 注册进来，宿主只提供挂载位；
 * 2026-09-21 临时记事本并入本区块（不再有第二个入口），「显示更多」指向 OA 工作台。
 */
import { Finished, ArrowRight } from '@element-plus/icons-vue'
import { useI18n } from '@/i18n-element'

const { t } = useI18n()

defineProps({
  config: { type: Object, required: true },
  todoPanel: { type: [Object, Function], default: null },
  compact: { type: Boolean, default: false }
})

const emit = defineEmits(['navigate', 'changed'])
</script>
