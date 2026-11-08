package com.houjiang.commons.vo.activity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName CheckDiscountLimitResultVO
 * @Description
 * @Author Amos
 * @Date 2022/10/18 15:18
 * @Version 1.0
 * @author Amos
 */
@Data
public class CheckSeckillLimitResultVO {
    private Integer goodsUpperLimit;
    private BigDecimal seckillPrice;
    private Date goodsStartTime;
    private Date goodsEndTime;
    private String goodsName;
    private Integer cancelTime;
    /**
     * id
     */
    private Integer seckillId;
}
