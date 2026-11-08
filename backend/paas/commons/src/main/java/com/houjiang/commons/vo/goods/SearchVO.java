package com.houjiang.commons.vo.goods;

import com.houjiang.commons.vo.shop.ArticleVO;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class SearchVO {

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 导航条 品牌集合
     */
    private List<GoodsBrandVO> navBrands;

    /**
     * 导航条 分类集合
     */
    private List<GoodsCatResultVO> navSaleCategories;

    /**
     * 属性集合
     */
    private List<GoodsAttrVO> attrs;

    /**
     * 商品列表
     */
    private List<ArticleVO> goodsList;

    /**
     * 商品总数
     */
    private Integer goodsCount;

    /**
     * 搜索类型 1:普通搜索 2:订货编码搜索
     */
    private Integer type;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 搜索行 品牌集合
     */
    private List<GoodsBrandVO> lineBrands;

    /**
     * 搜索行 分类集合
     */
    private List<GoodsCatResultVO> lineSaleCategories;

    /**
     * 查询plus是否开启
     */
    private Integer selectplus;

}
