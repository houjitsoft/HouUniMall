// 平台管理路由：插件中心、市场、授权与订单统一归属平台后台。
export default {
  path: '/platform',
  meta: { title: '平台管理', icon: 'Setting' },
  children: [
    { path: '/platform/plugins', component: () => import('@/views/platform/plugins/index.vue'), meta: { title: '插件中心' } },
    { path: '/platform/plugins/create', component: () => import('@/views/platform/plugins/create.vue'), meta: { title: '创建插件', hidden: true } },
    { path: '/platform/plugins/market', component: () => import('@/views/platform/plugins/market/index.vue'), meta: { title: '插件市场' } },
    { path: '/platform/plugins/market/detail', component: () => import('@/views/platform/plugins/market/detail.vue'), meta: { title: '插件详情', hidden: true } },
    { path: '/platform/plugins/market/checkout', component: () => import('@/views/platform/plugins/market/checkout.vue'), meta: { title: '购买插件', hidden: true } },
    { path: '/platform/plugins/licenses', component: () => import('@/views/platform/plugins/market/licenses.vue'), meta: { title: '授权管理' } },
    { path: '/platform/plugins/orders', component: () => import('@/views/platform/plugins/market/orders.vue'), meta: { title: '插件订单' } }
  ]
}
