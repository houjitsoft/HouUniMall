/**
 * 支付中心工作台（Dashboard）相关接口
 * 走 bff-gateway 统一网关：http://localhost:8396
 */
import request from '@/utils/request'

/**
 * 获取支付中心工作台概览指标
 * 返回字段（计划）：
 *  - todayAmount      今日交易金额（元）
 *  - todayCount       今日交易笔数
 *  - todayRefund      今日退款金额（元）
 *  - successRate      支付成功率（百分比，数值）
 * @param {Object} config 可选，透传 axios 配置（如 skipAuthRedirect/skipErrorMessage）
 * @returns {Promise<Object>}
 */
export const getPaymentOverview = async (config = {}) => {
  // 失败向上抛，由工作台展示明确错误；禁止静默吞掉导致误判「功能未实现」
  return request.post('/web/payment/dashboard/overview', {}, {
    skipErrorMessage: true,
    ...config
  })
}

export default {
  getPaymentOverview
}
