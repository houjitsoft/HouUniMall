package com.houjiang.commons.vo.shop;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class RetentionShopInfoVO {
    /**
     * 质保金余额
     */
    private BigDecimal retentionMoneyBalance;
    /**
     * 质保金可提现
     */
    private BigDecimal retentionMoneyWithdrawal;
    /**
     * 质保金待补缴
     */
    private BigDecimal retentionMoneyPayment;
    /**
     * 质保金基础额度
     */
    private BigDecimal retentionMoneyLimit;
    /**
     * 本月罚款金额
     */
    private BigDecimal monthFine;
    /**
     * 累计赔付金额
     */
    private BigDecimal accumulatedCompensation;
    /**
     * 质保金状态 0 无需缴纳 1 已缴纳 2 请及时补充 3 即将限权 4 已限权
     */
    private Integer retentionStatus;
    /**
     * 质保金说明
     */
    private String retentionIllustrate;
    /**
     * 账户余额
     */
    private String accountBalance;
    /**
     * 提现账户
     */
    private String userMobile;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date shopCreateTime;
    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paymentTime;
    /**
     * 创建时间
     */
    private String shopName;
    /**
     * 店铺id
     */
    private Integer shopId;


}
