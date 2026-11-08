/**
 * Mall 商城后台 - 商品管理 API
 * 对接 bff-web /web/mall/admin/goods/*
 */
import request from '@/utils/request'

/* ============ 商品 ============ */
/** 商品列表（type: sale/not_sale/audit/audit_refuse/recycle） */
export const mallGoodsList = (data) => request.post('/web/mall/admin/goods/lists', data)

/** 商品详情 */
export const mallGoodsDetail = (id) => request.get('/web/mall/admin/goods/detail', { params: { id } })

/** 新增/编辑商品 */
export const mallGoodsSet = (data) => request.post('/web/mall/admin/goods/set', data)

/** 商品设置（虚拟销量/浏览量/排序） */
export const mallGoodsGoodsSet = (data) => request.post('/web/mall/admin/goods/goods_set', data)

/** 审核 */
export const mallGoodsAudit = (data) => request.post('/web/mall/admin/goods/audit', data)

/** 重审/违规重审 */
export const mallGoodsReAudit = (data) => request.post('/web/mall/admin/goods/re_audit', data)

/** 删除（软删/回收） */
export const mallGoodsDel = (data) => request.post('/web/mall/admin/goods/del', data)

/** 上下架 */
export const mallGoodsChangeStatus = (data) => request.post('/web/mall/admin/goods/change_status', data)

/** 推荐切换 */
export const mallGoodsChangeRecommend = (data) => request.post('/web/mall/admin/goods/change_recommend', data)

/* ============ 分类 ============ */
export const mallCategoryList = (name) => request.get('/web/mall/admin/goods/category/lists', { params: { name } })
export const mallCategoryAll = () => request.get('/web/mall/admin/goods/category/all')
/** 分类树（用于级联选择器） */
export const mallCategoryTree = () => request.get('/web/mall/admin/goods/category/tree')
export const mallCategoryDetail = (id) => request.get('/web/mall/admin/goods/category/detail', { params: { id } })
export const mallCategoryAdd = (data) => request.post('/web/mall/admin/goods/category/add', data)
export const mallCategoryEdit = (data) => request.post('/web/mall/admin/goods/category/edit', data)
export const mallCategoryDel = (data) => request.post('/web/mall/admin/goods/category/del', data)
export const mallCategoryStatus = (data) => request.post('/web/mall/admin/goods/category/status', data)

/* ============ 评论 ============ */
export const mallCommentList = (data) => request.post('/web/mall/admin/goods/comment/lists', data)
export const mallCommentSetIsShow = (data) => request.post('/web/mall/admin/goods/comment/set_is_show', data)
export const mallCommentReply = (data) => request.post('/web/mall/admin/goods/comment/reply', data)
export const mallCommentDel = (data) => request.post('/web/mall/admin/goods/comment/del', data)

export default {
  mallGoodsList,
  mallGoodsDetail,
  mallGoodsSet,
  mallGoodsGoodsSet,
  mallGoodsAudit,
  mallGoodsReAudit,
  mallGoodsDel,
  mallGoodsChangeStatus,
  mallGoodsChangeRecommend,
  mallCategoryList,
  mallCategoryAll,
  mallCategoryTree,
  mallCategoryDetail,
  mallCategoryAdd,
  mallCategoryEdit,
  mallCategoryDel,
  mallCategoryStatus,
  mallCommentList,
  mallCommentSetIsShow,
  mallCommentReply,
  mallCommentDel
}
