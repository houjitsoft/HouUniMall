package com.houjiang.permission.vo;

import lombok.Data;

import java.util.List;

/**
 * 角色VO类
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
public class RoleVO {
    
    private Integer id;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 角色描述
     */
    private String desc;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 用户数量
     */
    private Integer num;
    
    /**
     * 菜单ID列表
     */
    private List<Integer> menusId;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 更新时间
     */
    private String updateTime;
}
