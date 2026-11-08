/** 项目状态统计卡片（与 hou_pm_project.status -1~8 一致） */
export const PROJECT_STATUS_STAT_CARDS = [
  { label: '全部项目', field: 'total', itemClass: 'stat-total', filterStatus: null },
  { label: '关闭', field: 'closed', itemClass: 'stat-closed', filterStatus: '-1' },
  { label: '未开始', field: 'notStarted', itemClass: 'stat-not-started', filterStatus: '0' },
  { label: '进行中', field: 'ongoing', itemClass: 'stat-ongoing', filterStatus: '1' },
  { label: '已完成', field: 'completed', itemClass: 'stat-completed', filterStatus: '2' },
  { label: '待收款', field: 'pendingPayment', itemClass: 'stat-pending-payment', filterStatus: '8' },
  { label: '归档', field: 'archived', itemClass: 'stat-archived', filterStatus: '3' },
  { label: '已暂停', field: 'paused', itemClass: 'stat-paused', filterStatus: '4' },
  { label: '已终止', field: 'terminated', itemClass: 'stat-terminated', filterStatus: '5' },
  { label: '已延期', field: 'delayed', itemClass: 'stat-delayed', filterStatus: '6' },
  { label: '售后中', field: 'aftersale', itemClass: 'stat-aftersale', filterStatus: '7' },
  { label: '其他状态', field: 'other', itemClass: 'stat-other', filterStatus: 'other' }
]

export function createEmptyProjectStats() {
  return {
    total: 0,
    ongoing: 0,
    notStarted: 0,
    completed: 0,
    pendingPayment: 0,
    archived: 0,
    aftersale: 0,
    paused: 0,
    terminated: 0,
    closed: 0,
    delayed: 0,
    other: 0
  }
}

/** camelCase → snake_case，兼容后端统计字段别名 */
function toSnakeCase(key) {
  return String(key).replace(/[A-Z]/g, (ch) => `_${ch.toLowerCase()}`)
}

/**
 * 读取统计字段（兼容 pendingPayment / pending_payment / PENDING_PAYMENT）
 * @param {Record<string, number>|null|undefined} stats
 * @param {string} field
 * @returns {number}
 */
export function readProjectStatField(stats, field) {
  if (!stats || !field) return 0
  const candidates = [field, toSnakeCase(field), String(field).toLowerCase(), toSnakeCase(field).toUpperCase()]
  for (const key of candidates) {
    if (stats[key] != null && stats[key] !== '') {
      const n = Number(stats[key])
      return Number.isFinite(n) ? n : 0
    }
  }
  const lowerField = String(field).toLowerCase()
  for (const [k, v] of Object.entries(stats)) {
    if (k && String(k).toLowerCase() === lowerField && v != null && v !== '') {
      const n = Number(v)
      return Number.isFinite(n) ? n : 0
    }
    if (k && String(k).toLowerCase() === toSnakeCase(field) && v != null && v !== '') {
      const n = Number(v)
      return Number.isFinite(n) ? n : 0
    }
  }
  return 0
}

export function applyProjectStats(target, stats) {
  if (!stats || !target) return
  Object.keys(createEmptyProjectStats()).forEach((key) => {
    target[key] = readProjectStatField(stats, key)
  })
}
