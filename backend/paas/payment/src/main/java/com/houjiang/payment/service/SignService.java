package com.houjiang.payment.service;

import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.base.PaymentBaseRequest;
import com.houjiang.payment.dto.base.PaymentBaseResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.util.SignUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 签名服务
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignService {
    
    private final TqfPaymentConfig paymentConfig;
    
    /**
     * 为请求对象生成签名
     * 
     * @param request 请求对象
     * @param <T> 请求类型
     */
    public <T extends PaymentBaseRequest> void signRequest(T request) {
        // 设置签名类型
        String signType = paymentConfig.getSignType();
        request.setSignType(signType);
        
        // 转换为Map
        Map<String, String> params = SignUtil.objectToSignMap(request);
        
        // 根据签名类型生成签名
        String sign;
        switch (signType) {
            case PaymentConstants.SignType.MD5:
                sign = SignUtil.signMD5(params, paymentConfig.getMd5Key());
                break;
            case PaymentConstants.SignType.RSA:
                sign = SignUtil.signRSA(params, paymentConfig.getRsaPrivateKey());
                break;
            case PaymentConstants.SignType.SM2:
                sign = SignUtil.signSM2(params, paymentConfig.getSm2PrivateKey());
                break;
            default:
                throw PaymentException.bizError("不支持的签名类型: " + signType);
        }
        
        request.setSign(sign);
        log.debug("请求签名完成, signType:{}, sign:{}", signType, sign);
    }
    
    /**
     * 验证响应签名
     * 
     * @param response 响应对象
     * @param <T> 响应类型
     * @return true-验证通过 false-验证失败
     */
    public <T extends PaymentBaseResponse> boolean verifyResponse(T response) {
        String signType = response.getSignType();
        if (signType == null) {
            log.warn("响应中未指定签名类型");
            return false;
        }
        
        // 转换为Map
        Map<String, String> params = SignUtil.objectToSignMap(response);
        
        // 根据签名类型验证签名
        boolean result;
        switch (signType) {
            case PaymentConstants.SignType.MD5:
                result = SignUtil.verifyMD5(params, paymentConfig.getMd5Key());
                break;
            case PaymentConstants.SignType.RSA:
                result = SignUtil.verifyRSA(params, paymentConfig.getRsaPublicKey());
                break;
            case PaymentConstants.SignType.SM2:
                result = SignUtil.verifySM2(params, paymentConfig.getSm2PublicKey());
                break;
            default:
                log.warn("不支持的签名类型: {}", signType);
                return false;
        }
        
        if (!result) {
            log.error("签名验证失败! signType:{}, sign:{}", signType, response.getSign());
        }
        
        return result;
    }
    
    /**
     * 验证通知签名
     * 
     * @param params 通知参数Map
     * @return true-验证通过 false-验证失败
     */
    public boolean verifyNotify(Map<String, String> params) {
        String signType = params.get("signType");
        if (signType == null) {
            log.warn("通知参数中未指定签名类型");
            return false;
        }
        
        // 根据签名类型验证签名
        boolean result;
        switch (signType) {
            case PaymentConstants.SignType.MD5:
                result = SignUtil.verifyMD5(params, paymentConfig.getMd5Key());
                break;
            case PaymentConstants.SignType.RSA:
                result = SignUtil.verifyRSA(params, paymentConfig.getRsaPublicKey());
                break;
            case PaymentConstants.SignType.SM2:
                result = SignUtil.verifySM2(params, paymentConfig.getSm2PublicKey());
                break;
            default:
                log.warn("不支持的签名类型: {}", signType);
                return false;
        }
        
        if (!result) {
            log.error("通知签名验证失败! signType:{}, params:{}", signType, params);
        }
        
        return result;
    }
}
