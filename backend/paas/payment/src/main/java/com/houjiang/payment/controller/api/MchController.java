package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.mch.MchRegRequest;
import com.houjiang.payment.dto.mch.MchRegResponse;
import com.houjiang.payment.service.api.MchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户模块Controller
 *
 * 对接通企付商户相关接口
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/mch")
@RequiredArgsConstructor
public class MchController {

    private final MchService mchService;

    /**
     * 商户进件
     * 用于服务商进件通企付商户
     *
     * 对应通企付接口：/mch/reg
     *
     * @param request 商户进件请求
     * @return 商户进件响应
     */
    @PostMapping("/reg")
    public MchRegResponse mchReg(@Valid @RequestBody MchRegRequest request) {
        log.info("收到商户进件请求, companyName:{}, cusid:{}, loginUsername:{}",
                request.getCompanyName(), request.getCusid(), request.getLoginUsername());
        return mchService.mchReg(request);
    }
}
