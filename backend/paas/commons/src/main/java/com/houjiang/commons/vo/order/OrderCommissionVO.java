package com.houjiang.commons.vo.order;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.TimeStampUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCommissionVO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 申请分享人id
     */
    private String shareUserId;

    /**
     * 申请分享人名称
     */
    private String shareUserName;

    /**
     * 申请分享人电话
     */
    private String shareUserMobile;

    /**
     * 一级推广用户id
     */
    private String tgOneUserId;

    /**
     * 一级推广用户名称
     */
    private String tgOneUserName;

    /**
     * 一级推广用户电话
     */
    private String tgOneUserMobile;

    /**
     * 二级推广用户id
     */
    private String tgTwoUserId;

    /**
     * 二级推广用户名称
     */
    private String tgTwoUserName;

    /**
     * 二级推广用户电话
     */
    private String tgTwoUserMobile;

    /**
     * 购买用户id
     */
    private String buyUserId;

    /**
     * 购买用户名称
     */
    private String buyUserName;

    /**
     * 购买用户电话
     */
    private String buyUserMobile;

    /**
     * 购买用户头像
     */
    private String buyUserAvatar;

    /**
     * 购买用户时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date buyUserTime;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 订单价格
     */
    private BigDecimal orderPrice;

    /**
     * 退款价格
     */
    private BigDecimal returnPrice;

    /**
     * 一级推广佣金比例（数字格式 运算需要自己/100）
     */
    private String oneTgRate;

    /**
     * 分享佣金比例（数字格式 运算需要自己/100）
     */
    private String fxRate;

    /**
     * 二级推广佣金比例（数字格式 运算需要自己/100）
     */
    private String twoTgRate;

    /**
     * 一级推广佣金
     */
    private BigDecimal oneTgCommission;

    /**
     * 二级推广佣金
     */
    private BigDecimal twoTgCommission;

    /**
     * 分享佣金
     */
    private BigDecimal fxCommission;

    /**
     * 一级退还推广佣金
     */
    private BigDecimal returnOneTgCommission;

    /**
     * 二级退还推广佣金
     */
    private BigDecimal returnTwoTgCommission;

    /**
     * 退还分享佣金
     */
    private BigDecimal returnFxCommission;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 支付时间
     */
    private String payTime;

    /**
     * 退还时间
     */
    private String returnTime;

    /**
     * 1会员微客佣金   2员工微客佣金
     */
    private Integer commissionType;

    /**
     * 购买用户是否分享用户下级  1是  2否
     */
    private Integer bindType;

    /**
     * 0未结算  1已结算
     */
    private Integer settleState;

    /**
     * 结算时间
     */
    private String settleTime;

    /**
     * 结算交易id
     */
    private String settleRecordId;

    /**
     * 分销结算给用户状态 0未结算 1已结算
     */
    private Integer fxSettlementStatus;

    /**
     * 分销结算给用户时间
     */
    private String fxSettlementTime;

    /**
     * 分享 1 推广 2
     */
    private Integer fxTg;

    /** 用户的绑定id */
    private String bindUserId;

    /**
     * 微客分销小店id
     */
    private Integer vkShopId;

    public OrderCommissionVO(String orderId, Integer orderStatus, BigDecimal returnPrice, BigDecimal returnOneTgCommission, BigDecimal returnTwoTgCommission, BigDecimal returnFxCommission, Integer goodsId) {
        this.orderId = orderId;
        this.goodsId = goodsId;
        this.orderStatus = orderStatus;
        this.returnPrice = returnPrice;
        this.returnOneTgCommission = returnOneTgCommission;
        this.returnTwoTgCommission = returnTwoTgCommission;
        this.returnFxCommission = returnFxCommission;
        this.returnTime = TimeStampUtil.getNowDate();
    }
}
