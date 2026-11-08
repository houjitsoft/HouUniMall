package com.houjiang.permission.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * 用户更新请求DTO
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
public class UserUpdateRequest {
    
    @NotBlank(message = "用户ID不能为空")
    private String id;
    
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名只能包含字母、数字、下划线，长度3-20位")
    private String username;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;
    
    private String avatar;
    
    private Integer status;

    /** 兼容旧调用方使用的状态字段名。 */
    private Integer userState;
    
    private List<Integer> roleIds;
}
