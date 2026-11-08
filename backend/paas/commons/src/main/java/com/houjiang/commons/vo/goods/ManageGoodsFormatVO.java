package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * 商品
 * @author Amos
 */
@Data
public class ManageGoodsFormatVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 位置
     */
    private Integer position;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 内容
     */
    private String content;

    public ManageGoodsFormatVO(Integer id, String name, Integer position, Integer shopId, String content) {
        super();
        this.id = id;
        this.name = name;
        this.position = position;
        this.shopId = shopId;
        this.content = content;
    }

    public ManageGoodsFormatVO() {
        super();
    }

}
