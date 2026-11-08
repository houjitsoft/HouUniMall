package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ManageGdsBaseAddResult
 * @Description 商家后台商品添加
 * @Author Amos
 * @Date 2023/4/7 14:06
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsBaseInfoDTO {

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
     * 货品名称
     */
    private String goodName;

    /**
     * 商品规格返回对象
     */
    private ManageSpecNameDTO goodsName;

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
     * 货品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 市场价
     */
    private BigDecimal goodsMarketPrice;

    /**
     * 库存
     */
    private Integer goodsStock;

    /**
     * 门店库存综合
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
     * 规格类型 多规格需求新增 20202/10/15
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
     * 规格值
     */
    private String specValue;

    /**
     * 体积
     */
    private BigDecimal goodsVolume;

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
     * 是否寄样
     */
    private Integer goodsSendBack;
    /**
     * 指导价
     */
    private BigDecimal guidePrice;

    /**
     * 商家编号
     */
    private String merchantCode;

    /**
     * 商品条型码
     */
    private String commodityBarTypeCode;

    /**
     * 起订量
     */
    private Integer moq;

    /**
     * 含税价格
     */
    private Double taxInclusiveAmount;

    /**
     * 税率
     */
    private Double taxRate;

    /**
     * 单位
     */
    private String dw;

    /**
     * 所需积分
     */
    private Integer needIntegral;

    /**
     * 仓库商品库存
     */
    private Integer storageGoodsStock;

    /**
     * 规格版本状态 1 正常  2 删除 3 下架
     */
    private Integer specVersionStatus;

    /**
     * 是否删除 0正常 1删除
     */
    private Integer isDelete;
}
