/**
 * 用户信息类型定义
 * 根据登录接口返回的数据结构定义
 */

// 企业详情类型
export interface BusinessDetail {
    businessId: number
    taxId: string
    companyName: string
    companyAddress: string
    businessLicense: string
    bankCardNo: string
    registerBank: string
    contactName: string
    contactNumber: string
    companyProvinceId: string
    companyCityId: string
    companyDistrictId: string
    companyDetailAddress: string
    mailAddress: string
    userId: string
    createTime: string
    lastUpdateTime: string
    isApproved: number
    auditComment: string
    businessCategory: boolean
    productionLicense: string
    registeredAddress: string
    approvalProcessStatus: number
    isRegister: number
    information: string
    notes: string
}

// 用户信息类型
export interface UserInfo {
    userId: string
    userName: string
    password: string
    internationalDialingCode: string
    code: string
    sign: string
    userState: number
    sessionId: string
    userPlatform: number
    userMobile: string
    userLastloginTime: string
    token: string
    userAvatar: string
    userGender: number
    sellerId: number
    sellerName: string
    shopId: number
    shopType: number
    shopStatus: number
    sellerIsAdmin: number
    limits: string
    userIsNew: string
    userMobileVerify: number
    userNickname: string
    bindOpenid: string
    isBusiness: number
    isShop: number
    businessDetail: BusinessDetail
    businessId: number
    companyName: string
    isDelete: number
    isEmployee: number
    isRealNameAuthentication: number
    unreadMessageNum: number
    userLastloginIp: string
    createUser: string
    createTime: string
    updateUser: string
    updateTime: string
    loginPlatform: number
    userAccount: string
    userPassword: string
    userKey: string
    userDelete: number
    rightsGroupId: number
    provinceId: number
    cityId: number
    districtId: number
    address: string
    rightsGroupName: string
    rightsGroupRightsIds: string
    rightsGroupAddTime: string
    rightsLimitsAll: string
    rightsLimitsState: string
    userRealname: string
    userIntegral: number
    userBirth: string
    isSalesman: number
    shopGradeId: number
    isSalesmanAccount: number
    type: number
}

/**
 * 查询是否收藏店铺接口参数类型
 */
export interface CheckFavoriteShopParams {
    shopId: number
}

/**
 * 添加收藏店铺接口参数类型（/mall-userms/user/favoritesShop/add）
 */
export interface AddFavoriteShopParams {
    shopId: number
    shopName: string
    shopLogo: string
}

/**
 * 取消收藏店铺接口参数类型（/mall-userms/user/favoritesShop/delete）
 */
export interface DeleteFavoriteShopParams {
    shopIds: string[]
}

/**
 * 用户默认地址类型
 */
export interface UserDefaultAddress {
    userAddressId: number
    userId: string
    userAddressContact: string
    userAddressProvinceId: number
    userAddressCityId: number
    userAddressAreaId: number
    userAddressArea: string
    userAddressAddress: string
    userAddressPhone: string
    userAddressDefault: number
    userAddressTime: string
    ts: string
    internationalType: number
    countryId: number
    countryName: string
    internationalDialingCode: string
}

/**
 * 用户地址类型
 */
export interface UserAddress {
    userAddressId: number
    userId: string
    userAddressContact: string
    userAddressProvinceId: number
    userAddressCityId: number
    userAddressAreaId: number
    userAddressArea: string
    userAddressAddress: string
    userAddressPhone: string
    userAddressDefault: number
    userAddressTime: string
    ts: string
    internationalType: number
    countryId: number
    countryName: string
    internationalDialingCode: string
}

/**
 * 获取地址列表接口参数类型
 */
export interface GetAddressesParams {
    platForm: number
    /** 代客下单ID，携带时返回代客订单关联地址 */
    valetOrderId?: string
}

/**
 * 添加地址接口参数类型
 */
export interface AddAddressParams {
    userAddressContact: string
    userAddressAddress: string
    userAddressPhone: string
    internationalType: number
    internationalDialingCode: string
    countryName: string
    countryId: number
    userAddressDefault: number
    userAddressProvinceId: number
    userAddressCityId: number
    userAddressAreaId: number
    userAddressArea: string
}

/**
 * 编辑地址接口参数类型
 */
export interface UpdateAddressParams {
    userAddressId: number
    userAddressContact: string
    internationalType: number
    internationalDialingCode: string
    countryName: string
    countryId: number
    userAddressProvinceId: number
    userAddressCityId: number
    userAddressAreaId: number
    userAddressArea: string
    userAddressAddress: string
    userAddressPhone: string
    userAddressDefault: number
}

/**
 * 删除地址接口参数类型
 */
export interface DeleteAddressParams {
    userAddressId: number
}

/**
 * 忘记密码接口参数类型
 */
export interface ForgotPasswordParams {
    userMobile: string
    phoneCode: string
    password: string
    interDialingCode: string
}

/**
 * 企业注册接口参数类型
 */
export interface BusinessRegistrationParams {
    companyName: string
    taxId: string
    registeredAddress: string
    businessLicense: string
    platForm?: string
    userMobile: string
    userEmail?: string
    phoneCode: string
    password: string
    company_register_agreement?: string
    interDialingCode: string
}

/**
 * 个人用户注册接口参数类型
 */
export interface IndividualRegistrationParams {
    phoneCode: string
    userMobile: string
    password: string
    platForm?: string
    interDialingCode?: string
}
