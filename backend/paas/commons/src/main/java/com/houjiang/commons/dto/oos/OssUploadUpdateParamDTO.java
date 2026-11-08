package com.houjiang.commons.dto.oos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文件
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OssUploadUpdateParamDTO {

    /**
     * 文件
     */
    private MultipartFile file;

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
     * 域名
     */
    private String endpoint;

    /**
     * token
     */
    private String token;

    /**
     * 文件大小
     */
    private byte[] fileBytes;

    /**
     * 店铺id
     */
    private Integer shopId;




}
