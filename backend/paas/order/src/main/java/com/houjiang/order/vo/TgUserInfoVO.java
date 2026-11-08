package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName TgUserInfoVO
 * @Description 推广用户信息
 * @Author Amos
 * @Date 2023/8/22 11:58
 * @Version 1.0
 * @author Amos
 */
@Data
public class TgUserInfoVO {

    /**
     * 购买用户的ID
     */
    private String buyUserId;

    /**
     * 购买用户的名称
     */
    private String buyUserName;

    /**
     * 购买用户的头像URL
     */
    private String buyUserAvatar;

    /**
     * 用户绑定时间
     */
    private String bindTime;

    /**
     * 用户绑定类型
     */
    private String bindType;

    /**
     * 总佣金
     */
    private BigDecimal totalCommission;

    /**
     * 订单数量
     */
    private Integer orderNum;

    /**
     * 退货数量
     */
    private Integer returnNum;

    /**
     * 订单总价
     */
    private BigDecimal orderPrice;

    /**
     * 退货总价
     */
    private BigDecimal returnPrice;

    /**
     * 支付时间
     */
    private String payTime;

}
