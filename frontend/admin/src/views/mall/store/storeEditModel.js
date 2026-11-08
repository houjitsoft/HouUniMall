export function normalizeStoreDetail(data = {}) {
  return {
    id: Number(data.id || 0),
    name: data.name || '',
    type: Number(data.type ?? 1),
    shopCategoryId: data.shopCategory?.id ?? data.shopCategoryId ?? null,
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
  return {
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
}
