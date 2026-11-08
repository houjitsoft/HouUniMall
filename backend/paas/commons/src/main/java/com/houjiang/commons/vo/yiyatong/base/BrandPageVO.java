package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

/**
 * @ClassName BrandVO
 * @Description
 * @Author Amos
 * @Date 2024/11/7 15:34
 * @Version 1.0
 * @author Amos
 */
@Data
public class BrandPageVO {

    /**
     * 当前页
     */
    private Integer current;

    /**
     * 总条数
     */
    private Integer total;

    /**
     * 页数
     */
    private Integer pages;

    /**
     * 每页条数
     */
    private Integer size;

    private Boolean hitCount;

    private Boolean searchCount;
}
