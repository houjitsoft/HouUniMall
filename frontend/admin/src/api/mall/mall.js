/**
 * 商城管理API接口
 * 包含营销活动、直播互动、发现内容等模块
 */

import request from '@/utils/request'

/**
 * 积分商城工具API
 */
export const pointsMallToolApi = {
  // 获取积分商品列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/points-tool/list', params)
      return response
    } catch (error) {
      console.error('获取积分商品列表失败:', error)
      throw error
    }
  },

  // 获取积分商品详情
  getDetail: async (id) => {
    try {
      const response = await request.post('/web/mall/points-tool/detail', { id })
      return response
    } catch (error) {
      console.error('获取积分商品详情失败:', error)
      throw error
    }
  },

  // 创建积分商品
  create: async (data) => {
    try {
      const response = await request.post('/web/mall/points-tool/create', data)
      return response
    } catch (error) {
      console.error('创建积分商品失败:', error)
      throw error
    }
  },

  // 更新积分商品
  update: async (data) => {
    try {
      const response = await request.post('/web/mall/points-tool/update', data)
      return response
    } catch (error) {
      console.error('更新积分商品失败:', error)
      throw error
    }
  },

  // 删除积分商品
  delete: async (id) => {
    try {
      const response = await request.post('/web/mall/points-tool/delete', { id })
      return response
    } catch (error) {
      console.error('删除积分商品失败:', error)
      throw error
    }
  },

  // 切换积分商品状态
  toggleStatus: async (id, status) => {
    try {
      const response = await request.post('/web/mall/points-tool/toggle-status', { id, status })
      return response
    } catch (error) {
      console.error('切换积分商品状态失败:', error)
      throw error
    }
  }
}

/**
 * 直播互动API
 */
export const liveActivityApi = {
  // 获取直播列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/live/list', params)
      return response
    } catch (error) {
      console.error('获取直播列表失败:', error)
      throw error
    }
  },

  // 获取直播详情
  getDetail: async (id) => {
    try {
      const response = await request.post('/web/mall/live/detail', { id })
      return response
    } catch (error) {
      console.error('获取直播详情失败:', error)
      throw error
    }
  },

  // 创建直播
  create: async (data) => {
    try {
      const response = await request.post('/web/mall/live/create', data)
      return response
    } catch (error) {
      console.error('创建直播失败:', error)
      throw error
    }
  },

  // 更新直播
  update: async (data) => {
    try {
      const response = await request.post('/web/mall/live/update', data)
      return response
    } catch (error) {
      console.error('更新直播失败:', error)
      throw error
    }
  },

  // 删除直播
  delete: async (id) => {
    try {
      const response = await request.post('/web/mall/live/delete', { id })
      return response
    } catch (error) {
      console.error('删除直播失败:', error)
      throw error
    }
  },

  // 开始直播
  start: async (id) => {
    try {
      const response = await request.post('/web/mall/live/start', { id })
      return response
    } catch (error) {
      console.error('开始直播失败:', error)
      throw error
    }
  },

  // 结束直播
  end: async (id) => {
    try {
      const response = await request.post('/web/mall/live/end', { id })
      return response
    } catch (error) {
      console.error('结束直播失败:', error)
      throw error
    }
  }
}

/**
 * 直播商品审核API
 */
export const liveProductAuditApi = {
  // 获取待审核商品列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/live/product/audit/list', params)
      return response
    } catch (error) {
      console.error('获取待审核商品列表失败:', error)
      throw error
    }
  },

  // 获取商品详情
  getDetail: async (id) => {
    try {
      const response = await request.post('/web/mall/live/product/audit/detail', { id })
      return response
    } catch (error) {
      console.error('获取商品详情失败:', error)
      throw error
    }
  },

  // 审核商品
  audit: async (data) => {
    try {
      const response = await request.post('/web/mall/live/product/audit/submit', data)
      return response
    } catch (error) {
      console.error('审核商品失败:', error)
      throw error
    }
  }
}

/**
 * 直播账号管理API
 */
export const liveAccountApi = {
  // 获取账号列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/live/account/list', params)
      return response
    } catch (error) {
      console.error('获取账号列表失败:', error)
      throw error
    }
  },

  // 获取账号详情
  getDetail: async (id) => {
    try {
      const response = await request.post('/web/mall/live/account/detail', { id })
      return response
    } catch (error) {
      console.error('获取账号详情失败:', error)
      throw error
    }
  },

  // 创建账号
  create: async (data) => {
    try {
      const response = await request.post('/web/mall/live/account/create', data)
      return response
    } catch (error) {
      console.error('创建账号失败:', error)
      throw error
    }
  },

  // 更新账号
  update: async (data) => {
    try {
      const response = await request.post('/web/mall/live/account/update', data)
      return response
    } catch (error) {
      console.error('更新账号失败:', error)
      throw error
    }
  },

  // 删除账号
  delete: async (id) => {
    try {
      const response = await request.post('/web/mall/live/account/delete', { id })
      return response
    } catch (error) {
      console.error('删除账号失败:', error)
      throw error
    }
  },

  // 切换账号状态
  toggleStatus: async (id, status) => {
    try {
      const response = await request.post('/web/mall/live/account/toggle-status', { id, status })
      return response
    } catch (error) {
      console.error('切换账号状态失败:', error)
      throw error
    }
  }
}

/**
 * 话题分类API
 */
export const topicCategoryApi = {
  // 获取分类列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/content/topic/category/list', params)
      return response
    } catch (error) {
      console.error('获取话题分类列表失败:', error)
      throw error
    }
  },

  // 获取分类详情
  getDetail: async (id) => {
    try {
      const response = await request.post('/web/mall/content/topic/category/detail', { id })
      return response
    } catch (error) {
      console.error('获取话题分类详情失败:', error)
      throw error
    }
  },

  // 创建分类
  create: async (data) => {
    try {
      const response = await request.post('/web/mall/content/topic/category/create', data)
      return response
    } catch (error) {
      console.error('创建话题分类失败:', error)
      throw error
    }
  },

  // 更新分类
  update: async (data) => {
    try {
      const response = await request.post('/web/mall/content/topic/category/update', data)
      return response
    } catch (error) {
      console.error('更新话题分类失败:', error)
      throw error
    }
  },

  // 删除分类
  delete: async (id) => {
    try {
      const response = await request.post('/web/mall/content/topic/category/delete', { id })
      return response
    } catch (error) {
      console.error('删除话题分类失败:', error)
      throw error
    }
  },

  // 切换分类状态
  toggleStatus: async (id, status) => {
    try {
      const response = await request.post('/web/mall/content/topic/category/toggle-status', { id, status })
      return response
    } catch (error) {
      console.error('切换话题分类状态失败:', error)
      throw error
    }
  }
}

/**
 * 选题列表API
 */
export const selectionListApi = {
  // 获取选题列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/content/selection/list', params)
      return response
    } catch (error) {
      console.error('获取选题列表失败:', error)
      throw error
    }
  },

  // 获取选题详情
  getDetail: async (id) => {
    try {
      const response = await request.post('/web/mall/content/selection/detail', { id })
      return response
    } catch (error) {
      console.error('获取选题详情失败:', error)
      throw error
    }
  },

  // 创建选题
  create: async (data) => {
    try {
      const response = await request.post('/web/mall/content/selection/create', data)
      return response
    } catch (error) {
      console.error('创建选题失败:', error)
      throw error
    }
  },

  // 更新选题
  update: async (data) => {
    try {
      const response = await request.post('/web/mall/content/selection/update', data)
      return response
    } catch (error) {
      console.error('更新选题失败:', error)
      throw error
    }
  },

  // 删除选题
  delete: async (id) => {
    try {
      const response = await request.post('/web/mall/content/selection/delete', { id })
      return response
    } catch (error) {
      console.error('删除选题失败:', error)
      throw error
    }
  },

  // 发布选题
  publish: async (id) => {
    try {
      const response = await request.post('/web/mall/content/selection/publish', { id })
      return response
    } catch (error) {
      console.error('发布选题失败:', error)
      throw error
    }
  },

  // 下架选题
  offline: async (id) => {
    try {
      const response = await request.post('/web/mall/content/selection/offline', { id })
      return response
    } catch (error) {
      console.error('下架选题失败:', error)
      throw error
    }
  }
}

/**
 * 心得管理API
 */
export const moodManageApi = {
  // 获取心得列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/content/mood/list', params)
      return response
    } catch (error) {
      console.error('获取心得列表失败:', error)
      throw error
    }
  },

  // 获取心得详情
  getDetail: async (id) => {
    try {
      const response = await request.post('/web/mall/content/mood/detail', { id })
      return response
    } catch (error) {
      console.error('获取心得详情失败:', error)
      throw error
    }
  },

  // 审核心得
  audit: async (data) => {
    try {
      const response = await request.post('/web/mall/content/mood/audit', data)
      return response
    } catch (error) {
      console.error('审核心得失败:', error)
      throw error
    }
  },

  // 删除心得
  delete: async (id) => {
    try {
      const response = await request.post('/web/mall/content/mood/delete', { id })
      return response
    } catch (error) {
      console.error('删除心得失败:', error)
      throw error
    }
  },

  // 下架心得
  offline: async (id) => {
    try {
      const response = await request.post('/web/mall/content/mood/offline', { id })
      return response
    } catch (error) {
      console.error('下架心得失败:', error)
      throw error
    }
  }
}

/**
 * 评论管理API
 */
export const reviewManageApi = {
  // 获取评论列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/content/review/list', params)
      return response
    } catch (error) {
      console.error('获取评论列表失败:', error)
      throw error
    }
  },

  // 获取评论详情
  getDetail: async (id) => {
    try {
      const response = await request.post('/web/mall/content/review/detail', { id })
      return response
    } catch (error) {
      console.error('获取评论详情失败:', error)
      throw error
    }
  },

  // 审核评论
  audit: async (data) => {
    try {
      const response = await request.post('/web/mall/content/review/audit', data)
      return response
    } catch (error) {
      console.error('审核评论失败:', error)
      throw error
    }
  },

  // 删除评论
  delete: async (id) => {
    try {
      const response = await request.post('/web/mall/content/review/delete', { id })
      return response
    } catch (error) {
      console.error('删除评论失败:', error)
      throw error
    }
  }
}

/**
 * 商城营销API（优惠券、秒杀、拼团）
 */
export const mallMarketingApi = {
  // 优惠券管理
  coupon: {
    // 获取优惠券列表
    getList: (params) => request.post('/web/mall/marketing/coupon/list', params),
    // 保存优惠券（新增或更新）
    save: (data) => request.post('/web/mall/marketing/coupon/save', data),
    // 删除优惠券
    delete: (params) => request.post('/web/mall/marketing/coupon/delete', params),
    // 发放优惠券
    issue: (params) => request.post('/web/mall/marketing/coupon/issue', params)
  },
  // 秒杀活动管理
  seckill: {
    // 获取秒杀活动列表
    getList: (params) => request.post('/web/mall/marketing/seckill/list', params),
    // 获取秒杀活动详情
    getDetail: (params) => request.post('/web/mall/marketing/seckill/detail', params),
    // 审核秒杀活动
    audit: (params) => request.post('/web/mall/marketing/seckill/audit', params),
    // 结束秒杀活动
    end: (params) => request.post('/web/mall/marketing/seckill/end', params)
  },
  // 拼团活动管理
  groupBuy: {
    // 获取拼团活动列表
    getList: (params) => request.post('/web/mall/marketing/group-buy/list', params),
    // 获取拼团活动详情
    getDetail: (params) => request.post('/web/mall/marketing/group-buy/detail', params),
    // 审核拼团活动
    audit: (params) => request.post('/web/mall/marketing/group-buy/audit', params),
    // 结束拼团活动
    end: (params) => request.post('/web/mall/marketing/group-buy/end', params)
  }
}

/**
 * 商城管理API集合导出
 */
export const mallApi = {
  pointsMallTool: pointsMallToolApi,
  liveActivity: liveActivityApi,
  liveProductAudit: liveProductAuditApi,
  liveAccount: liveAccountApi,
  topicCategory: topicCategoryApi,
  selectionList: selectionListApi,
  moodManage: moodManageApi,
  reviewManage: reviewManageApi,
  marketing: mallMarketingApi
}

export default mallApi
