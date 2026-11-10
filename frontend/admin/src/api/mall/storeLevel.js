/**
 * 平台后台 - 店铺等级设置 API
 *
 * 数据源：hou_shop_level。这张表同时决定入驻页 /bcenter/#/join/step3 的
 * 「店铺等级」下拉选项与「查看店铺权益和收费标准」弹层内容。
 *
 * 路径刻意使用 /merchant/shopLevel/manage/* 三段形式：
 * baseURL 是 /web，拼出 /web/merchant/shopLevel/manage/{action} 后由
 * BFF WebMerchantBizRouteController 的 /web/merchant/** 通配转发，自动补
 * /merchant/biz 前缀；三段路径不会被 WebMerchantController 的
 * /{module}/{action} 两段模板抢走（只有 /shopLevel/select 走精确映射特例）。
 */
import request from '@/utils/request'

// admin 构建产物没有 .env.production，VITE_APP_BASE_API 为空，
// 既有 api（如 mall/shopAdmin.js）一律写完整 /web/... 路径，此处保持一致。
const BASE = '/web/merchant/shopLevel/manage'

/** 平台后台：全部店铺等级列表（含不对入驻页展示的等级） */
export const shopLevelList = () => request.post(`${BASE}/list`, {})

/** 平台后台：新增或编辑店铺等级（id 为空为新增） */
export const shopLevelSave = (data) => request.post(`${BASE}/save`, data)

/** 平台后台：删除未被店铺使用的店铺等级 */
export const shopLevelDelete = (data) => request.post(`${BASE}/delete`, data)
