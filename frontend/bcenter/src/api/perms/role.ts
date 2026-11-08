import request from '@/utils/request'

// 角色列表
export function roleLists(params: any) {
    return request.get({ url: '/mall.shopRole/lists', params })
}
// 角色列表全部
export function roleAll(params: any) {
    return request.get({ url: '/mall.shopRole/other_lists', params })
}
// 添加角色
export function roleAdd(params: any) {
    return request.post({ url: '/mall.shopRole/add', params })
}
// 编辑角色
export function roleEdit(params: any) {
    return request.post({ url: '/mall.shopRole/edit', params })
}
// 删除角色
export function roleDelete(params: any) {
    return request.post({ url: '/mall.shopRole/delete', params })
}

// 角色详情
export function roleDetail(params: any) {
    return request.get({ url: '/mall.shopRole/detail', params })
}

// 分配权限
export function authMenuSet(params: any) {
    return request.post({ url: '/mall.shopRole/set_menu', params })
}

// 权限列表
export function authMenuLists() {
    return request.get({ url: '/index/menu_list' })
}