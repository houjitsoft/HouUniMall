import SeckillPanel from './views/SeckillPanel.vue'
import ThousandsPricePanel from './views/ThousandsPricePanel.vue'
import { setPluginContext } from './context.js'

export default {
  menus: [
    { path: '/plugin/order-seckill', title: '限时秒杀', icon: 'Timer', sort: 3390 },
    { path: '/plugin/order-seckill-price', title: '千客千价', icon: 'Discount', sort: 3380 }
  ],
  routes: [
    {
      path: '/plugin/order-seckill',
      component: SeckillPanel,
      meta: { title: '限时秒杀', pluginCode: 'order-seckill' }
    },
    {
      path: '/plugin/order-seckill-price',
      component: ThousandsPricePanel,
      meta: { title: '千客千价', pluginCode: 'order-seckill' }
    }
  ],
  setup(context) { setPluginContext(context) },
  teardown() { setPluginContext(null) }
}
