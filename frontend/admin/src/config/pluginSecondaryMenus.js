/**
 * 插件二级菜单声明表（宿主侧兜底）。
 *
 * 背景：插件的子页面路由由插件前端产物（ESM routes）动态注册，但其 menus 声明
 * 目前只有一级入口（子菜单声明链路 manifest → 后端 sync → 服务端菜单树 尚未打通），
 * 导致从顶栏宫格选中插件后，二级侧栏（TertiaryMenu）没有任何该插件的菜单可显示。
 *
 * 这里按插件编码声明子菜单（宿主侧兜底），在 app store 组装 dynamicMenus 时统一
 * 挂到对应 `/plugin/<code>` 节点的 children 上。子页面路由本就存在，这里只是给
 * 二级栏提供菜单入口，不涉及权限放行（子路径经 isMenuPathPrefix 前缀放行）。
 *
 * 新插件需要二级菜单时在此补一段声明即可；插件 menus 声明打通 children 后可移除。
 */
export const PLUGIN_SECONDARY_MENUS = Object.freeze({
  webpos: [
    { path: '/plugin/webpos/checkout', title: '收银开单', icon: 'ShoppingCart' },
    { path: '/plugin/webpos/orders', title: '订单管理', icon: 'Tickets' },
    { path: '/plugin/webpos/members', title: '会员管理', icon: 'User' },
    { path: '/plugin/webpos/staff', title: '店员管理', icon: 'Avatar' },
    { path: '/plugin/webpos/settings', title: '门店设置', icon: 'Setting' }
  ]
})

/** 子菜单 id 段：官方菜单（1xxxx/2xxxx/6xxxx）与插件运行时菜单（120000+）之外 */
const SECONDARY_MENU_ID_BASE = 230000

/** 稳定 hash：同一插件编码每次加载得到同一段 id，避免折叠状态等按 id 的逻辑跳变 */
function codeHash(code) {
  let h = 0
  const text = String(code || '')
  for (let i = 0; i < text.length; i += 1) {
    h = (h * 31 + text.charCodeAt(i)) % 9000
  }
  return h
}

function buildSecondaryNodes(code, parentPerms, items) {
  if (!Array.isArray(items) || !items.length) return []
  const offset = codeHash(code) * 10
  return items.map((item, index) => ({
    id: SECONDARY_MENU_ID_BASE + offset + index,
    pid: null,
    type: 'M',
    name: item.title || item.name || code,
    icon: item.icon || 'Document',
    sort: index + 1,
    perms: parentPerms || `plugin:${code}`,
    paths: item.path,
    component: '',
    is_show: 1,
    is_disable: 0,
    children: [],
    pluginCode: code,
    pluginRoute: true
  }))
}

/**
 * 为菜单树中 `/plugin/<code>` 一级节点挂上声明的二级菜单（就地修改）。
 *
 * 匹配任意层级下 paths 精确等于 `/plugin/<code>` 的节点（插件节点可能挂在
 * 顶栏分组根之下，pid 不再是 0），只在节点尚无 children 时挂载，幂等可重入。
 *
 * @param {Array} menuTree 用户菜单树（可修改副本）
 * @returns {Array} 同一棵树
 */
export function attachPluginSecondaryMenus(menuTree) {
  if (!Array.isArray(menuTree)) return menuTree
  const walk = (nodes) => {
    for (const node of nodes) {
      if (!node) continue
      const path = String(node.paths || node.path || '')
      const match = path.match(/^\/plugin\/([^/]+)$/)
      if (match) {
        const code = match[1]
        const declared = PLUGIN_SECONDARY_MENUS[code]
        const hasChildren = Array.isArray(node.children) && node.children.length > 0
        if (Array.isArray(declared) && declared.length && !hasChildren) {
          node.children = buildSecondaryNodes(code, node.perms, declared)
          node.children.forEach((child) => {
            child.pid = node.id
          })
        }
      }
      if (Array.isArray(node.children) && node.children.length) walk(node.children)
    }
  }
  walk(menuTree)
  return menuTree
}

export default {
  PLUGIN_SECONDARY_MENUS,
  attachPluginSecondaryMenus
}
