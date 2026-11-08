package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.miniapp.GetAppletUrlRequest;
import com.houjiang.payment.dto.miniapp.GetAppletUrlResponse;
import com.houjiang.payment.dto.miniapp.MiniAppOrderRequest;
import com.houjiang.payment.dto.miniapp.MiniAppOrderResponse;
import com.houjiang.payment.service.api.MiniAppPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 小程序支付接口
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/miniapp")
@RequiredArgsConstructor
public class MiniAppPayController {
    
    private final MiniAppPayService miniAppPayService;
    
    /**
     * 小程序下单
     * 生成签名参数，供前端拉起小程序收银台使用
     * 
     * @param request 下单请求
     * @return 下单响应
     */
    @PostMapping("/createOrder")
    public MiniAppOrderResponse createOrder(@Valid @RequestBody MiniAppOrderRequest request) {
        log.info("接收小程序下单请求, mchOrderNo:{}, amount:{}", 
                request.getMchOrderNo(), request.getAmount());
        return miniAppPayService.createOrder(request);
    }
    
    /**
     * 获取小程序链接
     * 生成访问通企付小程序的链接，需要转成二维码后用户扫码支付
     * 
     * @param request 获取链接请求
     * @return 小程序链接响应
     */
    @PostMapping("/getAppletUrl")
    public GetAppletUrlResponse getAppletUrl(@Valid @RequestBody GetAppletUrlRequest request) {
        log.info("获取小程序链接请求, mchOrderNo:{}, amount:{}", 
                request.getMchOrderNo(), request.getAmount());
        return miniAppPayService.getAppletUrl(request);
    }
    
    /**
     * 生成微信小程序调起参数
     * 返回extraData，用于wx.openEmbeddedMiniProgram
     * 
     * @param request 下单请求
     * @return extraData参数
     */
    @PostMapping("/wechat/generateParams")
    public Map<String, Object> generateWechatParams(@Valid @RequestBody MiniAppOrderRequest request) {
        log.info("生成微信小程序参数, mchOrderNo:{}", request.getMchOrderNo());
        
        String extraData = miniAppPayService.generateWechatMiniAppParams(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("appId", "wxe48aedff31caaf2d");
        result.put("path", "pages/index/index");
        result.put("extraData", extraData);
        
        return result;
    }
    
    /**
     * 生成支付宝小程序调起参数
     * 返回extraData，用于my.navigateToMiniProgram
     * 
     * @param request 下单请求
     * @return extraData参数
     */
    @PostMapping("/alipay/generateParams")
    public Map<String, Object> generateAlipayParams(@Valid @RequestBody MiniAppOrderRequest request) {
        log.info("生成支付宝小程序参数, mchOrderNo:{}", request.getMchOrderNo());
        
        String extraData = miniAppPayService.generateAlipayMiniAppParams(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("appId", "2021004162658199");
        result.put("path", "pages/index/index");
        result.put("extraData", extraData);
        
        return result;
    }
    
    /**
     * 生成支付宝H5跳转链接
     * 用于H5页面拉起支付宝小程序
     * 
     * @param request 下单请求
     * @return 跳转链接
     */
    @PostMapping("/alipay/generateH5Url")
    public Map<String, Object> generateAlipayH5Url(@Valid @RequestBody MiniAppOrderRequest request) {
        log.info("生成支付宝H5跳转链接, mchOrderNo:{}", request.getMchOrderNo());
        
        String url = miniAppPayService.generateAlipayH5Url(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("url", url);
        
        return result;
    }
}
