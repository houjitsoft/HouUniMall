import request from '@/utils/request'

// 岗位列表
export function jobsLists(params: any) {
    return request.get({ url: '/mall.shopJob/lists', params }, { ignoreCancelToken: true })
}

// 岗位列表全部
export function jobsAll(params: any) {
    return request.get({ url: '/mall.shopJob/other_lists', params })
}

// 添加岗位
export function jobsAdd(params: any) {
    return request.post({ url: '/mall.shopJob/add', params })
}

// 编辑岗位
export function jobsEdit(params: any) {
    return request.post({ url: '/mall.shopJob/edit', params })
}

// 删除岗位
export function jobsDelete(params: any) {
    return request.post({ url: '/mall.shopJob/delete', params })
}

// 岗位详情
export function jobsDetail(params: any) {
    return request.get({ url: '/mall.shopJob/detail', params })
}
