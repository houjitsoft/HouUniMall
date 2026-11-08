package com.houjiang.activity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName CheckDiscountLimitEntity
 * @Description
 * @Author Amos
 * @Date 2022/10/18 15:18
 * @Version 1.0
 * @author Amos
 */
@Data
public class CheckSeckillLimitVO {

    /**
     * 购买上限0为不限制
     */
    private Integer goodsUpperLimit;

    /**
     * 限时秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 开始时间
     */
    private Date goodsStartTime;

    /**
     * 结束时间
     */
    private Date goodsEndTime;

    /**
     * 当前活动已购买数量
     */
    private Integer buyNum;

    /**
     * 当前规格库存
     */
    private Integer seckillNum;

    /**
     * 所有规格总库存
     */
    private Integer seckillNums;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * id
     */
    private Integer seckillId;

}
