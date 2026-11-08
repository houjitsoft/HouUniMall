package com.houjiang.permission.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色创建请求DTO
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
public class RoleCreateRequest {
    
    @NotBlank(message = "角色名称不能为空")
    private String name;
    
    private String desc;
    
    @NotNull(message = "排序不能为空")
    private Integer sort;
    
    private List<Integer> menuIds;
}
