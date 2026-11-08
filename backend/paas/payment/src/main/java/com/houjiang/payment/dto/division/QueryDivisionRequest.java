package com.houjiang.payment.dto.division;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询分账订单请求DTO
 * 接口：17.查询分账订单
 * 请求地址：/division/query
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryDivisionRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商号(可选)
     * 用于服务商代发起交易,传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     * 默认为商户应用ID,若为服务商模式,需传入服务商appId
     */
    private String appId;
    
    /**
     * 商户分账订单号(可选)
     * 与assureOrderId二者传一即可
     */
    private String mchAssureOrderNo;
    
    /**
     * 通企付分账订单号(可选)
     * 与mchAssureOrderNo二者传一即可
     */
    private String assureOrderId;
}
