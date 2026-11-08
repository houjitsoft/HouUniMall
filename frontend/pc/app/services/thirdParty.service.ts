import type { ApiResponse } from '~/types/api'
import { useRequest } from '~/composables/useRequest'

const { post, get } = useRequest()

/**
 * 获取验证码接口（手机号）
 * 
 * @param userMobile 手机号
 * @param interDialingCode 国际区号，默认为 +86
 * @param type 类型，默认为 1（手机号）
 * @returns 验证码信息
 */
export const getVerificationCode = async (
    userMobile: string,
    interDialingCode: string = '+86',
    type: number = 1
): Promise<ApiResponse<string>> => {
    return await post<string>('/mall-third-party/sms/code/threecheckphoneandsend', {
        userMobile,
        interDialingCode,
        type
    })
}

/**
 * 发送验证码接口
 * 
 * 发送手机号或邮箱验证码
 * @param params 请求参数
 * @param params.userMobile 手机号或邮箱
 * @param params.interDialingCode 国际区号（如 "+86"），邮箱时可为空
 * @param params.type 类型：1-手机号，2-邮箱
 * @param params.scene 业务场景：register/login/forgot（注册必须传 register，后端会做重复校验）
 * @returns 发送结果
 */
export const sendCode = async (params: {
    userMobile: string
    interDialingCode?: string
    type: 1 | 2
    scene?: 'register' | 'login' | 'forgot'
}): Promise<ApiResponse<any>> => {
    return await post('/mall-third-party/sms/sendCode', {
        userMobile: params.userMobile,
        interDialingCode: params.interDialingCode || '+86',
        type: params.type,
        ...(params.scene ? { scene: params.scene } : {})
    })
}

/**
 * 检查手机号是否已注册
 * code===1 表示可用；否则视为已注册或校验失败
 */
export const checkPhoneAvailable = async (userMobile: string): Promise<ApiResponse<any>> => {
    return await post('/web/auth/check-phone', { userMobile })
}

/**
 * 发送验证码接口（采购需求发布专用）
 * 
 * @param userMobile 手机号
 * @returns 验证码信息，data字段包含验证码数字（如888999）
 */
export const sendVerificationCode = async (userMobile: string): Promise<ApiResponse<number>> => {
    return await post<number>('/mall-third-party/sms/sendVerificationCode', {
        userMobile
    })
}

/**
 * OSS图片上传接口
 * 
 * 上传图片到OSS服务器
 * @param params 上传参数
 * @param params.base64Str base64编码的图片字符串
 * @param params.streamName 文件流名称（可选，用于指定上传后的文件名）
 * @returns 上传结果，data字段包含图片URL
 */
export const uploadImageToOSS = async (params: { base64Str: string; streamName?: string }): Promise<ApiResponse<string>> => {
    return await post<string>('/mall-third-party/web/oss/upload', params)
}

/**
 * OSS文件上传接口
 * 
 * 上传文件到OSS服务器（支持文件和图片）
 * @param formData FormData对象，包含file字段
 * @returns 上传结果，data字段包含文件URL对象 { url: string }
 */
export const uploadFileToOSS = async (formData: FormData): Promise<ApiResponse<{ url: string }>> => {
    // 使用 post 方法，FormData 会自动处理 token 和 Content-Type
    // 需要删除默认的 Content-Type，让浏览器自动设置 multipart/form-data
    return await post<{ url: string }>('/mall-third-party/web/oss/uploadFile', formData, {
        headers: {
            'Content-Type': undefined as any // 删除默认的 Content-Type，让浏览器自动设置
        }
    })
}


/**
 * 获取指定语言的语言包（JSON：中文 key -> 翻译文案）
 *
 * 优先读站点静态包；没有则返回空对象，由 baiduTrans 按需补齐。
 *
 * @param code 语言 code，如 en、ja
 * @returns 语言包对象，形如 { "我的订单": "My Orders", ... }，可能挂在 data 或直接为响应体
 */
export const getI18nPackage = async (code: string): Promise<ApiResponse<Record<string, string>> | Record<string, string>> => {
    const lang = String(code || '').trim()
    if (!lang || lang === 'cn') return {}
    try {
        const res = await get<Record<string, string>>(`/resource/i18n/i18n_${lang}.json`)
        if (res && typeof (res as any).data === 'object') return res as ApiResponse<Record<string, string>>
        if (res && typeof res === 'object' && !Array.isArray(res)) return res as any
    } catch {
        // 静态包缺失时走百度翻译
    }
    return {}
}

/** UI 语种 code → 百度翻译 API code */
const BAIDU_LANG_MAP: Record<string, string> = {
    cn: 'zh',
    zh: 'zh',
    en: 'en',
    ja: 'jp',
    jp: 'jp',
    ko: 'kor',
    kor: 'kor',
    fr: 'fra',
    fra: 'fra',
    de: 'de',
    es: 'spa',
    spa: 'spa'
}

export const toBaiduLangCode = (code: string): string => {
    const key = String(code || '').trim().toLowerCase()
    return BAIDU_LANG_MAP[key] || key
}

/**
 * 百度翻译：单条文案翻译（走 BFF /web/thirdparty，不再依赖已失效的 mall-third-party legacy）
 *
 * @param params.src 原文（中文）
 * @param params.to 目标语言 code（UI code，内部映射为百度 code）
 * @param params.from 源语言，默认 cn
 * @returns 接口返回 { code, data: { dst, src }, message }
 */
export const baiduTrans = async (params: { src: string; to: string; from?: string }): Promise<ApiResponse<{ dst: string; src: string }>> => {
    const from = toBaiduLangCode(params.from ?? 'cn')
    const to = toBaiduLangCode(params.to)
    return await post<{ dst: string; src: string }>('/web/thirdparty/baidu/trans', {
        src: params.src,
        to,
        from
    })
}

