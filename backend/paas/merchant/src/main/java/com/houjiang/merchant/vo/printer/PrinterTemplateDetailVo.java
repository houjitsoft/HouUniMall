package com.houjiang.merchant.vo.printer;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class PrinterTemplateDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "类型 0商品订单")
    private Integer type;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "是否显示 0未使用 1已使用")
    private Integer printerUse;

    @Schema(description = "模版设置")
    private JSONObject content;


}
