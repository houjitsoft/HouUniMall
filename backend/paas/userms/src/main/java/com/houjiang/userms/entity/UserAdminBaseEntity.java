package com.houjiang.userms.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * @ClassName
 * @Description  后台用户基础信息表
 * @Author Amos
 * @Date 2023-06-27 14:05:30
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_user_admin_base")
@Data
public class UserAdminBaseEntity  implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private String userId;
    /**
     * 用户帐号
     */
    private String userAccount;
    /**
     * 密码：使用用户中心-此处废弃
     */
    private String userPassword;
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
     * 用户Key
     */
    private String userKey;
    /**
     * 是否被封禁，0：未封禁，1：封禁
     */
    private Integer userDelete;
    /**
     * 用户权限组id
     */
    private Integer rightsGroupId;
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
     * 性别，默认0，0女1男
     */
    private Integer userGender;
    /**
     * 账号创建时间
     */
    private Date creatAccountTime;
    /**
     * 所属部门 名称
     */
    @TableField(exist = false)
    private String userDepartmentName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
