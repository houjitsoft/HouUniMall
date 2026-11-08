package com.houjiang.payment.service.api.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.dto.common.UploadFileRequest;
import com.houjiang.payment.dto.common.UploadFileResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.CommonService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 通用服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public UploadFileResponse uploadFile(UploadFileRequest request) {
        log.info("开始文件上传, fileType:{}, fileMd5:{}", 
                request.getFileType(), request.getFileMd5());
        
        try {
            // 1. 参数校验
            validateUploadFile(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 验证文件MD5
            validateFileMd5(request);
            
            // 4. 生成签名(注意:文件流不参与签名)
            String originalFile = request.getFile();
            // 临时置空,不参与签名
            request.setFile(null);
            signService.signRequest(request);
            // 恢复文件流
            request.setFile(originalFile);
            
            // 5. 调用通企付API - 使用multipart/form-data格式
            String apiUrl = paymentConfig.getApiUrl() + "comm/uploadFile";
            log.debug("文件上传请求: {}", apiUrl);
            
            // 解码Base64文件数据
            byte[] fileBytes = Base64.decode(request.getFile());
            
            // 构建 multipart/form-data 请求
            HttpResponse httpResponse = HttpRequest.post(apiUrl)
                    .form("orgId", request.getOrgId())
                    .form("mchNo", request.getMchNo())
                    .form("appId", request.getAppId())
                    .form("reqTime", request.getReqTime())
                    .form("version", request.getVersion())
                    .form("sign", request.getSign())
                    .form("signType", request.getSignType())
                    .form("fileType", request.getFileType())
                    .form("fileMd5", request.getFileMd5())
                    .form("file", fileBytes, "file.jpg")
                    .timeout(paymentConfig.getConnectTimeout())
                    .execute();
            
            int status = httpResponse.getStatus();
            String jsonResponse = httpResponse.body();
            
            log.debug("文件上传响应, status:{}, body:{}", status, jsonResponse);
            
            if (status != 200) {
                log.error("文件上传HTTP请求失败, status:{}, body:{}", status, jsonResponse);
                throw PaymentException.bizError("文件上传请求失败, HTTP状态码: " + status);
            }
            
            // 6. 解析响应并验签
            UploadFileResponse response = JSONUtil.toBean(jsonResponse, UploadFileResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("文件上传响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 7. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("文件上传失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("文件上传失败: " + response.getMsg());
            }
            
            // TODO: 8. 保存文件记录到数据库
            // FileRecord fileRecord = FileRecord.builder()
            //     .fileId(response.getFileId())
            //     .fileType(request.getFileType())
            //     .fileMd5(request.getFileMd5())
            //     .fileSize(fileBytes.length)
            //     .uploadTime(new Date())
            //     .build();
            // fileRecordMapper.insert(fileRecord);
            
            log.info("文件上传成功, fileId:{}", response.getFileId());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传异常", e);
            throw PaymentException.systemError("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(UploadFileRequest request) {
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
     * 验证文件上传必填参数
     */
    private void validateUploadFile(UploadFileRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getFileType())) {
            throw PaymentException.bizError("文件类型不能为空");
        }
        if (StrUtil.isBlank(request.getFileMd5())) {
            throw PaymentException.bizError("文件MD5值不能为空");
        }
        if (StrUtil.isBlank(request.getFile())) {
            throw PaymentException.bizError("文件流不能为空");
        }
        
        // 校验文件类型
        if (!"3".equals(request.getFileType())) {
            throw PaymentException.bizError("不支持的文件类型: " + request.getFileType() 
                    + ",目前仅支持3-证件材料");
        }
    }
    
    /**
     * 验证文件MD5值
     */
    private void validateFileMd5(UploadFileRequest request) {
        try {
            // 计算文件流的MD5值
            String calculatedMd5 = MD5.create().digestHex(request.getFile());
            
            // 与提供的MD5值比较(忽略大小写)
            if (!calculatedMd5.equalsIgnoreCase(request.getFileMd5())) {
                log.error("文件MD5校验失败, 期望:{}, 实际:{}", 
                        request.getFileMd5(), calculatedMd5);
                throw PaymentException.bizError("文件MD5校验失败,文件可能已损坏");
            }
        } catch (Exception e) {
            log.error("文件MD5校验异常", e);
            throw PaymentException.bizError("文件MD5校验失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建模拟响应
     */
    private UploadFileResponse buildMockResponse(UploadFileRequest request) {
        UploadFileResponse response = new UploadFileResponse();
        response.setCode("0");
        response.setMsg("上传成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        
        // 生成模拟fileId
        response.setFileId("FILE" + System.currentTimeMillis());
        
        return response;
    }
}
