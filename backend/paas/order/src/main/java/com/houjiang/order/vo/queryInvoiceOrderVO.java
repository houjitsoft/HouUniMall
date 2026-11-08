package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName queryPurchaseOrderVO
 * @Description
 * @Author Amos
 * @Date 2024/7/25 11:17
 * @Version 1.0
 * @author Amos
 */
@Data
public class queryInvoiceOrderVO {
        /**
         * 订单编号，唯一标识一个订单。
         */
        private String orderId;

        /**
         * 进货单号
         */
        private String purchaseNumber;

        /**
         *供应商名称，表示订单所属的供应商商店。
         */
        private String shopName;


        /**
         * 订单折扣金额，表示订单总金额的折扣部分。
         */
        private BigDecimal orderDiscountFee;

        /**
         * 订单运费，表示订单中支付的运输费用。
         */
        private BigDecimal orderShippingFee;

        /**
         * 订单实际支付金额，包括商品金额、折扣和运费等。
         */
        private BigDecimal orderPaymentAmount;

        /**
         * 订单状态，表示订单的当前状态，如待支付、已支付、已发货等。
         */
        private String orderStatus;

        /**
         * 订单创建时间，记录订单的创建时间。
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date orderCreateTime;

        /**
         * 发票状态，表示发票的开具状态，如未开具、已开具等。
         */
        private String invoiceStatus;


        /**
         * 用户id
         */
        private String userId;

        /**
         * 1.线上支付  2.线下支付 3.货到付款 4.账期支付
         */
        private String paymentCode;

        /**
         * 审核状态  0 待上传   1 待审核 2 已审核 3 审核驳回
         */
        private Integer isApproval;

        /**
         * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除,4定时发布，5放入仓库
         */
        private Integer commonState;

        /**
         * 商品详情
         */
        private List<queryPurchaseGoodsDetailsVO> goodsDetailsList;

        /**
         * 发票类型: 0-普票；1-增值税专用票
         */
        private String invoiceType;


        /**
         * 发票功能开关状态:0-关闭;1-开启
         */
        private String invoiceSwitchStatus;



}


