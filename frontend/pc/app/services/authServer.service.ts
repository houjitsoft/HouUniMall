import type { ApiResponse } from '~/types/api'
import type { UserInfo } from '~/types/user'
import { encryptPassword } from '~/utils/JSEncrypt'

const { post, getConfig,get } = useRequest()

/** 滑动拼图验证码生成结果 */
export interface CaptchaGenerateData {
    bgImage: string
    imgToken: string
    targetX: number
    targetY: number
}

const loginRequestOptions = () => ({
    skipToken: true,
    timeout: getConfig().loginTimeout
})

/**
 * 密码登录接口
 *
 * @param userMobile 手机号（与接口字段一致）
 * @param password 密码（将使用 RSA 加密）
 * @param loginPlatform 登录平台，默认为 1
 * @returns 用户信息
 */
export const passwordLogin = async (
    userMobile: string,
    password: string,
    loginPlatform: number = 1,
    imgToken: string = ''
): Promise<ApiResponse<UserInfo>> => {
    // 加密密码
    const encryptedPassword = encryptPassword(password)

    return await post<UserInfo>('/web/mall/user/login', {
        userMobile,
        password: encryptedPassword,
        loginPlatform,
        imgToken
    }, loginRequestOptions())
}

/**
 * 验证码登录接口
 * 
 * @param userMobile 手机号
 * @param phoneCode 验证码
 * @param platForm 平台，默认为 1
 * @param interDialingCode 国际区号，默认为 +86
 * @returns 用户信息
 */
export const codeLogin = async (
    userMobile: string,
    phoneCode: string,
    platForm: number = 1,
    interDialingCode: string = '+86'
): Promise<ApiResponse<UserInfo>> => {
    return await post<UserInfo>('/web/mall/user/login', {
        userMobile,
        phoneCode,
        platForm,
        interDialingCode
    }, loginRequestOptions())
}


/**
 * 获取授权二维码
 * 
 * @returns 授权二维码
 */
export const getAuthorizationQRCode = async () => {
    return await get('/mall-auth-server/getAuthorizationQRCode')
}

/**
 * 轮询检查授权状态
 * @param params 参数
 * @returns 授权状态
 */
export const queryCheckAuthorizationStatus = async (params: { ticket:string }): Promise<ApiResponse<any>> => {
    return await get('/mall-auth-server/checkAuthorizationStatus',{params})
}

/**
 * 退出登录接口
 * 
 * @param token 用户 token
 * @returns 退出结果
 */
export const loginOut = async (token: string): Promise<ApiResponse<any>> => {
    return await post('/web/mall/user/logout', {
        token
    })
}


/**
 * 获取滑动拼图验证码（验证2）
 */
export const generateCaptcha = async (): Promise<ApiResponse<CaptchaGenerateData>> => {
    // 通过 BFF 的公开认证接口生成滑块验证码，避免调用已下线的旧别名。
    return await post<CaptchaGenerateData>('/web/auth/generate', {}, { skipToken: true })
}

/**
 * 校验滑动拼图验证码（验证2）
 * @param imgToken 验证码 token
 * @param targetX 用户拖动的横向位置
 * @param targetY 拼图纵向位置
 */
export const verifyGenerateCaptcha = async (
    imgToken: string,
    targetX: number,
    targetY: number
): Promise<ApiResponse<boolean>> => {
    return await post<boolean>('/web/auth/verifyGenerateCaptcha', {
        imgToken,
        targetX,
        targetY
    }, { skipToken: true })
}
