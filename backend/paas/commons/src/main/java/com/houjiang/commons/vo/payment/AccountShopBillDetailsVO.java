package com.houjiang.commons.vo.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName AccountShopBillDetailsVO
 * @Description
 * @Author Amos
 * @Date 2023/7/26 16:55
 * @Version 1.0
 * @author Amos
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountShopBillDetailsVO {

    /** 账单id */
    private String accountingPeriodId;
    /** 用户名称 */
    private String userName;
    /** 用户昵称 */
    private String userNickname;
    /** 公司名称 */
    private String companyName;
    /** 会员电话 */
    private String userMobile;
    /** 月度 */
    private String monthlyTime;
    /** 已用额度 */
    private BigDecimal usedQuota;
    /** 已回款 */
    private BigDecimal paymentCollected;
    /** 剩余账款 */
    private BigDecimal remainingAccounts;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    /** 截止还款日期 */
    private Date repaymentDate;
    /** 账款状态 */
    private Integer accountStatus;
    /** 当前回款 */
    private BigDecimal currentCollection;
    /** 回款凭证 */
    private String collectionVoucher;
    /** 回款说明 */
    private String voucherExplain;
    /** 回款审核状态 */
    private Integer collectionStatus;
    /** 用户id */
    private String userId;
    /** 店铺id */
    private Integer shopId;
    /** 店铺名称 */
    private String shopName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /** 提醒时间 */
    private Date reminderTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /** 月度开始时间 */
    private Date startTime;
}
