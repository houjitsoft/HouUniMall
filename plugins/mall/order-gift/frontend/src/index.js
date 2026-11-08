import GiftPanel from './views/GiftPanel.vue'
import { setPluginContext } from './context.js'

export default {
  menus: [
    { path: '/plugin/order-gift', title: '满赠', icon: 'Present', sort: 3330 }
  ],
  routes: [
    {
      path: '/plugin/order-gift',
      component: GiftPanel,
      meta: { title: '满赠', pluginCode: 'order-gift' }
    }
  ],
  setup(context) { setPluginContext(context) },
  teardown() { setPluginContext(null) }
}
