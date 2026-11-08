package com.houjiang.order.controller;

import com.houjiang.commons.dto.payment.MallPaymentNotifyRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateResponse;
import com.houjiang.commons.dto.payment.MallCashierStatusResponse;
import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.service.MallPaymentApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/order/mall/payment")
public class MallPaymentCallbackController {

    private final MallPaymentApplicationService paymentApplicationService;

    @PostMapping("/create")
    public ResponseResult<MallCashierCreateResponse> create(
            @RequestBody MallCashierCreateRequest request) {
        return ResponseResult.success(paymentApplicationService.createCashier(request));
    }

    @PostMapping("/status")
    public ResponseResult<MallCashierStatusResponse> status(
            @RequestParam("cashierNo") String cashierNo,
            @RequestParam("userId") String userId) {
        return ResponseResult.success(paymentApplicationService.queryStatus(cashierNo, userId));
    }

    @PostMapping("/refund")
    public ResponseResult<?> refund(@RequestBody MallRefundRequest request,
                                    @RequestParam("userId") String userId) {
        return ResponseResult.success(paymentApplicationService.refund(request, userId));
    }

    @PostMapping("/balance")
    public ResponseResult<String> balance(@RequestParam("userId") String userId) {
        return ResponseResult.success(paymentApplicationService.balance(userId));
    }

    @PostMapping("/notify")
    public ResponseResult<Void> paid(@Valid @RequestBody MallPaymentNotifyRequest request) {
        paymentApplicationService.handlePaid(request);
        return ResponseResult.success();
    }
}
