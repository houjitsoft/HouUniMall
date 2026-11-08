// 用户管理路由模块
export default {
  path: '/users',
  meta: { title: '用户管理', icon: 'User' },
  children: [
    {
      path: '',
      redirect: '/users/list'
    },
    {
      path: '/users/list',
      component: () => import('@/views/user/UserList.vue'),
      meta: { title: '用户列表' }
    },
    {
      path: '/users/role-list',
      component: () => import('@/views/user/RoleList.vue'),
      meta: { title: '角色管理' }
    },
    // 会员用户服务页面路由
    {
      path: '/users/member/user',
      component: () => import('@/views/user/UserManage.vue'),
      meta: { title: '用户管理' }
    },
    {
      path: '/users/member/level',
      component: () => import('@/views/user/MemberLevel.vue'),
      meta: { title: '会员等级' }
    },
    {
      path: '/users/member/points',
      component: () => import('@/views/user/PointsManage.vue'),
      meta: { title: '积分管理' }
    },
    {
      path: '/users/member/behavior',
      component: () => import('@/views/user/UserBehavior.vue'),
      meta: { title: '用户行为' }
    },
    {
      path: '/users/member/statistics',
      component: () => import('@/views/user/UserStatistics.vue'),
      meta: { title: '用户统计' }
    }
  ]
}