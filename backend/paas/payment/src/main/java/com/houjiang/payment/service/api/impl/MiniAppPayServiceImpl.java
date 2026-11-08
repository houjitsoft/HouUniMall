package com.houjiang.payment.service.api.impl;

import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.MiniAppConstants;
import com.houjiang.payment.dto.miniapp.GetAppletUrlRequest;
import com.houjiang.payment.dto.miniapp.GetAppletUrlResponse;
import com.houjiang.payment.dto.miniapp.MiniAppOrderRequest;
import com.houjiang.payment.dto.miniapp.MiniAppOrderResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.MiniAppPayService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 小程序支付服务实现
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppPayServiceImpl implements MiniAppPayService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public MiniAppOrderResponse createOrder(MiniAppOrderRequest request) {
        log.info("开始处理小程序下单, mchOrderNo:{}, amount:{}", request.getMchOrderNo(), request.getAmount());
        
        try {
            // 1. 填充基础参数
            fillBaseParams(request);
            
            // 2. 生成签名
            signService.signRequest(request);
            
            // TODO: 3. 这里不需要调用API，小程序下单是前端直接拉起收银台
            // 仅需生成签名参数返回给前端即可
            
            // 返回成功响应
            MiniAppOrderResponse response = new MiniAppOrderResponse();
            response.setCode(MiniAppConstants.ResultCode.SUCCESS);
            response.setMsg("参数生成成功");
            response.setMchOrderNo(request.getMchOrderNo());
            response.setAmount(request.getAmount());
            response.setMchNo(request.getMchNo());
            
            log.info("小程序下单参数生成成功, mchOrderNo:{}", request.getMchOrderNo());
            return response;
            
        } catch (Exception e) {
            log.error("小程序下单失败, mchOrderNo:" + request.getMchOrderNo(), e);
            throw PaymentException.systemError("小程序下单失败: " + e.getMessage());
        }
    }
    
    @Override
    public GetAppletUrlResponse getAppletUrl(GetAppletUrlRequest request) {
        log.info("开始获取小程序链接, mchOrderNo:{}, amount:{}", 
                request.getMchOrderNo(), request.getAmount());
        
        try {
            // 1. 填充基础参数
            fillBaseParams(request);
            
            // 2. 生成签名
            signService.signRequest(request);
            
            // 3. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "applet/getAppletUrl";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("获取小程序链接请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 4. 解析响应并验签
            GetAppletUrlResponse response = JSONUtil.toBean(jsonResponse, GetAppletUrlResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("获取小程序链接响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 5. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("获取小程序链接失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("获取小程序链接失败: " + response.getMsg());
            }
            
            log.info("获取小程序链接成功, mchOrderNo:{}, appletUrl:{}", 
                    request.getMchOrderNo(), response.getAppletUrl());
            return response;
            
        } catch (Exception e) {
            log.error("获取小程序链接失败, mchOrderNo:" + request.getMchOrderNo(), e);
            throw PaymentException.systemError("获取小程序链接失败: " + e.getMessage());
        }
    }
    
    @Override
    public String generateWechatMiniAppParams(MiniAppOrderRequest request) {
        log.info("生成微信小程序调起参数, mchOrderNo:{}", request.getMchOrderNo());
        
        try {
            // 填充基础参数
            fillBaseParams(request);
            
            // 生成签名
            signService.signRequest(request);
            
            // 构建extraData参数
            Map<String, Object> extraData = new HashMap<>();
            if (request.getOrgId() != null) {
                extraData.put("orgId", request.getOrgId());
            }
            extraData.put("mchNo", request.getMchNo());
            extraData.put("appId", request.getAppId());
            extraData.put("reqTime", request.getReqTime());
            extraData.put("version", request.getVersion());
            extraData.put("sign", request.getSign());
            extraData.put("signType", request.getSignType());
            extraData.put("mchOrderNo", request.getMchOrderNo());
            extraData.put("amount", request.getAmount());
            extraData.put("body", request.getBody());
            
            // 可选参数
            if (request.getGoodsInfo() != null) {
                extraData.put("goodsInfo", request.getGoodsInfo());
            }
            if (request.getNotifyUrl() != null) {
                extraData.put("notifyUrl", request.getNotifyUrl());
            }
            if (request.getExpiredTime() != null) {
                extraData.put("expiredTime", request.getExpiredTime());
            }
            if (request.getExtParam() != null) {
                extraData.put("extParam", request.getExtParam());
            }
            if (request.getChannelExtra() != null) {
                extraData.put("channelExtra", request.getChannelExtra());
            }
            if (request.getPayTypeInfo() != null) {
                extraData.put("payTypeInfo", request.getPayTypeInfo());
            }
            if (request.getDirectPayType() != null) {
                extraData.put("directPayType", request.getDirectPayType());
            }
            
            String json = JSONUtil.toJsonStr(extraData);
            log.info("微信小程序参数生成成功: {}", json);
            return json;
            
        } catch (Exception e) {
            log.error("生成微信小程序参数失败", e);
            throw PaymentException.systemError("生成微信小程序参数失败: " + e.getMessage());
        }
    }
    
    @Override
    public String generateAlipayMiniAppParams(MiniAppOrderRequest request) {
        log.info("生成支付宝小程序调起参数, mchOrderNo:{}", request.getMchOrderNo());
        
        try {
            // 填充基础参数
            fillBaseParams(request);
            
            // 生成签名
            signService.signRequest(request);
            
            // 构建extraData参数（与微信小程序相同）
            Map<String, Object> extraData = new HashMap<>();
            if (request.getOrgId() != null) {
                extraData.put("orgId", request.getOrgId());
            }
            extraData.put("mchNo", request.getMchNo());
            extraData.put("appId", request.getAppId());
            extraData.put("reqTime", request.getReqTime());
            extraData.put("version", request.getVersion());
            extraData.put("sign", request.getSign());
            extraData.put("signType", request.getSignType());
            extraData.put("mchOrderNo", request.getMchOrderNo());
            extraData.put("amount", request.getAmount());
            extraData.put("body", request.getBody());
            
            // 可选参数
            if (request.getGoodsInfo() != null) {
                extraData.put("goodsInfo", request.getGoodsInfo());
            }
            if (request.getNotifyUrl() != null) {
                extraData.put("notifyUrl", request.getNotifyUrl());
            }
            if (request.getExpiredTime() != null) {
                extraData.put("expiredTime", request.getExpiredTime());
            }
            if (request.getExtParam() != null) {
                extraData.put("extParam", request.getExtParam());
            }
            if (request.getChannelExtra() != null) {
                extraData.put("channelExtra", request.getChannelExtra());
            }
            if (request.getPayTypeInfo() != null) {
                extraData.put("payTypeInfo", request.getPayTypeInfo());
            }
            if (request.getDirectPayType() != null) {
                extraData.put("directPayType", request.getDirectPayType());
            }
            
            String json = JSONUtil.toJsonStr(extraData);
            log.info("支付宝小程序参数生成成功: {}", json);
            return json;
            
        } catch (Exception e) {
            log.error("生成支付宝小程序参数失败", e);
            throw PaymentException.systemError("生成支付宝小程序参数失败: " + e.getMessage());
        }
    }
    
    @Override
    public String generateAlipayH5Url(MiniAppOrderRequest request) {
        log.info("生成支付宝H5跳转链接, mchOrderNo:{}", request.getMchOrderNo());
        
        try {
            // 填充基础参数
            fillBaseParams(request);
            
            // 生成签名
            signService.signRequest(request);
            
            // 构建参数JSON
            Map<String, Object> params = new HashMap<>();
            if (request.getOrgId() != null) {
                params.put("orgId", request.getOrgId());
            }
            params.put("mchNo", request.getMchNo());
            params.put("appId", request.getAppId());
            params.put("reqTime", request.getReqTime());
            params.put("version", request.getVersion());
            params.put("signType", request.getSignType());
            params.put("mchOrderNo", request.getMchOrderNo());
            params.put("amount", request.getAmount());
            params.put("body", request.getBody());
            
            // RSA/SM2签名需要URL安全处理
            String sign = request.getSign();
            if (!"MD5".equals(request.getSignType())) {
                // 将+替换为-（URL安全格式）
                sign = sign.replace('+', '-');
                sign = URLEncoder.encode(sign, "UTF-8");
            }
            params.put("sign", sign);
            
            // 可选参数
            if (request.getGoodsInfo() != null) {
                params.put("goodsInfo", request.getGoodsInfo());
            }
            if (request.getNotifyUrl() != null) {
                params.put("notifyUrl", request.getNotifyUrl());
            }
            if (request.getExpiredTime() != null) {
                params.put("expiredTime", request.getExpiredTime());
            }
            if (request.getExtParam() != null) {
                params.put("extParam", request.getExtParam());
            }
            if (request.getChannelExtra() != null) {
                params.put("channelExtra", request.getChannelExtra());
            }
            if (request.getPayTypeInfo() != null) {
                params.put("payTypeInfo", request.getPayTypeInfo());
            }
            if (request.getDirectPayType() != null) {
                params.put("directPayType", request.getDirectPayType());
            }
            
            String json = JSONUtil.toJsonStr(params);
            
            // 第三步：拼接完整URL
            String schemeContent = MiniAppConstants.AlipayMiniApp.SCHEME_CONTENT + json;
            String encodedScheme = URLEncoder.encode(schemeContent, "UTF-8");
            String finalUrl = MiniAppConstants.AlipayMiniApp.SCHEME_PREFIX + encodedScheme;
            
            log.info("支付宝H5跳转链接生成成功");
            return finalUrl;
            
        } catch (UnsupportedEncodingException e) {
            log.error("URL编码失败", e);
            throw PaymentException.systemError("URL编码失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("生成支付宝H5链接失败", e);
            throw PaymentException.systemError("生成支付宝H5链接失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充基础参数 - MiniAppOrderRequest
     */
    private void fillBaseParams(MiniAppOrderRequest request) {
        if (request.getMchNo() == null) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (request.getAppId() == null) {
            request.setAppId(paymentConfig.getAppId());
        }
        if (request.getReqTime() == null) {
            request.setReqTime(TimeUtil.getCurrentTimestamp());
        }
        if (request.getVersion() == null) {
            request.setVersion("1.0");
        }
    }
    
    /**
     * 填充基础参数 - GetAppletUrlRequest
     */
    private void fillBaseParams(GetAppletUrlRequest request) {
        if (request.getMchNo() == null) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (request.getAppId() == null) {
            request.setAppId(paymentConfig.getAppId());
        }
        if (request.getReqTime() == null) {
            request.setReqTime(TimeUtil.getCurrentTimestamp());
        }
        if (request.getVersion() == null) {
            request.setVersion("1.0");
        }
    }
}
