package com.houjiang.mall.dto.auth;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 分页结果（对齐 like-admin 前端 usePaging：count + lists）
 * @author Amos
 */
@Data
public class MallPageResult<T> {

    private Long count;
    private Integer page_no;
    private Integer page_size;
    private List<T> lists;
    private Map<String, Object> extend;

    public static <T> MallPageResult<T> of(IPage<?> page, List<T> lists) {
        MallPageResult<T> result = new MallPageResult<>();
        result.setCount(page.getTotal());
        result.setPage_no((int) page.getCurrent());
        result.setPage_size((int) page.getSize());
        result.setLists(lists);
        return result;
    }
}
