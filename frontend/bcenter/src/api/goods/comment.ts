import request from '@/utils/request'

/**
 * @return { Promise }
 * @description 获取评价列表
 */
export function getCommentLists(params: Record<any, string>) {
    return request.get({ url: '/mall.goodsComment/lists', params })
}

/**
 * @return { Promise }
 * @param { number } id
 * @param { number } is_show
 * @description 评价显示
 */
export function apiCommentShow(params: { id: number, is_show: number }) {
    return request.post({ url: '/mall.goodsComment/set_is_show', params })
}

/**
 * @return { Promise }
 * @param { number } id
 * @description 审核售后
 */
export function apiCommentReply(data: { id: number, reply_content?: string }) {
    return request.post({ url: '/mall.goodsComment/set_reply', data })
}