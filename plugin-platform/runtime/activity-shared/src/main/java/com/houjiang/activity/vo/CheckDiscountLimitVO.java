package com.houjiang.activity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName CheckDiscountLimitEntity
 * @Description
 * @Author Amos
 * @Date 2022/10/18 15:18
 * @Version 1.0
 * @author Amos
 */
@Data
public class CheckDiscountLimitVO {
    /**
     * 购买上限0为不限制
     */
    private Integer goodsUpperLimit;
    /**
     * 购买下限 起订量
     */
    private Integer goodsLowerLimit;
    /**
     *每种商品限购 限购天数
     */
    private Integer discountUpperTime;
    /**
     *价格
     */
    private BigDecimal discountPrice;
    /**
     * 当前用户规定时间内已购买数量
     */
    private Integer buyNum;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * id
     */
    private Integer discountId;
}
