import { h } from 'vue'
import UniLoginEntry from './providers/UniLoginEntry.vue'
import UniLoginSettings from './pages/UniLoginSettings.vue'
import UniLoginCallback from './pages/UniLoginCallback.vue'
import { setPluginContext } from './context.js'
import { CALLBACK_ROUTE, CHANNELS, PLUGIN_CODE } from './channels.js'

/**
 * 统一登录插件（UniLogin）
 *
 * 插件本身不绑定任何厂商：可接入的登录渠道全部由 channels.js 的 CHANNELS 驱动，
 * 每新增一条渠道，登录页入口、授权回调路由、设置页 Tab 自动生成，无需改动组件。
 *
 * 宿主接入点：
 * 1) loginProviders —— 登录页「其他登录方式」区域的入口（宿主扩展点，未登录也可装载）；
 * 2) routes/menus   —— 登录后的凭据配置页与授权回调页。
 *
 * 服务端能力全部走宿主 auth 服务的 /web/auth/unilogin/{channel}/**（Core Port），
 * 插件不自建后端；渠道凭据由服务端按「设置页 → Nacos」的优先级解析。
 */

/** 把通用入口组件绑定成某个渠道的具体入口（宿主扩展点渲染时不传 props） */
const entryFor = (channel) => ({
  name: `UniLoginEntryFor_${channel.code}`,
  setup() {
    return () => h(UniLoginEntry, { channel })
  }
})

const manifest = {
  menus: [
    {
      path: `/plugin/${PLUGIN_CODE}/settings`,
      title: '统一登录',
      icon: 'Connection',
      sort: 9080
    }
  ],
  routes: [
    {
      path: '/settings',
      component: UniLoginSettings,
      meta: { title: '统一登录' }
    },
    {
      // 渠道由路径决定：/plugin/unilogin/callback/{channel}
      path: CALLBACK_ROUTE,
      component: UniLoginCallback,
      meta: { title: '第三方登录授权' }
    }
  ],
  loginProviders: CHANNELS.map((channel) => ({
    code: channel.code,
    title: channel.title,
    icon: channel.icon,
    entry: entryFor(channel)
  })),
  setup(context) {
    setPluginContext(context)
  },
  teardown() {
    setPluginContext(null)
  }
}

export default manifest
