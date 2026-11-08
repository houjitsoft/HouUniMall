// 认证管理路由模块
import { permissionRoutes } from './permission'

export default {
  path: '/auth',
  meta: { title: '认证服务', icon: 'Key' },
  children: [
    {
      path: '',
      redirect: '/login'
    },
    // 权限管理路由
    ...permissionRoutes
  ]
}

// 添加根级登录路由
export const loginRoute = {
  path: '/login',
  component: () => import('@/views/permission/Login.vue'),
  meta: { title: '登录' }
}

// 添加根级注册路由
export const registerRoute = {
  path: '/register',
  component: () => import('@/views/permission/Register.vue'),
  meta: { title: '注册' }
}

// 添加根级忘记密码路由
export const forgotPasswordRoute = {
  path: '/forgot-password',
  component: () => import('@/views/permission/ForgotPassword.vue'),
  meta: { title: '忘记密码', public: true }
}
