package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * INVENTORY（域 47）库存错误码，号段 47001-47999。
 *
 * <p>解决的问题：库存不足过去返回一句「库存不足」，用户不知道是「少几件」还是「系统算错」；
 * 而锁库存失败与扣减失败混在一起，超卖排查困难。本域按「记录缺失 / 不足 / 锁定失败 / 扣减失败」分开。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum InventoryErrorCode implements ErrorCodeDef {

    /**
     * 库存记录不存在（商品未初始化库存）。
     *
     * <p>触发：按 SKU 查不到库存记录。用户看到「该商品暂未维护库存」。
     *
     * <p>排查：确认 SKU 是否初始化库存；新商品需先入库。
     */
    INVENTORY_RECORD_NOT_FOUND(47001, ErrorMeta.builder(47001, "该商品暂未维护库存")
            .devMessage("按 SKU 未查到库存记录")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .troubleshooting("1. 确认 SKU 是否初始化库存；2. 新商品需先做入库单。")
            .legacyCodes(15034)
            .since("v1.2.0")
            .build()),

    /**
     * 库存参数不合法（数量为负、仓库 ID 缺失）。
     *
     * <p>触发：库存操作参数校验失败。用户看到「库存操作参数有误」。
     *
     * <p>排查：确认数量正负号与仓库归属。
     */
    INVENTORY_PARAM_INVALID(47002, ErrorMeta.builder(47002, "库存操作参数有误，请检查")
            .devMessage("库存操作参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 库存状态不允许（冻结中的库存不允许扣减）。
     *
     * <p>触发：对冻结库存执行操作。用户看到「库存已冻结，无法操作」。
     *
     * <p>排查：确认冻结来源（盘点、售后），解冻后再操作。
     */
    INVENTORY_STATE_NOT_ALLOWED(47003, ErrorMeta.builder(47003, "库存已冻结，无法操作")
            .devMessage("库存状态为冻结，操作被拒绝")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 库存不足。
     *
     * <p>触发：可用库存小于需求量。用户看到「库存不足，请减少购买数量」。
     *
     * <p>排查：确认是否超卖；核对锁定量与可用量计算口径。
     */
    INVENTORY_STOCK_NOT_ENOUGH(47006, ErrorMeta.builder(47006, "库存不足，请减少购买数量")
            .devMessage("可用库存小于需求数量")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15009, 15012, 15027, 15036, 15037)
            .since("v1.2.0")
            .remark("不回显具体剩余数量，避免暴露经营数据")
            .build()),

    /**
     * 查询不到库存数据（筛选条件无命中）。
     *
     * <p>触发：库存列表查询返回空。用户看到空状态占位，不弹 Toast。
     *
     * <p>排查：这是 INFO 级别，正常业务结果。
     */
    INVENTORY_QUERY_RESULT_EMPTY(47007, ErrorMeta.builder(47007, "没有找到符合条件的库存记录")
            .devMessage("库存列表查询无命中")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 库存锁定失败（并发抢占失败）。
     *
     * <p>触发：并发下锁库存未抢到。用户看到「抢购太火爆，请稍后重试」。
     *
     * <p>排查：这是高并发下的正常保护，可重试；如频繁出现需评估锁粒度。
     */
    INVENTORY_LOCK_FAILED(47010, ErrorMeta.builder(47010, "抢购太火爆，请稍后重试")
            .devMessage("并发锁库存未抢到")
            .level(ErrorLevel.WARN).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 库存扣减失败。
     *
     * <p>触发：扣减写库失败。用户看到「库存更新失败，请稍后重试」。
     *
     * <p>排查：<b>注意是否扣减了一半</b>，需核对扣减流水后再让用户重试。
     */
    INVENTORY_DEDUCT_FAILED(47011, ErrorMeta.builder(47011, "库存更新失败，请稍后重试")
            .devMessage("库存扣减写库失败，需核对扣减流水")
            .level(ErrorLevel.ERROR).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 核对库存扣减流水是否落库；2. 确认分布式事务未悬挂；3. 确认后重试。")
            .since("v1.2.0")
            .build())
    ;

    private final int code;
    private final ErrorMeta meta;

    InventoryErrorCode(int code, ErrorMeta meta) {
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
