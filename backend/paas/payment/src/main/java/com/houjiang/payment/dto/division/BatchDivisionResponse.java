package com.houjiang.payment.dto.division;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 批量分账响应DTO
 * 接口：15.批量分账
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BatchDivisionResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 分账批次号(必填)
     * 返回系统分账批次号
     */
    private String batchNo;
    
    /**
     * 分账状态(必填)
     * 1-进行中
     * 2-分账成功
     * 3-分账失败
     */
    private Integer state;
    
    /**
     * 上游渠道返回的错误码(可选)
     */
    private String errCode;
    
    /**
     * 上游渠道返回的错误描述(可选)
     */
    private String errMsg;
}
