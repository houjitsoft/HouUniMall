package com.houjiang.commons.dto.goods;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 * @date 2021-06-0413:54
 * @Description: 商品列表请求参数
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class FxGoodsListDTO extends PageHelper {
    /**
     * 1 默认排序 2销量排序  3 价格排序
     */
    private Integer orderType;

    /**
     // 1降序，2升序
/**
     *
     */
    private Integer saleNum;

    /**
     * 价格排序 1降序，2升序
     */
    private Integer priceOrder;

    /**
     * 分类id
     */
    private Integer catId;

    private List<Integer> catIds;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 店铺id
     */
    private Integer shopId;

    public FxGoodsListDTO(Integer goodsId) {
        this.goodsId = goodsId;
    }
}
