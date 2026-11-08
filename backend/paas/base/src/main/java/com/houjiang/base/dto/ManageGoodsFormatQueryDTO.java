package com.houjiang.base.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName ManageGoodsFormatQueryDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/11 16:14
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGoodsFormatQueryDTO extends UserTokenBeanDTO {

    private String name;

    private String shopId;
}
