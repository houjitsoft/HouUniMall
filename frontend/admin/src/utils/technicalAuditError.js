const DATABASE_ERROR_MARKERS = [
  'error updating database',
  'error querying database',
  'invalid json text',
  'data truncation',
  'sql:',
  'mapper.',
  'mysql',
]

export function technicalAuditErrorMessage(message) {
  const raw = String(message || '').trim()
  const normalized = raw.toLowerCase()
  if (DATABASE_ERROR_MARKERS.some(marker => normalized.includes(marker))) {
    return '检测结果保存失败，请重新点击“开始检测”'
  }
  if (!raw || raw === '网络错误') {
    return '检测未完成，请检查网络后重新点击“开始检测”'
  }
  return raw.length > 120 ? '检测未完成，请稍后重新点击“开始检测”' : raw
}
