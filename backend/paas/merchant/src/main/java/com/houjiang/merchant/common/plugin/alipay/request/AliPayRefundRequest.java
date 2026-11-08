package com.houjiang.merchant.common.plugin.alipay.request;

import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class AliPayRefundRequest implements Serializable {

    private static final long serialVersionUID = -1L;

    /** 订单流水号: (必须) */
    private String transactionId;

    /** 订单单号: (必须) */
    private String outTradeNo;

    /** 退款单号: (必须) */
    @SerializedName("out_refund_no")
    private String outRefundNo;

    /** 退款金额 : (必须)*/
    private BigDecimal refundAmount;

    /** 订单总额: (必须) */
    private BigDecimal totalAmount;

    /** 退款原因: 非必须  */
    private String reason;

    /** 通知接口: 非必须 */
    private String notifyUrl;

    /** 退款币种: 非必须 */
    private String currency;

    /** 商品信息: 非必须 */
    private List<WxPayRefundV3Request.GoodsDetail> goodsDetails;

    /** 子商户号: 非必须 */
    private String subMchid;

}
