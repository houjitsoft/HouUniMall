package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.instalment.CalFeeRequest;
import com.houjiang.payment.dto.instalment.CalFeeResponse;
import com.houjiang.payment.dto.instalment.InstalmentDetailDTO;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.InstalmentService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 分期服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstalmentServiceImpl implements InstalmentService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public CalFeeResponse calculateFee(CalFeeRequest request) {
        log.info("开始计算分期利息, amount:{}, wayCode:{}", 
                request.getAmount(), request.getWayCode());
        
        try {
            // 1. 参数校验
            validateCalFee(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 生成签名
            signService.signRequest(request);
            
            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "instalment/calFee";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("计算分期利息请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 5. 解析响应并验签
            CalFeeResponse response = JSONUtil.toBean(jsonResponse, CalFeeResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("计算分期利息响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("计算分期利息失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("计算分期利息失败: " + response.getMsg());
            }
            
            log.info("计算分期利息成功, 分期方案数:{}", 
                    response.getInstalmentList() != null ? response.getInstalmentList().size() : 0);
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("计算分期利息异常", e);
            throw PaymentException.systemError("计算分期利息失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(CalFeeRequest request) {
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
     * 验证分期利息计算必填参数
     */
    private void validateCalFee(CalFeeRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw PaymentException.bizError("支付金额必须大于0");
        }
        if (StrUtil.isBlank(request.getWayCode())) {
            throw PaymentException.bizError("支付方式不能为空");
        }
        
        // 校验支付方式(目前仅支持京东白条分期)
        if (!PaymentConstants.WayCode.JDBT_PAY.equals(request.getWayCode())) {
            throw PaymentException.bizError("不支持的支付方式: " + request.getWayCode() 
                    + ",目前仅支持京东白条分期(JDBT_PAY)");
        }
    }
    
    /**
     * 构建模拟响应
     * 京东白条典型分期方案: 3期、6期、12期、24期
     */
    private CalFeeResponse buildMockResponse(CalFeeRequest request) {
        CalFeeResponse response = new CalFeeResponse();
        response.setCode("0");
        response.setMsg("计算成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        
        // 构建分期明细列表
        List<InstalmentDetailDTO> instalmentList = new ArrayList<>();
        
        // 京东白条典型分期方案和利率
        // 3期: 1.5%, 6期: 3.0%, 12期: 6.0%, 24期: 12.0%
        int[] periods = {3, 6, 12, 24};
        double[] rates = {0.015, 0.030, 0.060, 0.120};
        
        for (int i = 0; i < periods.length; i++) {
            InstalmentDetailDTO detail = calculateInstalmentDetail(
                    request.getAmount(), periods[i], rates[i]);
            instalmentList.add(detail);
        }
        
        response.setInstalmentList(instalmentList);
        
        return response;
    }
    
    /**
     * 计算单个分期方案的明细
     * 
     * @param totalAmount 总金额,单位:分
     * @param periods 期数
     * @param rate 利率(如0.015表示1.5%)
     * @return 分期明细
     */
    private InstalmentDetailDTO calculateInstalmentDetail(Long totalAmount, int periods, double rate) {
        InstalmentDetailDTO detail = new InstalmentDetailDTO();
        detail.setInstalmentNum(periods);
        
        // 计算总利息 = 总金额 * 利率
        BigDecimal amount = new BigDecimal(totalAmount);
        BigDecimal rateDecimal = new BigDecimal(rate);
        BigDecimal totalInterest = amount.multiply(rateDecimal)
                .setScale(0, RoundingMode.HALF_UP);
        
        // 计算每期利息 = 总利息 / 期数
        BigDecimal periodsDecimal = new BigDecimal(periods);
        BigDecimal perInterest = totalInterest.divide(periodsDecimal, 0, RoundingMode.HALF_UP);
        
        // 计算每期还款金额 = (总金额 + 总利息) / 期数
        BigDecimal totalWithInterest = amount.add(totalInterest);
        BigDecimal perAmount = totalWithInterest.divide(periodsDecimal, 0, RoundingMode.HALF_UP);
        
        detail.setInstalmentRepInt(perInterest.longValue());
        detail.setInstalmentRepAmt(perAmount.longValue());
        
        return detail;
    }
}
