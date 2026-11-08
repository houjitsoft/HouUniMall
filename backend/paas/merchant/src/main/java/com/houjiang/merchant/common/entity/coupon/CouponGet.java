package com.houjiang.merchant.common.entity.coupon;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class CouponGet implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "优惠券id")
    private Integer couponId;

    @Schema(description = "使用开始时间")
    private Long startTime;

    @Schema(description = "使用结束时间")
    private Long endTime;

    @Schema(description = "状态 0未使用 1已使用 2已过期 3已作废")
    private Integer status;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "使用时间")
    private Long useTime;

}