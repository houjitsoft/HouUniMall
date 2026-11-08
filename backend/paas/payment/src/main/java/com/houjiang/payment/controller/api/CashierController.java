package com.houjiang.payment.controller.api;

import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.dto.payment.MallCashierCreateRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateResponse;
import com.houjiang.commons.dto.payment.MallCashierStatusResponse;
import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.payment.entity.PayRefundOrderEntity;
import com.houjiang.payment.service.cashier.MallCashierRefundService;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.cashier.CashierCreateRequest;
import com.houjiang.payment.dto.cashier.CashierCreateResponse;
import com.houjiang.payment.dto.cashier.CashierStatusResponse;
import com.houjiang.payment.service.api.CashierService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 统一收银台 Controller
 * 对应前端 /api/pay.js 的 cashierCreate/cashierStatus/cashierClose
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/cashier")
@RequiredArgsConstructor
public class CashierController {

    private final CashierService cashierService;
    private final MallCashierRefundService mallCashierRefundService;

    /**
     * 收银台下单
     * @param request 下单请求
     * @param http    请求上下文（用于提取 token）
     * @return 下单响应
     */
    @PostMapping("/create")
    public ResponseResult<CashierCreateResponse> create(@Valid @RequestBody CashierCreateRequest request,
                                                        HttpServletRequest http) {
        String userId = resolveUserId(http);
        if (!StringUtils.hasText(request.getClientIp())) {
            request.setClientIp(http.getRemoteAddr());
        }
        log.info("[cashier/create] userId={}, terminal={}, payMethod={}, amount={}",
                userId, request.getTerminal(), request.getPayMethod(), request.getAmount());
        return ResponseResult.success(cashierService.create(request, userId));
    }

    /** Internal mall/order-service entry. User identity is supplied by the trusted caller. */
    @PostMapping("/mall/create")
    public ResponseResult<MallCashierCreateResponse> createForMall(
            @Valid @RequestBody MallCashierCreateRequest request) {
        CashierCreateRequest cashierRequest = new CashierCreateRequest();
        cashierRequest.setMchOrderNo(request.getMallOrderNo());
        cashierRequest.setMallOrderNo(request.getMallOrderNo());
        cashierRequest.setRequestNo(request.getRequestNo());
        cashierRequest.setTerminal(request.getTerminal());
        cashierRequest.setPayMethod(request.getPayMethod());
        cashierRequest.setAmount(request.getAmount());
        cashierRequest.setCurrency(request.getCurrency());
        cashierRequest.setSubject(request.getSubject());
        cashierRequest.setClientIp(request.getClientIp());
        cashierRequest.setNotifyUrl(request.getNotifyUrl());
        cashierRequest.setReturnUrl(request.getReturnUrl());
        cashierRequest.setOpenId(request.getOpenId());
        cashierRequest.setChannelCode(request.getChannelCode());

        CashierCreateResponse created = cashierService.create(cashierRequest, request.getUserId());
        MallCashierCreateResponse response = new MallCashierCreateResponse();
        response.setCashierNo(created.getCashierNo());
        response.setMallOrderNo(request.getMallOrderNo());
        response.setRequestNo(request.getRequestNo());
        response.setPayOrderId(created.getPayOrderId());
        response.setPayMethod(created.getPayMethod());
        response.setTerminal(created.getTerminal());
        response.setAmount(created.getAmount());
        response.setStatus(String.valueOf(created.getStatus()));
        response.setPayData(created.getPayData());
        if (created.getExpireTime() != null) {
            response.setExpireTime(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(created.getExpireTime()), ZoneId.systemDefault()));
        }
        return ResponseResult.success(response);
    }

    @PostMapping("/mall/status")
    public ResponseResult<MallCashierStatusResponse> statusForMall(
            @RequestParam("cashierNo") String cashierNo) {
        CashierStatusResponse status = cashierService.query(cashierNo);
        MallCashierStatusResponse response = new MallCashierStatusResponse();
        response.setCashierNo(status.getCashierNo());
        response.setMallOrderNo(status.getMchOrderNo());
        response.setPayOrderId(status.getPayOrderId());
        response.setPayMethod(status.getPayMethod());
        response.setTerminal(status.getTerminal());
        response.setAmount(status.getAmount());
        response.setStatusCode(status.getStatus());
        response.setStatus(statusName(status.getStatus()));
        response.setStatusDesc(status.getStatusDesc());
        response.setErrCode(status.getErrCode());
        response.setErrMsg(status.getErrMsg());
        if (status.getSuccessTime() != null) {
            response.setPaidTime(LocalDateTime.ofInstant(
                    status.getSuccessTime().toInstant(), ZoneId.systemDefault()));
        }
        return ResponseResult.success(response);
    }

    @PostMapping("/mall/refund")
    public ResponseResult<PayRefundOrderEntity> refundForMall(
            @Valid @RequestBody MallRefundRequest request) {
        return ResponseResult.success(mallCashierRefundService.refund(request));
    }

    private String statusName(Integer status) {
        if (status == null) return "UNKNOWN";
        return switch (status) {
            case 0 -> "WAIT_PAY";
            case 1 -> "PAYING";
            case 2 -> "SUCCESS";
            case 3 -> "FAILED";
            case 4 -> "CLOSED";
            case 5 -> "REFUNDED";
            default -> "UNKNOWN";
        };
    }

    /**
     * 查询收银台订单状态
     */
    @PostMapping("/status")
    public ResponseResult<CashierStatusResponse> status(@RequestBody Map<String, String> body) {
        String cashierNo = body == null ? null : body.get("cashierNo");
        return ResponseResult.success(cashierService.query(cashierNo));
    }

    /**
     * 查询收银台订单状态（GET）
     */
    @GetMapping("/status/{cashierNo}")
    public ResponseResult<CashierStatusResponse> statusByPath(@PathVariable("cashierNo") String cashierNo) {
        return ResponseResult.success(cashierService.query(cashierNo));
    }

    /**
     * 关闭收银台订单
     */
    @PostMapping("/close")
    public ResponseResult<CashierStatusResponse> close(@RequestBody Map<String, String> body) {
        String cashierNo = body == null ? null : body.get("cashierNo");
        return ResponseResult.success(cashierService.close(cashierNo));
    }

    // ================= 私有方法 =================

    /**
     * 从请求上下文提取 userId；失败时抛异常（收银台接口必须登录）
     */
    private String resolveUserId(HttpServletRequest http) {
        String token = http.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            token = http.getHeader("token");
        }
        if (!StringUtils.hasText(token)) {
            throw new IllegalStateException("缺少登录凭证");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfoVO user = ParseTokenUtils.getUserInfoByToken(token);
        if (user == null || !StringUtils.hasText(user.getUserId())) {
            throw new IllegalStateException("登录凭证无效");
        }
        return user.getUserId();
    }
}
