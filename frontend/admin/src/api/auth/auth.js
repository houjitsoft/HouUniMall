/**
 * Auth模块API接口
 * 使用主服务的统一API服务层
 */

// 导入主服务的统一API服务
import request from '@/utils/request'
import { CryptoUtil } from '@/utils/crypto.js'
import { setAuthInfo, getToken } from '@/utils/auth.js'
import {
  readUserMenusCache,
  writeUserMenusCache,
  shouldCacheMenusParams
} from '@/utils/userMenusCache'

// 创建authApi实例
const authApi = request

/**
 * Auth模块API接口
 */
export const authService = {
  // 检查手机号是否存在
  checkPhone: async (phone) => {
    try {
      const response = await authApi.post('/web/auth/check-phone', { phone })
      return response
    } catch (error) {
      console.error('检查手机号失败:', error)
      throw error
    }
  },

  // 发送验证码
  sendCode: async (userMobile, type = 'login') => {
    try {
      const response = await authApi.post('/web/auth/smscode', { userMobile, type })
      return response
    } catch (error) {
      console.error('发送验证码失败:', error)
      throw error
    }
  },

  // 手机号登录
  phoneLogin: async (phone, code) => {
    try {
      const response = await authApi.post('/web/auth/codelogin', { 
        phone, 
        code,
        platForm: 2  // 管理后台登录，平台类型为2
      })
      
      // 登录成功后存储token和用户信息
      if (response.code === 1 && response.data?.token) {
        setAuthInfo(response.data.token, response.data)
      }
      
      return response
    } catch (error) {
      console.error('手机号登录失败:', error)
      throw error
    }
  },

  // 用户名/密码登录（Cloud平台管理后台，使用auth服务）
  usernameLogin: async (username, password, captcha) => {
    try {
      // Cloud平台登录：调用 auth 服务 → hou_user_info 表
      // auth服务接收 UserInfoVO 对象，字段为 userName/userMobile + password + platForm
      // 2026-10-30：新版 auth 对 BOSS 后台登录强制图形验证码（houcloud.auth.captcha.enabled），
      // 必须带 code（用户输入）+ sign（get-verify 下发的秒级时间戳签名），否则 21002。

      const response = await authApi.post('/web/auth/login', {
        userName: username,
        userMobile: username,
        password: password,
        platForm: '2',
        code: captcha?.code || '',
        sign: captcha?.sign || '',
      })
      
      if (response.code === 1 && response.data?.token) {
        setAuthInfo(response.data.token, response.data)
      }
      
      return response
    } catch (error) {
      console.error('登录失败:', error)
      
      // 详细的错误信息
      if (error.code === 'ECONNABORTED') {
        throw new Error('请求超时，请检查网络连接')
      } else if (error.response) {
        const status = error.response.status
        const message = error.response.data?.message || error.message || '登录失败，请重试'
        if (status) {
          throw new Error(message)
        }
        throw new Error(message)
      } else if (error.request) {
        // 请求已发出但没有收到响应
        throw new Error('网络连接失败，请检查网络')
      } else {
        // 其他错误
        throw new Error(error.message || '登录失败，请重试')
      }
    }
  },

  // 获取图形验证码（登录页用；BFF POST /web/auth/get-verify → auth /platform/userinfo/getVerify）
  // 返回 data: { code: base64图片(dataURL或裸base64), sign: 秒级时间戳签名 }
  getCaptcha: async () => {
    const response = await authApi.post('/web/auth/get-verify')
    if (response.code !== 1 || !response.data?.code) {
      throw new Error(response.message || '验证码获取失败')
    }
    const img = String(response.data.code)
    return {
      image: img.startsWith('data:') ? img : `data:image/png;base64,${img}`,
      sign: response.data.sign
    }
  },

  // 用户注册
  register: async (userData) => {
    try {
      // 对密码进行RSA加密
      const encryptedUserData = {
        ...userData,
        password: CryptoUtil.encryptPassword(userData.password)
      }
      const response = await authApi.post('/web/auth/register', encryptedUserData)
      return response
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  },

  // 获取客户端区域（国内默认手机号，国外默认邮箱）
  getClientRegion: async () => {
    try {
      const response = await authApi.get('/web/auth/client-region')
      return response
    } catch (error) {
      console.error('获取客户端区域失败:', error)
      throw error
    }
  },

  // 发送重置密码验证码（手机号或邮箱，走统一 smscode 接口）
  sendResetCode: async (userMobile, type = 'forgot') => {
    try {
      const response = await authApi.post('/web/auth/smscode', { userMobile, type })
      return response
    } catch (error) {
      console.error('发送重置验证码失败:', error)
      throw error
    }
  },

  // 忘记密码（userms 契约：userMobile、phoneCode、password）
  forgotPassword: async (userMobile, phoneCode, newPassword) => {
    try {
      const encryptedPassword = CryptoUtil.encryptPassword(newPassword)
      const response = await authApi.post('/web/auth/forgot-password', {
        userMobile,
        phoneCode,
        password: encryptedPassword
      })
      return response
    } catch (error) {
      console.error('重置密码失败:', error)
      throw error
    }
  },

  // 微信登录 - 获取二维码
  getWechatQRCode: async () => {
    try {
      const response = await authApi.post('/web/auth/wechat/qrcode', {})
      return response
    } catch (error) {
      console.error('获取微信二维码失败:', error)
      throw error
    }
  },

  // 微信登录 - 授权回调（用微信下发的 code 换取登录态）
  wechatCallback: async (code, state) => {
    try {
      const response = await authApi.get('/web/auth/wechat/callback', { params: { code, state } })
      return response
    } catch (error) {
      console.error('微信登录回调失败:', error)
      throw error
    }
  },

  // 微信登录 - 检查登录状态
  checkWechatLoginStatus: async (qrCodeId) => {
    try {
      const response = await authApi.get(`/web/auth/wechat/login-status/${qrCodeId}`)
      return response
    } catch (error) {
      console.error('检查微信登录状态失败:', error)
      throw error
    }
  },

  // 微信登录 - 取消登录
  cancelWechatLogin: async (qrCodeId) => {
    try {
      const response = await authApi.post(`/web/auth/wechat/cancel-login/${qrCodeId}`)
      return response
    } catch (error) {
      console.error('取消微信登录失败:', error)
      throw error
    }
  },

  // 更新用户信息
  updateUserInfo: async (userData) => {
    try {
      const response = await authApi.put('/web/auth/user-profile', userData)
      return response
    } catch (error) {
      console.error('更新用户信息失败:', error)
      throw error
    }
  },

  // 获取用户信息
  // 单独设置35秒超时，因为该接口涉及多表关联查询，耗时较长
  getUserInfo: async () => {
    try {
      const response = await authApi.post('/web/user/queryUserInfo', {}, { timeout: 35000 })
      return response
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  },

  /** queryUserInfo 失败或 orgList 为空时，直连 hou_user_organization 查组织 */
  getUserOrganizations: async (userId) => {
    try {
      const response = await authApi.get('/web/user/organization/getUserOrganizations', {
        params: { userId }
      })
      return response
    } catch (error) {
      console.error('获取用户组织列表失败:', error)
      throw error
    }
  },

  // ========== 权限管理相关API ==========
  
  // 获取用户导航菜单（hou_permission_menu；本地缓存优先，见 userMenusCache）
  getUserMenus: async (params = {}, options = {}) => {
    const { forceRefresh = false } = options
    const requestToken = getToken()
    if (!requestToken) {
      return { code: 0, message: '未登录' }
    }
    const canCache = shouldCacheMenusParams(params)
    if (!forceRefresh && canCache) {
      const cached = readUserMenusCache(params)
      if (cached != null) {
        return { code: 1, data: cached, fromCache: true }
      }
    }
    try {
      const response = await authApi.post('/web/permission/user/menus', params)
      // 请求属于发出时的账号，不能把迟到结果缓存成刚切换的新账号菜单。
      if (getToken() !== requestToken) return { code: 0, message: '会话已切换' }
      if (
        response &&
        (response.code === 1 || response.code === 200) &&
        response.data != null &&
        canCache
      ) {
        const list = Array.isArray(response.data) ? response.data : response.data.list
        if (Array.isArray(list)) writeUserMenusCache(params, list)
      }
      return response
    } catch (error) {
      if (getToken() !== requestToken) return { code: 0, message: '会话已切换' }
      if (canCache) {
        const cached = readUserMenusCache(params)
        if (cached != null) {
          console.warn('[getUserMenus] 网络异常，使用本地菜单缓存', error?.message)
          return { code: 1, data: cached, fromCache: true, offline: true }
        }
      }
      console.error('获取用户权限菜单失败:', error)
      throw error
    }
  },

  // 检查用户权限
  checkUserPermission: async (params) => {
    try {
      const response = await authApi.post('/web/permission/user/check', params)
      return response
    } catch (error) {
      console.error('检查用户权限失败:', error)
      throw error
    }
  },

  // 获取用户权限列表
  getUserPermissions: async (orgId) => {
    try {
      const response = await authApi.post('/web/permission/user/permissions', { orgId })
      return response
    } catch (error) {
      console.error('获取用户权限列表失败:', error)
      throw error
    }
  },

  // 批量检查用户权限
  batchCheckUserPermission: async (params) => {
    try {
      const response = await authApi.post('/web/permission/user/batch-check', params)
      return response
    } catch (error) {
      console.error('批量检查用户权限失败:', error)
      throw error
    }
  }
}

// 为了保持向后兼容，单独导出权限相关函数
export const getUserMenus = authService.getUserMenus
export const checkUserPermission = authService.checkUserPermission
export const getUserPermissions = authService.getUserPermissions
export const batchCheckUserPermission = authService.batchCheckUserPermission

// 导出authApi实例
export { authApi }

export default authService
