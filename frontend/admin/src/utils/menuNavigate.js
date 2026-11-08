/**
 * 侧栏菜单跳转：目录(M)无对应路由时落到第一个子菜单路径
 */

import {
  OA_WORKBENCH_MENU_ID,
  resolveWorkbenchAppPath
} from '@/utils/oaWorkbenchAppPath.js'
import {
  PLUGIN_ROUTE_CODES,
  normalizeLegacyPluginPath
} from '@/config/pluginRouteConfig.js'

export { PLUGIN_ROUTE_CODES }

/** 商城侧栏「工作台」菜单 id（叶子入口，直达数据概览） */
export const MALL_WORKBENCH_MENU_ID = 60001

/** 商城工作台旧子菜单「工作台概览」id（应隐藏，不再展开二级） */
export const MALL_WORKBENCH_CHILD_MENU_ID = 60101

/** 插件中心历史菜单路径，统一指向当前宿主页面。 */
export const MALL_PLUGIN_CENTER_PATH_ALIASES = new Map([
  ['/addons', '/platform/plugins'],
  ['/addon', '/platform/plugins'],
  ['/mall/addon', '/platform/plugins'],
  ['/mall/addons', '/platform/plugins'],
  ['/mall/market', '/platform/plugins/market'],
  ['/mall/market/detail', '/platform/plugins/market/detail'],
  ['/mall/market/checkout', '/platform/plugins/market/checkout'],
  ['/mall/market/licenses', '/platform/plugins/licenses'],
  ['/mall/market/orders', '/platform/plugins/orders']
])

/** 应用页面统一使用 /plugin/{code}/ 命名空间，并兼容历史路径。 */
const normalizePath = (path) => {
  if (!path || typeof path !== 'string') return ''
  const normalized = path.startsWith('/') ? path : `/${path}`
  return normalizeLegacyPluginPath(normalized)
}

export function resolveMallPluginCenterPath(path) {
  const normalized = normalizePath(path)
  return MALL_PLUGIN_CENTER_PATH_ALIASES.get(normalized) || normalized
}

/** 过宽占位路径（如工作台快捷菜单 /oa/）不能作为前缀匹配整棵 OA 子树 */
const isAmbiguousMenuPath = (path) => {
  const p = normalizePath(path)
  return p === '/plugin/oa' || p === '/plugin/oa/'
}

/**
 * 路由是否落在菜单 paths 下（排除 /oa/ 误匹配 /oa/attendance/*）
 */
export function isMenuPathPrefix(current, target) {
  const c = normalizePath(current)
  const t = normalizePath(target)
  if (!c || !t) return false
  if (c === t || c === `${t}/`) return true
  if (isAmbiguousMenuPath(t)) return false
  return c.startsWith(`${t}/`)
}

/**
 * 在菜单树中按 paths 查找节点
 * @param {Array} menus
 * @param {string} path
 * @returns {object|null}
 */
export function findMenuByPath(menus, path) {
  const target = normalizePath(path)
  if (!target || !menus?.length) return null
  let found = null
  const walk = (nodes) => {
    for (const node of nodes) {
      const p = normalizePath(node.paths || node.path)
      if (p && isMenuPathPrefix(target, p)) {
        if (!found || p.length > normalizePath(found.paths || found.path).length) {
          found = node
        }
      }
      if (node.children?.length) walk(node.children)
    }
  }
  walk(menus)
  return found
}

/** 库表 paths 纠错：考勤列表不应落在合同流程 URL */
export function resolveMenuItemPath(item) {
  if (!item) return '/'
  const perms = (item.perms || '').trim()
  if (perms === 'shell:oa:attendance:lists' || item.name === '考勤列表') {
    return '/plugin/oa/attendance/lists'
  }
  const comp = (item.component || '').trim().replace(/^\//, '')
  if (comp.includes('attendance/AttendanceList')) {
    return '/plugin/oa/attendance/lists'
  }
  const p = item.paths || item.path
  if (!p || typeof p !== 'string') return '/'
  const normalized = normalizePath(p)
  const pluginCenterPath = resolveMallPluginCenterPath(normalized)
  if (pluginCenterPath !== normalized) {
    return pluginCenterPath
  }
  // 日志管理：库表曾误配为 security 路径，统一落到 logging 审计页
  if (normalized === '/setting/security/login' || perms === 'shell:setting:security:login') {
    return '/setting/log/login'
  }
  if (normalized === '/setting/security/operation' || perms === 'shell:setting:security:operation') {
    return '/setting/log/operation'
  }
  if (normalized === '/setting/log' || normalized === '/setting/log/') {
    return '/setting/log/analysis'
  }
  // 误把 BFF API 路径配成菜单 paths 时纠正
  if (normalized.startsWith('/web/oa/attendance/lists')) {
    return '/plugin/oa/attendance/lists'
  }
  // 商城侧栏「工作台 / 商城概览」已并入核心仪表盘
  if (
    Number(item.id) === MALL_WORKBENCH_MENU_ID ||
    Number(item.id) === MALL_WORKBENCH_CHILD_MENU_ID ||
    perms === 'mall:workbench' ||
    perms === 'mall:workbench:index' ||
    normalized === '/mall/admin/workbench' ||
    normalized === '/mall/dashboard/overview'
  ) {
    return '/dashboard'
  }
  if (perms === 'shell:oa:supplier' || item.name === '服务商管理' || item.name === '服务商管理2') {
    return '/plugin/oa/supplier'
  }
  if (
    (normalized === '/supplier' || normalized.startsWith('/supplier/'))
    && (perms === 'shell:oa:supplier' || item.name === '服务商管理' || item.name === '服务商管理2')
  ) {
    return '/plugin/oa/supplier'
  }
  return normalized
}

/**
 * 查找所有 paths 精确匹配的菜单（同 path 可能有多条脏数据）
 */
export function findAllMenusByPath(menus, path) {
  const target = normalizePath(path)
  if (!target || !menus?.length) return []
  const found = []
  const walk = (nodes) => {
    for (const node of nodes) {
      const p = normalizePath(node.paths || node.path)
      if (p && p === target) {
        found.push(node)
      }
      if (node.children?.length) walk(node.children)
    }
  }
  walk(menus)
  return found
}

/**
 * @param {object} node
 * @returns {string}
 */
export function firstLeafMenuPath(node) {
  if (!node) return ''
  let cur = node
  while (cur?.children?.length) {
    const children = filterVisibleMenuItems(cur.children)
    if (!children.length) break
    cur = children[0]
  }
  return normalizePath(cur?.paths || cur?.path)
}

/**
 * @param {Array} menus 完整菜单树
 * @param {string} index 侧栏 index（一般为 paths）
 * @returns {string}
 */
export function resolveMenuNavigatePath(menus, index) {
  if (index == null || index === '') return ''
  const raw = String(index)
  if (raw === String(OA_WORKBENCH_MENU_ID)) return '/plugin/oa/apps'
  if (!raw.startsWith('/')) return ''
  const normalized = normalizePath(raw)
  if (OA_WORKBENCH_PATHS.includes(normalized)) return '/plugin/oa/apps'
  const exactMatches = findAllMenusByPath(menus, normalized)
  const attendanceMenu = exactMatches.find(
    (n) => (n.perms || '').includes('attendance:lists') || n.name === '考勤列表'
  )
  if (attendanceMenu) {
    return resolveMenuItemPath(attendanceMenu)
  }
  const node = findMenuByPath(menus, normalized)
  if (!node) return resolveMallPluginCenterPath(normalized)
  if (isOaWorkbenchMenu(node)) return '/plugin/oa/apps'
  if (node.children?.length) {
    const leaf = firstLeafMenuPath(node)
    return leaf ? resolveMenuItemPath({ ...node, paths: leaf, path: leaf }) : resolveMenuItemPath(node)
  }
  return resolveMenuItemPath(node)
}

/**
 * 按 perms 前缀过滤侧栏子节点，避免库表 pid 错乱时把「工作台」快捷项挂到「考勤」下
 */
function isSalarySidebarChild(parentPerms, child) {
  const cp = (child.perms || '').trim()
  const path = normalizePath(child.paths || child.path)
  if (parentPerms === 'shell:oa:salary') {
    if (path.startsWith('/plugin/oa/salary/') || path.startsWith('/plugin/oa/hr/salary')) {
      return true
    }
    if (cp === 'oa:hr:salary-rules' || cp.startsWith('oa:salary:')) {
      return true
    }
  }
  if (!cp) return false
  return cp === parentPerms || cp.startsWith(`${parentPerms}:`)
}

function filterSidebarChildren(parent, children) {
  const parentPerms = (parent.perms || '').trim()
  if (!parentPerms || parentPerms === 'shell:oa' || parentPerms === 'shell:oa:apps') {
    return children
  }
  if (!parentPerms.startsWith('shell:oa:')) {
    return children
  }
  return children.filter((ch) => isSalarySidebarChild(parentPerms, ch))
}

/**
 * 侧栏菜单树清洗：perms 过滤 + OA/商城工作台、考勤列表强制为叶子
 * @param {Array} items
 * @returns {Array}
 */
/**
 * 是否为商城侧栏「工作台」叶子入口（不展开子菜单，直达概览）
 * @param {object} item
 * @returns {boolean}
 */
export function isMallWorkbenchMenu(item) {
  if (!item) return false
  if (Number(item.id) === MALL_WORKBENCH_MENU_ID) return true
  if (Number(item.id) === MALL_WORKBENCH_CHILD_MENU_ID) return true
  const perms = String(item.perms || '').trim()
  if (perms === 'mall:workbench' || perms.startsWith('mall:workbench:')) return true
  const p = normalizePath(item.paths || item.path)
  if (p === '/mall/admin/workbench') return true
  return false
}

export function sanitizeSidebarMenuTree(items) {
  if (!items?.length) return []
  return filterVisibleMenuItems(items).map((item) => {
    const paths = normalizePath(item.paths || item.path)
    const isWorkbenchLeaf = isOaWorkbenchMenu(item) || isMallWorkbenchMenu(item)
    const isAttendanceListLeaf =
      paths === '/plugin/oa/attendance/lists' ||
      (item.perms || '').trim() === 'shell:oa:attendance:lists'

    if (isWorkbenchLeaf || isAttendanceListLeaf) {
      const { children, ...rest } = item
      return rest
    }

    let children = item.children
    if (children?.length) {
      children = filterSidebarChildren(item, children)
      children = sanitizeSidebarMenuTree(children)
    }
    if (!children?.length) {
      const { children: _c, ...rest } = item
      return rest
    }
    return { ...item, children }
  })
}

/**
 * 过滤侧栏可见子菜单
 * @param {Array} items 子菜单列表
 * @returns {Array}
 */
function filterVisibleMenuItems(items) {
  return (items || []).filter((m) => m && Number(m.is_show) !== 0 && m.type !== 'A' && Number(m.is_disable) !== 1)
}

/**
 * 根据当前路由解析侧栏是否应收窄为「模块子树」
 * 当前策略：不在深路径平铺子页（如薪资管理下各 Tab），始终由 TertiaryMenu 展示一级模块下完整二/三级树（可伸缩子菜单）
 * @param {Array} menus 完整菜单树
 * @param {string} currentPath
 * @returns {{ title: string, items: Array }|null}
 */
export function resolveSecondaryMenuGroup(menus, currentPath) {
  const target = normalizePath(currentPath)
  if (!target || !menus?.length) return null
  // 统一返回 null：二级侧栏保持 OA（等一级模块）完整目录树，子模块（薪资管理等）用 el-sub-menu 展开
  return null
}

/**
 * 在树中按 id 查找节点
 * @param {Array} menus
 * @param {number} id
 * @returns {object|null}
 */
export function findMenuById(menus, id) {
  if (!menus?.length || id == null) return null
  let found = null
  const walk = (nodes) => {
    for (const node of nodes) {
      if (node.id === id) {
        found = node
        return
      }
      if (node.children?.length) walk(node.children)
    }
  }
  walk(menus)
  return found
}

/**
 * 宽根路径（如 /oa）无法前缀匹配子路由时，按模块路径归属一级菜单
 * @param {Array} firstLevelMenus pid=0 的一级目录
 * @param {string} currentPath 当前路由
 * @returns {object|null}
 */
/**
 * 收集侧栏/路由可用的菜单 paths（排除 A 级与宽根 /oa）
 */
function appendResolvedWorkbenchAppPaths(menus, paths) {
  const walk = (nodes) => {
    if (!Array.isArray(nodes)) return
    for (const n of nodes) {
      const isWorkbenchNode = isOaWorkbenchMenu(n)
      if (isWorkbenchNode && n.children?.length) {
        for (const child of n.children) {
          if (!child || child.type === 'A' || child.is_show === 0) continue
          const resolved = resolveWorkbenchAppPath(child) || resolveMenuItemPath(child)
          const rp = normalizePath(resolved)
          if (rp && !isAmbiguousMenuPath(rp) && !paths.includes(rp)) {
            paths.push(rp)
          }
        }
      }
      if (n.children?.length) walk(n.children)
    }
  }
  walk(menus)
}

export function collectNavMenuPaths(menus) {
  const paths = []
  const walk = (nodes) => {
    if (!Array.isArray(nodes)) return
    for (const n of nodes) {
      if (!n || n.type === 'A' || n.is_show === 0) continue
      const p = normalizePath(resolveMenuItemPath(n))
      if (p && !isAmbiguousMenuPath(p)) {
        paths.push(p)
      }
      if (n.children?.length) walk(n.children)
    }
  }
  walk(menus)
  appendResolvedWorkbenchAppPaths(menus, paths)
  return paths
}

/** OA 工作台菜单 id（hou_permission_menu.id，应用宫格子菜单 pid） */
export { OA_WORKBENCH_MENU_ID } from '@/utils/oaWorkbenchAppPath.js'

/** 已废弃的 OA 应用中心占位目录（is_show=0，勿再作为应用父级） */
export const OA_WORKBENCH_PLACEHOLDER_MENU_ID = 55169

/** OA 工作台多路径等价（路由组件相同，权限库常用 /oa/apps） */
export const OA_WORKBENCH_PATHS = ['/plugin/oa/apps', '/plugin/oa/workbench', '/plugin/oa/dashboard']

/**
 * 是否为 OA 工作台路由（应展示 OA 下全部二级目录，而非「占位」等快捷子菜单）
 * @param {string} path
 * @returns {boolean}
 */
export function isOaWorkbenchPath(path) {
  const p = normalizePath(path)
  return OA_WORKBENCH_PATHS.includes(p)
}

/**
 * 是否为 OA 工作台菜单（目录下挂应用宫格，点击应进入 /oa/apps 而非第一个子页）
 * 注意：商城/支付/CRM/MES/GEO 等均有同名「工作台」，禁止仅按 name 判定
 * @param {object} item
 * @returns {boolean}
 */
export function isOaWorkbenchMenu(item) {
  if (!item) return false
  if (Number(item.id) === Number(OA_WORKBENCH_MENU_ID)) return true
  const p = normalizePath(item.paths || item.path)
  if (OA_WORKBENCH_PATHS.includes(p)) return true
  const perms = String(item.perms || '').trim()
  if (perms === 'shell:oa:apps' || perms.startsWith('shell:oa:apps:')) return true
  // 仅 OA 域内才用名称兜底，避免 /mall/dashboard/overview 等被改跳到 /oa/apps
  if (String(item.name || '').trim() !== '工作台') return false
  if (perms.startsWith('shell:oa')) return true
  return p === '/plugin/oa' || p.startsWith('/plugin/oa/')
}

/**
 * OA 二级侧栏去重：同 paths=/oa/apps 时保留「工作台」，隐藏脏数据「占位」
 * @param {Array} items
 * @returns {Array}
 */
export function sanitizeOaSecondaryMenus(items) {
  if (!Array.isArray(items) || items.length === 0) return items
  const hasWorkbench = items.some((m) => isOaWorkbenchMenu(m))
  if (!hasWorkbench) return items
  return items.filter((m) => {
    if (m?.id === OA_WORKBENCH_PLACEHOLDER_MENU_ID) return false
    const p = normalizePath(m?.paths || m?.path)
    if (m?.name === '占位' && (p === '/plugin/oa/apps-hub' || p === '/plugin/oa/apps')) return false
    return true
  })
}

/** 应用容器只授权自身；具体叶子菜单可包含详情路由，不能借根路径放开兄弟页。 */
function isPluginPathAllowed(menus, target) {
  const usable = node => node && node.type !== 'A' &&
    (node.is_show == null || Number(node.is_show) === 1) && Number(node.is_disable) !== 1 &&
    (node.display_pos ?? node.displayPos) !== 'hidden'
  const canonical = path => OA_WORKBENCH_PATHS.includes(path) ? '/plugin/oa/apps' : path.replace(/\/+$/, '')
  const wanted = canonical(target)
  const walk = nodes => (nodes || []).some(node => {
    if (!usable(node)) return false
    const children = (node.children || []).filter(usable)
    const path = canonical(normalizePath(resolveMenuItemPath(node)))
    if (path.startsWith('/plugin/')) {
      const applicationRoot = /^\/plugin\/[^/]+$/.test(path)
      // OA 旧宽根有子菜单时只作为容器；工作台别名仍对应同一授权页面。
      if (wanted === path && !(path === '/plugin/oa' && children.length)) return true
      if (wanted === '/plugin/oa' && path === '/plugin/oa/apps') return true
      if (!applicationRoot && !children.length && wanted.startsWith(`${path}/`)) return true
    }
    return walk(children)
  })
  return walk(menus)
}

const MALL_FINANCE_COMPATIBLE_PATHS = new Map([
  ['/mall/finance/center', '/mall/admin/finance/center'],
  ['/mall/finance/refund', '/mall/admin/finance/refund'],
  ['/mall/finance/mall', '/mall/admin/finance/mall'],
  ['/mall/finance/shop_details/shop_settle', '/mall/admin/finance/shop_details/shop_settle'],
  ['/mall/finance/shop_details/balance', '/mall/admin/finance/shop_details/balance'],
  ['/mall/finance/shop_details/settlement_record', '/mall/admin/finance/shop_details/settlement_record'],
  ['/mall/finance/shop_details/withdraw', '/mall/admin/finance/shop_details/withdraw'],
  ['/mall/finance/user_details/withdraw', '/mall/admin/finance/user_details/withdraw'],
  ['/mall/finance/user_details/balance', '/mall/admin/finance/user_details/balance'],
  ['/mall/finance/user_details/commission', '/mall/admin/finance/user_details/commission'],
  ['/mall/finance/user_details/deposit', '/mall/admin/finance/user_details/deposit'],
  ['/mall/finance/overview', '/mall/admin/finance/overview'],
  ['/mall/finance/income', '/mall/admin/finance/income'],
  ['/mall/finance/expense', '/mall/admin/finance/expense'],
  ['/mall/finance/report', '/mall/admin/finance/report'],
  ['/mall/finance/setting', '/mall/admin/finance/setting']
])

function isMallFinancePathAllowed(target, allowed) {
  const compatible = MALL_FINANCE_COMPATIBLE_PATHS.get(target)
  if (!compatible) return false
  return allowed.some((path) => path === compatible || isMenuPathPrefix(compatible, path))
}

/**
 * 当前路由是否在用户菜单权限树允许范围内
 */
export function isPathAllowedByMenuTree(menus, routePath) {
  const rawTarget = normalizePath(routePath)
  const target = resolveMallPluginCenterPath(rawTarget)
  if (!target || target === '/' || target === '/dashboard') {
    return true
  }
  if (target.startsWith('/plugin/')) return isPluginPathAllowed(menus, target)
  if (!menus?.length) {
    return true
  }
  const allowed = collectNavMenuPaths(menus)
  // 数据库菜单尚未完成迁移时，旧 /addons 或 /addon 仍应允许进入规范页面。
  if (target === '/platform/plugins' || target.startsWith('/platform/plugins/')) {
    const pluginCenterPaths = new Set(['/platform/plugins', '/mall/addons', ...MALL_PLUGIN_CENTER_PATH_ALIASES.keys()])
    if (allowed.some((path) => pluginCenterPaths.has(path))) {
      return true
    }
  }
  if (target.startsWith('/mall/finance/') && isMallFinancePathAllowed(target, allowed)) {
    return true
  }
  return allowed.some((p) => target === p || isMenuPathPrefix(target, p))
}

export function findFirstLevelMenuByAmbiguousRoot(firstLevelMenus, currentPath) {
  const normalized = normalizePath(currentPath)
  if (!normalized || !firstLevelMenus?.length) return null
  for (const menu of firstLevelMenus) {
    const mp = normalizePath(menu.paths || menu.path)
    if (!mp || !isAmbiguousMenuPath(mp)) continue
    const base = mp.replace(/\/$/, '')
    if (normalized === base || normalized.startsWith(`${base}/`)) {
      return menu
    }
  }
  return null
}

/** OA 企微管理目录菜单 id */
export const WECOM_MENU_ID = 11011
export const WECOM_ROOT_PATH = '/plugin/oa/wecom'

/**
 * 根据当前路由计算应展开的 sub-menu id（用于 OA 内嵌企微等多级目录）
 * @param {Array} items
 * @param {string} activePath
 * @returns {string[]}
 */
export function collectOpenMenuIds(items, activePath) {
  const target = normalizePath(activePath)
  if (!target || !items?.length) return []
  const opened = new Set()

  const walk = (nodes, ancestors) => {
    for (const node of nodes) {
      const p = normalizePath(node.paths || node.path)
      const chain = [...ancestors, node.id]
      const children = node.children || []
      if (children.length) {
        const childHit = walk(children, chain)
        const selfHit = p && isMenuPathPrefix(target, p)
        if (childHit || selfHit) {
          chain.forEach((id) => opened.add(String(id)))
          return true
        }
      } else if (p && isMenuPathPrefix(target, p)) {
        ancestors.forEach((id) => opened.add(String(id)))
        return true
      }
    }
    return false
  }

  walk(items, [])
  return [...opened]
}

/**
 * 过滤侧栏可见节点
 * @param {Array} items
 * @returns {Array}
 */
function visibleMenuNodes(items) {
  return (items || []).filter((m) => m && m.is_show !== 0 && m.type !== 'A')
}

/**
 * 将菜单节点转为可跳转路径（目录取首个叶子）
 * @param {object} node
 * @returns {string}
 */
function menuNodeTabPath(node) {
  if (!node) return ''
  if (isOaWorkbenchMenu(node)) return '/plugin/oa/apps'
  const kids = visibleMenuNodes(node.children)
  if (kids.length) {
    const leaf = firstLeafMenuPath(node)
    return leaf ? resolveMenuItemPath({ ...node, paths: leaf, path: leaf }) : resolveMenuItemPath(node)
  }
  return resolveMenuItemPath(node)
}

/**
 * 当前路由是否落在 Tab 对应路径下
 * @param {string} currentPath
 * @param {string} tabPath
 * @returns {boolean}
 */
function isTabPathActive(currentPath, tabPath) {
  const target = normalizePath(currentPath)
  const tab = normalizePath(tabPath)
  if (!target || !tab) return false
  return target === tab || isMenuPathPrefix(target, tab) || isMenuPathPrefix(tab, target)
}

/** PM 测试流程页：数据库未配子菜单时，用固定 Tab 兜底 */
function buildPmTestTabs(currentPath) {
  const tabs = [
    { id: 'dashboard', name: '测试工作台', path: '/plugin/pm/test/dashboard' },
    { id: 'requirement_review', name: '需求评审', path: '/plugin/pm/test/requirement' },
    { id: 'test_plan', name: '测试计划', path: '/plugin/pm/test/plan' },
    { id: 'case_design', name: '用例设计', path: '/plugin/pm/test/case' },
    { id: 'smoke', name: '提测冒烟', path: '/plugin/pm/test/smoke' },
    { id: 'functional', name: '功能测试', path: '/plugin/pm/test/functional' },
    { id: 'defect', name: '缺陷管理', path: '/plugin/pm/test/defect' },
    { id: 'dev_fix', name: '开发修复', path: '/plugin/pm/test/fix' },
    { id: 'regression', name: '回归测试', path: '/plugin/pm/test/regression' },
    { id: 'acceptance', name: '上线验收', path: '/plugin/pm/test/acceptance' },
    { id: 'report', name: '测试报告', path: '/plugin/pm/test/report' },
    { id: 'opentest', name: 'OpenTest工作流', path: '/plugin/pm/test/opentest' }
  ]
  if (!tabs.some((t) => isTabPathActive(currentPath, t.path))) {
    return { parentTitle: '', tabs: [] }
  }
  return { parentTitle: '测试管理', tabs }
}

/**
 * 解析移动端三级菜单横向 Tab（当前二级目录下的同级子页）
 * @param {Array} menuItems 二级侧栏菜单树
 * @param {string} currentPath 当前路由
 * @returns {{ parentTitle: string, tabs: Array<{ id: number|string, name: string, path: string }> }}
 */
export function resolveTertiaryMenuTabs(menuItems, currentPath) {
  const target = normalizePath(currentPath)
  if (!target) {
    return { parentTitle: '', tabs: [] }
  }
  if (!menuItems?.length) {
    if (target.startsWith('/plugin/pm/test')) {
      return buildPmTestTabs(target)
    }
    return { parentTitle: '', tabs: [] }
  }

  if (target.startsWith('/plugin/pm/test')) {
    return buildPmTestTabs(target)
  }

  let deepestNode = null
  let deepestPathLen = 0
  let parentNode = null

  const findDeepest = (nodes, parent) => {
    for (const node of visibleMenuNodes(nodes)) {
      const p = normalizePath(node.paths || node.path)
      const kids = visibleMenuNodes(node.children)

      if (kids.length) {
        if (p && isMenuPathPrefix(target, p) && p.length >= deepestPathLen) {
          deepestNode = node
          deepestPathLen = p.length
          parentNode = parent
        }
        findDeepest(kids, node)
      } else if (p && isMenuPathPrefix(target, p) && p.length >= deepestPathLen) {
        deepestNode = node
        deepestPathLen = p.length
        parentNode = parent
      }
    }
  }

  findDeepest(menuItems, null)

  const mapTabs = (nodes) =>
    visibleMenuNodes(nodes)
      .map((ch) => ({
        id: ch.id,
        name: ch.name,
        path: menuNodeTabPath(ch)
      }))
      .filter((t) => t.path)

  // 扁平二级菜单（如项目管理下的项目概览/项目列表/任务管理）：parent 为空但应展示同级 Tab
  if (!parentNode?.children?.length && deepestNode) {
    const siblings = mapTabs(menuItems)
    if (siblings.length >= 2 && siblings.some((t) => isTabPathActive(target, t.path))) {
      return { parentTitle: '', tabs: siblings }
    }
    return { parentTitle: '', tabs: [] }
  }

  if (!parentNode?.children?.length) {
    return { parentTitle: '', tabs: [] }
  }

  const tabs = mapTabs(parentNode.children)

  if (tabs.length < 2) {
    return { parentTitle: '', tabs: [] }
  }

  const inSubtree = tabs.some((t) => isTabPathActive(target, t.path))
  if (!inSubtree) {
    return { parentTitle: '', tabs: [] }
  }

  return {
    parentTitle: parentNode.name || '',
    tabs
  }
}

/**
 * 移动端二级菜单宫格数据（不嵌套展开，目录项落到首个叶子路径）
 * @param {Array} menuItems 二级侧栏菜单树
 * @returns {Array<{ id: number|string, name: string, icon: string, path: string, hasChildren: boolean }>}
 */
export function buildMobileSecondaryTiles(menuItems) {
  return visibleMenuNodes(menuItems).map((item) => {
    const kids = visibleMenuNodes(item.children)
    return {
      id: item.id,
      name: item.name,
      icon: item.icon || '',
      path: menuNodeTabPath(item),
      hasChildren: kids.length > 0
    }
  }).filter((t) => t.path)
}

/** GEO 子树兼容插件命名空间，目录可没有路径，错误跨模块项仍需过滤。 */
export function filterGeoMenuSubtree(nodes) {
  const result = []
  for (const node of nodes || []) {
    if (!node) continue
    const path = normalizeLegacyPluginPath(node.paths || node.path || '')
    const children = filterGeoMenuSubtree(node.children)
    if (path !== '/plugin/geo' && !path.startsWith('/plugin/geo/') && children.length === 0) continue
    result.push(children.length ? { ...node, children } : { ...node })
  }
  return result
}
