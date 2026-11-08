package com.houjiang.plugin.engine.config;

/**
 * 扩展点降级策略（R3.2 / §1.3）。
 *
 * <p>描述扩展点发生<b>故障</b>（抛异常 / 超时）时的兜底方式。<b>不</b>描述
 * {@code ctx.abort(reason)} 的业务中断——业务中断恒为中断，与降级无关。
 *
 * <p>策略语义：
 * <ul>
 *   <li>{@link #IGNORE}：记录故障后忽略，主流程继续（如登录后、红包查询等旁路能力）；</li>
 *   <li>{@link #ABORT}：fail-closed，故障中断主链路（金额 / 库存 / 支付等锁定集）；</li>
 *   <li>{@link #FALLBACK}：回落官方实现取值，主流程继续（如促销算价、运费）。</li>
 * </ul>
 *
 * @author HouCloud
 */
public enum ExtensionDegradePolicy {

    /** 故障忽略：记录后继续，绝不阻断主流程。 */
    IGNORE,

    /** 故障中断：fail-closed，抛故障异常中断主链路。 */
    ABORT,

    /** 故障回落：使用官方（宿主）取值继续。 */
    FALLBACK;

    /**
     * 解析配置值（大小写不敏感）。
     *
     * @param value 配置值，如 {@code "ignore"} / {@code "ABORT"} / {@code "fallback"}
     * @return 对应策略
     * @throws IllegalArgumentException 无法识别的取值
     */
    public static ExtensionDegradePolicy parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("降级策略配置不能为空");
        }
        try {
            return valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("无法识别的降级策略：" + value
                    + "，可选 IGNORE / ABORT / FALLBACK");
        }
    }
}
