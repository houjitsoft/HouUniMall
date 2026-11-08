/**
 * 表单相关类型定义
 * 统一管理所有表单相关的类型
 */

/**
 * 账号类型
 */
export type AccountType = 'phone' | 'email'

/**
 * 用户类型
 */
export type UserType = 'enterprise' | 'individual'

/**
 * 地区代码信息
 */
export interface CountryInfo {
    code: string
    name: string
    abbreviation?: string
    countryId?: number
    pinyin?: string
}

/**
 * 手机号/邮箱验证表单数据
 */
export interface PhoneEmailFormData {
    accountType: AccountType
    phone: string
    verificationCode: string
    email: string
    emailCode: string
    password: string
}

/**
 * 企业信息表单数据
 */
export interface CompanyFormData {
    companyName: string
    taxId: string
    registrationAddress: string
    businessLicense: string
}

/**
 * 注册审核信息表单数据
 */
export interface ReviewFormData {
    companyName?: string
    taxId?: string
    registrationAddress?: string
    phone: string
    email: string
}

/**
 * 注册表单完整数据
 */
export interface RegistrationFormData {
    userType: UserType
    accountType: AccountType
    agreedToTerms: boolean
    companyName: string
    taxId: string
    registrationAddress: string
    businessLicense: string
    phone: string
    password: string
    verificationCode: string
    email: string
    emailCode: string
}
