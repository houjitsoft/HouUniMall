package com.houjiang.userms.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class VkAddUserDTO extends PageHelper {

    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 来源  1 平台自己添加  2.通过成为微客条件
     */
    private Integer source;
    /**
     * 用户id
     */
    private String shareUserId;
    /**
     * 是否有上级
     */
    private Integer isSuperior;
    /**
     * 邀请人手机号
     */
    private String inviterMobile;
    /**
     * 用户类型 null全部 1.直接用户  2.间接用户
     */
    private Integer userType;
    /**
     * 是否是今日
     */
    private Integer isToday;
}
