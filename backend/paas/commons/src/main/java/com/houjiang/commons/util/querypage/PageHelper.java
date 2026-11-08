package com.houjiang.commons.util.querypage;

import lombok.Data;

/**
 * 分页信息
 * @author Amos
 */
@Data
public class PageHelper {

    /**
     * token
     */
    private String token;

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 页大小
     */
    private Integer pageSize;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 页
     */
    private Integer page;

    /**
     * 限度
     */
    private Integer limit;
    /**
     *  直播页
     */
    private Integer start;

}
