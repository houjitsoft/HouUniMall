package com.houjiang.order.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @ClassName GateQueryDeliveryOrdersDTO
 * @Description TODO
 * @Author Amos
 * @Date 2024/5/14 16:42
 * @Version 1.0
 * @author Amos
 */
@Data
public class GateQueryDeliveryOrdersDTO extends PageHelper {
    /**
     * 订单编号
     */
    private String orderId;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 门店id
     */
    private String shopGateId;
    /**
     * 买家名称
     */
    private String userName;
    /**
     * 门店名称
     */
    private String shopGateName;
    /**
     * 手机号
     */
    private String mobile;
    /**
     // 订单状态
/**
     * 订单状态
     */
    private Integer orderStatus;
    /**
     * 商家是否确认 0否 1是
     */
    private Integer shopAffirm;
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 0待取货 1已取货，待配送
     */
    private Integer pickup;
    /**
     * 骑手id
     */
    private Integer distributorId;
    /**
     * token
     */
    private String token;
    /**
     * 退款状态:0是无退款,1是退款中,2是退款完成,3退款不通过
     */
    private Integer orderRefundStatus;
    /**
     *  0 待配送  1 是待取货
     */
    private Integer type;

}
