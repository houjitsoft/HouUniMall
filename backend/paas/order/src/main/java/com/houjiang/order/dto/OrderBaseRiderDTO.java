package com.houjiang.order.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @ClassName OrderBaseRiderDTO
 * @Description 骑手订单信息入参
 * @Author Amos
 * @Date 2024/4/29 14:35
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderBaseRiderDTO extends PageHelper {

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 买家账号
     */
    private String buyerUserName;

    /**
     * 订单状态 1.待付款  3.待发货 4.已发货  6.确认收货 7.取消 8.退款中 9.已退款 11.待自提\r\n12 虚拟订单已过期  16 部分发货
     */
    private Integer status;

    /**
     * 骑手删除
     */
    private Integer isDeliveryDel;

    /**
     * 是否绑定骑手
     */
    private String distributorId;

    /**
     * 0 待取货 1待配送
     */
    private Integer pickup;
}
