/**
 * 首页相关类型定义
 */

/** 轮播图数据类型 */
export interface BannerItem {
    configValue: string
    opValue: string
    opType: number
    configEnable?: number | string
    [key: string]: any
}

/** 商城快报数据类型 */
export interface NewsItem {
    classifyId?: number
    informationId?: number
    informationTitle: string
    [key: string]: any
}

/** 子分类项数据类型 */
export interface SectionItem {
    catId: number
    catName: string
}

/** 子分类区域数据类型 */
export interface SubCategorySection {
    secordCatId: number
    secordCatName: string
    catId: number
    gdsCats: SectionItem[]
}

/** 子分类响应数据类型 */
export interface SubCategoryResponse {
    data?: SubCategorySection[]
}

/** 分类数据类型 */
export interface CategoryItem {
    catId: number
    catName: string
    icon?: string
    navPic?: string
    catPic?: string
    /** 子分类树（二级/三级），首页左栏与悬停面板使用 */
    children?: any[]
    secordNameList?: {
        catId: number
        catName: string
    }[]
}

/** 快捷入口数据类型 */
export interface QuickLinkItem {
    icon: string
    text: string
    url: string
    type: number
}

/** 通用商品类型（用于首页展示） */
export interface HomeProduct {
    catId?: number
    goodsId?: number
    commonId?: number
    goodsName?: string
    goodsImage?: string
    commonName?: string
    commonImage?: string
    goodsPrice?: string | number
    commonPrice?: string | number
    discountPrice?: string | number
    tags?: string[]
    shopName?: string
    tag?: string
    preferentialPrice?: number
}

/** 分类标签类型 */
export interface CategoryTab {
    catId: number
    catName: string
    leftAdpageNavUrl: string
    leftAdpageNavName: string
    pageId?: number
    pageTitle?: string
    pageTwoTitle?: string
}

/** 分类数据项类型 */
export interface CategoryDataItem {
    adpageBannerList?: HomeProduct[]
    leftAdpageNavList?: CategoryTab[]
    rightRec?: {
        rightAdpageNavName?: string
        rightAdpageNavGoodsId?: string
    }
    rightAdpageNavList?: CategoryTab[]
    rightRecGoodsList?: HomeProduct[]
    floorAdpageNameNew?: {
        catName: string
        catId: number
    }
    pageTitle?: string
    pageName?: string
    goods?: HomeProduct[]
}

/** 楼层导航项类型 */
export interface FloorNavItem {
    catId: number
    catName: string
    [key: string]: any
}

/** 楼层项类型 */
export interface FloorItem {
    floorName: string
    floorNavList: FloorNavItem[]
    [key: string]: any
}

/** 分类类型 */
export interface Cat {
    catId: number
    catName: string
    [key: string]: any
}
