<template>
  <div class="dashboard" :class="{ 'dashboard--mobile': isMobile }">
    <!-- 页面区块：顺序 / 显隐 / 每块配置都由偏好驱动 -->
    <div ref="sectionsRef" class="dashboard-sections">
      <!-- 首屏：欢迎区 → 商城核心指标 → 趋势 / 预警 / 排行 -->
      <div
        v-if="showTopPair"
        class="dashboard-top-pair"
      >
        <div
          v-for="section in topPairSections"
          :key="section.id"
          class="dashboard-section dashboard-section--pair"
          :data-id="section.id"
        >
          <div v-if="dashboard.editMode" class="section-controls">
            <el-tooltip :content="t('dashboard.editMode.dragSection')" placement="top">
              <el-button size="small" :icon="Rank" circle class="section-drag-handle" />
            </el-tooltip>
            <el-tooltip :content="t('dashboard.editMode.hideSection')" placement="top">
              <el-button size="small" :icon="Hide" circle @click="dashboard.toggleSectionVisible(section.id)" />
            </el-tooltip>
          </div>
          <component
            :is="section._meta.component"
            :ref="(el) => setSectionRef(section.id, el)"
            :config="section.config"
            v-bind="sectionProps(section.id)"
            @refresh="handleRefresh"
            @notifications="openNotificationDrawer"
            @settings="showSettings = true"
            @toggle-edit="toggleEdit"
            @navigate="goRoute"
            @changed="loadMall"
            @hide-item="hideItem"
            @reorder="onWidgetReorder"
            @update-config="(patch) => onSectionConfig(section.id, patch)"
          />
        </div>
      </div>

      <!-- 其余区块（含编辑模式下的全部区块、快捷操作、插件模块网格） -->
      <div
        v-for="section in middleSections"
        :key="section.id"
        class="dashboard-section"
        :data-id="section.id"
      >
        <div v-if="dashboard.editMode" class="section-controls">
          <el-tooltip :content="t('dashboard.editMode.dragSection')" placement="top">
            <el-button size="small" :icon="Rank" circle class="section-drag-handle" />
          </el-tooltip>
          <el-tooltip :content="t('dashboard.editMode.hideSection')" placement="top">
            <el-button size="small" :icon="Hide" circle @click="dashboard.toggleSectionVisible(section.id)" />
          </el-tooltip>
        </div>
        <component
          :is="section._meta.component"
          :ref="(el) => setSectionRef(section.id, el)"
          :config="section.config"
          v-bind="sectionProps(section.id)"
          @refresh="handleRefresh"
          @notifications="openNotificationDrawer"
          @settings="showSettings = true"
          @toggle-edit="toggleEdit"
          @navigate="goRoute"
          @changed="loadMall"
          @hide-item="hideItem"
          @reorder="onWidgetReorder"
          @update-config="(patch) => onSectionConfig(section.id, patch)"
        />
      </div>
      <el-empty
        v-if="renderedSections.length === 0"
        :description="t('dashboard.editMode.allSectionsHidden')"
      />
    </div>

    <!-- 设置抽屉 -->
    <el-drawer
      v-model="showSettings"
      :title="t('dashboard.toolbar.settings')"
      :size="isMobile ? '100%' : '380px'"
      direction="rtl"
      class="dashboard-drawer"
    >
      <el-form label-position="top">
        <el-form-item :label="t('dashboard.editMode.editModeSwitch')">
          <el-switch
            :model-value="dashboard.editMode"
            @change="setEditModeFromDrawer"
          />
        </el-form-item>
        <el-form-item :label="t('dashboard.editMode.cols')">
          <el-input-number v-model="colsLocal" :min="1" :max="24" @change="onColsChange" />
        </el-form-item>
        <el-form-item :label="t('dashboard.editMode.refresh')">
          <el-select v-model="refreshLocal" @change="onRefreshChange">
            <el-option :label="t('dashboard.editMode.refreshOff')" :value="0" />
            <el-option label="30s" :value="30" />
            <el-option label="1min" :value="60" />
            <el-option label="5min" :value="300" />
            <el-option label="10min" :value="600" />
          </el-select>
        </el-form-item>

        <el-divider>{{ t('dashboard.editMode.sections') }}</el-divider>
        <el-form-item>
          <div class="section-config-list">
            <div v-for="s in sectionList" :key="s.id" class="section-config-item">
              <div class="sci-head">
                <span class="sci-title">
                  <el-icon class="sci-icon"><component :is="sectionIconMap[s._meta.icon] || Grid" /></el-icon>
                  {{ t(s._meta.titleKey) }}
                </span>
                <span class="sci-ops">
                  <el-button
                    size="small"
                    :icon="Top"
                    :disabled="isFirstSection(s.id)"
                    @click="dashboard.moveSection(s.id, -1)"
                  />
                  <el-button
                    size="small"
                    :icon="Bottom"
                    :disabled="isLastSection(s.id)"
                    @click="dashboard.moveSection(s.id, 1)"
                  />
                  <el-switch
                    :model-value="s.visible === 1"
                    @change="dashboard.toggleSectionVisible(s.id)"
                  />
                </span>
              </div>
              <p class="sci-hint">{{ t(s._meta.hintKey) }}</p>

              <div v-if="s.visible === 1" class="sci-config">
                <template v-if="s.id === 'hero'">
                  <el-checkbox
                    :model-value="s.config.showGreeting"
                    @change="(v) => onSectionConfig(s.id, { showGreeting: v })"
                  >{{ t('dashboard.sections.hero.showGreeting') }}</el-checkbox>
                  <el-checkbox
                    :model-value="s.config.showDescription"
                    @change="(v) => onSectionConfig(s.id, { showDescription: v })"
                  >{{ t('dashboard.sections.hero.showDescription') }}</el-checkbox>
                  <el-checkbox
                    :model-value="s.config.showRefresh"
                    @change="(v) => onSectionConfig(s.id, { showRefresh: v })"
                  >{{ t('dashboard.sections.hero.showRefresh') }}</el-checkbox>
                  <el-checkbox
                    :model-value="s.config.showNotification"
                    @change="(v) => onSectionConfig(s.id, { showNotification: v })"
                  >{{ t('dashboard.sections.hero.showNotification') }}</el-checkbox>
                  <el-checkbox
                    :model-value="s.config.showSettings"
                    @change="(v) => onSectionConfig(s.id, { showSettings: v })"
                  >{{ t('dashboard.sections.hero.showSettings') }}</el-checkbox>
                  <el-checkbox
                    :model-value="s.config.showEdit"
                    @change="(v) => onSectionConfig(s.id, { showEdit: v })"
                  >{{ t('dashboard.sections.hero.showEdit') }}</el-checkbox>
                </template>

                <template v-else-if="s.id === 'stats'">
                  <p class="sci-sub">{{ t('dashboard.sections.stats.cards') }}</p>
                  <el-checkbox-group
                    :model-value="s.config.cards"
                    @change="(v) => onSectionConfig(s.id, { cards: v })"
                  >
                    <el-checkbox v-for="card in MALL_KPI_CARDS" :key="card.key" :label="card.key">
                      {{ t(card.title) }}
                    </el-checkbox>
                  </el-checkbox-group>
                </template>

                <template v-else-if="s.id === 'alerts'">
                  <p class="sci-sub">{{ t('dashboard.sections.alerts.items') }}</p>
                  <el-checkbox-group
                    :model-value="s.config.items"
                    @change="(v) => onSectionConfig(s.id, { items: v })"
                  >
                    <el-checkbox v-for="item in MALL_ALERT_ITEMS" :key="item.key" :label="item.key">
                      {{ t(item.title) }}
                    </el-checkbox>
                  </el-checkbox-group>
                </template>

                <template v-else-if="s.id === 'rank'">
                  <p class="sci-sub">{{ t('dashboard.sections.rank.modes') }}</p>
                  <el-checkbox-group
                    :model-value="s.config.modes"
                    @change="(v) => onSectionConfig(s.id, { modes: v })"
                  >
                    <el-checkbox v-for="m in RANK_MODES" :key="m" :label="m">
                      {{ t(`dashboard.rank.mode.${m}`) }}
                    </el-checkbox>
                  </el-checkbox-group>
                  <p class="sci-sub">{{ t('dashboard.sections.rank.limit') }}</p>
                  <el-radio-group
                    :model-value="s.config.limit"
                    size="small"
                    @change="(v) => onSectionConfig(s.id, { limit: v })"
                  >
                    <el-radio-button v-for="n in RANK_LIMITS" :key="n" :label="n">{{ n }}</el-radio-button>
                  </el-radio-group>
                </template>

                <template v-else-if="s.id === 'quick'">
                  <p class="sci-sub">{{ t('dashboard.sections.quick.actions') }}</p>
                  <el-checkbox-group
                    :model-value="s.config.actions"
                    @change="(v) => onSectionConfig(s.id, { actions: v })"
                  >
                    <el-checkbox v-for="act in QUICK_ACTIONS" :key="act.key" :label="act.key">
                      {{ t(act.title) }}
                    </el-checkbox>
                  </el-checkbox-group>
                </template>

                <template v-else-if="s.id === 'trend'">
                  <p class="sci-sub">{{ t('dashboard.sections.trend.days') }}</p>
                  <el-radio-group
                    :model-value="s.config.days"
                    size="small"
                    @change="(v) => onSectionConfig(s.id, { days: v })"
                  >
                    <el-radio-button v-for="d in TREND_DAY_OPTIONS" :key="d" :label="d">
                      {{ d === 7 ? t('dashboard.charts.last7') : t('dashboard.charts.last30') }}
                    </el-radio-button>
                  </el-radio-group>
                  <p class="sci-sub">{{ t('dashboard.sections.trend.series') }}</p>
                  <el-checkbox-group
                    :model-value="s.config.series"
                    @change="(v) => onSectionConfig(s.id, { series: v })"
                  >
                    <el-checkbox v-for="se in TREND_SERIES" :key="se.key" :label="se.key">
                      {{ t(se.title) }}
                    </el-checkbox>
                  </el-checkbox-group>
                </template>

                <template v-else-if="s.id === 'widgets'">
                  <el-checkbox
                    :model-value="s.config.showTitle"
                    @change="(v) => onSectionConfig(s.id, { showTitle: v })"
                  >{{ t('dashboard.sections.widgets.showTitle') }}</el-checkbox>
                </template>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-divider>{{ t('dashboard.editMode.modules') }}</el-divider>
        <el-form-item>
          <div class="module-toggle-list">
            <div v-for="m in allModulesForToggle" :key="m.moduleId" class="module-toggle-item">
              <span>
                <el-icon class="toggle-icon" :style="{ color: m._meta.color }">
                  <component :is="m._meta.icon" />
                </el-icon>
                {{ t(m._meta.title) }}
              </span>
              <el-switch
                :model-value="m.visible === 1"
                @change="dashboard.toggleVisible(m.moduleId)"
              />
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="danger" plain @click="onReset">
            {{ t('dashboard.editMode.reset') }}
          </el-button>
          <el-button type="primary" @click="onSave">
            {{ t('dashboard.editMode.save') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-drawer>

    <!-- 通知抽屉 -->
    <el-drawer
      v-model="showNotifications"
      :title="t('dashboard.notification.title')"
      :size="isMobile ? '100%' : '400px'"
      direction="rtl"
      class="dashboard-drawer"
    >
      <div class="notification-toolbar">
        <span class="notification-count">{{ unreadCount }} 条未读</span>
        <el-button
          v-if="unreadCount > 0"
          :icon="CircleCheck"
          text
          data-testid="notification-read-all"
          :loading="notificationActionLoading"
          @click="markAllNotificationsRead"
        >
          全部已读
        </el-button>
      </div>

      <div v-if="notificationLoading" class="notification-loading" aria-live="polite">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中</span>
      </div>
      <div v-else-if="notificationError" class="notification-error" role="alert">
        <span>{{ notificationError }}</span>
        <el-button :icon="RefreshRight" text data-testid="notification-retry" @click="loadNotifications(notificationPage)">
          重试
        </el-button>
      </div>
      <el-empty v-else-if="notifications.length === 0" :description="t('dashboard.notification.empty')" />
      <ul v-else class="notification-list">
        <li v-for="n in notifications" :key="n.id" class="notification-item" :class="{ 'is-unread': !n.read }">
          <button
            type="button"
            class="notification-entry"
            :data-testid="`notification-${n.id}`"
            @click="openNotification(n)"
          >
            <span class="notification-state" aria-hidden="true" />
            <span class="n-body">
              <span class="n-title">{{ n.title }}</span>
              <span v-if="n.content" class="n-desc">{{ n.content }}</span>
            </span>
            <time class="n-time">{{ formatNotificationTime(n.createTime) }}</time>
          </button>
        </li>
      </ul>

      <div v-if="notificationTotal > notificationSize" class="notification-pagination">
        <el-button text :disabled="notificationPage <= 1" @click="loadNotifications(notificationPage - 1)">
          上一页
        </el-button>
        <span>第 {{ notificationPage }} 页</span>
        <el-button
          text
          :disabled="notificationPage * notificationSize >= notificationTotal"
          data-testid="notification-next-page"
          @click="loadNotifications(notificationPage + 1)"
        >
          下一页
        </el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Rank, Hide, Loading, Top, Bottom, Grid, Sunny, TrendCharts,
  Operation, DataLine, Tickets, Document, Wallet, List, User, Goods, Box,
  Calendar, ChatDotRound, Cpu, KnifeFork, Van, Present, Lock, OfficeBuilding,
  SetUp, MagicStick, Setting, Bell, CircleCheck, RefreshRight, WarningFilled, Trophy
} from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, GridComponent, LegendComponent
} from 'echarts/components'

import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { useDashboardStore } from '@/stores/dashboard'
import { usePluginStore } from '@/stores/plugin'
import { getModuleById } from '@/config/dashboard/modules'
import {
  MALL_KPI_CARDS,
  MALL_ALERT_ITEMS,
  QUICK_ACTIONS,
  TREND_SERIES,
  TREND_DAY_OPTIONS,
  RANK_MODES,
  RANK_LIMITS
} from '@/config/dashboard/sections'
import { hasPermission } from '@/utils/permission'
import { useMallOverview } from '@/composables/useMallOverview'
import { inboxApi } from '@/api/setting/message'
import {
  formatNotificationTime,
  normalizeNotificationPage,
  notificationApiId,
  resolveNotificationRoute
} from './dashboardNotificationModel'

use([CanvasRenderer, LineChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent])

const router = useRouter()
const dashboard = useDashboardStore()
const pluginStore = usePluginStore()

const MOBILE_BP = 768
/**
 * 首帧即按真实视口判定，不要写死 false。
 * 写死 false 时，≤768px 的用户会先按桌面布局（首屏三栏 + 4 列指标）渲染一帧，
 * 挂载后才切到单列 mobile 布局，表现为「页面先撑开再塌缩」的跳变。
 */
const isMobile = ref(typeof window !== 'undefined' && window.innerWidth < MOBILE_BP)
const greetingText = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const updateDevice = () => {
  isMobile.value = window.innerWidth < MOBILE_BP
}

// 图标映射（Widget 元数据里图标是字符串名，在渲染时按名取组件）
const iconMap = {
  Tickets, Document, Wallet, List, User, Goods, Box, Calendar, DataLine,
  ChatDotRound, Cpu, Operation, KnifeFork, Van, Present, Lock, OfficeBuilding,
  SetUp, MagicStick, Setting, Bell
}

/** 设置面板里区块图标 */
const sectionIconMap = {
  Sunny, TrendCharts, Operation, DataLine, Grid, WarningFilled, Trophy
}

/**
 * 合并 store.items 与模块元数据
 * 过滤条件：visible=1 且当前用户拥有 perm 权限
 */
const renderedItems = computed(() =>
  dashboard.visibleItems
    .map((it) => {
      const meta = getModuleById(it.moduleId)
      if (!meta) return null
      if (meta.perm && !hasPermission(meta.perm)) return null
      return {
        ...it,
        _meta: {
          ...meta,
          icon: iconMap[meta.icon] || Tickets
        }
      }
    })
    .filter(Boolean)
)

/**
 * 设置面板全部模块（含隐藏）
 */
const allModulesForToggle = computed(() =>
  dashboard.items
    .map((it) => {
      const meta = getModuleById(it.moduleId)
      if (!meta) return null
      if (meta.perm && !hasPermission(meta.perm)) return null
      return { ...it, _meta: { ...meta, icon: iconMap[meta.icon] || Tickets } }
    })
    .filter(Boolean)
)

/** 页面区块：按偏好顺序渲染，带注册表元数据（老偏好 / 异常数据兜底为空数组） */
const renderedSections = computed(() => dashboard.orderedSections || [])
/** 设置面板用的全部区块（含隐藏） */
const sectionList = computed(() => dashboard.allSections || [])

/**
 * 首屏三栏（2026-10-28 密度优化版）：
 *   第 1 行 欢迎区（窄列）｜ 商城核心指标 4 列网格（占其余两列宽）
 *   第 2 行 交易趋势 / 运营待办与预警 / 热销排行
 * 快捷操作、插件模块网格等非商城首屏内容排在下方。
 * 编辑模式恢复单列，方便拖拽排序。
 */
const FIRST_SCREEN_ORDER = ['hero', 'stats', 'trend', 'alerts', 'rank']
const topPairSections = computed(() => {
  const sections = renderedSections.value
  return FIRST_SCREEN_ORDER
    .map((id) => sections.find((s) => s.id === id))
    .filter(Boolean)
})
const showTopPair = computed(() =>
  !isMobile.value &&
  !dashboard.editMode &&
  topPairSections.value.length >= 2
)
const middleSections = computed(() => {
  if (!showTopPair.value) return renderedSections.value
  const pairIds = new Set(topPairSections.value.map((s) => s.id))
  return renderedSections.value.filter((s) => !pairIds.has(s.id))
})

/** 各区块需要的 props 不同，按需下发，避免多余属性挂到 DOM 上 */
const sectionProps = (id) => {
  switch (id) {
    case 'hero':
      return {
        greetingText: greetingText.value,
        refreshing: refreshing.value,
        unreadCount: unreadCount.value,
        editMode: dashboard.editMode,
        isMobile: isMobile.value
      }
    case 'stats':
      return { kpi: mall.value.kpi, loading: mallLoading.value }
    case 'alerts':
      return { alerts: mall.value.alerts }
    case 'trend':
      return { trend: mall.value.trend, isMobile: isMobile.value }
    case 'rank':
      return {
        rank: {
          goodsTop: mall.value.goodsTop,
          categoryTop: mall.value.categoryTop
        }
      }
    case 'widgets':
      return {
        items: renderedItems.value,
        cols: dashboard.cols,
        editMode: dashboard.editMode,
        isMobile: isMobile.value
      }
    default:
      return {}
  }
}

/** 异步区块组件实例缓存，用于刷新时调用子组件 expose 的 refresh() */
const sectionRefs = {}
const setSectionRef = (id, el) => {
  if (el) sectionRefs[id] = el
  else delete sectionRefs[id]
}

const isFirstSection = (id) => sectionList.value.findIndex((s) => s.id === id) === 0
const isLastSection = (id) => sectionList.value.findIndex((s) => s.id === id) === sectionList.value.length - 1

// ---------------- 商城经营数据 ----------------
const { overview: mall, loading: mallLoading, load: loadMall } = useMallOverview()
const refreshing = ref(false)

// ---------------- 消息中心 ----------------
const showNotifications = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const notificationPage = ref(1)
const notificationSize = ref(20)
const notificationTotal = ref(0)
const notificationLoading = ref(false)
const notificationActionLoading = ref(false)
const notificationError = ref('')

const loadUnreadCount = async () => {
  const response = await inboxApi.getUnreadCount()
  unreadCount.value = Math.max(0, Number(response?.data || 0))
}

const loadNotifications = async (page = 1) => {
  if (page < 1) return
  notificationLoading.value = true
  notificationError.value = ''
  try {
    const response = await inboxApi.getList({ page, size: notificationSize.value })
    const result = normalizeNotificationPage(response)
    notifications.value = result.list
    notificationPage.value = result.page
    notificationSize.value = result.size
    notificationTotal.value = result.total
  } catch (error) {
    notificationError.value = error?.message || '消息加载失败'
  } finally {
    notificationLoading.value = false
  }
}

const refreshNotifications = async () => {
  await Promise.allSettled([loadUnreadCount(), loadNotifications(1)])
}

const openNotificationDrawer = async () => {
  showNotifications.value = true
  await refreshNotifications()
}

const openNotification = async (notification) => {
  if (!notification.read) {
    try {
      await inboxApi.markAsRead(notificationApiId(notification.id))
      notification.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      notificationError.value = error?.message || '标记已读失败'
      return
    }
  }
  const route = resolveNotificationRoute(notification)
  if (!route) return
  showNotifications.value = false
  await Promise.resolve(router.push(route)).catch(() => {})
}

const markAllNotificationsRead = async () => {
  notificationActionLoading.value = true
  try {
    await inboxApi.markAllAsRead()
    notifications.value.forEach((notification) => { notification.read = true })
    unreadCount.value = 0
  } catch (error) {
    notificationError.value = error?.message || '全部已读操作失败'
  } finally {
    notificationActionLoading.value = false
  }
}

// ---------------- 设置 ----------------
const showSettings = ref(false)
const colsLocal = ref(12)
const refreshLocal = ref(0)

const onColsChange = (v) => dashboard.setCols(v)
const onRefreshChange = (v) => {
  dashboard.setRefreshInterval(v)
  setupTimer()
}
const onSave = async () => {
  const ok = await dashboard.savePref()
  ElMessage[ok ? 'success' : 'warning'](
    ok ? t('dashboard.editMode.saveOk') : t('dashboard.editMode.saveLocalOnly')
  )
  showSettings.value = false
}
const onReset = async () => {
  await dashboard.resetPref()
  colsLocal.value = dashboard.cols
  refreshLocal.value = dashboard.refreshInterval
  ElMessage.success(t('dashboard.editMode.resetOk'))
}

/**
 * 区块配置变更：立即落到 store 并静默持久化，
 * 这样切换趋势天数、勾选统计卡之后刷新页面仍然是用户的选择
 */
const onSectionConfig = async (id, patch) => {
  dashboard.updateSectionConfig(id, patch)
  await dashboard.savePref()
}

// ---------------- 编辑模式 & 拖拽 ----------------
const sectionsRef = ref(null)
let sectionSortable = null

/** 区块级拖拽排序（编辑模式下生效，手柄 .section-drag-handle） */
const setupSectionSortable = () => {
  if (sectionSortable) {
    sectionSortable.destroy()
    sectionSortable = null
  }
  if (!dashboard.editMode || !sectionsRef.value) return
  sectionSortable = Sortable.create(sectionsRef.value, {
    animation: 180,
    handle: '.section-drag-handle',
    draggable: '.dashboard-section',
    ghostClass: 'section-ghost',
    chosenClass: 'section-chosen',
    onEnd: () => {
      const ids = Array.from(sectionsRef.value.querySelectorAll('.dashboard-section'))
        .map((el) => el.dataset.id)
      dashboard.reorderSections(ids)
    }
  })
}

const toggleEdit = async () => {
  dashboard.setEditMode(!dashboard.editMode)
  await nextTick()
  setupSectionSortable()
  if (!dashboard.editMode) await persistEdit()
}

/** 设置抽屉里的编辑模式开关：hero 上把编辑按钮关掉时，这里仍要能退出编辑 */
const setEditModeFromDrawer = async (v) => {
  dashboard.setEditMode(v)
  await nextTick()
  setupSectionSortable()
  if (!v) await persistEdit()
}

const persistEdit = async () => {
  const ok = await dashboard.savePref()
  ElMessage[ok ? 'success' : 'warning'](
    ok ? t('dashboard.editMode.saveOk') : t('dashboard.editMode.saveLocalOnly')
  )
}

const hideItem = (id) => dashboard.toggleVisible(id)
const onWidgetReorder = (ids) => dashboard.reorder(ids)
/**
 * 打开区块/卡片指向的页面。
 *
 * 2026-09-21 修复「工作台模块卡片点击更多不能跳转」：
 * 原来是裸 `router.push(path).catch(() => {})`，目标不可达时（插件未安装、页面已下线、
 * 无对应菜单权限）守卫会把导航重定向或中止，异常又被 catch 吞掉，用户看到的是
 * **点了完全没反应**。现在分三步：
 * 1. 先判断目标路由是否真的注册过 —— 落到兜底 404 说明页面不存在，等一次插件后台注册
 *    再判（插件路由是异步 import 的，首屏未必就绪）；
 * 2. 仍不可达且目标属于插件命名空间（/plugin/{code}/**）→ 说明该插件尚未安装/未部署，
 *    送到插件中心（可安装、启用、看状态），比原地不动有用；
 * 3. 其余不可达情况给明确提示，不再静默。
 */
const PLUGIN_CENTER_PATH = '/platform/plugins'
const isRouteAvailable = (path) => {
  const resolved = router.resolve(path)
  return !!resolved && resolved.name !== 'NotFound'
}
const pluginCodeOfPath = (path) => {
  const matched = /^\/plugin\/([^/]+)/.exec(String(path || ''))
  return matched ? matched[1] : ''
}

const goRoute = async (path) => {
  if (!path) return
  if (dashboard.editMode) return
  try {
    if (!isRouteAvailable(path)) {
      await pluginStore.waitForBackgroundPlugins()
    }
    if (isRouteAvailable(path)) {
      await router.push(path)
      return
    }
    const pluginCode = pluginCodeOfPath(path)
    if (pluginCode && isRouteAvailable(PLUGIN_CENTER_PATH)) {
      console.warn('[Dashboard] 插件页面未注册，转插件中心:', pluginCode)
      ElMessage.info(t('dashboard.toolbar.moduleNotInstalled'))
      await router.push(PLUGIN_CENTER_PATH)
      return
    }
    console.warn('[Dashboard] 模块页面不可达:', path)
    ElMessage.warning(t('dashboard.toolbar.moduleUnavailable'))
  } catch (err) {
    console.warn('[Dashboard] 模块跳转失败:', path, err?.message || err)
    ElMessage.warning(t('dashboard.toolbar.moduleUnavailable'))
  }
}

// ---------------- 自动刷新 ----------------
let timer = null
const setupTimer = () => {
  if (timer) { clearInterval(timer); timer = null }
  if (dashboard.refreshInterval > 0) {
    timer = setInterval(() => handleRefresh(true), dashboard.refreshInterval * 1000)
  }
}

/** 刷新各区块：趋势图走子组件 expose 的 refresh（数据由父级重新拉取） */
const refreshSections = () => {
  Object.values(sectionRefs).forEach((inst) => {
    if (inst && typeof inst.refresh === 'function') inst.refresh()
  })
}

const handleRefresh = async (silent = false) => {
  refreshing.value = true
  try {
    await Promise.allSettled([
      loadMall(),
      refreshNotifications()
    ])
    refreshSections()
    if (!silent) ElMessage.success(t('dashboard.toolbar.refreshOk'))
  } finally {
    refreshing.value = false
  }
}

watch(() => dashboard.cols, (v) => { colsLocal.value = v })
watch(() => dashboard.refreshInterval, (v) => { refreshLocal.value = v })
watch(() => dashboard.editMode, () => { nextTick(setupSectionSortable) })
watch(isMobile, () => { refreshSections() })

onMounted(async () => {
  updateDevice()
  window.addEventListener('resize', updateDevice, { passive: true })
  await dashboard.loadPref()
  colsLocal.value = dashboard.cols
  refreshLocal.value = dashboard.refreshInterval
  await Promise.all([loadMall(), refreshNotifications()])
  setupTimer()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateDevice)
  if (sectionSortable) sectionSortable.destroy()
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" src="./dashboard.page.scss"></style>
