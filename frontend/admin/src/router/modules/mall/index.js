// 商城管理路由模块
const mallRoutes = {
  path: '/mall',
  meta: { title: '商城管理', icon: 'Shop' },
  children: [
    // 商城概览已并入核心仪表盘 /dashboard，旧路径一律重定向
    { path: '', redirect: '/dashboard' },
    { path: '/mall/dashboard/overview', redirect: '/dashboard' },
    { path: '/mall/dashboard/quick', component: () => import('@/views/mall/dashboard/Quick.vue'), meta: { title: '快捷操作' } },
    { path: '/mall/dashboard/monitor', component: () => import('@/views/mall/dashboard/Monitor.vue'), meta: { title: '实时监控' } },
    // 旧「工作台」菜单路径兼容
    { path: '/mall/admin/workbench', redirect: '/dashboard' },

    // 插件中心
    { path: '/mall/addons', redirect: '/platform/plugins', meta: { title: '插件中心', hidden: true } },
    // 插件市场（市场侧：浏览已上架插件 + 按版本在线安装）
    { path: '/mall/market', redirect: '/platform/plugins/market', meta: { title: '插件市场', hidden: true } },
    { path: '/mall/market/detail', redirect: '/platform/plugins/market/detail', meta: { title: '插件详情', hidden: true } },
    { path: '/mall/market/licenses', redirect: '/platform/plugins/licenses', meta: { title: '授权管理', hidden: true } },
    { path: '/mall/market/checkout', redirect: '/platform/plugins/market/checkout', meta: { title: '购买插件', hidden: true } },
    { path: '/mall/market/orders', redirect: '/platform/plugins/orders', meta: { title: '我的订单', hidden: true } },
    
    // 商品管理
    // 一级目录与误拼的 /dashboard 都落到商品列表，避免「页面不存在」
    { path: '/mall/goods', redirect: '/mall/goods/list' },
    { path: '/mall/goods/dashboard', redirect: '/mall/goods/list' },
    { path: '/mall/goods/list', component: () => import('@/views/mall/goods/GoodsList.vue'), meta: { title: '商品列表' } },
    // 兼容旧商城管理端入口
    { path: '/mall/admin/goods/list', redirect: '/mall/goods/list' },
    { path: '/mall/goods/detail', component: () => import('@/views/mall/goods/GoodsDetailView.vue'), meta: { title: '商品详情', hidden: true } },
    { path: '/mall/goods/category', component: () => import('@/views/mall/goods/GoodsCategory.vue'), meta: { title: '商品分类' } },
    { path: '/mall/goods/comment', component: () => import('@/views/mall/goods/GoodsComment.vue'), meta: { title: '商品评论' } },
    { path: '/mall/goods/attribute', component: () => import('@/views/mall/attr/AttributeManagement.vue'), meta: { title: '商品属性' } },
    { path: '/mall/goods/attributegroup', component: () => import('@/views/mall/attr/AttributeGroupManagement.vue'), meta: { title: '属性组管理' } },
    { path: '/mall/goods/attributetype', component: () => import('@/views/mall/attr/AttributeTypeManagement.vue'), meta: { title: '属性类型管理' } },
    { path: '/mall/goods/brand', component: () => import('@/views/mall/brand/BrandManagement.vue'), meta: { title: '品牌管理' } },
    { path: '/mall/goods/brandtype', component: () => import('@/views/mall/brand/BrandTypeManagement.vue'), meta: { title: '品牌类型管理' } },
    { path: '/mall/goods/review', component: () => import('@/views/mall/goods/GoodsReview.vue'), meta: { title: '商品评价' } },
    { path: '/mall/goods/import', component: () => import('@/views/mall/goods/GoodsImport.vue'), meta: { title: '商品导入' } },
    { path: '/mall/goods/audit', component: () => import('@/views/mall/goods/GoodsAudit.vue'), meta: { title: '商品审核' } },
    { path: '/mall/goods/statistics', component: () => import('@/views/mall/goods/GoodsStatistics.vue'), meta: { title: '商品统计' } },
    { path: '/mall/goods/domesticsubstitution', component: () => import('@/views/mall/goods/DomesticReplace.vue'), meta: { title: '国产替换' } },
    
    // 订单管理
    { path: '/mall/order', redirect: '/mall/order/list' },
    { path: '/mall/order/dashboard', redirect: '/mall/order/list' },
    { path: '/mall/order/list', component: () => import('@/views/mall/order/OrderList.vue'), meta: { title: '订单列表' } },
    { path: '/mall/order/detail', component: () => import('@/views/mall/order/OrderDetail.vue'), meta: { title: '订单详情' } },
    { path: '/mall/order/refund', component: () => import('@/views/mall/order/RefundManagement.vue'), meta: { title: '退款管理' } },
    { path: '/mall/order/logistics', component: () => import('@/views/mall/order/LogisticsManagement.vue'), meta: { title: '物流管理' } },
    { path: '/mall/order/statistics', component: () => import('@/views/mall/order/OrderStatistics.vue'), meta: { title: '订单统计' } },
    { path: '/mall/order/exception', component: () => import('@/views/mall/order/OrderException.vue'), meta: { title: '异常订单' } },
    
    // 订单管理 - 订单
    { path: '/mall/order/all', component: () => import('@/views/mall/order/AllOrders.vue'), meta: { title: '全部订单' } },
    { path: '/mall/order/pre-occupy', component: () => import('@/views/mall/order/PreOccupyOrder.vue'), meta: { title: '预占订单' } },
    { path: '/mall/order/preset', component: () => import('@/views/mall/order/PresetOrder.vue'), meta: { title: '预置单' } },
    { path: '/mall/order/demand', component: () => import('@/views/mall/order/DemandPublish.vue'), meta: { title: '反向需求发布' } },
    { path: '/mall/order/inquiry', component: () => import('@/views/mall/order/InquiryOrder.vue'), meta: { title: '询价订单' } },
    { path: '/mall/order/quotation', component: () => import('@/views/mall/order/QuotationOrder.vue'), meta: { title: '发票订单' } },
    { path: '/mall/order/evaluation', component: () => import('@/views/mall/order/EvaluationManagement.vue'), meta: { title: '评价管理' } },
    { path: '/mall/order/recommend', component: () => import('@/views/mall/order/RecommendDatabase.vue'), meta: { title: '推荐品入库' } },
    
    // 订单管理 - 维权处理
    { path: '/mall/order/rights', component: () => import('@/views/mall/order/TransactionRights.vue'), meta: { title: '交易维权' } },
    { path: '/mall/order/complaint', component: () => import('@/views/mall/order/ComplaintManagement.vue'), meta: { title: '投诉管理' } },
    { path: '/mall/order/platform-rules', component: () => import('@/views/mall/order/PlatformRules.vue'), meta: { title: '平台管理规范' } },
    
    // 订单管理 - 订单售后
    { path: '/mall/order/refund-manage', component: () => import('@/views/mall/order/RefundManage.vue'), meta: { title: '退款管理' } },
    { path: '/mall/order/return-manage', component: () => import('@/views/mall/order/ReturnManage.vue'), meta: { title: '退货管理' } },
    { path: '/mall/order/exchange-manage', component: () => import('@/views/mall/order/ExchangeManage.vue'), meta: { title: '换货管理' } },
    { path: '/mall/order/offline-deposit', component: () => import('@/views/mall/order/OfflineDeposit.vue'), meta: { title: '线下订款管理' } },
    
    // 订单管理 - 订单发货
    { path: '/mall/order/remind-shipping', component: () => import('@/views/mall/order/RemindShipping.vue'), meta: { title: '提醒发货' } },
    
    // 客户管理
    { path: '/mall/customer', redirect: '/mall/customer/list' },
    { path: '/mall/customer/dashboard', redirect: '/mall/customer/list' },
    { path: '/mall/customer/list', component: () => import('@/views/mall/customer/CustomerList.vue'), meta: { title: '客户列表' } },
    { path: '/mall/customer/group', component: () => import('@/views/mall/customer/CustomerGroup.vue'), meta: { title: '客户分组' } },
    { path: '/mall/customer/tag', component: () => import('@/views/mall/customer/CustomerTag.vue'), meta: { title: '客户标签' } },
    { path: '/mall/customer/follow', component: () => import('@/views/mall/customer/CustomerFollow.vue'), meta: { title: '客户跟进' } },
    { path: '/mall/customer/statistics', component: () => import('@/views/mall/customer/CustomerStatistics.vue'), meta: { title: '客户统计' } },
    { path: '/mall/customer/import', component: () => import('@/views/mall/customer/CustomerImport.vue'), meta: { title: '客户导入' } },
    
    // 库存管理
    { path: '/mall/inventory', redirect: '/mall/inventory/list' },
    { path: '/mall/inventory/dashboard', redirect: '/mall/inventory/list' },
    { path: '/mall/inventory/list', component: () => import('@/views/mall/stock/StockManagement.vue'), meta: { title: '库存列表' } },
    { path: '/mall/inventory/inbound', component: () => import('@/views/mall/inventory/InboundManagement.vue'), meta: { title: '入库管理' } },
    { path: '/mall/inventory/outbound', component: () => import('@/views/mall/inventory/OutboundManagement.vue'), meta: { title: '出库管理' } },
    { path: '/mall/inventory/count', component: () => import('@/views/mall/inventory/InventoryCount.vue'), meta: { title: '库存盘点' } },
    { path: '/mall/inventory/alert', component: () => import('@/views/mall/inventory/InventoryAlert.vue'), meta: { title: '库存预警' } },
    { path: '/mall/inventory/statistics', component: () => import('@/views/mall/inventory/InventoryStatistics.vue'), meta: { title: '库存统计' } },
    
    // 数据统计
    { path: '/mall/statistics', redirect: '/mall/statistics/sales' },
    { path: '/mall/statistics/dashboard', redirect: '/mall/statistics/sales' },
    { path: '/mall/statistics/sales', component: () => import('@/views/mall/statistics/SalesStatistics.vue'), meta: { title: '销售统计' } },
    { path: '/mall/statistics/goods', component: () => import('@/views/mall/statistics/GoodsStatistics.vue'), meta: { title: '商品统计' } },
    { path: '/mall/statistics/customer', component: () => import('@/views/mall/statistics/CustomerStatistics.vue'), meta: { title: '客户统计' } },
    { path: '/mall/statistics/inventory', component: () => import('@/views/mall/statistics/InventoryStatistics.vue'), meta: { title: '库存统计' } },
    { path: '/mall/statistics/finance', component: () => import('@/views/mall/statistics/FinanceStatistics.vue'), meta: { title: '财务统计' } },
    { path: '/mall/statistics/report', component: () => import('@/views/mall/statistics/StatisticsReport.vue'), meta: { title: '统计报表' } },
    
    // 店铺与实体门店分开管理：店铺是线上经营主体，门店承载配送与 POS 能力
    { path: '/mall/store', redirect: '/mall/store/list' },
    { path: '/mall/store/dashboard', redirect: '/mall/store/list' },
    { path: '/mall/store/list', component: () => import('@/views/mall/store/StoreList.vue'), meta: { title: '店铺列表' } },
    { path: '/mall/store/edit', component: () => import('@/views/mall/store/StoreEdit.vue'), meta: { title: '编辑店铺', hidden: true, permissionPath: '/mall/store/list' } },
    // 兼容旧的商家列表地址，统一进入店铺列表
    { path: '/mall/merchant/list', redirect: '/mall/store/list', meta: { title: '店铺列表', hidden: true } },
    { path: '/mall/physical-store/list', component: () => import('@/views/mall/store/PhysicalStoreList.vue'), meta: { title: '实体门店列表', skipMenuCheck: true } },
    { path: '/mall/store/setting', component: () => import('@/views/mall/store/StoreSetting.vue'), meta: { title: '店铺设置' } },
    { path: '/mall/store/decoration', component: () => import('@/views/mall/store/StoreDecoration.vue'), meta: { title: '店铺装修' } },
    { path: '/mall/store/audit', component: () => import('@/views/mall/store/StoreAudit.vue'), meta: { title: '店铺审核' } },
    { path: '/mall/store/statistics', component: () => import('@/views/mall/store/StoreStatistics.vue'), meta: { title: '店铺统计' } },
    
    // 店铺管理 - 二级菜单
    { path: '/mall/store/management', component: () => import('@/views/mall/store/StoreManagement.vue'), meta: { title: '商家权限' } },
    { path: '/mall/store/merchant-entry', component: () => import('@/views/mall/store/MerchantEntry.vue'), meta: { title: '入驻申请' } },
    { path: '/mall/store/inquiry', component: () => import('@/views/mall/store/InquiryFunction.vue'), meta: { title: '经营类目' } },
    { path: '/mall/store/level', component: () => import('@/views/mall/store/StoreLevel.vue'), meta: { title: '店铺等级' } },
    { path: '/mall/store/cash-on-delivery', component: () => import('@/views/mall/store/CashOnDelivery.vue'), meta: { title: '货到付款' } },
    { path: '/mall/store/invoice', component: () => import('@/views/mall/store/InvoiceManagement.vue'), meta: { title: '发票管理' } },
    { path: '/mall/store/quality-deposit', component: () => import('@/views/mall/store/QualityDeposit.vue'), meta: { title: '质保金' } },
    
    // 物流管理
    { path: '/mall/logistics', redirect: '/mall/logistics/company' },
    { path: '/mall/logistics/dashboard', redirect: '/mall/logistics/company' },
    { path: '/mall/logistics/company', component: () => import('@/views/mall/logistics/LogisticsCompany.vue'), meta: { title: '物流公司' } },
    { path: '/mall/logistics/track', component: () => import('@/views/mall/logistics/LogisticsTrack.vue'), meta: { title: '物流跟踪' } },
    { path: '/mall/logistics/setting', component: () => import('@/views/mall/logistics/LogisticsSetting.vue'), meta: { title: '物流设置' } },
    { path: '/mall/logistics/statistics', component: () => import('@/views/mall/logistics/LogisticsStatistics.vue'), meta: { title: '物流统计' } },
    
    // 售后服务
    { path: '/mall/service', redirect: '/mall/service/ticket' },
    { path: '/mall/service/dashboard', redirect: '/mall/service/ticket' },
    { path: '/mall/service/ticket', component: () => import('@/views/mall/service/ServiceTicket.vue'), meta: { title: '服务工单' } },
    { path: '/mall/service/record', component: () => import('@/views/mall/service/ServiceRecord.vue'), meta: { title: '服务记录' } },
    { path: '/mall/service/review', component: () => import('@/views/mall/service/ServiceReview.vue'), meta: { title: '服务评价' } },
    { path: '/mall/service/statistics', component: () => import('@/views/mall/service/ServiceStatistics.vue'), meta: { title: '服务统计' } },
    { path: '/mall/service/setting', component: () => import('@/views/mall/service/ServiceSetting.vue'), meta: { title: '服务设置' } },
    
    // 系统设置
    { path: '/mall/setting/basic', component: () => import('@/views/mall/setting/BasicSetting.vue'), meta: { title: '基础设置' } },
    { path: '/mall/setting/payment', component: () => import('@/views/mall/setting/PaymentSetting.vue'), meta: { title: '支付设置' } },
    { path: '/mall/setting/logistics', component: () => import('@/views/mall/setting/LogisticsSetting.vue'), meta: { title: '物流设置' } },
    { path: '/mall/setting/notification', component: () => import('@/views/mall/setting/NotificationSetting.vue'), meta: { title: '通知设置' } },
    { path: '/mall/setting/security', component: () => import('@/views/mall/setting/SecuritySetting.vue'), meta: { title: '安全设置' } },
    
    // 装修管理（对齐老系统 decoration 菜单树）
    { path: '/mall/decoration', redirect: '/mall/decoration/pages/pages' },
    { path: '/mall/decoration/dashboard', redirect: '/mall/decoration/pages/pages' },
    { path: '/mall/decoration/pc_style', component: () => import('@/views/mall/decoration/pages/ThemeStyle.vue'), meta: { title: 'PC装修', platform: 'pc' } },
    { path: '/mall/decoration/mobile_style', component: () => import('@/views/mall/decoration/pages/ThemeStyle.vue'), meta: { title: '移动装修', platform: 'mobile' } },
    { path: '/mall/decoration/mall_style', redirect: '/mall/decoration/pc_style' },
    { path: '/mall/decoration/pages/pages', component: () => import('@/views/mall/decoration/pages/PageList.vue'), meta: { title: '首页装修' } },
    { path: '/mall/decoration/pages/template', component: () => import('@/views/mall/decoration/pages/TemplateSelect.vue'), meta: { title: '模板选择' } },
    { path: '/mall/decoration/pages/index', component: () => import('@/views/mall/decoration/pages/PageEditor.vue'), meta: { title: '页面编辑', hidden: true } },
    { path: '/mall/decoration/pages/goods_detiail', component: () => import('@/views/mall/decoration/pages/ProductDetail.vue'), meta: { title: '商品详情' } },
    { path: '/mall/decoration/pages/user_center', component: () => import('@/views/mall/decoration/pages/UserCenter.vue'), meta: { title: '个人中心' } },
    { path: '/mall/decoration/pages/data_allocation', component: () => import('@/views/mall/decoration/pages/DataConfig.vue'), meta: { title: '数据配置' } },
    { path: '/mall/decoration/tabbar', component: () => import('@/views/mall/decoration/pages/TabBar.vue'), meta: { title: '底部导航' } },
    // PC 端页面装修（与移动端同一批组件，靠 meta.platform 区分端）
    { path: '/mall/decoration/pc/pages', component: () => import('@/views/mall/decoration/pages/PageList.vue'), meta: { title: '首页装修', platform: 'pc' } },
    { path: '/mall/decoration/pc/template', component: () => import('@/views/mall/decoration/pages/TemplateSelect.vue'), meta: { title: '模板选择', platform: 'pc' } },
    { path: '/mall/decoration/pc/goods_detiail', component: () => import('@/views/mall/decoration/pages/ProductDetail.vue'), meta: { title: '商品详情', platform: 'pc' } },
    { path: '/mall/decoration/pc/user_center', component: () => import('@/views/mall/decoration/pages/UserCenter.vue'), meta: { title: '个人中心', platform: 'pc' } },
    { path: '/mall/decoration/pc/data_allocation', component: () => import('@/views/mall/decoration/pages/DataConfig.vue'), meta: { title: '数据配置', platform: 'pc' } },
    { path: '/mall/decoration/pc/index', component: () => import('@/views/mall/decoration/pages/PageEditor.vue'), meta: { title: '页面编辑', platform: 'pc', hidden: true } },
    // 兼容旧路径
    { path: '/mall/decoration/mobile', redirect: '/mall/decoration/pages/pages' },
    { path: '/mall/decoration/mobile/editor/:id', redirect: to => ({ path: '/mall/decoration/pages/index', query: { id: to.params.id } }) },
    { path: '/mall/decoration/theme', redirect: '/mall/decoration/pc_style' },
    { path: '/mall/admin/decoration/pages', redirect: '/mall/decoration/pages/pages' },
    { path: '/mall/admin/decoration/tabbar', redirect: '/mall/decoration/tabbar' },
    { path: '/mall/admin/decoration/theme', redirect: '/mall/decoration/pc_style' },
    { path: '/mall/decoration/pc', component: () => import('@/views/mall/decoration/PcDecoration.vue'), meta: { title: 'PC装修' } },
    { path: '/mall/decoration/special', component: () => import('@/views/mall/decoration/SpecialDecoration.vue'), meta: { title: '特色馆装修' } },

    // 商城结算（核心）：只承载商城交易资金、退款、店铺结算和用户资金。
    { path: '/mall/admin/finance', redirect: '/mall/finance/center', meta: { title: '商城结算', hidden: true } },
    { path: '/mall/finance', redirect: '/mall/finance/center', meta: { title: '商城结算', hidden: true } },
    { path: '/mall/finance/shop_details', redirect: '/mall/finance/shop_details/shop_settle', meta: { title: '店铺资金', hidden: true } },
    { path: '/mall/finance/user_details', redirect: '/mall/finance/user_details/withdraw', meta: { title: '用户资金', hidden: true } },

    // 旧 /mall/admin/finance 子路径只保留兼容地址，实际页面归一到核心商城结算路由。
    { path: '/mall/admin/finance/center', redirect: '/mall/finance/center', meta: { hidden: true } },
    { path: '/mall/admin/finance/refund', redirect: '/mall/finance/refund', meta: { hidden: true } },
    { path: '/mall/admin/finance/mall', redirect: '/mall/finance/mall', meta: { hidden: true } },
    { path: '/mall/admin/finance/shop_details', redirect: '/mall/finance/shop_details', meta: { hidden: true } },
    { path: '/mall/admin/finance/shop_details/shop_settle', redirect: '/mall/finance/shop_details/shop_settle', meta: { hidden: true } },
    { path: '/mall/admin/finance/shop_details/balance', redirect: '/mall/finance/shop_details/balance', meta: { hidden: true } },
    { path: '/mall/admin/finance/shop_details/settlement_record', redirect: '/mall/finance/shop_details/settlement_record', meta: { hidden: true } },
    { path: '/mall/admin/finance/shop_details/withdraw', redirect: '/mall/finance/shop_details/withdraw', meta: { hidden: true } },
    { path: '/mall/admin/finance/user_details', redirect: '/mall/finance/user_details', meta: { hidden: true } },
    { path: '/mall/admin/finance/user_details/withdraw', redirect: '/mall/finance/user_details/withdraw', meta: { hidden: true } },
    { path: '/mall/admin/finance/user_details/balance', redirect: '/mall/finance/user_details/balance', meta: { hidden: true } },
    { path: '/mall/admin/finance/user_details/commission', redirect: '/mall/finance/user_details/commission', meta: { hidden: true } },
    { path: '/mall/admin/finance/user_details/deposit', redirect: '/mall/finance/user_details/deposit', meta: { hidden: true } },
    { path: '/mall/admin/finance/setting', redirect: '/mall/finance/setting', meta: { hidden: true } },
    { path: '/mall/finance/overview', redirect: '/plugin/finance/dashboard', meta: { hidden: true } },
    { path: '/mall/finance/income', redirect: '/plugin/finance/income', meta: { hidden: true } },
    { path: '/mall/finance/expense', redirect: '/plugin/finance/spending', meta: { hidden: true } },
    { path: '/mall/finance/report', redirect: '/plugin/finance/report', meta: { hidden: true } },
    { path: '/mall/admin/finance/overview', redirect: '/plugin/finance/dashboard', meta: { hidden: true } },
    { path: '/mall/admin/finance/income', redirect: '/plugin/finance/income', meta: { hidden: true } },
    { path: '/mall/admin/finance/expense', redirect: '/plugin/finance/spending', meta: { hidden: true } },
    { path: '/mall/admin/finance/report', redirect: '/plugin/finance/report', meta: { hidden: true } }
  ]
}

// Plugin business pages are loaded from the installed plugin manifest. Keep
// the historical paths as redirects so bookmarks continue to resolve without
// registering the old host components in the active route table.
const legacyPluginPrefixes = ['/mall/tools', '/mall/live', '/mall/content']

// 营销活动总览：营销能力已按业务拆到 order 域插件，这里保留一个非插件入口
// 做跨插件只读聚合（数量 / 进行中 / 活动清单 + 跳转到各插件管理页）。
const marketingOverviewRoute = {
  path: '/mall/marketing',
  meta: { title: '营销活动' }
}

const legacyPluginRedirects = [
  { path: '/mall/marketing/:pathMatch(.+)', redirect: '/mall/marketing' },
  { path: '/mall/tools/:pathMatch(.*)*', redirect: '/platform/plugins' },
  { path: '/mall/live/:pathMatch(.*)*', redirect: '/plugin/mall-live' },
  { path: '/mall/content/:pathMatch(.*)*', redirect: '/plugin/cms' }
]

mallRoutes.children = mallRoutes.children
  .filter((route) => !legacyPluginPrefixes.some((prefix) => {
    const path = String(route.path || '')
    return path === prefix || path.startsWith(`${prefix}/`)
  }))
  .concat([marketingOverviewRoute])
  .concat(legacyPluginRedirects)

export default mallRoutes
