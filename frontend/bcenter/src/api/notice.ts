import request from '@/utils/request'

export function getNotice(params?: any) {
    return request.get({ url: '/mall.announcement/newest', params })
}
export function NoticeLists(params?: any) {
    return request.get({ url: '/mall.announcement/lists', params })
}
export function NoticeDetial(params?: any) {
    return request.get({ url: '/mall.announcement/detail', params })
}
