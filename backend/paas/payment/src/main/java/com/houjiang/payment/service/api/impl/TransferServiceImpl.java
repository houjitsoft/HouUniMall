package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.transfer.TransferOrderRequest;
import com.houjiang.payment.dto.transfer.TransferOrderResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.TransferService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 转账服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public TransferOrderResponse transferOrder(TransferOrderRequest request) {
        log.info("开始转账, mchOrderNo:{}, amount:{}, sendAccountNo:{}, accountNo:{}", 
                request.getMchOrderNo(), request.getAmount(), 
                request.getSendAccountNo(), request.getAccountNo());
        
        try {
            // 1. 参数校验
            validateTransferOrder(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 生成签名
            signService.signRequest(request);
            
            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "transferOrder";
            String jsonRequest = cn.hutool.json.JSONUtil.toJsonStr(request);
            log.debug("转账请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 5. 解析响应并验签
            TransferOrderResponse response = cn.hutool.json.JSONUtil.toBean(jsonResponse, TransferOrderResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("转账响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("转账失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("转账失败: " + response.getMsg());
            }
            
            // TODO: 7. 保存转账记录到数据库
            
            // TODO: 8. 若未返回终态,需要通过统一订单查询接口查询转账结果
            
            log.info("转账成功, transferId:{}, state:{}", 
                    response.getTransferId(), response.getState());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("转账异常", e);
            throw PaymentException.systemError("转账失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(TransferOrderRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        // 默认币种为人民币
        if (StrUtil.isBlank(request.getCurrency())) {
            request.setCurrency(PaymentConstants.CURRENCY_CNY);
        }
        
        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
    }
    
    /**
     * 验证转账必填参数
     */
    private void validateTransferOrder(TransferOrderRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getMchOrderNo())) {
            throw PaymentException.bizError("商户转账订单号不能为空");
        }
        if (StrUtil.isBlank(request.getIfCode())) {
            throw PaymentException.bizError("支付接口代码不能为空");
        }
        if (StrUtil.isBlank(request.getEntryType())) {
            throw PaymentException.bizError("入账方式不能为空");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw PaymentException.bizError("转账金额必须大于0");
        }
        if (StrUtil.isBlank(request.getCurrency())) {
            throw PaymentException.bizError("货币代码不能为空");
        }
        if (StrUtil.isBlank(request.getSendAccountNo())) {
            throw PaymentException.bizError("转账账户不能为空");
        }
        if (StrUtil.isBlank(request.getAccountNo())) {
            throw PaymentException.bizError("收款账户不能为空");
        }
        if (StrUtil.isBlank(request.getTransferDesc())) {
            throw PaymentException.bizError("转账备注不能为空");
        }
        
        // 校验支付接口代码(目前仅支持allinpay)
        if (!PaymentConstants.ChannelCode.ALLINPAY.equals(request.getIfCode())) {
            throw PaymentException.bizError("不支持的支付接口代码: " + request.getIfCode() 
                    + ",目前仅支持allinpay");
        }
        
        // 校验入账方式(仅支持收银宝余额)
        if (!PaymentConstants.EntryType.SYB_CASH.equals(request.getEntryType())) {
            throw PaymentException.bizError("不支持的入账方式: " + request.getEntryType() 
                    + ",仅支持收银宝余额(SYB_CASH)");
        }
        
        // 校验货币代码
        if (!PaymentConstants.CURRENCY_CNY.equals(request.getCurrency())) {
            throw PaymentException.bizError("不支持的货币代码: " + request.getCurrency() 
                    + ",仅支持人民币(CNY)");
        }
        
        // 校验出金账户和入金账户不能相同
        if (request.getSendAccountNo().equals(request.getAccountNo())) {
            throw PaymentException.bizError("转账账户和收款账户不能相同");
        }
    }
    
    /**
     * 构建模拟响应
     */
    private TransferOrderResponse buildMockResponse(TransferOrderRequest request) {
        TransferOrderResponse response = new TransferOrderResponse();
        response.setCode("0");
        response.setMsg("转账成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        response.setTransferId("TF" + System.currentTimeMillis());
        response.setChannelOrderNo("CH" + System.currentTimeMillis());
        response.setAccountNo(request.getAccountNo());
        response.setAmount(request.getAmount());
        response.setMchOrderNo(request.getMchOrderNo());
        // 2-转账成功
        response.setState(PaymentConstants.TransferStatus.SUCCESS);
        
        // 原样返回可选字段
        if (StrUtil.isNotBlank(request.getAccountName())) {
            response.setAccountName(request.getAccountName());
        }
        if (StrUtil.isNotBlank(request.getBankName())) {
            response.setBankName(request.getBankName());
        }
        
        return response;
    }
}
