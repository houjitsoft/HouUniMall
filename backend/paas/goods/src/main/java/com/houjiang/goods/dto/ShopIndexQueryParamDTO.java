package com.houjiang.goods.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @ClassName ShopIndexQueryParamDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/17 14:57
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShopIndexQueryParamDTO extends PageHelper {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品spu名称
     */
    private String commonName;

    /**
     *1价格排序，2销量排序 3综合排序 4新品推荐
     */
    private Integer orderType;

    /**
     *1降序，2升序
     */
    private Integer saleNum;

    /**
     *1降序，2升序
     */
    private Integer priceOrder;
}
