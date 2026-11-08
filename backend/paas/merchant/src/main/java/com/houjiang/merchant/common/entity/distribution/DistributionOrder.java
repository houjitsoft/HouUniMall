package com.houjiang.merchant.common.entity.distribution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class DistributionOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "修改时间")
    private Long createTime;

    @Schema(description = "创建时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "买家id")
    private Integer userId;

    @Schema(description = "分销商id")
    private Integer userDistributionId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单商品ID")
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
    private Long settlementTime;

}
