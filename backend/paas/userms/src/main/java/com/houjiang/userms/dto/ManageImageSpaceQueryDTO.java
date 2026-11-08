package com.houjiang.userms.dto;

import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName ManageImageSpaceQueryDTO
 * @Author Amos
 * @Date 2023/6/29 9:11
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageImageSpaceQueryDTO extends PageUserTokenBeanDTO {

    /**
     * 分组id
     */
    private Integer albumId;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 上传类型
     */
    private String uploadType;

    /**
     * 附件标题
     */
    private String uploadName;

}
