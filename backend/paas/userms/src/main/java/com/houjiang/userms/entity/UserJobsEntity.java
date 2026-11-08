package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 岗位管理表
 *
 * @author Amos
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hou_user_jobs")
public class UserJobsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("org_id")
    private Long orgId;

    @TableField("dept_id")
    private Long deptId;

    @TableField("job_code")
    private String jobCode;

    @TableField("job_name")
    private String jobName;

    @TableField("job_level")
    private String jobLevel;

    @TableField("job_category")
    private String jobCategory;

    @TableField("job_description")
    private String jobDescription;

    @TableField("job_requirements")
    private String jobRequirements;

    @TableField("min_salary")
    private BigDecimal minSalary;

    @TableField("max_salary")
    private BigDecimal maxSalary;

    @TableField("work_location")
    private String workLocation;

    @TableField("work_type")
    private String workType;

    @TableField("headcount")
    private Integer headcount;

    @TableField("current_count")
    private Integer currentCount;

    @TableField("status")
    private Integer status;

    @TableField("sort_order")
    private Integer sortOrder;

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