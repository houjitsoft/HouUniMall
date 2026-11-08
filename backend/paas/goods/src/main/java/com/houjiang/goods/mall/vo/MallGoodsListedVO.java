package com.houjiang.goods.mall.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品列表 VO（对齐 bbc MallGoodsListedVo 核心字段）
 * @author Amos
 */
@Data
public class MallGoodsListedVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer createTime;
    private Integer updateTime;
    private Integer shopId;
    private String name;
    private String code;
    private String descript;
    private String categoryIds;
    private List<Integer> categoryIdsArr;
    private String image;

    private Integer skuType;
    private String skuTypeText;

    private Integer totalStock;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minLinePrice;
    private BigDecimal maxLinePrice;
    private String priceText;

    private Integer saleNum;
    private Integer virtualSaleNum;
    private Integer allSaleNum;
    private Integer visitNum;

    private Integer sort;
    private Integer status;
    private String statusText;
    private Integer auditStatus;
    private String auditStatusText;
    private String auditContent;
    private Integer shopRecommend;
    private Integer isRecycle;

    private Integer isDistribution;
    private Integer isDiscount;

    /** 操作权限标记 */
    private Integer canSale;
    private Integer canAudit;
    private Integer canReAudit;
    private Integer canDelete;
    private Integer canRecycle;
}
