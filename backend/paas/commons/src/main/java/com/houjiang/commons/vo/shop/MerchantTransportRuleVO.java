package com.houjiang.commons.vo.shop;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 区域
 * @author Amos
 */
@Data
public class MerchantTransportRuleVO {

    /**
     * id
     */
    private Long id;

    /**
     * 区域城市名称
     */
    private String areaName;

    /**
     * 区域城市id
     */
    private String areaIds;

    /**
     * 默认数量
     */
    private Float defaultNum;

    /**
     * 默认运费
     */
    private BigDecimal defaultPrice;

    /**
     * 增加数量
     */
    private Float addNum;

    /**
     * 增加价格
     */
    private BigDecimal addPrice;


}
