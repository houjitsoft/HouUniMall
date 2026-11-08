package com.houjiang.mall.controller.pay;

import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.dto.pay.MallPayCreateParam;
import com.houjiang.mall.service.pay.MallCurrentUserResolver;
import com.houjiang.mall.service.pay.MallPaymentFacade;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/mall/pay")
public class MallPaymentController {

    private final MallPaymentFacade facade;
    private final MallCurrentUserResolver userResolver;

    @PostMapping("/create")
    public ResponseResult<?> create(@RequestBody MallPayCreateParam param, HttpServletRequest request) {
        return ResponseResult.success(facade.create(param, userResolver.resolve(request)));
    }

    @PostMapping("/status")
    public ResponseResult<?> status(@RequestBody Map<String, String> param, HttpServletRequest request) {
        return ResponseResult.success(facade.status(param.get("cashierNo"), userResolver.resolve(request)));
    }

    @PostMapping("/ways")
    public ResponseResult<?> ways(@RequestBody(required = false) Map<String, Object> param) {
        Integer scene = param == null || param.get("scene") == null ? null
                : Integer.valueOf(param.get("scene").toString());
        return ResponseResult.success(facade.ways(scene));
    }

    @PostMapping("/balance")
    public ResponseResult<?> balance(HttpServletRequest request) {
        return ResponseResult.success(facade.balance(userResolver.resolve(request)));
    }

    @PostMapping("/refund")
    public ResponseResult<?> refund(@RequestBody MallRefundRequest param, HttpServletRequest request) {
        return ResponseResult.success(facade.refund(param, userResolver.resolve(request)));
    }
}
