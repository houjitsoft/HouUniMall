/**
 * 支付管理API接口
 * 使用主服务的统一API服务层
 */

// 导入主服务的统一API服务
import request from '@/utils/request'

/**
 * 支付管理API接口
 */
export const payService = {
  // ==================== 支付管理接口 ====================
  
  /**
   * 创建支付订单
   * @param {Object} paymentData 支付数据
   * @returns {Promise}
   */
  createPayment: async (paymentData) => {
    try {
      const response = await request.post('/web/pay/create', paymentData)
      return response
    } catch (error) {
      console.error('创建支付订单失败:', error)
      throw error
    }
  },

  /**
   * 查询支付订单详情
   * @param {Number} id 支付订单ID
   * @returns {Promise}
   */
  getPaymentDetail: async (id) => {
    try {
      const response = await request.get(`/web/pay/detail/${id}`)
      return response
    } catch (error) {
      console.error('查询支付订单详情失败:', error)
      throw error
    }
  },

  /**
   * 查询支付订单列表
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getPaymentList: async (params) => {
    try {
      const response = await request.post('/web/pay/list', params)
      return response
    } catch (error) {
      console.error('查询支付订单列表失败:', error)
      throw error
    }
  },

  /**
   * 发起支付
   * @param {Object} params 支付参数
   * @returns {Promise}
   */
  initiatePayment(params) {
    return request({
      url: '/web/pay/initiate',
      method: 'post',
      data: params
    })
  },

  /**
   * 查询支付状态
   * @param {String} orderNo 订单号
   * @returns {Promise}
   */
  getPaymentStatus(orderNo) {
    return request({
      url: `/web/pay/status/${orderNo}`,
      method: 'get'
    })
  },

  /**
   * 关闭支付订单
   * @param {Object} params 关闭参数
   * @returns {Promise}
   */
  closePayment(params) {
    return request({
      url: '/web/pay/close',
      method: 'post',
      data: params
    })
  },

  // ==================== 退款管理接口 ====================
  
  /**
   * 创建退款订单
   * @param {Object} refundData 退款数据
   * @returns {Promise}
   */
  createRefund(refundData) {
    return request({
      url: '/web/pay/refund/create',
      method: 'post',
      data: refundData
    })
  },

  /**
   * 查询退款订单详情
   * @param {Number} id 退款订单ID
   * @returns {Promise}
   */
  getRefundDetail(id) {
    return request({
      url: `/web/pay/refund/detail/${id}`,
      method: 'get'
    })
  },

  /**
   * 查询退款订单列表
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getRefundList(params) {
    return request({
      url: '/web/pay/refund/list',
      method: 'post',
      data: params
    })
  },

  /**
   * 发起退款
   * @param {Object} params 退款参数
   * @returns {Promise}
   */
  initiateRefund(params) {
    return request({
      url: '/web/pay/refund/initiate',
      method: 'post',
      data: params
    })
  },

  /**
   * 查询退款状态
   * @param {String} refundNo 退款单号
   * @returns {Promise}
   */
  getRefundStatus(refundNo) {
    return request({
      url: `/web/pay/refund/status/${refundNo}`,
      method: 'get'
    })
  },

  // ==================== 账户管理接口 ====================
  
  /**
   * 查询账户余额
   * @param {String} userId 用户ID
   * @returns {Promise}
   */
  getAccountBalance(userId) {
    return request({
      url: `/web/pay/account/balance/${userId}`,
      method: 'get'
    })
  },

  /**
   * 查询交易记录
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getAccountRecords(params) {
    return request({
      url: '/web/pay/account/records',
      method: 'post',
      data: params
    })
  },

  /**
   * 查询资产明细
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getAssetDetails(params) {
    return request({
      url: '/web/pay/account/details',
      method: 'post',
      data: params
    })
  },

  // ==================== 充值管理接口 ====================
  
  /**
   * 创建充值订单
   * @param {Object} rechargeData 充值数据
   * @returns {Promise}
   */
  createRecharge(rechargeData) {
    return request({
      url: '/web/pay/recharge/create',
      method: 'post',
      data: rechargeData
    })
  },

  /**
   * 查询充值记录
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getRechargeList(params) {
    return request({
      url: '/web/pay/recharge/list',
      method: 'post',
      data: params
    })
  },

  /**
   * 查询充值详情
   * @param {string|number} id 充值记录主键
   * @returns {Promise}
   */
  getRechargeDetail(id) {
    return request({
      url: '/web/pay/recharge/detail',
      method: 'post',
      data: { id }
    })
  },

  // ==================== 提现管理接口 ====================
  
  /**
   * 申请提现
   * @param {Object} withdrawData 提现数据
   * @returns {Promise}
   */
  applyWithdraw(withdrawData) {
    return request({
      url: '/web/pay/withdraw/apply',
      method: 'post',
      data: withdrawData
    })
  },

  /**
   * 查询提现记录
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getWithdrawList(params) {
    return request({
      url: '/web/pay/withdraw/list',
      method: 'post',
      data: params
    })
  },

  /**
   * 查询提现详情
   * @param {string|number} id 提现记录主键
   * @returns {Promise}
   */
  getWithdrawDetail(id) {
    return request({
      url: '/web/pay/withdraw/detail',
      method: 'post',
      data: { id }
    })
  },

  // ==================== 支付配置接口 ====================
  
  /**
   * 查询支付方式
   * @returns {Promise}
   */
  getPaymentMethods() {
    return request({
      url: '/web/pay/config/methods',
      method: 'get'
    })
  },

  /**
   * 查询支付配置
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getPaymentConfig(params) {
    return request({
      url: '/web/pay/config/list',
      method: 'post',
      data: params
    })
  },

  /**
   * 新增支付配置（后端 channelConfigSave 是 upsert，不传 id 即为新增）
   * @param {Object} data 配置表单（channelCode/channelName/provider/payMethod/merchantNo/configMap/status/remark）
   * @returns {Promise}
   */
  createPaymentConfig(data) {
    return request({
      url: '/web/pay/config/update',
      method: 'post',
      data
    })
  },

  /**
   * 编辑支付配置（传 id 为更新）
   * @param {Object} data 配置表单
   * @returns {Promise}
   */
  updatePaymentConfig(data) {
    return request({
      url: '/web/pay/config/update',
      method: 'post',
      data
    })
  },

  /**
   * 启用/停用支付配置
   * @param {Object} params { id, status: 0|1 }
   * @returns {Promise}
   */
  updatePaymentConfigStatus(params) {
    return request({
      url: '/web/pay/config/toggle',
      method: 'post',
      data: params
    })
  },

  /**
   * 删除支付配置
   * @param {string|number} id 记录 id（调用方传标量，内部包装为 {id} body）
   * @returns {Promise}
   */
  deletePaymentConfig(id) {
    return request({
      url: '/web/pay/config/delete',
      method: 'post',
      data: { id }
    })
  },

  // ==================== 交易管理接口（扩展） ====================

  /**
   * 统一下单
   * @param {Object} params 下单参数
   * @returns {Promise}
   */
  unifiedOrder(params) {
    return request({
      url: '/web/pay/trade/unifiedOrder',
      method: 'post',
      data: params
    })
  },

  /**
   * 查询交易订单列表
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  queryTradeOrder(params) {
    return request({
      url: '/web/pay/trade/queryOrder',
      method: 'post',
      data: params
    })
  },

  /**
   * 关闭订单
   * @param {Object} params 关闭参数
   * @returns {Promise}
   */
  closeTradeOrder(params) {
    return request({
      url: '/web/pay/trade/closeOrder',
      method: 'post',
      data: params
    })
  },

  /**
   * 发起退款（交易层）
   * @param {Object} params 退款参数
   * @returns {Promise}
   */
  tradeRefund(params) {
    return request({
      url: '/web/pay/trade/refund',
      method: 'post',
      data: params
    })
  },

  /**
   * 退款查询（交易层）
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  queryTradeRefund(params) {
    return request({
      url: '/web/pay/trade/queryRefund',
      method: 'post',
      data: params
    })
  },

  // ==================== 会员管理接口 ====================

  /**
   * 企业会员开户
   * @param {Object} params 企业开户信息
   * @returns {Promise}
   */
  regEnterprise(params) {
    return request({
      url: '/web/pay/member/regEnterprise',
      method: 'post',
      data: params
    })
  },

  /**
   * 个人会员开户
   * @param {Object} params 个人开户信息
   * @returns {Promise}
   */
  regPerson(params) {
    return request({
      url: '/web/pay/member/regPerson',
      method: 'post',
      data: params
    })
  },

  /**
   * 会员信息查询
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  queryMember(params) {
    return request({
      url: '/web/pay/member/query',
      method: 'post',
      data: params
    })
  },

  /**
   * 会员资料补录
   * @param {Object} params 补录信息
   * @returns {Promise}
   */
  regSupplement(params) {
    return request({
      url: '/web/pay/member/regSupplement',
      method: 'post',
      data: params
    })
  },

  // ==================== 分账管理接口 ====================

  /**
   * 批量分账
   * @param {Object} params 分账参数
   * @returns {Promise}
   */
  batchDivision(params) {
    return request({
      url: '/web/pay/division/batch',
      method: 'post',
      data: params
    })
  },

  /**
   * 分账查询
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  queryDivision(params) {
    return request({
      url: '/web/pay/division/query',
      method: 'post',
      data: params
    })
  },

  /**
   * 分账撤销
   * @param {Object} params 撤销参数
   * @returns {Promise}
   */
  refundDivision(params) {
    return request({
      url: '/web/pay/division/refund',
      method: 'post',
      data: params
    })
  },

  // ==================== 商户管理接口 ====================

  /**
   * 商户进件
   * @param {Object} params 进件信息
   * @returns {Promise}
   */
  mchReg(params) {
    return request({
      url: '/web/pay/mch/reg',
      method: 'post',
      data: params
    })
  },

  /**
   * 商户列表查询
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  queryMchList(params) {
    return request({
      url: '/web/pay/mch/list',
      method: 'post',
      data: params
    })
  },

  // ==================== 对账管理接口 ====================

  /**
   * 对账文件下载
   * @param {Object} params 下载参数
   * @returns {Promise}
   */
  downloadStatement(params) {
    return request({
      url: '/web/pay/statement/download',
      method: 'post',
      data: params
    })
  },

  /**
   * 账单导入
   * @param {Object} params 导入参数
   * @returns {Promise}
   */
  importBill(params) {
    return request({
      url: '/web/pay/bill/import',
      method: 'post',
      data: params
    })
  },

  // ==================== 账户/余额管理接口 ====================

  /**
   * 余额查询
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  queryBalance(params) {
    return request({
      url: '/web/pay/balance/query',
      method: 'post',
      data: params
    })
  }
}

/**
 * 支付状态枚举
 */
export const PAY_STATUS = {
  PENDING: 1,      // 待支付
  PAID: 2,         // 已支付
  CANCELLED: 3,    // 已取消
  FAILED: 4        // 支付失败
}

/**
 * 支付状态文本映射
 */
export const PAY_STATUS_TEXT = {
  [PAY_STATUS.PENDING]: '待支付',
  [PAY_STATUS.PAID]: '已支付',
  [PAY_STATUS.CANCELLED]: '已取消',
  [PAY_STATUS.FAILED]: '支付失败'
}

/**
 * 支付状态标签类型映射
 */
export const PAY_STATUS_TAG_TYPE = {
  [PAY_STATUS.PENDING]: 'warning',
  [PAY_STATUS.PAID]: 'success',
  [PAY_STATUS.CANCELLED]: 'danger',
  [PAY_STATUS.FAILED]: 'danger'
}

/**
 * 支付渠道枚举
 */
export const PAY_CHANNEL = {
  WECHAT: 1,       // 微信支付
  ALIPAY: 2,       // 支付宝
  BALANCE: 3,      // 余额支付
  CARD: 4          // 充值卡支付
}

/**
 * 支付渠道文本映射
 */
export const PAY_CHANNEL_TEXT = {
  [PAY_CHANNEL.WECHAT]: '微信支付',
  [PAY_CHANNEL.ALIPAY]: '支付宝',
  [PAY_CHANNEL.BALANCE]: '余额支付',
  [PAY_CHANNEL.CARD]: '充值卡支付'
}

/**
 * 交易类型枚举
 */
export const TRADE_TYPE = {
  PURCHASE: 1,     // 商品购买
  RECHARGE: 2,     // 余额充值
  WITHDRAW: 3,     // 提现
  TRANSFER: 4,     // 转账
  REFUND: 5        // 退款
}

/**
 * 交易类型文本映射
 */
export const TRADE_TYPE_TEXT = {
  [TRADE_TYPE.PURCHASE]: '商品购买',
  [TRADE_TYPE.RECHARGE]: '余额充值',
  [TRADE_TYPE.WITHDRAW]: '提现',
  [TRADE_TYPE.TRANSFER]: '转账',
  [TRADE_TYPE.REFUND]: '退款'
}

// 对外导出实例与默认服务，便于按需引入
export { payService as payApi }
export default payService