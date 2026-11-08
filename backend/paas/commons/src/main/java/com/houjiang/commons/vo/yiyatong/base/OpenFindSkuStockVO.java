package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

/**
 * @ClassName OpenFindSkuStockVO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 16:58
 * @Version 1.0
 * @author Amos
 */
@Data
public class OpenFindSkuStockVO {

    /**
     * 库存数量
     */
    private String inventory;

    /**
     * skuId
     */
    private String skuId;

    /**
     * 商品状态：0、已上架1、已下架
     */
    private String status;

    private String skuCode;

    private String skuUnit;

    private String skuUnitName;

    private String skuUnitCode;

    private String skuUnitNameEn;

    private String skuUnitCodeEn;

    private String skuUnitNameCn;

    private String skuUnitCodeCn;

    private String skuUnitNameTh;
}
