import ActivityListView from './views/ActivityListView.vue'
import ActivityConfigView from './views/ActivityConfigView.vue'
import ActivityPrizeView from './views/ActivityPrizeView.vue'
import GiftLibraryView from './views/GiftLibraryView.vue'
import DrawRecordView from './views/DrawRecordView.vue'
import ParticipantView from './views/ParticipantView.vue'
import { setPluginContext } from './context.js'

/** 二级页面：路径与 plugin.yaml / 服务端菜单树逐一对应 */
const sections = [
  { path: '/plugin/userms-lucky-draw/index', title: '抽奖活动', icon: 'Trophy', component: ActivityListView },
  { path: '/plugin/userms-lucky-draw/config', title: '活动配置', icon: 'Setting', component: ActivityConfigView },
  { path: '/plugin/userms-lucky-draw/prizes', title: '奖项设置', icon: 'Medal', component: ActivityPrizeView },
  { path: '/plugin/userms-lucky-draw/gifts', title: '礼品库', icon: 'Present', component: GiftLibraryView },
  { path: '/plugin/userms-lucky-draw/records', title: '抽奖记录', icon: 'Tickets', component: DrawRecordView },
  { path: '/plugin/userms-lucky-draw/participants', title: '参与用户', icon: 'User', component: ParticipantView }
]

export default {
  menus: [{ path: '/plugin/userms-lucky-draw', title: '抽奖转盘', icon: 'Trophy', sort: 3290 }],
  routes: [
    {
      path: '/plugin/userms-lucky-draw',
      redirect: '/plugin/userms-lucky-draw/index',
      meta: { title: '抽奖转盘', pluginCode: 'userms-lucky-draw' }
    },
    ...sections.map((section) => ({
      path: section.path,
      component: section.component,
      meta: { title: section.title, pluginCode: 'userms-lucky-draw' }
    }))
  ],
  setup(context) {
    setPluginContext(context)
  },
  teardown() {
    setPluginContext(null)
  }
}
