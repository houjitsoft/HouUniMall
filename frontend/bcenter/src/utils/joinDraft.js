/**
 * 入驻 step3 登记表「本机草稿」（2026-11-10）
 *
 * 目标：当前会员填到一半刷新 / 关掉页面 / 从别的步骤再回来，已录入的信息不丢。
 * - 存储：复用 bcenter 既有 cache 工具（localStorage，like_admin_ 前缀）
 * - 隔离：按登录会员的稳定标识 + 入驻类型分键，不同会员互不可见
 * - 时效：7 天，过期自动丢弃；提交成功后由页面立即清除
 * - 边界：只保存表单录入值与已上传文件的 URL；密码、短信验证码、登录凭证一律不落盘。
 *   草稿只在本机浏览器，不上传服务端；证件号、银行卡号等入驻资料会随表单一起暂存，
 *   共用电脑时可用页面上的「清空重填」立即抹除。
 *
 * 这里不引用 joinAuth：主仓与开源树该文件导出不同，草稿工具只依赖两树一致的 cache.ts。
 */
import cache from '@/utils/cache'

const KEY_PREFIX = 'join_step3_draft_'
const TOKEN_KEY = 'shop_token'
const SCHEMA_VERSION = 1
const TTL_MS = 7 * 24 * 60 * 60 * 1000

/** 一次性凭证与服务端主键不落盘 */
const PRIVATE_FIELDS = new Set(['validateCode', 'settlePW', 'joinId'])
/** el-date-picker 未设 value-format，绑定的是 Date 对象，JSON 化后要还原 */
const DATE_FIELDS = new Set(['validateStartTime', 'validateEndTime'])
/** 判断「有没有实质录入」时忽略这些由后端/默认值填充的字段 */
const DEFAULT_FIELDS = new Set([
  'shopType',
  'idType',
  'internationalDialingCode',
  'tempArray',
  'tempArray1',
  'tempArraytow',
  'productClassId',
  'shopLevelId',
  'enterpriseNature',
])

function currentToken() {
  let token = ''
  try {
    token = cache.get(TOKEN_KEY) || ''
    if (!token || token === 'undefined' || token === 'null') {
      token = window.localStorage.getItem('token') || ''
    }
  } catch (e) {
    // 隐私模式 / localStorage 不可用时按无登录态处理，草稿功能自动降级
  }
  return typeof token === 'string' ? token : ''
}

function hash(text) {
  let h1 = 0x811c9dc5
  let h2 = 0x01000193
  for (let i = 0; i < text.length; i++) {
    const c = text.charCodeAt(i)
    h1 = Math.imul(h1 ^ c, 16777619) >>> 0
    h2 = Math.imul(h2 + c, 16777619) >>> 0
  }
  return `${h1.toString(36)}-${h2.toString(36)}`
}

/**
 * 会员标识：优先取 JWT payload 里的账号字段，重新登录换了 token 也能命中同一份草稿；
 * 解析不出时退化为 token 指纹，实在没有登录态则用 guest。
 */
function memberIdentity() {
  const raw = currentToken().replace(/^Bearer\s+/i, '')
  if (!raw) return 'guest'
  const parts = raw.split('.')
  if (parts.length === 3) {
    try {
      const b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
      const json = decodeURIComponent(
        Array.prototype.map
          .call(window.atob(b64), (c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join(''),
      )
      const payload = JSON.parse(json)
      const uid = payload.userId ?? payload.uid ?? payload.user_id ?? payload.id ?? payload.sub
      if (uid !== undefined && uid !== null && uid !== '') return `u_${String(uid)}`
    } catch (e) {
      // 非标准 JWT，退化成令牌指纹
    }
  }
  return `t_${hash(raw)}`
}

/** 同一会员 + 同一入驻类型共用一份草稿；joinId 不入键，避免空链接与回填后取不到 */
export function draftKey(shopType) {
  return `${KEY_PREFIX}${memberIdentity()}_s${String(shopType ?? '')}`
}

export function serializeForm(form) {
  const out = {}
  Object.keys(form || {}).forEach((key) => {
    if (PRIVATE_FIELDS.has(key)) return
    const value = form[key]
    out[key] = value instanceof Date ? value.toISOString() : value
  })
  return out
}

export function deserializeForm(form) {
  const out = {}
  Object.keys(form || {}).forEach((key) => {
    const value = form[key]
    if (DATE_FIELDS.has(key) && typeof value === 'string' && !Number.isNaN(Date.parse(value))) {
      out[key] = new Date(value)
    } else {
      out[key] = value
    }
  })
  return out
}

/** 空表单不写草稿，避免「清空重填」后被自动保存立刻重建 */
export function hasDraftContent(form, extra) {
  const filled = Object.entries(form || {}).some(([key, value]) => {
    if (PRIVATE_FIELDS.has(key) || DEFAULT_FIELDS.has(key)) return false
    if (Array.isArray(value)) return value.length > 0
    return value !== '' && value !== null && value !== undefined
  })
  if (filled) return true
  return !!(
    extra &&
    (extra.imgUrl1 || extra.agreementFileName || extra.fileName || extra.checked)
  )
}

export function readDraft(key) {
  const data = cache.get(key)
  if (!data || typeof data !== 'object') return null
  if (
    data.version !== SCHEMA_VERSION ||
    !data.savedAt ||
    Date.now() - data.savedAt > TTL_MS
  ) {
    cache.remove(key)
    return null
  }
  return data
}

/** 校验草稿确实属于当前会员，防止共用浏览器时误恢复他人内容 */
export function isOwnDraft(data) {
  return !data || !data.uid || data.uid === memberIdentity()
}

export function writeDraft(key, { form, extra, joinId, shopType }) {
  try {
    cache.set(key, {
      version: SCHEMA_VERSION,
      savedAt: Date.now(),
      uid: memberIdentity(),
      joinId: joinId || '',
      shopType: shopType ?? '',
      form: form || {},
      extra: extra || {},
    })
    return true
  } catch (e) {
    return false
  }
}

export function removeDraft(key) {
  try {
    cache.remove(key)
  } catch (e) {
    // localStorage 不可写时忽略
  }
}
