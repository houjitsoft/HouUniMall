package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Dashboard 用户偏好主表实体
 *
 * @author Amos
 * @since 2026-05-13
 */
@TableName("hou_base_dashboard_pref")
@Data
public class BaseDashboardPrefEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private String userId;

    /** 组织ID，可空 */
    private String orgId;

    /** 栅格列数，默认 12 */
    private Integer cols;

    /** 自动刷新间隔(秒)，0 表示关闭 */
    private Integer refreshInterval;

    /** 版本号 */
    private Integer version;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private Date createdAt;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
