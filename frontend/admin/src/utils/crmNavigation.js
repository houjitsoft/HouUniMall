/** 历史重复入口统一到负责该业务的页面；保留旧书签兼容。 */
export const crmNavigationAliases = {
 '/crm/clue/list':'/crm/clue', '/crm/clue/import':'/crm/clue',
 '/crm/follow/list':'/crm/follow',
 '/crm/sales/order':'/crm/sales/orders', '/crm/sales/follow':'/crm/follow',
 '/crm/opportunity/follow':'/crm/follow', '/crm/opportunity/assign':'/crm/opportunity/list',
 '/crm/opportunity/stage':'/crm/opportunity/list', '/crm/opportunity/convert':'/crm/opportunity/list',
 '/crm/opportunity/detail':'/crm/opportunity/list',
 '/crm/product/list':'/crm/product', '/crm/product/price':'/crm/product', '/crm/product/pricing':'/crm/product',
 '/crm/product/spec':'/crm/product', '/crm/product/stock':'/crm/product', '/crm/product/config':'/crm/product',
 '/crm/service/ticket':'/crm/service/tickets', '/crm/marketing/activity':'/crm/marketing/campaigns',
 '/crm/marketing/lead':'/crm/clue', '/crm/marketing/effect':'/crm/marketing/statistics',
 '/crm/setting/cleanup':'/crm/customer/recycle'
}
export const crmRecordRoutes = {
 'sales/target':'sales-target', 'sales/plan':'sales-plan', 'sales/activity':'sales-activity',
 'service/feedback':'service-feedback', 'service/knowledge':'service-knowledge'
}
