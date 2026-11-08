package com.houjiang.base.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName goodsInfo.java
 * @Description TODO 小程序提交审核商品接口请求对象
 * @createTime 2021年08月19日 10:58:00
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GoodsInfoDTO {

    /**
     * 封面图片URL
     */
    private String coverImgUrl;

    /**
     * 名称
     */
    private String name;

    /**
     * 1：一口价（只需要传入price，price2不传）
     * 2：价格区间（price字段为左边界，price2字段为右边界，price和price2必传）
     * 3：显示折扣价（price字段为原价，price2字段为现价， price和price2必传）
     */
    private Integer priceType;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 价格2
     */
    private BigDecimal price2;

    /**
     * 地址
     */
    private String url;

    /**
     * 第三方申请
     */
    private String thirdPartyAppid;

}
