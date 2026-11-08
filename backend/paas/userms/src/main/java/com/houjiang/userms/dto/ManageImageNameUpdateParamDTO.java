package com.houjiang.userms.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName ManageImageNameUpdateParamDTO
 * @Author Amos
 * @Date 2023/6/29 10:02
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageImageNameUpdateParamDTO extends UserTokenBeanDTO {

    /**
     * 附件id
     */
    private Integer uploadId;

    /**
     * 附件名
     */
    private String uploadName;
}
