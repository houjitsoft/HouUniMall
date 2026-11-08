package com.houjiang.order.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 退款订单详情
 * @author Amos
 */
@Data
public class BarterShippingInfoVO {

    /**
     * 换货id
     */
    @TableId(type = IdType.AUTO)
    private Integer orderBarterId;

    /**
     * 换货物流单号
     */
    private String barterShippingCode;

    /**
     * 换货公司id
     */
    private Integer barterPostCode;

    /**
     * 换货手机号
     */
    private String barterMobile;

    /**
     * 订单退款id
     */
    private Integer orderReturnId;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 退款编号
     */
    private String returnCode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 换货状态 0发出换货物流 1用户确认收货
     */
    private Integer barterStatus;

    /**
     * 收货时间
     */
    private Date deliveryTime;

    /**
     * 快递公司名称
     */
    private String expressName;
    /**
     * 物流公司名称
     */
    private String barterPostName;

}
