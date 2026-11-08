function splitCsvLine(line) {
  const cells = []
  let value = ''
  let quoted = false
  for (let index = 0; index < line.length; index += 1) {
    const char = line[index]
    if (char === '"') {
      if (quoted && line[index + 1] === '"') {
        value += '"'
        index += 1
      } else {
        quoted = !quoted
      }
    } else if (char === ',' && !quoted) {
      cells.push(value.trim())
      value = ''
    } else {
      value += char
    }
  }
  cells.push(value.trim())
  return cells
}

function normalizeTrend(value) {
  if (Array.isArray(value)) return value
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    throw new Error('趋势字段必须是 JSON 数组')
  }
}

function normalizeMetric(item, defaults = {}) {
  const indexValue = (value) => {
    if (value == null || String(value).trim() === '') return Number.NaN
    return Number(value)
  }
  const metric = {
    keyword: String(item?.keyword || item?.关键词 || '').trim(),
    heatIndex: indexValue(item?.heatIndex ?? item?.热度指数),
    competitionIndex: indexValue(item?.competitionIndex ?? item?.竞争指数),
    opportunityIndex: indexValue(item?.opportunityIndex ?? item?.机会指数),
    statDate: String(item?.statDate || item?.统计日期 || defaults.statDate || '').trim(),
    source: String(item?.source || item?.数据源 || defaults.source || '').trim(),
    region: String(item?.region || item?.地区 || defaults.region || '').trim(),
    trend: normalizeTrend(item?.trend ?? item?.趋势),
  }
  if (!metric.keyword) throw new Error('存在缺少关键词的记录')
  for (const [field, label] of [
    ['heatIndex', '热度指数'],
    ['competitionIndex', '竞争指数'],
    ['opportunityIndex', '机会指数'],
  ]) {
    if (!Number.isFinite(metric[field]) || metric[field] < 0 || metric[field] > 100) {
      throw new Error(`${metric.keyword} 的${label}必须是 0 到 100 的数字`)
    }
  }
  const parsedDate = new Date(`${metric.statDate}T00:00:00Z`)
  if (!/^\d{4}-\d{2}-\d{2}$/.test(metric.statDate)
      || Number.isNaN(parsedDate.getTime())
      || parsedDate.toISOString().slice(0, 10) !== metric.statDate) {
    throw new Error(`${metric.keyword} 缺少有效统计日期`)
  }
  if (!metric.source) throw new Error(`${metric.keyword} 缺少数据源`)
  if (!metric.region) throw new Error(`${metric.keyword} 缺少地区`)
  return metric
}

export function parseKeywordIndexInput(text, defaults = {}) {
  const source = String(text || '').replace(/^\uFEFF/, '').trim()
  if (!source) return []
  if (source.startsWith('[') || source.startsWith('{')) {
    const parsed = JSON.parse(source)
    const rows = Array.isArray(parsed) ? parsed : parsed.items
    if (!Array.isArray(rows)) throw new Error('JSON 必须是数组，或包含 items 数组')
    return rows.map((item) => normalizeMetric(item, defaults))
  }

  const lines = source.split(/\r?\n/).filter((line) => line.trim())
  if (lines.length < 2) throw new Error('CSV 至少需要表头和一行数据')
  const headers = splitCsvLine(lines[0])
  return lines.slice(1).map((line) => {
    const values = splitCsvLine(line)
    const item = Object.fromEntries(headers.map((header, index) => [header, values[index] ?? '']))
    return normalizeMetric(item, defaults)
  })
}

export function keywordIndexCsvTemplate() {
  return 'keyword,heatIndex,competitionIndex,opportunityIndex,statDate,source,region,trend\n'
    + 'S2B2B商城系统,82,61,76,2026-07-22,百度指数导出,全国,"[{""date"":""2026-07-22"",""value"":82}]"'
}
