package com.houjiang.payment.controller.api;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayDivisionReceiverEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.api.DivisionBizService;
import com.houjiang.payment.util.ApiTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 自研分账业务 Controller（W4 P0）。
 *
 * 路径前缀 /api/payment/division/biz/，与 DivisionController（通企付适配）物理隔离。
 *
 * 接口清单（4 个）：
 *   /receiver/bind     接收方绑定
 *   /receiver/list     接收方列表
 *   /receiver/unbind   接收方解绑
 *   /ruleEngine/calc   规则引擎计算
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/division/biz")
@RequiredArgsConstructor
public class DivisionBizController {

    private final DivisionBizService divisionBizService;

    @PostMapping("/receiver/bind")
    public ResponseResult<PayDivisionReceiverEntity> receiverBind(@RequestBody Map<String, Object> req,
                                                                  HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(divisionBizService.receiverBind(req, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        } catch (Exception ex) {
            log.error("[division/biz/receiverBind] error", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    @PostMapping("/receiver/list")
    public ResponseResult<List<PayDivisionReceiverEntity>> receiverList(@RequestBody Map<String, Object> req) {
        try {
            return ResponseResult.success(divisionBizService.receiverList(req));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    @PostMapping("/receiver/unbind")
    public ResponseResult<Boolean> receiverUnbind(@RequestBody Map<String, Object> req,
                                                  HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(divisionBizService.receiverUnbind(req, userId));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    @PostMapping("/ruleEngine/calc")
    public ResponseResult<List<Map<String, Object>>> ruleEngineCalc(@RequestBody Map<String, Object> req) {
        try {
            return ResponseResult.success(divisionBizService.ruleEngineCalc(req));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        } catch (Exception ex) {
            log.error("[division/biz/ruleEngineCalc] error", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }
}
