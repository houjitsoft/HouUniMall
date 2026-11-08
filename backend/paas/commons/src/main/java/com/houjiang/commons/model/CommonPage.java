package com.houjiang.commons.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * CommonPage 分页数据封装类
 * 提供通用的分页数据封装功能
 * @author Amos
 */
@Data
public class CommonPage<T> {
    /**
     * 当前页
     */
    private Long currentPage;
    /**
     * 每页数量
     */
    private Long pageSize;
    /**
     * 总页数
     */
    private Long totalPage;
    /**
     * 总记录数
     */
    private Long total;
    /**
     * 分页数据
     */
    private List<T> list;

    /**
     * 将MyBatis Plus 分页结果转化为通用结果
     */
    public static <T> CommonPage<T> covertByPage(Page<T> page) {
        CommonPage<T> commonPage = new CommonPage<>();
        commonPage.setCurrentPage(page.getCurrent());
        commonPage.setPageSize(page.getSize());
        commonPage.setTotal(page.getTotal());
        commonPage.setTotalPage(page.getPages());
        commonPage.setList(page.getRecords());
        return commonPage;
    }
}
