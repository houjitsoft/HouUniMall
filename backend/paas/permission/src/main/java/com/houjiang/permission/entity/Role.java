package com.houjiang.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体类
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
@TableName("hou_permission_role")
public class Role {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 角色描述
     */
    @TableField("`desc`")
    private String desc;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Integer createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Integer updateTime;

    /**
     * 删除时间
     */
    @TableField(value = "delete_time")
    private Integer deleteTime;
}
