package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.service.PaymentBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * 余额支付 购物卡支付 controller
 * @author Amos
 */

@RestController
@RequestMapping("payment")
public class PaymentBalanceController {

    @Autowired
    private PaymentBalanceService paymentBalanceService;

    /**
     *线下支付上传凭证，审核通过
/**
     *
     * @param map
     * @return
     */
    @Transactional
    @RequestMapping("checkPaymentVocher")
    public ResponseResult checkPaymentVocher(@RequestBody Map<Object, Object> map){
        ResponseResult result = paymentBalanceService.checkPaymentVocher(map);
        return result;
    }
}
