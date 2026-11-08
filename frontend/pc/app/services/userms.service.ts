import type { ApiResponse, ApiParams } from '~/types/api'
import type { UserMemberInformationData } from '~/types/base'
import type { PlusMemberPackageDatum, PlusInfoOrderPageData } from '~/types/plus'
import type {
    UserInfo,
    CheckFavoriteShopParams,
    AddFavoriteShopParams,
    DeleteFavoriteShopParams,
    UserDefaultAddress,
    UserAddress,
    GetAddressesParams,
    AddAddressParams,
    UpdateAddressParams,
    DeleteAddressParams,
    BusinessRegistrationParams,
    IndividualRegistrationParams,
    ForgotPasswordParams
} from '~/types/user'

const { post } = useRequest()

// 查询是否收藏店铺（data === 1 为已收藏）
export const checkFavoriteShopOrNot = async (params: CheckFavoriteShopParams) => {
    return await post<number>('/mall-userms/user/favoritesShop/check', params)
}

// 查询商品是否收藏（data === 1 为已收藏）
export const checkFavoriteOrNot = async (params: { commonId: string | number }) => {
    return await post<number>('/mall-userms/user/favoritesGoods/check', params)
}

// 收藏/取消收藏商品（data === 1 为已收藏，否则为取消收藏）
export const addFavoriteGoods = async (params: { commonId: string; goodsId: string }) => {
    return await post<number>('/mall-userms/user/favoritesGoods/add', params)
}

// 取消收藏商品（参数/返回值与添加收藏一致；data === 1 为已收藏，否则为取消收藏）
export const deleteFavoriteGoods = async (params: { commonIds: Array<string | number> }) => {
    return await post<number>('/mall-userms/user/favoritesGoods/delete', params)
}

// 收藏店铺（code === 1 成功）
export const addFavoriteShop = async (params: AddFavoriteShopParams) => {
    return await post('/mall-userms/user/favoritesShop/add', params)
}

// 取消收藏店铺（POST，code === 1 成功）
export const deleteFavoriteShop = async (params: DeleteFavoriteShopParams) => {
    return await post('/mall-userms/user/favoritesShop/delete', params)
}

/**
 * 获取用户信息接口
 * 
 * 登录成功后调用，获取完整的用户信息
 * @param token 用户 token
 * @returns 用户信息
 */
export const getUserInfo = async (token: string): Promise<ApiResponse<UserInfo>> => {
    return await post<UserInfo>('/mall-userms/user/ms/getuser', {
        token
    })
}

/**
 * 查询用户会员信息（是否 PLUS、等级名称等）
 */
export const getUserMemberInformation = async (params: ApiParams = {}): Promise<ApiResponse<UserMemberInformationData>> => {
    return await post<UserMemberInformationData>('/mall-userms/user/ms/getuserMemberInformation', params)
}

/**
 * 查询个人会员折扣（获取 memberClassId）
 */
export const queryPersonalDiscount = async (params: ApiParams = {}): Promise<ApiResponse<{ memberClassId?: number }>> => {
    return await post<{ memberClassId?: number }>('/mall-userms/user/ms/queryPersonalDiscount', params)
}

/**
 * 查询 PLUS 会员权益与套餐列表
 * @param params 可为空对象 {}
 */
export const selectPlusMember = async (params: ApiParams = {}): Promise<ApiResponse<PlusMemberPackageDatum[]>> => {
    return await post<PlusMemberPackageDatum[]>('/mall-userms/user/plusInfo/selectPlusMember', params, {
        skipToken: true
    })
}

/**
 * 查询 PLUS 充值记录（分页）
 * @param params 分页参数，如 { limit: 10 }
 */
export const getPlusInfoOrder = async (params: ApiParams = {}): Promise<ApiResponse<PlusInfoOrderPageData>> => {
    return await post<PlusInfoOrderPageData>('/mall-userms/user/plusInfoOrder/get', params)
}

/**
 * 获取用户默认地址接口
 * 
 * 获取用户的默认收货地址信息
 * @param params 请求参数，包含token（系统自动处理）
 * @returns 用户默认地址数据
 */
export const getDefaultAddresses = async (params?: ApiParams): Promise<ApiResponse<UserDefaultAddress>> => {
    return await post<UserDefaultAddress>('/web/user/address/detail', params || {})
}

/**
 * 获取地址列表接口
 * 
 * 获取用户的所有收货地址列表
 * @param params 请求参数，包含platForm
 * @returns 地址列表数据
 */
export const getAddresses = async (params: GetAddressesParams): Promise<ApiResponse<UserAddress[]>> => {
    return await post<UserAddress[]>('/web/user/address/list', params)
}

/**
 * 添加地址接口
 * 
 * 添加新的收货地址
 * @param params 请求参数
 * @returns 添加结果
 */
export const addAddress = async (params: AddAddressParams): Promise<ApiResponse<any>> => {
    return await post('/web/user/address/add', params)
}

/**
 * 编辑地址接口
 * 
 * 更新收货地址信息
 * @param params 请求参数
 * @returns 更新结果
 */
export const updateAddress = async (params: UpdateAddressParams): Promise<ApiResponse<any>> => {
    return await post('/web/user/address/edit', params)
}

/**
 * 删除地址接口
 * 
 * 删除指定的收货地址
 * @param params 请求参数，包含userAddressId
 * @returns 删除结果
 */
export const deleteAddress = async (params: DeleteAddressParams): Promise<ApiResponse<any>> => {
    return await post('/web/user/address/delete', params)
}

/**
 * 检查公司名称是否重复接口
 * 
 * 检查公司名称是否已被申请
 * @param params 请求参数，包含companyName
 * @returns 检查结果，data === 1 表示公司名称已被申请
 */
export const checkDuplicateCompanyName = async (params: { companyName: string }): Promise<ApiResponse<number>> => {
    return await post<number>('/mall-userms/user/businessInfo/checkCompanyName', params)
}

/**
 * 企业注册接口
 * 
 * 提交企业用户注册信息
 * @param params 请求参数
 * @param params.companyName 公司名称
 * @param params.taxId 税号
 * @param params.registeredAddress 注册地址
 * @param params.businessLicense 营业执照图片URL
 * @param params.platForm 平台，默认为"1"
 * @param params.userMobile 手机号
 * @param params.userEmail 邮箱（可选）
 * @param params.phoneCode 验证码
 * @param params.password 加密后的密码
 * @param params.company_register_agreement 是否同意企业注册协议，默认为"1"
 * @param params.interDialingCode 国际区号
 * @returns 注册结果
 */
export const insertBusinessInfo = async (params: BusinessRegistrationParams): Promise<ApiResponse<any>> => {
    return await post('/mall-userms//user/ms/business/register', {
        companyName: params.companyName,
        taxId: params.taxId,
        registeredAddress: params.registeredAddress,
        businessLicense: params.businessLicense,
        platForm: params.platForm || '1',
        userMobile: params.userMobile,
        userEmail: params.userEmail || '',
        phoneCode: params.phoneCode,
        password: params.password,
        company_register_agreement: params.company_register_agreement || '1',
        interDialingCode: params.interDialingCode
    })
}

/**
 * 个人用户注册接口
 * 
 * 提交个人用户注册信息
 * @param params 请求参数
 * @param params.phoneCode 验证码（手机号或邮箱验证码）
 * @param params.userMobile 手机号或邮箱地址（根据账号类型）
 * @param params.password 加密后的密码
 * @param params.platForm 平台，默认为"1"
 * @param params.interDialingCode 国际区号（手机号注册时必填，邮箱注册时可为空）
 * @returns 注册结果
 */
export const registerIndividual = async (params: IndividualRegistrationParams): Promise<ApiResponse<any>> => {
    return await post('/mall-userms/user/ms/register', {
        phoneCode: params.phoneCode,
        userMobile: params.userMobile,
        password: params.password,
        userId: '',
        shopId: '',
        platForm: params.platForm || '1',
        interDialingCode: params.interDialingCode || '+86'
    })
}

/**
 * 忘记密码接口
 * 
 * 重置用户密码
 * @param params 请求参数
 * @param params.userMobile 手机号或邮箱
 * @param params.phoneCode 验证码
 * @param params.password 加密后的密码
 * @param params.interDialingCode 国际区号
 * @returns 重置结果
 */
export const forgotPassword = async (params: ForgotPasswordParams): Promise<ApiResponse<any>> => {
    return await post('mall-userms/user/ms/forgotPassWord', {
        userMobile: params.userMobile,
        phoneCode: params.phoneCode,
        password: params.password,
        interDialingCode: params.interDialingCode
    })
}
