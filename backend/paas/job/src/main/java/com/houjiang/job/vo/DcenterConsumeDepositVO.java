package com.houjiang.job.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 数据大屏充值表-支付回调callback使用-确认付款
 * @TableName ${database.prefix.datav}hou_datav_consume_deposit
 * @author Amos
 */

@Data
public class DcenterConsumeDepositVO implements Serializable {
    /**
     * id
     */

    private Long id;

    /**
     * 用户号
     */
    private String depositBuyerId;

    /**
     * 交易金额
     */
    private BigDecimal depositTotalFee;

    /**
     * 交易创建时间
     */
    private Date depositGmtCreate;

    /**
     * 1是余额 2 是充值卡 
     */
    private Integer depositType;


}