import request from '@/utils/request'

export function apimoneyLists(params?: any) {
    return request.get({ url: '/mall.shopAccountLog/lists', params })
}

export function apiGatherAdd(params?: any) {
    return request.post({ url: '/gather.gather/add', params })
}
export function apiGatherDeal(params?: any) {
    return request.post({ url: '/gather.gather/deal', params })
}
export function apiGatherDel(params?: any) {
    return request.post({ url: '/gather.gather/delete', params })
}
export function apiGatherDetial(params?: any) {
    return request.get({ url: '/gather.gather/detail', params })
}
export function apiGatherlists(params?: any) {
    return request.get({ url: '/gather.gather/lists', params })
}
export function apiGathergooddetial(params?: any) {
    return request.get({ url: '/gather.gatherGoods/detail', params })
}
export function apiGathergoodSave(params?: any) {
    return request.post({ url: '/gather.gatherGoods/edit_goods', params })
}
export function apiGatherSavegood(params?: any) {
    return request.post({ url: '/gather.gatherGoods/save_goods', params })
}
