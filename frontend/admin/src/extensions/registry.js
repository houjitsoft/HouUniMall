/**
 * 站点级二开前端通道 · 宿主登记机制（T09 / R2.5）。
 *
 * 这一条是**构建期静态装配**通道，与租户级动态插件（`stores/plugin.js` 的运行时
 * 动态 `import()`）是两条独立的路：
 *
 * | 维度 | 站点级扩展（本模块） | 租户级动态插件（plugin.js） |
 * |---|---|---|
 * | 装配时机 | 构建期进入宿主 bundle（独立 chunk） | 运行时动态 import ESM |
 * | 路由命名空间 | `/ext/{code}/**` | `/plugin/{code}/**` |
 * | 启停 | 重新构建 | 插件中心按租户启停 |
 * | 用途 | 站点级长期定制 | 可购买、可授权、可启停的页面 |
 *
 * 登记流程：
 * 1. `discoverSiteExtensionManifests()` 用 `import.meta.glob` **静态**发现扩展清单；
 * 2. `loadSiteExtensionEntry(code)` 懒加载扩展入口（Vite 为其生成独立异步 chunk）；
 * 3. `registerSiteExtension(router, manifest)` 把路由挂到 `Layout` 下，路径恒为
 *    `/ext/{code}/**`，并强校验命名空间、拒绝占用官方/插件路由。
 *
 * 宿主接线（唯一调用点）：
 * ```js
 * import { installSiteExtensions } from '@/extensions/registry'
 * // 传入根路由实例，通常在 Layout 初始化（登录后）调用一次
 * await installSiteExtensions(router)
 * ```
 */

import { defineAsyncComponent, h, markRaw } from 'vue'
import architecture from '../../../../architecture/services.json'
const serviceNames = new Set(architecture.services.map((service) => service.name))
import {
  assertNotOfficialRoutePath,
  isSiteExtensionEnabled,
  normalizeSiteExtensionRoutePath,
  siteExtensionRouteName,
  validateSiteExtensionManifest
} from './siteExtensionContract'

/** 静态发现全部站点级扩展清单（构建期由 Vite 展开，产物随宿主 chunk）。 */
const manifestModules = import.meta.glob('./*/*/manifest.js', { eager: true })

/** 站点级扩展入口：懒加载，每个入口会被 Vite 拆成独立异步 chunk。 */
const entryModules = import.meta.glob('./*/*/index.js')

/** 扩展页面加载失败时的兜底 UI（不依赖宿主页面样式）。 */
const SiteExtensionRouteFallback = {
  name: 'SiteExtensionRouteFallback',
  setup() {
    return () =>
      h(
        'div',
        { style: 'padding:32px;color:#909399;font-size:14px;' },
        '站点级扩展页面加载失败：请确认该扩展已随宿主一起构建，并检查扩展入口导出。'
      )
  }
}

/**
 * 从 glob 结果路径中提取扩展 code。
 * @param {string} modulePath 形如 `./base/example-site/manifest.js`
 * @returns {string} 扩展 code
 */
function codeFromModulePath(modulePath) {
  const match = String(modulePath).match(/^\.\/[^/]+\/([^/]+)\//)
  return match ? match[1] : ''
}

/**
 * 静态发现并校验全部站点级扩展清单。
 * @returns {Array<object>} 归一化清单列表（按 code 升序）
 */
export function discoverSiteExtensionManifests() {
  const manifests = []
  for (const [modulePath, mod] of Object.entries(manifestModules)) {
    const declared = mod?.default ?? mod
    const code = codeFromModulePath(modulePath)
    try {
      const hostService = modulePath.split('/')[1]
      if (!serviceNames.has(hostService) || declared?.hostService !== hostService || declared?.code !== code) {
        throw new Error(`站点扩展清单归属与目录不一致：${modulePath}`)
      }
      if (Object.keys(manifestModules).filter((path) => codeFromModulePath(path) === code).length !== 1) {
        throw new Error(`站点扩展编码重复：${code}`)
      }
      const normalized = validateSiteExtensionManifest({ ...declared, code: declared?.code || code })
      manifests.push(markRaw({ ...normalized, hostService }))
    } catch (error) {
      // 单个清单非法不拖垮宿主，记录后在登记时跳过
      manifests.push(
        markRaw({
          code: code || 'unknown',
          name: code || 'unknown',
          version: '0.0.0',
          enabled: false,
          routes: [],
          invalid: true,
          error: error?.message || String(error)
        })
      )
    }
  }
  return manifests.sort((a, b) => (a.code < b.code ? -1 : a.code > b.code ? 1 : 0))
}

/**
 * 懒加载站点级扩展入口。
 * @param {string} code 扩展编码
 * @returns {Promise<object|null>} 入口默认导出；缺失时返回 null
 */
export async function loadSiteExtensionEntry(code) {
  const paths = Object.keys(entryModules).filter((path) => codeFromModulePath(path) === code)
  if (paths.length !== 1) return null
  const loader = entryModules[paths[0]]
  if (typeof loader !== 'function') return null
  const mod = await loader()
  return mod?.default ?? mod ?? null
}

/**
 * 归一化扩展入口：产出一组可直接挂载到 `Layout` 的路由记录。
 * @param {string} code 扩展编码
 * @param {object} entry 入口默认导出（含 routes）
 * @returns {Array<object>} 归一化后的路由声明（path/name/component/title）
 * @throws {Error} 路由越界时抛出
 */
export function normalizeSiteExtensionEntry(code, entry) {
  const routes = Array.isArray(entry?.routes) ? entry.routes : []
  return routes.map((route, index) => {
    if (!route || !route.path) {
      throw new Error(`站点级扩展 ${code} 存在缺少 path 的路由声明`)
    }
    const path = normalizeSiteExtensionRoutePath(code, route.path)
    assertNotOfficialRoutePath(path)
    return {
      path,
      name: route.name || siteExtensionRouteName(code, index),
      component: route.component,
      title: route.title || route.meta?.title || entry?.name || code
    }
  })
}

/**
 * 登记单个站点级扩展：读取入口 → 归一化路由 → 挂到 Layout。
 * @param {import('vue-router').Router} router 宿主路由实例
 * @param {object} manifest 已校验的扩展清单
 * @param {{urlPrefix?:string}} [options] 可选配置
 * @returns {Promise<{code:string, routes:string[], menus:number}>} 登记结果
 * @throws {Error} 入口缺失或路由越界时抛出
 */
export async function registerSiteExtension(router, manifest, options = {}) {
  const code = manifest?.code
  if (!router || typeof router.addRoute !== 'function') {
    throw new Error('站点级扩展登记需要可用的 router 实例')
  }
  const entry = await loadSiteExtensionEntry(code)
  if (!entry) {
    throw new Error(`站点级扩展 ${code} 缺少入口 src/extensions/${manifest.hostService}/${code}/index.js`)
  }
  const normalizedRoutes = normalizeSiteExtensionEntry(code, entry)

  const registered = []
  normalizedRoutes.forEach((route) => {
    const record = {
      path: route.path,
      name: route.name,
      component: defineAsyncComponent({
        loader: async () => route.component ?? SiteExtensionRouteFallback,
        errorComponent: SiteExtensionRouteFallback,
        delay: 200,
        timeout: 15000
      }),
      meta: {
        title: route.title,
        permissionPath: route.path,
        extensionCode: code,
        siteExtension: true
      }
    }
    if (router.hasRoute(record.name)) {
      throw new Error(`站点级扩展路由名冲突：${record.name}`)
    }
    router.addRoute('Layout', record)
    registered.push(route.path)
  })

  const menus = Array.isArray(entry?.menus) ? entry.menus.length : 0
  return { code, routes: registered, menus }
}

/**
 * 装配全部启用的站点级扩展（宿主唯一接线入口）。
 * @param {import('vue-router').Router} router 宿主路由实例
 * @param {{enabled?:string}} [options] enabled 覆盖构建期启用清单（便于测试/验收）
 * @returns {Promise<{registered:Array,skipped:Array,failed:Array}>} 装配汇总
 */
export async function installSiteExtensions(router, options = {}) {
  const enabledEnv =
    options.enabled !== undefined
      ? options.enabled
      : import.meta?.env?.VITE_SITE_EXTENSIONS
  const summary = { registered: [], skipped: [], failed: [] }
  for (const manifest of discoverSiteExtensionManifests()) {
    if (manifest.invalid) {
      summary.skipped.push({ code: manifest.code, reason: manifest.error })
      continue
    }
    if (!isSiteExtensionEnabled(manifest, enabledEnv)) {
      summary.skipped.push({ code: manifest.code, reason: '未启用（构建期清单未包含）' })
      continue
    }
    try {
      const result = await registerSiteExtension(router, manifest)
      summary.registered.push(result)
    } catch (error) {
      summary.failed.push({ code: manifest.code, reason: error?.message || String(error) })
    }
  }
  return summary
}

export default {
  discoverSiteExtensionManifests,
  loadSiteExtensionEntry,
  normalizeSiteExtensionEntry,
  registerSiteExtension,
  installSiteExtensions
}
