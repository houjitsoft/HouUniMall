package com.houjiang.merchant.validate.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class FinanceRefundSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "筛选类型: -1=全部, 0=退款中, 1=退款成功, 2=退款失败")
    private Integer refund_type;

    @Schema(description = "用户信息")
    private String user_info;

    @Schema(description = "退款单号")
    private String sn;

    @Schema(description = "来源单号")
    private String order_sn;

    private Integer refund_status;

    @Schema(description = "开始时间")
    private String start_time;

    @Schema(description = "结束时间")
    private String end_time;

}
