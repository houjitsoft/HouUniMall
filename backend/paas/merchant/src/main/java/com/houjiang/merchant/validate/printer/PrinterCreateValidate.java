package com.houjiang.merchant.validate.printer;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import jakarta.validation.constraints.*;

/**
 * @author Amos
 */
@Data
public class PrinterCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @NotNull(message = "名称缺失")
    @Schema(description = "名称")
    private String name;

    @NotNull(message = "brand参数缺失")
    @Schema(description = "打印机品牌 0飞蛾 1易联云")
    private Integer brand;

    @NotNull(message = "orderPrint参数缺失")
    @Schema(description = "商品订单打印设置")
    private JSONObject orderPrint;

    @NotNull(message = "content参数缺失")
    @Schema(description = "打印机设置")
    private JSONObject content;

}
