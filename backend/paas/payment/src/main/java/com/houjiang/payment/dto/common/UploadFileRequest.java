package com.houjiang.payment.dto.common;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件上传请求DTO
 * 接口：文件上传
 * 请求地址：/comm/uploadFile
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UploadFileRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商id(可选)
     * 用于服务商代发起交易,传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     * 默认为企业appId,若为服务商模式,需传入服务商appId
     */
    private String appId;
    
    /**
     * 接口版本号
     */
    private String version;
    
    /**
     * 文件类型(必填)
     * 3：证件材料
     */
    private String fileType;
    
    /**
     * 文件流的MD5值(必填)
     */
    private String fileMd5;
    
    /**
     * 文件流(必填)
     * 注意:文件流不参与签名
     */
    private String file;
}
