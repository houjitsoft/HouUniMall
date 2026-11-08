package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.quickpay.QuickPayAgreeRequest;
import com.houjiang.payment.dto.quickpay.QuickPayAgreeResponse;
import com.houjiang.payment.dto.quickpay.QuickPayUnbindRequest;
import com.houjiang.payment.dto.quickpay.QuickPayUnbindResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.QuickPayService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 快捷支付服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuickPayServiceImpl implements QuickPayService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public QuickPayAgreeResponse quickPayAgree(QuickPayAgreeRequest request) {
        log.info("开始快捷支付绑卡签约, agreeType:{}, merUserId:{}, acctNo:{}", 
                request.getAgreeType(), request.getMerUserId(), 
                maskCardNo(request.getAcctNo()));
        
        try {
            // 1. 参数校验
            validateQuickPayAgree(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 根据签约类型进行不同校验
            if (PaymentConstants.QuickPayAgreeType.APPLY.equals(request.getAgreeType())) {
                // 签约申请校验
                validateApply(request);
            } else if (PaymentConstants.QuickPayAgreeType.CONFIRM.equals(request.getAgreeType())) {
                // 签约确认校验
                validateConfirm(request);
            }
            
            // 4. 生成签名
            signService.signRequest(request);
            
            // 5. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "quickpay/agree";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("快捷支付绑卡签约请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 6. 解析响应并验签
            QuickPayAgreeResponse response = JSONUtil.toBean(jsonResponse, QuickPayAgreeResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("快捷支付绑卡签约响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 7. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("快捷支付绑卡签约失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("快捷支付绑卡签约失败: " + response.getMsg());
            }
            
            // TODO: 8. 保存签约记录到数据库
            
            log.info("快捷支付绑卡签约成功, agreeType:{}, merUserId:{}", 
                    request.getAgreeType(), request.getMerUserId());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("快捷支付绑卡签约异常", e);
            throw PaymentException.systemError("快捷支付绑卡签约失败: " + e.getMessage());
        }
    }
    
    @Override
    public QuickPayUnbindResponse quickPayUnbind(QuickPayUnbindRequest request) {
        log.info("开始快捷解绑, agreeId:{}, chanNo:{}", 
                request.getAgreeId(), request.getChanNo());
        
        try {
            // 1. 参数校验
            validateQuickPayUnbind(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 生成签名
            signService.signRequest(request);
            
            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "quickpay/unbind";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("快捷解绑请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 5. 解析响应并验签
            QuickPayUnbindResponse response = JSONUtil.toBean(jsonResponse, QuickPayUnbindResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("快捷解绑响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("快捷解绑失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("快捷解绑失败: " + response.getMsg());
            }
            
            // TODO: 7. 查询本地签约记录
            // QuickPayAgreement agreement = agreementMapper.selectByAgreeId(request.getAgreeId());
            
            // TODO: 8. 更新签约状态为已解绑
            // agreement.setStatus(2); // 0-未签约, 1-已签约, 2-已解绑
            // agreementMapper.updateById(agreement);
            
            // TODO: 9. 记录解绑日志
            // unbindLogMapper.insert(UnbindLog.builder()
            //     .agreeId(request.getAgreeId())
            //     .chanNo(request.getChanNo())
            //     .unbindTime(new Date())
            //     .build());
            
            log.info("快捷解绑成功, agreeId:{}, merUserId:{}", 
                    request.getAgreeId(), response.getMerUserId());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("快捷解绑异常", e);
            throw PaymentException.systemError("快捷解绑失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充请求基础参数(QuickPayAgreeRequest)
     */
    private void fillBaseRequest(QuickPayAgreeRequest request) {
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
     * 填充请求基础参数(QuickPayUnbindRequest)
     */
    private void fillBaseRequest(QuickPayUnbindRequest request) {
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
     * 验证快捷支付绑卡签约必填参数
     */
    private void validateQuickPayAgree(QuickPayAgreeRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getAgreeType())) {
            throw PaymentException.bizError("操作类型不能为空");
        }
        if (StrUtil.isBlank(request.getMerUserId())) {
            throw PaymentException.bizError("商户自定义用户号不能为空");
        }
        if (StrUtil.isBlank(request.getAcctType())) {
            throw PaymentException.bizError("卡类型不能为空");
        }
        if (StrUtil.isBlank(request.getAcctNo())) {
            throw PaymentException.bizError("银行卡号不能为空");
        }
        if (StrUtil.isBlank(request.getAcctName())) {
            throw PaymentException.bizError("户名不能为空");
        }
        if (StrUtil.isBlank(request.getIdentityType())) {
            throw PaymentException.bizError("证件类型不能为空");
        }
        if (StrUtil.isBlank(request.getIdNo())) {
            throw PaymentException.bizError("证件号不能为空");
        }
        if (StrUtil.isBlank(request.getPhone())) {
            throw PaymentException.bizError("银行预留手机号码不能为空");
        }
        
        // 校验渠道(目前仅支持allinpay)
        if (!PaymentConstants.ChannelCode.ALLINPAY.equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道: " + request.getChanNo() 
                    + ",目前仅支持收银宝渠道(allinpay)");
        }
        
        // 校验操作类型
        if (!PaymentConstants.QuickPayAgreeType.APPLY.equals(request.getAgreeType())
                && !PaymentConstants.QuickPayAgreeType.CONFIRM.equals(request.getAgreeType())) {
            throw PaymentException.bizError("不支持的操作类型: " + request.getAgreeType() 
                    + ",仅支持1-签约申请或2-签约确认");
        }
        
        // 校验卡类型
        if (!PaymentConstants.AcctType.DEBIT_CARD.equals(request.getAcctType())
                && !PaymentConstants.AcctType.CREDIT_CARD.equals(request.getAcctType())) {
            throw PaymentException.bizError("不支持的卡类型: " + request.getAcctType() 
                    + ",仅支持00-借记卡或02-准贷记卡/贷记卡");
        }
        
        // 校验证件类型
        if (!PaymentConstants.IdentityType.ID_CARD.equals(request.getIdentityType())
                && !PaymentConstants.IdentityType.PASSPORT.equals(request.getIdentityType())
                && !PaymentConstants.IdentityType.HK_MACAU.equals(request.getIdentityType())
                && !PaymentConstants.IdentityType.TAIWAN.equals(request.getIdentityType())) {
            throw PaymentException.bizError("不支持的证件类型: " + request.getIdentityType());
        }
        
        // 校验证件号末位X必须大写
        if (request.getIdNo().endsWith("x")) {
            throw PaymentException.bizError("证件号末位X必须大写");
        }
    }
    
    /**
     * 验证快捷解绑必填参数
     */
    private void validateQuickPayUnbind(QuickPayUnbindRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getAgreeId())) {
            throw PaymentException.bizError("协议编号不能为空");
        }
        
        // 校验渠道(目前仅支持allinpay)
        if (!PaymentConstants.ChannelCode.ALLINPAY.equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道: " + request.getChanNo() 
                    + ",目前仅支持收银宝渠道(allinpay)");
        }
    }
    
    /**
     * 验证签约申请特定参数
     */
    private void validateApply(QuickPayAgreeRequest request) {
        // 签约申请时,不需要验证码和thpInfo
        if (StrUtil.isNotBlank(request.getVerifyCode())) {
            log.warn("签约申请时不需要短信验证码");
        }
        if (StrUtil.isNotBlank(request.getThpInfo())) {
            log.warn("签约申请时不需要thpInfo");
        }
    }
    
    /**
     * 验证签约确认特定参数
     */
    private void validateConfirm(QuickPayAgreeRequest request) {
        // 签约确认时,必须有验证码
        if (StrUtil.isBlank(request.getVerifyCode())) {
            throw PaymentException.bizError("签约确认时短信验证码不能为空");
        }
        
        // 如果申请时返回了thpInfo,确认时必须原样带上
        // 这里暂不强制校验,因为可能申请接口没有返回thpInfo
        if (StrUtil.isBlank(request.getThpInfo())) {
            log.warn("签约确认时建议携带thpInfo");
        }
    }
    
    /**
     * 构建模拟响应(签约)
     */
    private QuickPayAgreeResponse buildMockAgreeResponse(QuickPayAgreeRequest request) {
        QuickPayAgreeResponse response = new QuickPayAgreeResponse();
        response.setCode("0");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        response.setMerUserId(request.getMerUserId());
        
        if (PaymentConstants.QuickPayAgreeType.APPLY.equals(request.getAgreeType())) {
            // 签约申请:返回thpInfo
            response.setMsg("签约申请成功,已发送短信验证码");
            response.setThpInfo("THP" + System.currentTimeMillis());
        } else {
            // 签约确认:返回agreeId、bankCode、bankName
            response.setMsg("签约确认成功");
            response.setAgreeId("AG" + System.currentTimeMillis());
            response.setBankCode("CCB");
            response.setBankName("中国建设银行");
        }
        
        return response;
    }
    
    /**
     * 构建模拟响应(解绑)
     */
    private QuickPayUnbindResponse buildMockUnbindResponse(QuickPayUnbindRequest request) {
        QuickPayUnbindResponse response = new QuickPayUnbindResponse();
        response.setCode("0");
        response.setMsg("解绑成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        
        // 模拟返回商户用户号
        response.setMerUserId("USER" + System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 脱敏银行卡号(仅显示前6位和后4位)
     */
    private String maskCardNo(String cardNo) {
        if (StrUtil.isBlank(cardNo) || cardNo.length() < 10) {
            return cardNo;
        }
        return cardNo.substring(0, 6) + "****" + cardNo.substring(cardNo.length() - 4);
    }
}
