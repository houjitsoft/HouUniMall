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
public class OrderAfterSaleLogCreateValidate implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "售后id")
    private Integer afterSaleId;

    @Schema(description = "操作人 0 系统 1 管理员  2 商家 3用户")
    private Integer operatorType;

    @Schema(description = "操作人id")
    private Integer operatorId;

    @Schema(description = "操作内容")
    private String content;
}
