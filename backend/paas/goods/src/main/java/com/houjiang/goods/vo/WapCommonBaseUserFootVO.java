package com.houjiang.goods.vo;


import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单使用 不可修改
 *
 * @author Amos
 */
@Data
public class WapCommonBaseUserFootVO {

    /**
     * 商品goods_id
     */
    private String goodsId;

    /**
     * 商品id 1
     */
    private Integer goodsIdOne;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 商品价格
     */
    private BigDecimal commonPrice;

    /**
     * 市场价
     */
    private BigDecimal commonMarketPrice;

    /**
     * 商品主图
     */
    private String commonImage;

    /**
     * 销量
     */
    private Integer commonSalenum;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;

    /**
     * 商品广告词
     */
    private String commonPromotionTips;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
     */
    private Integer commonIsXian;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
     */
    private Integer activityStatus;

    /**
     * 活动详情
     */
    private ActivityGoodsDetailDTO activityGoodsDetail;

    /**
     * 规格值
     */
    private String commonSpecValue;

    /**
     * 1-上架 2-下架 0店铺关闭手动下架
     */
    private Integer goodsIsShelves;

    /**
     * 是否询价,0为不可询，1为可询
     */
    private Integer isEnquiry;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 角标地址
     */
    private String cornerUrl;


    /**
     * 每人限购 0代表不限购
     */
    private Integer commonLimit;

    /**
     * 最大购买数量
     */
    private Integer goodsMaxSale;

    /**
     * 最小购买数量
     */
    private Integer goodsMinSale;

    /**
     * 商品价格
     */
    private Integer goodsPrice;

    /**
     * 商品税率
     */
    private Double taxRate;

    /**
     * 积分商品 0是 1否
     */
    private Integer integralGoods;

    /**
     * 所需的积分
     */
    private Integer needIntegral;



    /**
     * 积分支付比例
     */
    private Double pointsRate;



    /**
     * 现金价格
     */
    private BigDecimal cashPrice;

}
