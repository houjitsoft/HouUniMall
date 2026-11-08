package com.houjiang.payment.dto.miniapp;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 获取小程序链接请求
 * 接口：3.获取小程序链接
 * 请求URL: applet/getAppletUrl
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetAppletUrlRequest extends PaymentBaseRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 服务商id，用于服务商模式 */
    private String orgId;
    
    /** 商户号 */
    private String mchNo;
    
    /** 应用ID */
    private String appId;
    
    /** 商户订单号 */
    private String mchOrderNo;
    
    /** 金额（单位：分） */
    private String amount;
    
    /** 描述 */
    private String body;
    
    /** 商品备注，JSONArray格式 */
    private String goodsInfo;
    
    /** 通知地址 */
    private String notifyUrl;
    
    /** 链接有效期，单位秒，不传默认120秒，最小120秒，最大72小时 */
    private Integer validTime;
    
    /** 订单有效期，单位秒，默认1小时 */
    private Integer expiredTime;
    
    /** 备注，商户扩展参数，回调时原样返回 */
    private String extParam;
    
    /** 渠道参数，JSON字符串 */
    private String channelExtra;
    
    /** 收银台展示的付款方式，多个以英文逗号分隔 */
    private String payTypeInfo;
    
    /** 直接支付的支付方式 */
    private String directPayType;
    
    /** 生成链接方式，URL_Common */
    private String urlType = "URL_Common";
    
    /** 版本号，固定1.0 */
    private String version = "1.0";
}
