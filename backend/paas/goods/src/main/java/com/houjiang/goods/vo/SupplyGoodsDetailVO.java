package com.houjiang.goods.vo;

import com.houjiang.goods.entity.GoodsBaseEntity;
import com.houjiang.goods.entity.GoodsCommonEntity;
import com.houjiang.goods.entity.GoodsImagesEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 * @date 2021-06-0317:11
 * @Description: 供应商品详情
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplyGoodsDetailVO {
    /**
     * 通用名称
     */
    private String commonName;

    /**
     * 通用价格
     */
    private BigDecimal commonPrice;

    /**
     * 通用库存
     */
    private Integer commonStock;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 通用图片
     */
    private String commonImage;

    /**
     * 通用视频
     */
    private String commonVideo;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 运输区域
     */
    private String transportArea;

    /**
     * 商品规格列表
     */
    private List<GoodsBaseEntity> goodsSpecList;

    /**
     * 商品图片详情结果列表
     */
    private List<GoodsImagesEntity> goodsImages;

    /**
     * 通用商品描述
     */
    private String commonBody;


    public SupplyGoodsDetailVO(GoodsCommonEntity goodsCommon, String transportArea ) {
        this.commonName = goodsCommon.getCommonName();
        this.commonPrice = goodsCommon.getCommonPrice();
        this.commonStock = goodsCommon.getCommonStock();
        this.catName = goodsCommon.getCatName();
        this.commonImage = goodsCommon.getCommonImage();
        this.commonVideo = goodsCommon.getCommonVideo();
        this.shopName = goodsCommon.getShopName();
        this.transportArea = transportArea;
    }
}
