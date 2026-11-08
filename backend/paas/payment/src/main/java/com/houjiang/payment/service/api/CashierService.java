package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.cashier.CashierCreateRequest;
import com.houjiang.payment.dto.cashier.CashierCreateResponse;
import com.houjiang.payment.dto.cashier.CashierStatusResponse;

/**
 * 统一收银台服务
 * 根据 payMethod + terminal 路由到对应的支付渠道完成下单/查询/关单
 *
 * @author Amos
 */
public interface CashierService {

    /**
     * 创建收银台订单并返回前端调起参数
     * @param request 下单请求
     * @param userId  当前登录用户ID（来自 token）
     * @return 下单响应（含 payData）
     */
    CashierCreateResponse create(CashierCreateRequest request, String userId);

    /**
     * 查询收银台订单状态
     * @param cashierNo 收银台订单号
     * @return 状态响应
     */
    CashierStatusResponse query(String cashierNo);

    /**
     * 关闭收银台订单
     * @param cashierNo 收银台订单号
     * @return 最新状态
     */
    CashierStatusResponse close(String cashierNo);

    /**
     * 余额扣款（C 端收银台 “余额支付” 独立入口）
     * <p>仅允许下单人=当前 userId、订单状态=WAIT_PAY 、payMethod=BALANCE 三者一致时才可扣款。</p>
     * <p>扣款逻辑复用与《创建订单》同一事务中的 handleBalancePay，成功后订单点为 SUCCESS。</p>
     */
    CashierStatusResponse payByBalance(String cashierNo, String userId);
}
