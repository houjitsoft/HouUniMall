package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 微信小程序服务降级处理类
/**
 * 
 * @description 当微信小程序服务不可用时，提供降级响应
 * @author Amos
 * @date 2024-12-20
 */
@Component
public class WechatServiceFeignFallback implements WechatServiceFeign {

    /** ================= 用户相关接口 ================= */

    @Override
    public ResponseResult getWechatUserInfo(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult userLogin(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult updateWechatUserInfo(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult bindWechatUser(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult unbindWechatUser(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getWechatUserSession(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 小程序码相关接口 ================= */

    @Override
    public ResponseResult createWechatQRCode(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult createWechatUnlimitedQRCode(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getWechatQRCodeInfo(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 订阅消息相关接口 ================= */

    @Override
    public ResponseResult sendSubscribeMessage(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getSubscribeTemplateList(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult addSubscribeTemplate(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult deleteSubscribeTemplate(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 支付相关接口 ================= */

    @Override
    public ResponseResult unifiedWechatPay(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult queryWechatPayOrder(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult closeWechatPayOrder(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult refundWechatPay(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult wechatPayNotify(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult applyWechatRefund(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult queryWechatRefund(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 订单相关接口（微信） ================= */

    @Override
    public ResponseResult createWechatOrder(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult queryWechatOrder(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult closeWechatOrder(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult cancelOrder(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getOrderList(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getOrderDetail(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 授权相关接口 ================= */

    @Override
    public ResponseResult getWechatAuthCode(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getWechatAccessToken(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult refreshWechatAccessToken(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult checkWechatAuthStatus(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 媒体文件相关接口 ================= */

    @Override
    public ResponseResult uploadWechatMedia(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getWechatMedia(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult deleteWechatMedia(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 客服消息相关接口 ================= */

    @Override
    public ResponseResult sendCustomerMessage(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getCustomerSession(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult setCustomerTypingStatus(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 数据分析相关接口 ================= */

    @Override
    public ResponseResult getWechatUserAnalysis(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getWechatVisitAnalysis(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getWechatRetainAnalysis(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 内容安全检查相关接口 ================= */

    @Override
    public ResponseResult checkWechatContentSecurity(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult checkWechatImageSecurity(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 模板消息相关接口 ================= */

    @Override
    public ResponseResult sendWechatTemplateMessage(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getWechatTemplateLibrary(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult addWechatTemplate(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult deleteWechatTemplate(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 地址管理相关接口 ================= */

    @Override
    public ResponseResult getAddressList(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult addAddress(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult updateAddress(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult deleteAddress(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult searchNearbyPoi(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult deleteNearbyPoi(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult updateNearbyPoi(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult addNearbyPoi(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getRetainInfo(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getVisitDistribution(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getMonthlyVisitTrend(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getWeeklyVisitTrend(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getDailyVisitTrend(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult typing(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult sendCustomMessage(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult getTempMedia(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult uploadTempMedia(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult uploadContent(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    @Override
    public ResponseResult checkContent(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }

    /** ================= 统一服务接口 ================= */

    @Override
    public ResponseResult unifiedWechatService(Map<String, Object> param) {
        return ResponseResult.fail(503, "微信服务暂时不可用");
    }
}