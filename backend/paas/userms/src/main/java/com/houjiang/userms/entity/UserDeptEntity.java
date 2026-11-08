package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门管理表
 *
 * @author Amos
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hou_user_dept")

public class UserDeptEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    
    private Long id;

    @TableField("org_id")
    
    private Long orgId;

    @TableField("dept_code")
    
    private String deptCode;

    @TableField("dept_name")
    
    private String deptName;

    @TableField("parent_id")
    
    private Long parentId;

    @TableField("dept_type")
    
    private String deptType;

    @TableField("manager_id")
    
    private Long managerId;

    @TableField("manager_name")
    
    private String managerName;

    @TableField("level")
    
    private Integer level;

    @TableField("sort_order")
    
    private Integer sortOrder;

    @TableField("status")
    
    private Integer status;

    @TableField("remark")
    
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    
    private Long createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    
    private Long updateBy;
}
