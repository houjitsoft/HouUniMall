package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName OfficialDistriPriceVO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 16:47
 * @Version 1.0
 * @author Amos
 */

@Data
public class OfficialDistriPriceVO {

    /**
     * 市场价
     */
    private BigDecimal officialDistriPrice;

    /**
     * 分销价格
     */
    private BigDecimal channelPrice;

    /**
     * 产品代码，用于内部标识和追踪产品
     */
    private String code;

    /**
     * 报价类型或次数，表示报价的版本或次数
     */
    private Integer quotation;

    /**
     * 零售价，产品面向终端消费者的销售价格
     */
    private BigDecimal retailPrice;

    /**
     * 商品SKU ID，唯一标识商品的库存单元
     */
    private String skuId;

    /**
     * 状态，表示产品当前的状态（如：上架、下架等）
     */
    private String status;
}
