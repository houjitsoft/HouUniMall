package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.applet.CloseAppletUrlRequest;
import com.houjiang.payment.dto.applet.CloseAppletUrlResponse;
import com.houjiang.payment.service.api.AppletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/applet")
@RequiredArgsConstructor
public class AppletController {
    
    private final AppletService appletService;
    
    /**
     * 21. 关闭小程序链接
     * 用于关闭之前创建的小程序支付链接
     * 
     * 使用场景:
     * 1. 用户取消支付,关闭支付链接
     * 2. 订单超时未支付,关闭支付链接
     * 3. 商户主动关闭支付链接
     * 
     * 注意事项:
     * 1. 服务商id(orgId)必填
     * 2. 必须使用服务商密钥进行加签
     * 3. 链接关闭后无法再使用该链接进行支付
     * 4. closeState: 0-关闭失败, 1-关闭成功
     * 
     * @param request 关闭小程序链接请求
     * @return 关闭小程序链接响应
     */
    @PostMapping("/commonurl/close")
    public CloseAppletUrlResponse closeAppletUrl(@Valid @RequestBody CloseAppletUrlRequest request) {
        log.info("关闭小程序链接请求, mchOrderNo:{}", request.getMchOrderNo());
        return appletService.closeAppletUrl(request);
    }
}
