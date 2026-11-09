import { createRouter, createWebHistory } from 'vue-router'
import { getAppBase } from '@/utils/appBase'
import Layout from '@/layouts/Layout.vue'
import { beforeEachGuard } from '@/utils/routeGuard'
import { loginRoute, registerRoute, forgotPasswordRoute } from './modules/permission'

// 导入各个模块的路由
import userRoutes from './modules/user'
import productRoutes from './modules/product'
import orderRoutes from './modules/order'
import settingRoutes from './modules/setting'
import mallRoutes from './modules/mall'
import aiRoutes from './modules/ai'
import authRoutes from './modules/permission'
import dcenterRoutes from './modules/dcenter'
import goodsRoutes from './modules/goods'
// 低代码平台（views/lowcode）不随开源包分发，路由同步移除。
import workflowRoutes from './modules/workflow'
import platformRoutes from './modules/platform'
import paymentRoutes from './modules/payment'
import umsCorpOrderRoutes from './modules/ums-corp-order'
// GEO 模块路由（复制自 frontend/geo/src/router/modules/geo.js）

// 导入仪表盘页面
import Dashboard from '@/views/Dashboard.vue'
import { createLegacyPluginRedirects } from '@/config/pluginRouteConfig.js'

// 迁移到 PF4J 动态插件路由后，保留历史直达 URL，避免书签/旧菜单落到 404。
// 重定向目标由插件命名空间承接，插件仍按需动态加载，不在宿主静态编译页面。
const legacyPluginRedirects = createLegacyPluginRedirects()

// 2026-09-18 插件化摘除：项目管理(/project→pm 插件)、骑手配送(/rider)、AI网关(/aigateway)
// 从宿主静态路由迁入插件命名空间。这里只做地址兼容重定向（保留子路径），
// 不进 pluginRouteConfig 的 PLUGIN_ROUTE_CODES——那份清单被 callCoreApi 用来拦截
// 插件 API 穿透，而 rider/aigateway 的后端通道恰恰是 /web/rider、/web/aigateway。
const legacyModuleRedirect = (legacyRoot, pluginNs) => [
  {
    path: legacyRoot,
    redirect: pluginNs,
    meta: { legacyModuleRedirect: true }
  },
  {
    path: `${legacyRoot}/:pathMatch(.*)*`,
    redirect: (to) => {
      const suffix = to.params?.pathMatch
      const nestedPath = Array.isArray(suffix) ? suffix.join('/') : String(suffix || '')
      return `${pluginNs}${nestedPath ? `/${nestedPath}` : ''}`
    },
    meta: { legacyModuleRedirect: true }
  }
]

const legacyBuiltinModuleRedirects = [
  ...legacyModuleRedirect('/project', '/plugin/pm'),
  ...legacyModuleRedirect('/rider', '/plugin/rider'),
  ...legacyModuleRedirect('/aigateway', '/plugin/aigateway'),
  ...legacyModuleRedirect('/im', '/plugin/im'),
  // 支付中心已回归 payment 独立服务。旧插件地址只做兼容，页面由宿主 /payment/** 渲染。
  ...legacyModuleRedirect('/plugin/unipay', '/payment')
]

const routes = [
  // 登录路由
  loginRoute,
  // 微信快捷登录回调页（公开页面，微信扫码确认后回跳）
  {
    path: '/wechat-callback',
    name: 'WechatCallback',
    component: () => import('@/views/permission/WechatCallback.vue'),
    meta: { title: '微信登录', public: true }
  },
  // 统一登录渠道公开回调页。必须由宿主静态注册，不能依赖登录后才装载的插件路由。
  {
    path: '/plugin/unilogin/callback/:channel',
    name: 'UnifiedLoginCallback',
    component: () => import('@/views/permission/UnifiedLoginCallback.vue'),
    meta: { title: '第三方登录', public: true }
  },
  // 注册路由
  registerRoute,
  // 忘记密码路由
  forgotPasswordRoute,
  // 组织绑定页面（需要登录但不需要组织）
  {
    path: '/bind-organization',
    name: 'BindOrganization',
    component: () => import('@/views/permission/BindOrganization.vue'),
    meta: { title: '绑定组织', skipMenuCheck: true }
  },
  // 合同签署页面（公开页面，不需要登录）
  {
    path: '/contract/sign/:id',
    name: 'ContractSign',
    // Public contract signing remains a thin compatibility page while the
    // authenticated OA screens are loaded from the OA plugin.
    component: () => import('@/views/compat/ContractSign.vue'),
    meta: { title: '合同签署', public: true }
  },
  // 一键重启页面（公开页面，需管理员令牌）
  {
    path: '/restartall',
    name: 'RestartAll',
    component: () => import('@/views/admin/RestartAll.vue'),
    meta: { title: 'Java服务管理', public: true }
  },
  // Java 服务管理（IDEA Services 风格，公开页面，需管理员令牌）
  {
    path: '/admin/services',
    redirect: '/restartall'
  },
  {
    path: '/',
    // name 供插件动态路由挂载使用（T10：router.addRoute('Layout', pluginRoute)）
    name: 'Layout',
    component: Layout,
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: '/dashboard',
        component: Dashboard,
        meta: { title: '仪表盘' }
      },
      // 左下角「个人中心」入口：不属于任何菜单分组，需跳过菜单权限校验
      {
        path: '/profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心', skipMenuCheck: true }
      },
      // 银联企业订单分账：宿主静态路由兜底（优先于 PluginPending 通配）
      umsCorpOrderRoutes,
      // 插件深链「待注册」占位：插件路由是运行时 router.addRoute 注册的，
      // 冷启动直接打开 /plugin/{code}/** 时路由表里还没有它，会落到 404 兜底 ⇒
      // 用户看到「页面不存在」，但插件其实只是还在下载前端产物
      // （④→① 公网回源带宽有限，PM 这类 3MB 产物要十几秒）。
      // 这里用一条通配路由占位，命中后显示「正在加载应用」，插件注册完成即自动跳转。
      // 挂在 Layout 下（与插件路由同级）⇒ 保留侧边栏，且插件路由注册后
      // 由 vue-router 按更精确的静态路径优先匹配（已用 vue-router 4 实测确认）。
      {
        path: '/plugin/:pluginCode(.*)',
        name: 'PluginPending',
        component: () => import('@/views/error/PluginPending.vue'),
        meta: { title: '正在加载应用' }
      },
      ...legacyPluginRedirects,
      // 2026-09-18 插件化摘除的三个内置模块的旧地址兼容（/project /rider /aigateway）
      ...legacyBuiltinModuleRedirects,
      // 插件中心历史菜单曾使用 /addons、/addon；统一收敛到当前页面。
      // 保留兼容入口，避免数据库菜单或旧书签在迁移期间落入 404。
      {
        path: '/addons',
        redirect: '/platform/plugins'
      },
      {
        path: '/addon',
        redirect: '/platform/plugins'
      },
      {
        path: '/mall/addon',
        redirect: '/platform/plugins'
      },
      // 用户管理路由
      userRoutes,
      // 商品管理路由
      productRoutes,
      // 订单管理路由
      orderRoutes,
      // 系统设置路由
      settingRoutes,
      // 项目管理已迁入 pm 插件（/plugin/pm），见 legacyBuiltinModuleRedirects
      // PM管理路由
      // 商城模块路由
      mallRoutes,
      // 平台管理：插件中心、插件市场、授权与订单
      platformRoutes,
      // 活动模块路由
      // AI模块路由
      aiRoutes,
      // AI网关已迁入 aigateway 插件（/plugin/aigateway），见 legacyBuiltinModuleRedirects
      // 认证模块路由
      authRoutes,
      // 内容管理路由
      // CRM模块路由
      // 数据中心路由
      dcenterRoutes,
      // ERP 页面由 ERP 插件安装后通过插件清单动态注册，不在宿主静态路由中编译。
      // 商品模块路由
      goodsRoutes,
      // 即时通讯路由
      // 低代码路由
      // 制造执行系统路由
      // 点餐模块路由
      // 支付中心：payment 独立服务的后台页面，不经过插件加载器。
      paymentRoutes,
      // 骑手配送已迁入 rider 插件（/plugin/rider），见 legacyBuiltinModuleRedirects
      // 供应商模块路由
      // 工作流模块路由
      workflowRoutes,
      // 财务中心路由
      // AICode 二开工作台
      // GEO 模块路由（复制自 frontend/geo）
    ]
  },
  // 404 路由处理 - 必须放在最后（避免未注册路由静默跳仪表盘导致「菜单点了没反应」）
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  // 子路径部署（如 unimall.houjit.com'/admin）时基路径为 '/admin/'，
  // 根路径部署（unimall.houjit.com）时为 '/'，保证 router.push('/dashboard')
  // 解析出的是 /admin/dashboard 而不是 /dashboard
  history: createWebHistory(getAppBase()),
  routes
})

// 使用路由守卫
router.beforeEach(beforeEachGuard)

/**
 * 处理动态 import 加载失败问题
 * 场景：新版本上线后，旧 hash 的 chunk 文件已被清理（emptyOutDir），
 * 但用户浏览器仍缓存着旧 index.html，路由跳转时 fetch 旧 chunk 会 404，
 * 报错信息形如：Failed to fetch dynamically imported module: .../assets/Workbench-xxx.js
 * 解决方案：捕获错误后强制 reload，让浏览器重新拉取最新 index.html，
 * 通过 sessionStorage 标记防止 reload 后仍失败导致的死循环。
 * @param {Error} err - 路由错误对象
 * @param {Object} to - 目标路由
 */
router.onError((err, to) => {
  const msg = err && (err.message || String(err)) || ''
  const isChunkLoadError = (
    err?.name === 'ChunkLoadError' ||
    /Failed to fetch dynamically imported module/i.test(msg) ||
    /Loading chunk [\w-]+ failed/i.test(msg) ||
    /Importing a module script failed/i.test(msg) ||
    /error loading dynamically imported module/i.test(msg)
  )
  if (!isChunkLoadError) return

  const reloadKey = '__chunk_reload_at__'
  const last = Number(sessionStorage.getItem(reloadKey) || 0)
  const now = Date.now()
  // 10 秒内已经刷过一次就不再刷，避免死循环
  if (now - last < 10000) {
    console.error('[Router] 动态模块加载失败且已刷新过，停止重试：', err)
    return
  }
  sessionStorage.setItem(reloadKey, String(now))

  const target = (to && to.fullPath) ? to.fullPath : (window.location.pathname + window.location.search + window.location.hash)
  console.warn('[Router] 检测到旧版本 chunk 失效，自动刷新到：', target)
  window.location.replace(target)
})

export default router
