import { ALL_CATE_ID, collectNavItems, getMenuCateId, getMenuDisplayPos, isMenuGroupRoot } from './menuDisplay'

export const FAVORITES_CATE_ID = -2
const flag = value => value === true || value === 1 || value === '1' || value === 'true'
const normalizeCode = value => {
  const code = String(value || '').trim().toLowerCase()
  return code === 'aicloud' ? 'aicode' : code
}

export function menuPluginCode(menu) {
  const declared = menu?.plugin_code || menu?.pluginCode
  if (declared) return normalizeCode(declared)
  const path = String(menu?.paths || menu?.path || '')
  if (/^\/lowcode(?:\/|$)/.test(path)) return 'lowcode'
  return normalizeCode(path.match(/^\/plugin\/([^/?#]+)/)?.[1])
}

/** 与插件中心分类 Tab 同一口径：没有有效分类 id 一律算未分类，不回退到菜单上的 cate_id。 */
export function pluginCategoryId(row) {
  const id = Number(row?.cateId ?? row?.cate_id)
  return Number.isFinite(id) && id > 0 ? Math.trunc(id) : 0
}

// 权限树决定用户可进入的应用；目录只补充安装状态和分类，不贡献额外权限。
export function buildApplicationItems(tree = [], catalog = []) {
  const products = new Map((catalog || []).map(product => [normalizeCode(product.code), product]))
  const seen = new Set()
  return collectNavItems(tree).flatMap(menu => {
    if (getMenuDisplayPos(menu) !== 'plugin') return []
    const code = menuPluginCode(menu)
    if (!code || seen.has(code)) return []
    const product = products.get(code)
    const builtIn = code === 'lowcode' && /^\/lowcode(?:\/|$)/.test(String(menu.paths || menu.path || ''))
    if (!builtIn && (!product || !flag(product.installed) || !flag(product.enabled))) return []
    seen.add(code)
    return [{
      id: `plugin:${code}`, code, menu,
      name: product?.name || menu.name || code,
      icon: menu.icon,
      cateId: product ? pluginCategoryId(product) : getMenuCateId(menu),
      unavailable: product?.healthy === false || ['DOWN', 'UNAVAILABLE', 'FAILED'].includes(String(product?.healthStatus || '').toUpperCase())
    }]
  })
}

/**
 * 插件中心「已安装」口径：按目录列出已安装项（含无菜单权限的项）。
 * 右上角「插件应用」入口不要用这份清单，应走 buildApplicationItems（只列可进功能的应用）。
 */
export function buildInstalledApplicationItems(tree = [], catalog = []) {
  const menus = new Map()
  for (const menu of collectNavItems(tree)) {
    if (getMenuDisplayPos(menu) !== 'plugin') continue
    const code = menuPluginCode(menu)
    if (code && !menus.has(code)) menus.set(code, menu)
  }
  const seen = new Set()
  return (catalog || []).flatMap(product => {
    const code = normalizeCode(product?.code)
    if (!code || seen.has(code) || !flag(product?.installed)) return []
    seen.add(code)
    const menu = menus.get(code) || null
    const stopped = !flag(product?.enabled)
    const unhealthy = product?.healthy === false || ['DOWN', 'UNAVAILABLE', 'FAILED'].includes(String(product?.healthStatus || '').toUpperCase())
    return [{
      id: `plugin:${code}`,
      code,
      menu,
      name: product?.name || menu?.name || code,
      icon: menu?.icon,
      cateId: pluginCategoryId(product),
      entryPath: product?.entryPath || product?.entry_path || '',
      unavailable: stopped || unhealthy,
      unavailableReason: stopped ? 'disabled' : unhealthy ? 'health' : ''
    }]
  })
}

export function groupApplicationItems(items = [], categories = [], favoriteCodes = [], { includeEmpty = false } = {}) {
  const groups = [{ id: ALL_CATE_ID, name: '全部应用', items }]
  const favorites = items.filter(item => favoriteCodes.includes(item.code))
  if (favorites.length) groups.unshift({ id: FAVORITES_CATE_ID, name: '常用', items: favorites })
  const known = new Set()
  for (const category of categories || []) {
    const id = Number(category.id)
    if (!(id > 0) || known.has(id)) continue
    known.add(id)
    const members = items.filter(item => Number(item.cateId) === id)
    if (members.length || includeEmpty) groups.push({ id, name: category.name, items: members })
  }
  const uncategorized = items.filter(item => !known.has(Number(item.cateId)))
  if (uncategorized.length) groups.push({ id: 0, name: '未分类', items: uncategorized })
  return groups
}

export function filterApplicationItems(items = [], query = '') {
  const term = query.trim().toLocaleLowerCase()
  return term ? items.filter(item => `${item.name} ${item.code}`.toLocaleLowerCase().includes(term)) : items
}

// 管理视图保留隐藏/停用节点供维护；不会借由过滤改变数据库的父子关系。
export function selectManagedMenus(tree = [], view = 'core', { cateId = null, query = '', type = '' } = {}) {
  const roots = (tree || []).filter(menu => {
    const position = getMenuDisplayPos(menu)
    if (view === 'hidden') return position === 'hidden' || isMenuGroupRoot(menu)
    if (isMenuGroupRoot(menu) || position === 'hidden') return false
    if (view === 'applications') return position === 'plugin' && (cateId == null || getMenuCateId(menu) === Number(cateId))
    return position === 'left'
  })
  const term = query.trim().toLocaleLowerCase()
  if (!term && !type) return roots
  const filter = nodes => nodes.flatMap(node => {
    const children = filter(node.children || [])
    const matches = (!term || String(node.name || '').toLocaleLowerCase().includes(term)) && (!type || node.type === type)
    return matches ? [node] : children.length ? [{ ...node, children }] : []
  })
  return filter(roots)
}
