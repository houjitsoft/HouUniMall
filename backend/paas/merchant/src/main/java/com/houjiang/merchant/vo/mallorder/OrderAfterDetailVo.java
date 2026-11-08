package com.houjiang.merchant.vo.mallorder;

import com.houjiang.merchant.vo.mall.MallOrderAfterSaleLogListedVo;
import com.houjiang.merchant.vo.mall.MallOrderGoodsListedVo;
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
public class OrderAfterDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "商家id")
    private Integer shopId;

    @Schema(description = "订单ID")
    private Integer orderId;
    @Schema(description = "订单商品id")
    private Integer orderGoodsId;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;
    @Schema(description = "售后单号")
    private String sn;


    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "订单商品ids")
    private String orderGoodsIds;

    @Schema(description = "整单退款 1是 0否 2未记录")
    private Integer isOrderAll;

    @Schema(description = "类型 0仅退款")
    private Integer type;

    @Schema(description = "类型 0仅退款")
    private String typeText;

    @Schema(description = "状态 0申请中 10等待退款 20售后成功 30 售后失败")
    private Integer status;

    @Schema(description = "状态 0申请中 10等待退款 20售后成功 30 售后失败")
    private String statusText;

    @Schema(description = "1售后中：买家发起售后，等待卖家同意 11售后中：卖家同意售后，等待卖家退款 21售后成功：售后退款成功 31售后失败：卖家拒绝售后 32售后失败：买家取消售后")
    private Integer subStatus;

    @Schema(description = "1售后中：买家发起售后，等待卖家同意 11售后中：卖家同意售后，等待卖家退款 21售后成功：售后退款成功 31售后失败：卖家拒绝售后 32售后失败：买家取消售后")
    private String subStatusText;
    @Schema(description = "退款原因 0其他 10 7天无理由退 20不喜欢不想要 30快递/物流未发货 40空包裹 50 货物破损已拒签")
    private Integer refundReason;
    @Schema(description = "退款原因")
    private String refundReasonText;

    @Schema(description = "退款说明")
    private String refundContent;

    @Schema(description = "退款图片")
    private List<String> refundImage;

    @Schema(description = "退款金额")
    private BigDecimal refundMoney;

    @Schema(description = "拒绝原因")
    private String refuseContent;


    @Schema(description = "寄回备注")
    private String returnRemark;

    @Schema(description = "用户")
    private UserVo user;

    @Schema(description = "是否可以取消")
    private Integer canCancel;

    @Schema(description = "是否可以重复申请")
    private Integer canReApply;

    @Schema(description = "订单详情")
    private OrderDetailVo orderInfo;

    @Schema(description = "订单商品")
    private List<MallOrderGoodsListedVo> orderGoods;
    @Schema(description = "操作日志")
    private List<MallOrderAfterSaleLogListedVo> log;

    @Schema(description = "可审核")
    private Integer canAudit;

    @Schema(description = "可退款")
    private Integer canRefund;

    @Schema(description = "商家可收货")
    private Integer shopCanReceive;

    @Schema(description = "商家可收货")
    private Integer shopCanDeal;

}
