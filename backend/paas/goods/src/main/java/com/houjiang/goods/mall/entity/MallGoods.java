package com.houjiang.goods.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Mall 商品主表实体
 * 表：hou_mall_goods
 * 设计参考：bbc like-admin ls_mall_goods
 * @author Amos
 */
@Data
@TableName("hou_mall_goods")
public class MallGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer createTime;
    private Integer updateTime;
    private Integer deleteTime;

    private Integer shopId;
    private String name;
    private String code;
    private String descript;
    private String categoryIds;
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

    private Integer saleNum;
    private Integer virtualSaleNum;
    private Integer visitNum;
    private Integer virtualVisitNum;
    private BigDecimal saleMoney;

    private Integer sort;
    private Integer status;
    private Integer auditStatus;
    private String auditContent;
    private Integer shopRecommend;
    private Integer isRecycle;
    private String content;

    private Integer isDiscount;
    private Integer discountType;
    private Integer isDistribution;
    private Integer distributionType;
}
