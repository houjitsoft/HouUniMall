package com.houjiang.payment.dto.division;

import lombok.Data;

import java.io.Serializable;

/**
 * 分账撤销明细DTO
 * refundInfoList分账回退详情
 * 
 * @author Amos
 */
@Data
public class RefundInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 分账方的商户会员编号(必填)
     * yunst2isv和yunst2yhzh渠道上送
     */
    private String divisionBizMemNo;
    
    /**
     * 撤销金额,单位:分(必填)
     */
    private Long refundAmount;
}
