package com.houjiang.order.dto;

import lombok.Data;

import java.util.List;

/***
 *
/**
 * @author Amos
 *
 */
@Data
public class CommentsListDTO {

    /**
     * token
     */
    private String token;

    /**
     * 商户订单id
     */
    private String orderId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 1 ios  2 pc 3 wap 4 android 5默认好评
     */
    private Integer evaluationFrom;

    /**
     * 发货速度评分
     */
    private Integer evaluationDeliverycredit;

    /**
     * 服务态度评分
     */
    private Integer evaluationServicecredit;

    /**
     * 描述相符评分
     */
    private Integer evaluationDesccredit;

    /**
     * 是否匿名评价 0 不匿名 1  匿名
     */
    private Integer isanonymous;

    /**
     * 商品参数集合
     */
    private List<CommentsDTO> goodsList;


}
