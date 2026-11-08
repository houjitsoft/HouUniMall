package com.houjiang.merchant.validate.mallorder;

import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class OrderAfterGoodsReceiveValidate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "售后单id")
    @NotNull(message = "Id必填")
    private Integer id;

    @Schema(description = "是否同意")
    @IntegerContains(values = {1, 0}, message = "同意售后状态格式错误")
    private Integer agree;

    @Schema(description = "拒绝内容")
    private String refuseContent;
}
