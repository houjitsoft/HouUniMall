package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.trade.*;
import com.houjiang.payment.service.api.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 交易模块Controller
 * 按照文档规范重构的统一支付接口
 * 
 * 接口列表：
 * 1. 小程序下单
 * 2. 统一下单 (API)
 * 3. 获取小程序链接
 * 4. 统一订单查询
 * 5. 关闭支付订单
 * 6. 统一退款
 * 7. 支付通知
 * 8. 查询退款订单
 * 9. 退款通知
 * 10. 账单导入
 * 11. 对账文件下载
 * 12. 统一补偿查询
 * 13. 提现
 * 14. 提现结果通知
 * 15. 批量分账
 * 16. 分账结果通知
 * 17. 查询分账订单
 * 18. 分账撤销接口
 * 19. 分期利息计算
 * 20. 转账接口
 * 21. 关闭小程序链接
 * 22. 快捷支付帮卡签约
 * 23. 短信支付确认
 * 24. 重新获取支付确认短信
 * 25. 快捷解绑
 * 26. 分账撤销结果通知
 * 27. H5收银台下单
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    /**
     * 2. 统一下单 (API)
     * 支持微信、支付宝等多种支付方式的统一下单接口
     * 
     * @param request 下单请求参数
     * @return 下单响应结果
     */
    @PostMapping("/unifiedOrder")
    public UnifiedOrderResponse unifiedOrder(@Valid @RequestBody UnifiedOrderRequest request) {
        log.info("统一下单请求, mchOrderNo:{}, wayCode:{}, amount:{}", 
                request.getMchOrderNo(), request.getWayCode(), request.getAmount());
        return tradeService.unifiedOrder(request);
    }

    /**
     * 4. 统一订单查询
     * 查询订单的支付状态
     * 
     * @param request 查询请求参数
     * @return 订单状态信息
     */
    @PostMapping("/queryOrder")
    public QueryOrderResponse queryOrder(@Valid @RequestBody QueryOrderRequest request) {
        log.info("查询订单状态, mchOrderNo:{}, payOrderId:{}", 
                request.getMchOrderNo(), request.getPayOrderId());
        return tradeService.queryOrder(request);
    }

    /**
     * 5. 关闭支付订单
     * 关闭超时或用户取消的订单
     * 
     * @param request 关闭订单请求
     * @return 关闭结果
     */
    @PostMapping("/closeOrder")
    public CloseOrderResponse closeOrder(@Valid @RequestBody CloseOrderRequest request) {
        log.info("关闭支付订单, mchOrderNo:{}", request.getMchOrderNo());
        return tradeService.closeOrder(request);
    }

    /**
     * 6. 统一退款
     * 发起退款申请
     * 
     * @param request 退款请求参数
     * @return 退款响应结果
     */
    @PostMapping("/refund")
    public RefundResponse refund(@Valid @RequestBody RefundRequest request) {
        log.info("发起退款, mchOrderNo:{}, mchRefundNo:{}, refundAmount:{}", 
                request.getMchOrderNo(), request.getMchRefundNo(), request.getRefundAmount());
        return tradeService.refund(request);
    }

    /**
     * 8. 查询退款订单
     * 查询退款的处理状态
     * 
     * @param request 查询退款请求
     * @return 退款状态信息
     */
    @PostMapping("/queryRefund")
    public QueryRefundResponse queryRefund(@Valid @RequestBody QueryRefundRequest request) {
        log.info("查询退款状态, refundOrderId:{}, mchRefundNo:{}", 
                request.getRefundOrderId(), request.getMchRefundNo());
        return tradeService.queryRefund(request);
    }

    /**
     * 7. 支付通知
     * 接收第三方支付平台的异步通知
     * 
     * @param httpRequest HTTP请求对象
     * @return 通知处理结果
     */
    @PostMapping("/payNotify")
    public String payNotify(HttpServletRequest httpRequest) {
        log.info("接收支付通知");
        return tradeService.handlePayNotify(httpRequest);
    }

    /**
     * 9. 退款通知
     * 接收第三方支付平台的退款异步通知
     * 
     * @param httpRequest HTTP请求对象
     * @return 通知处理结果
     */
    @PostMapping("/refundNotify")
    public String refundNotify(HttpServletRequest httpRequest) {
        log.info("接收退款通知");
        return tradeService.handleRefundNotify(httpRequest);
    }
}
