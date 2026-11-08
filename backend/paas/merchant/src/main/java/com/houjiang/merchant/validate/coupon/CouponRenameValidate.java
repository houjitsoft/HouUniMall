package com.houjiang.merchant.validate.coupon;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 优惠券重命名 / 调整发放数量
 */
@Data
@Schema(description = "优惠券重命名参数")
public class CouponRenameValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "优惠券ID")
    private Integer id;

    @NotBlank(message = "name参数缺失")
    @Schema(description = "优惠券名称")
    private String name;

    @Schema(description = "发放数量（固定发放时生效）")
    private Integer sendTotal;
}
