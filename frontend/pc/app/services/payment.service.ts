import type { ApiResponse } from "~/types/api"
import { getToken } from "~/utils/auth"
import { getSafeRedirectUrl } from "~/utils/safeRedirectUrl"
import type {
    BalanceDisplayParams,
    AccountBalanceDisplayVO,
    EvokePaymentParams,
    PlusMemberPayParams
} from "~/types/payment"

const { post } = useRequest()

export const balanceDisplay = async (params: BalanceDisplayParams): Promise<ApiResponse<AccountBalanceDisplayVO>> => {
    return await post<AccountBalanceDisplayVO>("/mall-payment/payment/userResource/balanceDisplay", params)
}

export const checkPassword = async (): Promise<ApiResponse<number>> => {
    return await post<number>("/mall-payment/pay/userBase/checkPayPassword", {})
}

export const updatePaymentPassword = async (params: { passwd: string }): Promise<ApiResponse<unknown>> => {
    return await post("/mall-payment/pay/userBase/updatePaymentPassword", params)
}

/** Legacy entrance (余额/卡/账期/通联等仍走此口) */
export const evokePayment = async (params: EvokePaymentParams): Promise<ApiResponse<any>> => {
    return await post("/mall-payment/pay/entrance/userPay", params)
}

export const plusMemberPay = async (params: PlusMemberPayParams): Promise<ApiResponse<any>> => {
    return await post("/mall-payment/pay/userBase/plusMemberpay", params)
}

/** PayPal 支付：POST 请求 mall-payment/pay，参数在 URL 上，code===1 时跳转 data */
export const paypalPay = async (params: { currencyCode: string; money: number; orderId: string; url: string }) => {
    const token = getToken()
    const response = await post<string>("/mall-payment/pay", undefined, {
        params: {
            currencyCode: params.currencyCode,
            money: params.money,
            orderId: params.orderId,
            url: params.url,
            ...(token ? { token } : {})
        }
    })
    if (response.code === 1 && response.data && typeof window !== "undefined") {
        const safe = getSafeRedirectUrl(response.data)
        if (safe) window.location.href = safe
    }
    return response
}

/** ========== 统一收银台（BFF /web/pay/cashier/* → payment /api/payment/cashier/*） ========== */

export interface CashierCreateParams {
    terminal: string
    payMethod: string
    amount: number
    mallOrderNo?: string
    mchOrderNo?: string
    requestNo?: string
    subject?: string
    body?: string
    currency?: string
    openId?: string
    channelCode?: string
    notifyUrl?: string
    returnUrl?: string
    extParam?: string
    clientIp?: string
}

export interface CashierCreateResult {
    cashierNo: string
    mchOrderNo?: string
    payOrderId?: string
    payMethod?: string
    terminal?: string
    amount?: number
    status?: number
    payData?: Record<string, any>
    expireTime?: number
}

export interface CashierStatusResult {
    cashierNo: string
    payOrderId?: string
    mchOrderNo?: string
    payMethod?: string
    terminal?: string
    amount?: number
    status?: number
    statusDesc?: string
    statusCode?: string
    successTime?: string
    errCode?: string
    errMsg?: string
}

export const cashierCreate = async (params: CashierCreateParams): Promise<ApiResponse<CashierCreateResult>> => {
    return await post<CashierCreateResult>("/web/pay/cashier/create", params)
}

export const cashierStatus = async (params: { cashierNo: string }): Promise<ApiResponse<CashierStatusResult>> => {
    return await post<CashierStatusResult>("/web/pay/cashier/status", params)
}

export const cashierClose = async (params: { cashierNo: string }): Promise<ApiResponse<CashierStatusResult>> => {
    return await post<CashierStatusResult>("/web/pay/cashier/close", params)
}

/** @deprecated 保留兼容；PC 微信/支付宝轮询请改用 cashierStatus */
export const queryWxPayStatus = async (params: { orderId: string }) => {
    return await post<number>("/mall-payment/pay/WXPay/queryWxPayStatus", params)
}

/** @deprecated 保留兼容；PC Stripe 请改用 cashierCreate(payMethod=STRIPE) */
export const stripePay = async (params: { orderId: string; currencyCode: string; type: number }) => {
    const response = await post<string>("/mall-payment/stripe/pay", params)
    if (response.code === 1 && response.data && typeof window !== "undefined") {
        const safe = getSafeRedirectUrl(response.data)
        if (safe) window.location.href = safe
    }
    return response
}
