package com.houjiang.merchant.validate.printer;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import jakarta.validation.constraints.*;

/**
 * @author Amos
 */
@Data
public class PrinterTemplateCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @NotNull(message = "类型缺失")
    @Schema(description = "类型 0商品订单")
    private Integer type;

    @NotNull(message = "名称缺失")
    @Schema(description = "名称")
    @Length(max = 255, message = "名称不能大于255字符")
    private String name;

/**
 * @NotNull(message = "是否显示参数缺失")
 * @Schema(description = "是否显示 0未使用 1已使用")
 * private Integer printerUse;
 */

    @NotNull(message = "模版设置参数缺失")
    @Schema(description = "模版设置")
    private JSONObject content;

}
