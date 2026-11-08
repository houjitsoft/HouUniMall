package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * RIDER（域 35）骑手与配送错误码，号段 35001-35999。
 *
 * <p>解决的问题（PRD 痛点 1）：RiderCodeEnume 与 BizCodeEnume 共用了 11001-11009 这一段，
 * 同一个数字在两端语义完全不同（骑手输错验证码会被 admin 端当成越权拦截、
 * 提现填错账户会被踢出登录）。本域给骑手语义<b>独立号段</b>，彻底切断共用。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum RiderErrorCode implements ErrorCodeDef {

    /**
     * 骑手不存在（未注册或已注销）。
     *
     * <p>触发：按骑手 ID 查不到记录。用户看到「骑手不存在或已注销」。
     *
     * <p>排查：确认骑手 ID 与入驻状态。
     */
    RIDER_NOT_FOUND(35001, ErrorMeta.builder(35001, "骑手不存在或已注销")
            .devMessage("按骑手 ID 未查到骑手记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 骑手资料参数不合法。
     *
     * <p>触发：骑手资料提交校验失败。用户看到「资料填写有误，请检查后重试」。
     *
     * <p>排查：确认证件与联系方式格式。
     */
    RIDER_PARAM_INVALID(35002, ErrorMeta.builder(35002, "骑手资料填写有误，请检查")
            .devMessage("骑手资料参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 骑手状态不允许（未通过审核即接单）。
     *
     * <p>触发：对当前状态的骑手执行不允许的操作。用户看到「骑手当前状态不允许该操作」。
     *
     * <p>排查：确认骑手审核状态与接单开关。
     */
    RIDER_STATE_NOT_ALLOWED(35003, ErrorMeta.builder(35003, "骑手当前状态不允许该操作")
            .devMessage("骑手状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 骑手账号已被绑定（手机号重复注册）。
     *
     * <p>触发：注册时手机号冲突。用户看到「该手机号已注册为骑手」。
     *
     * <p>排查：引导用户直接登录。
     */
    RIDER_ALREADY_BOUND(35004, ErrorMeta.builder(35004, "该手机号已注册为骑手")
            .devMessage("骑手手机号唯一约束冲突")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 查询不到骑手数据（筛选条件无命中）。
     *
     * <p>触发：骑手列表查询返回空。用户看到空状态占位，不弹 Toast。
     *
     * <p>排查：这是 INFO 级别，正常业务结果。
     */
    RIDER_QUERY_RESULT_EMPTY(35007, ErrorMeta.builder(35007, "没有找到符合条件的骑手")
            .devMessage("骑手列表查询无命中")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 只能提现到骑手本人的账户。
     *
     * <p>触发：提现账户与骑手实名不一致。用户看到「只能提现到本人账户」。
     *
     * <p>排查：<b>历史坑点</b>：这个语义在 RiderCodeEnume 里占用了 11006，
     * 而 11006 在 BizCodeEnume 里是「用户未登录」，admin 端据此清 token 跳登录 ——
     * 骑手提现填错账户会被踢出登录。本码独立登记后彻底切断该误判。
     */
    RIDER_WITHDRAW_ACCOUNT_INVALID(35010, ErrorMeta.builder(35010, "只能提现到本人账户")
            .devMessage("提现账户与骑手实名不一致")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(11006)
            .troubleshooting("核对提现账户实名与骑手入驻实名是否一致；确认账户绑定流程是否校验了实名；"
                    + "注意旧码 11006 在主站侧语义为「用户未登录」（AUTH 域 30802），勿混用")
            .since("v1.2.0")
            .remark("认领冲突旧码 11006 的骑手侧语义（RiderCodeEnume.CAN_ONLY_WITHDRAW_ONESELF_ACCOUNT）；"
                    + "主站语义见 AUTH 域 30802，两者共同认领 11006 → AMBIGUOUS，禁止 1:1 自动映射")
            .build()),

    /**
     * 骑手审核未通过。
     *
     * <p>触发：审核未通过的骑手执行业务操作。用户看到「骑手尚未通过审核」。
     *
     * <p>排查：确认审核记录与驳回原因。
     */
    RIDER_AUDIT_NOT_PASSED(35011, ErrorMeta.builder(35011, "骑手尚未通过审核")
            .devMessage("骑手审核状态不为通过")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 骑手定位信息无效（经纬度缺失或超出范围）。
     *
     * <p>触发：上报定位时校验失败。用户看到「定位信息无效，请检查定位权限」。
     *
     * <p>排查：确认客户端定位权限与坐标系。
     */
    RIDER_LOCATION_INVALID(35012, ErrorMeta.builder(35012, "定位信息无效，请检查定位权限")
            .devMessage("骑手定位经纬度缺失或超出合法范围")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build())
    ;

    private final int code;
    private final ErrorMeta meta;

    RiderErrorCode(int code, ErrorMeta meta) {
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
