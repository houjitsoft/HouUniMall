/** 平台展示名 ↔ 库内 code，与后端 PlatformNames 对齐 */
export const PLATFORM_CODE_MAP = {
  小红书: 'xhs',
  xhs: 'xhs',
  今日头条: 'toutiao',
  头条号: 'toutiao',
  toutiao: 'toutiao',
  抖音: 'douyin',
  抖音号: 'douyin',
  douyin: 'douyin',
  视频号: 'channels',
  channels: 'channels',
  微信公众号: 'wechat',
  微信: 'wechat',
  wechat: 'wechat',
  知乎: 'zhihu',
  zhihu: 'zhihu',
  百家号: 'bjh',
  百度百家号: 'bjh',
  bjh: 'bjh',
  搜狐: 'sohu',
  搜狐网: 'sohu',
  搜狐号: 'sohu',
  sohu: 'sohu',
  // 海外平台仅保留映射供历史数据展示，不在 Cookie 授权列表
  Reddit: 'reddit',
  reddit: 'reddit',
  FB: 'facebook',
  Facebook: 'facebook',
  facebook: 'facebook',
  INS: 'instagram',
  Instagram: 'instagram',
  instagram: 'instagram',
  LinkedIn: 'linkedin',
  linkedin: 'linkedin',
  网易号: 'netease',
  netease: 'netease',
  微博: 'weibo',
  weibo: 'weibo',
  B站: 'bilibili',
  哔哩哔哩: 'bilibili',
  bilibili: 'bilibili',
  简书: 'jianshu',
  jianshu: 'jianshu',
  企鹅号: 'penguin',
  penguin: 'penguin',
  西瓜视频: 'xigua',
  西瓜: 'xigua',
  xigua: 'xigua',
  快手: 'kuaishou',
  kuaishou: 'kuaishou',
  天天快报: 'ttkuaibao',
  快报: 'ttkuaibao',
  ttkuaibao: 'ttkuaibao',
  kuaibao: 'ttkuaibao',
  优酷: 'youku',
  youku: 'youku',
  秒拍: 'miaopai',
  miaopai: 'miaopai',
  一点资讯: 'yidian',
  一点: 'yidian',
  yidian: 'yidian',
  其他: 'other',
  other: 'other',
  垂直论坛: 'forum',
  论坛账号: 'forum',
  forum: 'forum',
  百度热搜: 'baidu'
}

/** 已接入开放平台 OAuth 的平台 */
export const OAUTH_PLATFORM_CODES = ['toutiao', 'douyin']

/**
 * OAuth 平台控制台与文档（与后端 PlatformNames.oauthDocUrl 对齐）。
 * 小红书 2026-10-20 移除：开放平台无笔记发布类目，改走 Cookie 通道，无 OAuth 控制台可用。
 */
export const PLATFORM_OAUTH_PORTAL = {
  toutiao: {
    label: '头条开放平台',
    portal: 'https://open.toutiao.com/',
    doc: 'https://open.toutiao.com/'
  },
  douyin: {
    label: '抖音开放平台',
    portal: 'https://developer.open-douyin.com/',
    doc: 'https://developer.open-douyin.com/docs/resource/zh-CN/dop/develop/openapi/video-management/douyin/create-video/video-create'
  }
}

export function getOAuthPortalInfo(nameOrCode) {
  const code = toPlatformCode(nameOrCode)
  return code ? PLATFORM_OAUTH_PORTAL[code] || null : null
}

/** 使用 Cookie 会话授权的平台（非 OAuth） */
export const COOKIE_PLATFORM_CODES = [
  'channels', 'zhihu', 'wechat', 'bjh', 'bilibili', 'weibo',
  'sohu', 'netease', 'jianshu', 'penguin', 'forum', 'xhs',
  'xigua', 'kuaishou', 'ttkuaibao', 'youku', 'miaopai', 'yidian', 'other'
]

export function isCookiePlatform(nameOrCode) {
  const code = toPlatformCode(nameOrCode)
  return code && COOKIE_PLATFORM_CODES.includes(code) && !OAUTH_PLATFORM_CODES.includes(code)
}

/** 支持账号密码模拟登录并抓 Cookie（与后端 PlatformLoginRecipe.passwordSupported 对齐） */
export const PASSWORD_LOGIN_PLATFORMS = [
  'zhihu', 'bilibili', 'weibo', 'jianshu', 'netease', 'sohu', 'penguin', 'bjh'
]

/**
 * 网页端仅扫码、无密码登录的自动重登平台。
 * 小红书 2026-10-20 归入此列：小红书开放平台只有电商类目（无笔记发布 API，
 * note/publish 实测 404），笔记只能驱动创作服务平台网页发布，故与微信/抖音同为扫码制。
 */
export const QR_ONLY_RELOGIN_PLATFORMS = ['wechat', 'channels', 'douyin', 'xhs']

/**
 * 仅支持「手机号 + 短信验证码」登录的平台（弹窗两步输入，无二维码）。
 * 小红书 2026-10-20 Playwright 实测：创作服务平台登录页无二维码入口，
 * 只支持手机号验证码，故既不在扫码类也不在密码类，单列一类。
 */
export const SMS_LOGIN_PLATFORMS = ['xhs']

export function isSmsLoginPlatform(nameOrCode) {
  const code = toPlatformCode(nameOrCode)
  return Boolean(code && SMS_LOGIN_PLATFORMS.includes(code))
}

export function isPasswordLoginPlatform(nameOrCode) {
  const code = toPlatformCode(nameOrCode)
  return Boolean(code && PASSWORD_LOGIN_PLATFORMS.includes(code))
}

export function toPlatformCode(nameOrCode) {
  if (!nameOrCode) return null
  return PLATFORM_CODE_MAP[nameOrCode] || nameOrCode
}

export function toPlatformDisplay(code) {
  const entry = Object.entries(PLATFORM_CODE_MAP).find(([, v]) => v === code && !/^[a-z_-]+$/.test(Object.keys(PLATFORM_CODE_MAP).find(k => k === code) || ''))
  if (code === 'xhs') return '小红书'
  if (code === 'toutiao') return '今日头条'
  if (code === 'douyin') return '抖音'
  if (code === 'channels') return '视频号'
  if (code === 'wechat') return '微信公众号'
  if (code === 'zhihu') return '知乎'
  if (code === 'bjh') return '百家号'
  if (code === 'sohu') return '搜狐'
  if (code === 'reddit') return 'Reddit'
  if (code === 'facebook') return 'FB'
  if (code === 'instagram') return 'INS'
  if (code === 'linkedin') return 'LinkedIn'
  if (code === 'netease') return '网易号'
  if (code === 'weibo') return '微博'
  if (code === 'bilibili') return 'B站'
  if (code === 'jianshu') return '简书'
  if (code === 'penguin') return '企鹅号'
  if (code === 'xigua') return '西瓜视频'
  if (code === 'kuaishou') return '快手'
  if (code === 'ttkuaibao') return '天天快报'
  if (code === 'youku') return '优酷'
  if (code === 'miaopai') return '秒拍'
  if (code === 'yidian') return '一点资讯'
  if (code === 'other') return '其他'
  if (code === 'forum') return '垂直论坛'
  return entry?.[0] || code
}

export function isOAuthPlatform(nameOrCode) {
  return OAUTH_PLATFORM_CODES.includes(toPlatformCode(nameOrCode))
}

export function getPlatformSettingsPath(code) {
  const c = toPlatformCode(code)
  if (OAUTH_PLATFORM_CODES.includes(c)) return `/geo/settings/platform/${c}`
  return null
}

export function getOAuthCallbackUri(code) {
  const c = toPlatformCode(code)
  if (typeof window === 'undefined') return `/geo/oauth/callback/${c}`
  return `${window.location.origin}/geo/oauth/callback/${c}`
}

export const OAUTH_RETURN_KEY = 'geo_oauth_return_to'

/** Cookie 授权登录页（前端兜底，与 PlatformCookieClient.loginInfo 保持一致） */
/** 微信公众号 Cookie 复制备注（悬停提示图标时展示） */
export const WECHAT_COOKIE_COPY_REMARK = `正确复制方式：
1. 登录 mp.weixin.qq.com，等首页加载完成
2. F12 → Network → 点开 home?t=home/index&token=... 这类已登录请求
3. 在 Request Headers 里找到 Cookie:，整段原样复制粘贴（常见为 bizuin=...; data_ticket=...; uuid=... 等，不一定出现 slave_user）
4. 粘贴到 HouGEO 的 Cookie 输入框 → 校验 → 确认绑定

注意：
· Request Cookie 与 Response 的 Set-Cookie 格式不同，优先复制 Request Headers 的 Cookie 整段
· 若误复制了多行 Set-Cookie，也可直接粘贴，系统会自动合并为可用格式
· 不要只复制单个字段，需尽量完整`

export const PLATFORM_COOKIE_LOGIN = {
  channels: {
    loginUrl: 'https://channels.weixin.qq.com/platform/login',
    hint: '在浏览器登录视频号助手，F12 → Network 复制 Cookie',
    requiredKeys: 'sessionid;wxuin'
  },
  zhihu: {
    loginUrl: 'https://www.zhihu.com/signin',
    hint: '登录知乎后复制 Cookie（需包含 z_c0）',
    requiredKeys: 'z_c0'
  },
  wechat: {
    loginUrl: 'https://mp.weixin.qq.com/',
    hint: '登录微信公众平台后复制 Cookie',
    requiredKeys: 'bizuin、data_ticket 等（整段原样粘贴，不必含 slave_user）',
    remark: WECHAT_COOKIE_COPY_REMARK
  },
  bjh: {
    loginUrl: 'https://baijiahao.baidu.com/',
    hint: '登录百家号后，F12 → Network 复制 Request Headers 中的 Cookie 整段（需包含 BAIDUID，以及 BDUSS 或 BDUSS_BFESS），系统将连接百家号创作中心校验登录态。',
    requiredKeys: 'BAIDUID;BDUSS 或 BDUSS_BFESS',
    remoteVerificationSupported: true
  },
  bilibili: {
    loginUrl: 'https://member.bilibili.com/platform/home',
    hint: '登录哔哩哔哩创作中心后，F12 复制完整 Cookie（需包含 SESSDATA）',
    requiredKeys: 'SESSDATA'
  },
  weibo: {
    loginUrl: 'https://weibo.com/login.php',
    hint: '登录微博后复制 Cookie（需包含 SUB）。该平台暂无合法远程 Cookie 校验，系统仅本地保存凭证，不会把格式正确的 Cookie 误标为已授权。',
    requiredKeys: 'SUB'
  },
  sohu: {
    loginUrl: 'https://mp.sohu.com/',
    hint: '登录 mp.sohu.com 后，在 Network 筛选 Fetch/XHR，点开 getUserInfo 或 listNews，'
      + '从 Request Headers 复制完整 Cookie，系统将以搜狐真实接口响应确认登录态。'
      + '不要只复制页面 document。浏览器能打开后台不等于发文接口可用。'
      + 'authorId 建议留空由系统解析；手填时用请求头 Accountid 完整数字。',
    requiredKeys: '完整 Request Cookie（由搜狐远程接口确认）',
    authorIdHint: '在 F12 → Network 点开 XHR 请求，Request Headers 里找 Accountid（例如 122894014）；'
      + '或筛选 user?accountId= 复制完整数字。勿填截断的短号。'
  },
  penguin: {
    loginUrl: 'https://om.qq.com/userAuth/index',
    hint: '登录 om.qq.com 后，F12 → Network 复制完整 Cookie（需含 omtoken、userid）',
    requiredKeys: 'omtoken;userid'
  },
  jianshu: {
    loginUrl: 'https://www.jianshu.com/sign_in',
    hint: '登录简书后进入「写文章」，F12 复制 Cookie（需包含 _session_id）',
    requiredKeys: '_session_id'
  },
  netease: {
    loginUrl: 'https://mp.163.com/subscribe_v4/index.html',
    hint: '登录网易号后台后，F12 → Network 复制完整 Cookie。会话字段为 NTES_SESS（邮箱）或 NTES_YD_SESS（手机号）或 NTES_OSESS（第三方）；建议同时含 P_INFO。系统将请求 /wemedia/navinfo.do 校验登录态。',
    requiredKeys: 'NTES_SESS 或 NTES_YD_SESS;P_INFO',
    remoteVerificationSupported: true
  },
  forum: {
    loginUrl: '',
    hint: '垂直论坛无统一登录入口，请在目标论坛登录后复制 Cookie。该平台暂无合法远程 Cookie 校验，系统仅本地保存凭证，不会把格式正确的 Cookie 误标为已授权。',
    requiredKeys: ''
  },
  xigua: {
    loginUrl: 'https://studio.ixigua.com/',
    hint: '登录西瓜视频创作者中心后，F12 → Network 复制完整 Cookie。该平台暂无远程会话校验，系统仅本地保存凭证。',
    requiredKeys: ''
  },
  kuaishou: {
    loginUrl: 'https://cp.kuaishou.com/',
    hint: '登录快手创作者服务平台后，F12 → Network 复制完整 Cookie。该平台暂无远程会话校验，系统仅本地保存凭证。',
    requiredKeys: ''
  },
  ttkuaibao: {
    loginUrl: 'https://kuaibao.qq.com/',
    hint: '登录天天快报相关后台后复制 Cookie。该平台暂无远程会话校验，系统仅本地保存凭证。',
    requiredKeys: ''
  },
  youku: {
    loginUrl: 'https://mp.youku.com/',
    hint: '登录优酷创作者中心后，F12 → Network 复制完整 Cookie。该平台暂无远程会话校验，系统仅本地保存凭证。',
    requiredKeys: ''
  },
  miaopai: {
    loginUrl: 'https://www.miaopai.com/',
    hint: '登录秒拍后台后复制 Cookie。该平台暂无远程会话校验，系统仅本地保存凭证。',
    requiredKeys: ''
  },
  yidian: {
    loginUrl: 'https://mp.yidianzixun.com/',
    hint: '登录一点号后台后，F12 → Network 复制完整 Cookie。该平台暂无远程会话校验，系统仅本地保存凭证。',
    requiredKeys: ''
  },
  other: {
    loginUrl: '',
    hint: '未单列平台：登录对应创作者中心后复制 Cookie。该入口暂无远程会话校验，系统仅本地保存凭证。',
    requiredKeys: ''
  }
}

/** 获取 Cookie 登录指引（后端未返回 loginUrl 时使用） */
export function getCookieLoginFallback(nameOrCode) {
  const code = toPlatformCode(nameOrCode)
  const fb = code ? PLATFORM_COOKIE_LOGIN[code] : null
  if (!fb) {
    return {
      platform: code,
      authType: 'cookie',
      loginUrl: '',
      hint: '在对应平台创作者中心登录后，复制浏览器 Cookie',
      requiredKeys: '',
      passwordLoginSupported: isPasswordLoginPlatform(code)
    }
  }
  return {
    platform: code,
    platformCode: code,
    platformName: toPlatformDisplay(code),
    authType: 'cookie',
    passwordLoginSupported: isPasswordLoginPlatform(code),
    ...fb
  }
}
