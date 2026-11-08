/**
 * 站点级二开前端通道 · 纯契约层（T09 / R2.5）。
 *
 * 本模块**不依赖 Vite / Vue / 浏览器**，只描述「站点级扩展」这一条构建期静态装配通道
 * 的命名空间、登记规则与启用判定，因此可被宿主运行时、构建配置和 Node 单测共享：
 * - 运行时：`registry.js` 用它做路由归一化与命名空间强校验；
 * - 构建期：`vite.config.js` 用它做独立的构建输入登记；
 * - 测试期：`tests/site-extension-chunk.test.mjs` 直接 import 本模块做真实断言。
 *
 * 与租户级动态插件（`/plugin/{code}/**`，运行时 ESM 装载）的区别：
 * 站点级扩展在**构建期**进入宿主 bundle，产物是独立 chunk，随宿主一起发布；
 * 只做站点级长期定制，不做租户级动态启停。
 */

/** 站点级扩展路由命名空间前缀。租户级动态插件使用 `/plugin`，两者严格分离。 */
export const SITE_EXTENSION_ROUTE_PREFIX = '/ext'

/** 站点级扩展 code 约定：小写字母/数字开头，可含 `.` `_` `-`。 */
export const SITE_EXTENSION_CODE_PATTERN = /^[a-z0-9][a-z0-9._-]*$/

/** 站点级扩展目录内的入口文件与清单文件约定。 */
export const SITE_EXTENSION_ENTRY_FILE = 'index.js'
export const SITE_EXTENSION_MANIFEST_FILE = 'manifest.js'

/**
 * 官方（宿主）路由命名空间根。站点级扩展**禁止**占用这些路径，
 * 保证「官方 src/views 与官方路由不被覆盖」。此清单与 `src/router/modules/*` 对齐，
 * 仅作防御性守卫；站点级扩展路径恒为 `/ext/{code}`，正常不会命中。
 */
export const OFFICIAL_ROUTE_ROOTS = Object.freeze([
  '/dashboard', '/platform', '/mall', '/user', '/order', '/product', '/project',
  '/setting', '/goods', '/payment', '/permission', '/lowcode', '/dcenter',
  '/workflow', '/rider', '/login', '/register', '/bind-organization',
  // 租户级动态插件命名空间同样属于保留区，站点级扩展不得借用
  '/plugin'
])

/**
 * 校验扩展 code 是否符合约定。
 * @param {string} code 扩展编码
 * @returns {string} 归一化后的 code
 * @throws {Error} code 非法时抛出
 */
export function assertSiteExtensionCode(code) {
  const normalized = String(code == null ? '' : code).trim()
  if (!SITE_EXTENSION_CODE_PATTERN.test(normalized)) {
    throw new Error(`站点级扩展 code 非法（需匹配 ${SITE_EXTENSION_CODE_PATTERN}）：${code}`)
  }
  return normalized
}

/**
 * 把站点级扩展声明的路由路径归一化到 `/ext/{code}/**` 命名空间。
 *
 * 兼容两种历史/简写写法：`/{code}/...`（缺 `/ext` 前缀）会被迁移到 `/ext/{code}/...`。
 * 绝对地址、路径穿越与非本命名空间路径一律拒绝，避免站点级扩展抢占官方或插件路由。
 *
 * @param {string} code 扩展编码
 * @param {string} rawPath 原始路径
 * @returns {string} 归一化后的路径（不含尾斜杠）
 * @throws {Error} 路径非法时抛出
 */
export function normalizeSiteExtensionRoutePath(code, rawPath) {
  const normalizedCode = assertSiteExtensionCode(code)
  const raw = String(rawPath == null ? '' : rawPath).trim()
  if (!raw) {
    throw new Error('站点级扩展路由不能为空')
  }
  if (/^[a-z][a-z0-9+.-]*:\/\//i.test(raw) || raw.startsWith('//')) {
    throw new Error('站点级扩展路由不允许使用绝对地址')
  }
  if (raw.includes('..')) {
    throw new Error('站点级扩展路由不允许包含 ..')
  }

  const withSlash = raw.startsWith('/') ? raw : `/${raw}`
  const namespace = `${SITE_EXTENSION_ROUTE_PREFIX}/${normalizedCode}`

  if (withSlash === namespace || withSlash.startsWith(`${namespace}/`)) {
    const collapsed = withSlash.replace(/\/{2,}/g, '/').replace(/\/+$/, '')
    return collapsed || namespace
  }

  const legacy = `/${normalizedCode}`
  if (withSlash === legacy || withSlash.startsWith(`${legacy}/`)) {
    const migrated = `${namespace}${withSlash.slice(legacy.length)}`
    return migrated.replace(/\/{2,}/g, '/').replace(/\/+$/, '') || namespace
  }

  throw new Error(
    `站点级扩展 ${normalizedCode} 路由必须使用 ${namespace}/ 命名空间，收到：${raw}`
  )
}

/**
 * 判断路径是否落在官方保留路由区（含租户级 `/plugin` 命名空间）。
 * @param {string} path 路径
 * @returns {boolean}
 */
export function isOfficialRoutePath(path) {
  const normalized = String(path == null ? '' : path).trim()
  if (!normalized) return false
  return OFFICIAL_ROUTE_ROOTS.some(
    (root) => normalized === root || normalized.startsWith(`${root}/`)
  )
}

/**
 * 防御性守卫：站点级扩展路径不得落入官方/插件保留区。
 * @param {string} path 已归一化的路径
 * @throws {Error} 命中保留区时抛出
 */
export function assertNotOfficialRoutePath(path) {
  if (isOfficialRoutePath(path)) {
    throw new Error(`站点级扩展不得占用官方或插件路由：${path}`)
  }
}

/**
 * 生成站点级扩展路由名（用于 router.removeRoute 精确卸载与冲突排查）。
 * @param {string} code 扩展编码
 * @param {number} index 路由序号
 * @returns {string}
 */
export function siteExtensionRouteName(code, index) {
  const safe = assertSiteExtensionCode(code).replace(/[^a-z0-9]/gi, '_')
  return `site_${safe}_${Number(index) || 0}`
}

/**
 * 解析构建期启用清单（`import.meta.env.VITE_SITE_EXTENSIONS`）。
 * 支持 `*` / `all`（全部启用）或逗号/空白分隔的 code 列表；空值表示不额外启用。
 * @param {string} value 环境变量原始值
 * @returns {{mode:'all'|'none'|'list', codes:Set<string>}}
 */
export function parseSiteExtensionEnv(value) {
  const raw = String(value == null ? '' : value).trim()
  if (!raw) return { mode: 'none', codes: new Set() }
  const tokens = raw.split(/[,;\s]+/).map((token) => token.trim()).filter(Boolean)
  if (tokens.some((token) => token === '*' || token.toLowerCase() === 'all')) {
    return { mode: 'all', codes: new Set() }
  }
  return { mode: 'list', codes: new Set(tokens) }
}

/**
 * 判断某个站点级扩展在本次构建中是否启用。
 * 规则：构建期清单显式列出 → 启用；环境变量为 `*`/`all` → 启用；否则回落到 manifest.enabled。
 * @param {{code:string, enabled?:boolean}} manifest 扩展清单
 * @param {string} enabledEnv 构建期启用清单
 * @returns {boolean}
 */
export function isSiteExtensionEnabled(manifest, enabledEnv) {
  const code = String(manifest?.code || '').trim()
  if (!code) return false
  const parsed = parseSiteExtensionEnv(enabledEnv)
  if (parsed.mode === 'all') return true
  if (parsed.mode === 'list' && parsed.codes.has(code)) return true
  return manifest?.enabled === true
}

/**
 * 从清单集合中筛出本次构建启用的 code。
 * @param {Array<{code:string,enabled?:boolean}>} manifests 扩展清单集合
 * @param {string} enabledEnv 构建期启用清单
 * @returns {string[]} 升序 code 列表
 */
export function resolveEnabledSiteExtensionCodes(manifests, enabledEnv) {
  return (Array.isArray(manifests) ? manifests : [])
    .filter((manifest) => isSiteExtensionEnabled(manifest, enabledEnv))
    .map((manifest) => String(manifest.code))
    .sort()
}

/**
 * 校验一份站点级扩展清单是否符合约定。
 * @param {{code?:string,name?:string,enabled?:boolean,routes?:Array}} manifest 原始清单
 * @returns {{code:string,name:string,version:string,enabled:boolean,routes:Array}} 归一化清单
 * @throws {Error} 清单非法时抛出
 */
export function validateSiteExtensionManifest(manifest) {
  if (!manifest || typeof manifest !== 'object') {
    throw new Error('站点级扩展清单必须是对象')
  }
  const code = assertSiteExtensionCode(manifest.code)
  const routes = Array.isArray(manifest.routes) ? manifest.routes : []
  routes.forEach((route) => {
    if (!route || !route.path) {
      throw new Error(`站点级扩展 ${code} 存在缺少 path 的路由声明`)
    }
    const normalized = normalizeSiteExtensionRoutePath(code, route.path)
    assertNotOfficialRoutePath(normalized)
  })
  return {
    code,
    name: String(manifest.name || code),
    version: String(manifest.version || '0.0.0'),
    enabled: manifest.enabled === true,
    routes
  }
}

export default {
  SITE_EXTENSION_ROUTE_PREFIX,
  SITE_EXTENSION_CODE_PATTERN,
  SITE_EXTENSION_ENTRY_FILE,
  SITE_EXTENSION_MANIFEST_FILE,
  OFFICIAL_ROUTE_ROOTS,
  assertSiteExtensionCode,
  normalizeSiteExtensionRoutePath,
  isOfficialRoutePath,
  assertNotOfficialRoutePath,
  siteExtensionRouteName,
  parseSiteExtensionEnv,
  isSiteExtensionEnabled,
  resolveEnabledSiteExtensionCodes,
  validateSiteExtensionManifest
}
