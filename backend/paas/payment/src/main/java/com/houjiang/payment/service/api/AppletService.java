package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.applet.CloseAppletUrlRequest;
import com.houjiang.payment.dto.applet.CloseAppletUrlResponse;

/**
 * 小程序服务接口
 * 
 * @author Amos
 */
public interface AppletService {
    
    /**
     * 21. 关闭小程序链接
     * 用于关闭之前创建的小程序支付链接
     * 
     * @param request 关闭小程序链接请求
     * @return 关闭小程序链接响应
     */
    CloseAppletUrlResponse closeAppletUrl(CloseAppletUrlRequest request);
}
