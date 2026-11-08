/**
 * 装修接口返回解包：兼容 { code, data } 与直接 data，以及 list/lists。
 */
export function unwrapDecorate(res) {
  if (!res) return null
  if (res.data !== undefined && (res.code === 1 || res.code === 200 || res.code === 0)) {
    return res.data
  }
  if (res.lists || res.list || res.id || res.data) {
    return res.data !== undefined ? res.data : res
  }
  return res
}

export function unwrapPageList(res) {
  const data = unwrapDecorate(res) || {}
  const lists = data.lists || data.list || data.records || []
  return {
    lists: Array.isArray(lists) ? lists : [],
    count: Number(data.count ?? data.total ?? 0)
  }
}

export function asWidgets(raw) {
  if (!raw) return []
  if (!Array.isArray(raw)) return []
  const out = []
  for (const item of raw) {
    if (Array.isArray(item)) {
      for (const nested of item) {
        if (nested && typeof nested === 'object') out.push(nested)
      }
    } else if (item && typeof item === 'object') {
      out.push(item)
    }
  }
  return out
}

export function splitDecorateWidgets(raw, metaNames = ['page-meta', 'data-meta']) {
  const widgets = asWidgets(raw)
  if (!widgets.length) {
    return { pageMeta: [], pageData: [] }
  }
  if (metaNames.includes(widgets[0].name)) {
    return { pageMeta: [widgets[0]], pageData: widgets.slice(1) }
  }
  return { pageMeta: [], pageData: widgets }
}

export function stringifyDecorateSave(pageMeta, pageData) {
  const widgets = []
  if (Array.isArray(pageMeta) && pageMeta.length) {
    widgets.push(pageMeta)
  } else if (pageMeta && pageMeta.name) {
    widgets.push([pageMeta])
  }
  widgets.push(...(pageData || []))
  return JSON.stringify(widgets)
}
