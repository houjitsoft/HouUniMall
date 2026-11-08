package com.houjiang.merchant.validate.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class CouponSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "领取限制类型: 1=用户领取, 2=商家赠送")
    private Integer get_type;

    @Schema(description = "优惠券搜索")
    private String name;

    @Schema(description = "开始时间")
    private String start_time;

    @Schema(description = "结束时间")
    private String end_time;

    @Schema(description = "订单状态")
    private Integer status;
}
