function normalizeItems(items) {
  const source = Array.isArray(items) ? items : String(items ?? '').split(/\r?\n/)
  const seen = new Set()
  const normalized = []
  for (const item of source) {
    const text = String(item ?? '').trim()
    const key = text.toLocaleLowerCase()
    if (!text || seen.has(key)) continue
    seen.add(key)
    normalized.push(text)
  }
  return normalized
}

export function mapMonitorScopeResponse(data = {}) {
  const savedKeywords = normalizeItems(data.keywords)
  const enabled = new Set(savedKeywords.map((item) => item.toLocaleLowerCase()))
  const disabledKeywords = normalizeItems(data.disabledKeywords)
    .filter((item) => !enabled.has(item.toLocaleLowerCase()))
  const monitorSeeds = normalizeItems(data.monitorSeeds)

  return {
    projectId: data.projectId,
    models: normalizeItems(data.models),
    keywords: savedKeywords,
    disabledKeywords,
    channels: normalizeItems(data.channels),
    monitorSeeds,
    inheritedBrandSeeds: false,
  }
}

export function buildMonitorScopePayload(scope = {}) {
  return {
    models: normalizeItems(scope.models),
    keywords: normalizeItems(scope.keywords),
    disabledKeywords: normalizeItems(scope.disabledKeywords)
      .filter((item) => !normalizeItems(scope.keywords).some(
        (keyword) => keyword.toLocaleLowerCase() === item.toLocaleLowerCase()
      )),
    channels: normalizeItems(scope.channels),
  }
}

export function isMonitorScopeReadbackEqual(payload, readback) {
  return JSON.stringify(buildMonitorScopePayload(payload)) === JSON.stringify(buildMonitorScopePayload(readback))
}

export function mergeMonitorScopeKeywords(current, brandSeeds) {
  const keywords = normalizeItems(current)
  const seen = new Set(keywords.map((item) => item.toLocaleLowerCase()))
  let added = 0
  for (const seed of normalizeItems(brandSeeds)) {
    const key = seed.toLocaleLowerCase()
    if (seen.has(key)) continue
    keywords.push(seed)
    seen.add(key)
    added++
  }
  return { keywords, added }
}

export function completeMonitorScopeMinimumKeywords(current, brandSeeds, minimum = 5) {
  const keywords = normalizeItems(current)
  const required = Math.max(0, Number(minimum) || 0)
  if (keywords.length >= required) {
    return { keywords, added: 0, missing: 0 }
  }
  const merged = mergeMonitorScopeKeywords(keywords, brandSeeds)
  return {
    keywords: merged.keywords,
    added: merged.added,
    missing: Math.max(0, required - merged.keywords.length),
  }
}

export function hasMonitorScopeChanges(original, current) {
  return !isMonitorScopeReadbackEqual(original, current)
}

export function getMonitorScopeDefaultSeed(project = {}) {
  const monitorSeeds = normalizeItems(project.monitorSeeds)
  return monitorSeeds[0] || project.brandName || project.name || project.domain || ''
}

export function assertSingleMonitorSeed(input) {
  const seed = String(input ?? '').trim()
  if (seed && /[,，;；\r\n]/.test(seed)) {
    throw new Error('一次请输入一个种子词')
  }
  return seed
}
