package com.houjiang.activity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ShopSeckillGoodsListVO
 * @Description TODO
 * @Author Amos
 * @Date 2023/8/18 13:31
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShopSeckillGoodsListVO {

    /**
     * 
     */
    private Integer commonId;
    /**
     * 默认规格 ID（hou_goods_common.goods_id）。秒杀商品按规格落库，
     * 前端选中商品后直接用该值作为 specLists[].goodsId，不必再让运营手填。
     */
    private Integer goodsId;
    /**
     *商品名称
     */
    private String commonName; 
    /**
     *商品原价
     */
    private BigDecimal commonPrice; 
    /**
     *商品图片
     */
    private String commonImage; 
    /**
     *显示折扣：2秒杀
     */
    private Integer commonIsXian; 
    /**
     *
     */
    private Integer commonStock;
}
