package com.houjiang.plugin.api.extension.order;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 促销算价扩展点：秒杀 / 限时折扣 / 满减满折 / 优惠券等促销能力的统一入口。
 *
 * <p><b>背景</b>：原先 Core 的 {@code order} 通过 {@code @FeignClient("activity")}
 * 硬编码调用促销服务（{@code seckillCheckLimit} / {@code discountCheckLimit} /
 * {@code selectReductionPolicyPromotionContent}）。促销能力插件化后，若站点未安装
 * 促销插件，这些调用会因服务未注册而失败，直接打断下单算价主链路。
 *
 * <p><b>解耦方式</b>：Core 只面向本扩展点编程，不再 import 任何插件侧类型。
 * 未安装促销插件时没有注册实现，调用即"零开销跳过"，Core 正常按原价成交。
 *
 * <p><b>限购/资格校验</b>：校验不通过时应调用 {@link ExtensionContext#abort(String)}
 * 中断，由宿主抛出业务异常；<b>不要</b>在扩展点内自行抛异常（异常会被宿主记录并忽略，
 * 反而导致限购失效）。中断原因请写面向运营的中文说明。
 *
 * @author Amos
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface PromotionCalcExtension extends ExtensionPoint<PromotionCalcExtension.Context> {

    final class Context extends ExtensionContext {

        /** 促销类型：SECKILL 秒杀 / DISCOUNT 限时折扣 / REDUCTION 满减满折 / COUPON 优惠券 */
        public String promotionType;

        /** 下单用户 */
        public Long userId;

        /** 参与算价的商品行，元素约定：skuId / commonId / quantity / price */
        public List<Map<String, Object>> items = new ArrayList<>();

        /** 原价合计（宿主按官方价计算，只读） */
        public BigDecimal baseAmount = BigDecimal.ZERO;

        /** 优惠金额（插件写入） */
        public BigDecimal discountAmount = BigDecimal.ZERO;

        /** 促销命中明细（插件写入，随订单留存，便于对账与售后） */
        public Map<String, Object> promotionDetail = new HashMap<>();

        /** 透传参数（活动 ID、店铺 ID 等） */
        public Map<String, Object> extra = new HashMap<>();
    }

    /**
     * 执行前钩子（促销计算）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(PromotionCalcExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（促销计算）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(PromotionCalcExtension.Context ctx) {
        /* 默认不做收尾 */
    }

    /**
     * 宿主询问实现是否关心本次上下文；返回 {@code false} 时宿主可快速跳过本实现。
     *
     * <p>默认返回 {@code true}（关心）。用于避免为无关业务场景加载重实现。
     *
     * @param ctx 扩展点上下文
     * @return 是否需要执行
     * @since 1.0
     */
    default boolean supports(PromotionCalcExtension.Context ctx) {
        return true;
    }

    class Noop implements PromotionCalcExtension {
        @Override
        public void execute(Context ctx) { /* 默认无促销，按原价成交 */
        }
    }
}
