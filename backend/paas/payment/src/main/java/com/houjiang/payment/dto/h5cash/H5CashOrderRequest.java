package com.houjiang.payment.dto.h5cash;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * H5收银台下单请求DTO
 * 接口：27.H5收银台下单
 * 请求地址：/h5Cash/getH5Url
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class H5CashOrderRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商id(可选)
     * 用于服务商模式,传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     */
    private String appId;
    
    /**
     * 商户订单号(必填)
     */
    private String mchOrderNo;
    
    /**
     * 金额(必填)
     * 单位:分
     */
    private String amount;
    
    /**
     * 描述(必填)
     */
    private String body;
    
    /**
     * 通知地址(可选)
     */
    private String notifyUrl;
    
    /**
     * 微信点金计划跳转地址(可选)
     * 仅微信浏览器内打开有效
     */
    private String frontUrl;
    
    /**
     * 前端跳转地址(可选)
     * 支付完成后会带参跳转到该地址
     * 微信内打开的时候此跳转地址无效
     */
    private String jumpUrl;
    
    /**
     * 链接有效期(可选)
     * 单位秒,不传默认120秒
     * 最小120秒,最大支持72小时
     */
    private Integer validTime;
    
    /**
     * 订单失效时间(可选)
     * 单位秒,默认1小时
     * 订单在(创建时间+失效时间)后失效
     */
    private Integer expiredTime;
    
    /**
     * 备注(可选)
     * 商户扩展参数,回调时原样返回
     */
    private String extParam;
    
    /**
     * 商品备注(可选)
     * JSONArray格式
     * 例如：[{"goodsnm":"TEST_PAY","goodsnum":"1","goodsamt":"2"}]
     * 使用转账汇款时必填
     */
    private String goodsInfo;
    
    /**
     * 渠道参数(可选)
     * JSON字符串,特定渠道发起的额外参数
     * 例：收银宝子商户号cusid,集团商户号orgid
     * 使用转账汇款时,merUserId(商户用户标识)必填
     */
    private String channelExtra;
    
    /**
     * 收银台展示的付款方式(可选)
     * 多个以英文逗号(,)隔开
     * WX_PAY：微信
     * ALI_PAY：支付宝
     * ZZHK_PAY：转账汇款
     */
    private String payTypeInfo;
    
    /**
     * 直接支付的支付方式(可选)
     * 仅支持上送一种支付方式
     * 填入该参数则自动使用该支付方式下单并拉起支付
     * WX_JSAPI：微信公众号支付
     * ALI_QR：支付宝支付(微信环境内支付宝支付为吱口令)
     */
    private String directPayType;
}
