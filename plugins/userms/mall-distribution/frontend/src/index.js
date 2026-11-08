import DistributorList from './views/DistributorList.vue'
import DistributorLevel from './views/DistributorLevel.vue'
import CommissionRule from './views/CommissionRule.vue'
import CommissionRecord from './views/CommissionRecord.vue'
import DistributionGoods from './views/DistributionGoods.vue'
import DistributionOrder from './views/DistributionOrder.vue'
import DistributionSetting from './views/DistributionSetting.vue'
import DistributionSettle from './views/DistributionSettle.vue'
import DistributionWithdraw from './views/DistributionWithdraw.vue'
import { setPluginContext } from './context.js'

/**
 * 分销推广插件入口。
 *
 * 后端是 mall 主服务的原生 Controller（/mall/distribution/**），经 BFF 商城模块直通
 * 通道 /web/mall/module/distribution/** 访问（见 utils/request.js）。本插件只承载
 * 前端页面与菜单声明，与 rider 同构。
 */
const routes = [
  ['/plugin/mall-distribution', DistributorList, '分销推广'],
  ['/plugin/mall-distribution/distributor', DistributorList, '分销商管理'],
  ['/plugin/mall-distribution/level', DistributorLevel, '分销等级'],
  ['/plugin/mall-distribution/goods', DistributionGoods, '分销商品'],
  ['/plugin/mall-distribution/order', DistributionOrder, '分销订单'],
  ['/plugin/mall-distribution/rule', CommissionRule, '佣金规则'],
  ['/plugin/mall-distribution/commission', CommissionRecord, '佣金记录'],
  ['/plugin/mall-distribution/setting', DistributionSetting, '分销设置'],
  ['/plugin/mall-distribution/settle', DistributionSettle, '佣金结算'],
  ['/plugin/mall-distribution/withdraw', DistributionWithdraw, '提现审核']
]

export default {
  menus: [{ path: '/plugin/mall-distribution', title: '分销推广', icon: 'Share', sort: 3050 }],
  routes: routes.map(([path, component, title]) => ({
    path, component, meta: { title, pluginCode: 'mall-distribution' }
  })),
  setup(context) { setPluginContext(context) },
  teardown() { setPluginContext(null) }
}
