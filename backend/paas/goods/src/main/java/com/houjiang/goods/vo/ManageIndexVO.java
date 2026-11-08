package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName ManageIndexVO
 * @Description 商品数据
 * @Author Amos
 * @Date 2023/6/25 15:07
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageIndexVO {
    /**
     * 查询审核过的商品
     */
    private Integer verify;

    /**
     * 出售中商品数量
     */
    private Integer saleSum;

    /**
     * 待审核商品数量
     */
    private Integer waitVerify;

    /**
     * 仓库中的商品
     */
    private Integer storeHouse;

    /**
     * 违规下架商品
     */
    private Integer Irregularities;
}
