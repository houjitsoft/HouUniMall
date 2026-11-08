export function parseKeywordLines(value) {
  const seen = new Set()
  const result = []
  for (const item of String(value || '').split(/[\r\n,，]+/)) {
    const text = item.trim()
    const key = text.toLocaleLowerCase()
    if (!text || seen.has(key)) continue
    seen.add(key)
    result.push(text)
  }
  return result
}

function cartesian(lists, prefix = [], output = [], limit = 5000) {
  if (output.length >= limit) return output
  if (!lists.length) {
    output.push(prefix)
    return output
  }
  for (const item of lists[0]) {
    cartesian(lists.slice(1), [...prefix, item], output, limit)
    if (output.length >= limit) break
  }
  return output
}

export function generateKeywordCombinations(groups = {}, patterns = [], separator = '', limit = 5000) {
  const seen = new Set()
  const result = []
  for (const pattern of patterns) {
    const symbols = String(pattern).split('')
    const lists = symbols.map((symbol) => Array.isArray(groups[symbol]) ? groups[symbol] : []).filter((items) => items.length)
    if (lists.length !== symbols.length) continue
    for (const parts of cartesian(lists, [], [], limit - result.length)) {
      const text = parts.join(separator).trim()
      const key = text.toLocaleLowerCase()
      if (!text || seen.has(key)) continue
      seen.add(key)
      result.push(text)
      if (result.length >= limit) return result
    }
  }
  return result
}
