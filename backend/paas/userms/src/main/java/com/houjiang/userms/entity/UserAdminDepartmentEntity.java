package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 部门表
 * @TableName user_admin_department
 * @author Amos
 */
@TableName(value ="hou_user_admin_department")
@Data
public class UserAdminDepartmentEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 部门名称
     */
    private String userDepartment;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}