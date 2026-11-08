import { defineStore } from 'pinia'
import type { UserInfo } from '~/types/user'
import type { UserState } from '~/types/store'
import { passwordLogin, codeLogin, loginOut,queryCheckAuthorizationStatus } from '~/services/authServer.service'
import { getUserInfo } from '~/services/userms.service'
import { setToken, deleteToken, getToken } from '~/utils/auth'
import { usePlusStore } from '~/stores/modules/plus'

/** getuser 常不带积分或返回 0，刷新时避免覆盖积分页已同步的 userIntegral */
function pickUserIntegral(incoming: unknown, previous: unknown): number {
  const next = incoming == null || incoming === '' ? NaN : Number(incoming)
  const prev = previous == null || previous === '' ? NaN : Number(previous)
  if (Number.isFinite(next) && next !== 0) return next
  if (Number.isFinite(prev)) return prev
  return Number.isFinite(next) ? next : 0
}

export const useUserStore = defineStore('user', {
  // 状态
  state: (): UserState => ({
    userInfo: null,
    token: null,
    isLogin: false
  }),

  // 计算属性
  getters: {
    // 获取用户信息
    getUserInfo: (state) => state.userInfo,
    // 获取登录状态
    getIsLogin: (state) => state.isLogin,
    // 获取令牌
    getToken: (state) => state.token
  },

  // 操作方法
  actions: {
    // 设置用户信息
    setUserInfo(userInfo: UserInfo) {
      this.userInfo = userInfo
      this.isLogin = true
    },

    // 设置令牌
    setToken(token: string) {
      this.token = token
    },

    /**
     * 从持久化 token 恢复前端登录态。
     * 静态首页或浏览器从历史记录恢复时，Pinia 的 isLogin 可能仍是初始值，
     * 但 token cookie 已经存在；此时应立即恢复状态，避免顶部导航误显示“登录”。
     */
    restoreLoginState() {
      const token = this.token || (typeof window !== 'undefined' ? getToken() : null)
      if (!token) return false
      this.token = token
      if (!this.isLogin) {
        this.setUserInfo({
          ...(this.userInfo || {}),
          token
        } as UserInfo)
      }
      return true
    },

    /**
     * 完成登录状态初始化，并尽力补全用户资料。
     * 登录接口已经返回有效 token 时，资料接口暂时失败也不能把用户留在未登录状态。
     */
    async finishLogin(token: string, loginData: Partial<UserInfo> = {}) {
      // 先建立基础用户状态，确保 Header 等依赖 isLogin 的组件立即响应。
      this.setToken(token)
      this.setUserInfo({
        ...loginData,
        token
      } as UserInfo)

      try {
        const userInfoResponse = await getUserInfo(token)
        if (userInfoResponse.code === 1 && userInfoResponse.data) {
          const userInfo = {
            ...this.userInfo,
            ...userInfoResponse.data,
            token
          } as UserInfo
          this.setUserInfo(userInfo)
          return { success: true, data: userInfo }
        }

        console.error('获取用户信息失败:', userInfoResponse.message)
        return {
          success: true,
          data: this.userInfo,
          message: '登录成功，但获取用户信息失败'
        }
      } catch (userInfoError) {
        console.error('获取用户信息失败:', userInfoError)
        return {
          success: true,
          data: this.userInfo,
          message: '登录成功，但获取用户信息失败'
        }
      } finally {
        // 用户信息接口异常不能回滚已经成功的登录结果。
        // 某些旧商家资料为空时会返回 500/业务异常，但 token 仍然有效。
        if (token && !this.isLogin) {
          this.setUserInfo({
            ...(loginData || {}),
            ...(this.userInfo || {}),
            token
          } as UserInfo)
        }
      }
    },

    // 清除用户信息
    clearUserInfo() {
      this.userInfo = null
      this.token = null
      this.isLogin = false
      usePlusStore().clearUserMember()
    },

    /**
     * 密码登录
     *
     * @param userMobile 手机号（提交至 /mall-auth-server/user/login）
     * @param password 密码
     * @param loginPlatform 登录平台，默认为 1
     * @returns 登录结果
     */
    async loginByPassword(userMobile: string, password: string, loginPlatform: number = 1, imgToken: string = '') {
      try {
        const response = await passwordLogin(userMobile, password, loginPlatform , imgToken)

        if (response.code === 1 && response.data) {
          const loginData = response.data
          const token = loginData.token

          // 只保存 token 到 cookie
          if (token) {
            setToken(token)
          }

          if (!token) {
            return { success: false, message: '登录响应缺少 token' }
          }

          // 先保存基础登录状态，再异步补全用户信息
          return await this.finishLogin(token, loginData)

        } else {
          return { success: false, message: response.message || '登录失败' }
        }
      } catch (error: any) {
        console.error('密码登录失败:', error)
        return { success: false, message: error.message || '登录失败，请稍后重试' }
      }
    },

    /**
     * 验证码登录
     * 
     * @param userMobile 手机号
     * @param phoneCode 验证码
     * @param platForm 平台，默认为 1
     * @param interDialingCode 国际区号，默认为 +86
     * @returns 登录结果
     */
    async loginByCode(userMobile: string, phoneCode: string, platForm: number = 1, interDialingCode: string = '+86') {
      try {
        const response = await codeLogin(userMobile, phoneCode, platForm, interDialingCode)

        if (response.code === 1 && response.data) {
          const loginData = response.data
          const token = loginData.token

          // 只保存 token 到 cookie
          if (token) {
            setToken(token)
          }

          if (!token) {
            return { success: false, message: '登录响应缺少 token' }
          }

          // 先保存基础登录状态，再异步补全用户信息
          return await this.finishLogin(token, loginData)

        } else {
          return { success: false, message: response.message || '登录失败' }
        }
      } catch (error: any) {
        console.error('验证码登录失败:', error)
        return { success: false, message: error.message || '登录失败，请稍后重试' }
      }
    },

     /**
     * 授权登录
     * @param ticket  
     * @returns 登录结果
     */
    async loginByScan(ticket: string) {
      try {
        const response = await queryCheckAuthorizationStatus({ ticket })

        if (response.code === 1 && response.data) {
          const loginData = response.data
          const token = loginData.token

          // 只保存 token 到 cookie
          if (token && loginData.status == 'success') {
            setToken(token)
          } else {
            return { success: false, data: loginData }
          }

          // 只保存 token 到 store，不保存用户信息
          return await this.finishLogin(token, loginData)

        } else {
          return { success: false, message: response.message || '登录失败' }
        }
      } catch (error: any) {
        console.error('验证码登录失败:', error)
        return { success: false, message: error.message || '登录失败，请稍后重试' }
      }
    },

    /**
     * 退出登录
     * 
     * 调用退出登录接口，清除本地用户信息和 token
     */
    async logout() {
      const token = this.token || (typeof window !== 'undefined' ? getToken() : null)

      try {
        // 如果有 token，调用退出登录接口
        if (token) {
          await loginOut(token)
        }
      } catch (error) {
        console.error('退出登录接口调用失败:', error)
        // 即使接口调用失败，也清除本地数据
      } finally {
        // 清除本地用户信息和 token
        this.clearUserInfo()
        deleteToken()
      }
    },

    // 更新用户信息
    updateUserInfo(updates: Partial<UserInfo>) {
      if (!this.userInfo) return
      this.$patch({
        userInfo: {
          ...this.userInfo,
          ...updates
        }
      })
    },

    /**
     * 从服务端刷新用户信息（含积分等）
     * 用于页面展示需要最新用户数据的场景
     */
    async refreshUserInfo() {
      const token = this.token || (typeof window !== 'undefined' ? getToken() : null)
      if (!token) {
        return
      }

      try {
        const userInfoResponse = await getUserInfo(token)
        if (userInfoResponse.code === 1 && userInfoResponse.data) {
          const previousIntegral = this.userInfo?.userIntegral
          const incomingIntegral = userInfoResponse.data.userIntegral
          this.setUserInfo({
            ...this.userInfo,
            ...userInfoResponse.data,
            token,
            userIntegral: pickUserIntegral(incomingIntegral, previousIntegral)
          })
        }
      } catch (error) {
        console.error('刷新用户信息失败:', error)
      }
    }
  },

  persist: {
    key: 'user-state',
    storage: typeof window !== 'undefined' ? window.localStorage : undefined,
    // 只持久化 token：isLogin/userInfo 若一并还原，会在 hydration 前改掉 Header，导致 mismatch
    paths: ['token']
  } as any
})
