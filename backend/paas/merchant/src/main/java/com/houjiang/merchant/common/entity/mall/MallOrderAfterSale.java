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
public class MallOrderAfterSale implements Serializable {

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

    @Schema(description = "售后单号")
    private String sn;

    @Schema(description = "商家id")
    private Integer shopId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "来源")
    @TableField(value = "`from`")
    private Integer fromFeild;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "订单商品id")
    private Integer orderGoodsId;

    @Schema(description = "订单商品ids")
    private String orderGoodsIds;

    @Schema(description = "整单退款 1是 0否 2未记录")
    private Integer isOrderAll;

    @Schema(description = "类型 0仅退款")
    private Integer type;

    @Schema(description = "状态 0申请中 10等待退款 20售后成功 30 售后失败")
    private Integer status;

    @Schema(description = "1售后中：买家发起售后，等待卖家同意 11售后中：卖家同意售后，等待卖家退款 21售后成功：售后退款成功 31售后失败：卖家拒绝售后 32售后失败：买家取消售后")
    private Integer subStatus;

    @Schema(description = "退款原因 0其他 10 7天无理由退 20不喜欢不想要 30快递/物流未发货 40空包裹 50 货物破损已拒签")
    private Integer refundReason;

    @Schema(description = "退款说明")
    private String refundContent;

    @Schema(description = "退款图片")
    private String refundImage;

    @Schema(description = "退款金额")
    private BigDecimal refundMoney;

    @Schema(description = "拒绝原因")
    private String refuseContent;

    @Schema(description = "结算 0未结算 1已结算")
    private Integer settlementIs;

    @Schema(description = "服务费率")
    private BigDecimal serviceRatio;

    @Schema(description = "寄回物流公司")
    private String returnExpressCompany;

    @Schema(description = "寄回物流单号")
    private String returnExpressNo;

    @Schema(description = "寄回凭证")
    private String returnImage;

    @Schema(description = "寄回备注")
    private String returnRemark;

    @Schema(description = "寄回时间")
    private Long returnTime;
}