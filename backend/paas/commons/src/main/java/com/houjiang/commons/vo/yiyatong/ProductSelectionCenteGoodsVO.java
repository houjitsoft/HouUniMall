package com.houjiang.commons.vo.yiyatong;

import com.houjiang.commons.vo.yiyatong.base.PropertysVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName ProductSelectionCenteGoodsVO
 * @Description ProductSelectionCenteGoodsVO
 * @Author Amos
 * @Date 2024/7/23 10:25
 * @Version 1.0
 * @author Amos
 */
@Data
public class ProductSelectionCenteGoodsVO {
    /**
     * spuId
     */
    private String supplierCommonId;
    /**
     * skuId
     */
    private String supplierGoodsId;

    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品主图
     */
    private String goodsImage;
    /**
     * 协议价
     */
    private BigDecimal agreementPrice;
    /**
     * 利润率
     */
    private String profitMargin;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺联系方式
     */
    private String shopTel;
    /**
     * 商品规格
     */
    private String goodsSpec;
    /**
     * 品牌名称
     */
    private String brandName;
    /**
     * 供应商店铺id
     */
    private Integer supplierShopId;
    /**
     * 订货编码
     */
    private String goodsCode;
    /**
     * 起订量
     */
    private Integer moq;
    /**
     * 库存
     */
    private Integer minOrder;
    /**
     * 是否 已铺货
     */
    private Boolean productListingStart = false;
    /**
     * 是否 已购买
     */
    private Boolean isBuyCount = false;
    /**
     * 是否已代发
     */
    private Boolean dropShippingStart = false;

    /**
     * 商品含税金额 未算经销折扣
     */
    private BigDecimal taxInclusiveNoDiscount;
    /**
     * 经销折扣金额
     */
    private BigDecimal dealerDiscountAmout;
    /**
     * 库存
     */
    private Integer goodsStock;

    /**
     * 属性列表
     */
    private List<PropertysVO> propertys;

    /**
     * 商品重量
     */
    private BigDecimal commonCubage;


    /**
     * 分类id
     */
    private String catId;

    /**
     * 商品详情html
     */
    private String commonBody;


}
