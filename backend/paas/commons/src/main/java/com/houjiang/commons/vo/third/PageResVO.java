package com.houjiang.commons.vo.third;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @CreateDate: 2025/7/8 23:47
 * @author Amos
 */
@Data
public class PageResVO<T> implements Serializable {

    private long endRow;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
    private boolean isFirstPage;
    private boolean isLastPage;
    private List<T> list;
    private long navigateFirstPage;
    private long navigateLastPage;
    private long[] navigatepageNums;
    private long navigatePages;
    private long nextPage;
    /**
     * 每页的数量
     */
    private long pageNum;
    /**
     * 总页数
     */
    private long pages;
    /**
     * 当前页的数量
     */
    private long pageSize;
    private long prePage;
    private long size;
    private long startRow;
    /**
     * 总数
     */
    private long total;
}
