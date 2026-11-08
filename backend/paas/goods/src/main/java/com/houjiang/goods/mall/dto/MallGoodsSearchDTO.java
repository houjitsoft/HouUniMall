package com.houjiang.goods.mall.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品列表搜索参数
 * 对应 bbc MallGoodsSearchValidate + PageValidate
 * @author Amos
 */
@Data
public class MallGoodsSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页 */
    @JsonAlias({"page", "page_no"})
    private Integer pageNo = 1;
    /** 每页条数 */
    @JsonAlias({"limit", "page_size"})
    private Integer pageSize = 15;

    /** 商品名称（兼容 PC/H5 搜索字段 commonName/goodsName/kws/keyword） */
    @JsonAlias({"commonName", "goodsName", "kws", "keyword"})
    private String name;
    /** 商家名称 */
    private String shopName;
    /** 商品分类id */
    @JsonAlias({"cid", "category_id"})
    private String categoryId;
    /** 商家自定义分类id */
    @JsonAlias("shop_category_id")
    private Integer shopCategoryId;
    /** 商品状态类型 sale/not_sale/audit/audit_refuse */
    private String type;
    /** 商品状态同 type */
    private String status;
    /** 是否参与分销 */
    private Integer isDistribution;
    /** 店铺ID */
    @JsonAlias("shop_id")
    private Integer shopId;
    /** 前台语种：非 cn 时用已发布 i18n 覆盖展示名 */
    @JsonAlias({"language", "languageCode", "langCode"})
    private String lang;
}
