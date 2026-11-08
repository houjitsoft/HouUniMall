import VoucherPanel from './views/VoucherPanel.vue'
import { setPluginContext } from './context.js'

export default {
  menus: [
    { path: '/plugin/order-voucher', title: '优惠券', icon: 'Ticket', sort: 3370 }
  ],
  routes: [
    {
      path: '/plugin/order-voucher',
      component: VoucherPanel,
      meta: { title: '优惠券', pluginCode: 'order-voucher' }
    }
  ],
  setup(context) { setPluginContext(context) },
  teardown() { setPluginContext(null) }
}
