package com.houjiang.goods.dto;


import com.houjiang.commons.dto.goods.ManageGdsBaseAddParamvDTO;
import com.houjiang.commons.dto.goods.ManageGdsBaseAddPublishDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotGoodsDTO {
    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 销售数量
     */
    private Integer goodsSaleNum;

    /**
     * 热度   前端显示的时候 热度*销量
     */
    private Integer hotNum;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 供货价
     */
    private BigDecimal supplyPrice;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品广告语
     */
    private String commonPromotionTips;

    /**
     * 热销角标图id
     */
    private Integer cornerId;

    /**
     * 所属分类  最多三个   中间用逗号隔开
     */
    private String catIdList;

    /**
     * 排序 从小到大
     */
    private Integer goodsSort;

    /**
     * 1待审核  2审核通过  3审核不通过 4删除
     */
    private Integer goodsState;

    /**
     * 创建时间
     */
    private String createTime;

    public HotGoodsDTO(Integer goodsId) {
        this.goodsId = goodsId;
    }
    public HotGoodsDTO(ManageGdsBaseAddPublishDTO gdsBase) {
        this.goodsId = gdsBase.getGoodsId();
        this.goodsName = gdsBase.getGoodsName();
        this.commonId = gdsBase.getCommonId();
        this.goodsImage = gdsBase.getGoodsImage();
        this.brandName = gdsBase.getBrandName();
        this.shopId = gdsBase.getShopId();
        this.shopName = gdsBase.getShopName();
        this.supplyPrice = gdsBase.getGoodsPrice();
        this.goodsPrice = gdsBase.getHotPrice();
        this.commonPromotionTips = gdsBase.getGoodsPromotionTips();
    }

    public HotGoodsDTO(ManageGdsBaseAddParamvDTO gdsBase) {
        this.goodsId = gdsBase.getGoodsId();
        this.goodsName = gdsBase.getGoodsName();
        this.commonId = gdsBase.getCommonId();
        this.goodsImage = gdsBase.getGoodsImage();
        this.brandName = gdsBase.getBrandName();
        this.shopId = gdsBase.getShopId();
        this.shopName = gdsBase.getShopName();
        this.supplyPrice = gdsBase.getGoodsPrice();
        this.goodsPrice = gdsBase.getHotPrice();
        this.commonPromotionTips = gdsBase.getGoodsPromotionTips();
        this.goodsState = 1;
    }
}