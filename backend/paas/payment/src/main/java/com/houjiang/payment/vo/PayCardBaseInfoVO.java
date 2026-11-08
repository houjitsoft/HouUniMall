package com.houjiang.payment.vo;

import lombok.Data;

/**
 * 支付卡信息
 * @author Amos
 */
@Data
public class PayCardBaseInfoVO {

    /**
     * 卡id
     */
    private Integer cardId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 卡密码
     */
    private String cardPassword;

    /**
     * 卡片激活码
     */
    private String cardCode;

    /**
     * 激活时间或充值时间
     */
    private String cardFetchTime;

    /**
     * 媒体id
     */
    private Short cardMediaId;

    /**
     * 服务器id
     */
    private String serverId;

    /**
     * 用户账户
     */
    private String userAccount;

    /**
     * 卡时间
     */
    private String cardTime;

    /**
     * 卡片冻结金额
     */
    private String cardFrozeMoney;

    /**
     * 卡描述
     */
    private String cardDesc;

    /**
     * 卡金额
     */
    private double cardMoney;

    /**
     * 卡开始时间
     */
    private String cardStartTime;

    /**
     * 卡数量
     */
    private int cardNum;

    /**
     * 卡名称
     */
    private String cardName;

}
