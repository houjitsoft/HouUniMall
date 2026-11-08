// AI管理路由模块
export default {
  path: '/ai',
  meta: { title: 'AI智能', icon: 'MagicStick' },
  children: [
    {
      path: '',
      redirect: '/ai/dashboard'
    },
    {
      path: '/ai/dashboard',
      component: () => import('@/views/ai/AiDashboard.vue'),
      meta: { title: 'AI概览' }
    },
    {
      path: '/ai/agents',
      component: () => import('@/views/ai/AgentList.vue'),
      meta: { title: '智能体管理' }
    },
    {
      path: '/ai/workflows',
      component: () => import('@/views/ai/WorkflowList.vue'),
      meta: { title: '工作流编排' }
    },
    {
      path: '/ai/workflow/design',
      component: () => import('@/views/ai/WorkflowDesigner.vue'),
      meta: { title: '工作流设计器' }
    },
    {
      path: '/ai/chat',
      component: () => import('@/views/ai/AiChat.vue'),
      meta: { title: '智能对话' }
    },
    {
      path: '/ai/knowledge',
      component: () => import('@/views/ai/AiKnowledge.vue'),
      meta: { title: '知识库' }
    },
    {
      path: '/ai/generation',
      component: () => import('@/views/ai/AiGeneration.vue'),
      meta: { title: '智能生成' }
    },
    {
      path: '/ai/models',
      component: () => import('@/views/ai/AiModels.vue'),
      meta: { title: '模型管理' }
    },
    {
      path: '/ai/prompts',
      component: () => import('@/views/ai/AiPrompts.vue'),
      meta: { title: '提示词库' }
    },
    {
      path: '/ai/settings',
      component: () => import('@/views/ai/AiSettings.vue'),
      meta: { title: '系统设置' }
    },
    {
      path: '/ai/usage',
      component: () => import('@/views/ai/AiUsage.vue'),
      meta: { title: '用量统计' }
    },
    {
      path: '/ai/balance',
      component: () => import('@/views/ai/AiBalance.vue'),
      meta: { title: '余额配额' }
    }
  ]
}