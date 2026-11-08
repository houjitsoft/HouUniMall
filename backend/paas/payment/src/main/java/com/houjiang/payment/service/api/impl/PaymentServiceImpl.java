package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.pay.SmsConfirmRequest;
import com.houjiang.payment.dto.pay.SmsConfirmResponse;
import com.houjiang.payment.dto.pay.SmsResentRequest;
import com.houjiang.payment.dto.pay.SmsResentResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.PaymentService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public SmsConfirmResponse smsConfirm(SmsConfirmRequest request) {
        log.info("开始短信支付确认, mchOrderNo:{}, payOrderId:{}", 
                request.getMchOrderNo(), request.getPayOrderId());
        
        try {
            // 1. 参数校验
            validateSmsConfirm(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 生成签名
            signService.signRequest(request);
            
            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "pay/smsconfirm";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("短信支付确认请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 5. 解析响应并验签
            SmsConfirmResponse response = JSONUtil.toBean(jsonResponse, SmsConfirmResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("短信支付确认响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("短信支付确认失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("短信支付确认失败: " + response.getMsg());
            }
            
            // TODO: 7. 查询本地订单记录
            // PayOrder localOrder = payOrderMapper.selectByPayOrderId(request.getPayOrderId());
            
            // TODO: 8. 更新订单状态
            // localOrder.setState(response.getState());
            // payOrderMapper.updateById(localOrder);
            
            // TODO: 9. 如果支付成功,触发业务回调
            // if (PaymentConstants.OrderStatus.PAY_SUCCESS.equals(response.getState())) {
            //     callbackService.notifyMerchant(localOrder);
            // }
            
            log.info("短信支付确认成功, payOrderId:{}, state:{}", 
                    response.getPayOrderId(), response.getState());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("短信支付确认异常", e);
            throw PaymentException.systemError("短信支付确认失败: " + e.getMessage());
        }
    }
    
    @Override
    public SmsResentResponse smsResent(SmsResentRequest request) {
        log.info("开始重新获取支付确认短信, mchOrderNo:{}, payOrderId:{}", 
                request.getMchOrderNo(), request.getPayOrderId());
        
        try {
            // 1. 参数校验
            validateSmsResent(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 生成签名
            signService.signRequest(request);
            
            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "pay/smsresent";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("重新获取支付确认短信请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 5. 解析响应并验签
            SmsResentResponse response = JSONUtil.toBean(jsonResponse, SmsResentResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("重新获取短信响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("重新获取短信失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("重新获取短信失败: " + response.getMsg());
            }
            
            // TODO: 7. 记录短信重发次数
            // payOrderMapper.incrementSmsResentCount(request.getPayOrderId());
            
            // TODO: 8. 限制重发次数(例如3次)
            // if (resentCount >= 3) {
            //     throw PaymentException.bizError("短信重发次数超限,请重新下单");
            // }
            
            log.info("重新获取支付确认短信成功, payOrderId:{}", response.getPayOrderId());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("重新获取支付确认短信异常", e);
            throw PaymentException.systemError("重新获取支付确认短信失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充请求基础参数(SmsConfirmRequest)
     */
    private void fillBaseRequest(SmsConfirmRequest request) {
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
     * 填充请求基础参数(SmsResentRequest)
     */
    private void fillBaseRequest(SmsResentRequest request) {
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
     * 验证短信支付确认必填参数
     */
    private void validateSmsConfirm(SmsConfirmRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        
        // 商户订单号和支付订单号二选一
        if (StrUtil.isBlank(request.getMchOrderNo()) 
                && StrUtil.isBlank(request.getPayOrderId())) {
            throw PaymentException.bizError("商户订单号和支付订单号至少填写一个");
        }
        
        if (StrUtil.isBlank(request.getAgreeId())) {
            throw PaymentException.bizError("协议编号不能为空");
        }
        if (StrUtil.isBlank(request.getVerifyCode())) {
            throw PaymentException.bizError("短信验证码不能为空");
        }
    }
    
    /**
     * 验证重新获取短信必填参数
     */
    private void validateSmsResent(SmsResentRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        
        // 商户订单号和支付订单号二选一
        if (StrUtil.isBlank(request.getMchOrderNo()) 
                && StrUtil.isBlank(request.getPayOrderId())) {
            throw PaymentException.bizError("商户订单号和支付订单号至少填写一个");
        }
        
        if (StrUtil.isBlank(request.getAgreeId())) {
            throw PaymentException.bizError("协议编号不能为空");
        }
    }
    
    /**
     * 构建模拟响应(短信支付确认)
     */
    private SmsConfirmResponse buildMockConfirmResponse(SmsConfirmRequest request) {
        SmsConfirmResponse response = new SmsConfirmResponse();
        response.setCode("0");
        response.setMsg("支付成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        
        // 优先使用支付订单号
        if (StrUtil.isNotBlank(request.getPayOrderId())) {
            response.setPayOrderId(request.getPayOrderId());
        } else {
            response.setPayOrderId("P" + System.currentTimeMillis());
        }
        
        if (StrUtil.isNotBlank(request.getMchOrderNo())) {
            response.setMchOrderNo(request.getMchOrderNo());
        } else {
            response.setMchOrderNo("MCH" + System.currentTimeMillis());
        }
        
        // 2-支付成功
        response.setState(PaymentConstants.OrderStatus.PAY_SUCCESS);
        
        return response;
    }
    
    /**
     * 构建模拟响应(重新获取短信)
     */
    private SmsResentResponse buildMockSmsResentResponse(SmsResentRequest request) {
        SmsResentResponse response = new SmsResentResponse();
        response.setCode("0");
        response.setMsg("短信发送成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        
        // 优先使用支付订单号
        if (StrUtil.isNotBlank(request.getPayOrderId())) {
            response.setPayOrderId(request.getPayOrderId());
        } else {
            response.setPayOrderId("P" + System.currentTimeMillis());
        }
        
        // 如果请求中有payData,原样返回
        if (StrUtil.isNotBlank(request.getPayData())) {
            response.setPayData(request.getPayData());
        } else {
            // 模拟payData
            response.setPayData("mock_pay_data_" + System.currentTimeMillis());
        }
        
        return response;
    }
}
