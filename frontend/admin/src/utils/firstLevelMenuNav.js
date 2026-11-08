/**
 * 一级菜单（侧栏 + 顶栏右上角分组）共用的菜单树装配与跳转逻辑。
 *
 * 背景：宿主分组根「业务经营」（`shell:group:biz`）与「其他应用」（`shell:group:other`）
 * 从侧栏一级栏移到顶栏右上角（顶栏由 `components/HeaderMenuGroups.vue` 渲染）。
 * 为了让两处渲染的**功能与交互完全一致**，把原先写在 `FirstLevelMenu.vue` 里的
 * 「菜单树归一化 / 路径归一化 / 点击跳转」整段逻辑原样搬到这里，两个组件共用同一份实现，
 * 避免出现「侧栏能跳、顶栏跳错」这类双实现漂移。
 *
 * 注意：本文件里的跳转分支（CMS / 商城 / 系统设置 / 供应商 / OA / 支付 / 插件中心 / GEO …）
 * 是历史上多次线上纠偏沉淀的结果，迁移时保持语义不变，不要顺手"优化"掉。
 */

import { ElMessage } from 'element-plus'
import { normalizeLegacyPluginPath } from '@/config/pluginRouteConfig.js'
import { isOaWorkbenchMenu } from '@/utils/menuNavigate.js'
import { visibleChildren } from './menuDisplay.js'

/** GEO 插件卡片应进工作台首页 /plugin/geo（GEODashboard），勿落到子树首叶子。 */
const isGeoPluginHome = (menu) => {
  if (!menu) return false
  const path = normalizeLegacyPluginPath(String(menu.paths || menu.path || '')).replace(/\/+$/, '')
  if (path === '/plugin/geo') return true
  const perms = String(menu.perms || '').trim()
  if (perms === 'shell:geo:apps' || perms.startsWith('shell:geo:apps:')) return true
  return String(menu.name || '').trim() === 'GEO' && path.startsWith('/plugin/geo')
}

// 应用内部的分组不一定有页面；只从服务器可见子树寻找实际入口。
const firstApplicationPage = (menu, acceptsPath = () => true) => {
  for (const child of visibleChildren(menu)) {
    const descendant = firstApplicationPage(child, acceptsPath)
    if (descendant) return descendant
    const path = child.paths || child.path
    if (path && acceptsPath(path) && !(child.children || []).some(node => node.type !== 'A')) return path
  }
  return ''
}

/** 宿主菜单分组根标记（与后端 `PluginMenuSyncPort.HOST_GROUP_PARAM` 对应） */
export const MENU_GROUP_PARAM = 'host:menu-group'

/** 移到顶栏右上角展示的分组 key（其余分组根仍在侧栏） */
export const HEADER_GROUP_KEYS = ['biz', 'other']

/** 顶栏分组的稳定标识（perms），是判定的第一依据 */
export const HEADER_GROUP_PERMS = HEADER_GROUP_KEYS.map((key) => `shell:group:${key}`)

/** 顶栏分组名称兜底：库表 perms 异常时仍能识别（用户需求即以名称为准） */
export const HEADER_GROUP_NAMES = ['业务经营', '其他应用']

/** 是否为宿主菜单分组根 */
export const isMenuGroup = (menu) => (menu?.params || '') === MENU_GROUP_PARAM

/**
 * 是否为「应移到顶栏」的分组根。
 * 优先按 perms 稳定标识判定，名称仅作兜底。
 * @param {object} menu
 * @returns {boolean}
 */
export const isHeaderGroupMenu = (menu) => {
  if (!isMenuGroup(menu)) return false
  const perms = String(menu?.perms || '').trim()
  if (HEADER_GROUP_PERMS.includes(perms)) return true
  return HEADER_GROUP_NAMES.includes(String(menu?.name || '').trim())
}

/**
 * 分组内可见子菜单（排除隐藏/禁用行；is_show 缺省视为可见，兼容前端产物节点）
 * @param {object} menu
 * @returns {Array}
 */
export const visibleGroupChildren = (menu) => (menu?.children || []).filter(child =>
  child &&
  child.type !== 'A' &&
  (child.is_show == null || child.is_show === 1) &&
  child.is_disable !== 1
)

/** 分组只决定收纳位置；二级栏始终展示命中模块的完整授权子树。 */
export function resolveGroupedModuleMenu(group, currentPath) {
  const target = String(currentPath || '').replace(/\/+$/, '')
  let result = null
  let longest = 0
  const score = (nodes) => {
    let best = 0
    for (const node of visibleGroupChildren({ children: nodes })) {
      const raw = node.paths || node.path || ''
      const path = raw ? normalizeLegacyPluginPath(raw).replace(/\/+$/, '') : ''
      if (path && (target === path || target.startsWith(path + '/'))) best = Math.max(best, path.length)
      best = Math.max(best, score(node.children || []))
    }
    return best
  }
  for (const module of visibleGroupChildren(group)) {
    const match = score([module])
    if (match > longest) { result = module; longest = match }
  }
  return result
}

/** Resolve the owning group after alias normalization; canonical module paths win ties. */
export function resolveFirstLevelMenuByDescendant(menus, currentPath) {
  const target = normalizeLegacyPluginPath(currentPath).replace(/\/+$/, '')
  let result = null
  let best = 0
  const walk = (nodes, root) => {
    for (const node of visibleGroupChildren({ children: nodes })) {
      const raw = String(node.paths || node.path || '').replace(/\/+$/, '')
      const path = raw ? normalizeLegacyPluginPath(raw) : ''
      if (path && (target === path || (path !== '/plugin/oa' && target.startsWith(path + '/')))) {
        const score = path.length * 2 + (raw === path ? 1 : 0)
        if (score > best) { best = score; result = root }
      }
      walk(node.children || [], root)
    }
  }
  for (const root of menus || []) walk(root.children || [], root)
  return result
}

/**
 * 取出可见的一级菜单（pid=0 且 type=M 且 is_show=1）。
 * 空分组自动隐藏（方案 §5.4）：分组根下没有任何可见子项时不渲染。
 * @param {Array} menuList 已归一化的完整菜单树
 * @returns {Array}
 */
export const pickFirstLevelMenus = (menuList) => {
  const menus = (menuList || []).filter(menu =>
    menu.pid === 0 &&
    menu.type === 'M' &&
    menu.is_show === 1
  )
  return menus.filter(menu => !isMenuGroup(menu) || visibleGroupChildren(menu).length > 0)
}

/**
 * 路径归一化（原 `FirstLevelMenu.vue#normalizePath`，语义不变）
 * @param {string} path
 * @param {number} pid
 * @returns {string}
 */
export const normalizeMenuPath = (path, pid) => {
  let p = path || ''
  if (!p.startsWith('/')) p = '/' + p
  p = normalizeLegacyPluginPath(p)

  // 特殊处理：只针对OA模块（pid=7）进行路径转换
  // 其他模块直接返回原路径
  if (pid === 7) {
    if (p === '/workflow') return '/plugin/oa/workflow'
    if (p === '/dashboard' || p === '/apps') return '/plugin/oa/workbench'
    if (p === '/plugin/oa' || p.startsWith('/plugin/oa/')) return p
    if (!p.startsWith('/plugin/oa/')) return '/plugin/oa' + (p === '/' ? '' : p)
  }

  // 对于其他所有模块（包括生产制造 pid=0 或 其他pid），直接返回原路径
  return p
}

/**
 * 处理菜单点击，根据菜单配置决定跳转路径（原 `FirstLevelMenu.vue#handleMenuClick`）。
 * @param {Object} menu - 菜单对象
 * @param {Object} context
 * @param {import('vue-router').Router} context.router
 * @param {Function} [context.onNavigate] - 跳转成功后的回调（原 emit('navigate')）
 */
export const navigateFromMenuNode = (menu, { router, onNavigate } = {}) => {
  if (!menu || !router) return
  let path = menu.paths || menu.path || `/menu-${menu.id}`
  const pid = menu.pid || 0
  const isActivityEntry = normalizeLegacyPluginPath(path).replace(/\/+$/, '') === '/plugin/activity'

  // 如果菜单有子菜单，根据菜单名称决定跳转逻辑
  if (!isActivityEntry && menu.children && menu.children.length > 0) {
    // PF4J 插件菜单已经提供完整的 /plugin/{code} 路径，不追加宿主 dashboard。
    // OA / GEO 工作台入口例外：根路径本身就是首页，子树首叶子常是业务页（公告、关键词研究等）。
    if (String(menu.paths || menu.path || '').startsWith('/plugin/') || path === '/lowcode') {
      if (isOaWorkbenchMenu(menu)) {
        path = '/plugin/oa/apps'
      } else if (isGeoPluginHome(menu)) {
        path = '/plugin/geo'
      } else {
        path = firstApplicationPage(menu) || path
      }
    // CMS菜单特殊处理：跳转到统计控制台
    } else if (menu.name === 'CMS' || menu.paths === '/cms') {
      // 查找统计控制台菜单
      const statisticsMenu = menu.children.find(child =>
        child.paths && child.paths.includes('/statistics/dashboard')
      )
      if (statisticsMenu) {
        path = statisticsMenu.paths
      } else {
        const firstChild = menu.children[0]
        path = firstChild.paths || firstChild.path || path
      }
    } else {
      // 其他菜单：根据菜单名称特殊处理
      // 商城系统：概览已并入核心仪表盘
      if (menu.name === '商城系统' || menu.name === '商城管理' || menu.paths === '/mall') {
        path = '/dashboard'
      }
      // 系统设置：跳转到 /setting/website
      else if (menu.name === '系统设置' || menu.paths === '/setting') {
        const websiteMenu = menu.children.find(child =>
          child.paths && child.paths.includes('/setting/website')
        )
        if (websiteMenu && websiteMenu.paths) {
          path = websiteMenu.paths
        } else {
          path = '/setting/website'
        }
      }
      // 供应商：跳转到 /supplier/list
      else if (menu.name === '供应商' || menu.paths === '/supplier') {
        const listMenu = menu.children.find(child =>
          child.paths && child.paths.includes('/supplier/list')
        )
        if (listMenu && listMenu.paths) {
          path = listMenu.paths
        } else {
          path = '/supplier/list'
        }
      }
      // OA办公：进入工作台，侧栏保留 OA 全部二级模块
      else if (menu.name === 'OA办公' || menu.paths === '/oa' || menu.id === 7) {
        const workbench = menu.children.find(child =>
          child.paths && (child.paths === '/plugin/oa/apps' || child.paths === '/plugin/oa/workbench')
        )
        if (workbench?.paths) {
          path = workbench.paths
        } else {
          const firstNav = (menu.children || []).find(
            (ch) => ch.type !== 'A' && ch.is_show !== 0 && (ch.paths || ch.path)
          )
          path = firstNav ? (firstNav.paths || firstNav.path) : '/plugin/oa/workbench'
        }
      }
      // 支付中心：进入已授权工作台（对应菜单 id=248）
      else if (menu.name === '支付管理' || menu.name === '支付中心' || menu.paths === '/payment' || menu.paths === '/plugin/unipay') {
        const dashboardMenu = menu.children.find(child =>
          child.paths && (child.paths === '/payment/dashboard' || child.paths === '/plugin/unipay/dashboard')
        )
        if (dashboardMenu && dashboardMenu.paths) {
          path = dashboardMenu.paths === '/plugin/unipay/dashboard' ? '/payment/dashboard' : dashboardMenu.paths
        } else {
          path = '/payment/dashboard'
        }
      }
      // O2O retains only authorized rider children; there is no /rider/dashboard page.
      else if (menu.paths === '/rider') {
        const entries = visibleGroupChildren(menu).filter(child => child.paths || child.path)
        const list = entries.find(child => (child.paths || child.path) === '/rider/list')
        path = list?.paths || entries[0]?.paths || entries[0]?.path || path
      }
      // 插件中心：首页为 /platform/plugins，不存在 /platform/dashboard 路由
      else if (menu.name === '插件中心' || menu.paths === '/platform') {
        const centerMenu = (menu.children || []).find(child => (child.paths || child.path) === '/platform/plugins')
        const firstNav = (menu.children || []).find(
          (ch) => ch.type !== 'A' && ch.is_show !== 0 && (ch.paths || ch.path)
        )
        path = centerMenu?.paths || firstNav?.paths || firstNav?.path || '/platform/plugins'
      }
      // GEO 模块：工作台首页即 /geo（GEODashboard），不存在 /geo/dashboard 路由，勿拼接 dashboard
      else if (menu.name === 'GEO' || menu.paths === '/geo') {
        const geoHomeMenu = (menu.children || []).find(child => (child.paths || child.path) === '/geo')
        path = geoHomeMenu?.paths || geoHomeMenu?.path || '/geo'
      }
      // 商城业务升为独立一级后，目录自身没有 dashboard，沿授权子树进入实际页面。
      else if (String(menu.paths || menu.path || '').startsWith('/mall/')) {
        path = firstApplicationPage(menu, candidate => !router.resolve || router.resolve(normalizeMenuPath(candidate, pid)).name !== 'NotFound') || path
      }
      // 其他有子树的目录：只走真实页面。禁止臆造 {paths}/dashboard（会落到 404）。
      else {
        const dashboardMenu = (menu.children || []).find(child => {
          const childPath = child.paths || child.path || ''
          return childPath.includes('/dashboard') || childPath.endsWith('/dashboard')
        })
        if (dashboardMenu && (dashboardMenu.paths || dashboardMenu.path)) {
          path = dashboardMenu.paths || dashboardMenu.path
        } else {
          path = firstApplicationPage(menu) || path
        }
      }
    }
  }

  // 规范化路径
  path = normalizeMenuPath(path, pid)
  // 营销活动入口直达指定插件分类，并标记 entry=marketing，
  // 让插件中心隐藏 Hero/统计条，直接进列表；其它入口不受影响。
  if (isActivityEntry) path = '/platform/plugins?cateId=4&entry=marketing'

  if (path && path.startsWith('/')) {
    router.push(path).catch(err => {
      if (err.name !== 'NavigationDuplicated') {
        // 跳转失败，尝试回退到一级菜单路径
        const fallbackPath = normalizeMenuPath(menu.paths || menu.path || `/menu-${menu.id}`, pid)
        if (fallbackPath && fallbackPath !== path) {
          router.push(fallbackPath).catch(fallbackErr => {
            if (fallbackErr.name !== 'NavigationDuplicated') {
              ElMessage.error('页面跳转失败: ' + (fallbackErr.message || '未知错误'))
            }
          })
        } else {
          ElMessage.error('页面跳转失败: ' + (err.message || '未知错误'))
        }
      }
    })
    if (typeof onNavigate === 'function') onNavigate()
  } else {
    ElMessage.warning('菜单路径配置错误，无法跳转')
  }
}

/**
 * 处理菜单选择（el-menu 的 @select / el-dropdown 的 @command 共用）。
 * 原 `FirstLevelMenu.vue#handleMenuSelect`，语义不变。
 * @param {string} index - 菜单索引值（路径字符串）
 * @param {Object} context
 * @param {Array} context.menus - 已归一化的完整菜单树
 * @param {Array} context.firstLevelMenus - 可见一级菜单（含分组根的 children）
 * @param {import('vue-router').Router} context.router
 * @param {Function} [context.onNavigate]
 */
export const navigateFromMenuIndex = (index, { menus, firstLevelMenus, router, onNavigate } = {}) => {
  const roots = firstLevelMenus || []
  const allMenus = menus || []

  // 优先从 firstLevelMenus 中查找（因为这里包含了children数据）
  const menuItem = roots.find(menu => {
    const menuPath = menu.paths || menu.path || `/menu-${menu.id}`
    return menuPath === index
  })

  if (menuItem) {
    navigateFromMenuNode(menuItem, { router, onNavigate })
    return
  }

  // 分组根的子菜单点击：先在分组 children 中解析出菜单对象再跳转（方案 §5.4）
  const groupChild = roots
    .flatMap(menu => (menu.children || []))
    .find(child => (child.paths || child.path) === index)
  if (groupChild) {
    navigateFromMenuNode(groupChild, { router, onNavigate })
    return
  }

  // 如果firstLevelMenus中找不到，尝试从menuList中查找
  const menu = allMenus.find(m => (m.paths || m.path || `/menu-${m.id}`) === index)
  if (menu) {
    navigateFromMenuNode(menu, { router, onNavigate })
    return
  }

  // 直接使用index作为路径
  const path = normalizeMenuPath(index, 0)
  if (path && path.startsWith('/')) {
    router.push(path).catch(err => {
      if (err.name !== 'NavigationDuplicated') {
        console.error('路由跳转错误:', err)
      }
    })
    if (typeof onNavigate === 'function') onNavigate()
  }
}

/**
 * 规范化菜单路径（递归处理子菜单）。原 `FirstLevelMenu.vue#normalizeMenuTree`，语义不变。
 * @param {Array} menus - 菜单列表
 * @returns {Array} 规范化后的菜单列表
 */
export const normalizeMenuTree = (menus = []) => menus.map(m => {
  let normalizedPath = m.paths || m.path || ''
  if (normalizedPath && !normalizedPath.startsWith('/')) {
    normalizedPath = '/' + normalizedPath
  }
  // 只对OA模块（pid=7）进行路径转换
  if (m.pid === 7) {
    normalizedPath = normalizeMenuPath(normalizedPath, m.pid)
  }
  return {
    ...m,
    paths: normalizedPath,
    children: m.children ? normalizeMenuTree(m.children) : []
  }
})
