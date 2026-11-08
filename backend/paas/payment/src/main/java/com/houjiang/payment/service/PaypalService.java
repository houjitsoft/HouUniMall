package com.houjiang.payment.service;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.orders.Order;
import com.houjiang.payment.common.PaypalPaymentIntent;
import com.houjiang.payment.common.PaypalPaymentMethod;

import java.math.BigDecimal;

/**
 * @ClassName PaypalService
 * @Description Paypal接口服务
 * @Author Amos
 * @Date 2024/4/24 17:42
 * @Version 1.0
 * @author Amos
 */
public interface PaypalService {

    /**
     * 创建APP支付
     *
     * @param total        交易金额
     * @param currency     货币类型
     * @param cancelUrl    交易取消后跳转url
     * @param successUrl   交易成功后跳转url
     * @param mode         交易环境
     * @param clientId     交易客户id
     * @param clientSecret 交易标识
     * @return
     */
    Order createPaymentApp(BigDecimal total, String currency, String cancelUrl, String successUrl, String mode, String clientId, String clientSecret);

    /**
     * 创建PC支付方法
     *
     * @param total       交易金额
     * @param currency    货币类型
     * @param method      枚举-作用
     * @param intent      枚举-意图
     * @param description 交易描述
     * @param cancelUrl   交易取消后跳转url
     * @param successUrl  交易成功后跳转url
     * @return
     * @throws PayPalRESTException
     */
    Payment createPayment(BigDecimal total, String currency, PaypalPaymentMethod method, PaypalPaymentIntent intent, String description, String cancelUrl, String successUrl) throws PayPalRESTException;

    /**
     * 执行支付的方法(相当于提交事务)
     *
     * @param paymentId 支付ID
     * @param payerId   支付人ID
     * @return
     * @throws PayPalRESTException
     */
    Payment executePayment(String paymentId, String payerId) throws PayPalRESTException;

    /**
     * 退款
     *
     * @param tradeNo 第三方支付id
     * @param money   退款金额
     * @return
     */
    boolean refundSale(String tradeNo, BigDecimal money,String currencyCode);

    /**
     * 处理订单信息
     *
     * @param outTradeNo    订单号
     * @param transactionId 第三方支付id-便于退款
     */
    void notifyPay(String outTradeNo, String transactionId);
}
