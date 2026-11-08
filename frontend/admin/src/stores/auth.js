import { defineStore } from 'pinia'
import { getToken, setAuthInfo, clearAuthInfo, getUserId, normalizeUserIdInUserInfo } from '@/utils/auth'
import { authService } from '@/api/auth/auth.js'
import { useAppStore } from '@/stores/app'
import { invalidateUserMenusCache, refreshPermissionsFromServer } from '@/utils/userMenusCache'
import { syncCurrentOrgFromUserInfo, userHasOrganization, mergeOrgListIntoUserInfo, userOrgStatusUnknown } from '@/utils/orgUtils'

/** 登录成功后拉取最新侧栏菜单（避免 sessionStorage 旧缓存缺项） */
async function refreshMenusAfterLogin() {
  try {
    const appStore = useAppStore()
    invalidateUserMenusCache()
    appStore.clearMenus({ skipInvalidate: true })
    await refreshPermissionsFromServer(true)
    await appStore.fetchMenus(true)
  } catch (e) {
    console.warn('[AuthStore] refresh menus after login:', e)
  }
}

/**
 * 认证状态管理Store
 * 
 * 【核心原则】
 * 1. 单一数据源：auth store是用户信息的唯一来源
 * 2. 登录时获取：只在登录成功时调用getUserInfo() API
 * 3. 本地恢复：页面刷新时从localStorage恢复，不调用API
 * 4. 延迟验证：token验证由API请求时的响应拦截器处理
 * 
 * 【使用场景】
 * - 登录成功：调用usernameLogin/phoneLogin，自动保存userInfo
 * - 应用启动：调用initAuth()从localStorage恢复
 * - 组件使用：直接使用authStore.userInfo，不再调用getUserInfo()
 * - token过期：由响应拦截器自动处理，跳转登录页
 * 
 * 【禁止行为】
 * - 禁止在main.js中调用getUserInfo()
 * - 禁止在routeGuard中调用getUserInfo()
 * - 禁止在Layout/组件中调用getUserInfo()
 * - 只有登录成功后才能调用getUserInfo()
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken() || '',
    userInfo: null,  // 会在initAuth中从localStorage恢复
    isLoggedIn: false  // 会在initAuth中根据token设置
  }),

  getters: {
    isAuthenticated: (state) => !!state.token
  },

  actions: {
    /**
     * 初始化认证状态
     * 【重要】该方法只从localStorage恢复数据，不调用API
     * 
     * 调用时机：应用启动时在main.js中调用
     * 
     * @returns {void}
     */
    initAuth() {
      const token = getToken()
        
      if (token) {
        this.token = token
        this.isLoggedIn = true
          
        // 尝试从localStorage获取用户信息
        try {
          const userInfoStr = localStorage.getItem('userInfo')
                
          if (userInfoStr) {
            this.userInfo = JSON.parse(userInfoStr)
            if (!this.userInfo.orgList && this.userInfo.organizationList) {
              this.userInfo.orgList = this.userInfo.organizationList
            }
            normalizeUserIdInUserInfo(this.userInfo)
            // 页面刷新时保留用户已选择且仍有权限的组织；非法或残留组织仍会回退到主组织。
            syncCurrentOrgFromUserInfo(this.userInfo)
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
            // 刷新后 orgList 可能缺失，后台补全（不阻塞启动）
            if (userOrgStatusUnknown(this.userInfo)) {
              this.ensureOrganizationLoaded(this.userInfo).catch((e) => {
                console.warn('[AuthStore] initAuth 组织补全失败:', e?.message)
              })
            }
          } else {
            console.warn('[AuthStore] localStorage中没有用户信息，可能需要重新登录')
          }
        } catch (error) {
          console.error('[AuthStore] 解析用户信息失败:', error)
          // 解析失败时清理损坏的数据
          localStorage.removeItem('userInfo')
        }
      } else {
        // 未登录
      }
    },

    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      this.isLoggedIn = true
      // 同步保存到localStorage，确保刷新页面后不丢失
      if (userInfo) {
        if (!userInfo.orgList && userInfo.organizationList) {
          userInfo.orgList = userInfo.organizationList
        }
        normalizeUserIdInUserInfo(userInfo)
        // 登录后强制覆盖本地组织，避免上一账号残留 org 串到财务等接口
        syncCurrentOrgFromUserInfo(userInfo, true)
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        // 如果有token，也保存
        if (userInfo.token) {
          localStorage.setItem('token', userInfo.token)
          this.token = userInfo.token
        }
      }
    },

    /**
     * 判断用户是否已绑定组织
     * @param {Object|null} userInfo 用户信息对象
     * @returns {boolean} 是否已绑定组织
     */
    hasBoundOrganization(userInfo) {
      return userHasOrganization(userInfo)
    },

    /**
     * orgList 未加载时补全：JWT 解析 userId + getUserOrganizations 兜底
     * @param {Object|null} userInfo
     * @returns {Promise<Object|null>}
     */
    async ensureOrganizationLoaded(userInfo) {
      let info = userInfo || this.userInfo
      if (!info) {
        try {
          const raw = localStorage.getItem('userInfo')
          if (raw) info = JSON.parse(raw)
        } catch {
          /* ignore */
        }
      }
      if (!info) {
        return null
      }

      normalizeUserIdInUserInfo(info)
      this.userInfo = info

      if (userHasOrganization(info)) {
        localStorage.setItem('userInfo', JSON.stringify(info))
        return info
      }

      if (userOrgStatusUnknown(info) || !userHasOrganization(info)) {
        info = await this.mergeOrgListFromFallback(info)
      }

      return info
    },

    /**
     * queryUserInfo 未带回 orgList 时，从 hou_user_organization 兜底补全
     */
    async mergeOrgListFromFallback(userInfo) {
      let info = userInfo || this.userInfo
      if (!info) {
        try {
          const raw = localStorage.getItem('userInfo')
          if (raw) info = JSON.parse(raw)
        } catch {
          /* ignore */
        }
      }
      if (!info || userHasOrganization(info)) {
        return info
      }

      const userId = info.userId ?? info.user_id ?? getUserId()
      if (!userId) {
        return info
      }

      try {
        const orgResponse = await authService.getUserOrganizations(String(userId))
          if (orgResponse?.code === 1 || orgResponse?.code === 200) {
          const orgList = orgResponse.data
          if (Array.isArray(orgList) && orgList.length > 0) {
            mergeOrgListIntoUserInfo(info, orgList)
            this.userInfo = info
            localStorage.setItem('userInfo', JSON.stringify(info))
            console.info('[AuthStore] 已从 getUserOrganizations 补全组织信息', { count: orgList.length })
          } else if (Array.isArray(orgList) && orgList.length === 0) {
            // 明确无组织，避免 RouteGuard 一直当作 unknown
            info.orgList = []
            if (!info.organizationList) {
              info.organizationList = []
            }
            this.userInfo = info
            localStorage.setItem('userInfo', JSON.stringify(info))
          }
        }
      } catch (e) {
        console.warn('[AuthStore] 组织信息兜底查询失败:', e?.message)
      }

      return info
    },

    /**
     * 是否开启多组织模式（默认开启）
     * @returns {boolean} true=开启，false=关闭
     */
    isMultiOrganizationEnabled() {
      return localStorage.getItem('multiOrganizationEnabled') !== '0'
    },

    /**
     * 获取用户信息
     * 【重要】该方法只应在登录成功后调用，不应该在其他地方调用
     * 
     * 合法调用场景：
     * 1. Login.vue中登录成功后
     * 2. 手动刷新用户信息（如修改个人资料后）
     * 
     * 禁止调用场景：
     * 1. main.js应用启动时
     * 2. routeGuard路由守卫中
     * 3. Layout/组件生命周期中
     * 
     * @returns {Promise<Object>} 用户信息响应
     * @throws {Error} 当API调用失败时
     */
    async getUserInfo() {
      try {
        // 调用获取用户信息的API
        const response = await authService.getUserInfo()
        
        // 检查响应状态
        if (response.code === 1 || response.code === 200) {
          // 保存用户信息
          this.userInfo = response.data
          this.isLoggedIn = true
          // 同时更新localStorage中的用户信息
          if (response.data) {
            if (!response.data.orgList && response.data.organizationList) {
              response.data.orgList = response.data.organizationList
            }
            syncCurrentOrgFromUserInfo(response.data, true)
            await this.mergeOrgListFromFallback(response.data)
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          }
          return response
        } else if (response.code === 0) {
          // code === 0 表示失败，可能是未登录或其他错误
          const errorMsg = response.message || '获取用户信息失败'
          console.warn('[AuthStore] 获取用户信息失败:', errorMsg)
          
          // 如果是未登录相关的错误，清除认证信息
          if (errorMsg.includes('用户未登录') || errorMsg.includes('未登录') || errorMsg.includes('登录')) {
            this.logout()
          }
          
          throw new Error(errorMsg)
        } else {
          // 其他错误
          throw new Error(response.message || '获取用户信息失败')
        }
      } catch (error) {
        console.error('[AuthStore] 获取用户信息异常:', error)

        // queryUserInfo 失败时尝试从 hou_user_organization 补组织，避免误判未绑定
        try {
          await this.mergeOrgListFromFallback(this.userInfo)
          if (userHasOrganization(this.userInfo)) {
            return { code: 1, data: this.userInfo }
          }
        } catch {
          /* ignore */
        }
        
        // 如果是401错误（未授权）或"用户未登录"错误，清除认证信息
        if (error.response && error.response.status === 401) {
          this.logout()
        } else if (error.message && (error.message.includes('用户未登录') || error.message.includes('未登录'))) {
          this.logout()
        }
        
        throw error
      }
    },

    // 用户名密码登录（captcha: { code, sign } 图形验证码，新版 auth 强制校验）
    async usernameLogin(username, password, captcha) {
      try {
        // 调用真实的登录API
        const response = await authService.usernameLogin(username, password, captcha)
        
        if (response.code === 1) {
          // 保存认证信息（auth服务登录返回token字段）
          const token = response.data.token
          setAuthInfo(token, response.data)
          this.token = token
          this.isLoggedIn = true
          
          // 【重要】登录成功后立即获取完整的用户信息（包括org_id和orgList）
          try {
            const userInfoResponse = await this.getUserInfo()
            if (userInfoResponse && (userInfoResponse.code === 1 || userInfoResponse.code === 200)) {
              // getUserInfo已经更新了this.userInfo和localStorage
            }
          } catch (userInfoError) {
            console.error('[AuthStore] 登录后获取用户信息失败:', userInfoError)
            await this.mergeOrgListFromFallback()
          }
          
          // 仅在用户已绑定组织时获取权限，避免跳转绑定组织页时请求被取消
          try {
            if (!this.isMultiOrganizationEnabled() || this.hasBoundOrganization(this.userInfo)) {
              const permissionResponse = await authService.getUserPermissions()
              if (permissionResponse.code === 1) {
                sessionStorage.setItem('permissions', JSON.stringify(permissionResponse.data))
                localStorage.removeItem('permissions')
              }
            } else {
              sessionStorage.removeItem('permissions')
              localStorage.removeItem('permissions')
            }
          } catch (permissionError) {
            console.error('获取用户权限失败:', permissionError)
          }

          if (!this.isMultiOrganizationEnabled() || this.hasBoundOrganization(this.userInfo)) {
            await refreshMenusAfterLogin()
          }
        }
        
        return response
      } catch (error) {
        console.error('用户名密码登录失败:', error)
        throw error
      }
    },

    // 手机号登录
    async phoneLogin(phone, code) {
      try {
        // 调用真实的手机号登录API
        const response = await authService.phoneLogin(phone, code)
        
        if (response.code === 1) {
          // 保存认证信息（登录接口只返回token）
          setAuthInfo(response.data.token, response.data)
          this.token = response.data.token
          this.isLoggedIn = true
          
          // 【重要】登录成功后立即获取完整的用户信息（包括org_id和orgList）
          try {
            const userInfoResponse = await this.getUserInfo()
            if (userInfoResponse && (userInfoResponse.code === 1 || userInfoResponse.code === 200)) {
              // getUserInfo已经更新了this.userInfo和localStorage
            }
          } catch (userInfoError) {
            console.error('[AuthStore] 登录后获取用户信息失败:', userInfoError)
            await this.mergeOrgListFromFallback()
          }
          
          // 仅在用户已绑定组织时获取权限，避免跳转绑定组织页时请求被取消
          try {
            if (!this.isMultiOrganizationEnabled() || this.hasBoundOrganization(this.userInfo)) {
              const permissionResponse = await authService.getUserPermissions()
              if (permissionResponse.code === 1) {
                sessionStorage.setItem('permissions', JSON.stringify(permissionResponse.data))
                localStorage.removeItem('permissions')
              }
            } else {
              sessionStorage.removeItem('permissions')
              localStorage.removeItem('permissions')
            }
          } catch (permissionError) {
            console.error('获取用户权限失败:', permissionError)
          }

          if (!this.isMultiOrganizationEnabled() || this.hasBoundOrganization(this.userInfo)) {
            await refreshMenusAfterLogin()
          }
        }
        
        return response
      } catch (error) {
        console.error('手机号登录失败:', error)
        throw error
      }
    },

    // 获取微信二维码
    async getWechatQRCode() {
      try {
        // 调用真实的获取微信二维码API
        const response = await authService.getWechatQRCode()
        return response
      } catch (error) {
        console.error('获取微信二维码失败:', error)
        throw error
      }
    },

    // 微信登录 - 授权回调（code 换登录态）
    async wechatCallback(code, state) {
      try {
        const response = await authService.wechatCallback(code, state)
        return response
      } catch (error) {
        console.error('微信登录回调失败:', error)
        throw error
      }
    },

    // 检查微信登录状态
    async checkWechatLoginStatus(qrCodeId) {
      try {
        // 调用真实的检查微信登录状态API
        const response = await authService.checkWechatLoginStatus(qrCodeId)
        return response
      } catch (error) {
        console.error('检查微信登录状态失败:', error)
        throw error
      }
    },

    // 取消微信登录
    async cancelWechatLogin(qrCodeId) {
      try {
        // 调用真实的取消微信登录API
        const response = await authService.cancelWechatLogin(qrCodeId)
        return response
      } catch (error) {
        console.error('取消微信登录失败:', error)
        throw error
      }
    },

    // 发送验证码
    async sendCode(contact, type) {
      try {
        // 调用真实的发送验证码API
        const response = await authService.sendCode(contact, type)
        // 返回完整的响应，包括错误信息
        return response
      } catch (error) {
        console.error('发送验证码失败:', error)
        // 向上抛出错误，保留完整的错误信息
        throw error
      }
    },

    // 发送重置密码验证码
    async sendResetCode(userMobile) {
      return authService.sendResetCode(userMobile, 'forgot')
    },

    // 忘记密码
    async forgotPassword(userMobile, phoneCode, newPassword) {
      try {
        const response = await authService.forgotPassword(userMobile, phoneCode, newPassword)
        return response
      } catch (error) {
        console.error('重置密码失败:', error)
        throw error
      }
    },

    // 用户注册
    async register(userData) {
      try {
        // 调用真实的用户注册API
        const response = await authService.register(userData)
        return response
      } catch (error) {
        console.error('用户注册失败:', error)
        throw error
      }
    },

    // 退出登录
    logout() {
      clearAuthInfo()
      localStorage.removeItem('permissions')
      localStorage.removeItem('roles')
      this.token = ''
      this.userInfo = null
      this.isLoggedIn = false
      try {
        const appStore = useAppStore()
        appStore.clearMenus()
      } catch (e) {
        console.warn('[AuthStore] clearMenus on logout:', e)
      }
      // T10：卸载插件动态路由与菜单（异步执行，失败不阻塞登出）
      import('@/stores/plugin')
        .then(({ usePluginStore }) => usePluginStore().clearPlugins())
        .catch((e) => console.warn('[AuthStore] clearPlugins on logout:', e?.message))
      // 站点身份也要清：否则下一个账号登录前会读到上一个账号的 instanceId 用于展示
      import('@/utils/site')
        .then(({ clearSiteIdentity }) => clearSiteIdentity())
        .catch(() => {})
    }
  }
})
