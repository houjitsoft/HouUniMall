import request from '@/utils/request'

export type GoodsList = {
    name: string            //	query	string  否	搜索商品名称
    category_id: string     //	query	string  否	搜索平台分类
    shop_category_id: string//	query	string  否	搜索商品分类
    type: number | string   //	query	integer 否	搜索类型 销售中sale 待审核audit 审核未通过audit_refuse 库存预警stock_warning 仓库中not_sale 回收站recycle
}
// 商品列表
export function getGoodsLists(params: GoodsList) {
    return request.get({ url: '/mall.Goods/lists', params })
}

// 商品公用列表
export function getGoodsCommonLists(params: GoodsList) {
    return request.get({ url: '/mall.goods/commonList', params })
}

// 商品其它列表
export function getGoodsOtherList() {
    return request.get({ url: '/mall.Goods/otherList' })
}

// 商品详情
export function getGoodsDetail(params: { id: number }) {
    return request.get({ url: '/mall.Goods/detail', params })
}

export type SkuNameValue = {
    value: string
    image: string
}
export type SkuNameList = {
    name: string
    value: SkuNameValue[]
    has_image?: number
}
export type SkuItemList = {
    id?: number | string
    ids?: number[]
    image?: string
    sku_value_arr: string[]
    price: string
    line_price: string
    market_price: string
    stock: number
    weight: number
    volume: string
    code: string
}
export type GoodsForm = {
    id?: number | string            //	query	number  是	商品编辑时
    name: string                    //	query	string  是	商品名称
    code: string                    //	query	string  是	商品货号
    descript: string                //	query	string  是	商品描述
    shop_category_id: number | string//	query	integer 是	平台分类
    category_ids: number[]          //	query	array[integer]  是	商品分类[1,2,3]
    godos_image: string[]           //	query	array[string]  是	商品轮播图[1,2,3]
    video_is: number                //	query	integer 否	是否添加视频0 否 1是
    video_form: number              //	query	integer 否	视频来源 1素材库 2链接 video_is 1时必填
    video_cover: string             //	query	string  否	视频封面
    video_uri: string               //	query	string  否	视频地址 video_is 1时必填
    sku_type: number                //	query	integer 是	规格类型 0单规格 1多规格
    sku_name_list?: SkuNameList[]   //  query   array   否  规格名
    sku?: SkuItemList[]             //  query   array   否  规格项
    goods_send_mode: string[]       //	query	array[string]  是	配送方式 0快递配送 [0]
    express_type: number            //	query	integer 是	运费设置 0包邮 1运费模板
    express_tpl_id: string          //	query	string  否	运费模板ID express_type=1时必须
    content: string                 //	query	string  是	商品详情
    stock_warning: number | string  //	query	integer 否	库存预警
    virtual_sale_num: number | string//	query	integer 否	虚拟销量
    virtual_visit_num: number | string//	query	integer 否	虚拟浏览量
    sort: number                    //	query	integer 否	排序
    shop_recommend: number          //	query	integer 是	店铺推荐 1推荐 0不推荐
    status: number                  //	query	integer 是	状态 0仓库 1上架
}
// 商品添加
export function apiGoodsAdd(params: GoodsForm) {
    return request.post({ url: '/mall.Goods/add', params })
}

// 商品编辑
export function apiGoodsEdit(params: GoodsForm) {
    return request.post({ url: '/mall.Goods/edit', params })
}

// 商品上下架
export function apiGoodsStatus(params: { id: number, status: number }) {
    return request.post({ url: '/mall.Goods/status', params })
}

// 商品移到回收站
export function apiGoodsDel(params: { id: number }) {
    return request.post({ url: '/mall.Goods/delete', params })
}