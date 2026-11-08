/**
 * 登录期插件装载器
 *
 * 背景：登录页未登录，插件 store 的「已安装插件列表」接口需要 token，
 * 因此声明了登录方式的插件（manifest.loginProviders）必须在登录期单独装载一次。
 *
 * 装载策略（前端自闭环，不依赖任何新增后端接口）：
 * 1. 从 config/loginPlugins.js 取白名单（code + version）；
 * 2. 直接 import 前端静态托管的插件产物 /plugin-assets/{code}/{version}/index.mjs；
 * 3. 只取 manifest.loginProviders 注册到扩展点，不注册路由/菜单（那属于登录后的插件 store）；
 * 4. 产物缺失或装载失败一律静默跳过，绝不影响账号密码登录主流程。
 */

import request from '@/utils/request'
import { LOGIN_STAGE_PLUGINS } from '@/config/loginPlugins'
import { registerPluginLoginProviders } from '@/extensions/loginProviders'
import {
  buildCoreApiPath,
  exposeHostPluginRuntime,
  normalizePluginApiResponse
} from '@/stores/plugin'

let loaded = false
let pending = null

/** 插件在登录期可用的最小上下文：只开放宿主 Core Port，不给路由/菜单能力 */
function buildLoginStageContext(code) {
  const callCoreApi = async (path, body = {}, options = {}) => {
    const method = String(options?.method || 'post').toLowerCase()
    if (!['get', 'post', 'put', 'delete', 'patch'].includes(method)) {
      throw new Error(`Core API 不支持请求方法：${method}`)
    }
    const url = buildCoreApiPath(path)
    const cfg = options?.config || {}
    // 与 stores/plugin.js 保持一致：GET/DELETE 需合并 body 与 config.params，
    // 否则插件注入的 projectId 会被 config.params 覆盖。
    if (method === 'get' || method === 'delete') {
      const response = await request[method](url, {
        ...cfg,
        params: { ...(cfg.params || {}), ...(body || {}) }
      })
      return normalizePluginApiResponse(response)
    }
    const response = await request[method](url, body, cfg)
    return normalizePluginApiResponse(response)
  }
  return {
    pluginCode: code,
    stage: 'login',
    host: {
      vue: globalThis.__HOUCLOUD_PLUGIN_RUNTIME__?.vue || null
    },
    callApi(path, body, options) {
      // 登录期没有插件专属通道（泛化转发需要插件已注册），统一走 Core Port
      return callCoreApi(path, body, options)
    },
    callCoreApi,
    /**
     * 插件完成第三方登录后回调宿主写入登录态并进入首页。
     * 依赖延迟加载，避免 loader 与 router / store 形成静态循环依赖。
     */
    onLoginSuccess: async (payload) => {
      const token = payload?.token
      if (!token) return
      const [{ setAuthInfo }, authModule, routerModule] = await Promise.all([
        import('@/utils/auth'),
        import('@/stores/auth'),
        import('@/router')
      ])
      const authStore = authModule.useAuthStore()
      const userInfo = payload?.userInfo || payload
      setAuthInfo(token, userInfo)
      authStore.token = token
      authStore.userInfo = userInfo
      authStore.isLoggedIn = true
      const router = routerModule?.default
      if (router?.push) router.push('/')
      else window.location.assign('/')
    }
  }
}

async function loadOne({ code, version }) {
  if (!code || !version) return
  const url = `/plugin-assets/${code}/${version}/index.mjs`
  let mod
  try {
    mod = await import(/* @vite-ignore */ url)
  } catch (error) {
    // 产物未随包发布（如开源包不含 unilogin）：登录页不该因为一个缺失产物而报错，
    // 静默跳过即可——浏览器网络层仍会打一条 404，但不再冒泡成未处理异常。
    console.warn('[login-plugins] 登录插件产物不可用，已跳过:', url, error?.message || error)
    return
  }
  const manifest = mod?.default ?? mod
  const list = Array.isArray(manifest?.loginProviders) ? manifest.loginProviders : []
  if (!list.length) return
  if (typeof manifest.setup === 'function') {
    await manifest.setup(buildLoginStageContext(code))
  }
  registerPluginLoginProviders(code, list)
}

/**
 * 装载全部登录期插件（幂等；并发调用共享同一 Promise）
 * @returns {Promise<void>}
 */
export function loadLoginStagePlugins() {
  if (loaded) return Promise.resolve()
  if (pending) return pending
  pending = (async () => {
    try {
      exposeHostPluginRuntime()
    } catch (error) {
      console.warn('[login-plugins] 宿主运行时注入失败', error)
      return
    }
    await Promise.all(
      LOGIN_STAGE_PLUGINS.map((item) =>
        loadOne(item).catch((error) => {
          console.warn(`[login-plugins] 装载 ${item?.code} 失败，已跳过`, error?.message || error)
        })
      )
    )
    loaded = true
  })()
  return pending
}

/** 测试用：重置装载状态 */
export function resetLoginStagePlugins() {
  loaded = false
  pending = null
}
