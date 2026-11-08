/**
 * 支付渠道目录（Admin 支付设置 / 渠道凭证 / 路由 共用）
 *
 * code 必须与后端 PayMethodEnum.code 及 ChannelPayStrategy#getChannel() 一致，
 * 保存到 hou_pay_channel.channel_code / config_json 后，收银台 Strategy 才能读到。
 *
 * maturity:
 *   - live   : Strategy 已接官方 SDK（需用户填真实密钥才能真联调）
 *   - stub   : Strategy 存在但 createPay 仍 mock/TODO，禁止当作生产可用
 *
 * 不要把 wechat/alipay 小写码或 WX/ALI 路由缩写写进凭证保存；那些不会命中 Strategy。
 */
export const CHANNEL_MATURITY = {
  LIVE: 'live',
  STUB: 'stub'
}

/** @typedef {{ key: string, label: string, sensitive?: boolean, required?: boolean, type?: string, placeholder?: string }} ChannelField */

/**
 * @type {Array<{
 *   code: string,
 *   name: string,
 *   desc: string,
 *   provider: string,
 *   maturity: 'live'|'stub',
 *   color: string,
 *   fields: ChannelField[],
 *   stubReason?: string
 * }>}
 */
export const CHANNEL_CATALOG = [
  {
    code: 'WECHAT_V3',
    name: '微信支付 V3',
    desc: '官方 wechatpay-java，APIv3；Strategy=WechatV3PayStrategy',
    provider: 'WECHAT',
    maturity: 'live',
    color: '#07C160',
    fields: [
      { key: 'appId', label: 'AppID', required: true },
      { key: 'mchId', label: '商户号 mchId', required: true },
      { key: 'apiV3Key', label: 'APIv3 Key', sensitive: true, required: true },
      { key: 'certSerialNo', label: '商户证书序列号', required: true },
      { key: 'privateKey', label: '商户私钥（PEM）', sensitive: true, required: true, type: 'textarea',
        placeholder: '-----BEGIN PRIVATE KEY-----\\n...\\n-----END PRIVATE KEY-----' },
      { key: 'notifyUrl', label: '支付回调 notifyUrl', required: false,
        placeholder: 'https://你的域名/web/pay/notify/wechat' }
    ]
  },
  {
    code: 'ALIPAY',
    name: '支付宝',
    desc: '支付宝开放平台 RSA2；Strategy=AlipayPayStrategy（PC/WAP/APP）',
    provider: 'ALIPAY',
    maturity: 'live',
    color: '#1677FF',
    fields: [
      { key: 'gatewayUrl', label: '网关地址', required: false,
        placeholder: 'https://openapi.alipay.com/gateway.do' },
      { key: 'appId', label: 'AppID', required: true },
      { key: 'privateKey', label: '应用私钥（PEM）', sensitive: true, required: true, type: 'textarea',
        placeholder: '-----BEGIN PRIVATE KEY-----\\n...\\n-----END PRIVATE KEY-----' },
      { key: 'alipayPublicKey', label: '支付宝公钥', sensitive: true, required: true, type: 'textarea' },
      { key: 'notifyUrl', label: '支付回调 notifyUrl', required: false,
        placeholder: 'https://你的域名/web/pay/notify/alipay' }
    ]
  },
  {
    code: 'STRIPE',
    name: 'Stripe',
    desc: '国际卡；Strategy=StripePayStrategy（Checkout / PaymentIntent）',
    provider: 'STRIPE',
    maturity: 'live',
    color: '#635BFF',
    fields: [
      { key: 'publishableKey', label: 'Publishable Key', required: true,
        placeholder: 'pk_live_... 或 pk_test_...' },
      { key: 'secretKey', label: 'Secret Key', sensitive: true, required: true,
        placeholder: 'sk_live_... 或 sk_test_...' },
      { key: 'webhookSecret', label: 'Webhook Secret', sensitive: true, required: false,
        placeholder: 'whsec_...' },
      { key: 'merchantName', label: '商户显示名', required: false }
    ]
  },
  {
    code: 'YSF_PAY',
    name: '云闪付',
    desc: 'YsfPayStrategy 仍返回 mock URL，未接银联 acpsdk',
    provider: 'UNIONPAY',
    maturity: 'stub',
    color: '#E60012',
    stubReason: 'createPay 走 MOCK；填凭证也不会真扣款',
    fields: [
      { key: 'merchId', label: '商户号 merchId', required: true },
      { key: 'signKey', label: '签名密钥 signKey', sensitive: true, required: true }
    ]
  },
  {
    code: 'FME_PAY',
    name: 'FME 职工金融',
    desc: 'FmePayStrategy 骨架，合作方 API 未接',
    provider: 'FME',
    maturity: 'stub',
    color: '#FA8C16',
    stubReason: 'TODO 真实 HttpClient；当前 mock 网关',
    fields: [
      { key: 'partnerId', label: '合作方 partnerId', required: true },
      { key: 'appKey', label: 'AppKey', required: true },
      { key: 'appSecret', label: 'AppSecret', sensitive: true, required: true }
    ]
  },
  {
    code: 'ALI_CREDIT',
    name: '支付宝花呗',
    desc: 'AliCreditPayStrategy 仍 TODO creditPay，非生产',
    provider: 'ALIPAY',
    maturity: 'stub',
    color: '#1677FF',
    stubReason: '未接花呗 productCode；勿当正式渠道',
    fields: [
      { key: 'appId', label: 'AppID', required: true },
      { key: 'privateKey', label: '应用私钥（PEM）', sensitive: true, required: true, type: 'textarea' },
      { key: 'alipayPublicKey', label: '支付宝公钥', sensitive: true, required: true, type: 'textarea' }
    ]
  },
  {
    code: 'SHARE_PAY',
    name: '分账支付（骨架）',
    desc: 'SharePayStrategy 本地拆分 mock；真分账在分账中心配置接收方',
    provider: 'SHARE',
    maturity: 'stub',
    color: '#722ED1',
    stubReason: '未联动微信/支付宝 profitsharing；仅骨架',
    fields: [
      { key: 'remark', label: '备注（可选）', required: false,
        placeholder: '分账接收方请到「分账中心」配置' }
    ]
  }
]

export const LIVE_CHANNELS = CHANNEL_CATALOG.filter((c) => c.maturity === 'live')
export const STUB_CHANNELS = CHANNEL_CATALOG.filter((c) => c.maturity === 'stub')

export function findChannel(code) {
  return CHANNEL_CATALOG.find((c) => c.code === code) || null
}

/** 路由 / 下拉用：仅 live 默认可选；stub 单独列出并带标记 */
export function channelSelectOptions({ includeStub = true } = {}) {
  return CHANNEL_CATALOG
    .filter((c) => includeStub || c.maturity === 'live')
    .map((c) => ({
      label: c.maturity === 'stub' ? `${c.name}（骨架/Mock）` : c.name,
      value: c.code,
      maturity: c.maturity,
      disabled: false
    }))
}
