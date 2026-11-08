package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.statement.StatementDownloadRequest;
import com.houjiang.payment.dto.statement.StatementDownloadResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.StatementService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 对账服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public StatementDownloadResponse downloadStatement(StatementDownloadRequest request) {
        log.info("开始下载对账文件, mchNo:{}, day:{}, billType:{}, fileType:{}", 
                request.getMchNo(), request.getDay(), request.getBillType(), request.getFileType());
        
        try {
            // 1. 参数校验
            validateStatementDownload(request);
            
            // 2. 校验日期格式和时间限制
            validateDownloadTime(request.getDay());
            
            // 3. 填充基础参数
            fillBaseRequest(request);
            
            // 4. 生成签名
            signService.signRequest(request);
            
            // 5. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "accountstatement/getOrderFile";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("下载对账文件请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 6. 解析响应并验签
            StatementDownloadResponse response = JSONUtil.toBean(jsonResponse, StatementDownloadResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("下载对账文件响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 7. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("下载对账文件失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("下载对账文件失败: " + response.getMsg());
            }
            
            log.info("对账文件下载成功, fileUrl:{}", response.getFileUrl());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("对账文件下载异常, day:" + request.getDay(), e);
            throw PaymentException.systemError("对账文件下载失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(StatementDownloadRequest request) {
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
        
        // 设置默认值
        if (StrUtil.isBlank(request.getFileType())) {
            request.setFileType(PaymentConstants.FileType.CSV);
        }
        if (request.getBillType() == null) {
            request.setBillType(PaymentConstants.BillType.PAY);
        }
    }
    
    /**
     * 验证对账文件下载必填参数
     */
    private void validateStatementDownload(StatementDownloadRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getDay())) {
            throw PaymentException.bizError("交易日期不能为空");
        }
        
        // 校验日期格式
        try {
            LocalDate.parse(request.getDay(), DATE_FORMATTER);
        } catch (Exception e) {
            throw PaymentException.bizError("交易日期格式错误，应为yyyy-MM-dd");
        }
        
        // 校验文件类型
        if (StrUtil.isNotBlank(request.getFileType())) {
            if (!PaymentConstants.FileType.CSV.equals(request.getFileType()) 
                    && !PaymentConstants.FileType.EXCEL.equals(request.getFileType())) {
                throw PaymentException.bizError("文件类型只能是CSV或EXCEL");
            }
        }
        
        // 校验账单类型
        if (request.getBillType() != null) {
            if (request.getBillType() < 1 || request.getBillType() > 3) {
                throw PaymentException.bizError("账单类型只能是1(支付)、2(分账)或3(预消费)");
            }
        }
    }
    
    /**
     * 校验下载时间限制
     * D+1日11点之后可下载D日的对账文件
     */
    private void validateDownloadTime(String day) {
        try {
            LocalDate requestDate = LocalDate.parse(day, DATE_FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            
            // 检查日期是否是未来日期
            if (requestDate.isAfter(today)) {
                throw PaymentException.bizError("不能下载未来日期的对账文件");
            }
            
            // 检查是否是今天的对账文件
            if (requestDate.isEqual(today)) {
                throw PaymentException.bizError("当天对账文件需在次日11点后下载");
            }
            
            // 检查是否是昨天的对账文件，且当前时间是否已过11点
            if (requestDate.isEqual(today.minusDays(1))) {
                if (now.getHour() < 11) {
                    throw PaymentException.bizError("对账文件需在次日11点后下载，当前时间未到");
                }
            }
            
            log.info("对账文件下载时间校验通过, requestDate:{}, now:{}", requestDate, now);
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("校验下载时间异常", e);
            throw PaymentException.bizError("交易日期格式错误");
        }
    }
}
