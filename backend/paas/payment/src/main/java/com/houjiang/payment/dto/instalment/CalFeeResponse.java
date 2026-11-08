package com.houjiang.payment.dto.instalment;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 分期利息计算响应DTO
 * 接口：19.分期利息计算
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CalFeeResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 分期明细列表(可选)
     * JSONArray格式
     */
    private List<InstalmentDetailDTO> instalmentList;
}
