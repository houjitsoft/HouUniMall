package com.houjiang.order.vo;

import lombok.Data;

/**
 * @ClassName SalesRankingVO
 * @Description 销量排名
 * @Author Amos
 * @Date 2023/6/25 14:01
 * @Version 1.0
 * @author Amos
 */
@Data
public class SalesRankingVO {
    /**
     * 商品名
     */
    private String goodName;
    /**
     * 销量
     */
    private Integer Sales;
}
