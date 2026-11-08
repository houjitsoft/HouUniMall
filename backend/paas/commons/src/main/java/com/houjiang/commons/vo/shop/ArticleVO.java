package com.houjiang.commons.vo.shop;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品
 * @author Amos
 */
@Data
public class ArticleVO {

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
     * 商品图片
     */
    private String commonImage;

    /**
     * 商品sku名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private Double commonPrice;

    /**
     * 商品市场价格
     */
    private Double commonMarketPrice;

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
     * 商品分类id
     */
    private Integer catId;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
     */
    private Integer commonIsXian;

    /**
     * 商品列表优先展示
     */
    private Integer goodsListFirstShow;

    /**
     * 等级id
     */
    public Integer levelId;

    /**
     * 是否开通会员 0 过期 1 开 2 未开
     */
    private Integer plusMember;

    /**
     * plus会员折扣
     */
    private BigDecimal plusDiscount;



    /**
     * 积分支付比例
     */
    private Double pointsRate;

    /**
     * 现金价格
     */
    private BigDecimal cashPrice;


}
