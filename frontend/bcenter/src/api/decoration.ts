import request from '@/utils/request'

// 页面装修详情
export function getDecoratePages(params: any) {
    return request.get({ url: '/mall.shopDecorate/get', params }, { ignoreCancelToken: true })
}

// 页面装修保存
export function setDecoratePages(params: any) {
    return request.post({ url: '/mall.shopDecorate/save', params })
}
export function getHomeLists(params?: any) {
    return request.get({ url: '/mall.shopDecorate/home_lists', params })
}
export function delPages(params?: any) {
    return request.post({ url: '/mall.shopDecorate/delete', params })
}
export function addPages(params?: any) {
    return request.post({ url: '/mall.shopDecorate/add', params })
}
export function setpageHome(params?: any) {
    return request.post({ url: '/mall.shopDecorate/set_home', params })
}
