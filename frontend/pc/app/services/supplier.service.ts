import type { ApiResponse } from '~/types/api'
import type { ShopJoinAgreeData } from '~/types/business'

const { post } = useRequest()

/**
 * 供应商入驻同意接口
 * @returns 入驻状态数据
 */
export const supplierShopJoinBaseAgree = async (): Promise<ApiResponse<ShopJoinAgreeData>> => {
    return await post<ShopJoinAgreeData>('/mall-supplier/supplierShopJoinBase/agree', {})
}
