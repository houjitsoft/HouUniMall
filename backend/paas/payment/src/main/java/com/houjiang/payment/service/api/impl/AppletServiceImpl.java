package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.applet.CloseAppletUrlRequest;
import com.houjiang.payment.dto.applet.CloseAppletUrlResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.AppletService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 小程序服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppletServiceImpl implements AppletService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public CloseAppletUrlResponse closeAppletUrl(CloseAppletUrlRequest request) {
        log.info("开始关闭小程序链接, mchOrderNo:{}", request.getMchOrderNo());
        
        try {
            // 1. 参数校验
            validateCloseAppletUrl(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 生成签名
            signService.signRequest(request);
            
            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "applet/commonurl/colse";
            String jsonRequest = cn.hutool.json.JSONUtil.toJsonStr(request);
            log.debug("关闭小程序链接请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 5. 解析响应并验签
            CloseAppletUrlResponse response = cn.hutool.json.JSONUtil.toBean(jsonResponse, CloseAppletUrlResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("关闭小程序链接响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("关闭小程序链接失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("关闭小程序链接失败: " + response.getMsg());
            }
            
            // TODO: 7. 查询本地小程序链接记录
            // AppletUrl localUrl = appletUrlMapper.selectByMchOrderNo(request.getMchOrderNo());
            
            // TODO: 8. 更新链接状态为已关闭
            // localUrl.setState(AppletUrlState.CLOSED);
            // appletUrlMapper.updateById(localUrl);
            
            log.info("关闭小程序链接成功, mchOrderNo:{}, closeState:{}", 
                    request.getMchOrderNo(), response.getCloseState());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("关闭小程序链接异常", e);
            throw PaymentException.systemError("关闭小程序链接失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(CloseAppletUrlRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
    }
    
    /**
     * 验证关闭小程序链接必填参数
     */
    private void validateCloseAppletUrl(CloseAppletUrlRequest request) {
        if (StrUtil.isBlank(request.getOrgId())) {
            throw PaymentException.bizError("服务商id不能为空");
        }
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getMchOrderNo())) {
            throw PaymentException.bizError("商户订单号不能为空");
        }
    }
    
    /**
     * 构建模拟响应
     */
    private CloseAppletUrlResponse buildMockResponse(CloseAppletUrlRequest request) {
        CloseAppletUrlResponse response = new CloseAppletUrlResponse();
        response.setCode("0");
        response.setMsg("链接关闭成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        // 1-链接关闭成功
        response.setCloseState(PaymentConstants.AppletCloseState.SUCCESS);
        
        return response;
    }
}
