package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.miniapp.GetAppletUrlRequest;
import com.houjiang.payment.dto.miniapp.GetAppletUrlResponse;
import com.houjiang.payment.dto.miniapp.MiniAppOrderRequest;
import com.houjiang.payment.dto.miniapp.MiniAppOrderResponse;

/**
 * 小程序支付服务接口
 * 
 * @author Amos
 */
public interface MiniAppPayService {
    
    /**
     * 小程序下单
     * 生成小程序支付参数，用于拉起通企付小程序收银台
     * 
     * @param request 小程序下单请求
     * @return 小程序下单响应
     */
    MiniAppOrderResponse createOrder(MiniAppOrderRequest request);
    
    /**
     * 获取小程序链接
     * 生成访问通企付小程序的链接，需要转成二维码后用户扫码支付
     * 
     * @param request 获取链接请求
     * @return 小程序链接响应
     */
    GetAppletUrlResponse getAppletUrl(GetAppletUrlRequest request);
    
    /**
     * 生成微信小程序调起参数
     * 用于wx.openEmbeddedMiniProgram
     * 
     * @param request 下单请求
     * @return extraData参数JSON字符串
     */
    String generateWechatMiniAppParams(MiniAppOrderRequest request);
    
    /**
     * 生成支付宝小程序调起参数
     * 用于my.navigateToMiniProgram
     * 
     * @param request 下单请求
     * @return extraData参数JSON字符串
     */
    String generateAlipayMiniAppParams(MiniAppOrderRequest request);
    
    /**
     * 生成支付宝H5跳转链接
     * 用于H5拉起支付宝小程序
     * 
     * @param request 下单请求
     * @return 支付宝跳转链接
     */
    String generateAlipayH5Url(MiniAppOrderRequest request);
}
