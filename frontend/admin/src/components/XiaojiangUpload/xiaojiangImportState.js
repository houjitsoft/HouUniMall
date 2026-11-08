const SYSTEM_KEYS = ['mall', 'crm', 'mes', 'pm', 'erp', 'finance', 'supplier', 'payment', 'oa', 'geo', 'aicode']
const SYSTEM_NAMES = new Map([
  ['mall', 'mall'], ['商城', 'mall'], ['crm', 'crm'],
  ['mes', 'mes'], ['智能制造', 'mes'], ['生产制造', 'mes'],
  ['pm', 'pm'], ['项目管理', 'pm'], ['erp', 'erp'],
  ['finance', 'finance'], ['财务中心', 'finance'],
  ['supplier', 'supplier'], ['供应商', 'supplier'],
  ['payment', 'payment'], ['支付中心', 'payment'],
  ['oa', 'oa'], ['oa办公', 'oa'], ['geo', 'geo'], ['aicode', 'aicode']
])

export function menuSystemKey(menu = {}) {
  const path = String(menu.paths ?? menu.path ?? '').trim().toLowerCase()
  const pathKey = SYSTEM_KEYS.find((key) =>
    path === `/${key}`
    || path.startsWith(`/${key}/`)
    || path === `/plugin/${key}`
    || path.startsWith(`/plugin/${key}/`)
  )
  if (pathKey) return pathKey
  if (path === '/plugin/unipay' || path.startsWith('/plugin/unipay/')
    || path === '/shell/payment' || path.startsWith('/shell/payment/')) {
    return 'payment'
  }
  const name = String(menu.name ?? '').trim().toLowerCase()
  if ([
    '商城概览', '商品管理', '订单管理', '用户管理', '营销活动', '店铺管理',
    '库存管理', '售后服务', '物流管理', '装修管理', '数据统计'
  ].includes(name)) {
    return 'mall'
  }
  if (name === '供应商管理') return 'supplier'
  if (name === 'unipay') return 'payment'
  return SYSTEM_NAMES.get(name) || ''
}

export function derivePermittedSystems(menus = [], catalogSystems = []) {
  const permitted = new Set(
    menus
      .filter((menu) => (menu.pid ?? menu.parentId) !== undefined)
      .filter((menu) => (menu.is_show ?? menu.isShow) !== undefined)
      .filter((menu) => Number(menu.pid ?? menu.parentId) === 0
        && String(menu.type ?? menu.menuType ?? '').toUpperCase() === 'M'
        && Number(menu.is_show ?? menu.isShow) === 1)
      .map(menuSystemKey)
      .filter(Boolean)
  )
  return catalogSystems.filter((system) => permitted.has(system.key))
}

export function createImportState() {
  return {
    step: 'upload',
    draftNo: '',
    systemKey: '',
    description: '',
    fileList: [],
    items: []
  }
}

export function restoreDraftState(state, draft) {
  if (!draft) return state
  state.draftNo = draft.draftNo || state.draftNo
  state.systemKey = draft.systemKey || state.systemKey
  state.description = String(draft.description || '').trim()
  state.items = Array.isArray(draft.items) ? draft.items.map(normalizeItem) : []
  state.step = state.items.length > 0 ? 'preview' : 'upload'
  return state
}

export function normalizeItem(item = {}) {
  return {
    ...item,
    fields: { ...(item.fields || {}) },
    fieldSchema: Array.isArray(item.fieldSchema) ? [...item.fieldSchema] : [],
    status: item.status || 'recognized',
    saving: false
  }
}

export function switchItemTarget(item, target) {
  if (!item || !target) return item
  const previous = item.fields || {}
  item.functionKey = target.functionKey
  item.objectKey = target.objectKey
  item.functionName = target.functionName
  item.objectName = target.objectName
  item.fieldSchema = [...(target.fields || [])]
  item.fields = Object.fromEntries(item.fieldSchema.map((field) => [
    field.name,
    Object.prototype.hasOwnProperty.call(previous, field.name) ? previous[field.name] : ''
  ]))
  item.validationErrors = []
  return item
}

export function applyBatchResults(items, results = []) {
  const byId = new Map(results.map((result) => [Number(result.itemId), result]))
  const remaining = []
  for (const item of items) {
    const result = byId.get(Number(item.itemId))
    if (result?.status === 'success') continue
    if (result) {
      item.status = 'failed'
      item.errorMessage = result.message || result.errorMessage || '入库失败'
      item.confirming = false
    }
    remaining.push(item)
  }
  items.splice(0, items.length, ...remaining)
  return items
}

export function isRecognitionReady(state) {
  return Boolean(state.systemKey && state.description.trim() && state.fileList.length)
}
