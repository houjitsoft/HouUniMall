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

import java.util.Map;

/** Plugin Market 调用订单服务的出站适配器；契约仍由稳定 DTO 承载。 */
@FeignClient(name = "order", url = "${feign.order.url:}", contextId = "pluginMarketOrderPaasFeign")
public interface OrderPaasFeign {

    @PostMapping("/order/mall/payment/create")
    ResponseResult<MallCashierCreateResponse> createMallCashier(@RequestBody MallCashierCreateRequest request);

    @PostMapping("/order/mall/payment/status")
    ResponseResult<MallCashierStatusResponse> mallCashierStatus(@RequestParam("cashierNo") String cashierNo,
                                                                 @RequestParam("userId") String userId);

    @PostMapping("/order/mall/payment/refund")
    ResponseResult<?> refundMallPayment(@RequestBody MallRefundRequest request,
                                        @RequestParam("userId") String userId);

    @PostMapping("/order/mall/payment/balance")
    ResponseResult<String> mallBalance(@RequestParam("userId") String userId);

    @PostMapping("/order/base/queryOrders")
    ResponseResult queryOrders(@RequestBody Map<String, Object> param);
}
