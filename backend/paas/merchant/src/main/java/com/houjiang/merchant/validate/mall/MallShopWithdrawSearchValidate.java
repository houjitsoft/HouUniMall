package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopWithdrawSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "开始时间")
    private String start_time;

    @Schema(description = "结束时间")
    private String end_time;
}
