package com.houjiang.userms.vo;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * 用户分页信息
 * @author Amos
 */
@Data
public class UserPersonalBaseVO extends PageHelper {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户账户
     */
    private String userAccount;

    /**
     * 真实姓名
     */
    private String userRealname;

    /**
     * 昵称
     */
    private String userNickname;

    /**
     * 联系方式
     */
    private String userMobile;

    /**
     * 性别，0女1男
     */
    private Integer userGender;

    /**
     * 用户头像
     */
    private String headPortrait;

    /**
     * 职位
     */
    private String userPosition;

    /**
     * 所属部门
     */
    private String userDepartment;

    /**
     * 所属部门
     */
    private String userDepartmentName;

    /**
     * 用户权限组id
     */
    private Integer rightsGroupId;

    /**
     * 是否被封禁，0：未封禁，1封禁
     */
    private Integer userDelete;

    /**
     * 所属子站， 0总站
     */
    private Integer subSiteId;

    /**
     * 权限组名称
     */
    private String rightsGroupName;

    /**
     * token
     */
    private String token;

    /**
     * 密码
     */
    private String password;

}
