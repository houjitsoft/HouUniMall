/**
 * 商城Dashboard API接口
 */
import request from '@/utils/request'

/**
 * 获取商城概览数据
 */
export const getOverviewData = async () => {
  try {
    const response = await request.post('/web/mall/dashboard/overview')
    return response
  } catch (error) {
    console.error('获取概览数据失败:', error)
    throw error
  }
}

/**
 * 获取实时监控数据
 */
export const getRealtimeData = async () => {
  try {
    const response = await request.post('/web/mall/dashboard/realtime')
    return response
  } catch (error) {
    console.error('获取实时数据失败:', error)
    throw error
  }
}

/**
 * 获取销售趋势数据
 */
export const getSalesTrend = async (params) => {
  try {
    const response = await request.post('/web/mall/dashboard/sales-trend', params)
    return response
  } catch (error) {
    console.error('获取销售趋势失败:', error)
    throw error
  }
}

/**
 * 获取商品排行
 */
export const getGoodsRank = async (params) => {
  try {
    const response = await request.post('/web/mall/dashboard/goods-rank', params)
    return response
  } catch (error) {
    console.error('获取商品排行失败:', error)
    throw error
  }
}

/**
 * 获取快捷操作数据
 */
export const getQuickActions = async () => {
  try {
    const response = await request.post('/web/mall/dashboard/quick-actions')
    return response
  } catch (error) {
    console.error('获取快捷操作失败:', error)
    throw error
  }
}

export default {
  getOverviewData,
  getRealtimeData,
  getSalesTrend,
  getGoodsRank,
  getQuickActions
}
