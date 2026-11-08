package com.houjiang.payment.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.service.StripePaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName StripePaymentController
 * @Description stripe支付控制台
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/stripe/")
public class StripePaymentController {

    @Autowired
    private StripePaymentService stripePaymentService;


    /**
     * stripe支付
     * @param para
     * @return
     */
    @RequestMapping("/pay")
    public ResponseResult pay(@RequestBody Map<Object, Object> para) {
       return stripePaymentService.pay(para);
    }
    /**
     * stripe app 支付
     * @param para
     * @return
     */
    @RequestMapping("/payApp")
    public ResponseResult payApp(@RequestBody Map<Object, Object> para) throws StripeException {
        return stripePaymentService.payApp(para);
    }

    /**
     * stripe回调
     *
     * @param request
     */
    @RequestMapping("/stripeNotifyUrl")
    public ResponseEntity<String> stripeNotifyUrl(HttpServletRequest request) throws IOException, SignatureVerificationException {
        return stripePaymentService.stripeNotifyUrl(request);
    }

    /**
     * stripe 退款
     *
     * @param para
     */
    @RequestMapping("/stripeReturnAmount")
    public ResponseResult stripeReturnAmount(@RequestBody Map<Object, Object> para) {
        return stripePaymentService.stripeReturnAmount(para);
    }
}
