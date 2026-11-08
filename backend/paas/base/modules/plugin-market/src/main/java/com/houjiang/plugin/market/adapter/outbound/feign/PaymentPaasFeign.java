package com.houjiang.plugin.market.adapter.outbound.feign;

import com.houjiang.commons.dto.payment.MallCashierCreateRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateResponse;
import com.houjiang.commons.dto.payment.MallCashierStatusResponse;
import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Plugin Market → payment 收银台（mall 内部入口）。
 *
 * <p>禁止再走 order 的 {@code /order/mall/payment/*}；支付微服务才是资金与对账的权威源。
 */
@FeignClient(name = "payment", url = "${feign.payment.url:}", contextId = "pluginMarketPaymentPaasFeign")
public interface PaymentPaasFeign {

    @PostMapping("/api/payment/cashier/mall/create")
    ResponseResult<MallCashierCreateResponse> createMallCashier(@RequestBody MallCashierCreateRequest request);

    @PostMapping("/api/payment/cashier/mall/status")
    ResponseResult<MallCashierStatusResponse> mallCashierStatus(@RequestParam("cashierNo") String cashierNo);

    @PostMapping("/api/payment/cashier/mall/refund")
    ResponseResult<?> refundMallPayment(@RequestBody MallRefundRequest request);
}
