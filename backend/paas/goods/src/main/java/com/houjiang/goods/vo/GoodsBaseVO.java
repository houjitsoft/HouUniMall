package com.houjiang.goods.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class GoodsBaseVO {

    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 公共id
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
     * 经销商店铺id
     */
    private String supplierShopId;

    /**
     * 经销商商品id
     */
    private Integer supplierGoodsId;

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
     * 市场价
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
     * 颜色id
     */
    private Integer colorId;

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
     * 1-上架 2-下架
     */
    private Integer goodsIsShelves;
    /**
     * 商品来源id
     */
    private Integer goodsParentId;
    /**
     * 商品重量
     */
    private BigDecimal goodsCubage;
    /**
     * 商品价格
     */
    private BigDecimal goodsBabyPrice;
    /**
     * 最小限购数据
     */
    private Integer goodsMinSale;
    /**
     * 商家编号
     */
    private String merchantCode;
    /**
     * 商品条形码
     */
    private String commodityBarTypeCode;
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
     * 是否删除0:否;1:是
     */
    private Integer isDelete;
    /**
     * 创建者
     */
    private String createUser;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新用户
     */
    private String updateUser;
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 版本
     */
    private Integer sysVersion;
    /**
     * 时间戳
     */
    private Date ts;

    /**
     * 店铺分类
     */
    private String shopGateType;

}
