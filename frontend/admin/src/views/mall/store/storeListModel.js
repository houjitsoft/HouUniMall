function parseBusinessCategories(value) {
  if (Array.isArray(value)) return value.join('、')
  if (typeof value !== 'string' || !value.trim()) return '-'

  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed.join('、') : value
  } catch {
    return value
  }
}

export function buildStoreListParams(queryParams, pagination) {
  const params = {
    page: pagination.pageNo,
    size: pagination.pageSize
  }

  if (queryParams.name?.trim()) params.shopName = queryParams.name.trim()
  if (queryParams.ownerAccount?.trim()) params.ownerAccount = queryParams.ownerAccount.trim()
  if (queryParams.entryType !== undefined && queryParams.entryType !== null && queryParams.entryType !== '') params.entryType = Number(queryParams.entryType)
  if (queryParams.shopLevelId !== undefined && queryParams.shopLevelId !== null && queryParams.shopLevelId !== '') params.shopLevelId = Number(queryParams.shopLevelId)
  if (queryParams.type !== undefined && queryParams.type !== '') params.shopType = Number(queryParams.type)
  if (queryParams.shop_category_id !== undefined && queryParams.shop_category_id !== '') {
    params.shopCategoryId = Number(queryParams.shop_category_id)
  }
  if (queryParams.shop_status !== undefined && queryParams.shop_status !== '') params.status = Number(queryParams.shop_status)
  if (queryParams.business_status !== undefined && queryParams.business_status !== '') {
    params.businessStatus = Number(queryParams.business_status)
  }

  return params
}

export function buildCreateStorePayload(form) {
  const payload = {
    name: form.name.trim(),
    type: Number(form.type),
    shopCategoryId: form.shopCategoryId || null,
    contact: form.contact.trim(),
    mobile: form.mobile.trim(),
    addressInfo: form.addressInfo.trim(),
    lat: form.lat?.trim() || null,
    lng: form.lng?.trim() || null,
    shopStatus: Number(form.shopStatus),
    businessStatus: Number(form.businessStatus),
    accountMode: form.accountMode || 'new'
  }

  if (form.entryType) payload.entryType = Number(form.entryType)
  if (form.shopLevelId) payload.shopLevelId = Number(form.shopLevelId)
  if (payload.accountMode === 'existing') {
    payload.platformUserId = form.platformUserId ? String(form.platformUserId) : null
    payload.accountIdentifier = null
    payload.accountPassword = null
    payload.accountPasswordAgain = null
  } else {
    payload.platformUserId = null
    payload.accountIdentifier = form.accountIdentifier?.trim() || ''
    payload.accountPassword = form.accountPassword || ''
    payload.accountPasswordAgain = form.accountPasswordAgain || ''
  }

  return payload
}

export function normalizeStorePage(data) {
  const rows = data?.records || data?.lists || []
  return {
    list: rows.map((row) => ({
      ...row,
      name: row.name || row.shopName || '',
      logo: row.logo || row.shopLogo || '',
      account: row.account || {
        loginAccount: row.merchantName || row.shopNo || '-'
      },
      shop_category: row.shop_category || row.shopCategory || {
        name: parseBusinessCategories(row.businessCategories)
      },
      type_text: row.type_text || row.typeText || (
        row.type !== undefined
          ? ({ 0: '官方自营', 1: '商家入驻' }[Number(row.type)] || '-')
          : ({ 1: '官方自营', 2: '商家入驻' }[Number(row.shopType)] || '-')
      ),
      business_status: row.business_status ?? row.businessStatus ?? (Number(row.status) === 1 ? 1 : 0),
      shop_status: row.shop_status ?? row.shopStatus ?? (Number(row.status) === 1 ? 1 : 0),
      // 自营/入驻(type)与个人/企业(entryType)是不同维度，缺失时不猜测。
      entry_type_text: ({ 1: '个人入驻', 2: '企业入驻' }[row.entryType] || '—'),
      level_name: row.levelName || row.storeLevel || '—'
    })),
    total: Number(data?.total ?? data?.count ?? 0)
  }
}

export function normalizeShopCategoryPage(data) {
  return data?.records || data?.lists || (Array.isArray(data) ? data : [])
}

export function maskStoreAccount(value) {
  const account = String(value ?? '').trim()
  if (!account || account === '-') return '—'
  if (account.includes('*')) return account
  if (/^1\d{10}$/.test(account)) return `${account.slice(0, 3)}****${account.slice(-4)}`
  const at = account.indexOf('@')
  if (at > 0) return `${account.slice(0, Math.min(2, at))}***${account.slice(at)}`
  return account
}

export function formatStoreDate(value) {
  if (value === null || value === undefined || value === '' || Number(value) === 0) return '—'
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}/.test(value)) return value.slice(0, 10)
  const numeric = Number(value)
  if (!Number.isFinite(numeric) || numeric <= 0) return '—'
  const date = new Date(numeric < 1e12 ? numeric * 1000 : numeric)
  if (Number.isNaN(date.getTime())) return '—'
  const parts = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit' }).formatToParts(date)
  const field = (type) => parts.find((part) => part.type === type)?.value
  return `${field('year')}-${field('month')}-${field('day')}`
}

export function storeAuditState(status) {
  if (status === null || status === undefined || status === '') return { label: '未记录', tone: 'info' }
  return ({
    0: { label: '待审核', tone: 'warning' },
    1: { label: '开店成功', tone: 'success' },
    2: { label: '审核未通过', tone: 'danger' }
  })[Number(status)] || { label: '未记录', tone: 'info' }
}

function csvCell(value) {
  let text = String(value ?? '')
  // 防止表格软件把商家名称、账号等外部输入识别成公式。
  if (/^[\s\u0000-\u001f]*[=+@-]/.test(text) || /^[\t\r\n]/.test(text)) text = `'${text}`
  return `"${text.replaceAll('"', '""')}"`
}

export function buildStoreExportCsv(rows) {
  const header = ['店铺id', '店主账号', '店铺名称', '店铺logo', '店铺类型', '店铺等级', '商家电话', '开店时间', '店铺曾用名', '审核状态']
  const values = rows.map((row) => [row.id, maskStoreAccount(row.account?.loginAccount), row.name, row.logo,
    row.entry_type_text, row.level_name, row.mobile, formatStoreDate(row.createTime), row.previousName,
    storeAuditState(row.auditStatus).label])
  return '\uFEFF' + [header, ...values].map((row) => row.map(csvCell).join(',')).join('\r\n')
}
