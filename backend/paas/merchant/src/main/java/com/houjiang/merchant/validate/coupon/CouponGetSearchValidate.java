package com.houjiang.merchant.validate.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class CouponGetSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "优惠券名称")
    private String coupon_mame;

    @Schema(description = "用户信息")
    private String user;

    @Schema(description = "领取开始时间")
    private String get_time_start;

    @Schema(description = "领取结束时间")
    private String get_time_end;

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "单位ID")
    private Integer shopId;
}
