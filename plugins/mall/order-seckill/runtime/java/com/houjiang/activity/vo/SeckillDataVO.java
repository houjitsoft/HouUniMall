package com.houjiang.activity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName DiscountDataEntity
 * @Description
 * @Author Amos
 * @Date 2022/11/2 15:33
 * @Version 1.0
 * @author Amos
 */

@Data
public class SeckillDataVO {
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 规格名称
     */
    private String specName;
    /**
     * 付款商品件数
     */
    private Integer goodsNum;
    /**
     * 总成交额
     */
    private BigDecimal dealAmount;
    /**
     * 新客下单数
     */
    private Integer newerOrderNum;
    /**
     * 老客下单数
     */
    private Integer olderOrderNum;

}
