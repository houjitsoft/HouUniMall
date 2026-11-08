/**
 * 应用部署基路径（子路径部署支持）
 *
 * 背景：admin 构建产物只有一份（vite base 固定为 '/'），但同时服务两个站点：
 *   - unimall.houjit.com    —— 根路径部署（/login、/dashboard）
 *   - unimall.houjit.com'  —— 子路径部署（/admin/login、/admin/dashboard）
 * 前端路由与少量 window.location 跳转此前一律按根路径计算，导致在 unimall 下
 * 登录后跳到 /dashboard（丢掉 /admin 前缀），刷新时被主站 Nuxt 首页兜底。
 *
 * 约定：运行时根据当前 pathname 判断是否为 /admin 子路径部署，
 * 是则基路径为 '/admin/'，否则为 '/'.
 */

const ADMIN_PREFIX = '/admin'

/**
 * 获取应用部署基路径，返回值必定以 '/' 结尾（'/' 或 '/admin/'）
 * @returns {string}
 */
export function getAppBase () {
  if (typeof window === 'undefined') return '/'
  const path = window.location.pathname || '/'
  if (path === ADMIN_PREFIX || path.startsWith(ADMIN_PREFIX + '/')) {
    return ADMIN_PREFIX + '/'
  }
  return '/'
}

/**
 * 把应用内绝对路径（如 '/login'）拼上部署基路径，得到可直接用于
 * window.location 的真实 URL（'/' 或 '/admin/login'）
 * @param {string} path 应用内绝对路径，需以 '/' 开头
 * @returns {string}
 */
export function appUrl (path) {
  const p = String(path || '/')
  const base = getAppBase()
  if (base === '/') return p.startsWith('/') ? p : '/' + p
  return base + p.replace(/^\//, '')
}

/**
 * 判断当前浏览器地址是否等于应用内某个路径
 * 用于替代 `window.location.pathname !== '/login'` 这类在子路径下恒为真的判断
 * @param {string} path 应用内绝对路径，需以 '/' 开头
 * @returns {boolean}
 */
export function isAppPath (path) {
  if (typeof window === 'undefined') return false
  return (window.location.pathname || '/') === appUrl(path)
}

/**
 * 真实浏览器 pathname → 应用内路径（去掉部署基路径）
 * 用于把 `?redirect=` 参数、window.location.pathname 还原成 router 能识别的路径，
 * 否则子路径部署下 '/admin/order/list' 会被 router 当成未注册路由而落到 404
 * @param {string} pathname 真实 pathname，如 '/admin/order/list' 或 '/order/list'
 * @returns {string} 应用内绝对路径，如 '/order/list'
 */
export function toAppPath (pathname) {
  const p = String(pathname || '/')
  const base = getAppBase()
  if (base !== '/' && (p === base.slice(0, -1) || p.startsWith(base))) {
    return '/' + p.slice(base.length)
  }
  return p.startsWith('/') ? p : '/' + p
}

export default getAppBase
