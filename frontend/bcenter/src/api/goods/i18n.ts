import request from '@/utils/request'

/** 商品多语言：走 bcenter 商家专属接口，并由 BFF 校验商品归属。 */

export function getGoodsI18nList(commonId: number | string) {
    return request.get({ url: `/mall.Goods/i18n/${commonId}` })
}

export function saveGoodsI18n(commonId: number | string, data: { items: any[] }) {
    return request.post({
        url: `/mall.Goods/i18n/${commonId}/save`,
        data: { commonId, ...data }
    })
}

export function autoTranslateGoodsI18n(
    commonId: number | string,
    data: { langCodes: string[]; publish?: boolean }
) {
    return request.post({ url: `/mall.Goods/i18n/${commonId}/auto-translate`, data })
}
