import request from '@/utils/request'

// 折扣详情
export function getDiscountDetail(params: { id: number }) {
    return request.get({ url: '/mall.goods/discount_detail', params })
}

// 折扣状态
export function discountStatus(params: any) {
    return request.post({ url: '/mall.goods/discount_set', params })
}

// 折扣保存
export function discountSave(params: any) {
    return request.post({ url: '/mall.goods/discount_save', params })
}