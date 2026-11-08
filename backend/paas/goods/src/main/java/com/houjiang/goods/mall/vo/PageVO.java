package com.houjiang.goods.mall.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一分页 VO
 * @author Amos
 */
@Data
public class PageVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页 */
    private long pageNo;
    /** 每页条数 */
    private long pageSize;
    /** 总条数 */
    private long count;
    /** 数据 */
    private List<T> lists;

    /** 扩展统计（likeshop 分页 tabs 计数，如 count_sale） */
    private Map<String, Object> extend = new LinkedHashMap<>();

    public PageVO() {
    }

    public PageVO(long pageNo, long pageSize, long count, List<T> lists) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.count = count;
        this.lists = lists;
    }

    public PageVO(long pageNo, long pageSize, long count, List<T> lists, Map<String, Object> extend) {
        this(pageNo, pageSize, count, lists);
        this.extend = extend != null ? extend : new LinkedHashMap<>();
    }
}
