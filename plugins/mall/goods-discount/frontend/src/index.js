import DiscountPanel from './views/DiscountPanel.vue'
import { setPluginContext } from './context.js'

export default {
  menus: [
    { path: '/plugin/goods-discount', title: '商品折扣', icon: 'PriceTag', sort: 3270 }
  ],
  routes: [
    {
      path: '/plugin/goods-discount',
      component: DiscountPanel,
      meta: { title: '商品折扣', pluginCode: 'goods-discount' }
    },
    {
      path: '/plugin/goods-discount/index',
      component: DiscountPanel,
      meta: { title: '商品折扣管理', pluginCode: 'goods-discount' }
    }
  ],
  setup(context) { setPluginContext(context) },
  teardown() { setPluginContext(null) }
}
