package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * CART（域 41）购物车错误码，号段 41001-41999。
 *
 * <p>解决的问题：购物车码（15006-15009）在 BizCodeEnume 里混在 15 商品段，
 * 且 15006 出现 5 次语义各异，无法按码定位。本域独立登记。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum CartErrorCode implements ErrorCodeDef {

    /**
     * 购物车项不存在（已被删除或已下单）。
     *
     * <p>触发：按购物车项 ID 查不到记录。用户看到「购物车中没有该商品」。
     *
     * <p>排查：确认商品是否已下单出库；前端应刷新购物车。
     */
    CART_ITEM_NOT_FOUND(41001, ErrorMeta.builder(41001, "购物车中没有该商品")
            .devMessage("购物车项 ID 查不到记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 购物车参数不合法（数量为 0 或负数）。
     *
     * <p>触发：加购/改数量参数校验失败。用户看到「购买数量有误，请重新输入」。
     *
     * <p>排查：确认数量上下限与库存余量。
     */
    CART_PARAM_INVALID(41002, ErrorMeta.builder(41002, "购买数量有误，请重新输入")
            .devMessage("购物车数量参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 购物车状态不允许（已结算的项不允许改）。
     *
     * <p>触发：对已结算的购物车项执行修改。用户看到「该商品已结算，无法修改」。
     *
     * <p>排查：确认购物车项状态；已结算应重新加购。
     */
    CART_STATE_NOT_ALLOWED(41003, ErrorMeta.builder(41003, "该商品已结算，无法修改")
            .devMessage("购物车项状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 购买数量超出上限。
     *
     * <p>触发：数量超过单笔限购或库存。用户看到「购买数量超出上限」。
     *
     * <p>排查：确认限购规则与可用库存；库存不足请用 INVENTORY 域 47006。
     */
    CART_QUANTITY_EXCEEDED(41006, ErrorMeta.builder(41006, "购买数量超出上限")
            .devMessage("数量超过限购规则或可用库存")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 购物车为空。
     *
     * <p>触发：结算时购物车没有商品。用户看到空状态占位，不弹 Toast。
     *
     * <p>排查：这是 INFO 级别，正常业务结果。
     */
    CART_QUERY_RESULT_EMPTY(41007, ErrorMeta.builder(41007, "购物车还是空的")
            .devMessage("购物车列表为空")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 加入购物车失败。
     *
     * <p>触发：加购写入失败。用户看到「加入购物车失败，请稍后重试」。
     *
     * <p>排查：看服务端日志；可安全重试。
     */
    CART_ADD_FAILED(41010, ErrorMeta.builder(41010, "加入购物车失败，请稍后重试")
            .devMessage("加购写入失败，原始异常见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 修改购物车失败。
     *
     * <p>触发：改数量/改选中失败。用户看到「修改购物车失败，请稍后重试」。
     *
     * <p>排查：看服务端日志；可安全重试。
     */
    CART_UPDATE_FAILED(41011, ErrorMeta.builder(41011, "修改购物车失败，请稍后重试")
            .devMessage("购物车更新失败，原始异常见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),
    /**
     * 虚拟商品无法加入购物车。
     *
     * <p>触发/排查：虚拟商品不允许进购物车。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    CART_VIRTUAL_GOODS_NOT_ALLOWED(41101, ErrorMeta.builder(41101, "虚拟商品无法加入购物车")
            .devMessage("虚拟商品不允许进购物车")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15002)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    CartErrorCode(int code, ErrorMeta meta) {
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
