package com.houjiang.plugin.engine.config;

import com.houjiang.plugin.engine.registry.ExtensionPointKeys;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 扩展点降级策略表（R3.2 / §1.3）。
 *
 * <p>引擎内置 17 个扩展点的默认降级策略；宿主可用
 * {@code houcloud.plugin.degrade.{pointShortName}} 覆盖。键大小写不敏感，
 * {@code PriceCalc} / {@code price-calc} / {@code PRICE_CALC} 均解析到同一点。
 *
 * <p><b>未知扩展点</b>默认 {@link ExtensionDegradePolicy#ABORT}（fail-closed，宁中断不误放）。
 *
 * <p><b>锁定集</b>（金额 / 库存 / 支付等强一致链路）不允许被配置成
 * {@link ExtensionDegradePolicy#IGNORE}——{@link #applyOverrides(Map)} 检测到该配置时直接抛
 * {@link IllegalStateException}，使宿主启动 fail-fast，避免带着错误的金额 / 库存 / 支付结果继续。
 *
 * @author HouCloud
 */
public class ExtensionPolicyRegistry {

    /** 锁定集（point 短名的规范化键）：被配置成 IGNORE 会导致启动失败。 */
    public static final Set<String> LOCKED_POINTS = Set.of(
            ExtensionPointKeys.canonical("PriceCalc"),
            ExtensionPointKeys.canonical("CartCalc"),
            ExtensionPointKeys.canonical("StockLock"),
            ExtensionPointKeys.canonical("StockChange"),
            ExtensionPointKeys.canonical("OrderCreateBefore"),
            ExtensionPointKeys.canonical("OrderCreateAfter"),
            ExtensionPointKeys.canonical("AfterSaleCreate"),
            ExtensionPointKeys.canonical("PayCallback"),
            ExtensionPointKeys.canonical("PromotionPaySettle"),
            ExtensionPointKeys.canonical("RefundCreate"));

    /** 内置 17 点默认策略表（规范化键）。 */
    private static final Map<String, ExtensionDegradePolicy> DEFAULT_POLICIES = buildDefaults();

    /** 宿主覆盖项（规范化键 → policy）。 */
    private final Map<String, ExtensionDegradePolicy> overrides = new HashMap<>();

    private static Map<String, ExtensionDegradePolicy> buildDefaults() {
        Map<String, ExtensionDegradePolicy> map = new HashMap<>();
        // ABORT（fail-closed）：金额 / 库存 / 支付 / 下单前后置 / 售后 / 退款
        put(map, "PriceCalc", ExtensionDegradePolicy.ABORT);
        put(map, "CartCalc", ExtensionDegradePolicy.ABORT);
        put(map, "StockLock", ExtensionDegradePolicy.ABORT);
        put(map, "StockChange", ExtensionDegradePolicy.ABORT);
        put(map, "OrderCreateBefore", ExtensionDegradePolicy.ABORT);
        put(map, "OrderCreateAfter", ExtensionDegradePolicy.ABORT);
        put(map, "AfterSaleCreate", ExtensionDegradePolicy.ABORT);
        put(map, "PayCallback", ExtensionDegradePolicy.ABORT);
        put(map, "PromotionPaySettle", ExtensionDegradePolicy.ABORT);
        put(map, "RefundCreate", ExtensionDegradePolicy.ABORT);
        // CartValidate：校验型 before 点，故障时 fail-closed（非锁定集，可配置为 IGNORE）
        put(map, "CartValidate", ExtensionDegradePolicy.ABORT);
        // FALLBACK（回落官方）
        // PromotionCalc 维持 FALLBACK（不动配置），但需注意其残留风险：
        //   ① FALLBACK 只作用于「故障」（抛异常 / 超时）——故障时静默回落到官方价，主流程继续；
        //   ② 金额类扩展点若「业务上必须中断」（如防套利需拒绝下单），必须显式调用 ctx.abort(reason)
        //      （业务中断，非故障，不受 FALLBACK 影响）；若改为「抛异常」表达拒绝，会被 FALLBACK 静默吞掉，
        //      交易继续按官方原价成交，导致用户按原价付款、促销失效；
        //   ③ 宿主如需收紧为 fail-closed，可配置 houcloud.plugin.degrade.PromotionCalc=ABORT，
        //      此时该点故障将中断主链路（PromotionCalc 非锁定集，允许配置为 ABORT）。
        put(map, "PromotionCalc", ExtensionDegradePolicy.FALLBACK);
        put(map, "FreightCalc", ExtensionDegradePolicy.FALLBACK);
        // IGNORE（旁路能力，故障不阻断主流程）
        put(map, "LoginAfter", ExtensionDegradePolicy.IGNORE);
        put(map, "RedpacketQuery", ExtensionDegradePolicy.IGNORE);
        put(map, "MenuContribution", ExtensionDegradePolicy.IGNORE);
        put(map, "DashboardWidget", ExtensionDegradePolicy.IGNORE);
        return Map.copyOf(map);
    }

    private static void put(Map<String, ExtensionDegradePolicy> map, String point, ExtensionDegradePolicy policy) {
        map.put(ExtensionPointKeys.canonical(point), policy);
    }

    /**
     * 查询某个扩展点的降级策略。
     *
     * <p>顺序：宿主覆盖 → 内置默认 → {@link ExtensionDegradePolicy#ABORT}。
     */
    public ExtensionDegradePolicy policyOf(Class<?> point) {
        String key = ExtensionPointKeys.canonicalOf(point);
        ExtensionDegradePolicy override = overrides.get(key);
        if (override != null) {
            return override;
        }
        return DEFAULT_POLICIES.getOrDefault(key, ExtensionDegradePolicy.ABORT);
    }

    /** @return 该扩展点是否属于锁定集（金额 / 库存 / 支付）。 */
    public boolean isLocked(Class<?> point) {
        return LOCKED_POINTS.contains(ExtensionPointKeys.canonicalOf(point));
    }

    /**
     * 应用宿主配置覆盖。
     *
     * <p><b>fail-fast</b>：若把锁定集扩展点配置成 {@link ExtensionDegradePolicy#IGNORE}，
     * 直接抛 {@link IllegalStateException}，宿主启动失败。
     *
     * @param cfg 配置：键为扩展点短名，值为策略名
     */
    public void applyOverrides(Map<String, String> cfg) {
        if (cfg == null || cfg.isEmpty()) {
            return;
        }
        Map<String, ExtensionDegradePolicy> parsed = new HashMap<>();
        for (Map.Entry<String, String> entry : cfg.entrySet()) {
            String key = ExtensionPointKeys.canonical(entry.getKey());
            ExtensionDegradePolicy policy = ExtensionDegradePolicy.parse(entry.getValue());
            if (LOCKED_POINTS.contains(key) && policy == ExtensionDegradePolicy.IGNORE) {
                throw new IllegalStateException(String.format(
                        "锁定扩展点 %s 属于金额/库存/支付强一致链路，不允许降级为 IGNORE（fail-closed）。"
                                + "如需放宽请改用 ABORT 或 FALLBACK。", entry.getKey()));
            }
            parsed.put(key, policy);
        }
        overrides.putAll(parsed);
    }

    /** 当前生效的策略快照（含内置与覆盖），供诊断使用。 */
    public Map<String, ExtensionDegradePolicy> snapshot() {
        Map<String, ExtensionDegradePolicy> merged = new HashMap<>(DEFAULT_POLICIES);
        merged.putAll(overrides);
        return Map.copyOf(merged);
    }

    /** 扩展点短名：{@code PriceCalcExtension} → {@code PriceCalc}。 */
    public static String shortNameOf(Class<?> type) {
        return ExtensionPointKeys.shortName(type);
    }
}
