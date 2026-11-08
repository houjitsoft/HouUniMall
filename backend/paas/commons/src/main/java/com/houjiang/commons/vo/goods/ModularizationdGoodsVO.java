package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class ModularizationdGoodsVO {
    /**
     * 商品通用名称
     * 用于描述商品的普遍称呼，便于用户识别商品
     */
    private String commonName;

    /**
     * 商品通用图片地址
     * 用于展示商品的外观或包装，帮助用户更直观地了解商品
     */
    private String commonImage;

    /**
     * 商品ID
     * 唯一标识一个商品，用于后台管理和查询
     */
    private Integer goodsId;

    /**
     * 商品价格
     * 表示商品的售价，用于计算订单总额
     */
    private BigDecimal commonPrice;

    /**
     * 商品销售数量
     * 反映商品的受欢迎程度和销售情况
     */
    private Integer commonSalenum;

    /**
     * 商品上架状态
     * 标识商品是否已经上架销售，用于控制商品的显示和购买
     */
    private Integer goodsStart;

    /**
     * 活动状态
     * 表示商品是否参与某些特定活动，用于动态调整商品展示和价格
     */
    private Integer activityStatus;

    /**
     * 活动开始时间
     * 标识活动的起始时间，用于判断活动是否开始
     */
    private String startTime;

    /**
     * 活动结束时间
     * 标识活动的结束时间，用于判断活动是否已经结束
     */
    private String endTime;


    /**
     * 积分支付比例
     */
    private Double pointsRate;





    /**
     * 现金价格
     */
    private BigDecimal cashPrice;

}
