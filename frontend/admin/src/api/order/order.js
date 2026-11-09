/**
 * 订单管理API接口
 * 使用主服务的统一API服务层
 */

// 导入主服务的统一API服务
import request from '@/utils/request'

/**
 * 订单管理API接口
 */
export const orderService = {
  // 查询订单列表
  getOrderList: async (params) => {
    try {
      const response = await request.post('/web/order/list', params)
      return response
    } catch (error) {
      console.error('查询订单列表失败:', error)
      throw error
    }
  },

  // 获取订单详情（走 mall 管理端代理，order 侧 backGroundByOrderDetails + 契约适配）
  getOrderDetail: async (orderId) => {
    try {
      const response = await request.post('/web/mall/order/manage/detail', { orderId })
      return response
    } catch (error) {
      console.error('查询订单详情失败:', error)
      throw error
    }
  },

  // 创建订单
  createOrder: async (orderData) => {
    try {
      const response = await request.post('/web/order/create', orderData)
      return response
    } catch (error) {
      console.error('创建订单失败:', error)
      throw error
    }
  },

  // 更新订单信息
  updateOrder: async (orderData) => {
    try {
      const response = await request.post('/web/order/update', orderData)
      return response
    } catch (error) {
      console.error('更新订单信息失败:', error)
      throw error
    }
  },

  // 取消订单
  cancelOrder: async (params) => {
    try {
      const response = await request.post('/web/order/cancel', params)
      return response
    } catch (error) {
      console.error('取消订单失败:', error)
      throw error
    }
  },

  // 删除订单
  deleteOrder: async (params) => {
    try {
      const response = await request.post('/web/order/delete', params)
      return response
    } catch (error) {
      console.error('删除订单失败:', error)
      throw error
    }
  },

  // 确认收货
  confirmOrder: async (params) => {
    try {
      const response = await request.post('/web/order/confirm', params)
      return response
    } catch (error) {
      console.error('确认收货失败:', error)
      throw error
    }
  },

  // 订单发货
  shipOrder: async (params) => {
    try {
      const response = await request.post('/web/order/ship', params)
      return response
    } catch (error) {
      console.error('订单发货失败:', error)
      throw error
    }
  },

  // 更新订单状态
  updateOrderStatus: async (params) => {
    try {
      const response = await request.post('/web/order/updateStatus', params)
      return response
    } catch (error) {
      console.error('更新订单状态失败:', error)
      throw error
    }
  },

  // 订单支付
  payOrder: async (params) => {
    try {
      const response = await request.post('/web/order/pay', params)
      return response
    } catch (error) {
      console.error('订单支付失败:', error)
      throw error
    }
  },

  // 延迟收货
  delayToReceiveGoods: async (params) => {
    try {
      const response = await request.post('/web/order/delay', params)
      return response
    } catch (error) {
      console.error('延迟收货失败:', error)
      throw error
    }
  },

  // 订单统计数据
  // @param {Object} params 查询参数
  // @param {Object} config 可选，透传 axios 配置（如 skipAuthRedirect/skipErrorMessage）
  // 统计类聚合接口偶发返回鉴权类错误码，不能因此清 token 跳登录（标品测试：订单统计跳转登录页）
  // 调用方仍可通过 config.skipAuthRedirect = false 显式恢复默认行为。
  getOrderStatistics: async (params, config = {}) => {
    try {
      const response = await request.post('/web/order/statistics', params, { skipAuthRedirect: true, ...config })
      return response
    } catch (error) {
      if (!config.skipErrorMessage) {
        console.error('订单统计数据失败:', error)
      }
      throw error
    }
  },

  // 导出订单
  exportOrders: async (params) => {
    try {
      const response = await request.post('/web/order/export', params, {
        responseType: 'blob'
      })
      return response
    } catch (error) {
      console.error('导出订单失败:', error)
      throw error
    }
  },

  // 查询购物车列表
  getCartList: async (params) => {
    try {
      const response = await request.post('/web/order/cart/list', params)
      return response
    } catch (error) {
      console.error('查询购物车列表失败:', error)
      throw error
    }
  },

  // 添加到购物车
  addToCart: async (cartData) => {
    try {
      const response = await request.post('/web/order/cart/add', cartData)
      return response
    } catch (error) {
      console.error('添加到购物车失败:', error)
      throw error
    }
  },

  // 更新购物车
  updateCart: async (cartData) => {
    try {
      const response = await request.post('/web/order/cart/update', cartData)
      return response
    } catch (error) {
      console.error('更新购物车失败:', error)
      throw error
    }
  },

  // 删除购物车项
  deleteCartItem: async (params) => {
    try {
      const response = await request.post('/web/order/cart/delete', params)
      return response
    } catch (error) {
      console.error('删除购物车项失败:', error)
      throw error
    }
  },

  // 清空购物车
  clearCart: async () => {
    try {
      const response = await request.post('/web/order/cart/clear')
      return response
    } catch (error) {
      console.error('清空购物车失败:', error)
      throw error
    }
  },

  // 提交订单评价
  submitOrderReview: async (reviewData) => {
    try {
      const response = await request.post('/web/order/review/submit', reviewData)
      return response
    } catch (error) {
      console.error('提交订单评价失败:', error)
      throw error
    }
  },

  // 获取订单评价
  getOrderReview: async (orderId) => {
    try {
      const response = await request.post('/web/order/review/list', { orderId })
      return response
    } catch (error) {
      console.error('获取订单评价失败:', error)
      throw error
    }
  },

  // 申请售后
  applyAfterSale: async (afterSaleData) => {
    try {
      const response = await request.post('/web/order/aftersale/apply', afterSaleData)
      return response
    } catch (error) {
      console.error('申请售后失败:', error)
      throw error
    }
  },

  // 获取售后列表
  getAfterSaleList: async (params) => {
    try {
      const response = await request.post('/web/order/aftersale/list', params)
      return response
    } catch (error) {
      console.error('获取售后列表失败:', error)
      throw error
    }
  },

  // 获取售后详情
  getAfterSaleDetail: async (afterSaleId) => {
    try {
      const response = await request.post('/web/order/aftersale/detail', { orderReturnId: afterSaleId })
      return response
    } catch (error) {
      console.error('获取售后详情失败:', error)
      throw error
    }
  },

  // 更新售后状态
  updateAfterSaleStatus: async (params) => {
    try {
      const response = await request.post('/web/order/aftersale/update', params)
      return response
    } catch (error) {
      console.error('更新售后状态失败:', error)
      throw error
    }
  },

  // ================= v4 新增接口 =================

  // 延迟收货
  delayReceive: async (params) => {
    try {
      const response = await request.post('/web/order/delay', params)
      return response
    } catch (error) {
      console.error('延迟收货失败:', error)
      throw error
    }
  },

  // 修改快递单号
  changeShipment: async (params) => {
    try {
      const response = await request.post('/web/order/changeShip', params)
      return response
    } catch (error) {
      console.error('修改快递单号失败:', error)
      throw error
    }
  },

  // 换货列表
  getExchangeList: async (params) => {
    try {
      const response = await request.post('/web/order/exchange/list', params)
      return response
    } catch (error) {
      console.error('查询换货列表失败:', error)
      throw error
    }
  },

  // 换货详情
  getExchangeDetail: async (params) => {
    try {
      const response = await request.post('/web/order/exchange/detail', params)
      return response
    } catch (error) {
      console.error('查询换货详情失败:', error)
      throw error
    }
  },

  // 换货审核
  approveExchange: async (params) => {
    try {
      const response = await request.post('/web/order/exchange/approve', params)
      return response
    } catch (error) {
      console.error('换货审核失败:', error)
      throw error
    }
  },

  // 投诉列表
  getComplaintList: async (params) => {
    try {
      const response = await request.post('/web/order/complaint/list', params)
      return response
    } catch (error) {
      console.error('查询投诉列表失败:', error)
      throw error
    }
  },

  // 投诉详情
  getComplaintDetail: async (params) => {
    try {
      const response = await request.post('/web/order/complaint/detail', params)
      return response
    } catch (error) {
      console.error('查询投诉详情失败:', error)
      throw error
    }
  },

  // 处理投诉
  handleComplaint: async (params) => {
    try {
      const response = await request.post('/web/order/complaint/handle', params)
      return response
    } catch (error) {
      console.error('处理投诉失败:', error)
      throw error
    }
  },

  // 平台介入列表
  getPlatformList: async (params) => {
    try {
      const response = await request.post('/web/order/platform/return/list', params)
      return response
    } catch (error) {
      console.error('查询平台介入列表失败:', error)
      throw error
    }
  },

  // 平台介入详情
  getPlatformDetail: async (params) => {
    try {
      const response = await request.post('/web/order/platform/return/detail', params)
      return response
    } catch (error) {
      console.error('查询平台介入详情失败:', error)
      throw error
    }
  },

  // 平台介入处理
  processPlatform: async (params) => {
    try {
      const response = await request.post('/web/order/platform/return/process', params)
      return response
    } catch (error) {
      console.error('平台介入处理失败:', error)
      throw error
    }
  },

  // 结算列表
  getSettlementList: async (params) => {
    try {
      const response = await request.post('/web/order/settlement/list', params)
      return response
    } catch (error) {
      console.error('查询结算列表失败:', error)
      throw error
    }
  },

  // 结算详情
  getSettlementDetail: async (params) => {
    try {
      const response = await request.post('/web/order/settlement/detail', params)
      return response
    } catch (error) {
      console.error('查询结算详情失败:', error)
      throw error
    }
  },

  // 手动结算
  processSettlement: async (params) => {
    try {
      const response = await request.post('/web/order/settlement/process', params)
      return response
    } catch (error) {
      console.error('手动结算失败:', error)
      throw error
    }
  },

  // 发票详情
  getInvoiceDetail: async (params) => {
    try {
      const response = await request.post('/web/order/invoice/detail', params)
      return response
    } catch (error) {
      console.error('查询发票详情失败:', error)
      throw error
    }
  },

  // 发票审核
  auditInvoice: async (params) => {
    try {
      const response = await request.post('/web/order/invoice/audit', params)
      return response
    } catch (error) {
      console.error('发票审核失败:', error)
      throw error
    }
  },

  // 发票列表
  getInvoiceList: async (params) => {
    try {
      const response = await request.post('/web/order/invoice/list', params)
      return response
    } catch (error) {
      console.error('查询发票列表失败:', error)
      throw error
    }
  },

  // 评价列表（管理端）
  getEvaluationList: async (params) => {
    try {
      const response = await request.post('/web/order/evaluation/list', params)
      return response
    } catch (error) {
      console.error('查询评价列表失败:', error)
      throw error
    }
  },

  // 删除评价
  deleteEvaluation: async (params) => {
    try {
      const response = await request.post('/web/order/evaluation/delete', params)
      return response
    } catch (error) {
      console.error('删除评价失败:', error)
      throw error
    }
  },

  // 回复评价
  replyEvaluation: async (params) => {
    try {
      const response = await request.post('/web/order/evaluation/reply', params)
      return response
    } catch (error) {
      console.error('回复评价失败:', error)
      throw error
    }
  },

  // ==================== 佣金记录接口 ====================

  // 查询佣金记录列表
  getCommissionList: async (params) => {
    try {
      const response = await request.post('/web/order/commission/list', params)
      return response
    } catch (error) {
      console.error('查询佣金记录失败:', error)
      throw error
    }
  },

  // 查询分销订单列表
  getCommissionOrders: async (params) => {
    try {
      const response = await request.post('/web/order/commission/orders', params)
      return response
    } catch (error) {
      console.error('查询分销订单失败:', error)
      throw error
    }
  }
}

// 为了保持向后兼容，单独导出函数
export const {
  getOrderList,
  getOrderDetail,
  createOrder,
  updateOrder,
  cancelOrder,
  deleteOrder,
  confirmOrder,
  shipOrder,
  updateOrderStatus,
  payOrder,
  delayToReceiveGoods,
  getOrderStatistics,
  exportOrders,
  getCartList,
  addToCart,
  updateCart,
  deleteCartItem,
  clearCart,
  submitOrderReview,
  getOrderReview,
  applyAfterSale,
  getAfterSaleList,
  getAfterSaleDetail,
  updateAfterSaleStatus,
  delayReceive,
  changeShipment,
  getExchangeList,
  getExchangeDetail,
  approveExchange,
  getComplaintList,
  getComplaintDetail,
  handleComplaint,
  getPlatformList,
  getPlatformDetail,
  processPlatform,
  getSettlementList,
  getSettlementDetail,
  processSettlement,
  getInvoiceDetail,
  auditInvoice,
  getInvoiceList,
  getEvaluationList,
  deleteEvaluation,
  replyEvaluation,
  getCommissionList,
  getCommissionOrders
} = orderService

// 为了保持向后兼容，导出orderApi别名
export const orderApi = orderService

/**
 * 订单状态枚举
 */
export const ORDER_STATUS = {
  PENDING: 1,           // 待付款
  WAIT_SHIP: 3,        // 待发货
  SHIPPED: 4,          // 已发货
  CONFIRMED: 6,        // 确认收货
  CANCELLED: 7,        // 已取消
  REFUNDING: 8,        // 退款中
  REFUNDED: 9,         // 已退款
  WAIT_PICKUP: 11      // 待自提
}

/**
 * 订单状态文本映射
 */
export const ORDER_STATUS_TEXT = {
  [ORDER_STATUS.PENDING]: '待付款',
  [ORDER_STATUS.WAIT_SHIP]: '待发货',
  [ORDER_STATUS.SHIPPED]: '已发货',
  [ORDER_STATUS.CONFIRMED]: '确认收货',
  [ORDER_STATUS.CANCELLED]: '已取消',
  [ORDER_STATUS.REFUNDING]: '退款中',
  [ORDER_STATUS.REFUNDED]: '已退款',
  [ORDER_STATUS.WAIT_PICKUP]: '待自提'
}

/**
 * 订单状态标签类型映射
 */
export const ORDER_STATUS_TAG_TYPE = {
  [ORDER_STATUS.PENDING]: 'warning',
  [ORDER_STATUS.WAIT_SHIP]: 'primary',
  [ORDER_STATUS.SHIPPED]: 'info',
  [ORDER_STATUS.CONFIRMED]: 'success',
  [ORDER_STATUS.CANCELLED]: 'danger',
  [ORDER_STATUS.REFUNDING]: 'warning',
  [ORDER_STATUS.REFUNDED]: 'info',
  [ORDER_STATUS.WAIT_PICKUP]: 'primary'
}

/**
 * 支付方式枚举
 */
export const PAYMENT_METHOD = {
  BALANCE: 1,           // 余额支付
  ALIPAY: 2,           // 支付宝
  WECHAT: 3,           // 微信支付
  CARD: 6,             // 购物卡
  OFFLINE: 12          // 线下支付
}

/**
 * 支付方式文本映射
 */
export const PAYMENT_METHOD_TEXT = {
  [PAYMENT_METHOD.BALANCE]: '余额支付',
  [PAYMENT_METHOD.ALIPAY]: '支付宝',
  [PAYMENT_METHOD.WECHAT]: '微信支付',
  [PAYMENT_METHOD.CARD]: '购物卡',
  [PAYMENT_METHOD.OFFLINE]: '线下支付'
}

// 导出服务实例
export default orderService