/**
 * HTTP请求封装
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth.js'
import { getCurrentOrgId } from '@/utils/orgUtils'
import { appUrl, isAppPath } from '@/utils/appBase'
import { isSuccessCode, resolveError, shouldRedirectLogin, logBackendMessage } from '@/utils/errorCode'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API, // 基础URL
  timeout: 45000 // 全局 45s，对齐 BFF default readTimeout(30s)+缓冲，减少「前端先断、后端还在跑」
})

/**
 * 插件装载窗口的自愈重试。
 *
 * 背景：mall 重启后插件是在 Servlet 容器开始监听**之后**才装载的（PluginRuntimeBootstrap），
 * 大插件（如 geo，240MB / 600 条路由）要 1~4 分钟才注册完路由。这段窗口里 /web/plugin/**
 * 会命中「插件装载闸门」返回 503 + Retry-After。对使用者来说这是部署瞬时状态，
 * 不该以「接口报错」的形式暴露，所以在这里透明重试掉。
 *
 * 只对幂等方法重试：装载窗口内重放 POST 可能造成重复创建，宁可让用户看到一次失败。
 */
const PLUGIN_BOOT_MAX_RETRY = 20 // 单次等待 ≤15s，总上限约 5 分钟，覆盖超大插件
const PLUGIN_BOOT_HINT = '插件正在装载'

function parseBootBody (data) {
  if (!data) return {}
  if (typeof data === 'string') {
    try { return JSON.parse(data) } catch { return {} }
  }
  return data
}

function isPluginBootLoading (error) {
  if (error?.response?.status !== 503) return false
  const method = String(error?.config?.method || 'get').toLowerCase()
  if (method !== 'get' && method !== 'head') return false
  if (!String(error?.config?.url || '').includes('/plugin/')) return false
  const body = parseBootBody(error.response.data)
  return String(body?.error || '').includes(PLUGIN_BOOT_HINT) || body?.retryAfter != null
}

function bootRetryDelay (error) {
  const body = parseBootBody(error.response.data)
  const hinted = Number(error?.response?.headers?.['retry-after'] ?? body?.retryAfter)
  return Number.isFinite(hinted) && hinted > 0 ? Math.min(hinted * 1000, 15000) : 10000
}

/** @returns {Promise|null} 命中装载窗口时返回重试 Promise，否则返回 null 交回原错误处理 */
function retryPluginBootLoading (error) {
  if (!isPluginBootLoading(error)) return null
  const cfg = error.config || {}
  const attempt = Number(cfg.__pluginBootRetry || 0) + 1
  if (attempt > PLUGIN_BOOT_MAX_RETRY) return null
  const wait = bootRetryDelay(error)
  if (attempt === 1) {
    ElMessage({ message: '插件正在装载，页面将自动重试…', type: 'warning', duration: 3000 })
  }
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      service.request({ ...cfg, __pluginBootRetry: attempt }).then(resolve, reject)
    }, wait)
  })
}

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 登录接口设置独立超时，略大于BFF对auth服务的readTimeout(45s)
    if (config.url && (config.url.includes('/auth/login') || config.url.includes('/auth/codelogin'))) {
      config.timeout = 35000
    }
    
    // 为短信发送接口设置更长的超时时间（60秒）
    if (config.url && config.url.includes('/auth/smscode')) {
      config.timeout = 60000  // 60秒超时
    }

    // PM 企微客户群 / 第三方配置等可能级联 thirdparty+企微 API
    if (config.url && (
      config.url.includes('/projects/wecom-group-chats') ||
      config.url.includes('/setting/third-party')
    )) {
      config.timeout = 90000
    }

    // 企微通讯录/SCRM/考勤同步：拉取+落库可能超过默认 45s
    if (config.url && (
      config.url.includes('/wecom/department/sync') ||
      (config.url.includes('/wecom/scrm/') && config.url.includes('/sync')) ||
      config.url.includes('/oa/attendance/sync') ||
      config.url.includes('/oa/attendance/wecom/sync') ||
      config.url.includes('/oa/attendance/lists/sync')
    )) {
      config.timeout = 180000
    }

    // AI 对话：同步接口要等整段生成，实测长文案 60～70 秒，45 秒会先断（后端仍在跑，只留 Broken pipe）
    // 后台「试用对话」已改走 /web/ai/chat/stream 流式，这里给同步兜底路径留足余量。
    if (config.url && config.url.includes('/ai/chat')) {
      config.timeout = 180000
    }

    // 角色权限保存：全量菜单可达 2000+，后端批量写 + 祖先补全，需长于默认 45s
    if (config.url && (
      config.url.includes('/permission/role/edit') ||
      config.url.includes('/permission/role/add') ||
      config.url.includes('/permission/role/menu/batch-set')
    )) {
      config.timeout = 90000
    }

    // 添加token到请求头
    const token = getToken()
    // 对于登录、注册、合同签署等不需要token的接口，不添加token也不警告
    const isAuthEndpoint = config.url && (
      config.url.includes('/auth/login') || 
      config.url.includes('/auth/register') || 
      config.url.includes('/auth/codelogin') ||
      config.url.includes('/auth/unilogin/') ||
      config.url.includes('/auth/smscode') ||
      config.url.includes('/auth/forgot-password') ||
      config.url.includes('/contract/sign') ||  // 合同签署接口
      config.url.includes('/oa/contract/sign') ||  // 合同签署接口（OA服务）
      config.url.includes('/base/sys-config/website-switches') ||  // 网站开关，登录前后均可读
      config.url.includes('/base/sys-config/admin-language')  // 后台语言包，登录页渲染前就要取，此时必然无 token
    )
    
    if (token) {
      // 确保 token 不包含 Bearer 前缀，避免重复添加
      const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token
      config.headers.Authorization = `Bearer ${cleanToken}`
    } else if (!isAuthEndpoint) {
      // 只有非认证接口才警告缺少token
      console.warn('警告: 请求中没有Token!', config.url)
    }
    
    // 添加前端域名到请求头（用于后端动态生成链接）
    if (typeof window !== 'undefined') {
      const frontendOrigin = window.location.origin
      config.headers['X-Frontend-Origin'] = frontendOrigin
    }

    const orgId = getCurrentOrgId()
    if (orgId) {
      config.headers['X-Org-Id'] = orgId
    }

    // PM 接口统一补 orgId（登录后从 userInfo 自动推导）
    if (config.method?.toLowerCase() === 'post' && config.url?.includes('/web/plugin/pm/')) {
      const orgId = getCurrentOrgId()
      if (orgId) {
        if (config.data == null) {
          config.data = { orgId }
        } else if (typeof config.data === 'object' && !(config.data instanceof FormData)) {
          const hasOrgId = config.data.orgId != null && String(config.data.orgId).trim() !== ''
          const hasOrgIdSnake = config.data.org_id != null && String(config.data.org_id).trim() !== ''
          if (!hasOrgId && !hasOrgIdSnake) {
            config.data = { ...config.data, orgId }
          }
        }
      }
    }
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const reqConfig = response.config || {}
    if (reqConfig.responseType === 'blob') {
      return response
    }

    const res = response.data
    // 业务侧主动声明：本次请求即使返回未登录类错误，也不要清 token / 不要跳登录页
    // 用法：request.post(url, data, { skipAuthRedirect: true })
    // 适用场景：Dashboard 类聚合统计接口（接口失败不应让整个会话被踢）
    const skipAuthRedirect = reqConfig.skipAuthRedirect === true
    // 业务侧主动声明：本次请求失败不要弹 ElMessage 错误提示
    const skipErrorMessage = reqConfig.skipErrorMessage === true
    
    // 成功与否只看码集合（1 / 200），不再散写 `res.code === 200 || res.code === 1`
    // 注意：某些接口可能返回 code: 0 表示成功（如某些历史接口），由 isSuccessCode 统一承载
    if (isSuccessCode(res.code)) {
      return res
    }
    
    // 记录非成功的响应（业务侧声明 skipErrorMessage 时静默，如预期 404 降级场景）
    if (!skipErrorMessage) {
      console.warn('接口返回非成功状态:', {
        url: response.config.url,
        code: res.code,
        message: res.message,
        data: res.data
      })
    }
    
    // 检查是否有权限相关的错误：主判定只看错误码语义（PERMISSION 类别），
    // 不再写 `res.code === 11007` 裸码；message 关键词仅作兼容期兜底。
    const permissionResolved = resolveError(res.code, { backendMessage: res.message })
    const isPermissionError = permissionResolved.category === 'PERMISSION' || (res.message && (
      res.message.includes('权限不足') ||
      res.message.includes('无权限') ||
      res.message.includes('没有权限') ||
      res.message.includes('Permission denied')
    ))
    
    if (isPermissionError) {
      logBackendMessage(res, { url: response.config.url })
      // 组织上下文类 403/权限码：后端文案更可操作（如未加入组织），优先展示
      const backendMsg = typeof res.message === 'string' ? res.message.trim() : ''
      const preferBackend = backendMsg && (
        backendMsg.includes('组织')
        || backendMsg.includes('业务系统')
        || backendMsg.includes('智能入库')
      )
      const tip = preferBackend ? backendMsg : permissionResolved.text
      if (!skipErrorMessage) {
        import('element-plus').then(({ ElMessage }) => {
          ElMessage.error(tip)
        })
      }
      return Promise.reject(new Error(tip))
    }
    
    // 统一处理"用户未登录"的情况，无论错误码是什么（如 11006, 0 等）
    // 检查消息中是否包含"用户未登录"相关的关键词
    const requestPath = response.config.url || ''
    // 注册/验证码等匿名接口：业务错误信息里可能含「登录」字样，绝不能清 token 并强跳登录页
    const skipLoginRedirectForPath = requestPath.includes('/auth/register') ||
      requestPath.includes('/auth/smscode') ||
      requestPath.includes('/auth/codelogin') ||
      requestPath.includes('/auth/unilogin/') ||
      requestPath.includes('/auth/forgot-password') ||
      requestPath.includes('/base/sys-config/website-switches') ||
      requestPath.includes('/base/sys-config/admin-language')

    // 服务降级/暂时不可用类错误（如后端Redis/认证服务故障），这是服务端问题，
    // 不能清token强跳登录页，否则会误伤已登录用户。
    const isServiceUnavailable = res.message && (
      res.message.includes('暂时不可用') ||
      res.message.includes('服务不可用') ||
      res.message.includes('稍后重试') ||
      res.message.includes('请稍后再试')
    )

    const isNotLoggedIn = !isServiceUnavailable && (res.message && (
      res.message.includes('用户未登录') ||
      res.message.includes('未登录') ||
      res.message.includes('请重新登录') ||
      res.message.includes('请先登录') ||
      res.message.includes('请重新登陆') ||
      res.message.includes('token失效') ||
      res.message.includes('Token失效') ||
      res.message.includes('token非法') ||
      res.message.includes('认证失败') ||
      res.message.includes('认证过期')
    ) || (
      // 跳登录只由 AUTH 类别触发（11006 / 401 等未认领旧码在 config.js 里显式声明），
      // 不再在拦截器里写 `res.code === 11006 || res.code === 401`
      shouldRedirectLogin(resolveError(res.code, { backendMessage: res.message }))
    ))
    
    // 检查是否是合同签署相关的接口，这些接口的验证失败不应该跳转到登录页
    const isContractSignEndpoint = response.config.url && (
      response.config.url.includes('/contract/verify-phone') ||
      response.config.url.includes('/contract/sign') ||
      response.config.url.includes('/contract/sign-info') ||
      response.config.url.includes('/oa/contract/verify-phone') ||
      response.config.url.includes('/oa/contract/sign') ||
      response.config.url.includes('/oa/contract/sign-info')
    )
    
    // 检查当前是否在合同签署页面
    const isContractSignPage = window.location.pathname && (
      window.location.pathname.includes('/contract/sign')
    )
    
    if (isNotLoggedIn) {
      // 如果是合同签署相关的接口或页面，不跳转到登录页，只返回错误
      if (isContractSignEndpoint || isContractSignPage) {
        return Promise.reject(new Error(res.message || '验证失败'))
      }
      
      // 业务侧明确声明跳过登录跳转：仅返回错误，不清 token、不跳 /login
      // 避免 Dashboard 等聚合统计接口偶发返回 11006 时把整个会话踢掉
      if (skipAuthRedirect) {
        return Promise.reject(new Error(res.message || '用户未登录'))
      }
      
      // 如果是认证失败，清除认证信息并跳转到登录页面
      // 清除所有认证相关的本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userId')
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
      
      // 清除 auth store 中的状态（异步处理，不阻塞跳转）
      import('@/stores/auth').then(({ useAuthStore }) => {
        try {
          const authStore = useAuthStore()
          authStore.logout()
        } catch (error) {
          console.warn('清除 auth store 状态失败:', error)
        }
      }).catch(error => {
        console.warn('导入 auth store 失败:', error)
      })
      
      // 如果当前不在登录页面，则跳转到登录页面
      // 注意：子路径部署（/admin）下登录页真实地址是 /admin/login，不能硬编码 '/login'
      if (!isAppPath('/login')) {
        // 使用 setTimeout 延迟跳转，避免在路由守卫中重复处理
        setTimeout(() => {
          if (!isAppPath('/login')) {
            window.location.href = appUrl('/login')
          }
        }, 100)
      }
      
      return Promise.reject(new Error(res.message || '用户未登录'))
    }
    
    // 处理其他错误情况
    if (res.code === 0) {
      // 后端误用 fail(String) 时真实文案可能在 data 字符串里
      const detailMsg = (typeof res.data === 'string' && res.data.trim())
        ? res.data.trim()
        : (res.message || '操作失败')
      const error = new Error(detailMsg)
      error.response = {
        data: res
      }
      error.config = reqConfig
      error.silent = skipErrorMessage
      return Promise.reject(error)
    } else {
      // 其他状态码认为是错误
      // 创建一个包含完整响应信息的错误对象
      const error = new Error(res.message || 'Error')
      error.response = {
        data: res  // 保留完整的响应数据
      }
      error.config = reqConfig
      error.silent = skipErrorMessage
      return Promise.reject(error)
    }
  },
  error => {
    // 业务侧主动声明跳过登录跳转 / 错误提示
    const reqCfg = error.config || {}
    const skipAuthRedirect = reqCfg.skipAuthRedirect === true
    const skipErrorMessage = reqCfg.skipErrorMessage === true

    // 静默处理重复请求、预期降级等场景，不打印错误日志
    if (error.code === 'DUPLICATE_REQUEST' || error.silent === true) {
      return Promise.reject(error)
    }

    // 插件装载窗口（503 + Retry-After）对 GET 透明重试，不进日志也不弹错误
    const bootRetry = retryPluginBootLoading(error)
    if (bootRetry) {
      return bootRetry
    }

    // 装饰性请求（如顶栏告警红点）声明 skipErrorMessage 时，网络层失败也不刷 console.error。
    // 这类错误没有 response，响应分支不会给 error.silent 赋值，只能回读 config。
    if (!skipErrorMessage) {
      console.error('请求错误:', error.message || error)
    }
    
    // 处理HTTP错误状态码
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      const requestUrl = error.config?.url || ''
      
      // 检查是否是合同签署相关的接口
      const isContractSignEndpoint = requestUrl && (
        requestUrl.includes('/contract/verify-phone') ||
        requestUrl.includes('/contract/sign') ||
        requestUrl.includes('/contract/sign-info') ||
        requestUrl.includes('/oa/contract/verify-phone') ||
        requestUrl.includes('/oa/contract/sign') ||
        requestUrl.includes('/oa/contract/sign-info')
      )
      
      // 检查当前是否在合同签署页面
      const isContractSignPage = window.location.pathname && (
        window.location.pathname.includes('/contract/sign')
      )
      
      // 注册/验证码等匿名接口：业务错误信息里可能含「登录」字样，绝不能清 token 并强跳登录页
      const skipLoginRedirectForPath2 = requestUrl.includes('/auth/register') ||
        requestUrl.includes('/auth/smscode') ||
        requestUrl.includes('/auth/codelogin') ||
        requestUrl.includes('/auth/forgot-password') ||
        requestUrl.includes('/base/sys-config/website-switches') ||
        requestUrl.includes('/base/sys-config/admin-language')

      // 检查响应体中的错误码和消息，判断是否为"用户未登录"
      // 服务降级/暂时不可用类错误不应被当作未登录处理
      const isServiceUnavailable = data && data.message && (
        data.message.includes('暂时不可用') ||
        data.message.includes('服务不可用') ||
        data.message.includes('稍后重试') ||
        data.message.includes('请稍后再试')
      )

      const isNotLoggedIn = !skipLoginRedirectForPath2 && !isServiceUnavailable && (data && (
        // 跳登录只由 AUTH 类别触发（11006 / 401 等未认领旧码在 config.js 里显式声明），
        // 不再在拦截器里写 `data.code === 11006 || data.code === 401`
        shouldRedirectLogin(resolveError(data?.code, { backendMessage: data?.message })) ||
        // 检查消息（精确匹配，避免「登录」字样的业务错误误触发）
        (data.message && (
          data.message.includes('用户未登录') || 
          data.message.includes('未登录') || 
          data.message.includes('请重新登录') ||
          data.message.includes('请先登录') ||
          data.message.includes('请重新登陆') ||
          data.message.includes('token失效') ||
          data.message.includes('Token失效') ||
          data.message.includes('token非法') ||
          data.message.includes('认证失败') ||
          data.message.includes('认证过期')
        ))
      ) || status === 401)
      
      if (isNotLoggedIn) {
        // 对于登录接口的401错误，不自动跳转登录页面
        if (error.config && error.config.url && error.config.url.includes('/auth/login')) {
          ElMessage({
            message: '用户名或密码错误',
            type: 'error',
            duration: 5 * 1000
          })
        } else if (isContractSignEndpoint || isContractSignPage) {
          // 合同签署验证失败，不跳转到登录页，只返回错误
          return Promise.reject(error)
        } else if (skipAuthRedirect) {
          // 业务侧明确声明跳过登录跳转：不清 token、不跳 /login，仅返回错误
          return Promise.reject(error)
        } else {
          // 清除认证信息并跳转到登录页面
          // 清除所有认证相关的本地存储
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          localStorage.removeItem('userId')
          sessionStorage.removeItem('token')
          sessionStorage.removeItem('userInfo')
          
          // 清除 auth store 中的状态（异步处理，不阻塞跳转）
          import('@/stores/auth').then(({ useAuthStore }) => {
            try {
              const authStore = useAuthStore()
              authStore.logout()
            } catch (error) {
              console.warn('清除 auth store 状态失败:', error)
            }
          }).catch(error => {
            console.warn('导入 auth store 失败:', error)
          })
          
          // 如果当前不在登录页面，则跳转到登录页面
          // 注意：子路径部署（/admin）下登录页真实地址是 /admin/login，不能硬编码 '/login'
          if (!isAppPath('/login')) {
            setTimeout(() => {
              if (!isAppPath('/login')) {
                window.location.href = appUrl('/login')
              }
            }, 100)
          }
        }
      } else if (!skipErrorMessage) {
        // 其他错误，显示错误消息（业务侧声明 skipErrorMessage 时静默）
        // 只出可读中文文案，后端原文（SQL / 类名 / 堆栈）绝不直出，仅进日志
        const httpResolved = resolveError(data?.code, { backendMessage: data?.message })
        logBackendMessage(data, { url: error.config?.url, status })
        ElMessage({
          message: httpResolved.text,
          type: 'error',
          duration: 5 * 1000
        })
      }
    } else if (!skipErrorMessage) {
      // 只有非重复请求错误才显示错误消息（业务侧声明 skipErrorMessage 时静默）
      ElMessage({
        message: error.message || '网络错误',
        type: 'error',
        duration: 5 * 1000
      })
    }
    
    return Promise.reject(error)
  }
)

export default service
