import request from '@/utils/request'
import type { FreightModal } from "./freight.d"

// 运费模版列表
export function getFreightLists(params: { page_no: number, page_size: number }) {
    return request.get({ url: '/mall.expressTpl/lists', params })
}

// 运费模版详情
export function getFreightDetail(params: { id: number }) {
    return request.get({ url: '/mall.expressTpl/detail', params })
}

// 运费模版删除
export function apiFreightDel(params: { id: number }) {
    return request.post({ url: '/mall.expressTpl/delete', params })
}

// 运费模版新增
export function apiFreightAdd(params: FreightModal) {
    return request.post({ url: '/mall.expressTpl/add', params })
}

// 运费模版编辑
export function apiFreightEdit(params: FreightModal) {
    return request.post({ url: '/mall.expressTpl/edit', params })
}
