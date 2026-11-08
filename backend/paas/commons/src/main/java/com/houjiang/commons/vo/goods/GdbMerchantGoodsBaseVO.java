package com.houjiang.commons.vo.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GdbMerchantGoodsBaseVO {
    private Integer goodsId;

    private Integer commonId;

    private Integer shopId;

    private String shopName;

    private String goodsName;
    /**
     * 促销提示
     */
    private String goodsPromotionTips;

    private Integer catId;

    private Integer brandId;

    private String goodsSpec;

    private BigDecimal goodsPrice;

    private BigDecimal goodsMarketPrice;

    private Integer goodsStock;

    private Integer goodsAlarm;
    /**
     * 商家编号货号
     */
    private String goodsCode;
    /**
     * 商品二维码
     */
    private String goodsBarcode;
    /**
     * 是否推荐
     */
    private Boolean goodsIsRecommend;
    /**
     * 商品点击数量
     */
    private Integer goodsClick;
    /**
     * 销售数量
     */
    private Integer goodsSalenum;
    /**
     * 收藏数量
     */
    private Integer goodsCollect;
    /**
     * 商品图片
     */
    private String goodsImage;
    /**
     * 好评星级
     */
    private Byte goodsEvaluationGoodStar;
    /**
     * 评价数
     */
    private Integer goodsEvaluationCount;
    /**
     * 单人最大购买数量
     */
    private Integer goodsMaxSale;

    private Boolean goodsIsShelves;
    /**
     * 商品来源id
     */
    private Integer goodsParentId;
    /**
     * 成本价
     */
    private BigDecimal goodsCostPrice;
    /**
     * 重量
     */
    private BigDecimal goodsCubage;
    /**
     * 体积
     */
    private BigDecimal goodsVolume;
    /**
     * 商家编号
     */
    private String merchantCode;
    /**
     * 商品条型码
     */
    private String commodityBarTypeCode;
    /**
     * 是否使用0：未使用；1：已使用
     */
    private Integer isUse;
    /**
     * 规格值
     */
    private String specValue;
    /**
     * 佣金
     */
    private BigDecimal commission;

    /**
     * 是否寄回样品 0：是 1：否
     */
    private Integer goodsSendBack;
    /**
     * 直播卖点
     */
    private String sellingPoint;
    /**
     * 商品图片
      */
    private List<GdbMerchantGoodsImagesVO> goodsImages;

}