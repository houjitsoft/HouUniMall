package com.houjiang.merchant.vo.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class CouponDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券ID")
    private Integer id;

    @Schema(description = "可删除")
    private Integer canDelete;

    @Schema(description = "可编辑")
    private Integer canEdit;

    @Schema(description = "可结束")
    private Integer canEnd;

    @Schema(description = "可开始")
    private Integer canStart;

    @Schema(description = "可系统发放")
    private Integer canSysSend;


    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "发放限制 0不限数量 1限制数量")
    private Integer stockType;

    @Schema(description = "发放限制 0不限数量 1限制数量")
    private Object stockTypeText;

    @Schema(description = "发放数量")
    private Integer stock;

    @Schema(description = "已领取数量")
    private Integer hasGetNum;

    @Schema(description = "已使用数量")
    private Integer hasUseNum;

    @Schema(description = "使用时间 0固定日期 1领劵当日起 2领劵次日起")
    private Integer useTimeType;

    @Schema(description = "使用时间 0固定日期 1领劵当日起 2领劵次日起")
    private String useTimeTypeText;

    @Schema(description = "使用开始时间")
    private String useStartTime;

    @Schema(description = "使用结束时间")
    private String useEndTime;

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

    @Schema(description = "领取方式 0用户领取 1系统发放")
    private String getTypeText;

    @Schema(description = "领取限制 0不限制 1限制领取 2每天限制领取")
    private Integer getLimit;

    @Schema(description = "限制领取数量")
    private Integer getLimitNum;

    @Schema(description = "每天限制领取数量")
    private Integer getLimitDayNum;

    @Schema(description = "适用商品 0全部可用 1部分可用 2部分不可用")
    private Integer useGoodsType;

    @Schema(description = "适用商品 0全部可用 1部分可用 2部分不可用")
    private String useGoodsTypeText;


    @Schema(description = "状态 0未开始 1进行中 2已结束")
    private Integer status;

    @Schema(description = "状态 0未开始 1进行中 2已结束")
    private String statusText;

    @Schema(description = "领取限制")
    private String getLimitText;

    @Schema(description = "有效期限")
    private String useTimeText;

    @Schema(description = "优惠券内容")
    private String couponContent;
    @Schema(description = "使用方式")
    private String conditionTypeText;

    @Schema(description = "优惠券使用率")
    private BigDecimal hasUseRatio;

    @Schema(description = "商品")
    private List<CouponGooodsVo> goods;

    @Schema(description = "可领取")
    private Integer canUserGet;

}
