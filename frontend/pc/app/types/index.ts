/**
 * 类型定义统一导出
 * 方便统一导入使用
 */

// API 相关类型
export type { ApiResponse, ApiParams, EmptyRequestParams } from './api'

// 商品相关类型
export type {
    GoodsList,
    ShopList,
    GoodsBases,
    GoodsImage,
    GdsCommon,
    GoodsDetailData,
    ShopDetail,
    SelectGoodsSkuParams,
    GoodsSkuItem,
    SelectGoodsSkuResponse,
    CommonDetailDesc,
    RecommendGoodsParams,
    AddGoodsContrastParams,
    DeleteGoodsContrastParams,
    SelectGoodsContrastParams,
    GoodsContrastItem,
    QueryTransportAreaParams,
    ProductViewMode,
    ProductOrderType,
    ProductOrderDirection,
    ProductSearchParams,
    MatchedGoodsItem,
    MatchedGoodsResponse,
    GoodsFilterQueryItem,
    GoodsFilterQueryParams,
    GoodsFilterQueryData,
    VoucherTemplateVO,
    ArticleVO,
    GoodsCatResultVO,
    GoodsBrandVO,
    GoodsAttributeGroupVO,
    GoodsCategoryItem,
    SearchVO
} from './goods'

// 首页相关类型
export type {
    BannerItem,
    NewsItem,
    SectionItem,
    SubCategorySection,
    SubCategoryResponse,
    CategoryItem,
    QuickLinkItem,
    HomeProduct,
    CategoryTab,
    CategoryDataItem,
    FloorNavItem,
    FloorItem,
    Cat
} from './home'

// 商城相关类型
export type { NavLogoConfig, ExhibitionSlideItem, AllHallItem, RecommendHall, HallDetailData, ExhibitionIndexData } from './mall'

// Store 状态相关类型
export type {
    UserState,
    GoodsState,
    MallState,
    ArticleState,
    ComparisonItem,
    ComparisonState,
    ShopListRequest,
    ShopListResponse,
    WebShopListVO,
    ListQueryResultVO,
    ShopEvaluationVO,
    ShopPercentVO,
    ShopHeaderInfo,
    WebShopIndexVO,
    WebShopBaseIndexVO,
    ShopSlideUrlVO,
    CommonAndGoodsVO,
    ManageShopSelfCatMenuVO,
    WebShopGoodsListDTO,
    WebShopGoodsListResponse,
    ShopQualificationsInfo,
    StoreType
} from './store'

// UI 组件相关类型
export type { ToastPosition, ToastType, ToastOptions, ThemeConfig, Toast } from './ui'

// 用户相关类型
export type {
    UserInfo,
    BusinessDetail,
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
} from './user'

// 支付相关类型
export type {
    PaymentMethod,
    BalanceDisplayParams,
    AccountBalanceDisplayVO,
    EvokePaymentParams,
    QueryPaymentAmountParams,
    QueryPaymentAmountResult,
    PaymentPageState,
    PasswordInputModalProps,
    PasswordInputModalEmits,
    PasswordSetModalProps,
    PasswordSetModalEmits
} from './payment'
export { PaymentType } from './payment'

// 业务相关类型
export type {
    QueryDeliveryTimeParams,
    CalculatePriceParams,
    MapString,
    MapObject,
    ManjianzengzheGoodsRuleVO,
    ActivityGoodsDetailDTO,
    ActivityInfo,
    WapShippingVO,
    ShippingListDTO,
    ShopDTO,
    CreateOrdersParams,
    InvoiceInfo,
    InvoiceAddress,
    InvoiceTemplate,
    InsertInvoiceInfoParams,
    InsertInvoiceAddressParams,
    ShopJoinAgreeData,
    VoucherBaseVO
} from './business'

// 基础服务相关类型
export type {
    MerchantSysConfigResponse,
    BaseArticleGroupEntity,
    BaseArticleBaseEntity,
    PasswordConfig,
    DetectingImStatusData,
    InformationSliderConfig,
    InformationClassifyItem,
    InformationClassifyChildMenu,
    InformationListItem,
    InformationListData,
    InformationDetailData,
    PreviousArticle,
    NextArticle,
    BottomArticle,
    NextChapterData,
    IntegralConfigItem,
    IntegralIndexData,
    BankCardInfo
} from './base'

// 表单相关类型
export type {
    AccountType,
    UserType,
    CountryInfo,
    PhoneEmailFormData,
    CompanyFormData,
    ReviewFormData,
    RegistrationFormData
} from './form'

// 订单相关类型
export type {
    CommentsEntity,
    PagedByComments,
    GetCommentsParams,
    InsertShoppingCartParams,
    QueryShoppingCartData,
    EntityObjectsDTO,
    UserShoppingCartDTO,
    InsertPaymentVoucherParams
} from './order'

// AI 相关类型
export type {
    AiConversationItem,
    AiConversationListData,
    AiMessageHistoryItem,
    AiMessageHistoryListData,
    AiGoodsSearchItem,
    AiGoodsSearchData
} from './ai'

// 活动相关类型
export type {
    CombinationGoodsDTO,
    CombinationPcVO,
    CombinationBaseVO,
    ManJianZengZheProduct,
    DiscountProduct,
    SeckillProduct,
    TimeSlot,
    SortType
} from './activity'

// 组件相关类型
export type {
    PasswordInputProps,
    CompanyInfoFormProps,
    PhoneEmailVerificationProps,
    ReviewInfoProps,
    Step,
    StepIndicatorProps,
    UserTypeSelectionProps,
    UploadImagesProps,
    ConfirmModalProps,
    ConfirmModalEmits,
    ProductInformationProps,
    CartGoods,
    CartShopItem,
    GoodsListProps,
    TabItem,
    FeaturedPavilionDataItem,
    MallStore,
    ProductCustomType,
    FileItem,
    QuotationItem
} from './component'

// Composables 相关类型
export type {
    UseAreaCodeReturn,
    CountdownOptions,
    UseCountdownReturn,
    ConfirmModalOptions,
    DropdownState,
    DebounceTimer
} from './composable'

// 地址相关类型
export type {
    DistrictItem,
    Area,
    CountryEntity
} from './address'

// 工具类相关类型
export type { CookieOptions } from './utils'
export { PasswordFormat, PasswordStrength } from './utils'
