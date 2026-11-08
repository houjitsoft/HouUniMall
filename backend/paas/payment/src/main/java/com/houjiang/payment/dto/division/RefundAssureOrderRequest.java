package com.houjiang.payment.dto.division;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分账撤销请求DTO
 * 接口：18.分账撤销接口
 * 请求地址：/refund/refundAssureOrder
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundAssureOrderRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商id(可选)
     * 用于服务商代发起交易,传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     */
    private String appId;
    
    /**
     * 渠道编号(必填)
     * allinpay,与原分账订单相同
     */
    private String chanNo;
    
    /**
     * 支付结果异步回调URL(可选)
     * 只有传了该值才会发起回调,yunst2isv有效,其他渠道无通知
     */
    private String notifyUrl;
    
    /**
     * 商户订单号(必填)
     */
    private String refundMchOrderNo;
    
    /**
     * 原商户分账订单号(可选)
     * 与assureOrderId二选一传入
     */
    private String mchOrderNo;
    
    /**
     * 原通企付分账订单号(可选)
     * 与mchOrderNo二选一传入
     */
    private String assureOrderId;
    
    /**
     * 撤销金额,单位:分(必填)
     */
    private Long refundAmount;
    
    /**
     * 渠道参数(可选)
     * divisionRefundInfo特定渠道发起的额外参数,JSONArray字符串
     * allinpay渠道无需上送
     * yunst2isv如不上送默认从收款人撤销
     * yunst2yhzh必须上送
     */
    private String channelExtra;
}
