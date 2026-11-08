import PintuanPanel from './views/PintuanPanel.vue'
import PintuanSettings from './views/PintuanSettings.vue'
import PintuanReports from './views/PintuanReports.vue'
import { setPluginContext } from './context.js'

export default {
  menus: [
    { path: '/plugin/order-pintuan', title: '拼团', icon: 'UserFilled', sort: 3380 }
  ],
  routes: [
    {
      path: '/plugin/order-pintuan',
      component: PintuanPanel,
      meta: { title: '拼团管理', pluginCode: 'order-pintuan' }
    },
    {
      path: '/plugin/order-pintuan/index',
      component: PintuanPanel,
      meta: { title: '拼团管理', pluginCode: 'order-pintuan' }
    },
    {
      path: '/plugin/order-pintuan/settings',
      component: PintuanSettings,
      meta: { title: '拼团设置', pluginCode: 'order-pintuan' }
    },
    {
      path: '/plugin/order-pintuan/reports',
      component: PintuanReports,
      meta: { title: '拼团报表', pluginCode: 'order-pintuan' }
    }
  ],
  setup(context) { setPluginContext(context) },
  teardown() { setPluginContext(null) }
}
