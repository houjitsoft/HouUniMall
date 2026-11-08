package com.houjiang.goods.mall.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品编辑参数（含新增）
 * 对应 bbc MallGoodsUpdateValidate
 * @author Amos
 */
@Data
public class MallGoodsUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID, 为空表示新增 */
    private Integer id;
    private Integer shopId;
    private String name;
    private String code;
    private String descript;
    /** 分类 ID 数组（前端逗号分隔或数组） */
    private List<Integer> categoryIds;
    private Integer shopCategoryId;
    private String image;

    private Integer videoIs;
    private Integer videoFrom;
    private String videoCover;
    private String videoUri;

    private Integer skuType;
    private Integer isExpress;
    private Integer expressType;
    private Integer expressTplId;
    private BigDecimal expressMoney;

    private Integer totalStock;
    private Integer stockWarning;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minLinePrice;
    private BigDecimal maxLinePrice;

    /** 商家后台可设置的虚拟销量、虚拟浏览量 */
    private Integer virtualSaleNum;
    private Integer virtualVisitNum;

    private Integer sort;
    private Integer status;
    private Integer shopRecommend;
    private String content;

    private Integer isDiscount;
    private Integer discountType;
    private Integer isDistribution;
    private Integer distributionType;

    /** 轮播图 URL 列表 */
    private List<String> imageList;
    /** SKU 列表 */
    private List<SkuItem> skuList;
    /** SKU 规格名列表（多规格用） */
    private List<String> skuNameList;

    @Data
    public static class SkuItem implements Serializable {
        private Integer id;
        private String image;
        private String skuValueIds;
        private String skuValueArr;
        private BigDecimal price;
        private BigDecimal linePrice;
        private BigDecimal marketPrice;
        private Integer stock;
        private BigDecimal weight;
        private BigDecimal volume;
        private String code;
    }
}
