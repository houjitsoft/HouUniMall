// 数据中心路由模块
export default {
  path: '/dcenter',
  meta: { title: '数据中心', icon: 'DataAnalysis' },
  children: [
    {
      path: '',
      redirect: '/dcenter/dashboard'
    },
    {
      path: '/dcenter/dashboard',
      component: () => import('@/views/dcenter/DcenterDashboard.vue'),
      meta: { title: '数据中心' }
    },
    {
      path: '/dcenter/data-analysis',
      component: () => import('@/views/dcenter/DataAnalysis.vue'),
      meta: { title: '数据分析' }
    },
    {
      path: '/dcenter/system-config',
      component: () => import('@/views/dcenter/SystemConfig.vue'),
      meta: { title: '系统配置' }
    },
    {
      path: '/dcenter/log-management',
      component: () => import('@/views/dcenter/LogManagement.vue'),
      meta: { title: '日志管理' }
    },
    {
      path: '/dcenter/error-alert',
      component: () => import('@/views/dcenter/ErrorAlert.vue'),
      meta: { title: '运行告警' }
    }
  ]
}