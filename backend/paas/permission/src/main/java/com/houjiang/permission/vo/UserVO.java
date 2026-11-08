package com.houjiang.permission.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户VO类
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
public class UserVO {
    
    private String id;
    
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
     * 头像
     */
    private String avatar;
    
    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;
    
    /**
     * 状态文本
     */
    private String statusText;
    
    /**
     * 角色列表
     */
    private List<String> roles;
    
    /**
     * 最后登录时间
     */
    private String lastLoginTime;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 注册终端编码：mall / houcloud / hougeo
     */
    private String registerTerminal;

    /**
     * 注册终端展示：商城 / houcloud / hougeo
     */
    private String registerTerminalText;
}
