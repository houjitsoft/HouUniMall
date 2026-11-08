import request from '@/utils/request'
import type {SeckillListsQueryType, SeckillFormType} from "./seckill.d"


// 秒杀列表
export function getSeckillLists(params: SeckillListsQueryType) {
    return request.get({url: '/mall.ms/lists', params})
}

// 秒杀详情
export function getSeckillDetail(params: { id: number }) {
    return request.get({url: '/mall.ms/detail', params})
}

// 秒杀新增
export function apiSeckillAdd(params: SeckillFormType) {
    return request.post({url: '/mall.ms/add', params})
}

// 秒杀编辑
export function apiSeckillEdit(params: SeckillFormType) {
    return request.post({url: '/mall.ms/edit', params})
}

// 秒杀删除
export function apiSeckillDelete(params: { id: number }) {
    return request.post({url: '/mall.ms/delete', params})
}

// 秒杀提交
export function apiSeckillSubmit(params: { id: number }) {
    return request.post({url: '/mall.ms/submit', params})
}

// 秒杀开始活动
export function apiSeckillOpen(params: { id: number }) {
    return request.post({url: '/mall.ms/open', params})
}

// 秒杀结束活动
export function apiSeckillClose(params: { id: number }) {
    return request.post({url: '/mall.ms/close', params})
}