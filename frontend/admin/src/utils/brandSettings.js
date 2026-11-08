function normalizeText(value) {
  return String(value ?? '').trim()
}

function normalizeComparable(value) {
  return normalizeText(value).toLocaleLowerCase()
}

function normalizeDomain(value) {
  let domain = normalizeText(value).toLocaleLowerCase()
    .replace(/^https?:\/\//, '')
    .replace(/^\/\//, '')
  const slash = domain.indexOf('/')
  if (slash >= 0) domain = domain.slice(0, slash)
  return domain.replace(/\.+$/, '')
}

function itemText(item) {
  return normalizeText(item && typeof item === 'object' ? item.text : item)
}

export function addBrandSettingItem(input, items, options = {}) {
  const text = normalizeText(input)
  const type = options.type === 'alias' ? 'alias' : 'seed'
  const label = type === 'alias' ? '品牌别名' : '业务种子词'
  if (!text) throw new Error(`${label}不能为空`)
  if (text.length > 128) throw new Error(`${label}不能超过128个字符`)
  if (/[,，;；\r\n]/.test(text)) {
    throw new Error(type === 'alias' ? '一次请输入一个品牌别名' : '一次请输入一个种子词')
  }
  const normalized = normalizeComparable(text)
  if ((items || []).some((item) => normalizeComparable(itemText(item)) === normalized)) {
    throw new Error(`${label}已存在`)
  }
  if (type === 'alias' && normalized === normalizeComparable(options.primaryBrand)) {
    throw new Error('品牌别名不能与品牌名称相同')
  }
  return [...(items || []).map(itemText), text]
}

export function addBrandSettingItems(input, items, options = {}) {
  const candidates = String(input ?? '')
    .split(/[,，;；\r\n]+/)
    .map(normalizeText)
    .filter(Boolean)
  if (!candidates.length) {
    const label = options.type === 'alias' ? '品牌别名' : '业务种子词'
    throw new Error(`${label}不能为空`)
  }

  let result = [...(items || []).map(itemText)]
  for (const candidate of candidates) {
    try {
      result = addBrandSettingItem(candidate, result, options)
    } catch (error) {
      if (!String(error?.message || '').endsWith('已存在')) throw error
    }
  }
  return result
}

export function mergeRecommendedSeeds(current, candidates) {
  const result = [...(current || []).map(itemText).filter(Boolean)]
  const seen = new Set(result.map(normalizeComparable))
  for (const candidate of candidates || []) {
    const text = normalizeText(
      candidate && typeof candidate === 'object' ? candidate.keyword ?? candidate.text : candidate
    )
    if (!text || seen.has(normalizeComparable(text))) continue
    result.push(text)
    seen.add(normalizeComparable(text))
  }
  return result
}

export function moveBrandSettingItem(items, index, offset) {
  const list = [...(items || [])]
  const nextIndex = index + offset
  if (index < 0 || index >= list.length || nextIndex < 0 || nextIndex >= list.length) {
    return list
  }
  const [item] = list.splice(index, 1)
  list.splice(nextIndex, 0, item)
  return list
}

export function updateBrandSettingItem(items, index, input, options = {}) {
  const list = [...(items || []).map(itemText)]
  if (index < 0 || index >= list.length) return list
  const withoutCurrent = list.filter((_, itemIndex) => itemIndex !== index)
  const validated = addBrandSettingItem(input, withoutCurrent, options).at(-1)
  list[index] = validated
  return list
}

export function mapBrandSettingsResponse(data = {}) {
  const monitorSeeds = [...(data.monitorSeeds || [])]
    .sort((left, right) => Number(left?.sortOrder || 0) - Number(right?.sortOrder || 0))
    .map(itemText)
    .filter(Boolean)
  const brandAliases = (data.brandAliases || []).map(itemText).filter(Boolean)
  return {
    id: data.id ?? null,
    name: data.name || '',
    brandName: data.brandName || '',
    domain: data.domain || '',
    clientName: data.clientName || '',
    monitorSeeds,
    brandAliases,
    status: data.status || 'active',
    updateTime: data.updateTime || ''
  }
}

export function buildBrandSettingsPayload(form = {}) {
  const seeds = (form.monitorSeeds || []).reduce(
    (result, item) => addBrandSettingItem(itemText(item), result, { type: 'seed' }),
    []
  )
  const aliases = (form.brandAliases || []).reduce(
    (result, item) => addBrandSettingItem(itemText(item), result, {
      type: 'alias',
      primaryBrand: form.brandName
    }),
    []
  )
  return {
    name: normalizeText(form.name),
    brandName: normalizeText(form.brandName),
    domain: normalizeDomain(form.domain),
    clientName: normalizeText(form.clientName),
    monitorSeeds: seeds,
    brandAliases: aliases
  }
}

export function isBrandSettingsReadbackEqual(payload, readback) {
  const expected = buildBrandSettingsPayload(payload)
  const actual = buildBrandSettingsPayload(mapBrandSettingsResponse(readback))
  const fieldsEqual = ['name', 'brandName', 'domain', 'clientName'].every(
    (key) => expected[key] === actual[key]
  )
  const seedsEqual = expected.monitorSeeds.length === actual.monitorSeeds.length &&
    expected.monitorSeeds.every((seed, index) => seed === actual.monitorSeeds[index])
  const expectedAliases = [...expected.brandAliases].sort()
  const actualAliases = [...actual.brandAliases].sort()
  const aliasesEqual = expectedAliases.length === actualAliases.length &&
    expectedAliases.every((alias, index) => alias === actualAliases[index])
  return fieldsEqual && seedsEqual && aliasesEqual
}
