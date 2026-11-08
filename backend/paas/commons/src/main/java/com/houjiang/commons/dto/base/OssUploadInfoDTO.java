package com.houjiang.commons.dto.base;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * @ClassName OssUploadInfoDTO
 * @Description 上传信息
 * @Author Amos
 * @Date 2023/5/18 17:29
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OssUploadInfoDTO extends UserTokenBeanDTO {

    /**
     * 文件的base64格式
     */
    private String base64Str;

    /**
     * 文件后缀 eg: .docx
     */
    private String streamName;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 1图片2视频
     */
    private Integer type;

    /**
     * 端点
     */
    private String endpoint;



}
