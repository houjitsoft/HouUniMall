package com.houjiang.merchant.validate.coupon;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class CouponSysSendValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "名称")
    @NotNull(message = "必须提交发放数量")
    @Min(value = 1, message = "至少发放1张")
    private Integer nums;

    @Schema(description = "发放用户id")
    @NotNull(message = "必须提交发放用户")
    private List<Integer> user_ids;

    @Schema(description = "获得类型")
    private Integer getType;
}
