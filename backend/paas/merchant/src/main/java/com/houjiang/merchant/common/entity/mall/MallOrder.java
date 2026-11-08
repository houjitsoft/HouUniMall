package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallOrder implements Serializable {

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

    @Schema(description = "创建id 用于标识同一次提交的订单")
    private Integer createId;

    @Schema(description = "订单号")
    private String sn;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "订单类型 0普通订单 10秒杀 20 拼团")
    private Integer type;

    @Schema(description = "秒杀id")
    private Integer msId;

    @Schema(description = "拼团id")
    private Integer ptId;

    @Schema(description = "拼团开团id")
    private Integer ptOpenId;

    @Schema(description = "拼团参团id")
    private Integer ptOpenJoinId;

    @Schema(description = "订单来源 1微信小程序")
    @TableField(value = "`from`")
    private Integer fromFeild;

    @Schema(description = "状态 0待付款 1待发货 2待收货 3已完成 4已关闭")
    private Integer status;

    @Schema(description = "已支付 0未支付 1已支付")
    private Integer payIs;

    @Schema(description = "支付方式0未知 1余额 2微信 3支付宝")
    private Integer payWay;

    @Schema(description = "支付时间")
    private Long payTime;

    @Schema(description = "支付流水号")
    private String payTransactionNo;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "联系人")
    private String addressContact;

    @Schema(description = "联系电话")
    private String addressMobile;

    @Schema(description = "联系地址")
    private String addressContent;

    @Schema(description = "配送方式 0快递")
    private Integer deliveryType;

    @Schema(description = "总金额")
    private BigDecimal money;

    @Schema(description = "商品原价总额")
    private String goodsOriginMoney;

    @Schema(description = "商品购买价总额")
    private BigDecimal goodsMoney;

    @Schema(description = "商品总数")
    private Integer goodsNums;

    @Schema(description = "运费总额")
    private BigDecimal expressMoney;

    @Schema(description = "会员折扣总额")
    private BigDecimal discountMoney;

    @Schema(description = "使用优惠券金额")
    private BigDecimal couponMoney;

    @Schema(description = "优惠券领取id")
    private Integer couponGetId;

    @Schema(description = "应付金额")
    private BigDecimal needPayMoney;

    @Schema(description = "实付金额")
    private BigDecimal payMoney;

    @Schema(description = "订单商品数 0未记录")
    private Integer orderGoodsNum;

    @Schema(description = "商品数")
    private Integer totalGoodsNum;

    @Schema(description = "已发货 0未发货 1已发货")
    private Integer expressIs;

    @Schema(description = "发货时间")
    private Long expressTime;

    @Schema(description = "物流公司")
    private Integer expressCompanyId;

    @Schema(description = "物流单号")
    private String expressNo;

    @Schema(description = "收货时间")
    private Long expressConfirmTime;

    @Schema(description = "发货内容")
    private String deliveryContent;

    @Schema(description = "自动发货自定义内容")
    private String deliveryContent1;

    @Schema(description = "自动发货类型 0固定内容 1自定义内容")
    private Integer deliveryContentType;

    @Schema(description = "微信小程序发货信息录入")
    private Integer wechatMiniExpressSync;

    @Schema(description = "录入时间")
    private Long wechatMiniExpressSyncTime;

    @Schema(description = "已重新发货 0否 1是")
    private Integer expressAgain;

    @Schema(description = "取消时间")
    private Long cancelTime;

    @Schema(description = "售后截止时间")
    private Long afterSaleDeadline;

    @Schema(description = "用户备注")
    private String userRemark;

    @Schema(description = "商家备注")
    private String shopRemark;

    @Schema(description = "结算状态 0 未结算 1已结算")
    private Integer settlementIs;

    @Schema(description = "服务费率")
    private BigDecimal serviceRatio;

    @Schema(description = "店铺详情")
    private String shopInfo;

}