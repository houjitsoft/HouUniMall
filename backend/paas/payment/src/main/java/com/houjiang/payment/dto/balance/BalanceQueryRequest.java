package com.houjiang.payment.dto.balance;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一余额查询请求DTO
 * 接口：12.统一余额查询
 * 请求地址：/balance/query
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceQueryRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商ID（可选）
     * 用于服务商代发起交易，传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 商户号（必填）
     */
    private String mchNo;
    
    /**
     * 应用ID（必填）
     * 默认为商户appId，若为服务商模式，需传入服务商appId
     */
    private String appId;
    
    /**
     * 渠道（必填）
     * allinpay：收银宝
     * yunst2isv：云商通特色结算
     * yunst2yhzh：云商通银行账户
     */
    private String chanNo;
    
    /**
     * 渠道参数（必填）
     * JSON字符串，不同渠道参数不同：
     * 
     * (1) allinpay渠道需要指定商户号
     * 例如：{"orgid":"66045xxxxxxx","cusid":"66046xxxxxxxxx"}
     * 
     * (2) yunst2isv渠道需要指定会员及账户类型
     * 例如：{"bizUserId":"BS1738808XXX","acctType":"08"}
     * acctType取值：08-待结算户
     * 
     * (3) yunst2yhzh渠道需指定会员及账户类型
     * 例如：{"bizUserId":"xxxxx","acctType":"PENDING_SETTLE"}
     * acctType取值：NO_PWD-免密余额户、PENDING_SETTLE-待结算户
     */
    private String channelExtra;
}
