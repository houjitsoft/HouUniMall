package com.houjiang.order.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.Date;

/**
 * 月度报告
 * @author Amos
 */
@Data
public class MonthReportDTO extends UserTokenBeanDTO {

    /**
     * 支付(付款)时间
     */
    public Date paymentTime;

    /**
     * 支付(付款)开始时间
     */
    public String paymentTimeStart;

    /**
     * 支付(付款)结束时间
     */
    public String paymentTimeEnd;

    /**
     * 卖家店铺名称
     */
    public String shopName;

    /**
     * 订单状态
     */
    public Integer orderStatus;

    /**
     * 订单号
     */
    public String orderId;

    /**
     * 开始
     */
    public Integer start;

    /**
     * 限度
     */
    public Integer limit;

    /**
     * 类目名称
     */
    private String catName;

    /**
     * 发货时间
     */
    public String orderShippingTime;

    /**
     * 支付方式
     */
    public String payCode;

    /**
     * 开户名
     */
    public String bankAccountName;

    /**
     * 银行名
     */
    public String bankName;

    /**
     * 银行卡号
     */
    public String bankAccountNumber;

    /**
     * 明细创建时间
     */
    public String orderCreateTimeStart;


    /**
     * 明细创建时间
     */
    public String orderCreateTimeEnd;

}
