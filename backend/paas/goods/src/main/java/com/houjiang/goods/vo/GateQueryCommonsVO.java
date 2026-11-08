package com.houjiang.goods.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName GateQueryCommonsVO
 * @Description
 * @Author Amos
 * @Date 2023/8/2 10:06
 * @Version 1.0
 * @author Amos
 */
@Data
public class GateQueryCommonsVO {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除
     */
    private Integer commonState;

    /**
     * 商品图片
     */
    private String commonImage;

    /**
     * 商品价格
     */
    private BigDecimal commonPrice;

    /**
     * 门店库存
     */
    private Integer shopGateGoodsStock;
    /**
     * 虚拟商品 0 是 1 否 3 称重商品
     */
    private Integer virtualGoods;
}
