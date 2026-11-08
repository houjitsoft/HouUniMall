package com.houjiang.merchant.validate.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class DistributionOrderSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;


    @Schema(description = "用户搜索关键词")
    private String order_sn;

    @Schema(description = "买家信息")
    private String user;

    @Schema(description = "分销商信息")
    private String user_distribution;

    @Schema(description = "商品信息")
    private String goods_name;

    @Schema(description = "开始时间")
    private String start_time;

    @Schema(description = "结束时间")
    private String end_time;

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "店铺ID")
    private Integer shopId;

}
