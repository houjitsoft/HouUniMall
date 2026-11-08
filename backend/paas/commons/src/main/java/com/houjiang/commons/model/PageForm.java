package com.houjiang.commons.model;

import lombok.Data;

/**
 * 分页表单基类
 * 提供通用的分页参数
 * @author Amos
 */
@Data
public class PageForm {
    
    /**
     * 当前页码，默认第1页
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小，默认10条
     */
    private Integer pageSize = 10;
    
    /**
     * 排序字段
     */
    private String orderBy;
    
    /**
     * 排序方向：asc(升序)、desc(降序)
     */
    private String orderDirection = "desc";
    
    /**
     * 获取偏移量
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
    
    /**
     * 获取限制数量
     */
    public Integer getLimit() {
        return pageSize;
    }
}
