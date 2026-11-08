package com.houjiang.base.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName QuCommonsParam.java
 * @Description TODO
 * @createTime 2021年08月18日 14:36:00
 */
@Data
public class QuCommonsParamDTO extends UserTokenBeanDTO {

    /**
     * 商城id数组
     */
    private Integer[] commonIds;

}
