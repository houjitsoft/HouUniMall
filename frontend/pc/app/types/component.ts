/**
 * 组件相关类型定义
 * 统一管理所有组件的 Props 和 Emits 类型
 */

import type { GdsCommon, GoodsBases, GoodsImage,ShopDetail } from './goods'
import type { CompanyFormData, PhoneEmailFormData, ReviewFormData, UserType, CountryInfo } from './form'
import type { UserShoppingCartDTO } from './order'
import type { CategoryTab, HomeProduct, Cat } from './home'
import type { ShopDTO } from './business'

/**
 * 密码输入组件 Props
 */
export interface PasswordInputProps {
    modelValue: string
    /** 字段标签文本，默认为"密码" */
    fieldLabel?: string
}

/**
 * 企业信息表单组件 Props
 */
export interface CompanyInfoFormProps {
    formData: CompanyFormData
}

/**
 * 手机号/邮箱验证组件 Props
 */
export interface PhoneEmailVerificationProps {
    formData: PhoneEmailFormData
    selectedCountry: CountryInfo
    countdown: number
    emailCountdown: number
    /** 按钮配置：用于区分企业注册（下一步）和个人注册（立即注册） */
    submitButtonText?: string // 默认 "下一步"
    submitButtonAction?: 'next' | 'submit' // 默认 'next'
    isSubmitting?: boolean // 提交状态，用于个人注册
}

/**
 * 审核信息组件 Props
 */
export interface ReviewInfoProps {
    userType: UserType
    formData: ReviewFormData
    selectedCountry: CountryInfo
    isSubmitting: boolean
}

/**
 * 步骤指示器步骤类型
 */
export interface Step {
    stepNumber: number
    label: string
}

/**
 * 步骤指示器组件 Props
 */
export interface StepIndicatorProps {
    currentStep: number
    steps: Step[]
}

/**
 * 用户类型选择组件 Props
 */
export interface UserTypeSelectionProps {
    modelValue: 'enterprise' | 'individual' | ''
    agreedToTerms: boolean
    showEnterprise: boolean
    showIndividual: boolean
}

/**
 * 图片上传组件 Props
 */
export interface UploadImagesProps {
    modelValue: string
    accept?: string
    placeholder?: string
    hint?: string
    alt?: string
    maxSize?: number // 最大文件大小（字节），默认5MB
    streamName?: string // OSS上传时的文件流名称
    /** 图片压缩质量，范围 0~1，默认 0.7 */
    compressQuality?: number
}

/**
 * 确认弹窗组件 Props
 */
export interface ConfirmModalProps {
    visible: boolean
    message?: string
    title?: string
    confirmText?: string
    cancelText?: string
    keepShoppingText?: string // 继续购物按钮文字
    goNowText?: string // 查看按钮文字
    mode?: 'confirm' | 'shopping' // 模式：confirm 为通用确认对话框，shopping 为购物车场景
}

/**
 * 确认弹窗组件 Emits
 */
export interface ConfirmModalEmits {
    (e: 'close'): void
    (e: 'keepShopping'): void
    (e: 'goNow'): void
    (e: 'confirm'): void
    (e: 'cancel'): void
}

/**
 * 商品信息组件 Props
 */
export interface ProductInformationProps {
    goodsBases?: GoodsBases
    gdsCommon?: GdsCommon
    shopDetail?: ShopDetail
    goodsImages?: GoodsImage[]
}

/**
 * 购物车商品类型（扩展 UserShoppingCartDTO）
 */
export interface CartGoods extends UserShoppingCartDTO {
    checked?: boolean
    invalid?: boolean
    oldGoodsNum?: number
    goodsTotalPrice?: number
    hyPrice?: number // 价格
    taxIncludedPrice?: number // 原价
    subTotalFromResult?: number // 小计
    commonIsXian?: number // 活动标签类型（stepOrHyList）
}

/**
 * 购物车店铺项类型
 */
export interface CartShopItem {
    shopId: number
    shopName: string
    checked: boolean
    goodsList: CartGoods[]
    /** 店铺商品总价（前端计算用） */
    shopTotalPrice?: number
}

/**
 * 购物车商品列表组件 Props
 */
export interface GoodsListProps {
    shopList: CartShopItem[]
    allChecked?: boolean
    isInquiryBasket?: boolean
    priceResult?: ShopDTO | null
    /** 是否展示店铺头部（店铺名称/店铺勾选） */
    showShopHeader?: boolean
    /** 是否展示复选框列（店铺/商品勾选） */
    showCheckboxes?: boolean
}

/**
 * Tab项类型（扩展 CategoryTab）
 */
export interface TabItem extends CategoryTab {
    pageId: number
    pageTitle: string
    pageTwoTitle: string
    goods: HomeProduct[]
}

/**
 * 精选推荐盒子数据项类型
 */
export interface FeaturedPavilionDataItem {
    cat: Cat
    pageName: string
}

/**
 * 商城 Store 类型（用于组件 Props）
 */
export interface MallStore {
    getConfigValueByKey: (key: string) => string | undefined
}

/**
 * 商品信息自定义类型
 */
export interface ProductCustomType {
    deliveryTime?: string
    minNumber: number
    maxNumber: number
}

/**
 * 文件上传项类型
 */
export interface FileItem {
    name: string
    url: string
    type: string
}

/**
 * 同行报价项类型
 */
export interface QuotationItem {
    shopName: string
    expectedUnitPrice: string | number
    quotationTime: string
    isRealNameAuthentication?: number
    isBusiness?: number
}
