package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 批量发货查找代发货订单商品
 *
 * @author Amos
 */
@Data
public class ManageOrderbaseListVO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 订单id
     */
    private String orderGoodsId;

    /**
     * 购买人名称
     */
    private String buyerUserName;

    /**
     * 订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderDate;

    /**
     * 收货人姓名
     */
    private String orderReceiverName;

    /**
     * 收货人地址
     */
    private String orderReceiverAddress;

    /**
     * 收货人电话
     */
    private String orderReceiverContact;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品价格
     */
    private double orderGoodsAmount;

}