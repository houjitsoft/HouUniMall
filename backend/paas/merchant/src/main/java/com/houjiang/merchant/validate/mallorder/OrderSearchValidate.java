package com.houjiang.merchant.validate.mallorder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class OrderSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单搜索类型")
    private Integer key_word_type;

    @Schema(description = "订单搜素关键词")
    private String key_word;

    @Schema(description = "用户搜索类型")
    private Integer buyer_key_word_type;

    @Schema(description = "用户搜索关键词")
    private String buyer_key_word;

    @Schema(description = "订单来源")
    private Integer from;

    @Schema(description = "支付方式")
    private Integer pay_way;

    @Schema(description = "搜索时间类型")
    private String time_type;

    @Schema(description = "开始时间")
    private String start_time;

    @Schema(description = "结束时间")
    private String end_time;

    @Schema(description = "订单状态")
    private Integer status;
}
