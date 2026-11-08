package com.houjiang.payment.service.api.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.notify.WithdrawNotifyDTO;
import com.houjiang.payment.dto.withdraw.WithdrawApplyRequest;
import com.houjiang.payment.dto.withdraw.WithdrawApplyResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.WithdrawService;
import com.houjiang.payment.util.TimeUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 提现服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public WithdrawApplyResponse applyWithdraw(WithdrawApplyRequest request) {
        log.info("开始提现申请, mchOrderNo:{}, chanNo:{}, amount:{}", 
                request.getMchOrderNo(), request.getChanNo(), request.getAmount());
        
        try {
            // 1. 参数校验
            validateWithdrawApply(request);
            
            // 2. 根据渠道校验必填参数
            validateChannelRequiredFields(request);
            
            // 3. 填充基础参数
            fillBaseRequest(request);
            
            // 4. 生成签名
            signService.signRequest(request);
            
            // 5. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "pay/withdrawApply";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("提现申请请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 6. 解析响应并验签
            WithdrawApplyResponse response = JSONUtil.toBean(jsonResponse, WithdrawApplyResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("提现申请响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 7. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("提现申请失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("提现申请失败: " + response.getMsg());
            }
            
            // TODO: 8. 保存提现记录到数据库
            
            log.info("提现申请成功, withdrawOrderId:{}, mchOrderNo:{}", 
                    response.getWithdrawOrderId(), response.getMchOrderNo());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("提现申请异常, mchOrderNo:" + request.getMchOrderNo(), e);
            throw PaymentException.systemError("提现申请失败: " + e.getMessage());
        }
    }
    
    @Override
    public String handleWithdrawNotify(HttpServletRequest httpRequest) {
        log.info("收到提现结果通知");
        
        try {
            // 1. 解析通知参数(获取全量参数以兼容未来新增字段)
            Map<String, String> params = parseNotifyParams(httpRequest);
            log.info("提现通知参数: {}", params);
            
            // 2. 转换为DTO对象
            WithdrawNotifyDTO notifyDTO = mapToWithdrawNotify(params);
            
            // 3. 基础参数校验
            validateWithdrawNotify(notifyDTO);
            
            // 4. 验证签名
            if (!signService.verifyNotify(params)) {
                log.error("提现通知签名验证失败, withdrawOrderId:{}, mchOrderNo:{}", 
                        notifyDTO.getWithdrawOrderId(), notifyDTO.getMchOrderNo());
                return PaymentConstants.NotifyResult.FAIL;
            }
            
            // 5. 幂等性检查(防止重复通知)
            if (isDuplicateWithdrawNotify(notifyDTO.getWithdrawOrderId(), notifyDTO.getMchOrderNo())) {
                log.info("提现通知已处理过(幂等), withdrawOrderId:{}, mchOrderNo:{}", 
                        notifyDTO.getWithdrawOrderId(), notifyDTO.getMchOrderNo());
                return PaymentConstants.NotifyResult.SUCCESS;
            }
            
            // TODO: 6. 查询本地提现订单记录
            // WithdrawOrder localOrder = withdrawOrderMapper.selectByOrderId(notifyDTO.getWithdrawOrderId());
            
            // TODO: 7. 验证订单状态和金额
            // if (localOrder == null) {
            //     log.error("提现订单不存在, withdrawOrderId:{}", notifyDTO.getWithdrawOrderId());
            //     return PaymentConstants.NotifyResult.FAIL;
            // }
            
            // TODO: 8. 更新提现订单状态
            // localOrder.setState(notifyDTO.getState());
            // localOrder.setSuccessTime(notifyDTO.getSuccessTime());
            // localOrder.setChannelOrderNo(notifyDTO.getChannelOrderNo());
            // if (notifyDTO.getState().equals(PaymentConstants.WithdrawStatus.FAIL)) {
            //     localOrder.setErrCode(notifyDTO.getErrCode());
            //     localOrder.setErrMsg(notifyDTO.getErrMsg());
            // }
            // withdrawOrderMapper.updateById(localOrder);
            
            // TODO: 9. 更新商户会员账户余额(如果提现成功)
            // if (notifyDTO.getState().equals(PaymentConstants.WithdrawStatus.SUCCESS)) {
            //     accountService.deductBalance(notifyDTO.getBizUserId(), notifyDTO.getAmount());
            // }
            
            // TODO: 10. 标记通知已处理(Redis缓存)
            // String notifyKey = "withdraw:notify:" + notifyDTO.getWithdrawOrderId();
            // redisTemplate.opsForValue().set(notifyKey, "1", 24, TimeUnit.HOURS);
            
            // TODO: 11. 执行业务回调通知
            // if (StrUtil.isNotBlank(localOrder.getCallbackUrl())) {
            //     callbackService.notifyMerchant(localOrder.getCallbackUrl(), notifyDTO);
            // }
            
            log.info("提现通知处理成功, withdrawOrderId:{}, mchOrderNo:{}, state:{}", 
                    notifyDTO.getWithdrawOrderId(), notifyDTO.getMchOrderNo(), notifyDTO.getState());
            
            // 必须返回小写"success"
            return PaymentConstants.NotifyResult.SUCCESS;
            
        } catch (Exception e) {
            log.error("提现通知处理异常", e);
            return PaymentConstants.NotifyResult.FAIL;
        }
    }
    
    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(WithdrawApplyRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        // 如果没有请求流水号，自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
        
        // 设置默认提现方式
        if (StrUtil.isBlank(request.getWithdrawType())) {
            if (PaymentConstants.ChannelCode.ALLINPAY.equals(request.getChanNo())) {
                // allinpay默认T1
                request.setWithdrawType(PaymentConstants.WithdrawType.T1);
            } else {
                // 其他渠道默认D0
                request.setWithdrawType(PaymentConstants.WithdrawType.D0);
            }
        }
    }
    
    /**
     * 验证提现申请必填参数
     */
    private void validateWithdrawApply(WithdrawApplyRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getMchOrderNo())) {
            throw PaymentException.bizError("商户提现订单号不能为空");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw PaymentException.bizError("提现金额必须大于0");
        }
        
        // 校验渠道编码
        if (!PaymentConstants.ChannelCode.ALLINPAY.equals(request.getChanNo())
                && !PaymentConstants.ChannelCode.YUNST2ISV.equals(request.getChanNo())
                && !PaymentConstants.ChannelCode.YUNST.equals(request.getChanNo())
                && !PaymentConstants.ChannelCode.YUNST2YHZH.equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道类型: " + request.getChanNo());
        }
        
        // 校验提现方式
        if (StrUtil.isNotBlank(request.getWithdrawType())) {
            if (!PaymentConstants.WithdrawType.D0.equals(request.getWithdrawType())
                    && !PaymentConstants.WithdrawType.D1.equals(request.getWithdrawType())
                    && !PaymentConstants.WithdrawType.T1.equals(request.getWithdrawType())) {
                throw PaymentException.bizError("提现方式只能是D0、D1或T1");
            }
        }
        
        // 校验银行卡属性
        if (request.getBankCardPro() != null) {
            if (!PaymentConstants.BankCardType.PERSONAL.equals(request.getBankCardPro())
                    && !PaymentConstants.BankCardType.ENTERPRISE.equals(request.getBankCardPro())) {
                throw PaymentException.bizError("银行卡属性只能是0(个人)或1(企业)");
            }
        }
    }
    
    /**
     * 根据渠道校验必填参数
     */
    private void validateChannelRequiredFields(WithdrawApplyRequest request) {
        String chanNo = request.getChanNo();
        
        if (PaymentConstants.ChannelCode.ALLINPAY.equals(chanNo)) {
            // allinpay渠道校验
            // channelExtra可选，notifyUrl不可用
            if (StrUtil.isNotBlank(request.getNotifyUrl())) {
                log.warn("allinpay渠道不支持notifyUrl参数");
            }
            
        } else if (PaymentConstants.ChannelCode.YUNST2ISV.equals(chanNo)) {
            // yunst2isv渠道必传：bizUserId、bankCardNo
            if (StrUtil.isBlank(request.getBizUserId())) {
                throw PaymentException.bizError("yunst2isv渠道必须传入bizUserId(商户会员编号)");
            }
            if (StrUtil.isBlank(request.getBankCardNo())) {
                throw PaymentException.bizError("yunst2isv渠道必须传入bankCardNo(提现银行卡号)");
            }
            
        } else if (PaymentConstants.ChannelCode.YUNST.equals(chanNo)) {
            // yunst渠道必传：bizUserId、bankCardNo、bankCardPro
            if (StrUtil.isBlank(request.getBizUserId())) {
                throw PaymentException.bizError("yunst渠道必须传入bizUserId(商户会员编号)");
            }
            if (StrUtil.isBlank(request.getBankCardNo())) {
                throw PaymentException.bizError("yunst渠道必须传入bankCardNo(提现银行卡号)");
            }
            if (request.getBankCardPro() == null) {
                throw PaymentException.bizError("yunst渠道必须传入bankCardPro(银行卡/账户属性)");
            }
            
        } else if (PaymentConstants.ChannelCode.YUNST2YHZH.equals(chanNo)) {
            // yunst2yhzh渠道必传：bizUserId
            if (StrUtil.isBlank(request.getBizUserId())) {
                throw PaymentException.bizError("yunst2yhzh渠道必须传入bizUserId(商户会员编号)");
            }
        }
        
        log.info("渠道必填参数校验通过, chanNo:{}", chanNo);
    }
    
    /**
     * 解析通知参数(获取全量参数)
     */
    private Map<String, String> parseNotifyParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (StrUtil.isNotBlank(paramValue)) {
                params.put(paramName, paramValue);
            }
        }
        
        return params;
    }
    
    /**
     * 将Map参数转换为WithdrawNotifyDTO
     */
    private WithdrawNotifyDTO mapToWithdrawNotify(Map<String, String> params) {
        WithdrawNotifyDTO dto = new WithdrawNotifyDTO();
        
        // 使用Hutool的BeanUtil进行属性拷贝,自动处理类型转换
        BeanUtil.fillBeanWithMap(params, dto, true);
        
        return dto;
    }
    
    /**
     * 验证提现通知必填参数
     */
    private void validateWithdrawNotify(WithdrawNotifyDTO notifyDTO) {
        if (StrUtil.isBlank(notifyDTO.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getMchOrderNo())) {
            throw PaymentException.bizError("商户提现订单号不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getWithdrawOrderId())) {
            throw PaymentException.bizError("通企付提现订单号不能为空");
        }
        if (notifyDTO.getAmount() == null || notifyDTO.getAmount() <= 0) {
            throw PaymentException.bizError("提现金额必须大于0");
        }
        if (notifyDTO.getState() == null) {
            throw PaymentException.bizError("提现状态不能为空");
        }
        
        // 验证状态值:只有2(成功)和3(失败)会发起通知
        if (!PaymentConstants.WithdrawStatus.SUCCESS.equals(notifyDTO.getState())
                && !PaymentConstants.WithdrawStatus.FAIL.equals(notifyDTO.getState())) {
            throw PaymentException.bizError("提现状态异常,只能是2(成功)或3(失败): " + notifyDTO.getState());
        }
        
        if (StrUtil.isBlank(notifyDTO.getBizUserId())) {
            throw PaymentException.bizError("商户会员编号不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getChannelOrderNo())) {
            throw PaymentException.bizError("渠道提现订单号不能为空");
        }
        
        log.info("提现通知参数校验通过, withdrawOrderId:{}, state:{}", 
                notifyDTO.getWithdrawOrderId(), notifyDTO.getState());
    }
    
    /**
     * 检查是否重复通知(幂等性)
     */
    private boolean isDuplicateWithdrawNotify(String withdrawOrderId, String mchOrderNo) {
        // TODO: 从Redis缓存检查通知是否已处理
        // String notifyKey = "withdraw:notify:" + withdrawOrderId;
        // return redisTemplate.hasKey(notifyKey);
        
        // 临时实现:总是返回false
        return false;
    }
}
