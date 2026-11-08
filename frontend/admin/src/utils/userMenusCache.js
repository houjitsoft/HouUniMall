/**
 * 用户导航菜单本地缓存（hou_permission_menu → /web/permission/user/menus）
 *
 * - 仅缓存「当前用户」的菜单树（M/C 级导航），不缓存按钮权限列表
 * - 使用 localStorage 持久化，断网时可从缓存加载侧栏
 * - 权限标识仍由服务端实时拉取写入 sessionStorage，不参与离线持久化
 */

import { getToken } from '@/utils/auth'
import request from '@/utils/request'

/**
 * 菜单树缓存（localStorage）
 *
 * 版本号变更 = 强制所有在线用户下一次加载时丢弃旧菜单树、重新拉取。
 * 服务端改了角色授权 / 菜单归属后必须 +1，否则用户侧栏会一直停在旧结构
 * （localStorage 无 TTL，且服务端在旧包里没有 user:menus 缓存可清）。
 */
// v35：支付中心迁入 unipay 插件位并切换到 /plugin/unipay 路由，旧缓存必须失效。
// v36：插件中心(id=4) 移出左侧，仅保留右上角「插件应用」面板底部入口（2026-10-08）。
// v37：OA「月度工资条」菜单改名为「打款记录」（2026-10-08）。
// v38：商城设置(id=60011) 子项扁平化到系统设置，同名保留外层（2026-10-08）。
// v40：装修菜单重构为「PC装修(60136)」「移动装修(60251)」两个独立分组，
//      隐藏旧包装「装修管理(60010)」「页面装修(60134)」。服务端改了菜单归属，必须 bump 强制重拉。
// v41：ERP 库存管理下新增 7 个子菜单（库存单据/盘点/预警/批次效期/流水/安全库存/门店盈亏），
//      菜单由 20261016-erp-stock-menu.sql 写入 hou_permission_menu，必须 bump 否则用户看不到。
// v42：GEO 侧栏恢复「16 组 / 110 项」二级+三级分组（2026-09-22 曾被旧声明拍平），
//      菜单由 20261019-geo-nav-menu-restore.sql 重建，必须 bump 强制重拉。
// v43：GEO 删除「小红书平台设置」菜单（2026-10-20，开放平台无笔记发布类目，
//      改走创作服务平台 Cookie 通道），必须 bump 强制重拉。
// v44：新增「OCR 识别统计」插件菜单（/plugin/ocr，display_pos=plugin，由 20261022-ocr-admin-entry.sql
//      写入 hou_permission_menu，菜单 id=103652）。v43 缓存的菜单树里没有该插件位入口，
//      右上角「插件应用」面板因此不显示 OCR，必须 bump 强制重拉。
// v45：OCR 新增使用概览、接入用户、登录记录、识别记录四个真实权限子菜单。
// v46：AI 中台菜单归位（父 242 改名「AI智能」，6 个子页 shell:ai:*）。
// v47：AI 中台新增「用量统计」「余额配额」两个子菜单（/ai/usage、/ai/balance），
//      由 30-aiagent-front-menus.sql 写入 hou_permission_menu（id 61209/61210），必须 bump 否则看不到。
// v48：AI 中台新增「智能体管理」子菜单（/ai/agents，id 61353，由 32-aiagent-agent-menu.sql 写入），
//      必须 bump 否则看不到。
// v51：菜单铁律结构对齐开源初始化链路（140-menu-ironrule.sql：商城域 10 能力升 L1、
//      插件中心/商城管理壳移出左侧），开源评估重装后老缓存必须强制重拉。
// v52：开源裁剪未随包交付的商业插件位菜单（141-oss-plugin-menu-trim.sql），
//      插件市场不再把收费插件误显示为已安装，面板入口同步收回。
// v53：库存管理(1006)、售后服务(1011) 由左侧 L1 下沉为「交易管理(1003)」的二级菜单（2026-10-29），
//      服务端仅改 pid，不 bump 则老缓存仍把两者渲染成一级项。
// v54：物流管理(1010) 同样下沉为「交易管理(1003)」的二级菜单（2026-10-29）。
// v57：市场安装补齐插件菜单后，强制客户端重拉权限树，否则右上角「插件应用」仍为空。
// v58：抽奖转盘（userms-lucky-draw）由 1 个子菜单扩为 6 个（活动管理/活动配置/奖项设置/
//      礼品库/抽奖记录/参与用户，2026-11-02 写入 hou_permission_menu），不 bump 老缓存
//      仍只渲染「抽奖转盘管理」一项。
// v59：支付中心从 unipay 插件位回归 payment 独立服务，路径 /plugin/unipay/** → /payment/**，
//      菜单 display_pos=left。不 bump 则侧栏仍指向插件加载器，页面停在「插件页面加载失败」。
// v61：商品折扣插件分类从「未分类」修正为「客户 / 营销」（hou_permission_menu.cate_id 0→1），
//      不 bump 则顶栏「插件应用」面板仍按旧缓存把 goods-discount 归到未分类。
// v63：限时秒杀插件新增「千客千价」独立插件位入口（plugin.yaml 登记 order-seckill:price），
//      新插件应用菜单需 bump 才能让顶栏「插件应用」面板即时可见。
// v64：拼团(order-pintuan) 管理端补「拼团管理」二级菜单（平台库 hou_permission_menu id=103688，
//      pid=103672，paths=/plugin/order-pintuan/index）。前端 1.0.1 包已内置该路由，此前平台库缺
//      子菜单行导致进入插件后左侧无二级导航；补行后老缓存不会自动带出，必须 bump 强制重拉。
export const MENU_CACHE_KEY = 'houcloud_user_menus_v66'
/** 跨标签页失效时间戳（localStorage） */
export const STAMP_KEY = 'houcloud_user_menus_inv'
/** 旧版 sessionStorage key，迁移后清理 */
const LEGACY_SESSION_KEY = 'houcloud_user_menus_v11'
/** 历史版本 localStorage key，启动时清理，避免长期残留 */
const LEGACY_LOCAL_KEYS = ['houcloud_user_menus_v58', 'houcloud_user_menus_v53', 'houcloud_user_menus_v52', 'houcloud_user_menus_v51', 'houcloud_user_menus_v50', 'houcloud_user_menus_v47', 'houcloud_user_menus_v46', 'houcloud_user_menus_v45', 'houcloud_user_menus_v44', 'houcloud_user_menus_v43', 'houcloud_user_menus_v39', 'houcloud_user_menus_v38', 'houcloud_user_menus_v37', 'houcloud_user_menus_v36', 'houcloud_user_menus_v35', 'houcloud_user_menus_v34', 'houcloud_user_menus_v33', 'houcloud_user_menus_v32', 'houcloud_user_menus_v31', 'houcloud_user_menus_v30', 'houcloud_user_menus_v29', 'houcloud_user_menus_v28', 'houcloud_user_menus_v27', 'houcloud_user_menus_v26', 'houcloud_user_menus_v25', 'houcloud_user_menus_v24', 'houcloud_user_menus_v23', 'houcloud_user_menus_v22', 'houcloud_user_menus_v21', 'houcloud_user_menus_v20', 'houcloud_user_menus_v19', 'houcloud_user_menus_v18', 'houcloud_user_menus_v17', 'houcloud_user_menus_v16', 'houcloud_user_menus_v15', 'houcloud_user_menus_v14', 'houcloud_user_menus_v13', 'houcloud_user_menus_v12']

/**
 * 生成当前 token 的指纹，用于避免串用户缓存
 * @returns {string}
 */
export function tokenFingerprint() {
  const t = getToken()
  if (!t) return ''
  return t.length <= 24 ? t : `${t.slice(0, 12)}_${t.slice(-12)}`
}

/**
 * 是否可对本次请求参数做本地缓存（代查他人菜单等不缓存）
 * @param {Record<string, unknown>} params
 * @returns {boolean}
 */
export function shouldCacheMenusParams(params) {
  const p = params || {}
  if (p.userId != null && p.userId !== '') return false
  return true
}

/**
 * @param {Record<string, unknown>} params
 * @returns {string}
 */
function paramsKey(params) {
  const p = params || {}
  const keys = Object.keys(p).sort()
  return JSON.stringify(p, keys)
}

function readStamp() {
  return parseInt(localStorage.getItem(STAMP_KEY) || '0', 10)
}

function parseCachePayload(raw) {
  if (!raw) return null
  const parsed = JSON.parse(raw)
  if (parsed.fp !== tokenFingerprint()) return null
  if (parsed.savedAt < readStamp()) return null
  return parsed
}

/**
 * 从 localStorage 读取缓存的菜单数据
 * @param {Record<string, unknown>} params
 * @returns {unknown[]|null}
 */
export function readUserMenusCache(params) {
  try {
    if (!getToken()) return null
    const key = paramsKey(params)
    let parsed = parseCachePayload(localStorage.getItem(MENU_CACHE_KEY))
    if (!parsed) {
      parsed = parseCachePayload(sessionStorage.getItem(LEGACY_SESSION_KEY))
      if (parsed && parsed.paramsKey === key && Array.isArray(parsed.data)) {
        writeUserMenusCache(params, parsed.data)
        sessionStorage.removeItem(LEGACY_SESSION_KEY)
      } else {
        return null
      }
    }
    if (parsed.paramsKey !== key) return null
    // 空数组视为无效缓存，避免认证抖动后把「空菜单」写死导致一二级菜单长期空白
    if (!Array.isArray(parsed.data) || parsed.data.length === 0) return null
    return parsed.data
  } catch {
    return null
  }
}

/**
 * 写入菜单缓存（localStorage）
 * @param {Record<string, unknown>} params
 * @param {unknown[]} data
 */
export function writeUserMenusCache(params, data) {
  try {
    if (!getToken() || !shouldCacheMenusParams(params)) return
    if (!Array.isArray(data)) return
    if (data.length === 0) {
      // 成功的空权限结果必须清掉旧授权，避免离线回退又恢复已收回的菜单。
      invalidateUserMenusCache({ bumpStamp: false })
      return
    }
    const stamp = readStamp()
    let savedAt = Date.now()
    if (savedAt <= stamp) savedAt = stamp + 1
    const payload = {
      fp: tokenFingerprint(),
      paramsKey: paramsKey(params),
      savedAt,
      data
    }
    localStorage.setItem(MENU_CACHE_KEY, JSON.stringify(payload))
    sessionStorage.removeItem(LEGACY_SESSION_KEY)
  } catch (e) {
    console.warn('[userMenusCache] write failed', e)
  }
}

/**
 * 清除本地菜单缓存并广播失效时间戳（同域其他标签页可据此丢弃缓存）
 * @param {{ bumpStamp?: boolean }} [options]
 */
export function invalidateUserMenusCache(options = {}) {
  const { bumpStamp = true } = options
  try {
    localStorage.removeItem(MENU_CACHE_KEY)
    LEGACY_LOCAL_KEYS.forEach(key => localStorage.removeItem(key))
    sessionStorage.removeItem(LEGACY_SESSION_KEY)
    if (bumpStamp) {
      localStorage.setItem(STAMP_KEY, String(Date.now()))
    }
  } catch (e) {
    console.warn('[userMenusCache] invalidate failed', e)
  }
}

let lastPermissionsRefreshAt = 0
let permissionsRefreshPromise = null
const PERMISSIONS_REFRESH_INTERVAL_MS = 60_000

/**
 * 从服务端刷新当前用户按钮权限（仅 sessionStorage，不持久化到 localStorage）
 * @param {boolean} [force=false]
 * @returns {Promise<boolean>} 是否刷新成功
 */
export async function refreshPermissionsFromServer(force = false) {
  if (!getToken() || (typeof navigator !== 'undefined' && !navigator.onLine)) {
    return false
  }
  // 路由守卫、菜单缓存和登录收尾可能同时触发刷新；复用同一个请求，避免下游被请求风暴拖垮。
  if (permissionsRefreshPromise) {
    return permissionsRefreshPromise
  }
  const now = Date.now()
  if (!force && now - lastPermissionsRefreshAt < PERMISSIONS_REFRESH_INTERVAL_MS) {
    return false
  }
  // 失败也要进入冷却窗口，否则每次导航都会立即再次打到服务端。
  lastPermissionsRefreshAt = now
  const refreshPromise = (async () => {
    try {
      const { authService } = await import('@/api/auth/auth')
      const res = await authService.getUserPermissions()
      if (res && (res.code === 1 || res.code === 200) && Array.isArray(res.data)) {
        sessionStorage.setItem('permissions', JSON.stringify(res.data))
        localStorage.removeItem('permissions')
        window.dispatchEvent(new CustomEvent('houcloud:permissions-updated'))
        return true
      }
    } catch (e) {
      console.warn('[userMenusCache] refresh permissions failed', e)
    }
    return false
  })()
  permissionsRefreshPromise = refreshPromise
  try {
    return await refreshPromise
  } finally {
    if (permissionsRefreshPromise === refreshPromise) {
      permissionsRefreshPromise = null
    }
  }
}

/**
 * 菜单/角色变更后：清菜单缓存、刷新权限与侧栏
 * @returns {Promise<void>}
 */
export async function refreshMenusAfterPermissionChange() {
  invalidateUserMenusCache()
  const { useAppStore } = await import('@/stores/app')
  const appStore = useAppStore()
  // 保留当前侧栏，直到新菜单返回后再替换，避免一二级菜单闪空。
  appStore.clearMenus({ skipInvalidate: true, keepVisible: true })
  await refreshPermissionsFromServer(true)
  await appStore.fetchMenus(true)
}

/**
 * 右上角「清除缓存」：清本地菜单缓存并重新拉取菜单与权限
 * @param {{ clearServerCache?: boolean }} [options]
 * @returns {Promise<void>}
 */
export async function clearMenuCacheAndRefresh(options = {}) {
  const { clearServerCache = false } = options
  invalidateUserMenusCache()
  const { useAppStore } = await import('@/stores/app')
  const appStore = useAppStore()
  // 保留当前侧栏，直到新菜单返回后再替换，避免一二级菜单闪空。
  appStore.clearMenus({ skipInvalidate: true, keepVisible: true })
  if (clearServerCache) {
    try {
      await request.post('/web/permission/menu/clear-cache', {})
    } catch (e) {
      console.warn('[userMenusCache] server clear-cache failed', e)
    }
  }
  await refreshPermissionsFromServer(true)
  await appStore.fetchMenus(true)
}

/**
 * 其他标签页修改菜单/权限时，本标签页同步清缓存并拉取最新菜单
 */
export function initUserMenusCrossTabSync() {
  if (typeof window === 'undefined') return
  // 清掉上一次版本号留下的菜单树，避免 localStorage 里长期堆着多份历史菜单
  try {
    LEGACY_LOCAL_KEYS.forEach(key => localStorage.removeItem(key))
  } catch (e) {
    console.warn('[userMenusCache] purge legacy keys failed', e)
  }
  window.addEventListener('storage', (e) => {
    if (e.key !== STAMP_KEY || e.newValue == null) return
    refreshMenusAfterPermissionChange().catch(() => {})
  })
}
