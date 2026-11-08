package com.houjiang.userms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class VkUserListVO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userMobile;

    /**
     * 一级邀请人
     */
    private String oneInviter;

    /**
     * 二级邀请人
     */
    private String twoInviter;

    /**
     * 是否有上级
     */
    private Integer isSuperior;

    /**
     * 累计推广用户数
     */
    private Integer totalTgUserNum;

    /**
     * 累计结算订单数
     */
    private Integer totalSettlementOrderNum;

    /**
     * 累计推广商品数
     */
    private Integer totalTgGoodsNum;

    /**
     * 一级推广佣金
     */
    private BigDecimal totalOneTgCommission;

    /**
     * 二级推广佣金
     */
    private BigDecimal totalTwoTgCommission;

    /**
     * 累计分享佣金
     */
    private BigDecimal totalShareCommission;

    /**
     * 累计佣金
     */
    private BigDecimal totalCommission;

    /**
     * 已提现佣金
     */
    private BigDecimal withdrawnCommission;

    /**
     * 未提现佣金
     */
    private BigDecimal pendingWithdrawnCommission;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
