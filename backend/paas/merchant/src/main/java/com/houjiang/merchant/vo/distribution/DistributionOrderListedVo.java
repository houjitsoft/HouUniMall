package com.houjiang.merchant.vo.distribution;

import com.houjiang.merchant.common.entity.mall.MallOrder;
import com.houjiang.merchant.common.entity.mall.MallOrderGoods;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class DistributionOrderListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

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
    private String ratio;

    @Schema(description = "单独佣金")
    private String aloneMoney;

    @Schema(description = "佣金")
    private String money;

    @Schema(description = "状态 0待结算 1已结算 2已失效")
    private Integer status;

    @Schema(description = "结算时间")
    private String settlementTime;

    private MallShop shop;

    private MallOrder orderInfo;

    private MallOrderGoods orderGoods;

    private User user;

    private User userDistribution;

    private String statusText;

    private String levelText;


}
