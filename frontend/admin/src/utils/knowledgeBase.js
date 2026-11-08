export function buildKnowledgePersonaDraft(settings = {}) {
  const brandName = String(settings.brandName || settings.name || '').trim()
  const companyName = String(settings.clientName || brandName).trim()
  const services = Array.isArray(settings.monitorSeeds)
    ? settings.monitorSeeds
      .map((item) => String(item?.text ?? item ?? '').trim())
      .filter(Boolean)
    : []
  return {
    kbName: brandName ? `${brandName}企业知识库` : '',
    companyName,
    companyShortName: brandName,
    creationDirections: ['品牌曝光', 'SEO/GEO'],
    copyTypes: ['软文', '问答', '产品说明'],
    productService: services.join('、'),
  }
}

const PERSONA_DRAFT_FIELDS = [
  'kbName',
  'companyName',
  'companyShortName',
  'creationDirections',
  'copyTypes',
  'productService',
  'brandStory',
  'trustEndorsement',
  'otherInfo',
  'productPainPoints',
  'userPainPoints',
  'customerCases',
  'tags',
  'visibility',
]

export function mergeKnowledgePersonaDraft(base = {}, websitePersona = {}) {
  const merged = { ...(base || {}) }
  for (const field of PERSONA_DRAFT_FIELDS) {
    const value = websitePersona?.[field]
    if (Array.isArray(value) && value.length) {
      merged[field] = [...value]
    } else if (typeof value === 'string' && value.trim()) {
      merged[field] = value
    }
  }
  return merged
}

const PERSONA_TEXT_FIELDS = [
  'kbName',
  'companyName',
  'companyShortName',
  'productService',
  'brandStory',
  'trustEndorsement',
  'otherInfo',
  'productPainPoints',
  'userPainPoints',
  'customerCases',
]

export function isKnowledgePersonaEqual(left = {}, right = {}) {
  const textEqual = PERSONA_TEXT_FIELDS.every(
    (field) => String(left?.[field] ?? '') === String(right?.[field] ?? '')
  )
  if (!textEqual) return false

  return ['creationDirections', 'copyTypes'].every((field) => {
    const leftValues = Array.isArray(left?.[field]) ? left[field] : []
    const rightValues = Array.isArray(right?.[field]) ? right[field] : []
    return leftValues.length === rightValues.length
      && leftValues.every((value, index) => value === rightValues[index])
  })
}

export function isKnowledgeReadbackEqual(payload = {}, items = [], id = null) {
  const expectedTitle = String(payload.title || '').trim()
  const expectedContent = String(payload.content || '').trim()
  const expectedType = String(payload.itemType || payload.categoryRaw || payload.category || '').trim()
  return items.some((item) => {
    if (id != null && String(item.id) !== String(id)) return false
    const actualTypes = [item.categoryRaw, item.itemType, item.type, item.category]
      .map((value) => String(value || '').trim())
      .filter(Boolean)
    return String(item.title || '').trim() === expectedTitle
      && String(item.content || '').trim() === expectedContent
      && actualTypes.includes(expectedType)
  })
}
