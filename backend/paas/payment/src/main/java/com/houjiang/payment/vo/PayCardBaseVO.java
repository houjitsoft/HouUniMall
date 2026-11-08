package com.houjiang.payment.vo;

import lombok.Data;

/**
 * 支付卡
 * @author Amos
 */
@Data
public class PayCardBaseVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 卡片里面的奖品
     */
    private String cardPrize;

    /**
     * 卡id
     */
    private String cardId;

    /**
     * 卡名称
     */
    private String cardName;

    /**
     * 卡描述
     */
    private String cardDesc;

    /**
     * 未用
     */
    private String unused;

    /**
     * 卡号
     */
    private String cardNum;

    /**
     * 使用数量
     */
    private String useCount;

    /**
     * 开卡结束时间
     */
    private String cardEndTime;

    /**
     * 开卡开始时间
     */
    private String cardStartTime;
}
