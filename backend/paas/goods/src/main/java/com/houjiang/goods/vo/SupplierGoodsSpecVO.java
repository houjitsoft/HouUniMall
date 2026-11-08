package com.houjiang.goods.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName SupplierGoodsSpecVO
 * @Description
 * @Author Amos
 * @Date 2024/12/5 18:34
 * @Version 1.0
 * @author Amos
 */

@Data
public class SupplierGoodsSpecVO {

    private Integer commonId;

    private Integer goodsId;

    private String specValue;

    private BigDecimal goodsPrice;

    private BigDecimal msrp;

    private Integer catId;

    private List<GoodscCatVO> catList;

}
