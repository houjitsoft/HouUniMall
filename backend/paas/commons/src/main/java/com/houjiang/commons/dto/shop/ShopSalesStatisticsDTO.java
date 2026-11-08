package com.houjiang.commons.dto.shop;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 商店销售统计
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShopSalesStatisticsDTO extends PageHelper {

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 卡支付金额
     */
    private double cardPayment;

    /**
     * 余额支付
     */
    private double balancePayment;

    /**
     * 通联支付
     */
    private double communicationsPayment;

    /**
     * 微信10025054
     */
    private double weChatOnePayment;

    /**
     * 微信1498662282
     */
    private double weChatTwoPayment;

    /**
     * 支付宝支付
     */
    private double alipayPayment;

    /**
     * 代金券支付
     */
    private double voucherPrice;

    /**
     * 红包
     */
    private double orderRptPrice;

    /**
     * 运费
     */
    private double shippingFee;

    /**
     * 退款
     */
    private double refundMoney;

    /**
     * 合计
     */
    private double totalPayment;

    /**
     * 实付金额
     */
    private double orderGoodsPaymentAmount;


}
