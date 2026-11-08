package com.houjiang.permission.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色更新请求DTO
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
public class RoleUpdateRequest {
    
    @NotNull(message = "角色ID不能为空")
    private Integer id;
    
    @NotBlank(message = "角色名称不能为空")
    private String name;
    
    private String desc;
    
    @NotNull(message = "排序不能为空")
    private Integer sort;
    
    private List<Integer> menuIds;
}
