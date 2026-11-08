package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

import java.util.List;

/**
 * @ClassName OpenSkuInfoBySpuIdVO
 * @Description
 * @Author Amos
 * @Date 2024/11/6 18:58
 * @Version 1.0
 * @author Amos
 */
@Data
public class OpenSkuInfoBySpuIdVO {

    /**
     * 市场价
     */
    private String officialDistriPrice;
    /**
     * 起购数量
     */
    private Integer buyStartQty;
    /**
     * 规格信息
     */
    private List<SkuSpecVO> skuPropertyList;
    /**
     * 商品logo
     */
    private String skuPicUrl;
    /**
     * 库存
     */
    private String stock;
    /**
     * SKU
     */
    private String skuId;
    /**
     * 分销价
     */
    private String basePrice;
    /**
     * 商品状态：0、已上架1、已下架
     */
    private String status;
    /**
     * 建议零售价（建议零售价仅供API客户作为销售指导价参考，请勿直接使用建议零售价字段当做平台销售价格，否则建议零售价更新会影响平台的销售价格，请建立自己的平台销售价格字段及价格体系
     */
    private String suggestPrice;



}
