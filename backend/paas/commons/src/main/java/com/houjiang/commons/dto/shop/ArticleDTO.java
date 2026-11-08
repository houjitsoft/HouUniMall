package com.houjiang.commons.dto.shop;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class ArticleDTO {

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品spu名称
     */
    private String commonName;

    /**
     * 商品sku名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private Double commonPrice;

    /**
     * 商品价格
     */
    private Double goodsPrice;

    /**
     * 商品评论数
     */
    private Integer commonEvaluate;

    /**
     * 商品销量
     */
    private Integer commonSalenum;

    /**
     * 商品收藏量
     */
    private Integer commonCollect;


    /**
     * 是否自营商品 1 不是 2 是
     */
    private Integer shopSelfSupport;

    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 商品符合搜索评分
     */
    private Float score;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 商品规格
     */
    private String goodsSpec;
    /**
     * 商品属性内容Id
     */
    private String attributeContentId;

    /**
     * 分类id
     */
    private String catId;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 市场价
     */
    private BigDecimal commonMarketPrice;

    /**
     * 商品图片
     */
    private String commonImage;

    /**
     * 副标题，即商品广告词
     */
    private String commonPromotionTips;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;

    /**
     * 小宝价格
     */
    private BigDecimal commonBabyPrice;
    /**
     * 平均发货时间
     */
    private String avgShippingTime;
    /**
     * 虚拟商品 0 是 1 否
     */
    private Integer virtualGoods;
    /**
     * 虚拟商品有效期
     */
    private String periodOfValidity;
    /**
     * 商品列表优先展示
     */
    private Integer goodsListFirstShow;
    /**
     * 等级id
     */
    public Integer levelId;
    /**
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;
    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团
     */
    private Integer commonIsXian;

    /**
     * 是否开通会员 0 过期 1 开 2 未开
     */
    private Integer plusMember;
    /**
     * 1普通商品  2供应商商品 3品牌商品
     */
    private Integer goodsType;
    /**
     * plus会员折扣
     */
    private BigDecimal plusDiscount;

    /**
     * 是否上架1:上架
     */
    private Integer goodsIsShelves;

    /**
     * 图片向量
     */
    private float[] vector;

}
