package com.houjiang.mall.pay.plugin;

/**
 * 支付回调处理异常。
 *
 * <p>携带 {@link #fatal} 标记区分两类失败：
 * <ul>
 *   <li><b>致命（fatal=true）</b>：验签失败、订单号缺失、金额不一致 —— 回调方<b>不要再重发</b>，
 *       业务侧记一条失败流水等人工介入；</li>
 *   <li><b>非致命（fatal=false）</b>：订单当前状态不接受该回调（已关闭、重复回调已处理）——
 *       返回渠道成功应答让其停止重发，但不是错误。</li>
 * </ul>
 *
 * @author Amos
 */
public class PayNotifyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final boolean fatal;

    public PayNotifyException(String message) {
        this(message, true);
    }

    public PayNotifyException(String message, boolean fatal) {
        super(message);
        this.fatal = fatal;
    }

    /** 是否需要人工介入（true = 渠道会一直重发，需排查）。 */
    public boolean isFatal() {
        return fatal;
    }
}
