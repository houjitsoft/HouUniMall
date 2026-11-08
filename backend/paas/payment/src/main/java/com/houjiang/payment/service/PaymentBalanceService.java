package com.houjiang.payment.service;



import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface PaymentBalanceService {

    /**
     * 线下支付上传凭证，审核通过
     *
     * @param map
     * @return
     */
    ResponseResult checkPaymentVocher(Map<Object, Object> map);
}
