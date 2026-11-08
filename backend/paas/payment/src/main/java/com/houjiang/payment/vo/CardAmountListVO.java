package com.houjiang.payment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 卡金额集合
 * @author Amos
 */
@Data
public class CardAmountListVO {

    /**
     * 卡片激活码
     */
    private String cardCode;

    /**
     * 充值卡金额
     */
    private String cardMoney;

    /**
     * 卡片id
     */
    private String cardId;

    /**
     * 激活时间或充值时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cardFetchTime;

    /**
     * 领卡人Id
     */
    private String userId;

    /**
     * 领卡人账号
     */
    private String userAccount;

    /**
     * 卡片描述
     */
    private String cardDesc;

    /**
     * 卡的有效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cardStartTime;

    /**
     * 卡的有效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cardEndTime;

    /**
     * 购物卡密码
     */
    private String cardPassword;

}
