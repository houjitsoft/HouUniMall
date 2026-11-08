package com.houjiang.base.dto;

import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class NavSortLogoVO extends UserTokenBeanDTO {

    /**
     * 文案设置列表
     */
    private List<NavSortLogoDTO> logos;

}
