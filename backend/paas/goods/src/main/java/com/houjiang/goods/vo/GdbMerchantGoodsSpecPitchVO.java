package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName GdbMerchantGoodsSpecPitchVO
 * @Description GdbMerchantGoodsSpecPitchVO
 * @Author Amos
 * @Date 2023/8/15 17:13
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbMerchantGoodsSpecPitchVO {
    /**
     * 货品名称
     */
    private String goodsName;
    /**
     * 货品规格组合名称
     */
    private String goodsSpec;
    /**
     * 商品id
     */
    private String commonId;
}
