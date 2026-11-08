package com.houjiang.permission.dto;

import lombok.Data;

/**
 * 用户搜索请求DTO
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
public class UserSearchRequest extends PageRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String mobile;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 角色ID
     */
    private Integer roleId;

    /**
     * 注册终端：mall / houcloud / hougeo
     */
    private String registerTerminal;
}
