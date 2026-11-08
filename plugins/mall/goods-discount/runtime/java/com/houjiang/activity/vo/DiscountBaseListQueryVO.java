package com.houjiang.activity.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName DiscountBaseListQueryVO
 * @Description 限时折扣
 * @Author Amos
 * @Date 2023/7/3 15:11
 * @Version 1.0
 * @author Amos
 */
@Data
public class DiscountBaseListQueryVO {

    /**
     * 总数
     */
    private Integer totalCount;

    /**
     * 折扣信息
     */
    private List<DiscountBaseListVO> discountBaseList;
}
