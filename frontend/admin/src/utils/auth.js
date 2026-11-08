/**
 * 认证相关工具函数
 */

import { clearCurrentOrgId, syncCurrentOrgFromUserInfo } from '@/utils/orgUtils'

// 获取token
export const getToken = () => {
  return localStorage.getItem('token') || sessionStorage.getItem('token') || ''
}

/** JWT payload 使用 base64url，登录时 subject 即 userId */
function decodeJwtPayload(token) {
  if (!token || typeof token !== 'string') return null
  const parts = token.split('.')
  if (parts.length !== 3) return null
  try {
    let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const pad = base64.length % 4
    if (pad) base64 += '='.repeat(4 - pad)
    return JSON.parse(atob(base64))
  } catch {
    return null
  }
}

/** 从 JWT subject 解析 userId（cloud 登录 createJWT(userId)） */
export const parseUserIdFromToken = (token) => {
  const payload = decodeJwtPayload(token)
  const sub = payload?.sub
  return sub != null && String(sub).trim() ? String(sub).trim() : ''
}

/** 规范化 userInfo 中的 userId 并写入 localStorage */
export const normalizeUserIdInUserInfo = (userInfo, token = getToken()) => {
  if (!userInfo || typeof userInfo !== 'object') return userInfo

  let userId = userInfo.userId ?? userInfo.user_id ?? userInfo.id
  if (userId != null && String(userId).trim()) {
    userId = String(userId).trim()
  } else {
    userId = parseUserIdFromToken(token || userInfo.token)
  }

  if (userId) {
    userInfo.userId = userId
    localStorage.setItem('userId', userId)
  }

  return userInfo
}

// 获取用户ID
export const getUserId = () => {
  const stored = localStorage.getItem('userId')
  if (stored && String(stored).trim()) {
    return String(stored).trim()
  }

  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      const id = userInfo.userId ?? userInfo.user_id ?? userInfo.id
      if (id != null && String(id).trim()) {
        return String(id).trim()
      }
    }
  } catch (error) {
    console.error('解析用户信息失败:', error)
  }

  return parseUserIdFromToken(getToken()) || ''
}

// 获取用户信息
export const getUserInfo = () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      return JSON.parse(userInfoStr)
    }
  } catch (error) {
    console.error('解析用户信息失败:', error)
  }
  return null
}

// 设置认证信息
export const setAuthInfo = (token, userInfo) => {
  if (token) {
    localStorage.setItem('token', token)
  }
  if (userInfo) {
    if (!userInfo.orgList && userInfo.organizationList) {
      userInfo.orgList = userInfo.organizationList
    }
    normalizeUserIdInUserInfo(userInfo, token)
    syncCurrentOrgFromUserInfo(userInfo)
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  }
}

// 清除认证信息
export const clearAuthInfo = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userId')
  clearCurrentOrgId()
}

// 检查是否已认证
export const isAuthenticated = () => {
  const token = getToken()
  return !!token
}