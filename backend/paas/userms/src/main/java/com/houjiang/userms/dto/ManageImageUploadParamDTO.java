package com.houjiang.userms.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ManageImageUploadParamDTO
 * @Author Amos
 * @Date 2023/6/29 10:08
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageImageUploadParamDTO extends UserTokenBeanDTO {
    /**
     * 上传id
     */
    private Integer uploadId;

    /**
     * 附件id
     */
    private Integer albumId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 附件url前缀
     */
    private String uploadUrlPrefix;

    /**
     * 附件的path
     */
    private List<Map<String,String>> uploadPath;

    /**
     * 附件的path
     */
    private String path;

    /**
     * 原附件
     */
    private String uploadOriginal;

    /**
     * 源头-网站抓取
     */
    private String uploadSource;

    /**
     * 附件的类型image|video|
     */
    private String uploadType;

    /**
     * 原文件大小
     */
    private Integer uploadSize;

    /**
     * 上传的附件类型
     */
    private String uploadMimeType;

    /**
     * 附件日期
     */
    private Integer uploadTime;

    /**
     * 附件标题
     */
    private String uploadName;

    /**
     * 排序
     */
    private Integer uploadDisplayorder;
}
