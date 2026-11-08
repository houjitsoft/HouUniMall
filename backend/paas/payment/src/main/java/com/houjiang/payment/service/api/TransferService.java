package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.transfer.TransferOrderRequest;
import com.houjiang.payment.dto.transfer.TransferOrderResponse;

/**
 * 转账服务接口
 * 
 * @author Amos
 */
public interface TransferService {
    
    /**
     * 20. 转账接口
     * 用于商户之间余额转账
     * 同步返回转账结果,若未返回终态,请通过统一订单查询接口查询转账结果
     * 
     * @param request 转账请求
     * @return 转账响应
     */
    TransferOrderResponse transferOrder(TransferOrderRequest request);
}
