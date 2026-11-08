/**
 * 统一登录渠道注册表（UniLogin）
 *
 * 本插件不绑定任何单一厂商：「统一登录」只是一层框架，具体登录渠道由本表驱动。
 *
 * ── 新增一个渠道要做什么 ────────────────────────────────────────────────
 * 后端：无需改代码。服务端已内置标准 OAuth2 端点模板
 *      （backend/paas/auth/.../login/channel/LoginChannelDefaults.java），
 *      只要在「设置 → 第三方集成」里把凭据填上，或在 Nacos 里补
 *        unilogin.channels.<code>.app-id / app-secret / redirect-uri
 *      该渠道即在 /web/auth/unilogin/<code>/** 上可用。
 * 前端：在本表 CHANNELS 里补一条描述 + 对应 configFields，
 *      登录页入口、授权回调路由、设置页 Tab 会自动出现。
 *
 * 服务端通道固定为 /web/auth/unilogin/{code}/** —— 渠道编码即路径参数，
 * 因此本表的 code 必须与后端模板、第三方集成配置的平台键对应（见各处注释）。
 *
 * 字段说明：
 *   code           渠道唯一编码，同时作为登录方式扩展点的 provider code、后端路径参数
 *   defaultVisible 是否在登录页展示入口。缺省（不写或 true）即展示；
 *                  只有明确写 false 的渠道才不出现在登录页。
 *                  注意：展示 ≠ 可用。未配置凭据的渠道入口照样展示，
 *                  点开弹窗会给出「尚未配置凭据 + 去哪里配」的引导，
 *                  而不是让入口凭空消失、让人以为功能没做。
 *   configPlatform 第三方集成配置的 platform 键（设置页与后端共用的落库键）
 *   configKey      配置体内该渠道子对象的键名（与后端 ThirdPartyConfigDTO 字段一致）
 *   configFields   设置页表单字段，key 必须与后端 DTO 子对象字段名一致
 *   callbackPath   授权回调页完整路径，需要在第三方开放平台登记为回调地址
 */

export const PLUGIN_CODE = 'unilogin'
export const PLUGIN_VERSION = '1.0.2'

/** 回调路由（相对插件路由根，即 /plugin/unilogin） */
export const CALLBACK_ROUTE = '/callback/:channel?'

/** 回调地址的域内路径前缀（用于生成给第三方登记的回调地址） */
export const CALLBACK_BASE = `/plugin/${PLUGIN_CODE}/callback`

export const CHANNELS = [
  {
    code: 'wechat',
    title: '微信快捷登录',
    shortTitle: '微信',
    icon: 'ChatDotRound',
    brandColor: '#07c160',
    defaultVisible: true,
    configPlatform: 'wechat-login',
    configKey: 'wechatLogin',
    configFields: [
      { key: 'appId', label: 'AppID', required: true, hint: '微信开放平台「网站应用」的 AppID' },
      { key: 'appSecret', label: 'AppSecret', type: 'password', required: true, hint: '微信开放平台「网站应用」的 AppSecret' }
    ],
    callbackPath: `${CALLBACK_BASE}/wechat`,
    scanApp: '微信',
    steps: ['打开微信', '扫描上方二维码', '确认后自动登录'],
    guide: [
      { text: '在', link: { url: 'https://open.weixin.qq.com', label: '微信开放平台' }, tail: '创建「网站应用」并通过认证（需企业主体，300 元/年）。' },
      { text: '在应用详情配置「授权回调域」，只填域名，例如 ', code: 'unimall.houjit.com', tail: '（不带 https:// 和路径）。' },
      { text: '把该应用的 AppID / AppSecret 填到下面，回调地址保持默认即可。' },
      { text: '留空则回退使用 Nacos 配置 ', code: 'unilogin.channels.wechat.*', tail: '（兼容旧键 wechat.open.*）。' }
    ]
  },
  {
    code: 'wecom',
    title: '企业微信登录',
    shortTitle: '企微',
    icon: 'OfficeBuilding',
    brandColor: '#0082ef',
    configPlatform: 'wechat-work',
    configKey: 'wechatWork',
    configFields: [
      { key: 'corpId', label: '企业 CorpID', required: true, hint: '企业微信后台「我的企业 → 企业信息」里的企业 ID' },
      { key: 'secret', label: '应用 Secret', type: 'password', required: true, hint: '自建应用的 Secret' },
      { key: 'agentId', label: '应用 AgentId', hint: '自建应用的 AgentId，授权链接必须携带' }
    ],
    callbackPath: `${CALLBACK_BASE}/wecom`,
    scanApp: '企业微信',
    steps: ['打开企业微信', '扫描上方二维码', '确认后自动登录'],
    guide: [
      { text: '在', link: { url: 'https://work.weixin.qq.com', label: '企业微信管理后台' }, tail: '创建「自建应用」，拿到 CorpID、AgentId 与 Secret。' },
      { text: '在应用详情配置「企业微信授权登录」的回调域，填域名即可。' },
      { text: '把 CorpID 当作 AppID 填入下方，并补上 AgentId。' },
      { text: '成员登录后按 ', code: 'wecom_<userid>', tail: ' 匹配平台账号绑定关系，需先在「账号设置」里绑定。' }
    ]
  },
  {
    code: 'dingtalk',
    title: '钉钉登录',
    shortTitle: '钉钉',
    icon: 'ChatDotSquare',
    brandColor: '#1677ff',
    configPlatform: 'dingtalk',
    configKey: 'dingtalk',
    configFields: [
      { key: 'appKey', label: 'AppKey', required: true, hint: '钉钉开放平台应用的 AppKey（作为 client_id）' },
      { key: 'appSecret', label: 'AppSecret', type: 'password', required: true, hint: '钉钉开放平台应用的 AppSecret' },
      { key: 'agentId', label: 'AgentId', hint: '企业内部应用的 AgentId，扫码登录可不填' }
    ],
    callbackPath: `${CALLBACK_BASE}/dingtalk`,
    scanApp: '钉钉',
    steps: ['打开钉钉', '扫描上方二维码', '确认后自动登录'],
    guide: [
      { text: '在', link: { url: 'https://open-dev.dingtalk.com', label: '钉钉开放平台' }, tail: '创建「企业内部应用」，开启「扫码登录」能力。' },
      { text: '在登录与分享里配置回调域名，填域名即可。' },
      { text: '把 AppKey / AppSecret 填到下面，保存后即时生效。' },
      { text: '成员登录后按 ', code: 'dingtalk_<openId>', tail: ' 匹配平台账号绑定关系。' }
    ]
  },
  {
    code: 'qq',
    title: 'QQ 登录',
    shortTitle: 'QQ',
    icon: 'ChatRound',
    brandColor: '#12b7f5',
    configPlatform: 'qq-login',
    configKey: 'qqLogin',
    configFields: [
      { key: 'appId', label: 'APP ID', required: true, hint: 'QQ 互联应用的 APP ID' },
      { key: 'appSecret', label: 'APP Key', type: 'password', required: true, hint: 'QQ 互联应用的 APP Key' }
    ],
    callbackPath: `${CALLBACK_BASE}/qq`,
    scanApp: 'QQ',
    steps: ['打开手机 QQ', '扫描上方二维码', '确认后自动登录'],
    guide: [
      { text: '在', link: { url: 'https://connect.qq.com', label: 'QQ 互联' }, tail: '创建「网站应用」并通过审核。' },
      { text: '在应用详情配置回调地址，指向 ', code: `${CALLBACK_BASE}/qq`, tail: '。' },
      { text: '把 APP ID / APP Key 填到下面。' },
      { text: '成员登录后按 ', code: 'qq_<openid>', tail: ' 匹配平台账号绑定关系。' }
    ]
  },
  {
    code: 'feishu',
    title: '飞书登录',
    shortTitle: '飞书',
    icon: 'Promotion',
    brandColor: '#3370ff',
    configPlatform: 'feishu',
    configKey: 'feishu',
    configFields: [
      { key: 'appId', label: 'App ID', required: true, hint: '飞书开放平台应用的 App ID（作为 client_id）' },
      { key: 'appSecret', label: 'App Secret', type: 'password', required: true, hint: '飞书开放平台应用的 App Secret' }
    ],
    callbackPath: `${CALLBACK_BASE}/feishu`,
    scanApp: '飞书',
    steps: ['打开飞书', '扫描上方二维码', '确认后自动登录'],
    guide: [
      { text: '在', link: { url: 'https://open.feishu.cn', label: '飞书开放平台' }, tail: '创建「网页应用」，开启网页应用登录能力。' },
      { text: '在安全设置里把 ', code: `${CALLBACK_BASE}/feishu`, tail: ' 加入重定向 URL 白名单。' },
      { text: '把 App ID / App Secret 填到下面。' },
      { text: '成员登录后按 ', code: 'feishu_<open_id>', tail: ' 匹配平台账号绑定关系。' }
    ]
  }
]

/** 按渠道编码取描述 */
export function getChannel(code) {
  return CHANNELS.find((item) => item.code === code) || null
}

/** 默认渠道（回调页未指定 channel 时使用） */
export const DEFAULT_CHANNEL = CHANNELS[0]?.code || 'wechat'

/** 生成该渠道在插件路由下的回调路径（相对路径） */
export function callbackRouteOf(channel) {
  return `/callback/${channel.code}`
}
