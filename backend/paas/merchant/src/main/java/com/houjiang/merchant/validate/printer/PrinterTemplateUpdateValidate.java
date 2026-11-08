package com.houjiang.merchant.validate.printer;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import jakarta.validation.constraints.*;
import com.houjiang.merchant.common.validator.annotation.IDMust;

/**
 * 打印机模版参数
 * @author Amos
 */
@Data
@Schema(description = "打印机模版更新参数")
public class PrinterTemplateUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;


    @NotNull(message = "type参数缺失")
    @Schema(description = "类型 0商品订单")
    private Integer type;

    @NotNull(message = "name参数缺失")
    @Schema(description = "名称")
    private String name;

    @NotNull(message = "content参数缺失")
    @Schema(description = "模版设置")
    private JSONObject content;

}
