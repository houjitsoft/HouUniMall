package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限组表
 * @TableName user_admin_rights_group
 * @author Amos
 */
@TableName(value ="hou_user_admin_rights_group")
@Data
public class UserAdminRightsGroupEntity implements Serializable {
    /**
     * 权限组id
     */
    @TableId(type = IdType.AUTO)
    private Short rightsGroupId;

    /**
     * 权限组名称
     */
    private String rightsGroupName;

    /**
     * 创建时间
     */
    private Date rightsGroupAddTime;

    /**
     * 逗号分隔所有权限
     */
    private String rightsLimitsAll;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}