package com.houjiang.payment.dto.miniapp;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 获取小程序链接响应
 * 接口：3.获取小程序链接
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetAppletUrlResponse extends PaymentBaseResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 访问路径，需要转化为二维码后由用户扫码完成支付 */
    private String appletUrl;
    
    /** 生成链接方式，URL_Common */
    private String urlType;
}
