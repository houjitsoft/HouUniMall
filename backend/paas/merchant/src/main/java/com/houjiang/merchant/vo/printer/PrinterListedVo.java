package com.houjiang.merchant.vo.printer;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class PrinterListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "状态 1正常 0禁用")
    private Integer status;

    @Schema(description = "打印机品牌 0飞蛾 1易联云")
    private Integer brand;

    @Schema(description = "打印机品牌 0飞蛾 1易联云")
    private String brandText;

    @Schema(description = "商品订单打印设置")
    private JSONObject orderPrint;

    @Schema(description = "打印机设置")
    private JSONObject content;

}
