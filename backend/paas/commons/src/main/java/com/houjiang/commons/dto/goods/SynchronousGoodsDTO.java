package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName SynchronousGoodsDTO
 * @Description
 * @Author Amos
 * @Date 2024/7/24 18:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class SynchronousGoodsDTO {

    /**
     * 商店ID，用于标识商品所属的商店。
     * 该字段为私有成员变量，将在类的内部被使用，以关联商品和所属商店。
     */
    private Integer shopId;

    /**
     * 商品ID，用于唯一标识每个商品。
     * 该字段为私有成员变量，将在类的内部被用作商品的唯一标识符，可能用于检索或更新商品信息。
     */
    private Integer goodsId;
    /**
     * spuId
     */
    private Integer commonId;

    /**
     * 买家用户id
     */
    private String buyerUserId;
    /**
     * 购买商品数量
     */
    private Integer orderGoodsNum;

}
