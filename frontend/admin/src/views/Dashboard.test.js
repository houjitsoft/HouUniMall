// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  inboxApi: {
    getList: vi.fn(),
    getUnreadCount: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn()
  },
  push: vi.fn(),
  waitForBackgroundPlugins: vi.fn().mockResolvedValue(),
  dashboard: {
    visibleItems: [],
    items: [],
    sections: [],
    orderedSections: [],
    allSections: [],
    todoPanel: null,
    cols: 12,
    editMode: false,
    refreshInterval: 0,
    loadPref: vi.fn(),
    savePref: vi.fn(),
    resetPref: vi.fn(),
    setCols: vi.fn(),
    setRefreshInterval: vi.fn(),
    setEditMode: vi.fn(),
    toggleVisible: vi.fn(),
    reorder: vi.fn(),
    toggleSectionVisible: vi.fn(),
    moveSection: vi.fn(),
    reorderSections: vi.fn(),
    updateSectionConfig: vi.fn(),
    resetSectionConfig: vi.fn()
  }
}))

vi.mock('@/api/setting/message', () => ({ inboxApi: mocks.inboxApi }))
vi.mock('vue-router', () => ({ useRouter: () => ({ push: mocks.push }) }))
vi.mock('@/stores/dashboard', () => ({ useDashboardStore: () => mocks.dashboard }))
vi.mock('@/stores/plugin', () => ({
  usePluginStore: () => ({ waitForBackgroundPlugins: mocks.waitForBackgroundPlugins })
}))
vi.mock('@/i18n-element', () => ({ useI18n: () => ({ t: (value) => value }) }))
vi.mock('@/config/dashboard/modules', () => ({ DASHBOARD_MODULES: [], getModuleById: () => null }))
vi.mock('@/utils/permission', () => ({ hasPermission: () => true }))
vi.mock('@/api/order/order', () => ({ orderService: { getOrderStatistics: vi.fn().mockResolvedValue({ data: {} }) } }))
vi.mock('@/api/payment/dashboard', () => ({ getPaymentOverview: vi.fn().mockResolvedValue({ data: {} }) }))
// 首屏商城数据由 composable 聚合，单测里给一份空数据即可（不触发真实请求）
vi.mock('@/composables/useMallOverview', () => ({
  useMallOverview: () => ({
    overview: { value: { kpi: { trends: {} }, alerts: {}, trend: [], goodsTop: [], categoryTop: [] } },
    loading: { value: false },
    load: vi.fn().mockResolvedValue()
  })
}))
vi.mock('sortablejs', () => ({ default: { create: vi.fn() } }))
vi.mock('vue-echarts', () => ({ default: { template: '<div />' } }))
vi.mock('echarts/core', () => ({ use: vi.fn() }))
vi.mock('echarts/renderers', () => ({ CanvasRenderer: {} }))
vi.mock('echarts/charts', () => ({ LineChart: {} }))
vi.mock('echarts/components', () => ({
  TitleComponent: {}, TooltipComponent: {}, GridComponent: {}, LegendComponent: {}
}))
vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() }
}))
vi.mock('@element-plus/icons-vue', () => {
  const icon = { template: '<span />' }
  return {
    Bell: icon, Setting: icon, Refresh: icon, EditPen: icon, Check: icon,
    Rank: icon, Hide: icon, ArrowRight: icon, Loading: icon, Tickets: icon,
    Top: icon, Bottom: icon, Grid: icon, Sunny: icon, TrendCharts: icon,
    Document: icon, DataBoard: icon, Wallet: icon, List: icon, User: icon, Goods: icon, Box: icon,
    Calendar: icon, DataLine: icon, ChatDotRound: icon, Cpu: icon, Operation: icon,
    KnifeFork: icon, Van: icon, Present: icon, Lock: icon, OfficeBuilding: icon,
    SetUp: icon, MagicStick: icon, Plus: icon, CirclePlus: icon, Finished: icon,
    CircleCheck: icon, RefreshRight: icon,
    // 商城化首屏新增图标
    WarningFilled: icon, Trophy: icon, Money: icon, CreditCard: icon, RefreshLeft: icon,
    CaretTop: icon, CaretBottom: icon, Clock: icon, SoldOut: icon, Menu: icon, Shop: icon
  }
})

import Dashboard from './Dashboard.vue'
import { DASHBOARD_SECTIONS, buildDefaultSections } from '@/config/dashboard/sections'

/** 首页区块现在由偏好驱动，测试里给出默认六块，否则连通知按钮所在的欢迎区都不会渲染 */
const buildSectionsWithMeta = () => buildDefaultSections().map((it) => ({
  ...it,
  _meta: DASHBOARD_SECTIONS.find((s) => s.id === it.id)
}))

const passthrough = { template: '<div v-bind="$attrs"><slot /></div>' }
const button = {
  emits: ['click'],
  template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'
}

const mountDashboard = () => mount(Dashboard, {
  global: {
    stubs: {
      'el-button': button,
      'el-drawer': passthrough,
      'el-tooltip': passthrough,
      'el-badge': passthrough,
      'el-icon': passthrough,
      'el-empty': passthrough,
      'el-form': true,
      'el-form-item': true,
      'el-input-number': true,
      'el-select': true,
      'el-option': true,
      'el-divider': true,
      'el-switch': true,
      'el-checkbox': true,
      'el-checkbox-group': true,
      'el-radio-group': true,
      'el-radio-button': true,
      'el-card': passthrough,
      'el-pagination': passthrough,
      'v-chart': true
    }
  }
})

describe('Dashboard notifications', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    const withMeta = buildSectionsWithMeta()
    mocks.dashboard.sections = withMeta
    mocks.dashboard.allSections = withMeta
    mocks.dashboard.orderedSections = withMeta.filter((s) => s.visible === 1)
    mocks.dashboard.visibleItems = []
    mocks.dashboard.items = []
    mocks.dashboard.cols = 12
    mocks.dashboard.editMode = false
    mocks.dashboard.refreshInterval = 0
    mocks.dashboard.loadPref.mockResolvedValue()
    mocks.inboxApi.getUnreadCount.mockResolvedValue({ code: 1, data: 2 })
    mocks.inboxApi.getList.mockImplementation(async ({ page }) => ({
      code: 1,
      data: {
        list: page === 1 ? [
          {
            id: 91,
            title: '任务分配',
            content: '请处理任务',
            isRead: 0,
            bizType: 'PM_TASK',
            bizId: '42',
            createTime: 1784890000
          },
          {
            id: 92,
            title: '项目更新',
            content: '项目计划已调整',
            isRead: 0,
            bizType: 'PM_PROJECT',
            bizId: '9',
            createTime: 1784880000
          }
        ] : [{ id: 93, title: '下一页', isRead: 1 }],
        total: 21,
        page,
        size: 20
      }
    }))
    mocks.inboxApi.markAsRead.mockResolvedValue({ code: 1 })
    mocks.inboxApi.markAllAsRead.mockResolvedValue({ code: 1 })
    mocks.push.mockResolvedValue()
  })

  it('loads, refreshes, reads one, reads all and paginates', async () => {
    const wrapper = mountDashboard()
    await flushPromises()

    expect(mocks.inboxApi.getUnreadCount).toHaveBeenCalledTimes(1)
    expect(mocks.inboxApi.getList).toHaveBeenCalledWith({ page: 1, size: 20 })

    await wrapper.get('[data-testid="notification-trigger"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="notification-91"]').trigger('click')
    await flushPromises()
    expect(mocks.inboxApi.markAsRead).toHaveBeenCalledWith(91)
    expect(mocks.push).toHaveBeenCalledWith('/project/task/detail/42')

    await wrapper.get('[data-testid="notification-read-all"]').trigger('click')
    await flushPromises()
    expect(mocks.inboxApi.markAllAsRead).toHaveBeenCalledTimes(1)

    await wrapper.get('[data-testid="notification-next-page"]').trigger('click')
    await flushPromises()
    expect(mocks.inboxApi.getList).toHaveBeenCalledWith({ page: 2, size: 20 })
  })
})
