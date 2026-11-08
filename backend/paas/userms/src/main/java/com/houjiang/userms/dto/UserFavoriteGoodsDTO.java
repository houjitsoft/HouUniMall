package com.houjiang.userms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 用户商品
 * @author Amos
 */
@Data
public class UserFavoriteGoodsDTO {

    /**
     * 商品收藏编号
     */
    private Integer favoritesGoodsId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 收藏时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date favoritesGoodsTime;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 副标题，即商品广告词
     */
    private String commonPromotionTips;
    /**
     * 规格名称
     */
    private String commonSpecName;

    /**
     * 规格名称集合
     */
    private List<Object> commonSpecNameList;
    /**
     * 规格值
     */
    private String commonSpecValue;

    /**
     * 规格值集合
     */
    private List<Object> commonSpecValueList;

    /**
     * 商品主图
     */
    private String commonImage;

    /**
     * 商品价格
     */
    private BigDecimal commonPrice;

    /**
     * 市场价
     */
    private BigDecimal commonMarketPrice;

    /**
     * 库存
     */
    private Integer commonStock;

    /**
     * 销量
     */
    private Integer commonSalenum;

    /**
     * 是否上架1:上架
     */
    private Integer goodsIsShelves;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 是否可询价
     */
    private Integer isEnquiry;



    /**
     * 积分支付比例
     */
    private Double pointsRate;



    /**
     * 现金价格
     */
    private BigDecimal cashPrice;


}
