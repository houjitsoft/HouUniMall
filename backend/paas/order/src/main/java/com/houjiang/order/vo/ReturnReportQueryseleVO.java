package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.excel.ExcelCell;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class ReturnReportQueryseleVO implements Serializable {

    public static final long serialVersionUID = 1L;
    @ExcelCell(index = 1)
    /** 店铺名称@ */
    public String shopName;
    @ExcelCell(index = 2)
    /** 退货编号@ */
    public String returnCode;
    @ExcelCell(index = 3)
    /** 订单编号@ */
    public String orderNumber;
    @ExcelCell(index = 4)
    /** 退款金额@ */
    public Double returnCash;
    @ExcelCell(index = 5)
    /** 买家名称@ */
    public String buyerUserName;
    @ExcelCell(index = 6)
    /** 买家账号@ */
    public String buyerUserAccount;
    @ExcelCell(index = 7)
    /** 支付单号@ */
    public String paymentNumber;
    @ExcelCell(index = 8)
    /** @第三方支付平台交易号 */
    public String paymentOtherNumber;
    @ExcelCell(index = 9)
    /** @应付金额（商品实际支付金额 + 运费） */
    public Double orderPaymentAmount;
    @ExcelCell(index = 10)
    /** '支付方式1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付' */
    public String payCode;
    @ExcelCell(index = 10)
    /** @'1-新发起等待卖家审核 2-卖家审核通过 3-卖家审核不通过 4-卖家收到货物 5-平台审核通过', */
    public String returnState;
    @ExcelCell(index = 11)
    /** '1-退款申请 2-退货申请 3-虚拟退款', */
    public String returnType;
    @ExcelCell(index = 12)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /** 添加时间@ */
    public Date returnAddTime;
    @ExcelCell(index = 13)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /** 商家处理时间 */
    public Date returnShopTime;
    @ExcelCell(index = 14)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /** 退款完成时间 */
    public Date returnFinishTime;
    @ExcelCell(index = 15)
    /**  */
    public double tradePaymentAmount;

    @ExcelCell(index = 16)
    /**  */
    public double tradePaymentRechargeCard;

    @ExcelCell(index = 17)
    /**  */
    public double tradeThirdPartyAmount;

    private List<GoodsDetailsVO> goods;


}
