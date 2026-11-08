package com.houjiang.commons.dto.shop;


import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@ToString
public class WapShippingQueryDTO {

    /**
     * 商品重量
     */
    private BigDecimal goodsCubage;
    /**
     * 商品总运费
     */
    private BigDecimal totalGoodsShippingFee;
    /**
     * 运费模板
     */
    private Integer transportTemplate;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 规格名
     */
    private String goodsSpec;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品市场价格
     */
    private BigDecimal goodsMarketPrice;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 运费
     */
    private BigDecimal goodsShippingFee;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 1-上架 2-下架 0店铺关闭手动下架
     */
    private Integer goodsIsShelves;

    /**
     * 最大购买数量
     */
    private Integer goodsMaxSale;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 规格名称
     */
    private String commonSpecName;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团
     */
    private Integer commonIsXian;

    /**
     * 每人限购 0代表不限购
     */
    private Integer commonLimit;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团
     */
    private Integer activityStatus;

    /**
     * 活动详情
     */
    private ActivityGoodsDetailDTO activityGoodsDetailDto;

    /**
     * 商品价格
     */
    private BigDecimal goodsBabyPrice;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;

    /**
     * 小宝价格
     */
    private BigDecimal commonBabyPrice;

    /**
     * 大宝佣金
     */
    private BigDecimal bigbabyCommission;

    /**
     * 售卖区域id
     */
    private Integer transportAreaId;

    /**
     * 限购次数
     */
    private Integer restrictionTimes;

    /**
     * 限购小时数
     */
    private Integer restrictionHours;

    /**
     * 是否为聚宝:1是
     */
    private Integer commonIsTreasure;

    /**
     * 供应商店铺id
     */
    private Integer supplyShopId;

    /**
     * 供应商店铺名
     */
    private String supplyShopName;

    /**
     * 供应商商品id
     */
    private Integer supplierGoodsId;

    /**
     * 商品折扣金额
     */
    private BigDecimal goodsDisountPrice;

    /**
     * 商品支付金额
     */
    private BigDecimal goodsPaymentPrice;

    /**
     * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）
     */
    private Integer dealerGoodsFlag;

    /**
     * 商品供货价
     */
    private BigDecimal dealerGoodsPrice;

    /**
     * 是否分销商品  1是0否
     */
    private Integer fxGoods;

    /**
     * 推广佣金
     */
    private String tgRate;

    /**
     * 分享佣金
     */
    private String fxRate;

    /**
     * 待发货商品 经销商佣金比例
     */
    private String dealerCommission;

    /**
     * 待发货商品供应商佣金比例
     */
    private String supplyCommission;

    /**
     * 1普通商品   2供应商商品
     */
    private Integer goodsType;

    /**
     * 供应商商品指导价
     */
    private BigDecimal guidePrice;

    /**
     * 含税价格
     */
    private Double taxInclusiveAmount;

    /**
     * 税率
     */
    private Double taxRate;

    /**
     * 是否是询价商品
     */
    private Integer isEnquiry;


}
