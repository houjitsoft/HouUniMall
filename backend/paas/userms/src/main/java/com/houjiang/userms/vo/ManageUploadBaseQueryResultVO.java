package com.houjiang.userms.vo;

import lombok.Data;

/**
 * @ClassName ManageUploadBaseQueryResultVO
 * @Author Amos
 * @Date 2023/6/29 9:23
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageUploadBaseQueryResultVO {

    /**
     * 附件id
     */
    private Integer uploadId;

    /**
     * 附件的path
     */
    private String uploadPath;

    /**
     * 附件标题
     */
    private String uploadName;
}
