/**
 * 认证相关的 composable
 * 
 * 提供登录、退出登录、登录状态检查等认证相关的功能
 * 
 * @example
 * // 检查登录状态
 * const { isLogin } = useAuth()
 * if (!isLogin()) {
 *   // 已自动跳转到登录页
 * }
 * 
 * @example
 * // 密码登录
 * const { loginByPassword } = useAuth()
 * const result = await loginByPassword('13800138000', 'password')
 * 
 * @example
 * // 验证码登录
 * const { loginByCode } = useAuth()
 * const result = await loginByCode('13800138000', '123456')
 * 
 * @example
 * // 退出登录
 * const { logout } = useAuth()
 * await logout()
 */

import { useUserStore } from '~/stores/modules/user'
import { Toast } from '~/composables/useToast'
import { ucenterAuthUrl } from '~/utils/ucenterAuthUrl'

/**
 * 认证相关的 composable
 * 
 * @returns 认证相关的方法和状态
 */
export const useAuth = () => {
    const userStore = useUserStore()
    /**
     * 检查用户是否已登录
     * 如果未登录，则跳转到登录页面
     * 
     * @param redirectTo 登录后重定向的路径（可选，默认不重定向）
     * @returns 返回 true 表示已登录，false 表示未登录（已跳转）
     */
    const isLogin = (redirectTo?: string): boolean => {
        // 检查是否已登录
        const loginStatus = userStore.isLogin

        // 如果未登录
        if (!loginStatus) {
            navigateTo(ucenterAuthUrl('login', redirectTo), { external: true })
            return false
        }

        return true
    }

    /**
     * 密码登录
     *
     * @param userMobile 手机号
     * @param password 密码
     * @param loginPlatform 登录平台，默认为 1
     * @param showToast 是否显示提示信息，默认为 true
     * @returns 登录结果
     */
    const loginByPassword = async (
        userMobile: string,
        password: string,
        imgToken: string = '',
        loginPlatform: number = 1,
        showToast: boolean = true
    ) => {
        try {
            const result = await userStore.loginByPassword(userMobile, password, loginPlatform, imgToken)

            if (result.success) {
                if (showToast) {
                    Toast.success('登录成功')
                }
                return result
            } else {
                if (showToast) {
                    Toast.error(result.message || '登录失败')
                }
                return result
            }
        } catch (error: any) {
            const message = error.message || '登录失败，请稍后重试'
            if (showToast) {
                Toast.error(message)
            }
            return { success: false, message }
        }
    }

    /**
     * 验证码登录
     * 
     * @param userMobile 手机号
     * @param phoneCode 验证码
     * @param platForm 平台，默认为 1
     * @param interDialingCode 国际区号，默认为 +86
     * @param showToast 是否显示提示信息，默认为 true
     * @returns 登录结果
     */
    const loginByCode = async (
        userMobile: string,
        phoneCode: string,
        platForm: number = 1,
        interDialingCode: string = '+86',
        showToast: boolean = true
    ) => {
        try {
            const result = await userStore.loginByCode(userMobile, phoneCode, platForm, interDialingCode)

            if (result.success) {
                if (showToast) {
                    Toast.success('登录成功')
                }
                return result
            } else {
                if (showToast) {
                    Toast.error(result.message || '登录失败')
                }
                return result
            }
        } catch (error: any) {
            const message = error.message || '登录失败，请稍后重试'
            if (showToast) {
                Toast.error(message)
            }
            return { success: false, message }
        }
    }

    /**
     * 授权登录
     * @param ticket
     * @param showToast 是否显示提示信息，默认为 true
     * @param redirectToLogin 是否跳转到登录页，默认为 true
     */
    const loginByScan = async (ticket: string, showToast: boolean = true) => {
        try {
            // 调用 store 的退出登录方法
            const result = await userStore.loginByScan(ticket)

            if (result.success) {
                if (showToast) {
                    Toast.success('登录成功')
                }
                return result
            } else {
                if (showToast) {
                    // Toast.error(result.message || '登录失败')
                }
                return result
            }
        } catch (error: any) {
            const message = error.message || '登录失败，请稍后重试'
            if (showToast) {
                Toast.error(message)
            }
            return { success: false, message }
        }
    }

    /**
     * 退出登录
     * 
     * 调用退出登录接口，清除本地用户信息和 token，并跳转到登录页
     * 
     * @param showToast 是否显示提示信息，默认为 true
     * @param redirectToLogin 是否跳转到登录页，默认为 true
     */
    const logout = async (showToast: boolean = true, redirectToLogin: boolean = true) => {
        try {
            // 调用 store 的退出登录方法
            await userStore.logout()

            if (showToast) {
                Toast.success('已退出登录')
            }

            // 跳转到登录页
            if (redirectToLogin) {
                await navigateTo(ucenterAuthUrl('login'), { external: true })
            }
        } catch (error: any) {
            console.error('退出登录失败:', error)
            if (showToast) {
                Toast.error('退出登录失败，请稍后重试')
            }
        }
    }

    return {
        /** 检查登录状态 */
        isLogin,
        /** 密码登录 */
        loginByPassword,
        /** 验证码登录 */
        loginByCode,
        /** 授权登录 */
        loginByScan,
        /** 退出登录 */
        logout,
        /** 用户 store 实例（用于访问用户信息等） */
        userStore
    }
}

/**
 * 检查用户是否已登录（快捷方法）
 * 
 * 如果未登录，则跳转到登录页面
 * 
 * @param redirectTo 登录后重定向的路径（可选，默认不重定向）
 * @returns 返回 true 表示已登录，false 表示未登录（已跳转）
 * 
 * @example
 * if (!useIsLogin()) {
 *   return // 已跳转到登录页
 * }
 */
export const useIsLogin = (redirectTo?: string): boolean => {
    const { isLogin } = useAuth()
    return isLogin(redirectTo)
}

/**
 * 退出登录（快捷方法）
 * 
 * 调用退出登录接口，清除本地用户信息和 token，并跳转到登录页
 * 
 * @example
 * await useLogout()
 */
export const useLogout = async () => {
    const { logout } = useAuth()
    await logout()
}
