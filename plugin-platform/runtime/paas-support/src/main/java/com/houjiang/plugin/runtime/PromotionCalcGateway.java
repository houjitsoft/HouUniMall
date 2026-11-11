package com.houjiang.plugin.runtime;

import com.houjiang.plugin.api.extension.order.PromotionCalcExtension;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 促销算价门面：Core（order）算价的唯一入口，替代原先的 {@code @FeignClient("activity")}。
 *
 * <p><b>为什么必须换掉 Feign</b>：activity 下每一个活动（秒杀 / 限时折扣 / 满减满折 /
 * 优惠券 / 拼团 / 红包…）都是<b>独立插件</b>，各自实现 {@link PromotionCalcExtension}
 * 并以 {@code promotionType} 区分。届时不存在一个可被 {@code @FeignClient("activity")}
 * 寻址的单一服务，硬编码服务名会在插件化后直接失联。
 *
 * <p>Core 只面向扩展点编程，不 import 任何插件侧类型：
 * <ul>
 *   <li><b>无活动插件</b>：{@link ExtensionInvoker} 取到空列表，零开销跳过，按原价成交——
 *       这正是最终版 §3.2「不因插件未安装而阻断基础下单」；</li>
 *   <li><b>装了 N 个活动插件</b>：按优先级依次执行并累加优惠，无需 Core 知道有哪几个活动；</li>
 *   <li><b>插件抛异常</b>：由 {@link ExtensionInvoker} 隔离并记日志，绝不打断算价主链路；</li>
 *   <li><b>限购不通过</b>：插件调用 {@code abort}，本门面把原因回传，由 Core 抛业务异常。</li>
 * </ul>
 *
 * <p><b>金额兜底</b>：优惠额被夹在 {@code [0, baseAmount]} 之间。插件写入负数或超过原价都属异常，
 * 直接采信会让用户多付或倒贴，故夹取并告警。
 *
 * @author HouCloud
 */
public class PromotionCalcGateway {

    private static final Logger log = LoggerFactory.getLogger(PromotionCalcGateway.class);

    /**
     * 经宿主统一网关调用，而非直接使用 {@code ExtensionInvoker}。
     *
     * <p>这样业务中断与扩展点故障走同一套转译：引擎侧的 {@code ExtensionFaultException}
     * 由 {@link ExtensionGatewaySupport} 转成 {@link PluginFaultException}（7002），
     * 业务中断转成 {@link com.houjiang.plugin.runtime.PluginAbortException}（7001）。
     *
     * <p><b>注意当前降级策略</b>：引擎默认表（{@code ExtensionPolicyRegistry}）把
     * {@code PromotionCalc} 归为 {@code FALLBACK}——故障（异常 / 超时）时<b>回落官方价、
     * 不抛异常</b>，即优惠会静默消失。而 {@code order} 模块的
     * {@code LegacyActivityPromotionCalcExtension} 明确要求「Feign 失败必须 abort，
     * 不能静默当作无优惠」，两者语义冲突。需要 fail-closed 的站点必须显式覆盖：
     * {@code houcloud.plugin.degrade.PromotionCalc=ABORT}。
     */
    private final ExtensionGatewaySupport support;

    public PromotionCalcGateway(ExtensionInvoker invoker) {
        this.support = new ExtensionGatewaySupport(Objects.requireNonNull(invoker, "invoker"));
    }

    /** 算价结果。 */
    public record Result(BigDecimal discountAmount,
                         Map<String, Object> promotionDetail,
                         boolean aborted,
                         String abortReason) {
    }

    /**
     * 执行促销算价。
     *
     * @param promotionType 促销类型：SECKILL / DISCOUNT / REDUCTION / COUPON 等
     * @param userId        下单用户
     * @param items         商品行，元素约定 skuId / commonId / quantity / price
     * @param baseAmount    Core 按官方价算出的原价合计
     * @param extra         透传参数（活动 ID、店铺 ID 等）
     */
    public Result calc(String promotionType,
                       Long userId,
                       List<Map<String, Object>> items,
                       BigDecimal baseAmount,
                       Map<String, Object> extra) {
        return calc(promotionType, userId, items, baseAmount, extra, 0L);
    }

    /**
     * 执行促销算价（带超时提示的重载）。
     *
     * <p>占用额度等<b>写账本</b>类调用必须传 {@code timeoutHintMs}（建议 2000）：
     * 引擎默认 200ms 会把真实成功的写库掐成超时，宿主以为失败中断下单，
     * 插件线程却继续落库，造成额度泄漏（2026-11-11 E2E 实测）。
     *
     * @param timeoutHintMs 本次调用超时提示毫秒数；&le;0 用引擎全局默认
     */
    public Result calc(String promotionType,
                       Long userId,
                       List<Map<String, Object>> items,
                       BigDecimal baseAmount,
                       Map<String, Object> extra,
                       long timeoutHintMs) {
        PromotionCalcExtension.Context ctx = new PromotionCalcExtension.Context();
        ctx.promotionType = promotionType;
        ctx.userId = userId;
        if (items != null) {
            ctx.items = items;
        }
        BigDecimal base = baseAmount == null ? BigDecimal.ZERO : baseAmount;
        ctx.baseAmount = base;
        if (extra != null) {
            ctx.extra = extra;
        }
        if (timeoutHintMs > 0) {
            ctx.setTimeoutHintMs(timeoutHintMs);
        }

        support.invoke(PromotionCalcExtension.class, ctx);

        return new Result(clamp(ctx.discountAmount, base, promotionType),
                ctx.promotionDetail,
                ctx.isAborted(),
                ctx.getAbortReason());
    }

    private BigDecimal clamp(BigDecimal discountAmount, BigDecimal baseAmount, String promotionType) {
        if (discountAmount == null) {
            return BigDecimal.ZERO;
        }
        if (discountAmount.signum() < 0) {
            log.warn("[plugin-runtime] 促销插件返回负优惠，已夹为 0：promotionType={}, raw={}",
                    promotionType, discountAmount);
            return BigDecimal.ZERO;
        }
        if (discountAmount.compareTo(baseAmount) > 0) {
            log.warn("[plugin-runtime] 促销优惠超过原价，已夹为原价：promotionType={}, raw={}, base={}",
                    promotionType, discountAmount, baseAmount);
            return baseAmount;
        }
        return discountAmount;
    }
}
