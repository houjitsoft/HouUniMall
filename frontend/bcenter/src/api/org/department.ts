import request from '@/utils/request'

// 部门列表
export function deptLists(params?: any) {
    return request.get({ url: '/mall.shopDept/lists', params })
}

// 添加部门
export function deptAdd(params: any) {
    return request.post({ url: '/mall.shopDept/add', params })
}

// 编辑部门
export function deptEdit(params: any) {
    return request.post({ url: '/mall.shopDept/edit', params })
}

// 编辑顶级部门
export function deptTopEdit(params: any) {
    return request.post({ url: '/mall.shopDept/top_edit', params })
}

// 删除部门
export function deptDelete(params: any) {
    return request.post({ url: '/mall.shopDept/delete', params })
}

// 部门详情
export function deptDetail(params: any) {
    return request.get({ url: '/mall.shopDept/detail', params })
}
