/** 一级菜单落点的唯一装配规则。分类不作为菜单父节点。 */
export const DISPLAY_POS = { LEFT: 'left', PLUGIN: 'plugin', HIDDEN: 'hidden' }
export const UNCATEGORIZED_CATE_ID = 0
export const ALL_CATE_ID = -1
export const FIXED_CORE_MENU_IDS = [19, 1, 1009, 15, 4, 18]
export const isMenuGroupRoot = menu => menu?.params === 'host:menu-group'

const isVisibleNode = node => !!node && node.type !== 'A' &&
  (node.is_show == null || Number(node.is_show) === 1) && Number(node.is_disable) !== 1
export const visibleChildren = menu => (menu?.children || []).filter(isVisibleNode)

export const getMenuDisplayPos = menu => {
  const raw = String(menu?.display_pos ?? menu?.displayPos ?? '').trim().toLowerCase()
  if (Object.values(DISPLAY_POS).includes(raw)) return raw
  if (isMenuGroupRoot(menu)) return DISPLAY_POS.HIDDEN
  // 仅兼容缺失字段的历史缓存；不再按菜单编号猜测业务归属。
  return menu?.plugin_code || menu?.pluginCode || menu?.pluginRoute ||
    String(menu?.paths || menu?.path || '').startsWith('/plugin/') ? DISPLAY_POS.PLUGIN : DISPLAY_POS.LEFT
}
export const isLeftMenu = menu => getMenuDisplayPos(menu) === DISPLAY_POS.LEFT
export const isPluginMenu = menu => getMenuDisplayPos(menu) === DISPLAY_POS.PLUGIN
export const isHiddenMenu = menu => getMenuDisplayPos(menu) === DISPLAY_POS.HIDDEN
export const getMenuCateId = menu => {
  const id = Number(menu?.cate_id ?? menu?.cateId)
  return Number.isFinite(id) && id > 0 ? Math.trunc(id) : UNCATEGORIZED_CATE_ID
}

export function collectNavItems(tree = []) {
  return (tree || []).filter(menu => isVisibleNode(menu) &&
    (menu.pid == null || Number(menu.pid) === 0) && !isMenuGroupRoot(menu) && !isHiddenMenu(menu))
    .map(menu => ({ ...menu, display_pos: getMenuDisplayPos(menu) }))
}
export const pickLeftNavItems = (tree = []) => collectNavItems(tree).filter(isLeftMenu)
export const pickPluginNavItems = (tree = []) => collectNavItems(tree).filter(isPluginMenu)

/**
 * 判断路径是否落在某个菜单的子树内（自身 paths 或其任意后代命中）。
 * @param {object} menu
 * @param {string} path 当前路由路径
 * @returns {boolean}
 */
export function pathWithinMenu(menu, path) {
  if (!menu || !path) return false
  const norm = (p) => String(p || '').trim().replace(/\/+$/, '') || ''
  const target = norm(path)
  if (!target) return false
  const walk = (node) => {
    const raw = norm(node?.paths || node?.path)
    if (raw && (target === raw || target.startsWith(raw + '/'))) return true
    return visibleChildren(node).some(walk)
  }
  return walk(menu)
}

/** 在菜单树中按 id 查找节点 */
export function findMenuById(tree = [], id) {
  const wanted = String(id)
  if (!wanted) return null
  const stack = [...(tree || [])]
  while (stack.length) {
    const node = stack.pop()
    if (!node) continue
    if (String(node.id) === wanted) return node
    if (node.children?.length) stack.push(...node.children)
  }
  return null
}
