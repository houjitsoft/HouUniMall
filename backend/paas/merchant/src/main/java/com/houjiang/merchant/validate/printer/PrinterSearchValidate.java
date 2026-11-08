package com.houjiang.merchant.validate.printer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class PrinterSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "状态 1正常 0禁用")
    private Integer status;

    @Schema(description = "打印机品牌 0飞蛾 1易联云")
    private Integer brand;

    @Schema(description = "商品订单打印设置")
    private String orderPrint;

    @Schema(description = "打印机设置")
    private String content;

}
