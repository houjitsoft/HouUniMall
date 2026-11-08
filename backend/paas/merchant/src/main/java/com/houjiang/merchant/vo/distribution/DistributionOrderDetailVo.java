package com.houjiang.merchant.vo.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class DistributionOrderDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "买家id")
    private Integer userId;

    @Schema(description = "分销商id")
    private Integer userDistributionId;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "订单商品id")
    private Integer orderGoodsId;

    @Schema(description = "分销层级")
    private Integer level;

    @Schema(description = "分销比例")
    private BigDecimal ratio;

    @Schema(description = "单独佣金")
    private BigDecimal aloneMoney;

    @Schema(description = "佣金")
    private BigDecimal money;

    @Schema(description = "状态 0待结算 1已结算 2已失效")
    private Integer status;

    @Schema(description = "结算时间")
    private Integer settlementTime;


}
