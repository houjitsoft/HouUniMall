package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * MARKETING（域 45）营销错误码，号段 45001-45999。
 *
 * <p>解决的问题：优惠券 / 红包 / 代金券 / 购物卡 / 会员权益 / 微客分销的状态判断散落在
 * {@code BizCodeEnume} 的 16xxx、18xxx 段里，同一个 18006 既是「优惠券已使用」又是
 * 「活动已达购买上限」，只能靠文案区分。本域把营销状态按语义登记成独立号段。
 *
 * <p>码位约定：45001-45030 由「第二阶段：旧枚举改为引用统一码」登记，来自
 * {@code BizCodeEnume} 活项认领；45100 之后留给后续子模块（秒杀、拼团、直播）。
 *
 * @author Amos
 * @since v1.3.0
 */
public enum MarketingErrorCode implements ErrorCodeDef {
    /**
     * 优惠券不存在或已被删除。
     *
     * <p>触发/排查：按券码/券ID查不到优惠券。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_COUPON_NOT_FOUND(45001, ErrorMeta.builder(45001, "优惠券不存在或已被删除")
            .devMessage("按券码/券ID查不到优惠券")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.3.0")
            .build()),

    /**
     * 优惠券已被使用，请重新选择。
     *
     * <p>触发/排查：优惠券已被核销。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_COUPON_USED(45002, ErrorMeta.builder(45002, "优惠券已被使用，请重新选择")
            .devMessage("优惠券已被核销")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18006)
            .since("v1.3.0")
            .build()),

    /**
     * 优惠券已过期，请重新选择。
     *
     * <p>触发/排查：优惠券超出有效期。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_COUPON_EXPIRED(45003, ErrorMeta.builder(45003, "优惠券已过期，请重新选择")
            .devMessage("优惠券超出有效期")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16065)
            .since("v1.3.0")
            .build()),

    /**
     * 优惠券已被收回，请重新选择。
     *
     * <p>触发/排查：优惠券被平台回收。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_COUPON_RECYCLED(45004, ErrorMeta.builder(45004, "优惠券已被收回，请重新选择")
            .devMessage("优惠券被平台回收")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16066)
            .since("v1.3.0")
            .build()),

    /**
     * 红包已被使用，请重新选择。
     *
     * <p>触发/排查：红包已被核销。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_RED_ENVELOPE_USED(45005, ErrorMeta.builder(45005, "红包已被使用，请重新选择")
            .devMessage("红包已被核销")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16037)
            .since("v1.3.0")
            .build()),

    /**
     * 红包已过期，请重新选择。
     *
     * <p>触发/排查：红包超出有效期。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_RED_ENVELOPE_EXPIRED(45006, ErrorMeta.builder(45006, "红包已过期，请重新选择")
            .devMessage("红包超出有效期")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16064)
            .since("v1.3.0")
            .build()),

    /**
     * 暂无可用红包。
     *
     * <p>触发/排查：当前无可领取/可用红包。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_RED_ENVELOPE_NONE(45007, ErrorMeta.builder(45007, "暂无可用红包")
            .devMessage("当前无可领取/可用红包")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .legacyCodes(11034)
            .since("v1.3.0")
            .build()),

    /**
     * 赠送数量超出红包总数。
     *
     * <p>触发/排查：发放数量大于红包库存。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_RED_ENVELOPE_QUANTITY_EXCEEDED(45008, ErrorMeta.builder(45008, "赠送数量超出红包总数")
            .devMessage("发放数量大于红包库存")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18012)
            .since("v1.3.0")
            .build()),

    /**
     * 订单限额不可小于红包面额。
     *
     * <p>触发/排查：使用门槛金额配置非法。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_RED_ENVELOPE_LIMIT_INVALID(45009, ErrorMeta.builder(45009, "订单限额不可小于红包面额")
            .devMessage("使用门槛金额配置非法")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(18013)
            .since("v1.3.0")
            .build()),

    /**
     * 没有查询到可用的代金券。
     *
     * <p>触发/排查：按条件查不到可用代金券。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VOUCHER_NOT_FOUND(45010, ErrorMeta.builder(45010, "没有查询到可用的代金券")
            .devMessage("按条件查不到可用代金券")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .legacyCodes(16003, 18001)
            .since("v1.3.0")
            .build()),

    /**
     * 订单金额小于代金券面额，不可使用。
     *
     * <p>触发/排查：订单金额不足券面额。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VOUCHER_AMOUNT_INVALID(45011, ErrorMeta.builder(45011, "订单金额小于代金券面额，不可使用")
            .devMessage("订单金额不足券面额")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18002)
            .since("v1.3.0")
            .build()),

    /**
     * 当前商品不适用该兑换券。
     *
     * <p>触发/排查：商品不在券适用范围。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VOUCHER_GOODS_NOT_APPLICABLE(45012, ErrorMeta.builder(45012, "当前商品不适用该兑换券")
            .devMessage("商品不在券适用范围")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18017)
            .since("v1.3.0")
            .build()),

    /**
     * 兑换券已过期或已无可用。
     *
     * <p>触发/排查：用户持有的兑换券不可用。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VOUCHER_EXPIRED(45013, ErrorMeta.builder(45013, "兑换券已过期或已无可用")
            .devMessage("用户持有的兑换券不可用")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18018)
            .since("v1.3.0")
            .build()),

    /**
     * 当前兑换券活动已失效。
     *
     * <p>触发/排查：券所属活动状态异常。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VOUCHER_ACTIVITY_INVALID(45014, ErrorMeta.builder(45014, "当前兑换券活动已失效")
            .devMessage("券所属活动状态异常")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18016)
            .since("v1.3.0")
            .build()),

    /**
     * 活动未开始或已结束。
     *
     * <p>触发/排查：活动不在进行中的时间窗。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_ACTIVITY_NOT_ACTIVE(45015, ErrorMeta.builder(45015, "活动未开始或已结束")
            .devMessage("活动不在进行中的时间窗")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18006)
            .since("v1.3.0")
            .build()),

    /**
     * 该商品已达购买上限。
     *
     * <p>触发/排查：活动限购数量已用完。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_ACTIVITY_LIMIT_REACHED(45016, ErrorMeta.builder(45016, "该商品已达购买上限")
            .devMessage("活动限购数量已用完")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18006)
            .since("v1.3.0")
            .build()),

    /**
     * 请先选择参与活动的商品。
     *
     * <p>触发/排查：活动未选择任何商品。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_GOODS_NOT_SELECTED(45017, ErrorMeta.builder(45017, "请先选择参与活动的商品")
            .devMessage("活动未选择任何商品")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(18004)
            .since("v1.3.0")
            .build()),

    /**
     * 购物卡还未生效。
     *
     * <p>触发/排查：购物卡未到生效时间。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_CARD_NOT_STARTED(45018, ErrorMeta.builder(45018, "购物卡还未生效")
            .devMessage("购物卡未到生效时间")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18013)
            .since("v1.3.0")
            .build()),

    /**
     * 购物卡已失效。
     *
     * <p>触发/排查：购物卡已过有效期。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_CARD_EXPIRED(45019, ErrorMeta.builder(45019, "购物卡已失效")
            .devMessage("购物卡已过有效期")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18014)
            .since("v1.3.0")
            .build()),

    /**
     * 卡片数量需大于0。
     *
     * <p>触发/排查：制卡数量为非法值。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_CARD_QUANTITY_INVALID(45020, ErrorMeta.builder(45020, "卡片数量需大于0")
            .devMessage("制卡数量为非法值")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(18014)
            .since("v1.3.0")
            .build()),

    /**
     * 未开通会员或会员已过期。
     *
     * <p>触发/排查：付费会员权益已到期。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_MEMBER_EXPIRED(45021, ErrorMeta.builder(45021, "未开通会员或会员已过期")
            .devMessage("付费会员权益已到期")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18007)
            .since("v1.3.0")
            .build()),

    /**
     * 该权益已绑定套餐，请先解除后再删除。
     *
     * <p>触发/排查：删除权益时仍有套餐绑定。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_MEMBER_RIGHT_IN_USE_DELETE(45022, ErrorMeta.builder(45022, "该权益已绑定套餐，请先解除后再删除")
            .devMessage("删除权益时仍有套餐绑定")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17047)
            .since("v1.3.0")
            .build()),

    /**
     * 该权益已绑定套餐，无法禁用。
     *
     * <p>触发/排查：禁用权益时仍有套餐绑定。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_MEMBER_RIGHT_IN_USE_DISABLE(45023, ErrorMeta.builder(45023, "该权益已绑定套餐，无法禁用")
            .devMessage("禁用权益时仍有套餐绑定")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17048)
            .since("v1.3.0")
            .build()),

    /**
     * 微客推广已关闭。
     *
     * <p>触发/排查：微客推广功能未开启。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VK_CLOSED(45024, ErrorMeta.builder(45024, "微客推广已关闭")
            .devMessage("微客推广功能未开启")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17045)
            .since("v1.3.0")
            .build()),

    /**
     * 该用户已是微客。
     *
     * <p>触发/排查：重复申请成为微客。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VK_ALREADY(45025, ErrorMeta.builder(45025, "该用户已是微客")
            .devMessage("重复申请成为微客")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17046)
            .since("v1.3.0")
            .build()),

    /**
     * 分享人不是微客。
     *
     * <p>触发/排查：推荐人不具备微客身份。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VK_INVITER_NOT_VK(45026, ErrorMeta.builder(45026, "分享人不是微客")
            .devMessage("推荐人不具备微客身份")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17058)
            .since("v1.3.0")
            .build()),

    /**
     * 已存在邀请记录。
     *
     * <p>触发/排查：重复建立邀请关系。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VK_INVITE_RECORD_EXISTED(45027, ErrorMeta.builder(45027, "已存在邀请记录")
            .devMessage("重复建立邀请关系")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17058)
            .since("v1.3.0")
            .build()),

    /**
     * 用户不是微客。
     *
     * <p>触发/排查：被邀请人不是微客身份。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VK_USER_NOT_VK(45028, ErrorMeta.builder(45028, "用户不是微客")
            .devMessage("被邀请人不是微客身份")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17058)
            .since("v1.3.0")
            .build()),

    /**
     * 新邀请人不能与原来的相同。
     *
     * <p>触发/排查：重置邀请人与原邀请人一致。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VK_INVITER_DUPLICATE(45029, ErrorMeta.builder(45029, "新邀请人不能与原来的相同")
            .devMessage("重置邀请人与原邀请人一致")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17056)
            .since("v1.3.0")
            .build()),

    /**
     * 请输入微客身份的手机号。
     *
     * <p>触发/排查：手机号对应的账号不是微客。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    MARKETING_VK_PHONE_NOT_VK(45030, ErrorMeta.builder(45030, "请输入微客身份的手机号")
            .devMessage("手机号对应的账号不是微客")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17057)
            .since("v1.3.0")
            .build()),


    MARKETING_GIFT_GOODS_DISABLED(45801, ErrorMeta.builder(45801, "未开启购买礼包商品")
            .devMessage("微客设置里未开启「购买礼包商品」开关")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(17055)
            .build()),
    MARKETING_CARD_INVALID(45802, ErrorMeta.builder(45802, "请输入正确的购物卡")
            .devMessage("购物卡卡号为空或不存在")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(18017)
            .build()),
    ;

    private final int code;
    private final ErrorMeta meta;

    MarketingErrorCode(int code, ErrorMeta meta) {
        this.code = code;
        this.meta = meta;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public ErrorMeta meta() {
        return meta;
    }
}
