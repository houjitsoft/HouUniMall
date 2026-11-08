// 商品管理路由模块
export default {
  path: '/goods',
  meta: { title: '商品管理', icon: 'Goods' },
  children: [
    {
      path: '',
      redirect: '/goods/list'
    },
    {
      path: '/goods/list',
      component: () => import('@/views/goods/GoodsList.vue'),
      meta: { title: '商品列表' }
    },
    {
      path: '/goods/detail/:id',
      component: () => import('@/views/goods/GoodsDetail.vue'),
      meta: { title: '商品详情' }
    },
    {
      path: '/goods/create',
      component: () => import('@/views/goods/GoodsDetail.vue'),
      meta: { title: '新增商品' }
    },
    {
      path: '/goods/category',
      component: () => import('@/views/goods/CategoryList.vue'),
      meta: { title: '商品分类' }
    },
    {
      path: '/goods/brand',
      component: () => import('@/views/goods/BrandList.vue'),
      meta: { title: '商品品牌' }
    },
    {
      path: '/goods/attribute',
      component: () => import('@/views/goods/AttributeList.vue'),
      meta: { title: '商品属性' }
    },
    {
      path: '/goods/spec',
      component: () => import('@/views/goods/SpecList.vue'),
      meta: { title: '商品规格' }
    },
    {
      path: '/goods/stock',
      component: () => import('@/views/goods/StockList.vue'),
      meta: { title: '库存管理' }
    },
    {
      path: '/goods/statistics',
      component: () => import('@/views/goods/GoodsStatistics.vue'),
      meta: { title: '统计分析' }
    }
  ]
}