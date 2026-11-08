export function normalizeGoodsI18nId(value) {
    if (value === undefined || value === null || value === '') return ''
    return String(value)
}

export function shouldReloadGoodsI18n(currentId, previousId) {
    const current = normalizeGoodsI18nId(currentId)
    return current !== '' && current !== normalizeGoodsI18nId(previousId)
}
