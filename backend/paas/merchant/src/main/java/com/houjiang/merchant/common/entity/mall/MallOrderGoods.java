package com.houjiang.merchant.common.entity.mall;

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
public class MallOrderGoods implements Serializable {

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

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "商品id")
    private Integer goodsId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品图片")
    private String goodsImage;

    @Schema(description = "商品货号")
    private String goodsCode;

    @Schema(description = "sku id")
    private Integer goodsSkuId;

    @Schema(description = "商品规格值名称")
    private String goodsSkuValue;

    @Schema(description = "原价")
    private BigDecimal goodsOriginalPrice;

    @Schema(description = "购买价格")
    private BigDecimal goodsPrice;

    @Schema(description = "购买价格类型 0普通 1秒杀 2拼团")
    private Integer goodsPriceType;

    @Schema(description = "购买数量")
    private Integer goodsNum;

    @Schema(description = "总额")
    private BigDecimal money;

    @Schema(description = "商品购买价总额")
    private BigDecimal goodsMoney;

    @Schema(description = "商品原价总额")
    private BigDecimal goodsOriginMoney;

    @Schema(description = "运费总额")
    private BigDecimal expressMoney;

    @Schema(description = "折扣总额")
    private BigDecimal discountMoney;

    @Schema(description = "优惠券总额")
    private BigDecimal couponMoney;

    @Schema(description = "应付")
    private BigDecimal needPayMoney;

    @Schema(description = "实付")
    private BigDecimal payMoney;

    @Schema(description = "评论 0 否 1是 2不可评论")
    private Integer comment;

    @Schema(description = "售后 0 无售后 1售后处理中 2售后已处理")
    private Integer afterSale;

    @Schema(description = "售后id")
    private Integer afterSaleId;

    @Schema(description = "")
    private BigDecimal afterSaleMoney;

    @Schema(description = "记录内容")
    private String content;

}