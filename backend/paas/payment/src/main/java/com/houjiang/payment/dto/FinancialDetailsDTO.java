package com.houjiang.payment.dto;



import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * 财务详细信息
 * @author Amos
 */
@Data
public class FinancialDetailsDTO extends PageHelper {

    /**
     * 交易类型表
     */
    private Integer tradeTypeId;

    /**
     * 用户账号
     */
    private String userNickname;

    /**
     * 实际支付单号
     */
    private String recordPayorder;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String afterTime;

}
