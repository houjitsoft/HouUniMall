package com.houjiang.payment.dto.division;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分账撤销列表详情DTO
 * divisionRefundInfo分账撤销列表详情
 * 
 * @author Amos
 */
@Data
public class DivisionRefundInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 撤销金额,单位:分(必填)
     */
    private Long refundAmount;
    
    /**
     * 原订单收款人(可选)
     * yunst2isv渠道必须上送,其他渠道不用上送
     */
    private String divisionBizMemNo;
    
    /**
     * 平台抽佣分账退回金额,单位:分(可选)
     * 只对yunst2isv渠道生效
     */
    private Long refundCouponAmount;
    
    /**
     * 分账撤销明细(可选)
     * yunst2yhzh只允许上送一条
     */
    private List<RefundInfoDTO> refundInfoList;
}
