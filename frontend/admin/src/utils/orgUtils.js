/**
 * 从用户信息中解析主组织 ID
 * @param {Object|null} userInfo
 * @returns {string|null}
 */
export function resolveOrgIdFromUserInfo(userInfo) {
  if (!userInfo) return null

  const direct = userInfo.orgId ?? userInfo.org_id
  if (direct != null && String(direct).trim()) {
    return String(direct).trim()
  }

  const list = userInfo.orgList || userInfo.organizationList
  if (Array.isArray(list) && list.length > 0) {
    const primary = list.find((org) => org.is_primary === 1 || org.is_primary === '1' || org.is_primary === true) || list[0]
    const id = primary?.org_id ?? primary?.orgId ?? primary?.id
    if (id != null && String(id).trim()) {
      return String(id).trim()
    }
  }

  if (list && typeof list === 'object' && !Array.isArray(list)) {
    const keys = Object.keys(list).filter(Boolean)
    if (keys.length > 0) {
      return String(keys[0]).trim()
    }
  }

  return null
}

/**
 * 从 userInfo 判断是否已绑定组织（DB 有记录且接口带回 orgList/orgId 时为 true）
 * @param {Object|null} userInfo
 * @returns {boolean}
 */
export function userHasOrganization(userInfo) {
  if (!userInfo) return false

  const directOrg = userInfo.orgId ?? userInfo.org_id
  if (directOrg != null && String(directOrg).trim()) {
    return true
  }

  const orgList = userInfo.orgList ?? userInfo.organizationList
  if (orgList == null) return false

  if (Array.isArray(orgList)) {
    return orgList.length > 0 && orgList.some((org) => org && (org.org_id || org.orgId))
  }

  if (typeof orgList === 'object') {
    const keys = Object.keys(orgList)
    return keys.length > 0 && keys.some((key) => key && orgList[key])
  }

  return false
}

/**
 * orgList 未加载（queryUserInfo 失败或登录响应未带组织字段），不能当作「未绑定组织」
 * @param {Object|null} userInfo
 * @returns {boolean}
 */
export function userOrgStatusUnknown(userInfo) {
  if (!userInfo) return true

  const directOrg = userInfo.orgId ?? userInfo.org_id
  if (directOrg != null && String(directOrg).trim()) {
    return false
  }

  const orgList = userInfo.orgList ?? userInfo.organizationList
  return orgList == null
}

/**
 * 将组织列表合并进 userInfo 并写回 localStorage
 * @param {Object|null} userInfo
 * @param {Array|null} orgList
 * @returns {Object|null}
 */
export function mergeOrgListIntoUserInfo(userInfo, orgList) {
  if (!userInfo || !Array.isArray(orgList) || orgList.length === 0) {
    return userInfo
  }
  userInfo.orgList = orgList
  if (!userInfo.organizationList) {
    userInfo.organizationList = orgList
  }
  syncCurrentOrgFromUserInfo(userInfo)
  return userInfo
}

/**
 * 登录/刷新用户信息后，同步 currentOrgId 到 localStorage
 * @param {Object|null} userInfo
 * @param {boolean} force 是否强制覆盖已有 currentOrgId
 * @returns {string|null}
 */
export function syncCurrentOrgFromUserInfo(userInfo, force = false) {
  const resolved = resolveOrgIdFromUserInfo(userInfo)
  if (!resolved) return null

  const current = localStorage.getItem('currentOrgId')
  const list = userInfo?.orgList || userInfo?.organizationList
  let currentBelongsToUser = false
  if (Array.isArray(list) && list.length > 0) {
    currentBelongsToUser = list.some(
      (org) => String(org?.org_id ?? org?.orgId ?? org?.id ?? '').trim() === String(current || '').trim()
    )
  } else if (list && typeof list === 'object' && !Array.isArray(list)) {
    currentBelongsToUser = Object.prototype.hasOwnProperty.call(list, current || '')
  }
  // orgList 尚未加载时，绝不能沿用上一账号残留的 currentOrgId（否则会串组织财务数据）
  // 登录新账号或切换账号时，不能沿用上一个账号遗留的组织 ID。
  if (force || !current || !String(current).trim() || !currentBelongsToUser) {
    setCurrentOrgId(resolved, true)
  }
  return resolved
}

/**
 * 获取已选择或从用户信息解析出的组织 ID，不伪造默认组织。
 * @returns {string|null}
 */
export function getCurrentOrgIdOrNull() {
  let userInfo = null
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) userInfo = JSON.parse(userInfoStr)
  } catch (error) {
    console.warn('从 userInfo 解析 orgId 失败:', error)
  }

  const stored = localStorage.getItem('currentOrgId') || sessionStorage.getItem('currentOrgId')
  const storedId = stored && String(stored).trim() ? String(stored).trim() : ''
  if (storedId && userInfo) {
    const list = userInfo.orgList || userInfo.organizationList
    let belongs = false
    if (Array.isArray(list) && list.length > 0) {
      belongs = list.some(
        (org) => String(org?.org_id ?? org?.orgId ?? org?.id ?? '').trim() === storedId
      )
    } else if (userInfo.orgId != null && String(userInfo.orgId).trim() === storedId) {
      belongs = true
    }
    if (belongs) return storedId
    // 残留组织属于上一账号时丢弃，避免下游（如小匠帮你传）被误判为无权限
  } else if (storedId && !userInfo) {
    return storedId
  }

  if (userInfo) {
    const resolved = resolveOrgIdFromUserInfo(userInfo)
    if (resolved) {
      setCurrentOrgId(resolved, true)
      return resolved
    }
  }

  return null
}

/**
 * 获取当前组织ID（登录后从 userInfo 自动推导，无需业务页手动传）
 * @returns {string|null} 当前组织ID，未确定时返回 null
 */
export function getCurrentOrgId() {
  return getCurrentOrgIdOrNull()
}

/**
 * 获取当前登录用户 ID（用于按用户持久化偏好）
 * @returns {string}
 */
export function getCurrentUserId() {
  try {
    const userInfoStr = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      const id = userInfo.userId ?? userInfo.user_id ?? userInfo.id
      if (id != null && String(id).trim()) {
        return String(id).trim()
      }
    }
  } catch (error) {
    console.warn('从 userInfo 解析 userId 失败:', error)
  }
  return '0'
}

/**
 * PM 模块：上次选中项目的 localStorage 键（按用户 + 组织）
 * @returns {string}
 */
export function getPmLastProjectStorageKey() {
  return `pm_last_project_${getCurrentUserId()}_${getCurrentOrgId() || 'unresolved'}`
}

/**
 * 读取当前用户上次选中的项目 ID
 * @returns {number|null}
 */
export function readPmLastProjectId() {
  const raw = localStorage.getItem(getPmLastProjectStorageKey())
  if (raw == null || raw === '') return null
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? n : null
}

/**
 * 保存当前用户上次选中的项目 ID
 * @param {number|string|null} projectId
 */
export function savePmLastProjectId(projectId) {
  const key = getPmLastProjectStorageKey()
  if (projectId != null && projectId !== '') {
    localStorage.setItem(key, String(projectId))
  } else {
    localStorage.removeItem(key)
  }
}

/**
 * 设置当前组织ID
 * @param {string} orgId - 组织ID
 * @param {boolean} persist - 是否持久化存储
 */
export function setCurrentOrgId(orgId, persist = true) {
  if (orgId == null || !String(orgId).trim()) return
  const normalized = String(orgId).trim()
  if (persist) {
    localStorage.setItem('currentOrgId', normalized)
  } else {
    sessionStorage.setItem('currentOrgId', normalized)
  }
}

/**
 * 清除当前组织ID
 */
export function clearCurrentOrgId() {
  localStorage.removeItem('currentOrgId')
  sessionStorage.removeItem('currentOrgId')
}

/**
 * 获取组织树形结构
 * @param {Array} orgList - 组织列表
 * @returns {Array} 树形结构的组织列表
 */
export function buildOrgTree(orgList) {
  if (!Array.isArray(orgList) || orgList.length === 0) {
    return []
  }

  const orgMap = {}
  orgList.forEach(org => {
    orgMap[org.id] = { ...org, children: [] }
  })

  const tree = []
  orgList.forEach(org => {
    if (org.parentId === 0 || org.parentId === null || org.parentId === undefined) {
      tree.push(orgMap[org.id])
    } else if (orgMap[org.parentId]) {
      orgMap[org.parentId].children.push(orgMap[org.id])
    }
  })

  return tree
}

/**
 * 根据组织ID查找组织信息
 * @param {Array} orgList - 组织列表
 * @param {string} orgId - 组织ID
 * @returns {Object|null} 组织信息
 */
export function findOrgById(orgList, orgId) {
  if (!Array.isArray(orgList) || !orgId) {
    return null
  }

  for (const org of orgList) {
    if (org.id === orgId) {
      return org
    }
    if (org.children && org.children.length > 0) {
      const found = findOrgById(org.children, orgId)
      if (found) {
        return found
      }
    }
  }

  return null
}

/**
 * 获取组织的完整路径（从根节点到当前节点）
 * @param {Array} orgList - 组织列表
 * @param {string} orgId - 组织ID
 * @returns {Array} 组织路径数组
 */
export function getOrgPath(orgList, orgId) {
  if (!Array.isArray(orgList) || !orgId) {
    return []
  }

  const path = []

  function findPath(orgs, targetId) {
    for (const org of orgs) {
      path.push(org)
      if (org.id === targetId) {
        return true
      }
      if (org.children && org.children.length > 0) {
        if (findPath(org.children, targetId)) {
          return true
        }
      }
      path.pop()
    }
    return false
  }

  findPath(orgList, orgId)
  return path
}

export default {
  resolveOrgIdFromUserInfo,
  userHasOrganization,
  userOrgStatusUnknown,
  mergeOrgListIntoUserInfo,
  syncCurrentOrgFromUserInfo,
  getCurrentOrgId,
  getCurrentUserId,
  getPmLastProjectStorageKey,
  readPmLastProjectId,
  savePmLastProjectId,
  setCurrentOrgId,
  clearCurrentOrgId,
  buildOrgTree,
  findOrgById,
  getOrgPath
}
