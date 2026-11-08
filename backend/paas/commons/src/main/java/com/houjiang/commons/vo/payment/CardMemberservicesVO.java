package com.houjiang.commons.vo.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 卡用户
 * @author Amos
 */
@Data
public class CardMemberservicesVO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 电话
     */
    private String userMobile;

    /**
     * 待结算金额
     */
    private double userMoneyPendingSettlement;

    /**
     * 用户资金
     */
    private double userMoney;

    /**
     * 冻结资金
     */
    private double userMoneyFrozen;

    /**
     * 购物卡余额
     */
    private double userRechargeCard;

    /**
     * 冻结卡资金
     */
    private double userRechargeCardFrozen;

    /**
     * 上次登陆时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date userLastloginTime;




}
