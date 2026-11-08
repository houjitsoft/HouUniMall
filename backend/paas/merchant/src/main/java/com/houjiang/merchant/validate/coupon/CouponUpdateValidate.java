package com.houjiang.merchant.validate.coupon;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class CouponUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "发放限制 0不限数量 1限制数量")
    private Integer stock_type;

    @Schema(description = "发放数量")
    private Integer stock;

    @Schema(description = "已领取数量")
    private Integer has_get_num;

    @Schema(description = "已使用数量")
    private Integer has_use_num;

    @Schema(description = "使用时间 0固定日期 1领劵当日起 2领劵次日起")
    private Integer use_time_type;

    @Schema(description = "使用开始时间")
    private String use_start_time;

    @Schema(description = "使用结束时间")
    private String use_end_time;

    @Schema(description = "领劵当日起多少天内可用")
    private Integer use_current_day;

    @Schema(description = "领劵次日起多少天内可用")
    private Integer use_next_day;

    @Schema(description = "优惠券金额")
    private Integer money;

    @Schema(description = "使用方式 0无门槛 1订单满")
    private Integer condition_type;

    @Schema(description = "订单金额")
    private BigDecimal condition_order_money;

    @Schema(description = "领取方式 0用户领取 1系统发放")
    private Integer get_type;

    @Schema(description = "领取限制 0不限制 1限制领取 2每天限制领取")
    private Integer get_limit;

    @Schema(description = "限制领取数量")
    private Integer get_limit_num;

    @Schema(description = "每天限制领取数量")
    private Integer get_limit_day_num;

    @Schema(description = "适用商品 0全部可用 1部分可用 2部分不可用")
    private Integer use_goods_type;

    @Schema(description = "状态 0未开始 1进行中 2已结束")
    private Integer status;

    @Schema(description = "goods")
    private JSONArray goods;
}
