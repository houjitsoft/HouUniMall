package com.houjiang.merchant.validate.pt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class PtSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "活动状态")
    private Integer audit_status;

    @Schema(description = "商品名称")
    private String goods_name;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "活动开始时间")
    private String start_time;
    @Schema(description = "活动结束时间")
    private String end_time;

    @Schema(description = "商店ID")
    private Integer shopId;
}
