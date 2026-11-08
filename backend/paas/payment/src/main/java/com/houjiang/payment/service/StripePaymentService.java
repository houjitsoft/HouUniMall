package com.houjiang.payment.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.houjiang.commons.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName StripePaymentServer
 * @Author Amos
 * @Date 2024/12/18 13:00
 * @Version 1.0
 * @author Amos
 */
public interface StripePaymentService {
    /**
     * stripe支付
     * @param para
     * @return
     */
    ResponseResult pay(Map<Object, Object> para);

    /**
     *  stripe app 支付
     * @param para
     * @return
     */
    ResponseResult payApp(Map<Object, Object> para) throws StripeException;
    /**
     * stripe回调
     * @param request
     * @return
     * @throws IOException
     * @throws SignatureVerificationException
     */
    ResponseEntity<String> stripeNotifyUrl(HttpServletRequest request) throws IOException, SignatureVerificationException;

    /**
     *  stripe 退款
     * @param para
     * @return
     */
    ResponseResult stripeReturnAmount(Map<Object, Object> para);

}
