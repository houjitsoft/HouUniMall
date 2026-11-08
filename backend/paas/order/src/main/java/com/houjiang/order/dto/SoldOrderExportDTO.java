package com.houjiang.order.dto;

import com.houjiang.commons.util.excel.ExcelCell;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 已售订单导出实体类
 *
 * @author Amos
 */
@Data
public class SoldOrderExportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商户订单id
     */
    @ExcelCell(index = 1)
    private String orderId;

    /**
     * 买家名称
     */
    @ExcelCell(index = 2)
    private String buyerUserName;

    /**
     * 收货人名称
     */
    @ExcelCell(index = 3)
    private String orderReceiverName;

    /**
     * 收货人联系方式
     */
    @ExcelCell(index = 4)
    private String orderReceiverContact;

    /**
     * 省
     */
    @ExcelCell(index = 6)
    private String province;

    /**
     * 市
     */
    @ExcelCell(index = 7)
    private String city;

    /**
     * 地区
     */
    @ExcelCell(index = 8)
    private String area;

    /**
     * 收货人联系地址
     */
    @ExcelCell(index = 9)
    private String orderReceiverAddress;

    /**
     * 运费
     */
    @ExcelCell(index = 11)
    private BigDecimal orderShippingFee;

    /**
     * 总付款额度
     */
    @ExcelCell(index = 12)
    private BigDecimal orderPaymentAmount;

    /**
     * 商品名
     */
    @ExcelCell(index = 13)
    private String goodsName;

    /**
     * 店铺id
     */
    @ExcelCell(index = 14)
    private String shopId;

    /**
     * 规格
     */
    @ExcelCell(index = 15)
    private String orderSpecChineseValue;

    /**
     * 商品价格
     */
    @ExcelCell(index = 16)
    private BigDecimal goodsPrice;

    /**
     * 订单商品数量
     */
    @ExcelCell(index = 17)
    private Integer orderGoodsNum;

    /**
     * 优惠价格
     */
    @ExcelCell(index = 18)
    private BigDecimal orderDiscountFee;

    /**
     * 下单时间
     */
    @ExcelCell(index = 19)
    private String orderCreateTime;

    /**
     * 付款时间
     */
    @ExcelCell(index = 20)
    private String paymentTime;

    /**
     * 买家留言
     */
    @ExcelCell(index = 21)
    private String orderMessage;

    /**
     * 卖家备注
     */
    @ExcelCell(index = 22)
    private String orderSellerMessage;

    /**
     * 订单状态名称
     */
    private String orderStatusName;

    /**
     * 订单状态 1.待付款 2.已付款 3.待发货 4.已发货
     * 5.已签收 6.确认收货 7.取消 8.退款中 9.已退款 11.待自提
     * 12 虚拟订单已过期
     */
    private Integer orderStatus;
    /**
     * 退款状态:0是无退款,1是退款中,2是退款完成,3退款不通过
     */
    private Integer orderRefundStatus;
    /**
     * 退货状态:0是无退货,1是退货中,2是退货完成，3退货不通过
     */
    private Integer orderReturnStatus;

    /**
     * 发货时间
     */
    private String orderShippingTime;

    /**
     * 订单完成时间
     */
    private String orderFinishedTime;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 店铺名
     */
    private String shopName;

}