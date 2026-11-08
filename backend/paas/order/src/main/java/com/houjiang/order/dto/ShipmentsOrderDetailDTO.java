package com.houjiang.order.dto;

import com.houjiang.commons.util.excel.ExcelCell;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单详情
 * @author Amos
 */
@Data
public class ShipmentsOrderDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelCell(index = 1)
    private Integer id;

    /**
     * 订单号
     */
    @ExcelCell(index = 2)
    private String orderId;

    /**
     * 订单来源
     */
    @ExcelCell(index = 3)
    private Integer orderFrom;

    /**
     * 订单创建时间
     */
    @ExcelCell(index = 4)
    private String orderCreateTime;

    /**
     * 订单状态id
     */
    @ExcelCell(index = 5)
    private Integer orderStatus;

    /**
     * 订单状态名称
     */
    @ExcelCell(index = 6)
    private String orderStatusName;

    /**
     * 支付单号
     */
    @ExcelCell(index = 7)
    private String paymentNumber;

    /**
     * 支付方式
     */
    @ExcelCell(index = 8)
    private Integer paymentCode;

    /**
     * 支付方式
     */
    @ExcelCell(index = 9)
    private String paymentCodeName;

    /**
     * 支付时间
     */
    @ExcelCell(index = 10)
    private String paymentTime;

    /**
     * 配送公司id
     */
    @ExcelCell(index = 11)
    private String orderShippingExpressId;

    /**
     * 配送公司名字
     */
    @ExcelCell(index = 12)
    private String orderShippingExpressName;

    /**
     * 物流单号
     */
    @ExcelCell(index = 13)
    private String orderShippingCode;

    /**
     * 收货人姓名
     */
    @ExcelCell(index = 14)
    private String orderReceiverName;

    /**
     * 收货人详细地址
     */
    @ExcelCell(index = 15)
    private String orderReceiverAddress;

    /**
     * 收货人联系方式
     */
    @ExcelCell(index = 16)
    private String orderReceiverContact;

    /**
     * 订单应付金额
     */
    @ExcelCell(index = 17)
    private BigDecimal orderPaymentAmount;

    /**
     * 商品名称
     */
    @ExcelCell(index = 18)
    private String goodsName;

    /**
     * 商品数量
     */
    @ExcelCell(index = 19)
    private Integer orderGoodsNum;

    /**
     * 商品单价
     */
    @ExcelCell(index = 20)
    private BigDecimal goodsPrice;

    /**
     * 发货人名称
     */
    @ExcelCell(index = 21)
    private String orderSellerName;

    /**
     * 发货人地址
     */
    @ExcelCell(index = 22)
    private String orderSellerAddress;

    /**
     * 发货人手机号
     */
    @ExcelCell(index = 23)
    private String orderSellerContact;

    /**
     * 发货地址id
     */
    @ExcelCell(index = 24)
    private Integer shippingAddressId;

    /**
     * 商品id
     */
    @ExcelCell(index = 25)
    private Integer goodsId;

    /**
     * 发货数量
     */
    private Integer shipmentQuantity;

    /**
     * 发货状态 0待发货 1部分发货 2发货完成
     */
    private Integer shipmentStatus;
}
