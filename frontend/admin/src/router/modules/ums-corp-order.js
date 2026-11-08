/**
 * 银联企业订单分账插件页面（宿主静态路由兜底）。
 * 必须用绝对 path，并注册在 PluginPending 通配之前，避免冷启动落到「正在加载应用」。
 */
export default {
  path: '/plugin/ums-corp-order',
  meta: { title: '银联企业订单分账', pluginCode: 'ums-corp-order', skipMenuCheck: true },
  children: [
    {
      path: '',
      redirect: '/plugin/ums-corp-order/config'
    },
    {
      path: '/plugin/ums-corp-order/config',
      name: 'UmsCorpOrderConfig',
      component: () => import('@/views/ums-corp-order/Config.vue'),
      meta: { title: '渠道配置', pluginCode: 'ums-corp-order', skipMenuCheck: true }
    },
    {
      path: '/plugin/ums-corp-order/orders',
      name: 'UmsCorpOrderOrders',
      component: () => import('@/views/ums-corp-order/Orders.vue'),
      meta: { title: '企业订单', pluginCode: 'ums-corp-order', skipMenuCheck: true }
    },
    {
      path: '/plugin/ums-corp-order/split',
      name: 'UmsCorpOrderSplit',
      component: () => import('@/views/ums-corp-order/Split.vue'),
      meta: { title: '划付分账', pluginCode: 'ums-corp-order', skipMenuCheck: true }
    },
    {
      path: '/plugin/ums-corp-order/ops',
      name: 'UmsCorpOrderOps',
      component: () => import('@/views/ums-corp-order/Ops.vue'),
      meta: { title: '操作记录', pluginCode: 'ums-corp-order', skipMenuCheck: true }
    },
    {
      path: '/plugin/ums-corp-order/balance',
      name: 'UmsCorpOrderBalance',
      component: () => import('@/views/ums-corp-order/Balance.vue'),
      meta: { title: '商户余额', pluginCode: 'ums-corp-order', skipMenuCheck: true }
    }
  ]
}
