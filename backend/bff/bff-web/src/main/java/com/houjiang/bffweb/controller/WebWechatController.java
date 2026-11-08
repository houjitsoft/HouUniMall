package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.WechatServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信小程序Web控制器
/**
 * 
 * @description 提供微信小程序相关功能的Web接口
 * @author Amos
 * @date 2024-12-20
 */
@RestController
@RequestMapping("/web/wechat")
public class WebWechatController {

    @Autowired
    private WechatServiceFeign wechatServiceFeign;

    /** ================= 用户相关接口 ================= */
    
    @GetMapping("/user/info")
    public ResponseResult getWechatUserInfo(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getWechatUserInfo(param);
    }
    
    @PostMapping("/user/update")
    public ResponseResult updateWechatUserInfo(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.updateWechatUserInfo(param);
    }
    
    @PostMapping("/user/bind")
    public ResponseResult bindWechatUser(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.bindWechatUser(param);
    }
    
    @PostMapping("/user/unbind")
    public ResponseResult unbindWechatUser(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.unbindWechatUser(param);
    }
    
    @GetMapping("/user/session")
    public ResponseResult getWechatUserSession(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getWechatUserSession(param);
    }

    /** ================= 小程序码相关接口 ================= */
    
    @PostMapping("/qrcode/create")
    public ResponseResult createWechatQRCode(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.createWechatQRCode(param);
    }
    
    @PostMapping("/qrcode/unlimited")
    public ResponseResult createWechatUnlimitedQRCode(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.createWechatUnlimitedQRCode(param);
    }
    
    @GetMapping("/qrcode/info")
    public ResponseResult getWechatQRCodeInfo(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getWechatQRCodeInfo(param);
    }

    /** ================= 订阅消息相关接口 ================= */
    
    @PostMapping("/subscribe/send")
    public ResponseResult sendSubscribeMessage(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.sendSubscribeMessage(param);
    }
    
    @GetMapping("/subscribe/template/list")
    public ResponseResult getSubscribeTemplateList(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getSubscribeTemplateList(param);
    }
    
    @PostMapping("/subscribe/template/add")
    public ResponseResult addSubscribeTemplate(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.addSubscribeTemplate(param);
    }
    
    @PostMapping("/subscribe/template/delete")
    public ResponseResult deleteSubscribeTemplate(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.deleteSubscribeTemplate(param);
    }

    /** ================= 支付相关接口 ================= */
    
    @PostMapping("/pay/unified")
    public ResponseResult unifiedWechatPay(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.unifiedWechatPay(param);
    }
    
    @PostMapping("/pay/query")
    public ResponseResult queryWechatPayOrder(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.queryWechatPayOrder(param);
    }
    
    @PostMapping("/pay/close")
    public ResponseResult closeWechatPayOrder(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.closeWechatPayOrder(param);
    }
    
    @PostMapping("/pay/refund")
    public ResponseResult refundWechatPay(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.refundWechatPay(param);
    }
    
    @PostMapping("/pay/notify")
    public ResponseResult wechatPayNotify(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.wechatPayNotify(param);
    }

    /** ================= 退款相关接口================= */
    
    @PostMapping("/refund/apply")
    public ResponseResult applyWechatRefund(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.applyWechatRefund(param);
    }
    
    @GetMapping("/refund/query")
    public ResponseResult queryWechatRefund(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.queryWechatRefund(param);
    }

    /** ================= 订单相关接口 ================= */
    
    @PostMapping("/order/create")
    public ResponseResult createWechatOrder(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.createWechatOrder(param);
    }
    
    @GetMapping("/order/query")
    public ResponseResult queryWechatOrder(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.queryWechatOrder(param);
    }
    
    @PostMapping("/order/close")
    public ResponseResult closeWechatOrder(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.closeWechatOrder(param);
    }

    /** ================= 用户授权相关接口 ================= */
    
    @GetMapping("/auth/code")
    public ResponseResult getWechatAuthCode(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getWechatAuthCode(param);
    }
    
    @PostMapping("/auth/token")
    public ResponseResult getWechatAccessToken(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.getWechatAccessToken(param);
    }
    
    @PostMapping("/auth/refresh")
    public ResponseResult refreshWechatAccessToken(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.refreshWechatAccessToken(param);
    }
    
    @GetMapping("/auth/check")
    public ResponseResult checkWechatAuthStatus(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.checkWechatAuthStatus(param);
    }

    /** ================= 媒体相关接口 ================= */
    
    @PostMapping("/media/upload")
    public ResponseResult uploadWechatMedia(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.uploadWechatMedia(param);
    }
    
    @GetMapping("/media/get")
    public ResponseResult getWechatMedia(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getWechatMedia(param);
    }
    
    @PostMapping("/media/delete")
    public ResponseResult deleteWechatMedia(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.deleteWechatMedia(param);
    }

    /** ================= 客服消息相关接口 ================= */
    
    @PostMapping("/customer/send")
    public ResponseResult sendCustomerMessage(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.sendCustomerMessage(param);
    }
    
    @GetMapping("/customer/session")
    public ResponseResult getCustomerSession(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getCustomerSession(param);
    }
    
    @PostMapping("/customer/session/typing")
    public ResponseResult setCustomerTypingStatus(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.setCustomerTypingStatus(param);
    }

    /** ================= 数据分析相关接口 ================= */
    
    @GetMapping("/analysis/user")
    public ResponseResult getWechatUserAnalysis(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getWechatUserAnalysis(param);
    }
    
    @GetMapping("/analysis/visit")
    public ResponseResult getWechatVisitAnalysis(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getWechatVisitAnalysis(param);
    }
    
    @GetMapping("/analysis/retain")
    public ResponseResult getWechatRetainAnalysis(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getWechatRetainAnalysis(param);
    }

    /** ================= 内容安全相关接口 ================= */
    
    @PostMapping("/security/check")
    public ResponseResult checkWechatContentSecurity(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.checkWechatContentSecurity(param);
    }
    
    @PostMapping("/security/imgcheck")
    public ResponseResult checkWechatImageSecurity(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.checkWechatImageSecurity(param);
    }

    /** ================= 模板消息相关接口 ================= */
    
    @PostMapping("/template/send")
    public ResponseResult sendWechatTemplateMessage(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.sendWechatTemplateMessage(param);
    }
    
    @GetMapping("/template/library")
    public ResponseResult getWechatTemplateLibrary(@RequestParam Map<String, Object> param) {
        return wechatServiceFeign.getWechatTemplateLibrary(param);
    }
    
    @PostMapping("/template/add")
    public ResponseResult addWechatTemplate(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.addWechatTemplate(param);
    }
    
    @PostMapping("/template/delete")
    public ResponseResult deleteWechatTemplate(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.deleteWechatTemplate(param);
    }

    /** ================= 统一服务接口 ================= */
    
    @PostMapping("/service/unified")
    public ResponseResult unifiedWechatService(@RequestBody Map<String, Object> param) {
        return wechatServiceFeign.unifiedWechatService(param);
    }
}