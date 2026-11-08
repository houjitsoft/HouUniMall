package com.houjiang.merchant.validate.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class CouponRecordValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "筛选类型: 0=未使用,1=已使用,2=已过期,3=已作废")
    private Integer type;

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "获取途径: 1=用户领取,2=商家发放")
    private Integer channel;

    @Schema(description = "发放人: 空=全部,0=系统,其它=管理员")
    private Integer issuer;

    @Schema(description = "领取开始时间")
    private Long startTime;

    @Schema(description = "领取结束时间")
    private Long endTime;

}
