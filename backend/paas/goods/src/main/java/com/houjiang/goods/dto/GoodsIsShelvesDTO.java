package com.houjiang.goods.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsIsShelvesDTO {

    /**
     * 商品id 集合
     */
    private List<Integer> goodsIds;

    /**
     * 商品公共id集合
     */
    private List<Integer> commonIds;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * token
     */
    private String token;

}
