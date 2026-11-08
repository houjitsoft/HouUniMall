import request from '@/utils/request'


// 商品分类列表
export async function getGoodsCategoryLists(params: { page_no: number, page_size: number }) {
    const data = await request.get({ url: '/mall.shopGoodsCategory/lists', params })
    return {
        ...data,
        lists: data.lists.map((row: Record<string, any>) => ({
            ...row,
            is_show: row.isShow,
            create_time: row.createTime
        }))
    }
}

// 商品其它分类列表
export function getGoodsCategoryOtherLists() {
    return request.get({ url: '/mall.shopGoodsCategory/otherList' })
}

// 平台分类列表
export function getShopCategoryLists() {
    return request.get({ url: '/mall.goods/category_lists' })
}

// 商品分类详情
export async function getGoodsCategoryDetail(params: { id: number }) {
    const data = await request.get({ url: '/mall.shopGoodsCategory/detail', params })
    return { ...data, is_show: data.isShow }
}

export type CategoryForm = {
    id: string | number     //	body   number  否	分类ID
    name: string            //	body   string  是	分类名称
    image: string           //	body   string  否	分类图标
    sort: string | number   //	body   string  否	排序
    is_show: number         //	body   number  否	是否展示
    [index: string]: string | number;
}
// 商品分类添加
export function apiGoodsCategoryAdd(params: CategoryForm) {
    const { is_show, ...data } = params
    return request.post({ url: '/mall.shopGoodsCategory/add', params: { ...data, isShow: is_show } })
}

// 商品分类编辑
export function apiGoodsCategoryEdit(params: CategoryForm) {
    const { is_show, ...data } = params
    return request.post({ url: '/mall.shopGoodsCategory/edit', params: { ...data, isShow: is_show } })
}

// 商品分类删除
export function apiGoodsCategoryDel(params: { id: number }) {
    return request.post({ url: '/mall.shopGoodsCategory/delete', params })
}

// 商品分类状态
export function apiGoodsCategoryStatus(params: { id: number, is_show: number }) {
    return request.post({ url: '/mall.shopGoodsCategory/status', params: { id: params.id, isShow: params.is_show } })
}