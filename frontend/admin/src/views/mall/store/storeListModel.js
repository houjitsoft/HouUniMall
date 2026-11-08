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
  if (queryParams.type !== '') params.shopType = Number(queryParams.type)
  if (queryParams.shop_category_id !== undefined && queryParams.shop_category_id !== '') {
    params.shopCategoryId = Number(queryParams.shop_category_id)
  }
  if (queryParams.shop_status !== '') params.status = Number(queryParams.shop_status)
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
      shop_status: row.shop_status ?? row.shopStatus ?? (Number(row.status) === 1 ? 1 : 0)
    })),
    total: Number(data?.total ?? data?.count ?? 0)
  }
}

export function normalizeShopCategoryPage(data) {
  return data?.records || data?.lists || (Array.isArray(data) ? data : [])
}
