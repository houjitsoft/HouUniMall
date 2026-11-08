package com.houjiang.mall.feign;

import com.houjiang.commons.dto.payment.MallCashierCreateRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateResponse;
import com.houjiang.commons.dto.payment.MallCashierStatusResponse;
import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/** Mall 宿主调用订单服务的出站 Feign 适配器。 */
@FeignClient(name = "order", url = "${feign.order.url:}")
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

    /** PC 订单查询（order 侧对平台超管放开「仅本人」过滤，见 OrderBaseServiceImpl#queryOrdersByPC） */
    @PostMapping("/order/base/queryOrdersByPC")
    ResponseResult queryOrdersByPC(@RequestBody Map<String, Object> param);

    /**
     * 后台订单详情（按 orderId 查，不带用户过滤）。
     * 该路径不在 order 侧 JWT 白名单（body-token 免鉴权仅限 queryOrders/queryOrdersByPC），
     * 必须把调用方的 Authorization 头原样透传，否则被 30803 拒绝。
     */
    @PostMapping("/order/base/platForm/backGroundByOrderDetails")
    ResponseResult backGroundByOrderDetails(@RequestHeader("Authorization") String authorization,
                                            @RequestBody Map<String, Object> param);
}
