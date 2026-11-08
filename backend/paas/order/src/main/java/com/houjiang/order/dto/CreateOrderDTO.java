package com.houjiang.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName CreateOrderDTO
 * @Description
 * @Author Amos
 * @Date 2023/9/18 9:08
 * @Version 1.0
 * @author Amos
 */
@Data
public class CreateOrderDTO {

    Integer treasureOrderShopId;

    /**
     * 收货人姓名
     */
    String orderReceiverName;

    /**
     *收货人地址
     */
    String orderReceiverAddress;
    /**
     *收货人联系方式
     */
    String orderReceiverContact;
    /**
     *收货人省Id
     */
    Integer orderReceiverProvinceId;
    /**
     *收货人市Id
     */
    Integer orderReceiverCityId;
    /**
     *收货人区Id
     */
    Integer orderReceiverAreaId;

    /**
     * 付款码变量
     * 用于存储用户的付款码信息，以完成支付过程。
     * 选择使用Integer类型而不是String，是因为付款码通常是由数字组成的，
     * 使用Integer类型可以更有效地表示这些数字，并且在某些情况下可以利用整数运算的优化。
     */
    Integer payMentCode;



    Integer orderIsShopTake = 2;

    /**
     * 存储商品ID的列表。
     * 这个列表用于记录感兴趣的或需要进一步处理的商品ID。
     * 例如，它可以用于存储用户购物车中的商品ID，或者存储需要进行库存检查的商品ID。
     */
    List<Integer> goodsIds;


    /**
     * 订单来源
     */
    Integer orderFromNum;

    /**
     * 是否同城配送2-否 1-是
     */
    private Integer isDelivery;
    /**
     * 最终配送费
     */
    private BigDecimal fee;
    /**
     * 配送费
     */
    private BigDecimal pick;

    /**
     * 客户类型名称
     * 1、用家--嘉币，2、批发--劲豆，
     */
    private String customerTypeName;

    /**
     * 客户类型名称
     * 1、用家--嘉币，2、批发--劲豆，
     */
    private Integer customerType;


    /**
     * 外部
     */
    private Integer outShopId;

}
