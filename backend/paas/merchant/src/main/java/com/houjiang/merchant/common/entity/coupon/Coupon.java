package com.houjiang.merchant.common.entity.coupon;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "发放限制 0不限数量 1限制数量")
    private Integer stockType;

    @Schema(description = "发放数量")
    private Integer stock;

    @Schema(description = "已领取数量")
    private Integer hasGetNum;

    @Schema(description = "已使用数量")
    private Integer hasUseNum;

    @Schema(description = "使用时间 0固定日期 1领劵当日起 2领劵次日起")
    private Integer useTimeType;

    @Schema(description = "使用开始时间")
    private Long useStartTime;

    @Schema(description = "使用结束时间")
    private Long useEndTime;

    @Schema(description = "领劵当日起多少天内可用")
    private Integer useCurrentDay;

    @Schema(description = "领劵次日起多少天内可用")
    private Integer useNextDay;

    @Schema(description = "优惠券金额")
    private Integer money;

    @Schema(description = "使用方式 0无门槛 1订单满")
    private Integer conditionType;

    @Schema(description = "订单金额")
    private BigDecimal conditionOrderMoney;

    @Schema(description = "领取方式 0用户领取 1系统发放")
    private Integer getType;

    @Schema(description = "领取限制 0不限制 1限制领取 2每天限制领取")
    private Integer getLimit;

    @Schema(description = "限制领取数量")
    private Integer getLimitNum;

    @Schema(description = "每天限制领取数量")
    private Integer getLimitDayNum;

    @Schema(description = "适用商品 0全部可用 1部分可用 2部分不可用")
    private Integer useGoodsType;

    @Schema(description = "状态 0未开始 1进行中 2已结束")
    private Integer status;

}