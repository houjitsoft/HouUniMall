package com.houjiang.permission.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页响应VO
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
public class PageResult<T> {
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Long current;
    
    /**
     * 每页大小
     */
    private Long size;
    
    /**
     * 数据列表
     */
    private List<T> records;
    
    public PageResult() {}
    
    public PageResult(Long total, Long current, Long size, List<T> records) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.records = records;
    }
    
    public static <T> PageResult<T> of(Long total, Long current, Long size, List<T> records) {
        return new PageResult<>(total, current, size, records);
    }
}
