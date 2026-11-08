import request from '@/utils/request'

// 分销概览
export function getDistributionOverView(params?: any) {
    return request.get({ url: '/mall.userDistribution/count', params })
}

//分销订单列表
export function getOrderList(params?: any) {
    return request.get({ url: '/mall.distributionOrder/lists', params })
}

//参与/不参与 分销
export function statusChange(params?: any) {
    return request.post({ url: '/mall.goods/distribution_set', params })
}

//商品分销详情
export function distributionDetail(params?: any) {
    return request.get({ url: '/mall.goods/distribution_detail', params })
}

//商品分销保存
export function distributionSave(params?: any) {
    return request.post({ url: '/mall.goods/distribution_save', params })
}
