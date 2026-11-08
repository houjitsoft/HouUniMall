package com.houjiang.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 满减增折
 * @author Amos
 */
@Data
public class ManjianzengzheDTO {

    /**
     * 活动ID
     */
    private Integer promotionId;

    /**
     * 商品公共ID
     */
    private Integer commonId;

    /**
     * 商品ID
     */
    private Integer goodsId;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 下标
     */
    private Integer index;

    /**
     * 下标
     */
    private List<Integer> indexList;

    /**
     * 商品
     */
    private List<Integer> goodsIdList;

    /**
     * 商品价格
     */
    private List<BigDecimal> goodsPriceList;

}
