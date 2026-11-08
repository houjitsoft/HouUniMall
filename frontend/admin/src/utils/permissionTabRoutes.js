/** 权限管理模块顶部标签与路由映射 */
export const PERMISSION_TAB_ROUTES = {
  workbench: '/dashboard',
  menu: '/setting/permission/menu',
  role: '/setting/permission/role',
  member: '/setting/permission/member',
  admin: '/setting/permission/user'
}

export function getActivePermissionTab(path) {
  if (!path) return 'role'
  if (path.includes('/setting/permission/menu')) return 'menu'
  if (path.includes('/setting/permission/role')) return 'role'
  if (path.includes('/setting/permission/member') || path.includes('/setting/permission/employee')) return 'member'
  if (path.includes('/setting/permission/user')) return 'admin'
  if (path === '/dashboard' || path === '/') return 'workbench'
  return 'role'
}
