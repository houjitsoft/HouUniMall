import request from '@/utils/request'

// 快递公司版列表
export function getExpressCompanyLists() {
    return request.get({ url: '/mall.expressCompany/lists' })
}

// 配送方式配置
export function getDeliveryConfig() {
    return request.get({ url: '/mall.DeliveryType/get_config' })
}

export function setDeliveryConfig(params: Record<string, any>) {
    return request.post({ url: '/mall.DeliveryType/set_config', params })
}

// 物流追踪配置
export function getLogisticsConfig() {
    return request.get({ url: '/mall.DeliveryType/get_logistics_config' })
}

export function setLogisticsConfig(params: Record<string, any>) {
    return request.post({ url: '/mall.DeliveryType/set_logistics_config', params })
}
