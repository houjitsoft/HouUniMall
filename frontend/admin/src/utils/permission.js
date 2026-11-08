/**
 * 权限工具函数
 * 按钮权限优先读 sessionStorage（服务端实时刷新），不持久化到 localStorage，避免撤权后旧缓存仍生效
 */

function readLivePermissions() {
  try {
    const sessionRaw = sessionStorage.getItem('permissions')
    if (sessionRaw) {
      const parsed = JSON.parse(sessionRaw)
      if (Array.isArray(parsed)) return parsed
    }
    const legacy = localStorage.getItem('permissions')
    if (legacy) {
      const parsed = JSON.parse(legacy)
      if (Array.isArray(parsed)) return parsed
    }
  } catch {
    /* ignore */
  }
  return []
}

/**
 * 权限工具类
 */
export class PermissionUtils {
  /**
   * 获取权限类型描述
   * @param {string} permissionType - 权限类型
   * @returns {string} 权限类型描述
   */
  static getPermissionTypeDescription(permissionType) {
    const descriptionMap = {
      'READ': '查看',
      'WRITE': '编辑',
      'DELETE': '删除',
      'EXECUTE': '执行'
    }
    return descriptionMap[permissionType] || permissionType
  }

  /**
   * 检查是否有权限
   * @param {string|Array} permission - 权限标识
   * @returns {boolean}
   */
  static hasPermission(permission) {
    const permissions = readLivePermissions()
    
    if (Array.isArray(permission)) {
      return permission.some(p => permissions.includes(p))
    }
    
    return permissions.includes(permission)
  }

  /**
   * 检查是否有所有权限
   * @param {Array} permissions - 权限标识数组
   * @returns {boolean}
   */
  static hasAllPermissions(permissions) {
    const userPermissions = readLivePermissions()
    return permissions.every(p => userPermissions.includes(p))
  }

  /**
   * 检查是否有角色
   * @param {string|Array} role - 角色标识
   * @returns {boolean}
   */
  static hasRole(role) {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]')
    
    if (Array.isArray(role)) {
      return role.some(r => roles.includes(r))
    }
    
    return roles.includes(role)
  }

  /**
   * 检查是否为超级管理员
   * @returns {boolean}
   */
  static isSuperAdmin() {
    return PermissionUtils.hasRole('super_admin') || PermissionUtils.hasRole('admin')
  }
}

/**
 * 检查是否有权限
 * @param {string|Array} permission - 权限标识
 * @returns {boolean}
 */
export function hasPermission(permission) {
  return PermissionUtils.hasPermission(permission)
}

/**
 * 检查是否有所有权限
 * @param {Array} permissions - 权限标识数组
 * @returns {boolean}
 */
export function hasAllPermissions(permissions) {
  return PermissionUtils.hasAllPermissions(permissions)
}

/**
 * 检查是否有角色
 * @param {string|Array} role - 角色标识
 * @returns {boolean}
 */
export function hasRole(role) {
  return PermissionUtils.hasRole(role)
}

/**
 * 检查是否为超级管理员
 * @returns {boolean}
 */
export function isSuperAdmin() {
  return PermissionUtils.isSuperAdmin()
}

/**
 * 权限指令
 */
export const permissionDirective = {
  mounted(el, binding) {
    const { value } = binding
    
    if (value) {
      const hasAuth = PermissionUtils.hasPermission(value)
      
      if (!hasAuth) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}

export default {
  PermissionUtils,
  hasPermission,
  hasAllPermissions,
  hasRole,
  isSuperAdmin
}