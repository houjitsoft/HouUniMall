/**
 * 商户资质管理 API
 *
 * 16 终端 × 多支付渠道（WECHAT/ALIPAY/UNIONPAY/APPLE/GOOGLE/PAYPAL/STRIPE/HUAWEI）
 * 商户资质的登记、查询、编辑、启停、删除。
 *
 * 后端路径：/web/payment/merchantQual/**  (bff-web → payment.PayMerchantQualController)
 */
import request from '@/utils/request'

export const merchantQualApi = {
  /**
   * 分页查询
   * @param {Object} params { pageNum, pageSize, terminal?, channel?, env?, status?, keyword? }
   */
  page(params) {
    return request({ url: '/web/payment/merchantQual/page', method: 'post', data: params })
  },

  /**
   * 查询详情
   * @param {Number|String} id 主键
   * @param {Boolean} plainSensitive 是否解密返回敏感字段（编辑回填时传 true）
   */
  detail(id, plainSensitive = false) {
    return request({
      url: `/web/payment/merchantQual/detail/${id}`,
      method: 'get',
      params: { plainSensitive }
    })
  },

  /**
   * 保存（id 为空 = 新增；id 非空 = 更新）
   * @param {Object} data
   */
  save(data) {
    return request({ url: '/web/payment/merchantQual/save', method: 'post', data })
  },

  /**
   * 启用/停用
   * @param {Object} param { id, status }  status: 0=停用 1=启用
   */
  toggle(param) {
    return request({ url: '/web/payment/merchantQual/toggle', method: 'post', data: param })
  },

  /**
   * 删除（逻辑删除）
   * @param {Object} param { id }
   */
  delete(param) {
    return request({ url: '/web/payment/merchantQual/delete', method: 'post', data: param })
  }
}

/**
 * 16 终端枚举（与后端 TerminalTypeEnum 对齐）
 */
export const TERMINAL_OPTIONS = [
  { label: '微信小程序', value: 'WX_MINI' },
  { label: '支付宝小程序', value: 'ALI_MINI' },
  { label: '百度小程序', value: 'BD_MINI' },
  { label: '抖音小程序', value: 'TT_MINI' },
  { label: 'QQ小程序', value: 'QQ_MINI' },
  { label: '360小程序', value: '360_MINI' },
  { label: '快手小程序', value: 'KS_MINI' },
  { label: '飞书小程序', value: 'LARK_MINI' },
  { label: '京东小程序', value: 'JD_MINI' },
  { label: '小红书小程序', value: 'XHS_MINI' },
  { label: 'APP', value: 'APP' },
  { label: '华为元服务', value: 'HW' },
  { label: '快应用', value: 'QUICKAPP' },
  { label: '鸿蒙', value: 'HARMONY' },
  { label: 'H5/WAP', value: 'WAP' },
  { label: 'PC/Web', value: 'PC' }
]

/**
 * 8 支付渠道枚举
 */
export const CHANNEL_OPTIONS = [
  { label: '微信支付', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' },
  { label: '银联', value: 'UNIONPAY' },
  { label: 'Apple Pay', value: 'APPLE' },
  { label: 'Google Pay', value: 'GOOGLE' },
  { label: 'PayPal', value: 'PAYPAL' },
  { label: 'Stripe', value: 'STRIPE' },
  { label: '华为支付', value: 'HUAWEI' }
]

/**
 * 环境枚举
 */
export const ENV_OPTIONS = [
  { label: '生产', value: 'P' },
  { label: '沙箱', value: 'S' }
]

/**
 * 终端 + 渠道差异化字段配置
 *
 * 决定不同（终端+渠道）组合时，表单显示哪些字段、是否必填。
 * 缺失的渠道走默认（appId+appSecret+notifyUrl）。
 */
export const CHANNEL_FIELD_RULES = {
  WECHAT: {
    appId: { required: true, label: 'AppID' },
    appSecret: { required: true, label: 'AppSecret/小程序密钥', sensitive: true },
    mchId: { required: true, label: '商户号 mch_id' },
    apiKey: { required: true, label: 'APIv3 密钥', sensitive: true },
    notifyUrl: { required: true, label: '支付回调 URL' }
  },
  ALIPAY: {
    appId: { required: true, label: '支付宝 AppID' },
    appSecret: { required: true, label: '应用私钥', sensitive: true, textarea: true },
    apiPublicKey: { required: true, label: '支付宝公钥', textarea: true },
    notifyUrl: { required: true, label: '异步通知 URL' },
    returnUrl: { required: false, label: '同步跳转 URL' }
  },
  UNIONPAY: {
    appId: { required: true, label: '商户号' },
    appSecret: { required: true, label: '签名证书密码', sensitive: true },
    notifyUrl: { required: true, label: '后台通知地址' }
  },
  APPLE: {
    appId: { required: true, label: 'Bundle ID' },
    appSecret: { required: false, label: 'IAP Shared Secret', sensitive: true }
  },
  GOOGLE: {
    appId: { required: true, label: 'Package Name' },
    appSecret: { required: false, label: 'Service Account JSON', sensitive: true, textarea: true }
  },
  PAYPAL: {
    appId: { required: true, label: 'Client ID' },
    appSecret: { required: true, label: 'Client Secret', sensitive: true },
    notifyUrl: { required: false, label: 'Webhook URL' }
  },
  STRIPE: {
    appId: { required: true, label: 'Publishable Key' },
    appSecret: { required: true, label: 'Secret Key', sensitive: true },
    notifyUrl: { required: false, label: 'Webhook URL' }
  },
  HUAWEI: {
    appId: { required: true, label: 'App ID' },
    appSecret: { required: true, label: 'App Secret', sensitive: true }
  }
}

export const SENSITIVE_MASK = '******'

export default merchantQualApi
