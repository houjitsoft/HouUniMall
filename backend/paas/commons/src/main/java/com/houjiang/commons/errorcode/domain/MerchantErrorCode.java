package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * MERCHANT（域 33）商户与店铺错误码，号段 33001-33999。
 *
 * <p>解决的问题（team-lead 裁决 1、2）：cmsadmin 白名单里的两个码落在本域 ——
 * 旧码 10047「没有审核信息」→ {@link #MERCHANT_AUDIT_INFO_NOT_FOUND}（33001），
 * 旧码 16012「查看合同模板失败」→ {@link #MERCHANT_CONTRACT_TEMPLATE_NOT_FOUND}（33002）。
 * 两者语义都是「查不到数据」，按 INFO + EMPTY_STATE <b>保留放行行为</b>（不弹 Toast），
 * 但从魔法数字白名单改为按新码显式声明。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum MerchantErrorCode implements ErrorCodeDef {

    /**
     * 没有审核信息（商户尚未提交入驻审核资料）。
     *
     * <p>触发：查询商户审核记录为空。用户看到空状态占位，<b>不弹 Toast</b>。
     *
     * <p>排查：旧码 10047 在 cmsadmin 被放进 VALID_CODES 白名单放行；
     * 本码继续按 EMPTY_STATE 放行，但改为显式声明，禁止再写魔法数字白名单。
     */
    MERCHANT_AUDIT_INFO_NOT_FOUND(33001, ErrorMeta.builder(33001, "还没有提交审核信息")
            .devMessage("商户审核记录查询为空")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .legacyCodes(10047)
            .since("v1.2.0")
            .remark("cmsadmin 白名单码 10047 的新归属；保持放行，改按新码显式声明")
            .build()),

    /**
     * 合同模板未找到。
     *
     * <p>触发：查看/签署合同时找不到模板。用户看到空状态占位，<b>不弹 Toast</b>。
     *
     * <p>排查：旧码 16012（NO_CONTRACT_FOUND）经全仓检索确认<b>只有枚举定义、零处引用，是死常量</b>，
     * 暂挂 MERCHANT 域；若确认为平台侧 OA 合同则改挂 OA 域 53（码值一经发布不可回收，
     * 上线前必须确认归属）。
     */
    MERCHANT_CONTRACT_TEMPLATE_NOT_FOUND(33002, ErrorMeta.builder(33002, "没有可用的合同模板")
            .devMessage("合同模板查询为空（旧码 16012 NO_CONTRACT_FOUND，定义未使用）")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .legacyCodes(16012)
            .since("v1.2.0")
            .remark("cmsadmin 白名单码 16012 的新归属；定义未使用，归属待业务确认")
            .build()),

    /**
     * 审核状态不允许（已通过的资料不允许再改）。
     *
     * <p>触发：对已进入终态的审核记录执行操作。用户看到「当前审核状态不允许该操作」。
     *
     * <p>排查：确认审核状态机流转是否合法。
     */
    MERCHANT_AUDIT_STATE_NOT_ALLOWED(33003, ErrorMeta.builder(33003, "当前审核状态不允许该操作")
            .devMessage("审核状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 商户不存在。
     *
     * <p>触发：按商户 ID 查不到记录。用户看到「商户不存在或已注销」。
     *
     * <p>排查：确认商户 ID 与 merchant 表。
     */
    MERCHANT_NOT_FOUND(33010, ErrorMeta.builder(33010, "商户不存在或已注销")
            .devMessage("按商户 ID 未查到商户记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 门店不存在。
     *
     * <p>触发：按门店 ID 查不到记录。用户看到「门店不存在或已关闭」。
     *
     * <p>排查：确认门店 ID 与归属商户是否匹配。
     */
    MERCHANT_STORE_NOT_FOUND(33011, ErrorMeta.builder(33011, "门店不存在或已关闭")
            .devMessage("按门店 ID 未查到门店记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 入驻申请重复提交。
     *
     * <p>触发：同一主体在审核中再次提交。用户看到「已提交过入驻申请，请耐心等待」。
     *
     * <p>排查：这是保护性拦截，引导用户等待审核结果。
     */
    MERCHANT_APPLY_DUPLICATE(33012, ErrorMeta.builder(33012, "已提交过入驻申请，请耐心等待")
            .devMessage("同一主体存在审核中的入驻申请")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 查询不到商户数据（筛选条件无命中）。
     *
     * <p>触发：商户/门店列表查询返回空。用户看到空状态占位，不弹 Toast。
     *
     * <p>排查：这是 INFO 级别，正常业务结果。
     */
    MERCHANT_QUERY_RESULT_EMPTY(33013, ErrorMeta.builder(33013, "没有找到符合条件的商户")
            .devMessage("商户列表查询无命中")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),
    /**
     * 请先选择店铺经营类目。
     *
     * <p>触发/排查：店铺缺少经营类目入参。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_BUSINESS_CATEGORY_REQUIRED(33020, ErrorMeta.builder(33020, "请先选择店铺经营类目")
            .devMessage("店铺缺少经营类目入参")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(16061)
            .since("v1.3.0")
            .build()),

    /**
     * 店铺经营类目不符，请调整。
     *
     * <p>触发/排查：所选类目与店铺资质不匹配。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_BUSINESS_CATEGORY_MISMATCH(33021, ErrorMeta.builder(33021, "店铺经营类目不符，请调整")
            .devMessage("所选类目与店铺资质不匹配")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16039, 16062)
            .since("v1.3.0")
            .build()),

    /**
     * 店铺还有未完成订单，暂时无法关闭。
     *
     * <p>触发/排查：关店校验发现未完成或售后期内订单。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_HAS_UNFINISHED_ORDER(33022, ErrorMeta.builder(33022, "店铺还有未完成订单，暂时无法关闭")
            .devMessage("关店校验发现未完成或售后期内订单")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16039)
            .since("v1.3.0")
            .build()),

    /**
     * 该用户已开通店铺或已是其它店铺子账号。
     *
     * <p>触发/排查：重复开店校验未通过。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_ALREADY_OPENED(33023, ErrorMeta.builder(33023, "该用户已开通店铺或已是其它店铺子账号")
            .devMessage("重复开店校验未通过")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16045)
            .since("v1.3.0")
            .build()),

    /**
     * 仓库中还有关联商品，无法操作。
     *
     * <p>触发/排查：删除仓库前存在关联商品。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORAGE_GOODS_LINKED(33025, ErrorMeta.builder(33025, "仓库中还有关联商品，无法操作")
            .devMessage("删除仓库前存在关联商品")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16053)
            .since("v1.3.0")
            .build()),

    /**
     * 仓库中没有该商品。
     *
     * <p>触发/排查：仓库内不存在指定商品。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORAGE_GOODS_NOT_FOUND(33026, ErrorMeta.builder(33026, "仓库中没有该商品")
            .devMessage("仓库内不存在指定商品")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(16054)
            .since("v1.3.0")
            .build()),

    /**
     * 入驻信息不完整，请重新提交。
     *
     * <p>触发/排查：入驻申请必填字段缺失。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_JOIN_INFO_REQUIRED(33027, ErrorMeta.builder(33027, "入驻信息不完整，请重新提交")
            .devMessage("入驻申请必填字段缺失")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(16063)
            .since("v1.3.0")
            .build()),

    /**
     * 请填写仓储信息。
     *
     * <p>触发/排查：商品/店铺缺少仓储配置。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_STORAGE_REQUIRED(33028, ErrorMeta.builder(33028, "请填写仓储信息")
            .devMessage("商品/店铺缺少仓储配置")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(16067)
            .since("v1.3.0")
            .build()),

    /**
     * 请使用商家账号操作。
     *
     * <p>触发/排查：非商家账号访问商家功能。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_LOGIN_ONLY(33029, ErrorMeta.builder(33029, "请使用商家账号操作")
            .devMessage("非商家账号访问商家功能")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .legacyCodes(10095, 17054)
            .since("v1.3.0")
            .build()),

    /**
     * 该手机号已注册门店。
     *
     * <p>触发/排查：手机号已被门店占用。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_PHONE_ALREADY_REGISTERED(33030, ErrorMeta.builder(33030, "该手机号已注册门店")
            .devMessage("手机号已被门店占用")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17034)
            .since("v1.3.0")
            .build()),

    /**
     * 该店铺已授权小程序。
     *
     * <p>触发/排查：重复发起小程序授权。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_MINIAPP_AUTHORIZED(33031, ErrorMeta.builder(33031, "该店铺已授权小程序")
            .devMessage("重复发起小程序授权")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18015)
            .since("v1.3.0")
            .build()),

    /**
     * 该店铺尚未授权小程序。
     *
     * <p>触发/排查：未授权小程序即调用相关能力。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_MINIAPP_NOT_AUTHORIZED(33032, ErrorMeta.builder(33032, "该店铺尚未授权小程序")
            .devMessage("未授权小程序即调用相关能力")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(18016)
            .since("v1.3.0")
            .build()),

    /**
     * 店铺已关闭，请联系平台。
     *
     * <p>触发/排查：店铺处于关闭状态。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_CLOSED(33033, ErrorMeta.builder(33033, "店铺已关闭，请联系平台")
            .devMessage("店铺处于关闭状态")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16042)
            .since("v1.3.0")
            .build()),

    /**
     * 该用户尚未开通店铺。
     *
     * <p>触发/排查：用户没有已开通的店铺。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_NOT_OPENED(33034, ErrorMeta.builder(33034, "该用户尚未开通店铺")
            .devMessage("用户没有已开通的店铺")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(16001, 16044)
            .since("v1.3.0")
            .build()),

    /**
     * 开通店铺失败，请稍后重试。
     *
     * <p>触发/排查：开店流程执行失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    STORE_OPEN_FAILED(33035, ErrorMeta.builder(33035, "开通店铺失败，请稍后重试")
            .devMessage("开店流程执行失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(11021)
            .since("v1.3.0")
            .build()),


    MERCHANT_NOT_BUSINESS_USER(33801, ErrorMeta.builder(33801, "您不是商家用户，无法操作")
            .devMessage("当前账号未绑定商家主体")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(17038)
            .build()),
    MERCHANT_NOT_DISTRIBUTOR(33802, ErrorMeta.builder(33802, "未申请经销商")
            .devMessage("当前账号未提交经销商申请或申请未通过")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(17039)
            .build()),
    ;

    private final int code;
    private final ErrorMeta meta;

    MerchantErrorCode(int code, ErrorMeta meta) {
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
