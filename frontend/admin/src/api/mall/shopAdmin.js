/**
 * Mall 商城后台 - 商家管理 API
 * 对接 bff-web 实际路由：
 *   商家列表/详情/操作  → /web/mall/merchant/*
 *   入驻申请           → /web/mall/store/merchant-entry/*
 *   经营类目           → /web/mall/admin/shop/category/*（待补充后端）
 *   商家菜单           → /web/mall/admin/shop/menu/*（待补充后端）
 *   订单列表           → /web/mall/order/manage/list
 *   售后订单           → /web/mall/order/manage/aftersale/list
 */
import request from '@/utils/request'

/* ============ 商家列表 ============ */
/**
 * 商家列表
 * @param {object} data - 查询参数（page, size, name, type 等）
 * @returns {Promise}
 */
export const mallShopLists = (data) => request.post('/web/mall/merchant/list', data)

/**
 * 商家详情
 * @param {object} data - { id }
 * @returns {Promise}
 */
export const mallShopDetail = (data) => request.post('/web/mall/merchant/detail', data)

/**
 * 新增/编辑商家（保存设置）
 * @param {object} data - 商家数据
 * @returns {Promise}
 */
export const mallShopSave = (data) => request.post('/web/mall/merchant/update', data)

/**
 * 开通商家店铺和商家中心主账号
 * @param {object} data - 店铺与登录账号资料
 */
export const mallShopProvisionCreate = (data) => request.post('/web/mall/merchant/create', data)

/** 获取新增商家表单选项 */
export const mallShopProvisionOptions = () => request.post('/web/mall/merchant/options')

/** 平台店铺列表筛选和资料表单的只读等级选项。 */
export const mallShopLevelOptions = () => request.post('/web/mall/merchant/level-options')

/** 获取新增店铺时可绑定的统一账号 */
export const mallShopProvisionAccountOptions = (data) => request.post('/web/mall/merchant/account-options', data || {})

/**
 * 删除商家（暂无独立接口，预留）
 * @param {object} data - { id }
 * @returns {Promise}
 */
export const mallShopDel = (data) => request.post('/web/mall/store/setting/save', data)

/**
 * 更改商家状态（正常/冻结）
 * 通过保存设置接口传 status 字段实现
 * @param {object} data - { id, status }
 * @returns {Promise}
 */
export const mallShopStatus = (data) => request.post('/web/mall/merchant/status', data)

/* ============ 入驻申请 ============ */
/**
 * 入驻申请列表
 * @param {object} data - 查询参数
 * @returns {Promise}
 */
export const mallShopApplyLists = (data) => request.post('/web/mall/store/audit/list', data)

/**
 * 跟进/审核入驻申请
 * @param {object} data - { id, status, remark }
 * @returns {Promise}
 */
export const mallShopApplyFollow = (data) => request.post('/web/mall/store/audit', data)

/* ============ 经营类目 ============ */
/**
 * 经营类目列表
 * @param {object} data - 查询参数
 * @returns {Promise}
 */
export const mallShopCategoryLists = (data) => request.post('/web/mall/store/shop-category/list', data)

/**
 * 经营类目详情
 * @param {object} data - { id }
 * @returns {Promise}
 */
export const mallShopCategoryDetail = (data) => request.post('/web/mall/store/shop-category/detail', data)

/**
 * 新增经营类目
 * @param {object} data - 类目数据
 * @returns {Promise}
 */
export const mallShopCategoryAdd = (data) => request.post('/web/mall/store/shop-category/create', data)

/**
 * 编辑经营类目
 * @param {object} data - 类目数据（必须包含id）
 * @returns {Promise}
 */
export const mallShopCategoryEdit = (data) => request.post('/web/mall/store/shop-category/update', data)

/**
 * 删除经营类目
 * @param {object} data - { id }
 * @returns {Promise}
 */
export const mallShopCategoryDel = (data) => request.post('/web/mall/store/shop-category/delete', data)

/**
 * 更改经营类目状态
 * @param {object} data - { id, status }
 * @returns {Promise}
 */
export const mallShopCategoryStatus = (data) => request.post('/web/mall/store/shop-category/status', data)

/**
 * 获取启用经营类目列表（下拉选择用）
 * @returns {Promise}
 */
export const mallShopCategoryEnabled = () => request.post('/web/mall/store/shop-category/enabled')

/* ============ 商家菜单权限 ============ */
/**
 * 商家菜单树形列表
 * @returns {Promise}
 */
export const mallShopMenuLists = () => request.post('/web/mall/store/shop-menu/list')

/**
 * 商家菜单详情
 * @param {object} data - { id }
 * @returns {Promise}
 */
export const mallShopMenuDetail = (data) => request.post('/web/mall/store/shop-menu/detail', data)

/**
 * 新增商家菜单
 * @param {object} data - 菜单数据
 * @returns {Promise}
 */
export const mallShopMenuAdd = (data) => request.post('/web/mall/store/shop-menu/create', data)

/**
 * 编辑商家菜单
 * @param {object} data - 菜单数据（必须包含id）
 * @returns {Promise}
 */
export const mallShopMenuEdit = (data) => request.post('/web/mall/store/shop-menu/update', data)

/**
 * 删除商家菜单
 * @param {object} data - { id }
 * @returns {Promise}
 */
export const mallShopMenuDelete = (data) => request.post('/web/mall/store/shop-menu/delete', data)

/**
 * 更改商家菜单状态
 * @param {object} data - { id, status }
 * @returns {Promise}
 */
export const mallShopMenuStatus = (data) => request.post('/web/mall/store/shop-menu/status', data)

/**
 * 获取启用菜单列表（父级选择用）
 * @returns {Promise}
 */
export const mallShopMenuEnabled = () => request.post('/web/mall/store/shop-menu/enabled')

/* ============ 订单 ============ */
/**
 * 商城订单列表（管理端）
 * @param {object} data - 查询参数
 * @returns {Promise}
 */
export const mallOrderLists = (data) => request.post('/web/mall/order/manage/list', data)

/**
 * 售后订单列表（管理端）
 * @param {object} data - 查询参数
 * @returns {Promise}
 */
export const mallAfterSaleLists = (data) => request.post('/web/mall/order/manage/aftersale/list', data)

export default {
  mallShopLists,
  mallShopDetail,
  mallShopSave,
  mallShopProvisionCreate,
  mallShopProvisionOptions,
  mallShopDel,
  mallShopStatus,
  mallShopApplyLists,
  mallShopApplyFollow,
  mallShopCategoryLists,
  mallShopCategoryDetail,
  mallShopCategoryAdd,
  mallShopCategoryEdit,
  mallShopCategoryDel,
  mallShopCategoryStatus,
  mallShopCategoryEnabled,
  mallShopMenuLists,
  mallShopMenuDetail,
  mallShopMenuAdd,
  mallShopMenuEdit,
  mallShopMenuDelete,
  mallShopMenuStatus,
  mallShopMenuEnabled,
  mallOrderLists,
  mallAfterSaleLists
}
