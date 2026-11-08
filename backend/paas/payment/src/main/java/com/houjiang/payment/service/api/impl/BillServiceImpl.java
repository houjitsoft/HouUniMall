package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.dto.bill.BillImportRequest;
import com.houjiang.payment.dto.bill.BillImportResponse;
import com.houjiang.payment.dto.bill.BillInfoDTO;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.BillService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账单服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public BillImportResponse importBill(BillImportRequest request) {
        log.info("开始导入账单, termNo:{}, billsInfo:{}", 
                request.getTermNo(), request.getBillsInfo());
        
        try {
            // 1. 参数校验
            validateBillImport(request);
            
            // 2. 解析账单信息
            List<BillInfoDTO> billList = parseBillsInfo(request.getBillsInfo());
            log.info("解析账单信息成功, 账单数量:{}", billList.size());
            
            // 3. 账单数量校验
            if (billList.size() > 1000) {
                throw PaymentException.bizError("账单数量超过最大限制1000笔");
            }
            
            // 4. 填充基础参数
            fillBaseRequest(request);
            
            // 5. 生成签名
            signService.signRequest(request);
            
            // 6. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "pay/billImport";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("账单导入请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 7. 解析响应并验签
            BillImportResponse response = JSONUtil.toBean(jsonResponse, BillImportResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("账单导入响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 8. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("账单导入失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("账单导入失败: " + response.getMsg());
            }
            
            // TODO: 9. 保存账单导入记录到数据库
            
            log.info("账单导入成功, importBatch:{}, billCount:{}", 
                    response.getImportBatch(), billList.size());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("账单导入异常, termNo:" + request.getTermNo(), e);
            throw PaymentException.systemError("账单导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(BillImportRequest request) {
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
     * 验证账单导入必填参数
     */
    private void validateBillImport(BillImportRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getTermNo())) {
            throw PaymentException.bizError("收款模板编号不能为空");
        }
        if (StrUtil.isBlank(request.getBillsInfo())) {
            throw PaymentException.bizError("账单信息不能为空");
        }
    }
    
    /**
     * 解析账单信息JSON字符串
     */
    private List<BillInfoDTO> parseBillsInfo(String billsInfoJson) {
        try {
            JSONArray jsonArray = JSONUtil.parseArray(billsInfoJson);
            return jsonArray.toList(BillInfoDTO.class);
        } catch (Exception e) {
            log.error("解析账单信息失败: {}", billsInfoJson, e);
            throw PaymentException.bizError("账单信息格式错误");
        }
    }
}
