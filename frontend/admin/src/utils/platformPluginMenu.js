/**
 * Keep the plugin center and marketplace under the platform section.
 *
 * The permission service is the source of truth for which menu nodes a user
 * may see. During the migration it can still return the old mall paths, so
 * this normalizer moves only those returned nodes; it never invents access to
 * a plugin page that was not already granted.
 */

export const PLATFORM_PLUGIN_ROOT_PATH = '/platform'
export const PLATFORM_PLUGIN_CENTER_PATH = '/platform/plugins'
export const PLATFORM_PLUGIN_MARKET_PATH = '/platform/plugins/market'

const LEGACY_PLUGIN_PATHS = new Map([
  ['/addon', PLATFORM_PLUGIN_CENTER_PATH],
  ['/mall/addon', PLATFORM_PLUGIN_CENTER_PATH],
  ['/mall/addons', PLATFORM_PLUGIN_CENTER_PATH],
  ['/mall/market', PLATFORM_PLUGIN_MARKET_PATH]
])

// 旧版顶级「插件中心」入口（/addons、/addon 空根）直接改造为平台插件菜单根，
// 避免与迁移后的 /platform 根并存出现两个「插件中心」一级菜单。
const LEGACY_ROOT_PATHS = new Set(['/addons', '/addon'])

const normalizePath = (path) => {
  if (!path || typeof path !== 'string') return ''
  const value = path.trim()
  if (!value) return ''
  const withSlash = value.startsWith('/') ? value : `/${value}`
  return withSlash.length > 1 ? withSlash.replace(/\/+$/, '') : withSlash
}

export function normalizePlatformPluginPath(path) {
  const normalized = normalizePath(path)
  return LEGACY_PLUGIN_PATHS.get(normalized) || normalized
}

const isPlatformPluginEntry = (path) => (
  path === PLATFORM_PLUGIN_CENTER_PATH || path === PLATFORM_PLUGIN_MARKET_PATH
)

const buildPlatformRoot = () => ({
  id: 119000,
  pid: 0,
  type: 'M',
  name: '\u63d2\u4ef6\u4e2d\u5fc3',
  icon: 'Grid',
  sort: 9800,
  perms: 'shell:platform',
  paths: PLATFORM_PLUGIN_ROOT_PATH,
  component: '',
  selected: '',
  params: '',
  is_cache: 0,
  is_show: 1,
  is_disable: 0,
  children: []
})

/**
 * Move legacy plugin center/market entries to the platform menu root.
 * The input tree is mutated and returned so it can be used during store
 * rebuilds without changing the existing menu-store contract.
 */
export function normalizePlatformPluginMenus(menuTree) {
  if (!Array.isArray(menuTree)) return menuTree

  const moved = []
  let platformRoot = null

  // 只有「无子树的叶子入口」才参与「旧路径 → 平台插件」迁移。
  // 反例：库里 id=11024「插件中心」paths=/mall/addons，挂在商城管理下，
  // 但带 7 个子项 /mall/admin/application/*（文章/素材/客服/消息/公告/预存款/数据采集），
  // 是商城应用功能模块而非跳板入口。若一并命中 /mall/addons 映射，整棵子树会被搬到
  // 一级「插件中心」下，表现为「侧栏插件中心里混进一批商城菜单」——与库里归属完全对不上。
  // 同理，纯跳板（children 为空）才搬；带子树的节点一律留在原父菜单下。
  const isLeafEntry = (node) => !(Array.isArray(node?.children) && node.children.length > 0)

  const visit = (nodes) => {
    const kept = []
    for (const original of nodes || []) {
      if (!original || typeof original !== 'object') continue
      const node = { ...original }
      const rawPath = normalizePath(node.paths || node.path)
      // 旧顶级 /addons 空根：升级为 /platform 插件中心根，保留其 id 以维持权限展示
      if (Number(node.pid) === 0 && LEGACY_ROOT_PATHS.has(rawPath) && isLeafEntry(original)) {
        node.paths = PLATFORM_PLUGIN_ROOT_PATH
        node.name = '\u63d2\u4ef6\u4e2d\u5fc3'
        platformRoot = platformRoot || node
        node.children = visit(node.children || [])
        kept.push(node)
        continue
      }
      // Business containers own their routes; only leaf jump entries use legacy aliases.
      const path = isLeafEntry(original) ? normalizePlatformPluginPath(rawPath) : rawPath

      if (path === PLATFORM_PLUGIN_ROOT_PATH) {
        platformRoot = platformRoot || node
        node.paths = PLATFORM_PLUGIN_ROOT_PATH
        node.children = visit(node.children)
        kept.push(node)
        continue
      }

      if (isLeafEntry(original) && isPlatformPluginEntry(path)) {
        node.paths = path
        node.children = visit(node.children)
        moved.push(node)
        continue
      }

      node.paths = path
      node.children = visit(node.children)
      kept.push(node)
    }
    return kept
  }

  const normalizedTree = visit(menuTree)
  if (!moved.length) {
    menuTree.splice(0, menuTree.length, ...normalizedTree)
    return menuTree
  }

  if (!platformRoot) {
    platformRoot = buildPlatformRoot()
    normalizedTree.push(platformRoot)
  }

  const children = Array.isArray(platformRoot.children) ? platformRoot.children : []
  const existingPaths = new Set(children.map((child) => normalizePlatformPluginPath(child?.paths || child?.path)))
  for (const node of moved) {
    if (existingPaths.has(node.paths)) continue
    node.pid = platformRoot.id
    node.paths = normalizePlatformPluginPath(node.paths)
    children.push(node)
    existingPaths.add(node.paths)
  }
  // 固定展示顺序：插件中心 → 插件市场 → 其余按 sort
  const entryOrder = (path) => (path === PLATFORM_PLUGIN_CENTER_PATH ? 0 : path === PLATFORM_PLUGIN_MARKET_PATH ? 1 : 2)
  children.sort((a, b) => {
    const diff = entryOrder(a?.paths) - entryOrder(b?.paths)
    return diff !== 0 ? diff : (Number(a?.sort) || 0) - (Number(b?.sort) || 0)
  })
  platformRoot.children = children

  menuTree.splice(0, menuTree.length, ...normalizedTree)
  return menuTree
}

export default {
  PLATFORM_PLUGIN_ROOT_PATH,
  PLATFORM_PLUGIN_CENTER_PATH,
  PLATFORM_PLUGIN_MARKET_PATH,
  normalizePlatformPluginPath,
  normalizePlatformPluginMenus
}
