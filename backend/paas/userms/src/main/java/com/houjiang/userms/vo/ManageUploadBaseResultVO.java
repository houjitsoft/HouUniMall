package com.houjiang.userms.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ManageUploadBaseResultVO
 * @Author Amos
 * @Date 2023/6/29 9:22
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageUploadBaseResultVO extends PageListVO {

    /**
     * 附件集合
     */
    private List<ManageUploadBaseQueryResultVO> images;

}
