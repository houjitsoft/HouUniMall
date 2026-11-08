package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.common.UploadFileRequest;
import com.houjiang.payment.dto.common.UploadFileResponse;

/**
 * 通用服务接口
 * 
 * @author Amos
 */
public interface CommonService {
    
    /**
     * 文件上传
     * 上传文件并返回fileId
     * 
     * @param request 文件上传请求
     * @return 文件上传响应
     */
    UploadFileResponse uploadFile(UploadFileRequest request);
}
