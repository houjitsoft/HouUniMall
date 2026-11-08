/**
 * GEO 页面在 Cloud 内的登录态适配层
 * 复用 Cloud 登录态（localStorage.token / userInfo），向 GEO 页面暴露兼容接口。
 */
import {
  getToken as getCloudToken,
  getUserInfo,
  setAuthInfo,
  clearAuthInfo,
  parseUserIdFromToken,
  normalizeUserIdInUserInfo
} from '@/utils/auth'
import { appUrl, toAppPath } from '@/utils/appBase'

export function getToken() {
  return getCloudToken()
}

export function isLoggedIn() {
  return !!getCloudToken()
}

export function getUser() {
  return getUserInfo()
}

export function setToken(token) {
  if (token) {
    localStorage.setItem('token', token)
  }
}

export function setUser(user) {
  if (user) {
    setAuthInfo(getCloudToken(), user)
  }
}

export function clearAuth() {
  clearAuthInfo()
}

export function normalizeAuthUser(user) {
  return normalizeUserIdInUserInfo(user, getCloudToken())
}

export function parseUserIdFromJwt(token) {
  return parseUserIdFromToken(token)
}

export function redirectToLogin() {
  // 子路径部署（/admin）下登录页真实地址是 /admin/login，
  // 且 redirect 参数必须是应用内路径（去掉 /admin 前缀），否则登录后落到 404
  const loginPath = appUrl('/login')
  if (window.location.pathname === loginPath) return
  const current = toAppPath(window.location.pathname)
  const redirect = encodeURIComponent(current + window.location.search)
  window.location.href = `${loginPath}?redirect=${redirect}`
}
