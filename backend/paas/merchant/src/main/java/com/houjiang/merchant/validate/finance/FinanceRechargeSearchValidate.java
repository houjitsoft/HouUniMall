package com.houjiang.merchant.validate.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class FinanceRechargeSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单编号")
    private String sn;

    @Schema(description = "关键词")
    private String user_info;

    @Schema(description = "支付方式")
    private Integer pay_way;

    @Schema(description = "支付状态")
    private Integer pay_status;

    @Schema(description = "开始时间")
    private String start_time;

    @Schema(description = "结束时间")
    private String end_time;

    @Schema(description = "导出信息")
    private Integer export;
    @Schema(description = "file_name")
    private String file_name;

    @Schema(description = "page_start")
    private Integer page_start;

    @Schema(description = "page_end")
    private Integer page_end;

    @Schema(description = "page_size")
    private Integer page_size;

    @Schema(description = "page_type")
    private Integer page_type;

}
