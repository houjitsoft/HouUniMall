package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopDecorateCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IntegerContains(values = {0,1,2}, message = "装修类型错误")
    @NotNull(message = "店铺装修类型")
    @Schema(description = "类型 0店铺主页 1 客服 2底部导航")
    private Integer type;

    @NotNull(message = "店铺装修名称不能为空")
    @Schema(description = "店铺装修名称")
    private String name;

    @NotNull(message = "装修内容不能为空")
    @Schema(description = "装修内容")
    private String content;

    @Schema(description = "公共配置")
    private String common;

    @Schema(description = "店铺id")
    private Integer shopId;
}
