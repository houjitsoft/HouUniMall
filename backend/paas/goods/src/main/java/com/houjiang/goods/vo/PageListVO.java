package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class PageListVO<T> {

    private List<T> resultList;

    /**
     * 总条数
     */
    private Integer totalCount;
}
