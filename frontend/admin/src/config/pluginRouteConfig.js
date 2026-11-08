/**
 * 已迁移到 PF4J 动态前后端的产品插件。
 *
 * 三个消费方共用这份清单：旧地址重定向（{@link createLegacyPluginRedirects}）、
 * 插件命名空间判定、以及 {@code buildCoreApiPath()} 的路径分流校验。
 *
 * <p>⚠️ <b>进这份清单等于声明「该插件的接口挂在 `/web/plugin/{code}/**` 上」</b>：
 * 有 PF4J 后端的插件，BFF 只暴露插件专属通道，前端若发 `/web/{code}/**` 必然 404。
 * {@code buildCoreApiPath()} 会提前把这类请求变成明确报错（fail fast），
 * 让插件作者在编码期就发现通道用错，而不是上线后拿到一串 404。
 *
 * <p>注意这<b>不是安全边界</b>：插件产物与宿主同处一个 JS 上下文，`fetch()` 可直接绕过；
 * 真正的访问控制由后端 {@code PluginDispatcher} 按「当前用户权限」校验。
 * 前端这层只是开发期约束，别把它当防线。
 *
 * <p>因此「有前端入口」不等于「该进清单」：没有 PF4J 后端、靠宿主透传的插件
 * 一律放 {@link HOST_PASSTHROUGH_PLUGIN_CODES}，加进来会让它的全部接口立刻报错。
 */
export const PLUGIN_ROUTE_CODES = Object.freeze([
  'activity', 'aicode', 'cms', 'crm', 'erp', 'finance', 'geo', 'im',
  'logging', 'mall-live', 'mes', 'oa', 'orderfood', 'pm', 'supplier', 'webpos'
])

/**
 * 有前端入口、但<b>不自建 PF4J 后端</b>的插件：接口由宿主 BFF 直接路由
 * （形如 {@code /web/aigateway/**}），前端一律用宿主 {@code callCoreApi} 透传原始路径。
 *
 * <p>这些插件<b>不能</b>进 {@link PLUGIN_ROUTE_CODES}：一旦进去，
 * {@code buildCoreApiPath()} 会以「插件 API 必须通过插件专属通道调用」为由拒掉它们的
 * 每一个请求 —— 而它们的后端并没有挂载在 {@code /web/plugin/{code}/**} 上，于是全线 404。
 *
 * <p>它们的旧地址重定向不走这份清单，由 {@code router/index.js} 的
 * {@code legacyModuleRedirect()} 单独承接（见该文件 2026-09-18 注释）。
 *
 * <p>放进这里只表示「已登记、不要被误加进清单」，不产生重定向、也不改路由。
 */
export const HOST_PASSTHROUGH_PLUGIN_CODES = Object.freeze([
  'aigateway', 'rider', 'unilogin',
  // 支付中心已回归 payment 独立服务（/payment、/web/payment）。保留编码，避免被误加进
  // PLUGIN_ROUTE_CODES 后把 /web/payment/** 拦成插件通道。
  'unipay',
  // 返利结算 / 分销推广 / 积分商城：后端是 mall 主服务的原生 Controller
  // （/mall/rebate/**、/mall/distribution/**、/mall/points-tool/**），经 BFF 商城模块
  // 直通通道 /web/mall/module/{module}/** 访问，没有 /web/plugin/{code}/** 路由。
  // 与 rider 同构，绝不能进 PLUGIN_ROUTE_CODES。
  'mall-rebate', 'mall-distribution', 'mall-points'
])

/** 已下线或更名的插件编码，只用于兼容历史地址。 */
export const LEGACY_PLUGIN_ROUTE_ALIASES = Object.freeze({
  aicloud: 'aicode'
})

export const PLUGIN_ROUTE_CODE_SET = new Set([
  ...PLUGIN_ROUTE_CODES,
  ...Object.keys(LEGACY_PLUGIN_ROUTE_ALIASES)
])

function resolvePluginRouteCode(code) {
  return LEGACY_PLUGIN_ROUTE_ALIASES[code] || code
}

/** 将插件历史直达地址规范到动态插件命名空间。 */
export function normalizeLegacyPluginPath(path) {
  if (!path || typeof path !== 'string') return ''
  const normalized = path.startsWith('/') ? path : `/${path}`
  const pluginMatch = normalized.match(/^\/plugin\/([^/]+)(\/.*)?$/)
  if (pluginMatch) {
    const code = pluginMatch[1]
    if (!PLUGIN_ROUTE_CODE_SET.has(code)) return normalized
    return `/plugin/${resolvePluginRouteCode(code)}${pluginMatch[2] || ''}`
  }

  const match = normalized.match(/^\/([^/]+)(\/.*)?$/)
  if (!match || !PLUGIN_ROUTE_CODE_SET.has(match[1])) return normalized
  return `/plugin/${resolvePluginRouteCode(match[1])}${match[2] || ''}`
}

/** 为浏览器书签、旧菜单和历史分享链接注册宿主兼容重定向。 */
export function createLegacyPluginRedirects() {
  const legacyRootRedirects = PLUGIN_ROUTE_CODES.flatMap((code) => ([
    {
      path: `/${code}`,
      redirect: `/plugin/${code}`,
      meta: { pluginCode: code, pluginRoute: true }
    },
    {
      path: `/${code}/:pathMatch(.*)*`,
      redirect: (to) => {
        const suffix = to.params?.pathMatch
        const nestedPath = Array.isArray(suffix) ? suffix.join('/') : String(suffix || '')
        return `/plugin/${code}${nestedPath ? `/${nestedPath}` : ''}`
      },
      meta: { pluginCode: code, pluginRoute: true }
    }
  ]))

  const aliasRedirects = Object.entries(LEGACY_PLUGIN_ROUTE_ALIASES).flatMap(([alias, code]) => {
    const nestedRedirect = (to) => {
      const suffix = to.params?.pathMatch
      const nestedPath = Array.isArray(suffix) ? suffix.join('/') : String(suffix || '')
      return `/plugin/${code}${nestedPath ? `/${nestedPath}` : ''}`
    }
    return [
      {
        path: `/${alias}`,
        redirect: `/plugin/${code}`,
        meta: { pluginCode: code, pluginRoute: true }
      },
      {
        path: `/${alias}/:pathMatch(.*)*`,
        redirect: nestedRedirect,
        meta: { pluginCode: code, pluginRoute: true }
      },
      {
        path: `/plugin/${alias}`,
        redirect: `/plugin/${code}`,
        meta: { pluginCode: code, pluginRoute: true }
      },
      {
        path: `/plugin/${alias}/:pathMatch(.*)*`,
        redirect: nestedRedirect,
        meta: { pluginCode: code, pluginRoute: true }
      }
    ]
  })

  return [...legacyRootRedirects, ...aliasRedirects]
}
