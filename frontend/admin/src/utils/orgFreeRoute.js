/**
 * 无「组织作用域」的模块路由判定（供 Layout 顶栏组织切换器使用）。
 *
 * 黑名单策略：默认显示组织切换；仅对外经营 / 内容 / 平台工具类模块隐藏。
 * 以 '-' 结尾的前缀按「族」匹配（如 /plugin/order- → /plugin/order-pintuan）。
 */

export const ORG_FREE_ROUTE_PREFIXES = [
  '/mall',
  '/goods',
  '/order',
  '/payment',
  '/plugin/unipay',
  '/product',
  '/supplier',
  '/plugin/supplier',
  '/rider',
  '/plugin/orderfood',
  '/plugin/webpos',
  '/plugin/order-',
  '/plugin/goods-',
  '/plugin/mall-',
  '/plugin/national-subsidy',
  '/plugin/cms',
  '/plugin/activity',
  '/plugin/im',
  '/im',
  '/aicode',
  '/plugin/aicode',
  '/ai',
  '/aigateway',
  '/lowcode',
  '/dcenter',
  '/workflow',
  '/dataset',
  '/experiment',
  '/evaluator',
  '/modelconfig',
  '/observability',
  '/prompt',
  '/dashboard',
  '/error',
  '/compat',
  '/platform',
  '/plugin/demo-coupon',
  '/plugin/logging'
]

/**
 * @param {string} path 当前路由 path
 * @param {string[]} [prefixes]
 * @returns {boolean}
 */
export function isOrgFreeRoute(path, prefixes = ORG_FREE_ROUTE_PREFIXES) {
  const p = path || ''
  if (!p || p === '/') return true
  return prefixes.some((prefix) => {
    if (p === prefix || p.startsWith(`${prefix}/`)) return true
    if (prefix.endsWith('-') && p.startsWith(prefix)) return true
    return false
  })
}
