import request from '@/utils/request'

// 配置
export function getConfig() {
    return request.get({ url: '/config/getConfig' })
}

// 工作台主页
export function getWorkbench() {
    return request.get({ url: '/mall.workbench/index' })
}

//字典数据
export function getDictData(params: any) {
    return request.get({ url: '/config/dict', params })
}

//省市区
export function getAreaLists() {
    return request.get({ url: '/area/lists_all' })
}

//搜索地址
export function apiMapRegionSearch(params: any) {
    return request.post({ url: '/area/tencent_search', params })
}
