package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class CommissionRateBeanVO {

    /**
     * 类目佣金
     */
    private BigDecimal catCommission;

    /**
     * 大宝占类目佣金的比例
     */
    private BigDecimal babyCommission;
}
