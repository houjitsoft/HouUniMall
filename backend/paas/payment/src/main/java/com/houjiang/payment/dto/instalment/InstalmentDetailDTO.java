package com.houjiang.payment.dto.instalment;

import lombok.Data;

import java.io.Serializable;

/**
 * 分期明细DTO
 * instalmentList分期明细
 * 
 * @author Amos
 */
@Data
public class InstalmentDetailDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 期数(必填)
     */
    private Integer instalmentNum;
    
    /**
     * 每期还款金额,单位:分(必填)
     */
    private Long instalmentRepAmt;
    
    /**
     * 每期利息,单位:分(必填)
     */
    private Long instalmentRepInt;
}
