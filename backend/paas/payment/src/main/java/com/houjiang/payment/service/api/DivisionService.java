package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.division.BatchDivisionRequest;
import com.houjiang.payment.dto.division.BatchDivisionResponse;
import com.houjiang.payment.dto.division.QueryDivisionRequest;
import com.houjiang.payment.dto.division.QueryDivisionResponse;
import com.houjiang.payment.dto.division.RefundAssureOrderRequest;
import com.houjiang.payment.dto.division.RefundAssureOrderResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 分账服务接口
 * 
 * @author Amos
 */
public interface DivisionService {
    
    /**
     * 15. 批量分账
     * 用于对收款订单发起分账
     * 
     * @param request 批量分账请求
     * @return 批量分账响应
     */
    BatchDivisionResponse batchDivision(BatchDivisionRequest request);
    
    /**
     * 16. 分账结果通知处理
     * 接收第三方支付平台的分账异步通知
     * 
     * @param httpRequest HTTP请求对象
     * @return 通知处理结果 success/fail
     */
    String handleDivisionNotify(HttpServletRequest httpRequest);
    
    /**
     * 17. 查询分账订单
     * 用于查询批量分账请求中单笔订单的分账结果
     * 
     * @param request 查询分账订单请求
     * @return 查询分账订单响应
     */
    QueryDivisionResponse queryDivision(QueryDivisionRequest request);
    
    /**
     * 18. 分账撤销
     * 将已分账的资金从原分账订单的入金方退回到出金方
     * 
     * @param request 分账撤销请求
     * @return 分账撤销响应
     */
    RefundAssureOrderResponse refundAssureOrder(RefundAssureOrderRequest request);
    
    /**
     * 26. 分账撤销结果通知处理
     * 接收第三方支付平台的分账撤销异步通知
     * 
     * @param httpRequest HTTP请求对象
     * @return 通知处理结果 success/fail
     */
    String handleRefundAssureNotify(HttpServletRequest httpRequest);
}
