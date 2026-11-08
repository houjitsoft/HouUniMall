package com.houjiang.goods.dto;


import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class SupperGoodsPriceDTO extends PageUserTokenBeanDTO {

    /**
     * 商品id
     */
    private String goodsId;
    /**
     * 商品id
     */
    private String commonId;
    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;
    /**
     * 分类信息
     */
    Integer catId;

}
