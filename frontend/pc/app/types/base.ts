/**
 * 基础服务相关类型定义
 */

/**
 * 商户系统配置响应类型
 */
export interface MerchantSysConfigResponse {
    configValue: string
    configKey: string
    [key: string]: any
}

/**
 * 密码规则配置接口
 */
export interface PasswordConfig {
    /** 密码格式：1-纯数字;2-纯英文;3-数字英文;4-数字英文符号 */
    passwordFormat: string
    /** 最小长度 */
    passwordLength: string
    /** 密码规则描述 */
    desc: string
}

/**
 * 文章分类(base_article_group)实体类
 */
export interface BaseArticleGroupEntity {
    /**
     * 自增主键
     */
    articleGroupId?: number
    /**
     * 排序
     */
    articleGroupSort?: number
    /**
     * 标题
     */
    articleGroupTitle?: string
    [property: string]: any
}

/**
 * 文章(base_article_base)实体类
 */
export interface BaseArticleBaseEntity {
    /**
     * 添加时间
     */
    articleAddTime?: string
    /**
     * 描述-富文本
     */
    articleDesc?: string
    /**
     * 组
     */
    articleGroupId?: number
    /**
     * 组名称
     */
    articleGroupTitle?: string
    /**
     * 文章ID
     */
    articleId?: number
    /**
     * 排序
     */
    articleSort?: number
    /**
     * 状态 1:启用  2:关闭
     */
    articleStatus?: number
    /**
     * 标题
     */
    articleTitle?: string
    /**
     * 调用网址-url，默认为本页面构造的网址，可填写其它页面
     */
    articleUrl?: string
    [property: string]: any
}

/**
 * 底部帮助 / 文章页 getHelpInfo 接口数据
 */
export interface HelpInfoArticleGroup extends BaseArticleGroupEntity {
    articletitle?: null
    articletitle2?: BaseArticleBaseEntity[]
}

export interface HelpInfoData {
    articleGroups?: HelpInfoArticleGroup[]
    webColor?: string
}

/**
 * 资讯轮播图配置项
 */
export interface InformationSliderConfig {
    /**
     * 备注
     */
    configComment: string
    configEnable: number
    /**
     * 图片key  唯一
     */
    configKey: string
    configSort: number
    /**
     * 数据类型
     */
    configType: string
    /**
     * 图片url
     */
    configValue: string
    createBy: number
    createDate: string
    opType: string
    opValue: string
    terminalType: number
    updateBy: number
    updateDate: string
    [property: string]: any
}

/**
 * 资讯分类子菜单项
 */
export interface InformationClassifyChildMenu {
    childMenu: null
    /**
     * 分类id
     */
    classifyId: number
    /**
     * 等级
     */
    classifyLevel: number
    /**
     * 分类名称
     */
    classifyName: string
    /**
     * 上级id
     */
    classifyParentId: number
    /**
     * 显示 1 是 0否
     */
    classifyShowStatus: number
    /**
     * 排序
     */
    classifySort: number
    [property: string]: any
}

/**
 * 资讯分类项
 */
export interface InformationClassifyItem {
    /**
     * 子级
     */
    childMenu: InformationClassifyChildMenu[]
    /**
     * 分类id
     */
    classifyId: number
    /**
     * 分类等级
     */
    classifyLevel: number
    /**
     * 分类名称
     */
    classifyName: string
    /**
     * 上级id
     */
    classifyParentId: number
    /**
     * 显示 1 是 0否
     */
    classifyShowStatus: number
    /**
     * 排序
     */
    classifySort: number
    [property: string]: any
}

/**
 * 资讯列表项
 */
export interface InformationListItem {
    /**
     * 分类id
     */
    classifyId: null
    classifyName: null
    classifyParentId: null
    createTime: null
    /**
     * 咨询封面
     */
    informationCover: string
    /**
     * 咨询内容
     */
    informationDesc: string
    /**
     * 咨询id
     */
    informationId: number
    /**
     * 资讯状态 0已暂存 1已发布 2已下架
     */
    informationStatus: number
    /**
     * 咨询标题
     */
    informationTitle: string
    /**
     * 是否推荐 0不推荐 1推荐
     */
    isRecommend: number
    /**
     * 浏览量
     */
    pageView: number
    pageViewNatural: null
    pageViewSet: null
    releaseSource: null
    /**
     * 发布时间
     */
    releaseTime: null | string
    updateTime: null
    [property: string]: any
}

/**
 * 资讯列表分页数据
 */
export interface InformationListData {
    currPage: number
    list: InformationListItem[]
    pageSize: number
    totalCount: number
    totalPage: number
    [property: string]: any
}

/**
 * 资讯详情数据
 */
export interface InformationDetailData {
    classifyId: number
    classifyName: null
    classifyParentId: null
    createTime: string
    /**
     * 咨询封面
     */
    informationCover: string
    /**
     * 咨询内容
     */
    informationDesc: string
    /**
     * 咨询id
     */
    informationId: number
    /**
     * 资讯状态 0已暂存 1已发布 2已下架
     */
    informationStatus: number
    /**
     * 咨询标题
     */
    informationTitle: string
    /**
     * 是否推荐 0不推荐 1推荐
     */
    isRecommend: number
    /**
     * 浏览量
     */
    pageView: number
    pageViewNatural: number
    pageViewSet: number
    releaseSource: string
    /**
     * 发布时间
     */
    releaseTime: string
    updateTime: string
    [property: string]: any
}

/**
 * 上一篇资讯
 */
export interface PreviousArticle {
    classifyId: number
    classifyName: null
    classifyParentId: null
    createTime: null
    informationCover: string
    informationCoverList: string[]
    informationDesc: string
    informationId: number
    informationStatus: number
    informationTitle: string
    isRecommend: number
    pageView: number
    pageViewNatural: null
    pageViewSet: null
    releaseSource: null
    releaseTime: string
    updateTime: null
    [property: string]: any
}

/**
 * 下一篇资讯
 */
export interface NextArticle {
    classifyId: number
    classifyName: null
    classifyParentId: null
    createTime: null
    informationCover: string
    informationCoverList: string[]
    informationDesc: string
    informationId: number
    informationStatus: number
    informationTitle: string
    isRecommend: number
    pageView: number
    pageViewNatural: null
    pageViewSet: null
    releaseSource: null
    releaseTime: null
    updateTime: null
    [property: string]: any
}

/**
 * 底部推荐资讯
 */
export interface BottomArticle {
    classifyId: number
    classifyName: null
    classifyParentId: null
    createTime: null
    informationCover: string
    informationCoverList: string[]
    informationDesc: string
    informationId: number
    informationStatus: number
    informationTitle: string
    isRecommend: number
    pageView: number
    pageViewNatural: null
    pageViewSet: null
    releaseSource: null
    releaseTime: string
    updateTime: null
    [property: string]: any
}

/**
 * 上一篇下一篇推荐数据
 */
export interface NextChapterData {
    /**
     * 底部推荐
     */
    bottom: BottomArticle[]
    /**
     * 下一篇
     */
    next: NextArticle
    /**
     * 上一篇
     */
    previous: PreviousArticle
    [property: string]: any
}

/**
 * 积分商城配置项
 */
export interface IntegralConfigItem {
    /** 配置键 */
    configKey: string
    /** 配置值 */
    configValue: string
    /** 轮播图列表 */
    slideList: any
    /** 操作类型 */
    opType: string
    /** 操作值 */
    opValue: string
    /** 配置备注 */
    configComment: string
    /** 通用名称 */
    commonName: any
    [property: string]: any
}

/**
 * 积分商城首页数据
 */
export interface IntegralIndexData {
    /** 结果映射 */
    resultmap?: Record<string, any>
    /** 配置列表 */
    list?: IntegralConfigItem[]
}

/**
 * 积分商城首页 resultmap 结构
 */
export interface IntegralResultMap {
    /** 最高等级时返回 code */
    code?: number
    /** 用户会员等级名称 */
    memberName?: string
    /** 用户会员等级下一级名称 */
    memberNameNext?: string
    /** 距离下次升级还需多少积分 */
    distanceIntegral?: number
    /** 会员折扣 */
    gradeDiscount?: string
    /** 是否开通 plus 会员 */
    isplusMember?: number
    /** 用户姓名 */
    userName?: string
    /** 用户头像 */
    userAvatar?: string
    /** 当前会员积分 */
    memberIntegral?: number | string
    /** 下一级所需积分 */
    nextGradeRule?: number
    /** 会员等级图标/链接（后端字段名 memberUrl） */
    memberUrl?: string
    /** plus 会员套餐类型 */
    thaliType?: number
    /** 冻结积分 */
    frozenIntegra?: number
    /** 已兑换礼品数量 */
    orderCount?: number | string
}

/**
 * 客服配置接口返回类型
 */
export interface DetectingImStatusData {
    configEnable?: number
    configValue?: string
    configValue2?: string
}

/**
 * 银行卡信息实体
 */
export interface BankCardInfo {
    /** 主键ID */
    id?: number
    /** 银行卡卡号 */
    bankCardId?: string
    /** 开户人姓名 */
    bankCardName?: string
    /** 开户银行名称 */
    bankName?: string
    /** 备注 */
    brankNote?: string
    /** 修改时间 yyyy-MM-dd HH:mm:ss */
    brankUpdateTime?: string
    /** 银联号 */
    brankUnionpayGood?: string
    [property: string]: any
}

/** 用户会员信息（含 PLUS） */
export interface UserMemberInformationData {
    /** 是否开通 PLUS：1 是 0 否 */
    isplusMember?: number | string
    /** 会员等级名称，如钻石、青铜 */
    memberName?: string
}
