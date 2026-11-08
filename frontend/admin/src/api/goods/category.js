import request from '@/utils/request'

/**
 * 获取商品分类列表
 * 路径已修正：BFF MallGoodsController 提供 POST /web/mall/goods/category/list（无 /lists 且不支持 GET）
 * @param {Object} params 查询参数
 */
export function getGoodsCategoryLists(params) {
  return request.post('/web/mall/goods/category/list', params)
}
