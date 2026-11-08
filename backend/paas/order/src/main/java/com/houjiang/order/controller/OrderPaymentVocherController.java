package com.houjiang.order.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.PaymentVocherDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName OrderPaymentVocherController
 * @Description 支付凭证
 * @Author Amos
 * @Date 2023/6/26 14:43
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/paymentVocher")
public class OrderPaymentVocherController extends BaseController{

    /**
     * 添加支付凭证
     *
     * @param param
     * @return
     */
    @PostMapping("/insert")
    public ResponseResult insertPaymentVocher(@RequestBody PaymentVocherDTO param) {
        ResponseResult result = orderPaymentVocherService.insertPaymentVocher(param);
        return result;
    }

    /**
     * 根据orderId 获取已经上传的支付凭证信息
     *
     * @param param
     * @return
     */
    @PostMapping("/query")
    public ResponseResult querkVocherImages(@RequestBody PaymentVocherDTO param) {
        ResponseResult result = orderPaymentVocherService.querkVocherImages(param);
        return result;
    }

    /**
     * 订单支付（BFF 层 payOrder 对应接口）
     * 校验订单 + 返回供 payment 服务使用的支付参数，不直接完成扣款。
     *
     * @param param 支付入参（orderId / token，可选 payCode / clientType）
     * @return 支付参数结果
     */
    @PostMapping("/payOrder")
    public ResponseResult payOrder(@RequestBody Map<String, Object> param) {
        return orderPaymentVocherService.payOrder(param);
    }
}
