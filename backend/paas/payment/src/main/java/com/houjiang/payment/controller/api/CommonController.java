package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.common.UploadFileRequest;
import com.houjiang.payment.dto.common.UploadFileResponse;
import com.houjiang.payment.service.api.CommonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 通用模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/comm")
@RequiredArgsConstructor
public class CommonController {
    
    private final CommonService commonService;
    
    /**
     * 文件上传
     * 上传文件并返回fileId
     * 
     * 使用场景:
     * 1. 上传证件材料(如营业执照、身份证等)
     * 2. 需要在其他接口中引用文件时使用fileId
     * 
     * 流程说明:
     * 第一步: 准备文件
     *        - 读取需要上传的文件
     *        - 计算文件流的MD5值
     *        - 将文件转为Base64编码字符串
     * 
     * 第二步: 调用本接口上传
     *        - 必填:fileType(文件类型)、fileMd5、file(文件流)
     *        - file参数不参与签名
     *        - 系统验证文件MD5值
     * 
     * 第三步: 获取fileId
     *        - 上传成功后返回fileId
     *        - fileId长期有效
     *        - 可在其他接口中使用该fileId引用文件
     * 
     * 参数说明:
     * 必填参数:
     * - fileType: 文件类型,3-证件材料
     * - fileMd5: 文件流的MD5值,用于验证文件完整性
     * - file: 文件流,通常为Base64编码字符串
     * 
     * 特殊说明:
     * 1. file参数不参与签名
     *    - 签名时需要临时排除file参数
     *    - 其他参数正常参与签名
     * 
     * 2. fileMd5验证
     *    - 必须提供正确的文件MD5值
     *    - 系统会计算文件流的MD5值并校验
     *    - MD5不匹配会返回错误
     * 
     * 3. fileId
     *    - 上传成功后返回唯一的fileId
     *    - fileId长期有效,可重复使用
     *    - 可在其他接口中通过fileId引用该文件
     * 
     * 4. 文件类型
     *    - 目前仅支持3-证件材料
     *    - 后续可能支持更多文件类型
     * 
     * 注意事项:
     * 1. file参数不参与签名,但fileMd5参与签名
     * 2. 文件大小建议不超过5MB
     * 3. 支持常见图片格式(jpg/png/pdf等)
     * 4. fileId长期有效,建议保存到数据库
     * 5. 相同文件重复上传会生成不同的fileId
     * 6. fileMd5必须与实际文件流匹配
     * 
     * @param request 文件上传请求
     * @return 文件上传响应
     */
    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@Valid @RequestBody UploadFileRequest request) {
        log.info("文件上传请求, fileType:{}", request.getFileType());
        return commonService.uploadFile(request);
    }
}
