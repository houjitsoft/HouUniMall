package com.houjiang.permission.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 分页请求DTO
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
public class PageRequest {
    
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    @JsonAlias({"page", "current"})
    private Integer pageNo;
    
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    @JsonAlias({"limit", "size"})
    private Integer pageSize;
}
