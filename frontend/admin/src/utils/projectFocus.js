/**
 * 项目概览「重点关注」列表过滤（排除占位/空名称等无效数据）
 */
export function isMeaningfulProjectName(name) {
  const n = String(name ?? '').trim()
  if (!n) return false
  if (n === '占位') return false
  if (/^占位\d*$/i.test(n)) return false
  if (n === '未命名项目' || n === '测试项目') return false
  return true
}

export function filterFocusProjects(list) {
  if (!Array.isArray(list)) return []
  return list.filter((p) => isMeaningfulProjectName(p?.name))
}
