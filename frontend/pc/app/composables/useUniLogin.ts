/**
 * 统一登录（UniLogin）挂载能力 —— 商城前台（PC 买家端）
 *
 * 与后台 admin 共用同一套后端能力（/web/auth/unilogin/{channel}/**）与同一张用户表，
 * 但 UI 层不复用 admin 的插件产物：admin 用 Element Plus，商城前台用 Nuxt UI，
 * 组件产物无法直接跨框架渲染。因此这里只复用「协议与渠道清单」：
 *
 *   - 渠道清单来自后端 /web/auth/unilogin/channels，新增渠道不需要改前端；
 *   - 未绑定时走 /auth/unilogin/{channel}/bind-login（手机号 + 验证码 → 登录或建号并绑定）。
 *
 * 登录页只放一个挂载点，不写死任何渠道。
 */

import { useRequest } from '~/composables/useRequest'
import { getVerificationCode } from '~/services/thirdParty.service'

export interface UniLoginChannel {
    code: string
    name: string
    enabled: boolean
}

/** 取业务体：request 拦截器已剥掉 HTTP 外壳，这里再剥一层 data */
const unwrap = (response: any) => {
    if (response && typeof response === 'object' && 'data' in response) {
        return response.data ?? response
    }
    return response ?? null
}

export const useUniLogin = () => {
    const { get, post } = useRequest()

    /** 渠道概况（编码/名称/是否已配置凭据）；失败返回空数组，登录页入口区自动隐藏 */
    const loadChannels = async (): Promise<UniLoginChannel[]> => {
        try {
            const response = await get<any>('/web/auth/unilogin/channels', { skipToken: true })
            const list = unwrap(response)
            const items = Array.isArray(list) ? list : (Array.isArray(list?.data) ? list.data : null)
            if (!items) return []
            return items.filter((item: any) => item && item.code)
        } catch (error) {
            console.warn('[uni-login] 渠道清单获取失败，已跳过第三方登录入口', error)
            return []
        }
    }

    /** 取扫码地址：enabled !== true 表示服务端还没配该渠道凭据 */
    const fetchQrcode = async (channel: string) => {
        const response = await post<any>(`/web/auth/unilogin/${channel}/qrcode`, {}, { skipToken: true })
        return unwrap(response)
    }

    /** 轮询扫码状态：waiting / scanned / confirmed / unbound / expired */
    const fetchStatus = async (channel: string, qrCodeId: string) => {
        const response = await get<any>(`/web/auth/unilogin/${channel}/login-status/${qrCodeId}`, { skipToken: true })
        return unwrap(response)
    }

    /** 授权平台回跳后，用 code + state 完成本次登录会话 */
    const completeCallback = async (channel: string, code: string, state: string) => {
        const response = await get<any>(`/web/auth/unilogin/${channel}/callback`, {
            params: { code, state },
            skipToken: true
        })
        return unwrap(response)
    }

    /** 取消本次扫码（关闭弹窗时调用，避免留下无人认领的会话） */
    const cancelLogin = async (channel: string, qrCodeId: string) => {
        try {
            await post(`/web/auth/unilogin/${channel}/cancel-login/${qrCodeId}`, {}, { skipToken: true })
        } catch (error) {
            // 取消失败不影响前端流程
        }
    }

    /**
     * 扫码后绑定：手机号已注册则登录并写入绑定，未注册则先自动建号再绑定。
     * 会员因此不需要先有账号，首次扫码即可开通。
     */
    const bindLogin = async (channel: string, qrCodeId: string, userMobile: string, phoneCode: string) => {
        const response = await post<any>(`/web/auth/unilogin/${channel}/bind-login`, {
            qrCodeId,
            userMobile,
            phoneCode
        }, { skipToken: true })
        return unwrap(response)
    }

    /** 发送短信验证码（复用商城既有通道，与登录/注册共用同一份 Redis 验证码） */
    const sendSmsCode = async (userMobile: string, interDialingCode = '+86') => {
        return await getVerificationCode(userMobile, interDialingCode, 1)
    }

    return { loadChannels, fetchQrcode, fetchStatus, completeCallback, cancelLogin, bindLogin, sendSmsCode }
}
