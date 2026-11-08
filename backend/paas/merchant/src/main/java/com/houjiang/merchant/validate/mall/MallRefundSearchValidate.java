package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallRefundSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "退款单号")
    private String sn;

    @Schema(description = "来源单号")
    private String from_sn;
    @Schema(description = "用户昵称")
    private String nickname;
    @Schema(description = "退款类型")
    private Integer type;
    @Schema(description = "退款状态")
    private Integer status;
    @Schema(description = "开始时间")
    private String start_time;

    @Schema(description = "结束时间")
    private String end_time;
}
