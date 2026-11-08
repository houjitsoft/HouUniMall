package com.houjiang.goods.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 已售订单商品详情
 *
 * @author Amos
 */
@Data
public class GoodsDetailsDTO extends PageHelper {

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 商品名
     */
    private String goodsName;

    /**
     * 商品图
     */
    private String goodsImage;

    /**
     * 商品价格
     */
    private String goodsPrice;

    /**
     * 快递名
     */
    private String expressName;

    /**
     * 订单商品数量
     */
    private Integer orderGoodsNum;
    /**
     * 门店id
     */
    private Integer shopGateId;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 退款状态：0：无退款 1：退款中 2：退款完成  3：商家拒绝退款
     */
    private String goodsRefundStatus;

    /**
     * 退货数量
     */
    private String orderGoodsReturnnum;

    /**
     * 规格
     */
    private String orderSpecChineseValue;

    /**
     * 实付价格
     */
    private double orderGoodsPaymentAmount;

    /**
     * 优惠券金额
     */
    private BigDecimal voucherPrice;

    /**
     * 红包抵扣金额
     */
    private BigDecimal orderRptPrice;

    /**
     * 商品id
     */
    private String commonId;

    /**
     * 供应商商品价格
     */
    private BigDecimal supperGoodsPrice;

    /**
     * 应商订单总价 实付价 （含运费）
     */
    private BigDecimal supperPaymentAmount;

    /**
     * 供应商订单总价 (不含运费)
     */
    private BigDecimal supperOrderAmount;

    /**
     * 1：会员商品，2：阶梯价商品
     */
    private Integer hyOrStep;

    /**
     * 单位
     */
    private String dw;

    /**
     * 含税单价
     */
    private BigDecimal goodsTaxPrice;

    /**
     * 未税单价
     */
    private BigDecimal goodsWeiPrice;

    /**
     * 税率
     */
    private Double taxRate;

    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 店铺名
     */
    private String shopName;
    /**
     * 库存
     */
    private Integer goodsStock;
    /**
     * 规格
     */
    private String specValueName;

    /**
     * 场次
     */
    private Date sessionTime;

    private List<Map> specValues;

}
