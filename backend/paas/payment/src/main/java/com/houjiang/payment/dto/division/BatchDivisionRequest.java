package com.houjiang.payment.dto.division;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 批量分账请求DTO
 * 接口：15.批量分账
 * 请求地址：/batchDivision/division
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BatchDivisionRequest extends PaymentBaseRequest {
    
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
     * 分账结果异步回调URL(可选)
     */
    private String notifyUrl;
    
    /**
     * 分账渠道(必填)
     * allinpay: 收银宝渠道
     * yunst2isv: 特色结算渠道
     * yunst2yhzh: 银行账户渠道
     */
    private String chanNo;
    
    /**
     * 待分账列表(必填)
     * JSONArray字符串,最多支持50条
     * 当渠道为allinpay和yunst2yhzh时,仅支持上送一条数据
     */
    private String batchDivisionInfos;
}
