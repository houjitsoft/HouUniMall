package com.houjiang.activity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class GoodsPiecingTogetherDTO {
    /**
     * 拼团名称
     */
    private String name;
    /**
     *成团人数
     */
    private Integer personNum;
    /**
     * spuId
     */
    private Integer commonId;
    /**
     * skuId
     */
    private Integer goodsId;
    /**
     *原价
     */
    private BigDecimal priceOri;
    /**
     *拼团价
     */
    private BigDecimal price;
    /**
     *商品主图
     */
    private String commonImage;
    /**
     *商品名称
     */
    private String commonName;
    /**
     *结束时间
     */
    private String endTime;
    /**
     *开始时间
     */
    private String startTime;


}
