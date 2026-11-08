package com.houjiang.merchant.vo.mallorder;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.vo.mall.MallExpressCompanySingleDetailVo;
import com.houjiang.merchant.vo.mall.MallOrderGoodsListedVo;
import com.houjiang.merchant.vo.mall.MallOrderLogListedVo;
import com.houjiang.merchant.vo.mall.MallShopSimplifyDetailVo;
import com.houjiang.merchant.vo.pt.PtOpenDetailVo;
import com.houjiang.merchant.vo.user.UserVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class LogisticsDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "收货人")
    private String addressContact;

    @Schema(description = "收货人电话")
    private String addressMobile;

    @Schema(description = "收货人地址")
    private String addressContent;

    @Schema(description = "自动取消提醒时间")
    private String autoCancelRemainTime;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "售后按钮")
    private Integer canApplyRefund;
    @Schema(description = "取消按钮")
    private Integer canCancel;

    @Schema(description = "确认按钮")
    private Integer canConfirm;

    @Schema(description = "物流按钮")
    private Integer canSeeLogistics;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "下单用户")
    private String nickname;

    @Schema(description = "实付金额")
    private String payMoney;

    @Schema(description = "是否支付: [0=未支付, 1=已支付]")
    private Integer payIs;

    @Schema(description = "支付状态描述")
    private String payIsMsg;

    @Schema(description = "优惠券金额")
    private BigDecimal couponMoney;
    @Schema(description = "创建时间")
    private String createTime;
    /** 获取总优惠金额 */
    @Schema(description = "总优惠金额")
    private BigDecimal decMoney;
    @Schema(description = "配送方式")
    private Integer deliveryType;

    @Schema(description = "配送方式")
    private String deliveryTypeText;

    @Schema(description = "会员折扣总额")
    private BigDecimal discountMoney;

    @Schema(description = "收货时间")
    private String expressConfirmTime;

    @Schema(description = "已发货 0未发货 1已发货")
    private Integer expressIs;
    @Schema(description = "已发货 0未发货 1已发货")
    private String expressIsText;
    @Schema(description = "运费总额")
    private BigDecimal expressMoney;

    @Schema(description = "店铺id")
    private Integer shopId;
    @Schema(description = "店铺信息")
    private MallShopSimplifyDetailVo shopInfo;

    @Schema(description = "是否自动取消")
    private Object isAutoCancel;
    @Schema(description = "订单类型 0普通订单 10秒杀 20 拼团")
    private Integer type;

    @Schema(description = "订单类型 0普通订单 10秒杀 20 拼团")
    private String typeText;

    @Schema(description = "秒杀id")
    private Integer msId;

    @Schema(description = "应付金额")
    private BigDecimal needPayMoney;

    @Schema(description = "拼团id")
    private Integer ptId;

    @Schema(description = "拼团")
    private PtOpenDetailVo ptOpen;

    @Schema(description = "拼团开团id")
    private Integer ptOpenId;

    @Schema(description = "拼团参团id")
    private Integer ptOpenJoinId;

    @Schema(description = "支付时间")
    private String payTime;

    @Schema(description = "订单号")
    private String sn;
    @Schema(description = "状态 0待付款 1待发货 2待收货 3已完成 4已关闭")
    private Integer status;

    @Schema(description = "待发货")
    private String statusText;

    @Schema(description = "订单商品")
    private List<MallOrderGoodsListedVo> goods;

    @Schema(description = "售后截止时间")
    private Long afterSaleDeadline;

    @Schema(description = "取消时间")
    private String cancelTime;

    @Schema(description = "优惠券领取id")
    private Integer couponGetId;

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

    @Schema(description = "订单来源 1微信小程序")
    private Integer from;

    @Schema(description = "微信公众号")
    private String fromText;

    @Schema(description = "创建id 用于标识同一次提交的订单")
    private Integer createId;


    @Schema(description = "支付方式0未知 1余额 2微信 3支付宝")
    private Integer payWay;

    @Schema(description = "支付方式0未知 1余额 2微信 3支付宝")
    private String payWayText;

    @Schema(description = "支付流水号")
    private String payTransactionNo;

    @Schema(description = "总金额")
    private BigDecimal money;

    @Schema(description = "商品原价总额")
    private String goodsOriginMoney;

    @Schema(description = "商品购买价总额")
    private BigDecimal goodsMoney;

    @Schema(description = "商品总数")
    private Integer goodsNums;

    @Schema(description = "订单商品数 0未记录")
    private Integer orderGoodsNum;

    @Schema(description = "商品数")
    private Integer totalGoodsNum;

    @Schema(description = "发货时间")
    private String expressTime;

    @Schema(description = "物流公司")
    private Integer expressCompanyId;

    @Schema(description = "物流单号")
    private String expressNo;

    @Schema(description = "用户备注")
    private String userRemark;

    @Schema(description = "商家备注")
    private String shopRemark;

    @Schema(description = "结算状态 0 未结算 1已结算")
    private Integer settlementIs;

    @Schema(description = "服务费率")
    private BigDecimal serviceRatio;
    @Schema(description = "用户")
    private UserVo user;

    @Schema(description = "快递公司")
    private MallExpressCompanySingleDetailVo expressCompany;

    @Schema(description = "订单日志")
    private List<MallOrderLogListedVo> log;

    @Schema(description = "是否可以发货")
    private Integer canSend;

    @Schema(description = "物流错误信息")
    private String logisticsError;

    @Schema(description = "logistics")
    private Object logistics;

}
