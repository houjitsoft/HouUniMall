package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.division.BatchDivisionRequest;
import com.houjiang.payment.dto.division.BatchDivisionResponse;
import com.houjiang.payment.dto.division.QueryDivisionRequest;
import com.houjiang.payment.dto.division.QueryDivisionResponse;
import com.houjiang.payment.dto.division.RefundAssureOrderRequest;
import com.houjiang.payment.dto.division.RefundAssureOrderResponse;
import com.houjiang.payment.service.api.DivisionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 分账模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/division")
@RequiredArgsConstructor
public class DivisionController {
    
    private final DivisionService divisionService;
    
    /**
     * 15. 批量分账
     * 用于对收款订单发起分账
     * 
     * 使用场景:
     * 1. yunst2isv渠道: 分账处理完成后会通过分账结果通知接口逐笔进行通知,业务系统需以异步通知中的分账结果为准
     * 2. allinpay渠道: 需要通过查询分账订单接口主动查询分账结果
     * 3. yunst2yhzh渠道: 实时响应分账结果
     * 
     * @param request 批量分账请求
     * @return 批量分账响应
     */
    @PostMapping("/batch")
    public BatchDivisionResponse batchDivision(@Valid @RequestBody BatchDivisionRequest request) {
        log.info("批量分账请求, chanNo:{}", request.getChanNo());
        return divisionService.batchDivision(request);
    }
    
    /**
     * 16. 分账结果通知
     * 接收第三方支付平台的分账异步通知
     * 
     * 注意事项:
     * 1. 一般有终态时才会主动通知(分账成功或分账失败)
     * 2. yunst2isv渠道分账处理完成后会逐笔进行通知
     * 3. 业务系统需以异步通知中的分账结果为准
     * 4. 必须返回小写"success"确认收到通知
     * 
     * @param httpRequest HTTP请求对象
     * @return 通知处理结果 success/fail
     */
    @PostMapping("/notify")
    public String divisionNotify(HttpServletRequest httpRequest) {
        log.info("收到分账结果通知");
        return divisionService.handleDivisionNotify(httpRequest);
    }
    
    /**
     * 17. 查询分账订单
     * 用于查询批量分账请求中单笔订单的分账结果
     * 
     * @param request 查询分账订单请求
     * @return 查询分账订单响应
     */
    @PostMapping("/query")
    public QueryDivisionResponse queryDivision(@Valid @RequestBody QueryDivisionRequest request) {
        log.info("查询分账订单请求, mchAssureOrderNo:{}, assureOrderId:{}", 
                request.getMchAssureOrderNo(), request.getAssureOrderId());
        return divisionService.queryDivision(request);
    }
    
    /**
     * 18. 分账撤销
     * 将已分账的资金从原分账订单的入金方退回到出金方
     * 
     * 注意事项:
     * 1. allinpay渠道无需上送channelExtra参数
     * 2. yunst2isv如不上送默认从收款人撤销
     * 3. yunst2yhzh必须上送channelExtra,且refundInfoList只允许上送一条
     * 4. 只有yunst2isv渠道有异步通知,其他渠道无通知
     * 
     * @param request 分账撤销请求
     * @return 分账撤销响应
     */
    @PostMapping("/refund")
    public RefundAssureOrderResponse refundAssureOrder(@Valid @RequestBody RefundAssureOrderRequest request) {
        log.info("分账撤销请求, chanNo:{}, refundMchOrderNo:{}, refundAmount:{}", 
                request.getChanNo(), request.getRefundMchOrderNo(), request.getRefundAmount());
        return divisionService.refundAssureOrder(request);
    }
    
    /**
     * 26. 分账撤销结果通知
     * 接收第三方支付平台的分账撤销异步通知
     * 
     * 注意事项:
     * 1. 返回的字符串必须是小写success
     * 2. 前后不能有空格和换行符
     * 3. 返回success表示处理成功,通企付不会再次通知
     * 4. 返回非success表示处理失败,通企付会重试
     * 5. 最多重试5次,间隔: 30s、90s、180s、300s、450s
     * 6. 成功和失败都会通知(state=2成功, state=3失败)
     * 
     * @param request HTTP请求对象
     * @return 通知处理结果 success/fail
     */
    @PostMapping("/notify/refundAssure")
    public String refundAssureNotify(HttpServletRequest request) {
        log.info("接收分账撤销结果通知");
        return divisionService.handleRefundAssureNotify(request);
    }
}
