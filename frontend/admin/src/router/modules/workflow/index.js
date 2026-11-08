// 工作流路由模块
export default {
  path: '/workflow',
  meta: { title: '工作流', icon: 'Connection' },
  children: [
    {
      path: '',
      redirect: '/workflow/dashboard'
    },
    {
      path: '/workflow/dashboard',
      component: () => import('@/views/workflow/WorkflowDashboard.vue'),
      meta: { title: '工作流概览' }
    },
    {
      path: '/workflow/processes',
      component: () => import('@/views/workflow/ProcessList.vue'),
      meta: { title: '流程管理' }
    },
    {
      path: '/workflow/applications',
      component: () => import('@/views/workflow/ApplicationList.vue'),
      meta: { title: '我的申请' }
    },
    {
      path: '/workflow/tasks',
      component: () => import('@/views/workflow/TaskList.vue'),
      meta: { title: '待办任务' }
    },
    {
      path: '/workflow/statistics',
      component: () => import('@/views/workflow/Statistics.vue'),
      meta: { title: '流程统计' }
    }
  ]
}