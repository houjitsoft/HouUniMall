export function buildPhysicalStoreListParams(query, pagination) {
  const params = { page: pagination.pageNo, size: pagination.pageSize }
  if (query.storeName?.trim()) params.storeName = query.storeName.trim()
  if (query.merchantId !== '') params.merchantId = Number(query.merchantId)
  if (query.businessStatus !== '') params.businessStatus = Number(query.businessStatus)
  if (query.riderDeliveryEnabled !== '') {
    params.riderDeliveryEnabled = Number(query.riderDeliveryEnabled)
  }
  if (query.posEnabled !== '') params.posEnabled = Number(query.posEnabled)
  return params
}

export function buildPhysicalStorePayload(form) {
  const payload = {
    merchantId: Number(form.merchantId),
    storeNo: form.storeNo.trim(),
    storeName: form.storeName.trim(),
    contactPerson: form.contactPerson.trim(),
    contactPhone: form.contactPhone.trim(),
    province: form.province.trim(),
    city: form.city.trim(),
    district: form.district.trim(),
    address: form.address.trim(),
    businessStatus: Number(form.businessStatus),
    riderDeliveryEnabled: Number(form.riderDeliveryEnabled),
    posEnabled: Number(form.posEnabled)
  }
  if (form.accountMode) {
    payload.accountMode = form.accountMode
    payload.platformUserId = form.accountMode === 'existing' && form.platformUserId
      ? String(form.platformUserId)
      : null
    payload.accountIdentifier = form.accountMode === 'new' ? form.accountIdentifier.trim() : null
    payload.accountPassword = form.accountMode === 'new' ? form.accountPassword : null
    payload.accountPasswordAgain = form.accountMode === 'new' ? form.accountPasswordAgain : null
  }
  return payload
}

export function normalizePhysicalStorePage(data) {
  return {
    list: Array.isArray(data?.records) ? data.records : [],
    total: Number(data?.total || 0)
  }
}
