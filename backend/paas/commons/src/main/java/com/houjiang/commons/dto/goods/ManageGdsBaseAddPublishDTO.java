package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName ManageGdsBaseAddPublishDTO
 * @Description 商家后台商品添加
 * @Author Amos
 * @Date 2023/6/26 15:03
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsBaseAddPublishDTO {
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 店铺名
     */
    private String shopName;
    /**
     * 商品名
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
     * 商品规格
     */
    private String goodsSpec;
    /**
     * 商品规格值
     */
    private String goodsSpecValue;
    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;
    /**
     * 市场价
     */
    private BigDecimal goodsMarketPrice;
    /**
     * 市场价
     */
    private Integer goodsStock;
    /**
     * 门店库存总和最大值
     */
    private Integer goodsShopGateStock;
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
     * 商品推荐 1是，0否 默认0'
     */
    private Integer goodsIsRecommend;
    /**
     * 商品主图
     */
    private String goodsImage;
    /**
     * 单人最大购买数量
     */
    private Integer goodsMaxSale;
    /**
     * 1-上架 2-下架
     */
    private Integer goodsIsShelves;
    /**
     * 品牌id
     */
    private Integer brandId;
    /**
     * 品牌名称
     */
    private String brandName;
    /**
     * 商品重量
     */
    private BigDecimal goodsCubage;

    /**
     * 商品价格
     */
    private BigDecimal goodsBabyPrice;

    /**
     * 最小限购数
     */
    private Integer goodsMinSale;

    /**
     * 规格类型 多规格需求新增
     */
    private String specId;
    /**
     * 类型id
     */
    private String typeId;
    /**
     * 规格名称
     */
    private String specName;

    /**
     * 是否使用(0:未使用;1:已使用)
     */
    private Integer isUse;
    /**
     * 商家编号
     */
    private String merchantCode;
    /**
     * 商品条形码
     */
    private String commodityBarTypeCode;
    /**
     * 规格
     */
    private List<GoodBaseSpecValuesJsonBeanDTO> specValues;
    /**
     * 体积
     */
    private String goodsVolume;
    /**
     * 成本价
     */
    private BigDecimal goodsCostPrice;
    /**
     * 佣金
     */
    private BigDecimal commission;
    /**
     * 直播卖点
     */
    private String sellingPoint;
    /**
     * 商品是否寄回样品
     */
    private Integer goodsSendBack;
    /**
     * 爆品指导价
     */
    private BigDecimal hotPrice;

    /**
     * 兑换积分商品所需积分
     */
    private Integer needIntegral;

    /**
     * 含税价格
     */
    private Double taxInclusiveAmount;

    /**
     * 税率
     */
    private Double taxRate;

    /**
     * 起订量
     */
    private Integer moq;
    /**
     * 单位
     */
    private String dw;

    /**
     * 是否可询价
     */
    private Boolean isEnquiry;

}
