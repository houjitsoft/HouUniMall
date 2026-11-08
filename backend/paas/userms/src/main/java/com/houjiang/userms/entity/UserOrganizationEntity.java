package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName UserOrganizationEntity
 * @Description 用户组织关联实体
 * @Author Amos
 * @Date 2024-12-19
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_user_organization")
@Data
public class UserOrganizationEntity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 是否主组织：1-是，0-否
     */
    private Integer isPrimary;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
