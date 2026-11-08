package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.trade.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 交易服务接口
 * 按照文档规范定义的支付交易相关服务
 * 
 * @author Amos
 */
public interface TradeService {

    /**
     * 2. 统一下单
     * 
     * @param request 下单请求参数
     * @return 下单响应结果
     */
    UnifiedOrderResponse unifiedOrder(UnifiedOrderRequest request);

    /**
     * 4. 统一订单查询
     * 
     * @param request 查询请求参数
     * @return 订单状态信息
     */
    QueryOrderResponse queryOrder(QueryOrderRequest request);

    /**
     * 5. 关闭支付订单
     * 
     * @param request 关闭订单请求
     * @return 关闭结果
     */
    CloseOrderResponse closeOrder(CloseOrderRequest request);

    /**
     * 6. 统一退款
     * 
     * @param request 退款请求参数
     * @return 退款响应结果
     */
    RefundResponse refund(RefundRequest request);

    /**
     * 8. 查询退款订单
     * 
     * @param request 查询退款请求
     * @return 退款状态信息
     */
    QueryRefundResponse queryRefund(QueryRefundRequest request);

    /**
     * 7. 支付通知处理
     * 
     * @param httpRequest HTTP请求对象
     * @return 通知处理结果 SUCCESS/FAIL
     */
    String handlePayNotify(HttpServletRequest httpRequest);

    /**
     * 9. 退款通知处理
     * 
     * @param httpRequest HTTP请求对象
     * @return 通知处理结果 SUCCESS/FAIL
     */
    String handleRefundNotify(HttpServletRequest httpRequest);
}
