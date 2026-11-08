import config from '@/config'
import request from '@/utils/request'

// 登录
export function login(params: Record<string, any>) {
    return request.post({ url: '/login/login', params: { ...params, terminal: config.terminal } })
}

// 退出登录
export function logout() {
    return request.post({ url: '/login/out' })
}

// 用户信息
export function getUserInfo() {
    return request.get({ url: '/index/mySelf' })
}

// 获取管理员信息
export function getShopUserInfo() {
    return request.post({ url: '/mall.shop/account_detail' })
}

// 编辑管理员信息
export function setUserInfo(params: any) {
    return request.post({ url: '/mall.shop/account_update', params })
}
