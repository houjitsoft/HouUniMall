package com.houjiang.userms.dto;


import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * 数组信息
 * @author Amos
 */
@Data
public class MakeTagDTO extends UserTokenBeanDTO {

    /**
     * 用户id数组
     */
    private String[] userIdArray;

    /**
     * 会员标签id数组
     */
    private String[] tagIdArray;
}
