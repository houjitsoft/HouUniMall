const TASK_STATES = {
  idle: { label: '尚未分析', type: 'info', polling: false, retryable: true },
  pending: { label: '等待分析', type: 'warning', polling: true, retryable: false },
  running: { label: '正在分析官网', type: 'primary', polling: true, retryable: false },
  success: { label: '官网分析完成', type: 'success', polling: false, retryable: true },
  review: { label: '结果待校验', type: 'warning', polling: false, retryable: true },
  failed: { label: '结果待校验', type: 'warning', polling: false, retryable: true }
}

export function catalogTaskState(status) {
  return TASK_STATES[String(status || '').toLowerCase()] || TASK_STATES.idle
}

export function normalizeAliases(raw) {
  const values = Array.isArray(raw)
    ? raw
    : String(raw || '').split(/[,，\n]/)
  const seen = new Set()
  const result = []
  for (const value of values) {
    const alias = String(value ?? '').trim()
    const key = alias.toLocaleLowerCase()
    if (!alias || seen.has(key)) continue
    seen.add(key)
    result.push(alias)
  }
  return result
}

export function filterCatalogItems(items, keyword) {
  const query = String(keyword || '').trim().toLocaleLowerCase()
  if (!query) return items || []
  return (items || []).filter((item) => {
    const aliases = normalizeAliases(item?.aliases)
    const searchable = [
      item?.name,
      item?.description,
      item?.domain,
      item?.sellingPoints,
      ...aliases
    ]
    return searchable.some(value => String(value || '').toLocaleLowerCase().includes(query))
  })
}
