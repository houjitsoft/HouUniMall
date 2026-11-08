// 商品管理路由模块
export default {
  path: '/products',
  meta: { title: '商品管理', icon: 'Goods' },
  children: [
    {
      path: '',
      redirect: '/products/list'
    },
    {
      path: '/products/list',
      component: () => import('@/views/product/ProductList.vue'),
      meta: { title: '商品列表' }
    },
    {
      path: '/products/create',
      component: () => import('@/views/product/ProductCreate.vue'),
      meta: { title: '创建商品' }
    },
    {
      path: '/products/edit/:id',
      component: () => import('@/views/product/ProductEdit.vue'),
      meta: { title: '编辑商品' },
      props: true
    }
  ]
}