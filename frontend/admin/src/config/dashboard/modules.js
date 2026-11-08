/**
 * Core Dashboard 模块注册表。
 *
 * 2026-09-21：OA、MES、CMS、活动四个模块补注册（此前只有 perm、页面无卡片入口）。
 * 它们仍以占位 Widget 呈现（真实业务由各自插件页面承载），卡片仅作首页直达入口，
 * route 指向插件位路径，点击后进入插件页面。
 * ERP 与支付走「宿主卡片 + 运行时判断」：
 * - ErpShipmentWidget 自行判断 erp 插件是否启用，未安装时显示"ERP 插件未安装"空态；
 * - PaymentWidget 读取宿主 /web/payment/dashboard/overview，失败时静默占位。
 * 二者在 2026-09-14 插件化迁移（fc40b9488）中被一并摘掉，2026-09-20 复查时补回。
 *
 * 🔴 2026-09-21 route 逐个校正（此前多数卡片点了没反应）：
 * 原值有两条错因 —— ① 指向已下线/从未存在的页面；② 走 /{code} 旧地址，
 * 依赖 normalizeLegacyPluginPath 重定向到 /plugin/{code}，一旦重定向链上任何
 * 一环不可达，守卫会静默把用户留在仪表盘。现在统一写「最终可达路径」：
 * - payment  /finance/fund/payment-order → /payment/dashboard（库里 248「工作台」，payment 独立服务）
 * - crm      /crm       → /plugin/crm      （插件根路由，不经重定向）
 * - im       /im        → /plugin/im
 * - supplier /supplier  → /plugin/supplier
 * - orderfood/orderfood → /plugin/orderfood
 * - rider    /rider     → /plugin/rider
 * - order    /order     → /mall/order/list （订单管理菜单首个叶子）
 * - goods    /goods     → /mall/goods/list
 * - auth     /auth      → /setting/permission/user（/auth 只会 redirect 到 /login）
 * - dcenter  /dcenter   → /dcenter/dashboard（该页无侧栏菜单，由守卫按模块权限放行）
 */
import { defineAsyncComponent, markRaw } from 'vue'

const asyncCoreWidget = (name) => markRaw(
  defineAsyncComponent(() => import(`@/components/dashboard/${name}.vue`))
)

const coreModules = [
  ['payment', 'dashboard.modules.payment.title', 'Wallet', '#67c23a', '/payment/dashboard', 'PaymentWidget'],
  ['erp', 'dashboard.modules.erp.title', 'Box', '#e6a23c', '/plugin/erp/dashboard', 'ErpShipmentWidget'],
  ['project', 'dashboard.modules.project.title', 'Calendar', '#f56c6c', '/plugin/pm', 'ProjectTimelineWidget'],
  ['crm', 'dashboard.modules.crm.title', 'User', '#409eff', '/plugin/crm', 'CrmWidget'],
  ['order', 'dashboard.modules.order.title', 'List', '#67c23a', '/mall/order/list', 'OrderWidget'],
  ['goods', 'dashboard.modules.goods.title', 'Goods', '#e6a23c', '/mall/goods/list', 'GoodsWidget'],
  ['supplier', 'dashboard.modules.supplier.title', 'OfficeBuilding', '#f56c6c', '/plugin/supplier', 'SupplierWidget'],
  ['workflow', 'dashboard.modules.workflow.title', 'SetUp', '#409eff', '/workflow', 'WorkflowWidget'],
  ['ai', 'dashboard.modules.ai.title', 'MagicStick', '#67c23a', '/ai', 'AiWidget'],
  ['dcenter', 'dashboard.modules.dcenter.title', 'DataLine', '#f56c6c', '/dcenter/dashboard', 'DcenterWidget'],
  ['im', 'dashboard.modules.im.title', 'ChatDotRound', '#409eff', '/plugin/im', 'ImWidget'],
  ['lowcode', 'dashboard.modules.lowcode.title', 'Cpu', '#67c23a', '/lowcode', 'LowcodeWidget'],
  ['orderfood', 'dashboard.modules.orderfood.title', 'KnifeFork', '#f56c6c', '/plugin/orderfood', 'OrderfoodWidget'],
  ['rider', 'dashboard.modules.rider.title', 'Van', '#409eff', '/plugin/rider', 'RiderWidget'],
  ['auth', 'dashboard.modules.auth.title', 'Lock', '#e6a23c', '/setting/permission/user', 'AuthWidget'],
  ['setting', 'dashboard.modules.setting.title', 'Setting', '#f56c6c', '/setting', 'SettingWidget'],
  // 2026-09-21 补齐：库里 dashboard:module:{oa,mes,cms,activity} 四个模块此前未注册，
  // 首页没有卡片入口。注册后由 normalizeItems 追加到已有偏好配置末尾（visible=1）。
  ['oa', 'dashboard.modules.oa.title', 'Notebook', '#409eff', '/plugin/oa/apps', 'OaWidget'],
  ['mes', 'dashboard.modules.mes.title', 'Monitor', '#409eff', '/plugin/mes/dashboard', 'MesWidget'],
  ['cms', 'dashboard.modules.cms.title', 'DocumentCopy', '#67c23a', '/plugin/cms', 'CmsWidget'],
  ['activity', 'dashboard.modules.activity.title', 'Present', '#e6a23c', '/plugin/activity', 'ActivityWidget']
]

export const DASHBOARD_MODULES = coreModules.map(
  ([id, title, icon, color, route, widget], index) => ({
    id,
    title,
    icon,
    color,
    perm: `dashboard:module:${id}`,
    route,
    component: asyncCoreWidget(widget),
    defaultW: 4,
    defaultH: 1,
    defaultSort: index + 1
  })
)

export const getModuleById = (id) => DASHBOARD_MODULES.find((module) => module.id === id) || null

/**
 * 按「卡片直达路径」反查模块（精确匹配，不做前缀放宽）。
 *
 * 用途：路由守卫放行工作台卡片入口。卡片本身已按 `dashboard:module:<id>` 授权，
 * 但目标页未必有侧栏菜单（典型：/dcenter/dashboard、/payment/dashboard），
 * 若守卫仍要求「路径必须在菜单树里」，用户点了卡片只会被静默弹回仪表盘。
 * 只精确匹配注册的 route，不会顺带放开该模块的整棵子树。
 *
 * @param {string} path 目标路径（already 规范化，如 /plugin/crm）
 * @returns {object|null} 命中的模块注册项
 */
export const findModuleByRoute = (path) =>
  DASHBOARD_MODULES.find((module) => module.route === path) || null

export const buildDefaultItems = () => DASHBOARD_MODULES
  .slice()
  .sort((a, b) => a.defaultSort - b.defaultSort)
  .map((module, index) => ({
    moduleId: module.id,
    visible: 1,
    sort: index,
    x: 0,
    y: 0,
    w: module.defaultW,
    h: module.defaultH
  }))

export default DASHBOARD_MODULES
