package com.houjiang.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * 商城（mall）分销内部事件客户端
 *
 * <p>订单在「确认收货/完成」时把订单事件推给商城分销模块，由商城按分销关系与分佣规则
 * 生成佣金。走内部端点 {@code /internal/distribution/**}，不带用户态，凭据是
 * {@code X-Internal-Job-Token}（Nacos {@code houcloud.plugin.internal-job-token}）。
 *
 * <p>本接口位于 {@code com.houjiang.order.feign}，已被
 * {@code @EnableFeignClients(basePackages = {"com.houjiang.order.feign", ...})} 扫描，
 * 无需改动启动类。
 *
 * @author Amos
 */
@FeignClient(name = "mall")
public interface MallDistributionClient {

    /**
     * 订单完成（确认收货）事件：商城侧登记/更新分销订单并生成佣金
     *
     * @param body  orderId / orderNo / shopId / buyerId / shareUserId / orderAmount
     * @param token 内部任务令牌
     * @return 处理结果（ok / generated / generatedAmount）
     */
    @PostMapping("/internal/distribution/order/finish")
    Map<String, Object> orderFinish(@RequestBody Map<String, Object> body,
                                    @RequestHeader("X-Internal-Job-Token") String token);

    /**
     * 下单事件：商城侧登记分销订单（未完成状态，只作预估收益展示，不产生佣金）
     *
     * @param body  orderId / orderNo / shopId / buyerId / shareUserId / orderAmount
     * @param token 内部任务令牌
     * @return 处理结果（ok / duplicate / distributorId）
     */
    @PostMapping("/internal/distribution/order/create")
    Map<String, Object> orderCreate(@RequestBody Map<String, Object> body,
                                    @RequestHeader("X-Internal-Job-Token") String token);

    /**
     * 订单失效（退款/关闭）事件：作废该订单已生成的待结算佣金
     */
    @PostMapping("/internal/distribution/order/cancel")
    Map<String, Object> orderCancel(@RequestBody Map<String, Object> body,
                                    @RequestHeader("X-Internal-Job-Token") String token);
}
