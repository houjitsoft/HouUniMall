import request from '@/utils/request'

// 拼团商品列表
export function getPTgoodsList(params: any) {
    return request.get({ url: '/mall.pt/lists', params })
}

// 拼团商品详情
export function PTgoodsDetial(params: any) {
    return request.get({ url: '/mall.pt/detail', params })
}

//拼团商品创建
export function addPTGoods(params: any) {
    return request.post({ url: '/mall.pt/add', params })
}

//拼团商品编辑
export function editPTGoods(params: any) {
    return request.post({ url: '/mall.pt/edit', params })
}

//拼团商品删除
export function delPTGoods(params: any) {
    return request.post({ url: '/mall.pt/delete', params })
}

//拼团商品提交审核
export function PTGoodsSubmit(params: any) {
    return request.post({ url: '/mall.pt/submit', params })
}

//拼团商品开始活动
export function PTGoodsBegin(params: any) {
    return request.post({ url: '/mall.pt/open', params })
}

//拼团商品结束活动
export function PTGoodsOver(params: any) {
    return request.post({ url: '/mall.pt/close', params })
}

//拼团列表
export function getPTList(params: any) {
    return request.get({ url: '/mall.ptOpen/lists', params })
}

//拼团详情
export function getPTDetail(params: any) {
    return request.get({ url: '/mall.ptOpen/detail', params })
}

export function PTOver(params: any) {
    return request.post({ url: '/mall.ptOpen/close', params })
}
