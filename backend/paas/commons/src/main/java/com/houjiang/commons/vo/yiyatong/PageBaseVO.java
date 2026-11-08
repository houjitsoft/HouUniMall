package com.houjiang.commons.vo.yiyatong;

import lombok.Data;

/**
 * @ClassName PageBaseVO
 * @Description
 * @Author Amos
 * @Date 2024/11/6 15:06
 * @Version 1.0
 * @author Amos
 */

@Data
public class PageBaseVO {

    /**
     * 总数
     */
    private Integer total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 页码
     */
    private Integer pageIndex;

    /**
     * 每页大小
     */
    private Integer pageSize;

}
