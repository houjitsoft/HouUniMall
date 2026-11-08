package com.houjiang.commons.dto.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName AccountPeriodParamDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/26 16:52
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountPeriodParamDTO {

    /**
     * 账期id
     */
    private Integer accountId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 是否授权账期 0，未授权 1，已授权 2，已冻结
     */
    private Integer isAccount;

    /**
     * 账期是否限额 0，不限 1，限额
     */
    private Integer accountIsLimit;

    /**
     * 账期金额
     */
    private BigDecimal accountPeriodPrice;

    /**
     * 账期时间
     */
    private Integer accountPeriodTime;

    /**
     * 账期插入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date accountInsertTime;

    /**
     * 插入或更新
     */
    private Integer insertOrUpdate;

    /**
     * 已用金额
     */
    private BigDecimal amountUsed;

    /**
     * 可用金额
     */
    private BigDecimal availableAmount;

    /**
     * 本月已用金额
     */
    private BigDecimal monthAmountUsed;

    private String token;

    public AccountPeriodParamDTO(Integer shopId, String userId, BigDecimal amountUsed, BigDecimal availableAmount, BigDecimal monthAmountUsed) {
        this.shopId = shopId;
        this.userId = userId;
        this.amountUsed = amountUsed;
        this.availableAmount = availableAmount;
        this.monthAmountUsed = monthAmountUsed;
    }
}
