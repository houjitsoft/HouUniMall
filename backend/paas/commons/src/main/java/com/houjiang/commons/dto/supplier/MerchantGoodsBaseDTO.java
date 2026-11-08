package com.houjiang.commons.dto.supplier;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 基础商品
 * @author Amos
 */
@Data
public class MerchantGoodsBaseDTO {

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 促销提示
     */
    private String goodsPromotionTips;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 市场价
     */
    private BigDecimal goodsMarketPrice;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 库存预警值
     */
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

    /**
     * 是否上架1:上架
     */
    private Integer goodsIsShelves;

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
     * 起订量
     */
    private Integer moq;

    /**
     * 税率
     */
    private Double taxRate;

    /**
     * 含税价格
     */
    private BigDecimal taxInclusiveAmount;
}
