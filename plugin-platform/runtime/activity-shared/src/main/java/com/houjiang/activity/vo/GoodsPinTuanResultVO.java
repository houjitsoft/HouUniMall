package com.houjiang.activity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class GoodsPinTuanResultVO {
    /**
     * 拼团名称
     */
    private String name;
    /**
     * 成团人数
     */
    private Integer personNum;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 原价
     */
    private BigDecimal priceOri;
    /**
     * 拼团价
     */
    private BigDecimal price;
    /**
     * 商品主图
     */
    private String commonImage;
    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 二维码 base64
     */
    private String orCode;
    /**
     * app 二维码 base64
     */
    private String appOrCode;
    /**
     * 1为可用。 0 不可用
     */
    private Integer status;
    /**
     * 商品类型
     */
    private Integer goodsType;
    /**
     * 店铺id
     */
    private Integer shopId;

}
