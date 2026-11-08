package com.houjiang.payment.dto.bill;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账单导入请求DTO
 * 接口：10.账单导入
 * 请求地址：pay/billImport
 * 
 * 使用场景：
 * 通过接口导入账单，员工可通过通企付企业服务助手查询账单并分享账单码牌用于收款
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BillImportRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商ID（可选）
     * 用于服务商模式，传了此参数需要用服务商密钥进行验签
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
     * 收款模板编号（必填）
     */
    private String termNo;
    
    /**
     * 账单信息（必填）
     * 最大1000笔，为JSONArray字符串
     */
    private String billsInfo;
}
