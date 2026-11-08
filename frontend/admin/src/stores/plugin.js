import { resolvePluginFrontendUrl } from '@/utils/pluginFrontendUrl'
import { defineStore } from 'pinia'
import { defineAsyncComponent, h, markRaw } from 'vue'
import * as VueRuntime from 'vue'
import * as VueRouterRuntime from 'vue-router'
import * as PiniaRuntime from 'pinia'
import * as VueI18nRuntime from 'vue-i18n'
import * as ElementPlusRuntime from 'element-plus'
import * as ElementPlusIconsRuntime from '@element-plus/icons-vue'
import * as ElementPlusZhCnRuntime from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusEnRuntime from 'element-plus/dist/locale/en.mjs'
import * as ElementPlusFrRuntime from 'element-plus/dist/locale/fr.mjs'
import * as ElementPlusDeRuntime from 'element-plus/dist/locale/de.mjs'
import * as ElementPlusJaRuntime from 'element-plus/dist/locale/ja.mjs'
import * as ElementPlusMsRuntime from 'element-plus/dist/locale/ms.mjs'
import request from '@/utils/request'
import { getPluginList } from '@/api/plugin-center/pluginCenter'
import { useAppStore } from '@/stores/app'
import { buildPluginMenuNodes, normalizePluginRoutePath } from '@/utils/pluginMenuMerge'
import { invalidateUserMenusCache } from '@/utils/userMenusCache'
import { useDashboardStore } from '@/stores/dashboard'
import { getSiteInstanceId } from '@/utils/site'
import { createPluginI18n, registerPluginMessages, unregisterPluginMessages } from '@/i18n-element'
import { PLUGIN_ROUTE_CODE_SET } from '@/config/pluginRouteConfig.js'
import {
  registerPluginLoginProviders,
  unregisterPluginLoginProviders
} from '@/extensions/loginProviders'

/**
 * 插件前端加载器（T10）。
 *
 * 契约（《开源插件化架构》8.2）：插件前端产物是 ESM，默认导出：
 *   export default {
 *     messages: { 'zh-CN': { menu: { name: '优惠券' } }, en: { menu: { name: 'Coupon' } } },
 *     menus: [{ path: '/plugin/coupon', i18nKey: 'menu.name', title: '优惠券', icon: 'Ticket' }],
 *     routes: [{ path: '/plugin/coupon', component: CouponIndex }],
 *     setup(ctx) { ... },
 *     teardown() { ... }
 *   }
 *
 * 宿主职责：
 * 1) 后端返回带版本的 `frontendUrl` → 动态 import（vite 不参与编译，运行时加载）；
 * 2) 组件用 defineAsyncComponent 包装，加载失败有兜底 UI，不拖垮宿主；
 * 3) 菜单写入 appStore 额外菜单源 → 侧栏可见 + 路由守卫放行（权限联动）；
 * 4) 卸载：router.removeRoute + teardown() + 移除 link[data-plugin=code]；
 * 5) 安全红线：插件禁止直连 `/web/**`，只能通过宿主注入的 ctx.callApi。
 */

/** 插件 API 统一入口前缀（T11 由 BFF 泛化转发到插件自身服务） */
export const PLUGIN_API_PREFIX = '/web/plugin'

/**
 * The host request interceptor already returns the business response body.
 * Only unwrap a real Axios-like response; unwrapping every object with a
 * `data` field discards `code` and `message` from plugin API responses.
 */
export function normalizePluginApiResponse(response) {
  const isHttpResponse = response && typeof response === 'object' &&
    Object.prototype.hasOwnProperty.call(response, 'data') &&
    (Object.prototype.hasOwnProperty.call(response, 'status') ||
      Object.prototype.hasOwnProperty.call(response, 'headers') ||
      Object.prototype.hasOwnProperty.call(response, 'config'))
  return isHttpResponse ? response.data : response
}

/** 演示插件开关：默认关闭，仅在真实页面验收时手动打开 */
export const PLUGIN_DEMO_FLAG_KEY = 'pluginDemoEnabled'

/** 内置演示插件产物（随 cloud 静态目录发布，见 frontend/admin/public/plugin-assets/demo） */
export const PLUGIN_DEMO_URL = '/plugin-assets/demo/1.0.0/index.mjs'

export const PLUGIN_DEMO_PLUGIN = {
  code: 'demo-coupon',
  name: '优惠券（内置演示插件）',
  version: '1.0.0',
  frontendUrl: PLUGIN_DEMO_URL,
  enabled: true,
  demo: true
}

/** 宿主 Vue 运行时：注入给插件，避免插件产物出现裸模块说明符 */
let hostVueRuntime = null
let installedPluginsLoadTask = null
/** 后台注册任务的 Promise（首屏放行后仍在跑），供路由守卫按需等待 */
let backgroundPluginsTask = null
/** 正在动态 import 的插件编码，避免安装补注册和后台注册并发装两次 */
const registeringPluginCodes = new Set()

export const PLUGIN_RUNTIME_GLOBAL_KEY = '__HOUCLOUD_PLUGIN_RUNTIME__'

/**
 * Runtime plugin bundles must render with the exact framework instances used
 * by the host. A second bundled Vue runtime breaks component context, scoped
 * slots and composition APIs when the host mounts the plugin component.
 */
export function exposeHostPluginRuntime() {
  if (typeof globalThis === 'undefined') return null
  const runtime = {
    vue: VueRuntime,
    'vue-router': VueRouterRuntime,
    pinia: PiniaRuntime,
    'vue-i18n': VueI18nRuntime,
    'element-plus': ElementPlusRuntime,
    '@element-plus/icons-vue': ElementPlusIconsRuntime,
    'element-plus/dist/locale/zh-cn.mjs': ElementPlusZhCnRuntime,
    'element-plus/dist/locale/en.mjs': ElementPlusEnRuntime,
    'element-plus/dist/locale/fr.mjs': ElementPlusFrRuntime,
    'element-plus/dist/locale/de.mjs': ElementPlusDeRuntime,
    'element-plus/dist/locale/ja.mjs': ElementPlusJaRuntime,
    'element-plus/dist/locale/ms.mjs': ElementPlusMsRuntime
  }
  globalThis[PLUGIN_RUNTIME_GLOBAL_KEY] = runtime
  return runtime
}

exposeHostPluginRuntime()

/**
 * 确保宿主 Vue 运行时已加载
 * @returns {Promise<object>}
 */
async function ensureHostVue() {
  if (!hostVueRuntime) {
    hostVueRuntime = await import('vue')
  }
  return hostVueRuntime
}

/** 插件页面加载失败时的兜底 UI（不依赖宿主页面样式） */
const PluginRouteFallback = {
  name: 'PluginRouteFallback',
  setup() {
    return () =>
      h(
        'div',
        { style: 'padding:32px;color:#909399;font-size:14px;' },
        '插件页面加载失败：请在「系统设置 → 插件中心」确认该插件已启用，或联系插件开发者。'
      )
  }
}

/**
 * 归一化插件 API 路径，强制收敛到 `/web/plugin/{code}/**`
 *
 * 拒绝绝对地址与路径穿越，避免插件绕过宿主直连内部接口。
 *
 * @param {string} code 插件编码
 * @param {string} path 插件声明的相对路径（如 `coupon/list`）
 * @returns {string} 可直接请求的 URL
 */
export function buildPluginApiPath(code, path) {
  const raw = String(path == null ? '' : path).trim()
  if (!raw) {
    throw new Error('插件 API 路径不能为空')
  }
  if (/^[a-z][a-z0-9+.-]*:\/\//i.test(raw) || raw.startsWith('//')) {
    throw new Error('插件 API 不允许使用绝对地址')
  }
  if (raw.includes('..')) {
    throw new Error('插件 API 路径不允许包含 ..')
  }
  const suffix = raw.replace(/^\/+/, '')
  if (!suffix) {
    throw new Error('插件 API 路径不能为空')
  }
  return `${PLUGIN_API_PREFIX}/${code}/${suffix}`
}

/**
 * 规范化插件可访问的 Core API 路径，只放行稳定的 /web 或 /api Core Port。
 *
 * <p>对首段命中插件清单的路径抛错，目的是<b>路径分流</b>：有 PF4J 后端的插件接口只挂在
 * {@code /web/plugin/{code}/**}，前端若发 {@code /web/{code}/**} 必然 404，
 * 这里提前把它变成明确报错，让插件作者在编码期就发现通道用错。
 *
 * <p><b>这不是安全边界</b>：插件产物与宿主同处一个 JS 上下文，{@code fetch()} 可直接绕过。
 * 真正的访问控制由后端 PluginDispatcher 按「当前用户权限」校验，跟调用方是哪个插件无关。
 */
export function buildCoreApiPath(path) {
  const raw = String(path == null ? '' : path).trim()
  if (!raw) throw new Error('Core API 路径不能为空')
  if (/^[a-z][a-z0-9+.-]*:\/\//i.test(raw) || raw.startsWith('//')) {
    throw new Error('Core API 不允许使用绝对地址')
  }
  if (raw.includes('..')) throw new Error('Core API 路径不允许包含 ..')
  const normalized = `/${raw.replace(/^\/+/, '')}`
  const match = normalized.match(/^\/(?:web|api)\/([^/]+)/i)
  if (!match) throw new Error('Core API 必须使用 /web 或 /api 前缀')
  const firstSegment = String(match[1]).toLowerCase()
  if (firstSegment === 'plugin' || PLUGIN_ROUTE_CODE_SET.has(firstSegment)) {
    throw new Error('插件 API 必须通过插件专属通道调用')
  }
  return normalized
}

/**
 * 演示插件开关是否打开（URL 参数优先级高于 localStorage，便于临时验收）
 * @returns {boolean}
 */
export function isPluginDemoEnabled() {
  try {
    if (typeof window === 'undefined') return false
    const value = new URLSearchParams(window.location.search).get('pluginDemo')
    if (value === '1') {
      window.localStorage.setItem(PLUGIN_DEMO_FLAG_KEY, '1')
      return true
    }
    if (value === '0') {
      window.localStorage.removeItem(PLUGIN_DEMO_FLAG_KEY)
      return false
    }
    return window.localStorage.getItem(PLUGIN_DEMO_FLAG_KEY) === '1'
  } catch {
    return false
  }
}

/**
 * 解析插件前端入口 URL
 * @param {{frontendUrl?:string}} plugin 插件信息
 * @returns {string}
 */
function resolveFrontendUrl(plugin) {
  const revisions = typeof __HOST_PLUGIN_FRONTEND_REVISIONS__ === 'undefined' ? {} : __HOST_PLUGIN_FRONTEND_REVISIONS__
  return resolvePluginFrontendUrl(plugin, revisions)
}

/**
 * 归一化插件默认导出
 * @param {string} code 插件编码
 * @param {object} mod 动态 import 的模块
 * @returns {{menus:Array,routes:Array,setup:Function|null,teardown:Function|null}}
 */
export function normalizePluginManifest(code, mod) {
  const manifest = mod?.default ?? mod
  if (!manifest || typeof manifest !== 'object') {
    throw new Error(`插件 ${code} 前端产物缺少默认导出`)
  }
  const namespace = `/plugin/${String(code).trim()}`
  const declaredRoutes = Array.isArray(manifest.routes)
    ? manifest.routes.filter((r) => r && r.path).map((route) => ({
      ...route,
      path: normalizePluginRoutePath(code, route.path),
      children: Array.isArray(route.children) ? route.children : undefined
    }))
    : []
  const invalidRoute = declaredRoutes.find(
    (route) => route.path !== namespace && !route.path.startsWith(`${namespace}/`)
  )
  if (invalidRoute) {
    throw new Error(`插件 ${code} 路由必须使用 ${namespace}/ 命名空间: ${invalidRoute.path}`)
  }
  const routes = declaredRoutes
  const menus = Array.isArray(manifest.menus) ? manifest.menus.filter(Boolean) : []
  const loginProviders = Array.isArray(manifest.loginProviders)
    ? manifest.loginProviders.filter((item) => item && item.entry)
    : []
  // 2026-09-20：插件可向宿主工作台注入面板（slot -> 组件加载器）。
  // 商业模块插件化后，首页的「每日待办 + 临时记事本」由 OA 插件提供，靠这条通道挂回首页。
  const dashboardPanels = manifest.dashboardPanels && typeof manifest.dashboardPanels === 'object'
    ? Object.fromEntries(
      Object.entries(manifest.dashboardPanels).filter(([, loader]) => typeof loader === 'function')
    )
    : {}
  return {
    menus,
    routes,
    loginProviders,
    dashboardPanels,
    layout: typeof manifest.layout === 'function' ? markRaw(defineAsyncComponent({
      loader: async () => {
        const loaded = await manifest.layout()
        return loaded?.default ?? loaded
      },
      errorComponent: PluginRouteFallback,
      timeout: 15000
    })) : null,
    setup: typeof manifest.setup === 'function' ? manifest.setup : null,
    teardown: typeof manifest.teardown === 'function' ? manifest.teardown : null,
    messages: manifest.messages && typeof manifest.messages === 'object' ? manifest.messages : {}
  }
}

/**
 * 生成插件路由名（用于精确 removeRoute）
 * @param {string} code 插件编码
 * @param {number} index 路由序号
 * @returns {string}
 */
function pluginRouteName(code, index) {
  return `plugin_${code}_${index}`
}

/**
 * 移除插件注入的样式/资源标签
 * @param {string} code 插件编码
 */
function removePluginAssetLinks(code) {
  try {
    if (typeof document === 'undefined') return
    document
      .querySelectorAll(`link[data-plugin="${code}"], style[data-plugin="${code}"]`)
      .forEach((el) => el.parentNode?.removeChild(el))
  } catch {
    /* ignore */
  }
}

export const usePluginStore = defineStore('plugin', {
  state: () => ({
    /** 后端返回的已安装插件 */
    installed: [],
    /** 已成功注册的插件编码 */
    loadedCodes: [],
    /** code → 归一化后的生命周期，避免被 Vue 深度代理 */
    manifests: {},
    /** 插件贡献的菜单节点（进 appStore 额外菜单源） */
    pluginMenus: [],
    /** 已注册的插件路由名 */
    registeredRouteNames: [],
    /** 单个插件失败不影响其他插件 */
    errors: [],
    loading: false,
    loadedOnce: false
  }),

  getters: {
    /** 已启用的插件 */
    enabledPlugins: (state) => state.installed.filter((p) => p?.enabled),
    /** 是否已加载任何插件 */
    hasPlugins: (state) => state.loadedCodes.length > 0,
    summary: (state) => ({
      installed: state.installed.length,
      loaded: state.loadedCodes.length,
      menus: state.pluginMenus.length,
      failed: state.errors.length
    })
  },

  actions: {
    /**
     * 加载并注册全部已启用插件
     *
     * 2026-09-19 首屏优化：深链直达（例如刷新 /plugin/geo）时，原来会在这里**串行**
     * 把 20+ 个插件的 index.mjs 全部 import 完才返回，而路由守卫 await 了它，
     * 导致刷新页面白屏十几秒。现在支持 priorityCode：只先把「当前路径所属的插件」
     * 注册好就返回放行导航，其余插件转入后台继续注册，注册完再刷新一次菜单。
     *
     * @param {{force?:boolean, priorityCode?:string|null}} [options]
     *   force=true 时重新走一遍（幂等）；
     *   priorityCode 为当前访问路径对应的插件编码（如 /plugin/geo → geo）
     * @returns {Promise<boolean>} 是否执行完成（单个插件失败也算完成）
     */
    async loadInstalledPlugins(options = {}) {
      const { force = false, priorityCode = null } = options
      if (installedPluginsLoadTask) return installedPluginsLoadTask
      if (this.loadedOnce && !force) return true
      this.loading = true
      this.errors = []
      let backgroundStarted = false
      const loadTask = (async () => {
        let list = []
        try {
          const data = await getPluginList({ status: 'enabled', instanceId: getSiteInstanceId() })
          list = Array.isArray(data) ? data : data?.list || []
        } catch (error) {
          this.errors.push({ code: '*', message: error?.message || String(error) })
          return false
        }
        this.installed = list

        const targets = list.filter((p) => p?.enabled && p?.frontendUrl)

        // 优先插件排到队首（其余保持原顺序，避免菜单顺序被打乱）
        const ordered = priorityCode
          ? [...targets].sort((a, b) => (a.code === priorityCode ? -1 : 0) - (b.code === priorityCode ? -1 : 0))
          : targets

        if (priorityCode && ordered.length > 0 && ordered[0].code === priorityCode) {
          const [first, ...rest] = ordered
          await this.registerPlugin(first)
          // 只等第一个插件：它的路由已经注册，守卫可以直接重导航到目标页
          this.loadedOnce = true
          this.applyMenus()
          backgroundStarted = true
          void this.registerPluginsInBackground(rest)
          return true
        }

        // 非插件路径（如 /dashboard）：插件清单已经拿到，插件本身与当前页面无关，
        // 全部转后台注册并立即放行。否则刷新仪表盘也要干等 20+ 秒的插件加载。
        this.loadedOnce = true
        backgroundStarted = true
        void this.registerPluginsInBackground(ordered)
        return true
      })()
      installedPluginsLoadTask = loadTask
      try {
        return await loadTask
      } finally {
        if (installedPluginsLoadTask === loadTask) installedPluginsLoadTask = null
        // 后台仍在注册剩余插件时保持 loading，避免 UI 误报「已加载完成」
        if (!backgroundStarted) this.loading = false
      }
    },

    /**
     * 后台注册剩余插件（不阻塞首屏导航）
     * @param {Array<Object>} plugins 待注册插件列表
     * @returns {Promise<boolean>}
     */
    async registerPluginsInBackground(plugins = []) {
      const task = (async () => {
        for (const plugin of plugins) {
          await this.registerPlugin(plugin)
        }
        if (isPluginDemoEnabled() && !this.loadedCodes.includes(PLUGIN_DEMO_PLUGIN.code)) {
          await this.registerPlugin(PLUGIN_DEMO_PLUGIN)
        }
        this.applyMenus()
        return true
      })()
      backgroundPluginsTask = task
      try {
        return await task
      } catch (error) {
        this.errors.push({ code: '*', message: error?.message || String(error) })
        return false
      } finally {
        if (backgroundPluginsTask === task) backgroundPluginsTask = null
        this.loading = false
      }
    },

    /**
     * 等待后台插件注册完成
     *
     * 用于守卫：首屏已放行但后台还没轮到某个插件时（用户立刻点了插件菜单），
     * 在这里补等一下，避免直接落到 NotFound。
     * @returns {Promise<boolean>}
     */
    async waitForBackgroundPlugins() {
      if (!backgroundPluginsTask) return true
      try {
        return await backgroundPluginsTask
      } catch {
        return false
      }
    },

    /**
     * 注册单个插件：动态 import → 注册路由 → 执行 setup → 合并菜单
     * @param {{code:string,frontendUrl:string,name?:string,version?:string}} plugin 插件信息
     * @returns {Promise<boolean>} 是否注册成功
     */
    async registerPlugin(plugin) {
      const code = plugin?.code
      if (!code || this.loadedCodes.includes(code) || registeringPluginCodes.has(code)) return false
      const url = resolveFrontendUrl(plugin)
      if (!url) {
        this.errors.push({ code, message: '缺少插件前端入口 URL' })
        return false
      }
      registeringPluginCodes.add(code)

      try {
        exposeHostPluginRuntime()
        const mod = await import(/* @vite-ignore */ url)
        const manifest = normalizePluginManifest(code, mod)
        const router = await this.resolveRouter()
        if (!router) throw new Error('路由实例不可用')

        // 先执行 setup：插件可在其中完成初始化，并接收宿主注入的 ctx（含 Vue 运行时）
        const ctx = await this.buildContext(plugin)
        registerPluginMessages(code, manifest.messages)
        const pluginI18n = createPluginI18n(code)
        if (manifest.setup) {
          await manifest.setup(ctx)
        }

        let routeIndex = 0
        const registerRoute = (route, parentName = null) => {
          const path = String(route.path).startsWith('/') ? String(route.path) : `/${route.path}`
          const name = route.name || pluginRouteName(code, routeIndex++)
          const routeRecord = {
            path,
                name,
                component: defineAsyncComponent({
                  loader: async () => {
                    const component = typeof route.component === 'function'
                      ? await route.component()
                      : route.component
                    return component?.default ?? component ?? PluginRouteFallback
                  },
                  errorComponent: PluginRouteFallback,
                  delay: 200,
                  timeout: 15000
            }),
            meta: {
              title: route.meta?.titleKey ? pluginI18n.t(route.meta.titleKey) : (route.meta?.title || route.title || plugin.name || code),
              permissionPath: path,
              pluginCode: code,
              pluginRoute: true
            }
          }
          if (Array.isArray(route.children) && route.children.length) {
            routeRecord.children = route.children.map((child) => {
              const childPath = String(child.path || '').replace(/^\/+/, '')
              const childName = child.name || pluginRouteName(code, routeIndex++)
              const childRecord = {
                ...child,
                path: childPath,
                    name: childName,
                    component: defineAsyncComponent({
                      loader: async () => {
                        const component = typeof child.component === 'function'
                          ? await child.component()
                          : child.component
                        return component?.default ?? component ?? PluginRouteFallback
                      },
                      errorComponent: PluginRouteFallback,
                      delay: 200,
                      timeout: 15000
                }),
                meta: {
                  title: child.meta?.titleKey ? pluginI18n.t(child.meta.titleKey) : (child.meta?.title || child.title || plugin.name || code),
                  permissionPath: `${path}/${childPath}`.replace(/\/+/g, '/'),
                  pluginCode: code,
                  pluginRoute: true
                }
              }
              if (Array.isArray(child.children) && child.children.length) {
                childRecord.children = child.children
              }
              if (!this.registeredRouteNames.includes(childName)) this.registeredRouteNames.push(childName)
              return childRecord
            })
          }
          router.addRoute(parentName || 'Layout', routeRecord)
          if (!this.registeredRouteNames.includes(name)) {
            this.registeredRouteNames.push(name)
          }
        }
        manifest.routes.forEach((route) => registerRoute(route))

        // 插件声明的工作台面板（如 OA 的「每日待办 + 临时记事本」）挂到首页对应 slot
        if (Object.keys(manifest.dashboardPanels || {}).length) {
          const dashboardStore = useDashboardStore()
          Object.entries(manifest.dashboardPanels).forEach(([slot, loader]) => {
            dashboardStore.registerPluginPanel(slot, {
              code,
              component: defineAsyncComponent({
                loader: async () => {
                  const imported = await loader()
                  return imported?.default ?? imported
                },
                timeout: 15000
              })
            })
          })
        }
        this.manifests[code] = markRaw(manifest)
        this.pluginMenus.push(...buildPluginMenuNodes(plugin, manifest.menus))
        if (manifest.loginProviders.length) {
          registerPluginLoginProviders(code, manifest.loginProviders)
        }
        this.loadedCodes.push(code)
        return true
      } catch (error) {
        this.errors.push({ code, message: error?.message || String(error) })
        return false
      } finally {
        registeringPluginCodes.delete(code)
      }
    },

    /**
     * 安装 / 卸载 / 启停之后补齐运行时。
     * 首屏加载只拿过一次已启用列表，之后新装的插件不会再进 loadedCodes，
     * 右上角即使出现入口，点进去也匹配不到路由。
     * @returns {Promise<boolean>}
     */
    async syncInstalledPlugins() {
      if (installedPluginsLoadTask) {
        try { await installedPluginsLoadTask } catch { /* 首屏失败不阻断本次补注册 */ }
      }
      if (backgroundPluginsTask) {
        try { await backgroundPluginsTask } catch { /* 同上 */ }
      }
      const data = await getPluginList({ status: 'enabled', instanceId: getSiteInstanceId() })
      const list = Array.isArray(data) ? data : data?.list || []
      this.installed = list
      const enabledCodes = new Set(
        list.filter((plugin) => plugin?.enabled && plugin?.frontendUrl).map((plugin) => plugin.code)
      )
      if (isPluginDemoEnabled()) enabledCodes.add(PLUGIN_DEMO_PLUGIN.code)
      for (const code of [...this.loadedCodes]) {
        if (!enabledCodes.has(code)) await this.unregisterPlugin(code)
      }
      for (const plugin of list) {
        if (plugin?.enabled && plugin?.frontendUrl) await this.registerPlugin(plugin)
      }
      if (isPluginDemoEnabled() && !this.loadedCodes.includes(PLUGIN_DEMO_PLUGIN.code)) {
        await this.registerPlugin(PLUGIN_DEMO_PLUGIN)
      }
      this.applyMenus()
      return true
    },

    /**
     * 卸载插件：teardown → removeRoute → 移除菜单与资源标签
     * @param {string} code 插件编码
     * @returns {Promise<boolean>}
     */
    async unregisterPlugin(code) {
      if (!code) return false
      const manifest = this.manifests[code]
      try {
        if (manifest?.teardown) await manifest.teardown()
      } catch (error) {
        this.errors.push({ code, message: `teardown 失败: ${error?.message || error}` })
      }

      const router = await this.resolveRouter()
      if (router) {
        for (const name of this.registeredRouteNames.filter((n) => n.startsWith(`plugin_${code}_`))) {
          if (router.hasRoute(name)) router.removeRoute(name)
        }
      }
      this.registeredRouteNames = this.registeredRouteNames.filter((n) => !n.startsWith(`plugin_${code}_`))
      this.pluginMenus = this.pluginMenus.filter((menu) => menu.pluginCode !== code)
      unregisterPluginLoginProviders(code)
      useDashboardStore().unregisterPluginPanels(code)
      delete this.manifests[code]
      unregisterPluginMessages(code)
      this.loadedCodes = this.loadedCodes.filter((c) => c !== code)
      removePluginAssetLinks(code)
      // 菜单生命周期联动（方案 §9.3，修缺口 G3）：后端已删权限菜单行，
      // 但 localStorage 快照（houcloud_user_menus_v13）仍持有旧行，命中缓存即渲染幽灵菜单。
      // 必须失效本地缓存并强拉服务端菜单，让 baseMenus 一并收敛。
      try {
        invalidateUserMenusCache()
        const appStoreRef = useAppStore()
        appStoreRef.fetchMenus(true).catch(() => {})
      } catch (error) {
        console.warn('[PluginStore] 卸载后刷新用户菜单失败', error?.message || error)
      }
      this.applyMenus()
      return true
    },

    /** 清空全部插件（退出登录 / 重置时调用） */
    async clearPlugins() {
      for (const code of [...this.loadedCodes]) {
        await this.unregisterPlugin(code)
      }
      this.installed = []
      this.pluginMenus = []
      this.registeredRouteNames = []
      this.errors = []
      this.loadedOnce = false
      this.applyMenus()
    },

    /** 供宿主聚合页按需调用已安装插件的 API，不暴露插件实现类。 */
    async callPluginApi(code, path, body = {}, options = {}) {
      const method = String(options?.method || 'post').toLowerCase()
      if (!['get', 'post', 'put', 'delete', 'patch'].includes(method)) {
        throw new Error(`插件 API 不支持请求方法：${method}`)
      }
      const url = buildPluginApiPath(code, path)
      const cfg = options?.config || {}
      // GET/DELETE 的查询串必须合并 body 与 config.params：插件普遍在 body 里注入
      // projectId，而调用方又常把自身查询条件放在 config.params，
      // 早期写法 `{ params: body, ...config }` 会让 config.params 覆盖 body，
      // projectId 丢失 → 后端返回「缺少项目上下文」。
      if (method === 'get' || method === 'delete') {
        const response = await request[method](url, {
          ...cfg,
          params: { ...(cfg.params || {}), ...(body || {}) }
        })
        return normalizePluginApiResponse(response)
      }
      const response = await request[method](url, body, cfg)
      return normalizePluginApiResponse(response)
    },

    /** 供插件前端访问稳定 Core Port；访问其他插件请走 callPluginApi（专属通道）。 */
    async callCoreApi(path, body = {}, options = {}) {
      const method = String(options?.method || 'post').toLowerCase()
      if (!['get', 'post', 'put', 'delete', 'patch'].includes(method)) {
        throw new Error(`Core API 不支持请求方法：${method}`)
      }
      const url = buildCoreApiPath(path)
      const cfg = options?.config || {}
      // 同 callPluginApi：GET/DELETE 需合并 body 与 config.params，避免注入的
      // projectId 等上下文参数被 config.params 覆盖。
      if (method === 'get' || method === 'delete') {
        const response = await request[method](url, {
          ...cfg,
          params: { ...(cfg.params || {}), ...(body || {}) }
        })
        return normalizePluginApiResponse(response)
      }
      const response = await request[method](url, body, cfg)
      return normalizePluginApiResponse(response)
    },

    /** 把插件菜单写入 app store 的额外菜单源（触发菜单树重建 → 侧栏与权限联动） */
    applyMenus() {
      const appStore = useAppStore()
      appStore.setExtraMenus(this.pluginMenus)
    },

    /**
     * 宿主注入给插件的运行上下文
     * @param {object} plugin 插件信息
     * @returns {Promise<object>} ctx
     */
    async buildContext(plugin) {
      const code = plugin.code
      const store = this
      const vue = await ensureHostVue()
      const router = await store.resolveRouter()
      return {
        pluginCode: code,
        version: plugin.version,
        host: {
          vue,
          router,
          navigate: (path) => router?.push(path)
        },
        callApi(path, body = {}, options = {}) {
          return store.callPluginApi(code, path, body, options)
        },
        /**
         * Explicit cross-plugin call for composite plugin screens. The target
         * code is still normalized through the same /web/plugin/{code} guard.
         */
        callPluginApi(targetCode, path, body = {}, options = {}) {
          return store.callPluginApi(targetCode, path, body, options)
        },
        callCoreApi(path, body = {}, options = {}) {
          return store.callCoreApi(path, body, options)
        },
        i18n: createPluginI18n(code),
        hasMenu(path) {
          const target = String(path || '').replace(/\/+$/, '')
          return store.pluginMenus.some((menu) => String(menu.paths || '').replace(/\/+$/, '') === target)
        }
      }
    },

    /**
     * 获取路由实例（动态 import 规避 store ↔ router ↔ guard 的循环依赖）
     * @returns {Promise<object|null>}
     */
    async resolveRouter() {
      try {
        const mod = await import('@/router')
        return mod?.default || null
      } catch {
        return null
      }
    }
  }
})

export default usePluginStore
