/**
 * Core Dashboard 页面区块注册表（2026-09-21）。
 *
 * 2026-09-21 商城化重构：
 * 首页定位已经是「商城经营」，但首屏此前摆的是 OA / 支付口径的卡片（待办、合同、
 * 今日营收、订单）和一堆非商城插件卡片。这里把区块重新组织成商城视角：
 *
 *   首屏：hero（欢迎） → stats（商城核心指标） → alerts（运营待办与预警）
 *         → trend（交易趋势） → rank（热销商品 / 品类排行）
 *   第二屏及以后：quick（快捷操作） → widgets（插件模块网格）
 *
 * 每日待办已迁至全局顶栏，不再注册为首页区块；旧偏好由 normalizeSections 过滤。
 * 区块 id 沿用旧的 hero/stats/quick/trend/widgets，新增 alerts 与 rank。
 * 老用户偏好里没有 alerts / rank，stores/dashboard.js 的 normalizeSections 会按
 * 默认顺序整表迁移（保留用户原有的显隐与区块配置），不会出现「新区块掉到最底下」。
 *
 * 每个区块的数据结构与 widget 明细一致：{ id, visible, sort, config }
 * config 由各区块自己定义，读写都经过 sanitizeSectionConfig 兜底，
 * 保证后端返回脏数据 / 老版本无该字段时页面不会崩。
 */
import { markRaw } from 'vue'
import HeroSection from '@/components/dashboard/sections/HeroSection.vue'
import MallKpiSection from '@/components/dashboard/sections/MallKpiSection.vue'
import MallAlertsSection from '@/components/dashboard/sections/MallAlertsSection.vue'
import MallRankSection from '@/components/dashboard/sections/MallRankSection.vue'
import QuickActionsSection from '@/components/dashboard/sections/QuickActionsSection.vue'
import TrendSection from '@/components/dashboard/sections/TrendSection.vue'
import WidgetsSection from '@/components/dashboard/sections/WidgetsSection.vue'

/**
 * 商城核心经营指标卡片：key 与 kpi 对象字段名一致。
 * trendKey 指向 kpi.trends 里的环比字段，留空表示不做环比。
 */
export const MALL_KPI_CARDS = [
  { key: 'gmvToday', title: 'dashboard.kpi.gmvToday', icon: 'Wallet', theme: 'primary', route: '/mall/statistics/sales', field: 'gmvToday', valueType: 'money', trendKey: 'gmvToday', compareKey: 'dashboard.kpi.compareYesterday' },
  { key: 'orderToday', title: 'dashboard.kpi.orderToday', icon: 'List', theme: 'success', route: '/mall/order/list', field: 'orderToday', valueType: 'num', trendKey: 'orderToday', compareKey: 'dashboard.kpi.compareYesterday' },
  { key: 'gmv7d', title: 'dashboard.kpi.gmv7d', icon: 'TrendCharts', theme: 'warning', route: '/mall/statistics/sales', field: 'gmv7d', valueType: 'money', trendKey: 'gmv7d', compareKey: 'dashboard.kpi.comparePrev7d' },
  { key: 'order7d', title: 'dashboard.kpi.order7d', icon: 'Tickets', theme: 'danger', route: '/mall/order/list', field: 'order7d', valueType: 'num', trendKey: 'order7d', compareKey: 'dashboard.kpi.comparePrev7d' },
  { key: 'buyersToday', title: 'dashboard.kpi.buyersToday', icon: 'User', theme: 'primary', route: '/mall/customer/list', field: 'buyersToday', valueType: 'num', trendKey: 'buyersToday', compareKey: 'dashboard.kpi.compareYesterday' },
  { key: 'avgOrderValue', title: 'dashboard.kpi.avgOrderValue', icon: 'Money', theme: 'success', route: '/mall/statistics/sales', field: 'avgOrderValue', valueType: 'money', trendKey: 'avgOrderValue', compareKey: 'dashboard.kpi.compareYesterday' },
  { key: 'payRate', title: 'dashboard.kpi.payRate', icon: 'CreditCard', theme: 'warning', route: '/mall/statistics/sales', field: 'payRate', valueType: 'percent', trendKey: '' },
  { key: 'refundToday', title: 'dashboard.kpi.refundToday', icon: 'RefreshLeft', theme: 'danger', route: '/mall/order/refund-manage', field: 'refundToday', valueType: 'money', trendKey: '' }
]

/** 兼容旧引用：统计概览卡片就是现在的商城核心指标卡片 */
export const STAT_CARDS = MALL_KPI_CARDS

/**
 * 商城运营待办与预警项：field 与 alerts 对象字段名一致。
 * level 决定计数为 0 时的呈现（normal 灰色 / warn 橙色 / danger 红色徽标）。
 */
export const MALL_ALERT_ITEMS = [
  { key: 'pendingShip', title: 'dashboard.alerts.pendingShip', icon: 'Van', level: 'danger', route: '/mall/order/remind-shipping', field: 'pendingShip' },
  { key: 'pendingPay', title: 'dashboard.alerts.pendingPay', icon: 'Clock', level: 'warn', route: '/mall/order/list', field: 'pendingPay' },
  { key: 'refundAfterSale', title: 'dashboard.alerts.refundAfterSale', icon: 'RefreshLeft', level: 'danger', route: '/mall/order/refund-manage', field: 'refundAfterSale' },
  { key: 'lowStock', title: 'dashboard.alerts.lowStock', icon: 'Box', level: 'warn', route: '/mall/inventory/alert', field: 'lowStock' },
  { key: 'offShelf', title: 'dashboard.alerts.offShelf', icon: 'SoldOut', level: 'normal', route: '/mall/goods/list', field: 'offShelf' },
  { key: 'riskGoods', title: 'dashboard.alerts.riskGoods', icon: 'WarningFilled', level: 'danger', route: '/mall/goods/audit', field: 'riskGoods' },
  { key: 'pendingMerchant', title: 'dashboard.alerts.pendingMerchant', icon: 'OfficeBuilding', level: 'warn', route: '/mall/store/merchant-entry', field: 'pendingMerchant' }
]

/** 快捷操作：默认顺序即注册顺序（全部指向商城动作） */
export const QUICK_ACTIONS = [
  { key: 'new-goods', title: 'dashboard.quickActions.newGoods', icon: 'Plus', color: '#409eff', gradient: 'linear-gradient(135deg, #409eff 0%, #66b1ff 100%)', route: '/mall/goods/list' },
  { key: 'order-ship', title: 'dashboard.quickActions.orderShip', icon: 'Van', color: '#67c23a', gradient: 'linear-gradient(135deg, #67c23a 0%, #95d475 100%)', route: '/mall/order/list' },
  { key: 'refund-audit', title: 'dashboard.quickActions.refundAudit', icon: 'RefreshLeft', color: '#f56c6c', gradient: 'linear-gradient(135deg, #f56c6c 0%, #f89898 100%)', route: '/mall/order/refund-manage' },
  { key: 'stock-check', title: 'dashboard.quickActions.stockCheck', icon: 'Box', color: '#e6a23c', gradient: 'linear-gradient(135deg, #e6a23c 0%, #f3c06b 100%)', route: '/mall/inventory/count' },
  { key: 'goods-category', title: 'dashboard.quickActions.goodsCategory', icon: 'Menu', color: '#909399', gradient: 'linear-gradient(135deg, #909399 0%, #b1b3b8 100%)', route: '/mall/goods/category' },
  { key: 'store-manage', title: 'dashboard.quickActions.storeManage', icon: 'Shop', color: '#409eff', gradient: 'linear-gradient(135deg, #3a7bd5 0%, #409eff 100%)', route: '/mall/store/list' }
]

/** 趋势图可选序列 */
export const TREND_SERIES = [
  { key: 'revenue', title: 'dashboard.charts.revenue' },
  { key: 'order', title: 'dashboard.charts.order' }
]

/** 趋势图可选天数（后端按日聚合 30 天，前端截取） */
export const TREND_DAY_OPTIONS = [7, 30]

/** 热销排行可选维度 */
export const RANK_MODES = ['goods', 'category']

/** 排行条数 */
export const RANK_LIMITS = [5, 8, 10]

/**
 * 卡片 / 动作清单换版即重置的版本号。
 * 2026-09-21 首页商城化把统计卡与快捷操作整套换掉：老偏好里的旧 key 过滤后
 * 可能只剩一两个「恰好同名」的项，页面会呈现出残缺的一行。带版本号后，
 * 老配置在 sanitize 阶段直接回落到新默认值（用户改过的显隐偏好仍可重新勾选）。
 */
const STATS_CONFIG_VERSION = 2

/**
 * 区块注册表。sort 越小越靠前。
 * 组件同步引入：首屏区块异步化只会让首屏多一轮加载等待，
 * 且异步组件在单测里要多轮 flush 才能拿到 DOM。
 */
const coreSections = [
  {
    id: 'hero',
    titleKey: 'dashboard.sections.hero.title',
    hintKey: 'dashboard.sections.hero.hint',
    icon: 'Sunny',
    defaultSort: 0,
    component: markRaw(HeroSection)
  },
  {
    id: 'stats',
    titleKey: 'dashboard.sections.stats.title',
    hintKey: 'dashboard.sections.stats.hint',
    icon: 'TrendCharts',
    defaultSort: 1,
    component: markRaw(MallKpiSection)
  },
  {
    id: 'alerts',
    titleKey: 'dashboard.sections.alerts.title',
    hintKey: 'dashboard.sections.alerts.hint',
    icon: 'WarningFilled',
    defaultSort: 2,
    component: markRaw(MallAlertsSection)
  },
  {
    id: 'trend',
    titleKey: 'dashboard.sections.trend.title',
    hintKey: 'dashboard.sections.trend.hint',
    icon: 'DataLine',
    defaultSort: 3,
    component: markRaw(TrendSection)
  },
  {
    id: 'rank',
    titleKey: 'dashboard.sections.rank.title',
    hintKey: 'dashboard.sections.rank.hint',
    icon: 'Trophy',
    defaultSort: 4,
    component: markRaw(MallRankSection)
  },
  {
    id: 'quick',
    titleKey: 'dashboard.sections.quick.title',
    hintKey: 'dashboard.sections.quick.hint',
    icon: 'Operation',
    defaultSort: 5,
    component: markRaw(QuickActionsSection)
  },
  {
    id: 'widgets',
    titleKey: 'dashboard.sections.widgets.title',
    hintKey: 'dashboard.sections.widgets.hint',
    icon: 'Grid',
    defaultSort: 6,
    component: markRaw(WidgetsSection)
  }
]

export const DASHBOARD_SECTIONS = coreSections

export const getSectionById = (id) => DASHBOARD_SECTIONS.find((s) => s.id === id) || null

/** 默认值：全部区块可见、全部子项全开 */
export const buildDefaultSectionConfig = (id) => {
  switch (id) {
    case 'hero':
      return {
        showGreeting: true,
        showDescription: true,
        showRefresh: true,
        showNotification: true,
        showSettings: true,
        showEdit: true
      }
    case 'stats':
      return { _v: STATS_CONFIG_VERSION, cards: MALL_KPI_CARDS.map((c) => c.key) }
    case 'alerts':
      return { items: MALL_ALERT_ITEMS.map((i) => i.key) }
    case 'rank':
      return { modes: [...RANK_MODES], limit: 8 }
    case 'quick':
      return { _v: STATS_CONFIG_VERSION, actions: QUICK_ACTIONS.map((a) => a.key) }
    case 'trend':
      return { days: 7, series: TREND_SERIES.map((s) => s.key) }
    case 'widgets':
      return { showTitle: true }
    default:
      return {}
  }
}

const asBool = (v, def) => (typeof v === 'boolean' ? v : def)

/** 数组型配置：过滤非法项 + 去重 + 保序；全被过滤掉时回落默认值，避免配置成空页面 */
const normalizeKeyList = (value, allowedKeys) => {
  const list = Array.isArray(value) ? value.filter((k) => allowedKeys.includes(k)) : []
  const seen = new Set()
  const result = []
  list.forEach((k) => {
    if (!seen.has(k)) {
      seen.add(k)
      result.push(k)
    }
  })
  return result.length ? result : [...allowedKeys]
}

/**
 * 配置兜底：与默认值合并，非法值丢弃。
 * 后端返回脏数据、或老用户偏好里没有该区块的 config 时都走这里。
 */
export const sanitizeSectionConfig = (id, config) => {
  const def = buildDefaultSectionConfig(id)
  const cfg = config && typeof config === 'object' ? config : {}
  switch (id) {
    case 'hero':
      return {
        showGreeting: asBool(cfg.showGreeting, def.showGreeting),
        showDescription: asBool(cfg.showDescription, def.showDescription),
        showRefresh: asBool(cfg.showRefresh, def.showRefresh),
        showNotification: asBool(cfg.showNotification, def.showNotification),
        showSettings: asBool(cfg.showSettings, def.showSettings),
        showEdit: asBool(cfg.showEdit, def.showEdit)
      }
    case 'stats':
      // 版本不匹配（换过卡片清单）时整组回落默认，避免残留旧 key 造成残缺布局
      if (cfg._v !== def._v) return { ...def }
      return { _v: def._v, cards: normalizeKeyList(cfg.cards, def.cards) }
    case 'alerts':
      return { items: normalizeKeyList(cfg.items, def.items) }
    case 'rank': {
      const limit = Number(cfg.limit)
      return {
        modes: normalizeKeyList(cfg.modes, def.modes),
        limit: RANK_LIMITS.includes(limit) ? limit : def.limit
      }
    }
    case 'quick':
      if (cfg._v !== def._v) return { ...def }
      return { _v: def._v, actions: normalizeKeyList(cfg.actions, def.actions) }
    case 'trend': {
      const days = Number(cfg.days)
      return {
        days: TREND_DAY_OPTIONS.includes(days) ? days : def.days,
        series: normalizeKeyList(cfg.series, def.series)
      }
    }
    case 'widgets':
      return { showTitle: asBool(cfg.showTitle, def.showTitle) }
    default:
      return { ...def }
  }
}

/** 默认区块列表（后端无记录时使用） */
export const buildDefaultSections = () => DASHBOARD_SECTIONS
  .slice()
  .sort((a, b) => a.defaultSort - b.defaultSort)
  .map((section, index) => ({
    id: section.id,
    visible: 1,
    sort: index,
    config: buildDefaultSectionConfig(section.id)
  }))

export default DASHBOARD_SECTIONS
