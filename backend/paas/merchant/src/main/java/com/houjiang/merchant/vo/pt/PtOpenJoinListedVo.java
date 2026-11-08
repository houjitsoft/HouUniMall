package com.houjiang.merchant.vo.pt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.houjiang.merchant.vo.mallorder.MallOrderSimpleGoodsVo;
import com.houjiang.merchant.vo.mallorder.OrderDetailVo;
import com.houjiang.merchant.vo.user.UserVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class PtOpenJoinListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "拼团id")
    private Integer ptId;

    @Schema(description = "拼团开团id")
    private Integer ptOpenId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "订单商品id")
    private Integer orderGoodsId;

    @Schema(description = "是否团长 1是 0否")
    private Integer isOpen;

    @Schema(description = "已退款 1是 0否 拼团失败时退款记录")
    private Integer isRefund;

    @Schema(description = "用户")
    private UserVo user;

    @Schema(description = "订单商品")
    private MallOrderSimpleGoodsVo orderGoods;
    @Schema(description = "订单详情")
    @JsonProperty("orderInfo")
    private OrderDetailVo orderInfo;
}
