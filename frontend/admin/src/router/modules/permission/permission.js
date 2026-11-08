/**
 * 权限管理路由配置
 * 
 * @author Amos
 * @since 2025-01-07
 */

/**
 * 权限管理路由配置
 */
export const permissionRoutes = [
  {
    path: '/permission',
    name: 'Permission',
    component: () => import('@/views/permission/PermissionIndex.vue'),
    meta: {
      title: '权限管理',
      icon: 'Lock',
      permission: {
        resourceCode: 'auth:permission',
        type: 'READ'
      }
    }
  },
  {
    path: '/permission/management',
    name: 'PermissionManagement',
    component: () => import('@/views/permission/PermissionManagement.vue'),
    meta: {
      title: '权限管理',
      icon: 'Lock',
      permission: {
        resourceCode: 'auth:permission:management',
        type: 'READ'
      }
    }
  },
  {
    path: '/permission/config',
    name: 'PermissionConfig',
    component: () => import('@/views/permission/PermissionConfig.vue'),
    meta: {
      title: '权限配置',
      icon: 'Setting',
      permission: {
        resourceCode: 'auth:permission:config',
        type: 'READ'
      }
    }
  }
]

/**
 * 权限管理菜单配置
 */
export const permissionMenuConfig = {
  title: '权限管理',
  icon: 'Lock',
  path: '/permission',
  permission: 'auth:permission:read',
  children: [
    {
      title: '权限管理',
      icon: 'Lock',
      path: '/permission/management',
      permission: 'auth:permission:management:read'
    },
    {
      title: '权限配置',
      icon: 'Setting',
      path: '/permission/config',
      permission: 'auth:permission:config:read'
    }
  ]
}

/**
 * 权限管理面包屑配置
 */
export const permissionBreadcrumbConfig = {
  '/permission': [
    { title: '首页', path: '/' },
    { title: '权限管理', path: '/permission' }
  ],
  '/permission/management': [
    { title: '首页', path: '/' },
    { title: '权限管理', path: '/permission' },
    { title: '权限管理', path: '/permission/management' }
  ],
  '/permission/config': [
    { title: '首页', path: '/' },
    { title: '权限管理', path: '/permission' },
    { title: '权限配置', path: '/permission/config' }
  ]
}

export default permissionRoutes