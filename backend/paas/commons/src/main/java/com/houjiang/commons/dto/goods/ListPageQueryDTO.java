package com.houjiang.commons.dto.goods;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ListPageQueryDTO extends PageHelper {

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品公共名称
     */
    private String commonName;

    /**
     * 0 默认排序 1价格排序，2销量排序 3综合排序 4更新时间 5评论数 6积分排序
     */
    private Integer orderType;

    /**
     * 价格排序 1降序，2升序
     */
    private Integer priceOrder;

    /**
     * 评论排序 1降序，2升序
     */
    private Integer commentOrder;

    /**
     * 最低价
     */
    private BigDecimal minPrice;

    /**
     * 最高价
     */
    private BigDecimal maxPrice;

    /**
     * 店铺自营
     */
    private String shopSelfSupport;

    /**
     * 1降序，2升序
     */
    private Integer saleNum;

    /**
     * 分类id集合
     */
    private List<Integer> catIds;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 是否是积分商品 0是 1否
     */
    private Integer integralGoods;

    /**
     * 热门兑换 1是 2否
     */
    private Integer hotExchange;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 1 一键上架  2立即购买
     */
    private Integer sellDeliver;

    /**
     * 1普通商品 2供应商商品
     */
    private Integer goodsType;

    /**
     * 是否为app
     */
    private Integer isApp;
    /**
     * 品牌名称
     */
    private String brandName;
    /**
     * 商品分类属性
     */
    private String attributeContentName;
    /**
     * 品牌id集合
     */
    private List<String> brandIdList;
    /**
     * 分类属性内容id集合
     */
    private List<String> attributeContentIdList;

    public void setCommonName(String commonName) {
        if (null != commonName && !commonName.equals("")) {
            this.commonName = commonName.trim();
        } else {
            this.commonName = commonName;
        }
    }

    public void setGoodsName(String goodsName) {
        if (null != goodsName && !goodsName.equals("")) {
            this.goodsName = goodsName.trim();
        } else {
            this.goodsName = goodsName;
        }
    }
}
