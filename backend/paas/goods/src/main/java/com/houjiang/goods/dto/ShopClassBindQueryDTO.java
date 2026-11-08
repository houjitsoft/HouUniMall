package com.houjiang.goods.dto;

import lombok.Data;

/**
 * @ClassName ShopClassBindQueryDTO
 * @Description 店铺类目删除
 * @Author Amos
 * @Date 2023/6/6 14:29
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShopClassBindQueryDTO extends UserTokenBeanDTO{

    /**
     * 店铺经营类目绑定id
     */
    private Integer shopClassBindId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品分类id
     */
    private Integer productClassId;

    /**
     * 入住id
     */
    private String joinId;
}
