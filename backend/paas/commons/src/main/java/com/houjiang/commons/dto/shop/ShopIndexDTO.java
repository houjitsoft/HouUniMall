package com.houjiang.commons.dto.shop;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 店铺
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShopIndexDTO extends PageHelper {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品公共名称
     */
    private String commonName;

    /**
     * 1价格排序，2销量排序 3综合排序 4新品推荐
     */
    private Integer orderType;

    /**
     * 1降序，2升序
     */
    private Integer saleNum;

    /**
     * 1降序，2升序
     */
    private Integer priceOrder;


}
