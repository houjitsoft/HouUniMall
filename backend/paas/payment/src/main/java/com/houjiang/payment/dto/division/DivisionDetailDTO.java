package com.houjiang.payment.dto.division;

import lombok.Data;

/**
 * 分账信息明细DTO
 * 
 * @author Amos
 */
@Data
public class DivisionDetailDTO {
    
    /**
     * 分账方的商户会员编号(必填)
     * yunst2isv和yunst2yhzh渠道: 上送分账方的商户会员编号
     * allinpay渠道: 上送分账入金的收银宝商户号
     */
    private String divisionBizMemNo;
    
    /**
     * 分账金额,单位:分(必填)
     */
    private Long amount;
    
    /**
     * 备注,最多100个字符(可选)
     */
    private String remark;
}
