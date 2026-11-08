/**
 * API统一导出文件
 * 只提供核心工具，具体API请从对应模块导入
 */

import request from '@/utils/request'
// 导出request实例
export default request

// 从各模块导出API实例
export { authApi } from './auth/auth.js'
export { goodsApi, categoryApi, brandApi, specApi, statisticsApi as goodsStatisticsApi, attributeApi } from './goods/goods.js'
export { workflowApi, formApi } from './workflow/workflow.js'
export { settingApi } from './setting/setting.js'
export { aiApi } from './ai/ai.js'
// 商业域（crm/orderfood/supplier）不随开源包分发，导出同步移除；
// 对应的 api/<域>/ 目录在公开树中不存在，保留导出会让 vite 直接构建失败。
export { orderApi } from './order/order.js'
export { payApi } from './payment/pay.js'
export { mallApi, pointsMallToolApi, liveActivityApi, liveProductAuditApi, liveAccountApi, topicCategoryApi, selectionListApi, moodManageApi, reviewManageApi } from './mall/mall.js'