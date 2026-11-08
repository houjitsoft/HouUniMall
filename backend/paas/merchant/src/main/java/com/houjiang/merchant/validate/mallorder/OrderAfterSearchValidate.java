package com.houjiang.merchant.validate.mallorder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class OrderAfterSearchValidate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "订单搜索类型")
    private Integer key_word_type;

    @Schema(description = "订单搜素关键词")
    private String key_word;

    @Schema(description = "商品搜素关键词")
    private String goods_name;

    @Schema(description = "订单来源")
    private Integer order_from;

    @Schema(description = "订单类型")
    private Integer type;

    @Schema(description = "开始时间")
    private String start_time;

    @Schema(description = "结束时间")
    private String end_time;

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "店铺ID")
    private Integer shopId;
}
