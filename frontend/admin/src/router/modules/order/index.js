// 订单管理路由模块
export default {
  path: '/orders',
  meta: { title: '订单管理', icon: 'Document' },
  children: [
    {
      path: '',
      redirect: '/orders/list'
    },
    {
      path: '/orders/list',
      component: () => import('@/views/order/OrderList.vue'),
      meta: { title: '订单列表' }
    },
    {
      path: '/orders/detail',
      component: () => import('@/views/order/OrderDetail.vue'),
      meta: { title: '订单详情' }
    },
    {
      path: '/orders/cart',
      component: () => import('@/views/order/CartList.vue'),
      meta: { title: '购物车管理' }
    },
    {
      path: '/orders/return',
      component: () => import('@/views/order/ReturnList.vue'),
      meta: { title: '退换货管理' }
    },
    {
      path: '/orders/statistics',
      component: () => import('@/views/order/OrderStatistics.vue'),
      meta: { title: '订单统计' }
    },
    {
      path: '/orders/analysis',
      component: () => import('@/views/order/OrderAnalysis.vue'),
      meta: { title: '订单分析' }
    },
    {
      path: '/orders/exception',
      component: () => import('@/views/order/OrderException.vue'),
      meta: { title: '异常订单' }
    }
  ]
}