package com.houjiang.commons.vo.activity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName CheckDiscountLimitResultVO
 * @Description
 * @Author Amos
 * @Date 2022/10/18 15:18
 * @Version 1.0
 * @author Amos
 */
@Data
public class CheckDiscountLimitResultVO {
    private Integer goodsUpperLimit;
    private Integer discountUpperTime;
    private BigDecimal discountPrice;
    private String goodsName;
    /**
     * id
     */
    private Integer discountId;
}
