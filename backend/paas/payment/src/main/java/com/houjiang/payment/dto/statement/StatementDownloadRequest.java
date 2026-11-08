package com.houjiang.payment.dto.statement;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对账文件下载请求DTO
 * 接口：11.对账文件下载
 * 请求地址：/accountstatement/getOrderFile
 * 
 * 简要描述：
 * 1. 支持核心企业按天下载对账文件，D+1日11点之后可下载D日的支付交易对账文件
 * 2. 服务商模式下可下载单个或所有核心企业的对账单
 * 3. 支持分账交易对账单和预消费交易对账单下载
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StatementDownloadRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商ID（可选）
     * 用于服务商代发起交易，传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 商户号（必填）
     * 支持传入服务商id，当传入服务商id的时候获取的为服务商对账单
     * 需要后台配置生成服务商对账单后才能使用
     */
    private String mchNo;
    
    /**
     * 应用ID（必填）
     * 默认为企业appId，若为服务商模式，需传入服务商appId
     */
    private String appId;
    
    /**
     * 交易日期（必填）
     * 格式：yyyy-MM-dd
     * D+1日11点之后可下载D日的对账文件
     */
    private String day;
    
    /**
     * 文件类型（可选）
     * CSV/EXCEL（不传默认CSV）
     */
    private String fileType;
    
    /**
     * 账单类型（可选）
     * 1-支付交易对账单（默认）
     * 2-分账交易对账单
     * 3-预消费交易对账单（仅有预消费交易的才有该对账单）
     */
    private Integer billType;
}
