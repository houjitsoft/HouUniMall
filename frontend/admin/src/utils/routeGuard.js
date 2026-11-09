/**
 * 路由守卫工具函数
 */

import { getToken, getUserId } from '@/utils/auth'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { usePluginStore } from '@/stores/plugin'
import { refreshPermissionsFromServer } from '@/utils/userMenusCache'
import { findAllMenusByPath, isPathAllowedByMenuTree, resolveMallPluginCenterPath } from '@/utils/menuNavigate'
import { toAppPath } from '@/utils/appBase'
import { userHasOrganization, userOrgStatusUnknown } from '@/utils/orgUtils'
import { hasPermission } from '@/utils/permission'
import { findModuleByRoute } from '@/config/dashboard/modules'

/**
 * 无需菜单树校验的系统页（不在侧栏菜单中，但登录后可访问）
 */
const MENU_CHECK_SKIP_PATHS = new Set([
  '/bind-organization'
])

/**
 * 兜底 404 补跳记录：fullPath → 最近一次补跳时间。
 *
 * 插件路由是异步注册的，首次匹配到兜底 404 时补跳一次即可命中真实路由。
 * 没有这个限制时，目标插件确实没安装/未启用会形成「重定向到自己」的闭环，
 * vue-router 判定为无限重定向后**中止整次导航** —— 页面原地不动，
 * 用户看到的就是「点了没反应」（工作台卡片点不动就是这个原因）。
 */
const notFoundRetriedAt = new Map()
const NOT_FOUND_RETRY_WINDOW_MS = 3000

/**
 * 插件深链时守卫最多同步等待插件注册完成的时长。
 *
 * <p>插件前端产物从 ① 经 ④ 公网回源（带宽 ~140KB/s），PM 这类 3MB 产物要 17-25 秒。
 * 守卫同步 await 期间 Vue 不会渲染任何组件，页面全白 ⇒ 只能等。
 * 超过这个上限就放行到 PluginPending 占位路由，让用户看到「正在加载应用」，
 * 插件就绪后由占位页自己跳转。
 */
const PLUGIN_ROUTE_WAIT_MS = 2500

/**
 * 给 promise 加超时：超时后 resolve(undefined)，不 reject（不打断导航）。
 *
 * @param {Promise<any>} promise 待等待的 promise
 * @param {number} ms 超时毫秒
 * @returns {Promise<any>}
 */
function withTimeout(promise, ms) {
  if (!promise || typeof promise.then !== 'function') return Promise.resolve()
  return Promise.race([
    promise.catch(() => undefined),
    new Promise(resolve => setTimeout(() => resolve(undefined), ms))
  ])
}

function shouldSkipMenuCheck(to) {
  if (to.meta?.skipMenuCheck || to.meta?.public) {
    return true
  }
  return MENU_CHECK_SKIP_PATHS.has(to.path)
}

/**
 * 是否放行「工作台模块卡片」直达路径。
 *
 * 工作台卡片已按 `dashboard:module:<id>` 授权后才渲染，但目标页未必有侧栏菜单，
 * 例如 /dcenter/dashboard（数据中心，无侧栏入口）。这类路径按「模块权限」放行，
 * 不要求同时出现在菜单树里；只做精确路径匹配，不会因为卡片注册了 /plugin/pm
 * 就放开 /plugin/pm/** 整棵子树。
 *
 * @param {string} path 目标路由路径
 * @returns {boolean}
 */
function isDashboardShortcutAllowed(path) {
  const module = findModuleByRoute(path)
  if (!module?.perm) return false
  return hasPermission(module.perm)
}

/**
 * 是否开启多组织模式
 * @returns {boolean} true=开启，false=关闭
 */
function isMultiOrganizationEnabled() {
  return localStorage.getItem('multiOrganizationEnabled') !== '0'
}

/**
 * 判断是否来自A级菜单重定向
 * @param {Object} route - 路由对象
 * @returns {boolean} 是否来自A级菜单重定向
 */
export function isFromALevelRedirect(route) {
  // 检查查询参数中是否有fromALevel标识
  return route.query?.fromALevel === 'true'
}

/**
 * 从路由中获取激活的标签页
 * @param {Object} route - 路由对象
 * @returns {string} 激活的标签页标识
 */
export function getActiveTabFromRoute(route) {
  // 从查询参数或路径中提取标签页信息
  return route.query?.tab || route.params?.tab || 'default'
}

/**
 * 清理重定向参数
 * @param {Object} query - 路由查询参数
 * @returns {Object} 清理后的查询参数
 */
export function cleanRedirectParams(query) {
  // 创建一个新的查询参数对象，排除重定向相关的参数
  const cleanedQuery = { ...query }
  delete cleanedQuery.fromALevel
  delete cleanedQuery.redirectedFrom
  delete cleanedQuery.tab
  return cleanedQuery
}

/**
 * 支付中心旧插件地址 → payment 服务页面。
 * /plugin/unipay 与 /plugin/unipay/recharge 分别落到工作台和对应子页。
 * @param {string} path
 * @returns {string} 命中时返回新路径，否则空串
 */
function legacyPaymentServicePath(path) {
  const value = String(path || '')
  if (value === '/plugin/unipay') return '/payment/dashboard'
  if (value.startsWith('/plugin/unipay/')) {
    return `/payment/${value.slice('/plugin/unipay/'.length)}`
  }
  return ''
}

/**
 * 从访问路径中提取插件编码
 * 例：/plugin/geo/workflow → geo；/plugin/erp → erp
 * @param {string} path 路由路径
 * @returns {string|null} 插件编码，非插件路径返回 null
 */
function extractPluginCodeFromPath(path) {
  const match = /^\/plugin\/([^/]+)/.exec(String(path || ''))
  return match ? match[1] : null
}

/**
 * 路由前置守卫
 * @param {Object} to - 目标路由
 * @param {Object} from - 来源路由
 * @param {Function} next - 下一步函数
 */
export async function beforeEachGuard(to, from, next) {
  // 设置页面标题
  if (to.meta && to.meta.title) {
    document.title = `${to.meta.title} - UniMall管理后台`
  } else {
    document.title = 'UniMall管理后台'
  }

  // 【老式A级菜单重定向URL纠偏】
  // A级菜单机制已废弃（子菜单有各自独立页面），
  // 但浏览器书签/历史/未更新的生产构建可能仍会生成形如
  // /oa/salary?fromALevel=true&redirectedFrom=/oa/salary/setting&tab=setting 的路径。
  // 统一纠偏到 redirectedFrom 指向的真实子菜单页，并清除旧参数。
  if (to.query && to.query.fromALevel === 'true' && to.query.redirectedFrom) {
    const target = String(to.query.redirectedFrom)
    if (target.startsWith('/') && target !== to.path) {
      next({ path: target, replace: true })
      return
    }
  }

  // 检查是否为公开页面（不需要登录）
  if (to.meta && to.meta.public) {
    next()
    return
  }
  
  // 检查是否为合同签署页面（公开页面）
  if (to.path.startsWith('/contract/sign')) {
    next()
    return
  }

  // 支付中心是 payment 独立服务。旧插件地址在拉插件产物之前就改写，避免停在插件失败页。
  const paymentServicePath = legacyPaymentServicePath(to.path)
  if (paymentServicePath) {
    next({ path: paymentServicePath, query: to.query, hash: to.hash, replace: true })
    return
  }
  
  // 检查用户是否已登录
  const token = getToken()
  const isLoginPath = to.path === '/login'
  const isRegisterPath = to.path === '/register'
  const isForgotPasswordPath = to.path === '/forgot-password'
  const isPublicAuthPath = isLoginPath || isRegisterPath || isForgotPasswordPath
  
  // 如果没有token且访问的不是登录/注册/忘记密码页面，则重定向到登录页面
  if (!token && !isPublicAuthPath) {
    next({
      path: '/login',
      query: { redirect: to.fullPath } // 保存用户原本想要访问的页面
    })
    return
  }

  // 营销活动旧地址与菜单入口一致，目标页仍经过正常登录和菜单权限校验。
  // 带 entry=marketing，让插件中心以紧凑列表展示（隐藏 Hero/统计）；其它入口不受影响。
  if (token && to.path.replace(/\/+$/, '') === '/plugin/activity') {
    next({ path: '/platform/plugins', query: { cateId: '4', entry: 'marketing' }, replace: true })
    return
  }

  // Dynamic plugin routes must be registered before a direct plugin URL is
  // evaluated. Otherwise the initial match is the static 404 route and the
  // plugin screen cannot be resolved on a browser refresh.
  if (token && !isPublicAuthPath) {
    try {
      const pluginStore = usePluginStore()
      const pluginCode = extractPluginCodeFromPath(to.path)
      if (!pluginStore.loadedOnce) {
        // 2026-09-19 首屏优化：不再在这里串行 import 全部 20+ 个插件的 index.mjs，
        // 否则刷新 /plugin/geo 这类深链地址会白屏十几秒。现在插件路径只等「它自己那一个
        // 插件」，非插件路径（/dashboard 等）一个都不等，其余插件全部转后台注册。
        //
        // 2026-10-20 加超时上限：插件产物从 ① 经 ④ 公网回源，带宽只有 ~140KB/s，
        // PM 这类 3MB 产物要 17-25 秒才能下完。守卫同步 await 期间 Vue 不会渲染任何
        // 组件（页面全白），用户只看到标题变成「正在加载应用」却没有任何内容。
        // 改成最多等 PLUGIN_ROUTE_WAIT_MS：超时后先放行到 PluginPending 占位路由
        // （该路由在 Layout 下，能正常渲染「正在加载应用」并保留侧边栏），
        // 插件就绪后由占位页自己 router.replace 回真实路径。
        await withTimeout(
          pluginStore.loadInstalledPlugins({ priorityCode: pluginCode }),
          PLUGIN_ROUTE_WAIT_MS
        )
      } else if (pluginCode && !pluginStore.loadedCodes.includes(pluginCode)) {
        // 后台注册还没轮到它（用户首屏放行后马上点了这个插件的菜单），补等一下。
        // 只 waitForBackgroundPlugins() 会干等 44 个插件串行下载完，目标插件常常排在
        // 30 名之后，60 秒占位页必然超时。这里改为「先把目标插件提到队首并行注册」：
        // registerPlugin 内部有 registeringPluginCodes / loadedCodes 去重，后台队列轮到时会自动跳过。
        const target = (pluginStore.installed || []).find(
          (item) => item?.code === pluginCode && item.enabled && item.frontendUrl
        )
        if (target) {
          await withTimeout(pluginStore.registerPlugin(target), PLUGIN_ROUTE_WAIT_MS)
        } else {
          await withTimeout(pluginStore.waitForBackgroundPlugins(), PLUGIN_ROUTE_WAIT_MS)
        }
      }
      if (to.name === 'NotFound' && pluginStore.loadedCodes.length > 0) {
        // 只补跳一次（3s 窗口内同一路径不重复补跳），否则目标插件不可达时会死循环，
        // vue-router 中止导航 → 页面原地不动（工作台卡片「点了没反应」的第二个成因）。
        const key = to.fullPath
        const now = Date.now()
        const lastAt = notFoundRetriedAt.get(key) || 0
        if (now - lastAt > NOT_FOUND_RETRY_WINDOW_MS) {
          notFoundRetriedAt.set(key, now)
          next({ path: to.fullPath, replace: true })
          return
        }
      }
    } catch (error) {
      console.warn('[RouteGuard] 插件路由加载跳过:', error?.message)
    }
  }
  
  // 如果已经登录且访问的是登录/注册/忘记密码页面，则重定向到首页
  if (token && isPublicAuthPath) {
    next({ path: '/' })
    return
  }
  
  // 【重要修复】不再在路由守卫中获取用户信息
  // 原因：
  // 1. 用户登录时authStore.usernameLogin/phoneLogin已经获取了userInfo
  // 2. 每次路由切换都会触发路由守卫，导致重复调用getUserInfo
  // 3. 如果token在Redis中过期，getUserInfo会返回"用户未登录"错误
  // 4. 响应拦截器会清除localStorage并跳转登录页，导致已登录用户被强制登出
  // 5. 与Layout组件的getUserInfo调用形成双重调用，问题加剧
  // 解决方案：
  // - 路由守卫只检查token是否存在，不主动获取userInfo
  // - userInfo在登录时已经获取，保存在authStore和localStorage中
  // - 如果token过期，由API请求时的响应拦截器处理，自动跳转登录页
  // - 避免在进入页面前就因token过期导致的意外登出
  
  // 检查用户是否已绑定组织（仅多组织模式开启时执行）
  if (token && isMultiOrganizationEnabled()) {
    try {
      const authStore = useAuthStore()
      // 优先从store获取，如果没有则从localStorage获取
      let userInfo = authStore.userInfo
      if (!userInfo) {
        const userInfoStr = localStorage.getItem('userInfo')
        if (userInfoStr) {
          try {
            userInfo = JSON.parse(userInfoStr)
          } catch (e) {
            console.error('解析用户信息失败:', e)
          }
        }
      }
      
      // 如果用户信息存在，检查是否有组织
      if (userInfo) {
        let hasOrg = userHasOrganization(userInfo)
        const isBindOrgPath = to.path === '/bind-organization'

        if (!hasOrg && !isBindOrgPath && userOrgStatusUnknown(userInfo)) {
          userInfo = await authStore.ensureOrganizationLoaded(userInfo)
          hasOrg = userHasOrganization(userInfo)
        }

        if (!hasOrg && !isBindOrgPath) {
          if (userOrgStatusUnknown(userInfo)) {
            console.warn('[RouteGuard] 组织信息仍未加载，跳过绑定组织跳转（避免 queryUserInfo 失败误判）', {
              userId: userInfo.userId ?? userInfo.user_id ?? getUserId()
            })
          } else {
            console.log('[RouteGuard] 用户未绑定组织，跳转到绑定组织页面', {
              orgList: userInfo.orgList ?? userInfo.organizationList
            })
            next({
              path: '/bind-organization',
              query: { redirect: to.fullPath }
            })
            return
          }
        }

        if (hasOrg && isBindOrgPath) {
          // 归一化：redirect 可能带部署基路径前缀（子路径部署下由外部跳转带入）
          const redirect = toAppPath(to.query.redirect || '/dashboard')
          console.log('[RouteGuard] 用户已绑定组织，从绑定页面重定向到:', redirect)
          next({ path: redirect })
          return
        }
      } else {
        // 如果用户信息不存在，可能是首次登录，允许访问绑定组织页面
        const isBindOrgPath = to.path === '/bind-organization'
        if (!isBindOrgPath) {
          // 如果不在绑定组织页面，尝试获取用户信息
          // 注意：这里不等待getUserInfo完成，避免阻塞路由
          // 如果getUserInfo失败，会在API请求时由响应拦截器处理
          console.log('[RouteGuard] 用户信息不存在，尝试获取用户信息')
          // 不阻塞路由，继续执行
        }
      }
    } catch (error) {
      console.error('检查组织信息失败:', error)
      // 如果检查失败，继续路由，避免阻塞
    }
  }
  
  // 无菜单权限的路由禁止访问（系统页、未绑定组织阶段跳过）
  if (getToken() && to.path && !shouldSkipMenuCheck(to)) {
    const permissionPath = to.meta?.permissionPath || to.path
    const isPluginRoute = resolveMallPluginCenterPath(permissionPath).startsWith('/plugin/')
    try {
      const authStore = useAuthStore()
      let userInfo = authStore.userInfo
      if (!userInfo) {
        try {
          const raw = localStorage.getItem('userInfo')
          if (raw) userInfo = JSON.parse(raw)
        } catch {
          /* ignore */
        }
      }
      // 未绑定组织时不做菜单校验，避免与 bind-organization 形成重定向死循环
      if (
        isMultiOrganizationEnabled()
        && userInfo
        && !userHasOrganization(userInfo)
        && !userOrgStatusUnknown(userInfo)
      ) {
        next()
        return
      }

      const appStore = useAppStore()
      if (!appStore.menuLoaded) {
        await appStore.fetchMenus(false)
      }
      if (typeof navigator !== 'undefined' && navigator.onLine) {
        refreshPermissionsFromServer().catch(() => {})
      }
      const menus = appStore.baseMenus
      if ((menus?.length || isPluginRoute) && !isPathAllowedByMenuTree(menus, permissionPath)) {
        // 菜单树里没有这条路径时的兜底：工作台模块卡片的直达路径（卡片已按
        // dashboard:module:<id> 授权）放行 —— 目标页未必有侧栏菜单（/dcenter/dashboard
        // 就没有），不放行的话用户点卡片会被静默弹回仪表盘。
        // 冷启动深链（直接刷新该页）时按钮权限表可能还没拉回来，补等一次再判。
        if (!isPluginRoute && findModuleByRoute(to.path)?.perm) {
          let shortcutAllowed = isDashboardShortcutAllowed(to.path)
          if (!shortcutAllowed) {
            await refreshPermissionsFromServer().catch(() => {})
            shortcutAllowed = isDashboardShortcutAllowed(to.path)
          }
          if (shortcutAllowed) {
            next()
            return
          }
        }
        console.warn(isPluginRoute
          ? '[RouteGuard] 应用路径未获服务器菜单授权，请登记并授权对应菜单:'
          : '[RouteGuard] 无菜单权限，拒绝访问:', to.path)
        next({ path: '/dashboard', replace: true })
        return
      }
    } catch (e) {
      if (isPluginRoute) {
        console.warn('[RouteGuard] 应用菜单权限加载失败，拒绝访问:', to.path)
        next({ path: '/dashboard', replace: true })
        return
      }
      console.warn('[RouteGuard] 菜单权限校验跳过:', e?.message)
    }
  }

  // 考勤列表误配为 /oa/contract/process 时，直接打开 URL 也纠偏到正确页面
  if (to.path === '/plugin/oa/contract/process') {
    try {
      const appStore = useAppStore()
      if (appStore.menuLoaded && appStore.baseMenus?.length) {
        const matches = findAllMenusByPath(appStore.baseMenus, '/plugin/oa/contract/process')
        const attendance = matches.find(
          (m) => (m.perms || '').includes('attendance:lists') || m.name === '考勤列表'
        )
        if (attendance) {
          next({ path: '/plugin/oa/attendance/lists', replace: true })
          return
        }
      }
    } catch (e) {
      console.warn('[RouteGuard] 考勤列表路径纠偏跳过:', e?.message)
    }
  }

  // 继续路由（不等待用户信息获取完成）
  next()
}

export default {
  isFromALevelRedirect,
  getActiveTabFromRoute,
  cleanRedirectParams,
  beforeEachGuard
}
