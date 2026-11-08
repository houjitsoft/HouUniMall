package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName queryPurchaseGoodsDetailsVO
 * @Description
 * @Author Amos
 * @Date 2024/7/25 18:04
 * @Version 1.0
 * @author Amos
 */

@Data
public class queryPurchaseGoodsDetailsVO {
    /**
     * 订单编号，唯一标识一个订单。
     */
    private String orderId;

    /**
     *供应商名称，表示订单所属的供应商商店。
     */
    private String shopName;

    /**
     * 商品编号，唯一标识一个商品。
     */
    private String goodsId;

    /**
     * 商品名称，表示订单中购买的商品名称。
     */
    private String goodsName;

    /**
     * 订单中商品的数量。
     */
    private String orderGoodsNum;

    /**
     * 商品单价，表示每个商品的售价。
     */
    private BigDecimal goodsPrice;


    /**
     * 订货编码
     */
    private String goodsCode;

    /**
     * 发货状态 0待发货 1部分发货 2发货完成
     */
    private Integer isApproval;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除,4定时发布，5放入仓库
     */
    private Integer commonState;

    /**
     * 订单状态，表示订单的当前状态，如待支付、已支付、已发货等。
     */
    private String orderStatus;

    /**
     *发货数量
     */
    private Integer shipmentQuantity;

    /**
     * 含税金额  *  下单数量 - 经销折扣金额
     */
    private BigDecimal totalTaxInclusiveAmount;
    /**
     * 产品规格
     */
    private String orderSpecChineseValue;
    /**
     * 单位
     */
    private String dw;
    /**
     * 含税单价
     */
    private BigDecimal goodsTaxPrice;
    /**
     * 未税单减
     */
    private BigDecimal goodsWeiPrice;
    /**
     * 含税总额
     */
    private BigDecimal orderPaymentAmount;
    /**
     * 税率
     */
    private BigDecimal taxRate;
    /**
     * 1：会员价，2：阶梯价，3：询价，4：限时折扣，5：限时秒杀，6：满减赠，7代下单，8：满减折，9：竞价，10：组合
     */
    private Integer hyOrStep;
}
