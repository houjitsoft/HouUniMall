/**
 * 生成从商品详情进入 IM 的路由。商品快照仅用于首条咨询卡片展示，
 * 后续商品详情仍以 goodsId 对应的商城数据为准。
 *
 * @param {Record<string, unknown>} product
 * @returns {string}
 */
export function buildConsultationChatPath(product = {}) {
    const shopId = toPositiveInteger(product.shopId)
    const goodsId = toPositiveInteger(product.goodsId)
    if (!shopId || !goodsId) return ''

    const params = new URLSearchParams({
        isMall: '1',
        shopId: String(shopId),
        consultGoodsId: String(goodsId)
    })

    appendValue(params, 'consultCommonId', toPositiveInteger(product.commonId))
    appendValue(params, 'consultName', cleanText(product.commonName, 160))
    appendValue(params, 'consultImage', cleanText(product.commonImage, 1200))

    const price = Number(product.commonPrice)
    if (Number.isFinite(price) && price >= 0) {
        params.set('consultPrice', String(price))
    }

    return `/user/chat?${params.toString()}`
}

function toPositiveInteger(value) {
    const number = Number(value)
    return Number.isInteger(number) && number > 0 ? number : 0
}

function cleanText(value, maxLength) {
    if (typeof value !== 'string') return ''
    return value.trim().slice(0, maxLength)
}

function appendValue(params, key, value) {
    if (value !== '' && value !== 0 && value != null) {
        params.set(key, String(value))
    }
}
