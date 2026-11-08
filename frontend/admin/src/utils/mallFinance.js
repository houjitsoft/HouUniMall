export function unwrapData(response) {
  if (response == null) return {}
  if (Object.prototype.hasOwnProperty.call(response, 'data')) return response.data ?? {}
  return response
}

export function normalizePage(payload) {
  const data = unwrapData(payload)
  const list = data.records || data.lists || data.list || data.rows || []
  return {
    list: Array.isArray(list) ? list : [],
    total: Number(data.total ?? data.count ?? data.totalCount ?? 0),
    page: Number(data.current ?? data.page_no ?? data.page ?? 1),
    size: Number(data.size ?? data.page_size ?? data.pageSize ?? 10),
    extend: data.extend || {}
  }
}

export function compactQuery(query = {}) {
  return Object.fromEntries(Object.entries(query).filter(([, value]) => (
    value !== undefined && value !== null && (typeof value !== 'string' || value.trim() !== '')
  )))
}

export function formatMoney(value) {
  const number = Number(value ?? 0)
  return Number.isFinite(number)
    ? number.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    : '0.00'
}

export function formatDateTime(value) {
  if (value == null || value === '') return '-'
  if (typeof value === 'string') {
    const normalized = value.replace('T', ' ')
    if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/.test(normalized)) {
      return normalized.slice(0, 19)
    }
  }
  const date = new Date(typeof value === 'number' && value < 1e12 ? value * 1000 : value)
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = (part) => String(part).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

export function pickValue(row, fields, fallback = '-') {
  for (const field of Array.isArray(fields) ? fields : [fields]) {
    const value = String(field).split('.').reduce((current, key) => current?.[key], row)
    if (value !== undefined && value !== null && value !== '') return value
  }
  return fallback
}

export function financeStatus(value, kind = 'default') {
  const maps = {
    default: {
      0: ['待处理', 'warning'],
      1: ['已完成', 'success'],
      2: ['已拒绝', 'danger'],
      3: ['已打款', 'success']
    },
    payment: {
      0: ['待支付', 'warning'],
      1: ['已支付', 'success'],
      2: ['已关闭', 'info']
    },
    refund: {
      0: ['退款中', 'warning'],
      1: ['退款成功', 'success'],
      2: ['退款失败', 'danger']
    }
  }
  const [text, type] = maps[kind]?.[value] || [String(value ?? '未知'), 'info']
  return { text, type }
}
