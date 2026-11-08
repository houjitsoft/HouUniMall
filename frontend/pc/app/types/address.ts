/**
 * 地址相关类型定义
 */

/**
 * 地区数据类型
 */
export interface DistrictItem {
    districtName: string
    districtId: string
    childDistrict?: DistrictItem[]
}

/**
 * 地区数据类型（用于地区代码选择）
 */
export interface Area {
    name: string
    code: string
    pinyin: string  // 拼音首字母（大写）
    countryId?: number  // 国家ID
}

/**
 * 接口返回的国家数据类型
 */
export interface CountryEntity {
    id: string
    countryCommonUse: number | string  // 是否常用（1/0 或 '1'/'0'）
    countryInitial: string  // 国家首字母
    countryName: string  // 国家名称
    deleteType: number | string  // 删除类型 1 不能正常 2 可以删除
    internationalDialingCode: string  // 国际区号
    countryId?: number  // 国家ID
}
