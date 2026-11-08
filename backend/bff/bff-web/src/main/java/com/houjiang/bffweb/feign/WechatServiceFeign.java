package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信小程序服务Feign客户端
/**
 * 
 * @description 提供微信小程序相关功能的远程调用接口
 * @author Amos
 * @date 2024-12-20
 */
@FeignClient(value = "wechat")
public interface WechatServiceFeign {

    // ================= 用户相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/user/info")
    ResponseResult getWechatUserInfo(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/user/login")
    ResponseResult userLogin(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/user/update")
    ResponseResult updateWechatUserInfo(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/user/bind")
    ResponseResult bindWechatUser(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/user/unbind")
    ResponseResult unbindWechatUser(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/user/session")
    ResponseResult getWechatUserSession(@RequestParam Map<String, Object> param);

    // ================= 小程序码相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/qrcode/create")
    ResponseResult createWechatQRCode(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/qrcode/unlimited")
    ResponseResult createWechatUnlimitedQRCode(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/qrcode/info")
    ResponseResult getWechatQRCodeInfo(@RequestParam Map<String, Object> param);

    // ================= 订阅消息相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/subscribe/send")
    ResponseResult sendSubscribeMessage(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/subscribe/template/list")
    ResponseResult getSubscribeTemplateList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/subscribe/template/add")
    ResponseResult addSubscribeTemplate(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/subscribe/template/delete")
    ResponseResult deleteSubscribeTemplate(@RequestBody Map<String, Object> param);

    // ================= 支付相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/pay/unified")
    ResponseResult unifiedWechatPay(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/pay/query")
    ResponseResult queryWechatPayOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/pay/close")
    ResponseResult closeWechatPayOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/pay/refund")
    ResponseResult refundWechatPay(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/pay/notify")
    ResponseResult wechatPayNotify(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/refund/apply")
    ResponseResult applyWechatRefund(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/refund/query")
    ResponseResult queryWechatRefund(@RequestParam Map<String, Object> param);

    // ================= 模板消息相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/template/send")
    ResponseResult sendWechatTemplateMessage(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/template/list")
    ResponseResult getWechatTemplateLibrary(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/template/add")
    ResponseResult addWechatTemplate(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/template/delete")
    ResponseResult deleteWechatTemplate(@RequestBody Map<String, Object> param);

    // ================= 内容管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/content/check")
    ResponseResult checkContent(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/content/upload")
    ResponseResult uploadContent(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/media/temp/upload")
    ResponseResult uploadTempMedia(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/media/temp/get")
    ResponseResult getTempMedia(@RequestParam Map<String, Object> param);

    // ================= 客服消息相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/custom/send")
    ResponseResult sendCustomMessage(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/custom/typing")
    ResponseResult typing(@RequestParam Map<String, Object> param);

    // ================= 数据分析相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/analysis/dailyvisit")
    ResponseResult getDailyVisitTrend(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/analysis/weeklyvisit")
    ResponseResult getWeeklyVisitTrend(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/analysis/monthlyvisit")
    ResponseResult getMonthlyVisitTrend(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/analysis/visitdistribution")
    ResponseResult getVisitDistribution(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/analysis/retain")
    ResponseResult getRetainInfo(@RequestParam Map<String, Object> param);

    // ================= 附近的小程序相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/nearby/add")
    ResponseResult addNearbyPoi(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/nearby/update")
    ResponseResult updateNearbyPoi(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/nearby/delete")
    ResponseResult deleteNearbyPoi(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/nearby/search")
    ResponseResult searchNearbyPoi(@RequestParam Map<String, Object> param);

    // ================= 地址相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/address/list")
    ResponseResult getAddressList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/address/add")
    ResponseResult addAddress(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/address/update")
    ResponseResult updateAddress(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/address/delete")
    ResponseResult deleteAddress(@RequestBody Map<String, Object> param);

    // ================= 订单相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/order/list")
    ResponseResult getOrderList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/order/detail")
    ResponseResult getOrderDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/order/create")
    ResponseResult createWechatOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/order/cancel")
    ResponseResult cancelOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/order/query")
    ResponseResult queryWechatOrder(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/order/close")
    ResponseResult closeWechatOrder(@RequestBody Map<String, Object> param);
    
    // ================= 授权相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/auth/code")
    ResponseResult getWechatAuthCode(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/auth/token")
    ResponseResult getWechatAccessToken(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/auth/refresh")
    ResponseResult refreshWechatAccessToken(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/auth/check")
    ResponseResult checkWechatAuthStatus(@RequestParam Map<String, Object> param);
    
    // ================= 媒体文件相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/media/upload")
    ResponseResult uploadWechatMedia(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/media/get")
    ResponseResult getWechatMedia(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/media/delete")
    ResponseResult deleteWechatMedia(@RequestBody Map<String, Object> param);
    
    // ================= 客服消息相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/customer/send")
    ResponseResult sendCustomerMessage(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/customer/session")
    ResponseResult getCustomerSession(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/customer/session/typing")
    ResponseResult setCustomerTypingStatus(@RequestBody Map<String, Object> param);
    
    // ================= 数据分析相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/analysis/user")
    ResponseResult getWechatUserAnalysis(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/analysis/visit")
    ResponseResult getWechatVisitAnalysis(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/wechat/analysis/retain")
    ResponseResult getWechatRetainAnalysis(@RequestParam Map<String, Object> param);
    
    // ================= 内容安全检查相关接口=================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/security/check")
    ResponseResult checkWechatContentSecurity(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/security/imgcheck")
    ResponseResult checkWechatImageSecurity(@RequestBody Map<String, Object> param);
    
    
    // ================= 统一服务接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/wechat/service/unified")
    ResponseResult unifiedWechatService(@RequestBody Map<String, Object> param);
}