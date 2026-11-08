//菜单主题类型
export enum ThemeEnum {
    LIGHT = 'light',
    DARK = 'dark'
}

// 菜单类型
export enum MenuEnum {
    CATALOGUE = 0,
    MENU = 1,
    BUTTON = 2
}

// 屏幕
export enum ScreenEnum {
    SM = 640,
    MD = 768,
    LG = 1024,
    XL = 1280,
    '2XL' = 1536
}

// 客户端类型
export enum ClientEnum {
    MP_WEIXIN = 1, // 微信-小程序
    OA_WEIXIN = 2, // 微信-公众号
    H5 = 3, // H5
    PC = 4, // PC
    IOS = 5, //苹果
    ANDROID = 6 //安卓
}

export const ClientMap = {
    [ClientEnum.MP_WEIXIN]: '微信小程序',
    [ClientEnum.OA_WEIXIN]: '微信公众号',
    [ClientEnum.H5]: '手机H5',
    [ClientEnum.PC]: '电脑PC',
    [ClientEnum.IOS]: '苹果APP',
    [ClientEnum.ANDROID]: '安卓APP'
}

// 支付类型
export enum PayEnum {
    WALLET = 1,  // 钱包余额支付
    WECHAT = 2,  // 微信支付
    ALIPAY = 3   // 支付宝支付
}

export const PayMap = {
    [PayEnum.WALLET]: '钱包余额支付',
    [PayEnum.WECHAT]: '微信支付',
    [PayEnum.ALIPAY]: '支付宝支付',
}

// 余额变动类型
export enum ChangeTypeEnums {
    DEPOSIT = 1000,
    PAY_ORDER = 1010,
    CANCEL_ORDER = 1020,
    AFTER_SALES_REFUND = 1030,
    SYSTEM_ADD = 1040,
    SYSTEM_LESSEN = 1050
}

export const ChangeTypeMap = {
    [ChangeTypeEnums.DEPOSIT]: '用户充值余额',
    [ChangeTypeEnums.PAY_ORDER]: '下单扣减余额',
    [ChangeTypeEnums.CANCEL_ORDER]: '取消订单退回余额',
    [ChangeTypeEnums.AFTER_SALES_REFUND]: '售后退回余额',
    [ChangeTypeEnums.SYSTEM_ADD]: '系统增加余额',
    [ChangeTypeEnums.SYSTEM_LESSEN]: '系统减少余额'
}

// 退款类型
export enum RefundTypeEnums {
    SYSTEM_CANCEL = 0,
    ADMIN_CANCEL = 1,
    SHOP_CANCEL = 2,
    USER_CANCEL = 3,
    AFTER_SALES_REFUND = 4
}
export const RefundTypeMap = {
    [RefundTypeEnums.SYSTEM_CANCEL]: '系统取消订单',
    [RefundTypeEnums.ADMIN_CANCEL]: '管理员取消订单',
    [RefundTypeEnums.SHOP_CANCEL]: '商家取消订单',
    [RefundTypeEnums.USER_CANCEL]: '用户取消订单',
    [RefundTypeEnums.AFTER_SALES_REFUND]: '售后退款'
}