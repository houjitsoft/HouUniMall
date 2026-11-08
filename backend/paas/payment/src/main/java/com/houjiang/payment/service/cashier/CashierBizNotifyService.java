package com.houjiang.payment.service.cashier;

import com.houjiang.payment.entity.PayCashierOrderEntity;

/**
 * 收银台业务回调 hook（Task 1 预留）
 *
 * <p>支付成功后由 {@code CashierCallbackService} 调用，将支付成功事件
 * 转发给业务方（比如订单服务、会员服务）。当前默认实现仅做 HTTP 透传
 * （调用订单上送的 notifyUrl），后续可扩展为 Feign/MQ 等多种策略。
 *
 * @author Amos
 */
public interface CashierBizNotifyService {

    /**
     * 支付成功通知业务方
     * @param order 收银台订单（已置为 SUCCESS）
     * @return true 表示业务方已成功接收
     */
    boolean notifyPaySuccess(PayCashierOrderEntity order);
}
