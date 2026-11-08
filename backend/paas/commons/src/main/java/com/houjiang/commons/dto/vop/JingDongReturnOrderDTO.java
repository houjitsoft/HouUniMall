package com.houjiang.commons.dto.vop;

import lombok.Data;

import java.util.List;

/**
 * @ClassName JingDongReturnOrderDTO
 * @Author Amos
 * @Date 2025/6/17 15:06
 * @Version 1.0
 * @author Amos
 */
@Data
public class JingDongReturnOrderDTO {
    /**
     * 商城订单号
     */
    private String orderId;
    /**
     * 1.退款 2.仅退款 3退货退款 4换货
     */
    private Integer returnMethodType;
    /**
     * 退单原因
     */
    private String returnReason;
    /**
     * 京东订单号
     */
    private String jdOrderId;
    /**
     * 京东 售后申请单号
     */
    private String jdReturenOrderId;
    /**
     * 京东 售后申请单号 集合
     */
    private List<String> jdReturenOrderIdList;
    /**
     * 退单商品信息
     */
    private List<JingDongReturnGoodsDTO> goodsList;
    /**
     * 取件地址信息
     */
    private OrderAddresDTO pickupAddress;
    /**
     * 返件地址信息
     */
    private OrderAddresDTO returnAddress;
    /**
     * 售后图片
     */
    private  List<String> voucher ;
    /**
     * 物流单号
     */
    private String returnShippingCode;
    /**
     * 物流名称
     */
    private String expressName;




}
