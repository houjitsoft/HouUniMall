package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.balance.BalanceQueryRequest;
import com.houjiang.payment.dto.balance.BalanceQueryResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.BalanceService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 余额服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public BalanceQueryResponse queryBalance(BalanceQueryRequest request) {
        log.info("开始查询余额, mchNo:{}, chanNo:{}", request.getMchNo(), request.getChanNo());
        
        try {
            // 1. 参数校验
            validateBalanceQuery(request);
            
            // 2. 校验渠道参数
            validateChannelExtra(request.getChanNo(), request.getChannelExtra());
            
            // 3. 填充基础参数
            fillBaseRequest(request);
            
            // 4. 生成签名
            signService.signRequest(request);
            
            // 5. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "balance/query";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("查询余额请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 6. 解析响应并验签
            BalanceQueryResponse response = JSONUtil.toBean(jsonResponse, BalanceQueryResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("查询余额响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 7. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("查询余额失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("查询余额失败: " + response.getMsg());
            }
            
            log.info("查询余额成功, chanNo:{}, amount:{}", request.getChanNo(), response.getAmount());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询余额异常, chanNo:" + request.getChanNo(), e);
            throw PaymentException.systemError("查询余额失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(BalanceQueryRequest request) {
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
    }
    
    /**
     * 验证余额查询必填参数
     */
    private void validateBalanceQuery(BalanceQueryRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道不能为空");
        }
        if (StrUtil.isBlank(request.getChannelExtra())) {
            throw PaymentException.bizError("渠道参数不能为空");
        }
        
        // 校验渠道编码
        if (!PaymentConstants.ChannelCode.ALLINPAY.equals(request.getChanNo())
                && !PaymentConstants.ChannelCode.YUNST2ISV.equals(request.getChanNo())
                && !PaymentConstants.ChannelCode.YUNST2YHZH.equals(request.getChanNo())) {
            throw PaymentException.bizError("不支持的渠道类型: " + request.getChanNo());
        }
    }
    
    /**
     * 校验渠道参数格式
     */
    private void validateChannelExtra(String chanNo, String channelExtra) {
        try {
            // 解析JSON
            if (!JSONUtil.isTypeJSON(channelExtra)) {
                throw PaymentException.bizError("渠道参数必须是JSON格式");
            }
            
            // 根据不同渠道校验必填字段
            if (PaymentConstants.ChannelCode.ALLINPAY.equals(chanNo)) {
                // allinpay渠道需要指定商户号
                // 例如：{"orgid":"66045xxxxxxx","cusid":"66046xxxxxxxxx"}
                // 这里简单校验，实际可以更严格
                if (!channelExtra.contains("cusid")) {
                    log.warn("allinpay渠道建议包含cusid参数");
                }
            } else if (PaymentConstants.ChannelCode.YUNST2ISV.equals(chanNo)) {
                // yunst2isv渠道需要指定会员及账户类型
                // 例如：{"bizUserId":"BS1738808XXX","acctType":"08"}
                if (!channelExtra.contains("bizUserId") || !channelExtra.contains("acctType")) {
                    throw PaymentException.bizError("yunst2isv渠道需要指定bizUserId和acctType");
                }
            } else if (PaymentConstants.ChannelCode.YUNST2YHZH.equals(chanNo)) {
                // yunst2yhzh渠道需指定会员及账户类型
                // 例如：{"bizUserId":"xxxxx","acctType":"PENDING_SETTLE"}
                if (!channelExtra.contains("bizUserId") || !channelExtra.contains("acctType")) {
                    throw PaymentException.bizError("yunst2yhzh渠道需要指定bizUserId和acctType");
                }
            }
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("校验渠道参数异常: {}", channelExtra, e);
            throw PaymentException.bizError("渠道参数格式错误");
        }
    }
    
    /**
     * 构建模拟响应（根据不同渠道返回不同字段）
     */
    private BalanceQueryResponse buildMockResponse(BalanceQueryRequest request) {
        BalanceQueryResponse response = new BalanceQueryResponse();
        response.setCode("0");
        response.setMsg("查询成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        
        if (PaymentConstants.ChannelCode.ALLINPAY.equals(request.getChanNo())) {
            // allinpay渠道必返：amount、cusid
            // 1000元
            response.setAmount(100000L);
            response.setCusid("66046xxxxxxxxx");
            
        } else if (PaymentConstants.ChannelCode.YUNST2ISV.equals(request.getChanNo())) {
            // yunst2isv渠道必返：amount、availableAmt、transitAmt
            // 总余额 = 可用余额 + 在途余额
            // 800元
            response.setAvailableAmt(80000L);
            // 200元
            response.setTransitAmt(20000L);
            // 1000元
            response.setAmount(100000L);
            
        } else if (PaymentConstants.ChannelCode.YUNST2YHZH.equals(request.getChanNo())) {
            // yunst2yhzh渠道返回：amount
            // 1000元
            response.setAmount(100000L);
        }
        
        return response;
    }
}
