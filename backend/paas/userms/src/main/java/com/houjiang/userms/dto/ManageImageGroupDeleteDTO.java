package com.houjiang.userms.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName ManageImageGroupDeleteDTO
 * @Author Amos
 * @Date 2023/6/28 17:25
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageImageGroupDeleteDTO extends UserTokenBeanDTO {

    /**
     *附件id
     */
    private Integer albumId;
}
