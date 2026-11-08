package com.houjiang.payment.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付卡信息
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayCardInfoVO {

    /**
     * 卡片激活码
     */
    private String cardCode;

    /**
     * 卡片密码
     */
    private String cardPassword;

    /**
     * 卡片id
     */
    private String cardId;

    /**
     * 领奖时间
     */
    private String cardFetchTime;

    /**
     * 媒体id
     */
    private String cardMediaId;

    /**
     * 服务器id
     */
    private String serverId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用戶名
     */
    private String userAccount;

    /**
     * 卡片
     */
    private String cardTime;

    /**
     * 卡片金额
     */
    private String cardMoney;

    /**
     * 卡片冻结金额
     */
    private String cardFrozeMoney;

    /**
     * 时间类型
     */
    private Integer timeType;


}
