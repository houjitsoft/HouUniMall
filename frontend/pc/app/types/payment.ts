/**
 * 支付相关类型定义
 */

/**
 * 支付方式类型
 */
export type PaymentMethod = 
    | 'balance'     // 余额支付 (1)
    | 'alipay-pc'  // 阿里支付PC (5)
    | 'wechat-pc'  // PC微信支付 (11)
    | 'card'       // 购物卡支付 (3)
    | 'integral'   // 纯积分支付 (7)
    | 'retention'  // 质保金支付 (13)
    | 'allinpay'   // 通联公对公支付 (22)

/**
 * 支付类型常量
 */
export enum PaymentType {
    ONLINE = 1  // 在线支付
}

/**
 * 支付方式枚举值映射
 */
export enum PaymentMethodCode {
    BALANCE = '1',      // 余额支付
    ALIPAY_H5 = '2',    // 阿里H5支付
    CARD = '3',         // 购物卡支付
    ALIPAY_PC = '5',    // 阿里支付PC
    WECHAT_H5 = '6',    // H5微信支付
    INTEGRAL = '7',     // 纯积分支付
    WECHAT_APPLET = '8', // 微信小程序支付
    WECHAT_APP = '9',   // APP微信支付
    ALIPAY_APP = '10',  // 阿里APP支付
    WECHAT_PC = '11',   // PC微信支付
    RETENTION = '13',   // 质保金支付
    ALLINPAY = '22'     // 通联公对公支付
}

/**
 * 支付方式到枚举值的映射
 */
export const PAYMENT_METHOD_CODE_MAP: Record<PaymentMethod, string> = {
    'balance': PaymentMethodCode.BALANCE,
    'alipay-pc': PaymentMethodCode.ALIPAY_PC,
    'wechat-pc': PaymentMethodCode.WECHAT_PC,
    'card': PaymentMethodCode.CARD,
    'integral': PaymentMethodCode.INTEGRAL,
    'retention': PaymentMethodCode.RETENTION,
    'allinpay': PaymentMethodCode.ALLINPAY
}

/**
 * 余额显示参数
 */
export interface BalanceDisplayParams {
    orderId: string
}

/**
 * 账户余额显示信息
 */
export interface AccountBalanceDisplayVO {
    accountIsLimit?: number
    availableAmount?: number
    isAccount?: number
    availableRebateAmount?: number
    paymentTime?: string
    seckillCancelTime?: string
    TotalUserAssets?: number
    UserIntegral?: number
    UserMoney?: number
    UserMoneyFrozen?: number
    UserMoneyPendingSettlement?: number
    UserPackageMoney?: number
    UserPoints?: number
    UserPointsFrozen?: number
    userRechargeCard?: number
    UserRechargeCardFrozen?: number
    [property: string]: any
}

/**
 * 唤起支付参数
 */
export interface EvokePaymentParams {
    passwd: string
    orderId?: string
    orderIdList?: string  // 订单ID列表，值与orderId一致
    type: number  // 支付方式代码（枚举值）
    payPrice?: number
    thaliEquity?: string
    thaliType?: number
}

export interface PlusMemberPayParams {
    passwd: string
    payPrice: number
    thaliEquity: string
    thaliType: number
    type: 1 | 2
}

/**
 * 查询支付金额参数
 */
export interface QueryPaymentAmountParams {
    paymentNumber: string
}

/**
 * 查询支付金额结果
 */
export interface QueryPaymentAmountResult {
    orderCreateTime?: string
    orderCancelTime?: string
    orderPaymentAmount?: number
}

/**
 * 支付页面状态
 */
export interface PaymentPageState {
    // 支付金额信息
    paymentAmount: QueryPaymentAmountResult | null
    // 余额信息
    balanceInfo: AccountBalanceDisplayVO | null
    // 选中的支付方式（支付类型码：1余额 2卡 24账期 5支付宝 11微信 14PayPal 15Stripe 22通联）
    selectedMethod: number
    // 倒计时（秒）
    countdown: number
    // 密码相关
    hasPassword: boolean
    // 弹窗显示状态
    showPasswordModal: boolean
    showSetPasswordModal: boolean
    // 二维码弹窗
    showQrcodeModal?: boolean
    qrcode?: string
}

/**
 * 密码输入弹窗 Props
 */
export interface PasswordInputModalProps {
    visible: boolean
    title?: string
    placeholder?: string
}

/**
 * 密码输入弹窗 Emits
 */
export interface PasswordInputModalEmits {
    (e: 'close'): void
    (e: 'confirm', password: string): void
}

/**
 * 密码设置弹窗 Props
 */
export interface PasswordSetModalProps {
    visible: boolean
    title?: string
    placeholder?: string
}

/**
 * 密码设置弹窗 Emits
 */
export interface PasswordSetModalEmits {
    (e: 'close'): void
    (e: 'confirm', password: string): void
}
