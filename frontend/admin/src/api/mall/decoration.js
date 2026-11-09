/**
 * 商城装修模块API接口
 * 包含商城风格、页面装修、底部导航、营销数据、店铺推荐等功能
 */

import request from '@/utils/request'

/**
 * 获取商城风格主题
 * @returns {Promise} 商城风格配置数据
 */
export function getMallStyle() {
  return request.get('/web/mall/config/get_mall_style')
}

/**
 * 保存商城风格主题
 * @param {Object} data - 风格主题配置数据
 * @returns {Promise} 保存结果
 */
export function setMallStyle(data) {
  return request.post('/web/mall/config/set_mall_style', data)
}

/**
 * 获取页面装修详情
 * @param {Object} params - 查询参数（如页面ID等）
 * @returns {Promise} 页面装修详情数据
 */
export function getPageDetail(params) {
  return request.get('/web/mall/decorate/page/detail', { params })
}

/**
 * 保存页面装修数据
 * @param {Object} data - 页面装修配置数据
 * @returns {Promise} 保存结果
 */
export function savePage(data) {
  return request.post('/web/mall/decorate/page/save', data)
}

/**
 * 获取首页装修页面列表
 * @param {Object} params - 查询参数（如分页信息等）
 * @returns {Promise} 首页装修页面列表数据
 */
export function getHomeList(params) {
  return request.get('/web/mall/decorate/page/home_lists', { params })
}

/**
 * 删除装修页面
 * @param {Object} data - 删除参数（如页面ID）
 * @returns {Promise} 删除结果
 */
export function deletePage(data) {
  return request.post('/web/mall/decorate/page/delete', data)
}

/**
 * 新增装修页面
 * @param {Object} data - 新增页面参数（如页面名称、类型等）
 * @returns {Promise} 新增结果
 */
export function addPage(data) {
  return request.post('/web/mall/decorate/page/add', data)
}

/**
 * 设置首页装修页面
 * @param {Object} data - 设置参数（如页面ID）
 * @returns {Promise} 设置结果
 */
export function setHomePage(data) {
  return request.post('/web/mall/decorate/page/set_home', data)
}

/**
 * 获取首页文章数据
 * @returns {Promise} 首页文章数据列表
 */
export function getArticleData() {
  return request.get('/web/mall/decorate/data/article')
}

/**
 * 获取底部导航详情
 * @returns {Promise} 底部导航配置数据
 */
export function getTabbarDetail() {
  return request.get('/web/mall/decorate/tabbar/detail')
}

/**
 * 保存底部导航配置
 * @param {Object} data - 底部导航配置数据
 * @returns {Promise} 保存结果
 */
export function saveTabbar(data) {
  return request.post('/web/mall/decorate/tabbar/save', data)
}

/**
 * 移动端页面模板库
 * 与 H5 首页读的是同一条装修页面记录（hou_base_decorate_page.page_template_code）
 * @returns {Promise} 模板列表（code/name/summary/themeColor/style/columns）
 */
export function getDecoratePageTemplates() {
  return request.post('/web/mall/decorate/page/template/list')
}

/**
 * 读取某装修页面当前套用的模板 code
 * @param {Object} data - { pageId }
 * @returns {Promise} templateCode 字符串，未套用为空串
 */
export function getDecoratePageTemplate(data) {
  return request.post('/web/mall/decorate/page/template/current', data)
}

/**
 * 为装修页面套用模板
 * @param {Object} data - { pageId, templateCode }，templateCode 传空串取消套用
 * @returns {Promise} 实际生效的 templateCode
 */
export function applyDecoratePageTemplate(data) {
  return request.post('/web/mall/decorate/page/template/apply', data)
}

/**
 * 获取拼团商品列表（营销数据）
 * @param {Object} params - 查询参数（如分页信息等）
 * @returns {Promise} 拼团商品列表数据
 */
export function getTeamGoods(params) {
  return request.get('/web/mall/pt/goodsOtherLists', { params })
}

/**
 * 获取秒杀商品列表（营销数据）
 * @param {Object} params - 查询参数（如分页信息等）
 * @returns {Promise} 秒杀商品列表数据
 */
export function getSeckillGoods(params) {
  return request.get('/web/mall/ms/goodsOtherLists', { params })
}

/**
 * 获取推荐店铺列表
 * @param {Object} params - 查询参数（如分页信息等）
 * @returns {Promise} 推荐店铺列表数据
 */
export function getRecommendShops(params) {
  return request.get('/web/mall/shop/recommend_lists', { params })
}

/* =====================================================================
 * 底部导航（平台级）—— 与 H5 首页底部栏读的是同一条记录
 *
 * H5 底部栏取 hou_base_modularizationd 里 page_is_index = 3 那条的 page_data，
 * 结构 { color, defaultColor, background, showIcon, items:[{navTitle,navUrl,icon,selectedIcon,show,navIndex}] }。
 * 老接口 /web/mall/decorate/tabbar/* 读的是 hou_decorate_tabbar（H5 不读），属历史遗留，
 * 后台改那边不会生效，故统一切到这个接口。
 * ===================================================================== */

/**
 * 读取平台底部导航配置
 * @returns {Promise} { pagedata: '<json 字符串>', pageId }
 */
export function getBottomNavDetail() {
  return request.post('/web/base/baseModularizationd/queryBottomNavigation', {})
}

/**
 * 保存平台底部导航配置
 * @param {Object} data - { pageId, pagedata: '<json 字符串>' }
 * ⚠️ 后端 ModularizationdDTO 的 @JsonProperty 是 pagedata（全小写）；传 pageData 会被忽略并把线上底栏写成 NULL。
 * @returns {Promise}
 */
export function saveBottomNav(data) {
  return request.post('/web/base/baseModularizationd/bottomNavigationUpdate', data)
}
