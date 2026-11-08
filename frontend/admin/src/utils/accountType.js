const PHONE_REGEX = /^1[3-9]\d{9}$/
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

/** 中国大陆及港澳台常用时区，用于 IP 接口不可用时的兜底 */
const DOMESTIC_TIMEZONES = new Set([
  'Asia/Shanghai',
  'Asia/Chongqing',
  'Asia/Harbin',
  'Asia/Urumqi',
  'Asia/Hong_Kong',
  'Asia/Macau',
  'Asia/Taipei'
])

/** 去除手机号中的空格、横线等分隔（粘贴 132 6270 8190 / 132-6270-8190 时常见） */
export function normalizePhoneInput(value) {
  return String(value || '').replace(/[\s-]+/g, '')
}

/** 提交前规范化账号：邮箱仅 trim，手机号去除空格 */
export function normalizeAccountInput(value) {
  const raw = String(value || '').trim()
  if (!raw) return ''
  if (raw.includes('@')) return raw
  return normalizePhoneInput(raw)
}

export function isPhoneAccount(value) {
  return PHONE_REGEX.test(normalizePhoneInput(value).trim())
}

export function isEmailAccount(value) {
  return EMAIL_REGEX.test(String(value || '').trim())
}

/**
 * 根据输入内容推断账号类型；无法确定时返回 null
 */
export function detectAccountMode(value) {
  const trimmed = String(value || '').trim()
  if (!trimmed) return null
  if (trimmed.includes('@')) return 'email'
  const phoneDigits = normalizePhoneInput(trimmed)
  if (/^\d+$/.test(phoneDigits)) return 'phone'
  if (isEmailAccount(trimmed)) return 'email'
  if (isPhoneAccount(trimmed)) return 'phone'
  return null
}

/**
 * 浏览器时区兜底：国内默认手机号，国外默认邮箱
 */
export function guessDomesticByTimezone() {
  try {
    const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone
    if (timezone && DOMESTIC_TIMEZONES.has(timezone)) {
      return true
    }
    if (timezone && timezone.startsWith('Asia/')) {
      return true
    }
  } catch {
    /* ignore */
  }
  return false
}

export function resolveDefaultAccountMode(options = {}) {
  const { accountMode, domestic } = options
  if (accountMode === 'phone' || accountMode === 'email') {
    return accountMode
  }
  if (typeof domestic === 'boolean') {
    return domestic ? 'phone' : 'email'
  }
  if (typeof domestic === 'string') {
    return domestic === 'true' ? 'phone' : 'email'
  }
  return guessDomesticByTimezone() ? 'phone' : 'email'
}
