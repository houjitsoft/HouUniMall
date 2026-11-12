export function normalizeStoreDetail(data = {}) {
  return {
    id: Number(data.id || 0),
    name: data.name || '',
    type: Number(data.type ?? 1),
    shopCategoryId: data.shopCategory?.id ?? data.shopCategoryId ?? null,
    entryType: data.entryType ?? null,
    shopLevelId: data.shopLevelId ?? null,
    contact: data.contact || '',
    mobile: data.mobile || '',
    addressInfo: data.addressInfo || '',
    lat: data.lat || '',
    lng: data.lng || '',
    shopStatus: Number(data.shopStatus ?? 1),
    businessStatus: Number(data.businessStatus ?? 1),
    loginAccount: data.account?.loginAccount || '-'
  }
}

export function buildStoreUpdatePayload(form) {
  const payload = {
    id: Number(form.id),
    name: form.name.trim(),
    type: Number(form.type),
    shopCategoryId: form.shopCategoryId || null,
    contact: form.contact.trim(),
    mobile: form.mobile.trim(),
    addressInfo: form.addressInfo.trim(),
    lat: form.lat?.trim() || '',
    lng: form.lng?.trim() || '',
    shopStatus: Number(form.shopStatus),
    businessStatus: Number(form.businessStatus)
  }
  if (form.entryType) payload.entryType = Number(form.entryType)
  if (form.shopLevelId) payload.shopLevelId = Number(form.shopLevelId)
  return payload
}
