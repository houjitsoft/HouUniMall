/**
 * 商品管理API接口
 * 使用主服务的统一API服务层
 */

// 导入主服务的统一API服务
import request from '@/utils/request'

/**
 * 商品管理API接口
 */
export const goodsService = {
  // 获取商品列表
  getGoodsList: async (params) => {
    try {
      const response = await request.post('/web/mall/goods/list', params)
      return response
    } catch (error) {
      console.error('获取商品列表失败:', error)
      throw error
    }
  },

  // 获取商品详情
  getGoodsDetail: async (id) => {
    try {
      const response = await request.post('/web/mall/goods/detail', { id })
      return response
    } catch (error) {
      console.error('获取商品详情失败:', error)
      throw error
    }
  },

  // 创建商品
  createGoods: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/create', data)
      return response
    } catch (error) {
      console.error('创建商品失败:', error)
      throw error
    }
  },

  // 更新商品
  updateGoods: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/update', data)
      return response
    } catch (error) {
      console.error('更新商品失败:', error)
      throw error
    }
  },

  // 删除商品
  deleteGoods: async (id) => {
    try {
      const response = await request.post('/web/mall/goods/delete', { id })
      return response
    } catch (error) {
      console.error('删除商品失败:', error)
      throw error
    }
  },

  // 更新商品状态
  updateGoodsStatus: async (id, status) => {
    try {
      const response = await request.post('/web/mall/goods/status', { id, status })
      return response
    } catch (error) {
      console.error('更新商品状态失败:', error)
      throw error
    }
  },

  // 更新商品库存
  updateGoodsStock: async (id, stock) => {
    try {
      const response = await request.post('/web/mall/goods/stock', { id, stock })
      return response
    } catch (error) {
      console.error('更新商品库存失败:', error)
      throw error
    }
  },

  // 获取商品统计数据
  getGoodsStatistics: async () => {
    try {
      const response = await request.post('/web/mall/goods/statistics')
      return response
    } catch (error) {
      console.error('获取商品统计数据失败:', error)
      throw error
    }
  },

  // 获取商品概览数据
  getGoodsOverview: async () => {
    try {
      const response = await request.post('/web/mall/goods/overview')
      return response
    } catch (error) {
      console.error('获取商品概览数据失败:', error)
      throw error
    }
  }
}

/**
 * 商品分类API接口
 */
export const categoryService = {
  // 获取分类列表
  getCategoryList: async (params) => {
    try {
      const response = await request.post('/web/mall/goods/category/list', params)
      return response
    } catch (error) {
      console.error('获取分类列表失败:', error)
      throw error
    }
  },

  // 获取分类树形结构
  getCategoryTree: async () => {
    try {
      const response = await request.post('/web/mall/goods/category/tree')
      return response
    } catch (error) {
      console.error('获取分类树形结构失败:', error)
      throw error
    }
  },

  // 创建分类
  createCategory: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/category/create', data)
      return response
    } catch (error) {
      console.error('创建分类失败:', error)
      throw error
    }
  },

  // 更新分类
  updateCategory: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/category/update', data)
      return response
    } catch (error) {
      console.error('更新分类失败:', error)
      throw error
    }
  },

  // 删除分类
  deleteCategory: async (id) => {
    try {
      const response = await request.post('/web/mall/goods/category/delete', { id })
      return response
    } catch (error) {
      console.error('删除分类失败:', error)
      throw error
    }
  }
}

/**
 * 商品品牌API接口
 */
export const brandService = {
  // 获取品牌列表
  getBrandList: async (params) => {
    try {
      const response = await request.post('/web/mall/goods/brand/list', params)
      return response
    } catch (error) {
      console.error('获取品牌列表失败:', error)
      throw error
    }
  },

  // 创建品牌
  createBrand: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/brand/create', data)
      return response
    } catch (error) {
      console.error('创建品牌失败:', error)
      throw error
    }
  },

  // 更新品牌
  updateBrand: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/brand/update', data)
      return response
    } catch (error) {
      console.error('更新品牌失败:', error)
      throw error
    }
  },

  // 删除品牌
  deleteBrand: async (id) => {
    try {
      const response = await request.post('/web/mall/goods/brand/delete', { id })
      return response
    } catch (error) {
      console.error('删除品牌失败:', error)
      throw error
    }
  },

  // 获取所有品牌列表（不分页）
  getAllBrands: async () => {
    try {
      const response = await request.post('/web/mall/goods/brand/all')
      return response
    } catch (error) {
      console.error('获取所有品牌列表失败:', error)
      throw error
    }
  }
}

/**
 * 品牌类型管理API接口
 */
export const brandTypeService = {
  // 获取品牌类型列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/goods/brand-type/list', params)
      return response
    } catch (error) {
      console.error('获取品牌类型列表失败:', error)
      throw error
    }
  },

  // 创建品牌类型
  create: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/brand-type/create', data)
      return response
    } catch (error) {
      console.error('创建品牌类型失败:', error)
      throw error
    }
  },

  // 更新品牌类型
  update: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/brand-type/update', data)
      return response
    } catch (error) {
      console.error('更新品牌类型失败:', error)
      throw error
    }
  },

  // 删除品牌类型
  delete: async (id) => {
    try {
      const response = await request.post('/web/mall/goods/brand-type/delete', { id })
      return response
    } catch (error) {
      console.error('删除品牌类型失败:', error)
      throw error
    }
  }
}

/**
 * 商品规格API接口
 */
export const specService = {
  // 获取规格列表
  getSpecList: async (params) => {
    try {
      const response = await request.post('/web/mall/goods/spec/list', params)
      return response
    } catch (error) {
      console.error('获取规格列表失败:', error)
      throw error
    }
  },

  // 创建规格
  createSpec: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/spec/create', data)
      return response
    } catch (error) {
      console.error('创建规格失败:', error)
      throw error
    }
  },

  // 更新规格
  updateSpec: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/spec/update', data)
      return response
    } catch (error) {
      console.error('更新规格失败:', error)
      throw error
    }
  },

  // 删除规格
  deleteSpec: async (id) => {
    try {
      const response = await request.post('/web/mall/goods/spec/delete', { id })
      return response
    } catch (error) {
      console.error('删除规格失败:', error)
      throw error
    }
  }
}

/**
 * 商品统计API接口
 */
export const goodsStatisticsService = {
  // 获取商品统计数据
  getGoodsStatistics: async () => {
    try {
      const response = await request.post('/web/mall/goods/statistics')
      return response
    } catch (error) {
      console.error('获取商品统计数据失败:', error)
      throw error
    }
  },

  // 获取商品概览数据
  getGoodsOverview: async () => {
    try {
      const response = await request.post('/web/mall/goods/overview')
      return response
    } catch (error) {
      console.error('获取商品概览数据失败:', error)
      throw error
    }
  },

  // 获取热销商品排行榜
  getTopGoods: async () => {
    try {
      const response = await request.post('/web/mall/goods/top')
      return response
    } catch (error) {
      console.error('获取热销商品排行榜失败:', error)
      throw error
    }
  },

  // 获取商品销量趋势
  getSalesTrend: async () => {
    try {
      const response = await request.post('/web/mall/goods/sales/trend')
      return response
    } catch (error) {
      console.error('获取商品销量趋势失败:', error)
      throw error
    }
  },

  // 获取商品分类分布
  getCategoryDistribution: async () => {
    try {
      const response = await request.post('/web/mall/goods/category/distribution')
      return response
    } catch (error) {
      console.error('获取商品分类分布失败:', error)
      throw error
    }
  }
}

/**
 * 商品属性API接口
 */
export const attributeService = {
  // 获取属性列表
  getAttributeList: async (params) => {
    try {
      const response = await request.post('/web/mall/goods/attribute/list', params)
      return response
    } catch (error) {
      console.error('获取属性列表失败:', error)
      throw error
    }
  },

  // 创建属性
  createAttribute: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/attribute/create', data)
      return response
    } catch (error) {
      console.error('创建属性失败:', error)
      throw error
    }
  },

  // 更新属性
  updateAttribute: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/attribute/update', data)
      return response
    } catch (error) {
      console.error('更新属性失败:', error)
      throw error
    }
  },

  // 删除属性
  deleteAttribute: async (id) => {
    try {
      const response = await request.post('/web/mall/goods/attribute/delete', { id })
      return response
    } catch (error) {
      console.error('删除属性失败:', error)
      throw error
    }
  }
}

// 为了保持向后兼容，单独导出函数
export const {
  getGoodsList,
  getGoodsDetail,
  createGoods,
  updateGoods,
  deleteGoods,
  updateGoodsStatus,
  updateGoodsStock,
  getGoodsStatistics,
  getGoodsOverview
} = goodsService

export const getGoodsLists = goodsService.getGoodsList

export const {
  getCategoryList,
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory
} = categoryService

export const {
  getBrandList,
  createBrand,
  updateBrand,
  deleteBrand
} = brandService

export const {
  getSpecList,
  createSpec,
  updateSpec,
  deleteSpec
} = specService

export const {
  getTopGoods,
  getSalesTrend,
  getCategoryDistribution
} = goodsStatisticsService

export const {
  getAttributeList,
  createAttribute,
  updateAttribute,
  deleteAttribute
} = attributeService

/**
 * 国产替换API接口
 */
export const domesticReplaceService = {
  // 获取国产替换列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/goods/domestic-replace/list', params)
      return response
    } catch (error) {
      console.error('获取国产替换列表失败:', error)
      throw error
    }
  },

  // 获取替代品列表
  getReplacementList: async (params) => {
    try {
      const response = await request.post('/web/mall/goods/domestic-replace/replacement-list', params)
      return response
    } catch (error) {
      console.error('获取替代品列表失败:', error)
      throw error
    }
  }
}

/**
 * 属性组管理API接口
 */
export const attributeGroupService = {
  // 获取属性组列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/goods/attribute-group/list', params)
      return response
    } catch (error) {
      console.error('获取属性组列表失败:', error)
      throw error
    }
  },

  // 创建属性组
  create: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/attribute-group/create', data)
      return response
    } catch (error) {
      console.error('创建属性组失败:', error)
      throw error
    }
  },

  // 更新属性组
  update: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/attribute-group/update', data)
      return response
    } catch (error) {
      console.error('更新属性组失败:', error)
      throw error
    }
  },

  // 删除属性组
  delete: async (id) => {
    try {
      const response = await request.post('/web/mall/goods/attribute-group/delete', { id })
      return response
    } catch (error) {
      console.error('删除属性组失败:', error)
      throw error
    }
  },

  // 获取属性类型列表
  getAttributeTypes: async () => {
    try {
      const response = await request.post('/web/mall/goods/attribute-type/list')
      return response
    } catch (error) {
      console.error('获取属性类型列表失败:', error)
      throw error
    }
  }
}

/**
 * 属性类型管理API接口
 */
export const attributeTypeService = {
  // 获取属性类型列表
  getList: async (params) => {
    try {
      const response = await request.post('/web/mall/goods/attribute-type/page', params)
      return response
    } catch (error) {
      console.error('获取属性类型列表失败:', error)
      throw error
    }
  },

  // 创建属性类型
  create: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/attribute-type/create', data)
      return response
    } catch (error) {
      console.error('创建属性类型失败:', error)
      throw error
    }
  },

  // 更新属性类型
  update: async (data) => {
    try {
      const response = await request.post('/web/mall/goods/attribute-type/update', data)
      return response
    } catch (error) {
      console.error('更新属性类型失败:', error)
      throw error
    }
  },

  // 删除属性类型
  delete: async (id) => {
    try {
      const response = await request.post('/web/mall/goods/attribute-type/delete', { id })
      return response
    } catch (error) {
      console.error('删除属性类型失败:', error)
      throw error
    }
  }
}

// 为了保持向后兼容，添加API别名
export const goodsApi = goodsService
export const categoryApi = categoryService
export const brandApi = brandService
export const specApi = specService
export const statisticsApi = goodsStatisticsService
export const attributeApi = attributeService
export const domesticReplaceApi = domesticReplaceService
export const attributeGroupApi = attributeGroupService
export const attributeTypeApi = attributeTypeService
export const brandTypeApi = brandTypeService

// 导出服务实例
export default {
  goodsService,
  categoryService,
  brandService,
  specService,
  goodsStatisticsService,
  attributeService,
  domesticReplaceService,
  attributeGroupService,
  attributeTypeService,
  brandTypeService
}